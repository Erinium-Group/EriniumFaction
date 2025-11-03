package fr.eriniumgroup.erinium_faction.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.minimap.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Overlay HUD de la minimap
 */
@EventBusSubscriber(modid = EFC.MODID, value = Dist.CLIENT)
public class MinimapOverlay {
    private static final ResourceLocation FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/ui/frame-minimap.png");
    private static final ResourceLocation PLAYER_MARKER = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/player-arrow.png");
    private static final ResourceLocation WAYPOINT_HOME = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-home.png");
    private static final ResourceLocation WAYPOINT_MINE = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-mine.png");
    private static final ResourceLocation WAYPOINT_FARM = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-farm.png");
    private static final ResourceLocation WAYPOINT_DEATH = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-death.png");
    private static final ResourceLocation WAYPOINT_CUSTOM = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-custom.png");
    private static final ResourceLocation WAYPOINT_OTHER = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-custom.png");

    private static final int PIXELS_PER_CHUNK = 16; // 1 chunk = 16x16 pixels
    private static final ChunkTextureCache chunkCache = new ChunkTextureCache(MinimapConfig.CHUNK_CACHE_SIZE);
    private static int tickCounter = 0;

    /**
     * Obtenir le cache partagé pour que fullscreen utilise le même
     */
    public static ChunkTextureCache getSharedCache() {
        return chunkCache;
    }

    // Optimisation: ne render que si le joueur a bougé
    private static BlockPos lastPlayerPos = null;
    private static int lastFrameSize = 0;
    private static boolean lastUndergroundState = false;
    private static int lastPlayerY = 0;
    private static ChunkPos lastPlayerChunk = null;

    // Sync des claims
    private static int claimSyncTimer = 0;
    private static final int CLAIM_SYNC_INTERVAL = 100; // Sync toutes les 5 secondes (100 ticks)

    /**
     * Initialiser le cache sur disque quand le joueur rejoint un monde
     */
    @SubscribeEvent
    public static void onPlayerJoinWorld(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Obtenir le nom du monde et de la dimension
        String worldName = "world"; // Nom par défaut
        try {
            // Utiliser le nom du serveur ou du monde solo
            if (mc.getCurrentServer() != null) {
                worldName = mc.getCurrentServer().name.replaceAll("[^a-zA-Z0-9_-]", "_");
            } else if (mc.level.isClientSide()) {
                worldName = "singleplayer";
            }
        } catch (Exception ignored) {}

        String dimensionName = mc.level.dimension().location().getPath();

        // Initialiser le cache sur disque
        chunkCache.initializeDiskCache(worldName, dimensionName);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Afficher la minimap même si certains écrans sont ouverts (chat, config minimap)
        if (mc.screen != null) {
            // Autoriser l'affichage pour le chat et le config de minimap
            boolean isChat = mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen;
            boolean isMinimapConfig = mc.screen instanceof fr.eriniumgroup.erinium_faction.gui.screens.MinimapConfigScreen;
            if (!isChat && !isMinimapConfig) {
                return; // Ne pas afficher pour les autres GUI
            }
        }

        // Ne pas afficher en mode debug
        try {
            if (mc.getDebugOverlay().showDebugScreen()) return;
        } catch (Exception ignored) {}

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position de la minimap
        int minimapX = MinimapConfig.minimapX;
        int minimapY = MinimapConfig.minimapY;

        // Limiter la position aux bords de l'écran
        minimapX = Math.max(0, Math.min(minimapX, screenWidth - MinimapConfig.MINIMAP_FRAME_SIZE));
        minimapY = Math.max(0, Math.min(minimapY, screenHeight - MinimapConfig.MINIMAP_FRAME_SIZE));

        // Rendu de la frame
        renderFrame(graphics, minimapX, minimapY);

        // Rendu des directions cardinales (N, S, E, W)
        renderCardinalDirections(graphics, mc, minimapX, minimapY);

        // Rendu du terrain
        renderTerrain(graphics, mc, minimapX, minimapY);

        // Rendu des claims
        renderClaims(graphics, mc, minimapX, minimapY);

        // Rendu des waypoints
        renderWaypoints(graphics, mc, minimapX, minimapY);

        // Rendu du joueur au centre
        renderPlayerMarker(graphics, mc.player, minimapX, minimapY);

        // Rendu des membres de faction (si dans une faction)
        renderFactionMembers(graphics, mc, minimapX, minimapY);

        // Sync des claims périodiquement
        claimSyncTimer++;
        if (claimSyncTimer >= CLAIM_SYNC_INTERVAL) {
            claimSyncTimer = 0;
            requestClaimsUpdate(mc);
        }

        // Incrémenter le compteur de ticks
        tickCounter++;
    }

    /**
     * Demande une mise à jour des claims autour du joueur
     */
    private static void requestClaimsUpdate(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        int centerCx = playerPos.getX() >> 4;
        int centerCz = playerPos.getZ() >> 4;
        int radius = 10; // 10 chunks de rayon

        String dimension = mc.level.dimension().location().toString();

        // Envoyer le request au serveur
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
            new fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage(
                dimension, centerCx, centerCz, radius
            )
        );
    }

    private static void renderFrame(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        graphics.blit(FRAME_TEXTURE, x, y, 0, 0, MinimapConfig.MINIMAP_FRAME_SIZE, MinimapConfig.MINIMAP_FRAME_SIZE,
                     MinimapConfig.MINIMAP_FRAME_SIZE, MinimapConfig.MINIMAP_FRAME_SIZE);
        RenderSystem.disableBlend();
    }

    private static void renderCardinalDirections(GuiGraphics graphics, Minecraft mc, int minimapX, int minimapY) {
        if (mc.player == null) return;

        int centerX = minimapX + MinimapConfig.MINIMAP_FRAME_SIZE / 2;
        int centerY = minimapY + MinimapConfig.MINIMAP_FRAME_SIZE / 2;
        int radius = MinimapConfig.MINIMAP_FRAME_SIZE / 2 - 15; // Distance depuis le centre

        // Positions FIXES - N toujours en haut, pas de rotation
        // Nord en haut (0°)
        graphics.drawString(mc.font, "N", centerX - mc.font.width("N") / 2, minimapY + 12, 0xFFFFFFFF, true);

        // Est à droite (90°)
        graphics.drawString(mc.font, "E", minimapX + MinimapConfig.MINIMAP_FRAME_SIZE - 18, centerY - mc.font.lineHeight / 2, 0xFFCCCCCC, true);

        // Sud en bas (180°)
        graphics.drawString(mc.font, "S", centerX - mc.font.width("S") / 2, minimapY + MinimapConfig.MINIMAP_FRAME_SIZE - 20, 0xFFCCCCCC, true);

        // Ouest à gauche (270°)
        graphics.drawString(mc.font, "W", minimapX + 12, centerY - mc.font.lineHeight / 2, 0xFFCCCCCC, true);
    }

    private static void renderTerrain(GuiGraphics graphics, Minecraft mc, int minimapX, int minimapY) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        Level level = mc.level;
        if (level == null) return;

        // Le padding de la frame est de 10px de chaque côté
        // Donc la zone de rendu commence à (minimapX + 10, minimapY + 10)
        // et se termine à (minimapX + FRAME_SIZE - 10, minimapY + FRAME_SIZE - 10)
        int padding = 6;
        int mapX = minimapX + padding;
        int mapY = minimapY + padding;
        int mapSize = MinimapConfig.MINIMAP_FRAME_SIZE - (padding * 2);

        // Centre de la minimap = centre de la frame
        int centerX = minimapX + MinimapConfig.MINIMAP_FRAME_SIZE / 2;
        int centerY = minimapY + MinimapConfig.MINIMAP_FRAME_SIZE / 2;

        // Position du joueur
        BlockPos playerPos = player.blockPosition();
        ChunkPos currentChunk = new ChunkPos(playerPos);
        boolean underground = isUnderground(level, playerPos);

        // Détecter si le joueur a changé de chunk
        boolean changedChunk = lastPlayerChunk == null || !lastPlayerChunk.equals(currentChunk);

        // Détecter le changement de Y-level significatif (10+ blocs)
        int yDiff = Math.abs(playerPos.getY() - lastPlayerY);
        boolean significantYChange = yDiff >= 10;

        // Si le joueur a changé de chunk, NE RIEN FAIRE
        // Les chunks seront automatiquement générés à la demande lors du rendu
        // Marquer dirty causait l'affichage d'anciennes textures aux mauvaises positions
        if (changedChunk) {
            lastPlayerChunk = currentChunk;
            // Pas besoin de marquer dirty - les nouveaux chunks seront générés automatiquement
        }

        // Si le mode underground a changé OU si le joueur a bougé verticalement de façon significative
        if (underground != lastUndergroundState || significantYChange) {
            // Marquer dirty pour forcer la régénération car le contenu a vraiment changé
            for (int cx = -MinimapConfig.CHUNK_UPDATE_RADIUS; cx <= MinimapConfig.CHUNK_UPDATE_RADIUS; cx++) {
                for (int cz = -MinimapConfig.CHUNK_UPDATE_RADIUS; cz <= MinimapConfig.CHUNK_UPDATE_RADIUS; cz++) {
                    chunkCache.markDirty(new ChunkPos(currentChunk.x + cx, currentChunk.z + cz), underground);
                }
            }
            lastUndergroundState = underground;
            lastPlayerY = playerPos.getY();
        }

        // Optimisation: vérifier si on doit recalculer
        boolean playerMoved = lastPlayerPos == null || !lastPlayerPos.equals(playerPos);
        boolean sizeChanged = lastFrameSize != MinimapConfig.MINIMAP_FRAME_SIZE;

        lastPlayerPos = playerPos.immutable();
        lastFrameSize = MinimapConfig.MINIMAP_FRAME_SIZE;

        // Calculer les chunks visibles (réduit à 6 chunks de rayon pour performance)
        int chunkRenderDist = 6; // RÉDUIT de 8 à 6 pour meilleure performance
        ChunkPos playerChunk = new ChunkPos(playerPos);

        // Activer le clipping pour empêcher le terrain de déborder
        graphics.enableScissor(mapX, mapY, mapX + mapSize, mapY + mapSize);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Initialiser le cache si besoin (centre sur le joueur)
        chunkCache.initializeIfNeeded(playerChunk);

        // Générer les chunks visibles dans la grande texture (max 4 par frame pour meilleure perf)
        int chunksGenerated = 0;
        int maxChunksPerFrame = 4;

        for (int chunkX = -chunkRenderDist; chunkX <= chunkRenderDist; chunkX++) {
            for (int chunkZ = -chunkRenderDist; chunkZ <= chunkRenderDist; chunkZ++) {
                if (chunksGenerated >= maxChunksPerFrame) break;

                ChunkPos chunkPos = new ChunkPos(playerChunk.x + chunkX, playerChunk.z + chunkZ);
                chunkCache.renderChunkAt(chunkPos, level, underground);
                chunksGenerated++;
            }
            if (chunksGenerated >= maxChunksPerFrame) break;
        }

        // Obtenir la texture
        ResourceLocation textureId = chunkCache.getTextureId(underground);
        ChunkTextureCache.TextureCoords coords = chunkCache.getTextureCoords(playerChunk);

        // Calculer la position exacte du joueur en pixels dans la texture
        float playerPixelX = (playerChunk.x - chunkCache.getOriginChunkX()) * PIXELS_PER_CHUNK +
                            (float)(playerPos.getX() - playerChunk.x * 16);
        float playerPixelZ = (playerChunk.z - chunkCache.getOriginChunkZ()) * PIXELS_PER_CHUNK +
                            (float)(playerPos.getZ() - playerChunk.z * 16);

        // Taille de la zone visible en pixels dans la texture
        float visibleRadiusPixels = (mapSize / 2.0f) / MinimapConfig.currentZoom;

        // Calculer les UV pour afficher la zone autour du joueur
        float uMin = (playerPixelX - visibleRadiusPixels) / coords.totalWidth();
        float vMin = (playerPixelZ - visibleRadiusPixels) / coords.totalHeight();
        float uMax = (playerPixelX + visibleRadiusPixels) / coords.totalWidth();
        float vMax = (playerPixelZ + visibleRadiusPixels) / coords.totalHeight();

        // Convertir UV (0-1) en coordonnées de pixels dans la texture
        int texU = (int)(uMin * coords.totalWidth());
        int texV = (int)(vMin * coords.totalHeight());
        int texWidth = (int)((uMax - uMin) * coords.totalWidth());
        int texHeight = (int)((vMax - vMin) * coords.totalHeight());

        // Dessiner la portion de texture
        graphics.blit(textureId, mapX, mapY, texU, texV, mapSize, mapSize, coords.totalWidth(), coords.totalHeight());

        RenderSystem.disableBlend();

        // Désactiver le clipping
        graphics.disableScissor();
    }

    private static boolean isUnderground(Level level, BlockPos pos) {
        // Logique: On est underground si on est VRAIMENT sous terre
        // 1. Si en dessous de Y=55 (niveau des grottes) -> underground
        // 2. OU si on a des blocs SOLIDES PLEINS au-dessus de nous (pas water, leaves, etc)

        boolean belowCaveLevel = pos.getY() < 55;
        if (belowCaveLevel) {
            return true; // Toujours underground si sous Y=55
        }

        // Vérifier s'il y a des blocs solides pleins au-dessus
        // On cherche jusqu'à 10 blocs au-dessus pour détecter un "toit"
        int solidBlocksAbove = 0;
        BlockPos.MutableBlockPos checkPos = pos.mutable();

        for (int y = 1; y <= 10; y++) {
            checkPos.set(pos.getX(), pos.getY() + y, pos.getZ());
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(checkPos);

            // Vérifier si c'est un bloc solide plein (pas water, leaves, glass, etc)
            if (!state.isAir() && state.isSolidRender(level, checkPos)) {
                solidBlocksAbove++;
            }
        }

        // On considère qu'on est underground si on a au moins 3 blocs solides pleins au-dessus
        return solidBlocksAbove >= 3;
    }

    private static void renderClaims(GuiGraphics graphics, Minecraft mc, int minimapX, int minimapY) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        // Activer le clipping pour empêcher de déborder
        int padding = 2;
        int mapX = minimapX + padding;
        int mapY = minimapY + padding;
        int mapSize = MinimapConfig.MINIMAP_FRAME_SIZE - (padding * 2);
        graphics.enableScissor(mapX, mapY, mapX + mapSize, mapY + mapSize);

        int centerX = minimapX + MinimapConfig.MINIMAP_FRAME_SIZE / 2;
        int centerY = minimapY + MinimapConfig.MINIMAP_FRAME_SIZE / 2;

        BlockPos playerPos = player.blockPosition();
        int renderDist = MinimapConfig.RENDER_DISTANCE_CHUNKS / 2;

        for (int chunkX = -renderDist; chunkX <= renderDist; chunkX++) {
            for (int chunkZ = -renderDist; chunkZ <= renderDist; chunkZ++) {
                int worldChunkX = (playerPos.getX() >> 4) + chunkX;
                int worldChunkZ = (playerPos.getZ() >> 4) + chunkZ;

                ChunkPos chunkPos = new ChunkPos(worldChunkX, worldChunkZ);
                ClaimRenderHelper.ClaimInfo claimInfo = ClaimRenderHelper.getClaimInfo(chunkPos, player.level().dimension().location().toString());

                if (claimInfo != null) {
                    float offsetX = (worldChunkX * 16 - playerPos.getX()) * MinimapConfig.currentZoom;
                    float offsetZ = (worldChunkZ * 16 - playerPos.getZ()) * MinimapConfig.currentZoom;

                    int renderX = (int) (centerX + offsetX);
                    int renderY = (int) (centerY + offsetZ);
                    int size = (int) (16 * MinimapConfig.currentZoom);

                    ClaimRenderHelper.ClaimRelation relation = ClaimRenderHelper.getClaimRelation(claimInfo, player);
                    ClaimRenderHelper.renderClaimOverlayDynamic(graphics, renderX, renderY, size, claimInfo, relation);
                }
            }
        }

        graphics.disableScissor();
    }

    private static void renderWaypoints(GuiGraphics graphics, Minecraft mc, int minimapX, int minimapY) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        List<Waypoint> waypoints = WaypointManager.getInstance().getWaypointsForDimension(player.level().dimension());

        int centerX = minimapX + MinimapConfig.MINIMAP_FRAME_SIZE / 2;
        int centerY = minimapY + MinimapConfig.MINIMAP_FRAME_SIZE / 2;

        BlockPos playerPos = player.blockPosition();

        RenderSystem.enableBlend();

        for (Waypoint waypoint : waypoints) {
            // Skip disabled waypoints
            if (!waypoint.isEnabled()) {
                continue;
            }

            BlockPos waypointPos = waypoint.getPosition();

            // Calculer position relative
            float offsetX = (waypointPos.getX() - playerPos.getX()) * MinimapConfig.currentZoom;
            float offsetZ = (waypointPos.getZ() - playerPos.getZ()) * MinimapConfig.currentZoom;

            int renderX = (int) (centerX + offsetX);
            int renderY = (int) (centerY + offsetZ);

            // Limiter à la zone visible
            int mapSize = MinimapConfig.MINIMAP_SIZE;
            int mapX = minimapX + 10;
            int mapY = minimapY + 10;

            if (renderX >= mapX - 8 && renderX <= mapX + mapSize &&
                renderY >= mapY - 8 && renderY <= mapY + mapSize) {

                // Draw colored dot (small circle)
                int color = waypoint.getEffectiveColor();
                int radius = 2;

                // Draw filled circle (simple approach with multiple fills)
                for (int r = 0; r <= radius; r++) {
                    int size = (int) Math.sqrt(radius * radius - r * r);
                    graphics.fill(renderX - size, renderY - r, renderX + size + 1, renderY - r + 1, color);
                    if (r != 0) {
                        graphics.fill(renderX - size, renderY + r, renderX + size + 1, renderY + r + 1, color);
                    }
                }
            }
        }

        RenderSystem.disableBlend();
    }

    private static ResourceLocation getWaypointIcon(Waypoint.WaypointColor color) {
        return switch (color) {
            case RED -> WAYPOINT_HOME;
            case BLUE -> WAYPOINT_MINE;
            case GREEN -> WAYPOINT_FARM;
            case PURPLE -> WAYPOINT_DEATH;
            case ORANGE -> WAYPOINT_OTHER;
            case YELLOW -> WAYPOINT_OTHER;
            case CUSTOM -> WAYPOINT_CUSTOM;
        };
    }

    private static void renderPlayerMarker(GuiGraphics graphics, LocalPlayer player, int minimapX, int minimapY) {
        int centerX = minimapX + MinimapConfig.MINIMAP_FRAME_SIZE / 2;
        int centerY = minimapY + MinimapConfig.MINIMAP_FRAME_SIZE / 2;

        RenderSystem.enableBlend();

        // Appliquer la rotation basée sur la direction du joueur (yaw)
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // Translater au centre du joueur
        poseStack.translate(centerX, centerY, 0);

        // Appliquer la rotation (yaw + 180 car la texture pointe vers le haut par défaut)
        float rotation = (player.getYRot() + 180) % 360;
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));

        // Render la texture centrée
        graphics.blit(PLAYER_MARKER, -8, -8, 0, 0, 16, 16, 16, 16);

        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    private static void renderFactionMembers(GuiGraphics graphics, Minecraft mc, int minimapX, int minimapY) {
        // TODO: Afficher les membres de faction
        // Sera implémenté après vérification du système de faction
    }

    public static void onChunkChanged(ChunkPos chunkPos) {
        // Marquer dirty pour BOTH caches (surface et underground)
        chunkCache.markDirty(chunkPos, false); // surface
        chunkCache.markDirty(chunkPos, true);  // underground
    }

    public static void clearCache() {
        chunkCache.clear();
    }
}
