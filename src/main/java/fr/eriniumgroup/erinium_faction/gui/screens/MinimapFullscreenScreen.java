package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.common.network.packets.ChunkClaimPacket;
import fr.eriniumgroup.erinium_faction.common.network.packets.WaypointActionPacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.minimap.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI fullscreen simplifié pour la minimap
 */
public class MinimapFullscreenScreen extends Screen {
    // Dimensions maximales (réduites drastiquement pour éviter chevauchement avec waypoints/boutons)
    private static final int MAX_GUI_WIDTH = 280;
    private static final int MAX_GUI_HEIGHT = 190;

    // Dimensions calculées
    private int guiWidth;
    private int guiHeight;
    private int guiLeft;
    private int guiTop;
    private float guiScale;

    // Position et zoom
    private float mapCenterX = 0;
    private float mapCenterZ = 0;
    private float mapZoom = 1.0f;

    // Dragging pour pan
    private boolean isDragging = false;
    private int dragStartX = 0;
    private int dragStartY = 0;
    private float dragStartMapX = 0;
    private float dragStartMapZ = 0;

    // Dragging pour claim/unclaim
    private boolean isDraggingClaim = false;
    private ChunkPos dragClaimStart = null;
    private ChunkPos dragClaimEnd = null;
    private boolean dragClaimMode = true; // true = claim, false = unclaim

    // Waypoint editor
    private WaypointEditorPopup waypointEditor = null;

    // Context menu
    private ChunkContextMenu contextMenu = null;

    // Cache partagé avec l'overlay (pour que les chunks soient déjà générés)
    private final ChunkTextureCache chunkCache = fr.eriniumgroup.erinium_faction.client.overlay.MinimapOverlay.getSharedCache();

    // Flag pour forcer le refresh des claims
    private boolean needsClaimRefresh = false;

    // Mode d'affichage : surface ou underground
    private boolean viewUnderground = false;

    // Pas de génération - seulement affichage des chunks en cache

    public MinimapFullscreenScreen() {
        super(Component.translatable("gui.erinium_faction.minimap.title"));
    }

    @Override
    protected void init() {
        super.init();

        // Les chunks sont déjà préchargés automatiquement lors de l'initialisation du cache
        // (voir ChunkTextureCache.initializeDiskCache() -> preloadCachedChunks())

        // Enregistrer un listener pour être notifié des changements de claims
        ClaimRenderHelper.setClaimChangeListener(() -> {
            needsClaimRefresh = true;
        });

        // Par défaut, taille de base 400x270 (scale 1.0)
        guiScale = 1.0f;

        // Si l'écran est plus petit que 400x270, on scale DOWN
        if (width < MAX_GUI_WIDTH || height < MAX_GUI_HEIGHT) {
            float scaleX = (float) width / MAX_GUI_WIDTH;
            float scaleY = (float) height / MAX_GUI_HEIGHT;
            guiScale = Math.min(scaleX, scaleY);
        }
        // Si l'écran est plus grand que 400x270, on peut scale UP jusqu'à 2x max
        else if (width > MAX_GUI_WIDTH && height > MAX_GUI_HEIGHT) {
            float scaleX = (float) width / MAX_GUI_WIDTH;
            float scaleY = (float) height / MAX_GUI_HEIGHT;
            float maxScale = Math.min(scaleX, scaleY);
            guiScale = Math.min(maxScale, 2.0f);
        }

        guiWidth = (int) (MAX_GUI_WIDTH * guiScale);
        guiHeight = (int) (MAX_GUI_HEIGHT * guiScale);
        guiLeft = (width - guiWidth) / 2;
        guiTop = (height - guiHeight) / 2;

        // Centrer sur le joueur
        if (minecraft != null && minecraft.player != null) {
            mapCenterX = (float) minecraft.player.getX();
            mapCenterZ = (float) minecraft.player.getZ();
        }

        // Bouton fermer (top-right)
        addRenderableWidget(Button.builder(Component.literal("X"), btn -> onClose())
            .bounds(guiLeft + guiWidth - 25, guiTop + 5, 20, 20)
            .build());

        // Boutons dans la zone de gauche
        int leftBtnX = guiLeft + 10;
        int leftBtnY = guiTop + 30;

        // Bouton Waypoints
        addRenderableWidget(Button.builder(Component.literal("Waypoints"), btn -> {
            openWaypointListGUI();
        }).bounds(leftBtnX, leftBtnY, 70, 18).build());

        // Bouton toggle Surface/Underground
        addRenderableWidget(Button.builder(
            Component.literal(viewUnderground ? "Underground" : "Surface"),
            btn -> {
                viewUnderground = !viewUnderground;
                btn.setMessage(Component.literal(viewUnderground ? "Underground" : "Surface"));
            }
        ).bounds(leftBtnX, leftBtnY + 25, 70, 18).build());

        // Bouton Config
        addRenderableWidget(Button.builder(Component.literal("Config"), btn -> {
            if (minecraft != null) {
                minecraft.setScreen(new MinimapConfigScreen(this));
            }
        }).bounds(guiLeft + guiWidth - 60, guiTop + guiHeight - 25, 50, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - pas de flou, pas de fond noir
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render background first (sans flou)
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // IMPORTANT: Si les claims ont changé, on force un refresh complet
        if (needsClaimRefresh) {
            needsClaimRefresh = false;
            // Le cache a déjà été mis à jour par ClaimRenderHelper.updateClaimsFromPacket
            // On n'a rien à faire de plus - le prochain render utilisera les nouvelles données
        }

        // Fond du GUI
        graphics.fill(guiLeft, guiTop, guiLeft + guiWidth, guiTop + guiHeight, 0xDD1a1a1a);

        // Bordure dorée
        graphics.fill(guiLeft - 2, guiTop - 2, guiLeft + guiWidth + 2, guiTop, 0xFFd4af37);
        graphics.fill(guiLeft - 2, guiTop + guiHeight, guiLeft + guiWidth + 2, guiTop + guiHeight + 2, 0xFFd4af37);
        graphics.fill(guiLeft - 2, guiTop, guiLeft, guiTop + guiHeight, 0xFFd4af37);
        graphics.fill(guiLeft + guiWidth, guiTop, guiLeft + guiWidth + 2, guiTop + guiHeight, 0xFFd4af37);

        // Title
        graphics.drawCenteredString(font, title, guiLeft + guiWidth / 2, guiTop + 10, 0xFFffd700);

        // Render map
        renderMap(graphics, mouseX, mouseY);

        // Render widgets (buttons)
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render waypoint list
        renderWaypointList(graphics);

        // Render waypoint editor popup
        if (waypointEditor != null) {
            waypointEditor.render(graphics, mouseX, mouseY, partialTick);
        }

        // Render context menu
        if (contextMenu != null) {
            contextMenu.render(graphics, mouseX, mouseY);
        }

        // Render tooltips (at the very end so they're on top)
        if (contextMenu == null && waypointEditor == null) {
            renderTooltips(graphics, mouseX, mouseY);
        }
    }

    private void renderMap(GuiGraphics graphics, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) return;

        // Marges réduites drastiquement pour maximiser la map
        int mapX = guiLeft + 8;  // Marge gauche minimale (pour waypoint list)
        int mapY = guiTop + 25;  // Marge haut minimale (pour titre)
        int mapWidth = guiWidth - 16;  // Marges gauche + droite = 8+8
        int mapHeight = guiHeight - 32; // Marges haut + bas = 25+7

        // Center
        int centerX = mapX + mapWidth / 2;
        int centerY = mapY + mapHeight / 2;

        // Scissors pour limiter le rendu
        graphics.enableScissor(mapX, mapY, mapX + mapWidth, mapY + mapHeight);

        // Render terrain avec textures (rapide)
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        renderTerrainChunks(graphics, centerX, centerY, mapX, mapY, mapWidth, mapHeight);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();

        // Render chunk grid
        renderChunkGrid(graphics, centerX, centerY, mapX, mapY, mapWidth, mapHeight);

        // Render claims
        renderClaims(graphics, centerX, centerY, mapX, mapY, mapWidth, mapHeight);

        // Render claim drag selection
        if (isDraggingClaim && dragClaimStart != null && dragClaimEnd != null) {
            renderClaimDragSelection(graphics, centerX, centerY, mapX, mapY, mapWidth, mapHeight);
        }

        // Render waypoints
        renderWaypoints(graphics, centerX, centerY, mapX, mapY, mapWidth, mapHeight);

        // Render player marker (position FIXE au centre, ne bouge PAS avec le pan)
        if (minecraft != null && minecraft.player != null) {
            BlockPos playerPos = minecraft.player.blockPosition();

            // Calculer la position du joueur par rapport au centre de la map
            float offsetX = (playerPos.getX() - mapCenterX) * mapZoom;
            float offsetZ = (playerPos.getZ() - mapCenterZ) * mapZoom;

            int playerRenderX = (int) (centerX + offsetX);
            int playerRenderY = (int) (centerY + offsetZ);

            // Render seulement si le joueur est visible dans la map
            if (playerRenderX >= mapX && playerRenderX <= mapX + mapWidth &&
                playerRenderY >= mapY && playerRenderY <= mapY + mapHeight) {
                ResourceLocation playerMarker = ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/player-default.png");
                graphics.blit(playerMarker, playerRenderX - 4, playerRenderY - 4, 0, 0, 8, 8, 8, 8);
            }
        }

        graphics.disableScissor();

        // Border
        graphics.fill(mapX - 1, mapY - 1, mapX + mapWidth + 1, mapY, 0xFF666666);
        graphics.fill(mapX - 1, mapY + mapHeight, mapX + mapWidth + 1, mapY + mapHeight + 1, 0xFF666666);
        graphics.fill(mapX - 1, mapY, mapX, mapY + mapHeight, 0xFF666666);
        graphics.fill(mapX + mapWidth, mapY, mapX + mapWidth + 1, mapY + mapHeight, 0xFF666666);
    }

    private void renderTerrainChunks(GuiGraphics graphics, int centerX, int centerY, int mapX, int mapY, int mapWidth, int mapHeight) {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            return;
        }

        int chunkSize = (int) (16 * mapZoom);

        // Calculer les chunks visibles à l'écran
        int centerChunkX = (int) (mapCenterX / 16);
        int centerChunkZ = (int) (mapCenterZ / 16);

        // Afficher TOUS les chunks en cache (pas de limite de rayon)
        // On itère sur tous les chunks disponibles dans le cache
        for (var entry : chunkCache.getAllCached(viewUnderground).entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            ChunkTextureCache.CachedChunkTexture cached = entry.getValue();

            // Calculer position de rendu (utiliser Math.round pour éviter les gaps)
            float offsetX = (chunkPos.x * 16 - mapCenterX) * mapZoom;
            float offsetZ = (chunkPos.z * 16 - mapCenterZ) * mapZoom;

            int renderX = Math.round(centerX + offsetX);
            int renderY = Math.round(centerY + offsetZ);

            // Skip si vraiment hors écran (avec marge)
            if (renderX + chunkSize < mapX - 16 || renderX > mapX + mapWidth + 16 ||
                renderY + chunkSize < mapY - 16 || renderY > mapY + mapHeight + 16) {
                continue;
            }

            // Render exactement à la bonne taille (pas de +1 qui cause des overlaps)
            graphics.blit(cached.getTextureId(), renderX, renderY, 0, 0, chunkSize, chunkSize, chunkSize, chunkSize);
        }
    }

    private boolean isUnderground(Level level, BlockPos pos) {
        boolean belowCaveLevel = pos.getY() < 55;
        boolean noSky = !level.canSeeSky(pos);
        return belowCaveLevel || noSky;
    }

    private void renderChunkGrid(GuiGraphics graphics, int centerX, int centerY, int mapX, int mapY, int mapWidth, int mapHeight) {
        if (minecraft == null) return;

        int chunkSize = (int) (16 * mapZoom);
        int renderDist = 16;

        // Couleur de la grille (blanc très transparent)
        int gridColor = 0x20FFFFFF; // Très fin et transparent (~12% opacity)

        for (int chunkX = -renderDist; chunkX <= renderDist; chunkX++) {
            for (int chunkZ = -renderDist; chunkZ <= renderDist; chunkZ++) {
                int worldChunkX = (int) (mapCenterX / 16) + chunkX;
                int worldChunkZ = (int) (mapCenterZ / 16) + chunkZ;

                float offsetX = (worldChunkX * 16 - mapCenterX) * mapZoom;
                float offsetZ = (worldChunkZ * 16 - mapCenterZ) * mapZoom;

                int renderX = (int) (centerX + offsetX);
                int renderY = (int) (centerY + offsetZ);

                if (renderX + chunkSize >= mapX && renderX <= mapX + mapWidth &&
                    renderY + chunkSize >= mapY && renderY <= mapY + mapHeight) {

                    // Dessiner seulement les bordures haut et gauche (pour éviter double ligne)
                    graphics.fill(renderX, renderY, renderX + chunkSize, renderY + 1, gridColor);
                    graphics.fill(renderX, renderY, renderX + 1, renderY + chunkSize, gridColor);
                }
            }
        }
    }

    private void renderClaims(GuiGraphics graphics, int centerX, int centerY, int mapX, int mapY, int mapWidth, int mapHeight) {
        if (minecraft == null || minecraft.player == null) return;

        int chunkSize = (int) (16 * mapZoom);
        String dimension = minecraft.player.level().dimension().location().toString();

        // Position du joueur en chunks
        int playerChunkX = minecraft.player.blockPosition().getX() >> 4;
        int playerChunkZ = minecraft.player.blockPosition().getZ() >> 4;
        int maxDistanceChunks = 10; // Distance max pour claims ennemis/alliés

        // Parcourir TOUS les claims du cache au lieu d'un rayon fixe
        for (var entry : ClaimRenderHelper.getAllClaims().entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            ClaimRenderHelper.ClaimInfo claimInfo = entry.getValue();

            if (claimInfo == null) continue;

            // Vérifier la dimension
            if (!claimInfo.dimension().equals(dimension)) continue;

            // Calculer la relation avec ce claim
            ClaimRenderHelper.ClaimRelation relation = ClaimRenderHelper.getClaimRelation(claimInfo, minecraft.player);

            // Calculer la distance depuis le joueur
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            int distanceChunks = Math.max(dx, dz);

            // Filtrer selon la distance SAUF pour safezone, warzone et nos propres claims
            boolean isSpecialClaim = claimInfo.factionId().equalsIgnoreCase("safezone") ||
                                     claimInfo.factionId().equalsIgnoreCase("warzone") ||
                                     relation == ClaimRenderHelper.ClaimRelation.OWNED;

            if (!isSpecialClaim && distanceChunks > maxDistanceChunks) {
                continue; // Trop loin, skip ce claim
            }

            // Calculer la position de rendu
            float offsetX = (chunkPos.x * 16 - mapCenterX) * mapZoom;
            float offsetZ = (chunkPos.z * 16 - mapCenterZ) * mapZoom;

            int renderX = (int) (centerX + offsetX);
            int renderY = (int) (centerY + offsetZ);

            // Vérifier si visible à l'écran
            if (renderX + chunkSize >= mapX && renderX <= mapX + mapWidth &&
                renderY + chunkSize >= mapY && renderY <= mapY + mapHeight) {

                ClaimRenderHelper.renderClaimOverlayDynamic(graphics, renderX, renderY, chunkSize, claimInfo, relation);
            }
        }
    }

    private void renderClaimDragSelection(GuiGraphics graphics, int centerX, int centerY, int mapX, int mapY, int mapWidth, int mapHeight) {
        int minX = Math.min(dragClaimStart.x, dragClaimEnd.x);
        int maxX = Math.max(dragClaimStart.x, dragClaimEnd.x);
        int minZ = Math.min(dragClaimStart.z, dragClaimEnd.z);
        int maxZ = Math.max(dragClaimStart.z, dragClaimEnd.z);

        int chunkSize = (int) (16 * mapZoom);

        // Couleur selon le mode (vert pour claim, rouge pour unclaim)
        int fillColor = dragClaimMode ? 0x4000FF00 : 0x40FF0000;
        int borderColor = dragClaimMode ? 0xFF00FF00 : 0xFFFF0000;

        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                float offsetX = (chunkX * 16 - mapCenterX) * mapZoom;
                float offsetZ = (chunkZ * 16 - mapCenterZ) * mapZoom;

                int renderX = (int) (centerX + offsetX);
                int renderY = (int) (centerY + offsetZ);

                if (renderX + chunkSize >= mapX && renderX <= mapX + mapWidth &&
                    renderY + chunkSize >= mapY && renderY <= mapY + mapHeight) {

                    // Remplissage semi-transparent
                    graphics.fill(renderX, renderY, renderX + chunkSize, renderY + chunkSize, fillColor);

                    // Bordure
                    graphics.fill(renderX, renderY, renderX + chunkSize, renderY + 2, borderColor);
                    graphics.fill(renderX, renderY + chunkSize - 2, renderX + chunkSize, renderY + chunkSize, borderColor);
                    graphics.fill(renderX, renderY, renderX + 2, renderY + chunkSize, borderColor);
                    graphics.fill(renderX + chunkSize - 2, renderY, renderX + chunkSize, renderY + chunkSize, borderColor);
                }
            }
        }

        // Afficher le nombre de chunks sélectionnés
        int count = (maxX - minX + 1) * (maxZ - minZ + 1);
        String text = (dragClaimMode ? "§aClaim: " : "§cUnclaim: ") + count + " chunks";
        graphics.drawString(font, Component.literal(text), mapX + 5, mapY + 5, 0xFFFFFFFF, true);
    }

    private void renderWaypoints(GuiGraphics graphics, int centerX, int centerY, int mapX, int mapY, int mapWidth, int mapHeight) {
        if (minecraft == null || minecraft.player == null) return;

        List<Waypoint> waypoints = WaypointManager.getInstance().getWaypointsForDimension(minecraft.player.level().dimension());

        for (Waypoint waypoint : waypoints) {
            // Skip disabled waypoints
            if (!waypoint.isEnabled()) {
                continue;
            }

            BlockPos pos = waypoint.getPosition();

            float offsetX = (pos.getX() - mapCenterX) * mapZoom;
            float offsetZ = (pos.getZ() - mapCenterZ) * mapZoom;

            int renderX = (int) (centerX + offsetX);
            int renderY = (int) (centerY + offsetZ);

            if (renderX >= mapX - 8 && renderX <= mapX + mapWidth &&
                renderY >= mapY - 8 && renderY <= mapY + mapHeight) {

                // Draw colored dot (small circle)
                int color = waypoint.getEffectiveColor();
                int radius = 3;

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
    }

    private void renderWaypointList(GuiGraphics graphics) {
        if (minecraft == null || minecraft.player == null) return;

        List<Waypoint> waypoints = WaypointManager.getInstance().getWaypointsForDimension(minecraft.player.level().dimension());

        int x = guiLeft + 10;
        int y = guiTop + 120;
        int maxY = guiTop + guiHeight - 10;

        graphics.drawString(font, "Waypoints:", x, y, 0xFFffd700, false);
        y += 12;

        int count = 0;
        for (Waypoint waypoint : waypoints) {
            // Skip disabled waypoints
            if (!waypoint.isEnabled()) {
                continue;
            }

            if (y >= maxY) break;
            if (count >= 5) break;

            // Couleur indicator
            graphics.fill(x, y, x + 6, y + 6, waypoint.getEffectiveColor());

            // Nom
            String name = waypoint.getName();
            if (name.length() > 10) name = name.substring(0, 10) + "...";
            graphics.drawString(font, name, x + 8, y, 0xFFFFFFFF, false);

            y += 10;
            count++;
        }
    }

    private ResourceLocation getWaypointIcon(Waypoint.WaypointColor color) {
        return switch (color) {
            case RED -> ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-home.png");
            case BLUE -> ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-mine.png");
            case GREEN -> ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-farm.png");
            case PURPLE -> ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-death.png");
            case ORANGE -> ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-custom.png");
            case YELLOW -> ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-custom.png");
            case CUSTOM -> ResourceLocation.fromNamespaceAndPath(EFC.MODID, "textures/minimap/markers/waypoint-custom.png");
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Context menu priority
        if (contextMenu != null) {
            if (contextMenu.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Waypoint editor
        if (waypointEditor != null) {
            if (waypointEditor.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Check if any button was clicked first (widgets have priority)
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int mapX = guiLeft + 65;
        int mapY = guiTop + 30;
        int mapWidth = guiWidth - 140;
        int mapHeight = guiHeight - 40;

        // Clic dans la zone de carte (only if no widget consumed the click)
        if (mouseX >= mapX && mouseX <= mapX + mapWidth &&
            mouseY >= mapY && mouseY <= mapY + mapHeight) {

            if (button == 0) { // Left click
                boolean shiftDown = hasShiftDown();
                boolean ctrlDown = hasControlDown();

                // SHIFT + Left click = start claim drag
                if (shiftDown && !ctrlDown) {
                    startClaimDrag(mouseX, mouseY, true);
                    return true;
                }
                // CTRL + Left click = start unclaim drag
                else if (ctrlDown && !shiftDown) {
                    startClaimDrag(mouseX, mouseY, false);
                    return true;
                }
                // Normal left click (no modifier) = pan map
                else if (!shiftDown && !ctrlDown) {
                    isDragging = true;
                    dragStartX = (int) mouseX;
                    dragStartY = (int) mouseY;
                    dragStartMapX = mapCenterX;
                    dragStartMapZ = mapCenterZ;
                    return true;
                }
            } else if (button == 1) { // Right click
                // Check if clicked on a waypoint first
                Waypoint clickedWaypoint = getWaypointAtMouse(mouseX, mouseY);
                if (clickedWaypoint != null) {
                    openWaypointEditor(clickedWaypoint);
                    return true;
                }
                // Otherwise open context menu for chunk
                openContextMenu(mouseX, mouseY);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // PRIORITY: claim drag d'abord
        if (isDraggingClaim && button == 0) {
            updateClaimDrag(mouseX, mouseY);
            return true;
        }

        // Pan de la map seulement si PAS en mode claim
        if (isDragging && button == 0 && !isDraggingClaim) {
            float deltaX = (float) (mouseX - dragStartX);
            float deltaY = (float) (mouseY - dragStartY);

            mapCenterX = dragStartMapX - (deltaX / mapZoom);
            mapCenterZ = dragStartMapZ - (deltaY / mapZoom);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isDraggingClaim) {
                finishClaimDrag();
            }
            isDragging = false;
            isDraggingClaim = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            mapZoom = Math.min(4.0f, mapZoom * 1.1f);
        } else if (scrollY < 0) {
            mapZoom = Math.max(0.25f, mapZoom / 1.1f);
        }
        return true;
    }

    private void openWaypointEditor(Waypoint waypoint) {
        if (minecraft != null && minecraft.player != null) {
            BlockPos playerPos = minecraft.player.blockPosition();
            waypointEditor = new WaypointEditorPopup(this, waypoint, playerPos, minecraft.player.level().dimension());
        }
    }

    private void openWaypointListGUI() {
        if (minecraft != null) {
            minecraft.setScreen(new WaypointListScreen(this));
        }
    }

    public void closeWaypointEditor(boolean save, WaypointEditorPopup.WaypointData data) {
        if (save && data != null) {
            WaypointActionPacket.Action action;

            // Si enabled = false, c'est une suppression
            if (!data.enabled()) {
                action = WaypointActionPacket.Action.REMOVE;
            } else if (data.isNew()) {
                action = WaypointActionPacket.Action.ADD;
            } else {
                action = WaypointActionPacket.Action.UPDATE;
            }

            // Envoyer le packet avec customRGB et enabled
            PacketDistributor.sendToServer(new WaypointActionPacket(
                action,
                data.id(),
                data.name(),
                data.position(),
                data.dimension(),
                data.color(),
                data.customRGB(),
                data.enabled()
            ));
        }
        waypointEditor = null;
    }

    private void openContextMenu(double mouseX, double mouseY) {
        if (minecraft == null || minecraft.player == null) return;

        int mapX = guiLeft + 65;
        int mapY = guiTop + 30;
        int mapWidth = guiWidth - 140;
        int mapHeight = guiHeight - 40;

        int centerX = mapX + mapWidth / 2;
        int centerY = mapY + mapHeight / 2;

        // Calculer le chunk cliqué
        float relativeX = (float) (mouseX - centerX);
        float relativeZ = (float) (mouseY - centerY);

        float worldX = mapCenterX + (relativeX / mapZoom);
        float worldZ = mapCenterZ + (relativeZ / mapZoom);

        int chunkX = (int) Math.floor(worldX / 16);
        int chunkZ = (int) Math.floor(worldZ / 16);

        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        // Ouvrir le context menu à la position de la souris
        contextMenu = new ChunkContextMenu(this, chunkPos, (int) mouseX, (int) mouseY);
    }

    public void closeContextMenu() {
        contextMenu = null;
    }

    private void startClaimDrag(double mouseX, double mouseY, boolean claimMode) {
        if (minecraft == null) return;

        ChunkPos chunk = getChunkAtMouse(mouseX, mouseY);
        if (chunk != null) {
            isDraggingClaim = true;
            dragClaimStart = chunk;
            dragClaimEnd = chunk;
            dragClaimMode = claimMode;
        }
    }

    private void updateClaimDrag(double mouseX, double mouseY) {
        ChunkPos chunk = getChunkAtMouse(mouseX, mouseY);
        if (chunk != null) {
            dragClaimEnd = chunk;
        }
    }

    private void finishClaimDrag() {
        if (dragClaimStart == null || dragClaimEnd == null || minecraft == null || minecraft.player == null) return;

        // Calculer la zone rectangulaire
        int minX = Math.min(dragClaimStart.x, dragClaimEnd.x);
        int maxX = Math.max(dragClaimStart.x, dragClaimEnd.x);
        int minZ = Math.min(dragClaimStart.z, dragClaimEnd.z);
        int maxZ = Math.max(dragClaimStart.z, dragClaimEnd.z);

        // Créer la liste de tous les chunks de la zone
        List<ChunkClaimPacket.ChunkPosition> chunks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(new ChunkClaimPacket.ChunkPosition(x, z));
            }
        }

        // Envoyer le packet au serveur
        String dimensionStr = minecraft.player.level().dimension().location().toString();
        ChunkClaimPacket.Action action = dragClaimMode ? ChunkClaimPacket.Action.CLAIM : ChunkClaimPacket.Action.UNCLAIM;

        PacketDistributor.sendToServer(new ChunkClaimPacket(
            action,
            dimensionStr,
            chunks
        ));

        dragClaimStart = null;
        dragClaimEnd = null;

        // Le serveur enverra automatiquement les claims mis à jour via ClaimsMapDataMessage
        // dans ChunkClaimPacket.handleServerSide() - pas besoin de request supplémentaire!
    }

    /**
     * Demande une mise à jour des claims autour du joueur
     */
    private void requestClaimsUpdate() {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) return;

        BlockPos playerPos = minecraft.player.blockPosition();
        int centerCx = playerPos.getX() >> 4;
        int centerCz = playerPos.getZ() >> 4;
        int radius = 20; // Large rayon pour couvrir toute la fullscreen map

        String dimension = minecraft.level.dimension().location().toString();

        // Envoyer le request au serveur
        PacketDistributor.sendToServer(
            new fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage(
                dimension, centerCx, centerCz, radius
            )
        );
    }

    private ChunkPos getChunkAtMouse(double mouseX, double mouseY) {
        int mapX = guiLeft + 65;
        int mapY = guiTop + 30;
        int mapWidth = guiWidth - 140;
        int mapHeight = guiHeight - 40;

        int centerX = mapX + mapWidth / 2;
        int centerY = mapY + mapHeight / 2;

        float relativeX = (float) (mouseX - centerX);
        float relativeZ = (float) (mouseY - centerY);

        float worldX = mapCenterX + (relativeX / mapZoom);
        float worldZ = mapCenterZ + (relativeZ / mapZoom);

        int chunkX = (int) Math.floor(worldX / 16);
        int chunkZ = (int) Math.floor(worldZ / 16);

        return new ChunkPos(chunkX, chunkZ);
    }

    private Waypoint getWaypointAtMouse(double mouseX, double mouseY) {
        if (minecraft == null || minecraft.player == null) return null;

        int mapX = guiLeft + 65;
        int mapY = guiTop + 30;
        int mapWidth = guiWidth - 140;
        int mapHeight = guiHeight - 40;

        int centerX = mapX + mapWidth / 2;
        int centerY = mapY + mapHeight / 2;

        List<Waypoint> waypoints = WaypointManager.getInstance().getWaypointsForDimension(minecraft.player.level().dimension());

        // Rayon de détection (10 pixels)
        int detectionRadius = 10;

        for (Waypoint waypoint : waypoints) {
            // Skip disabled waypoints
            if (!waypoint.isEnabled()) {
                continue;
            }

            BlockPos pos = waypoint.getPosition();

            float offsetX = (pos.getX() - mapCenterX) * mapZoom;
            float offsetZ = (pos.getZ() - mapCenterZ) * mapZoom;

            int renderX = (int) (centerX + offsetX);
            int renderY = (int) (centerY + offsetZ);

            // Check si la souris est proche du waypoint
            double distX = mouseX - renderX;
            double distY = mouseY - renderY;
            double distance = Math.sqrt(distX * distX + distY * distY);

            if (distance <= detectionRadius) {
                return waypoint;
            }
        }

        return null;
    }

    private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null) return;

        // Check for waypoint hover first (priority over claims)
        Waypoint hoveredWaypoint = getWaypointAtMouse(mouseX, mouseY);
        if (hoveredWaypoint != null) {
            // Render waypoint name tooltip
            Component tooltip = Component.literal(hoveredWaypoint.getName());
            graphics.renderTooltip(font, tooltip, mouseX, mouseY);
            return;
        }

        // Check for claim hover
        ChunkPos hoveredChunk = getChunkAtMouse(mouseX, mouseY);
        if (hoveredChunk != null) {
            String dimension = minecraft.level.dimension().location().toString();
            ClaimRenderHelper.ClaimInfo claimInfo = ClaimRenderHelper.getClaimInfo(hoveredChunk, dimension);

            if (claimInfo != null) {
                String factionId = claimInfo.factionId();
                Component tooltip;

                // Check for special factions
                if ("safezone".equals(factionId)) {
                    tooltip = Component.literal("§aSafezone");
                } else if ("warzone".equals(factionId)) {
                    tooltip = Component.literal("§4Warzone");
                } else {
                    // Regular faction - show display name
                    tooltip = Component.literal(claimInfo.factionName());
                }

                graphics.renderTooltip(font, tooltip, mouseX, mouseY);
            }
            // No tooltip for wilderness
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
