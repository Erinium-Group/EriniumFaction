package fr.eriniumgroup.erinium_faction.client.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fr.eriniumgroup.erinium_faction.client.waypoint.Waypoint;
import fr.eriniumgroup.erinium_faction.client.waypoint.WaypointManager;
import fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapDataMessage;
import fr.eriniumgroup.erinium_faction.gui.MinimapOverlayConfig;
import fr.eriniumgroup.erinium_faction.gui.screens.MapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renderer optimisé pour l'overlay de minimap
 * Utilise un cache ultra-agressif: on render qu'une fois par chunk
 */
public class MinimapOverlayRenderer {
    private MinimapOverlayConfig config;
    private ClaimsMapDataMessage cachedData;

    // Cache des couleurs par faction
    private final Map<String, Integer> factionColorCache = new HashMap<>();

    // Couleurs chargées depuis MapConfig
    private int gridColor;
    private int crossColor;

    // Cache texture pour 0 impact FPS
    private int cachedPlayerChunkX = Integer.MAX_VALUE;
    private int cachedPlayerChunkZ = Integer.MAX_VALUE;
    private DynamicTexture cachedTexture = null;
    private ResourceLocation cachedTextureLocation = null;
    private int cachedSize = 0;
    private int cachedVisibleChunks = 0;
    private int cachedCellSize = 0;
    private boolean cachedRoundShape = false;

    public MinimapOverlayRenderer(MinimapOverlayConfig config) {
        this.config = config;
        loadColors();
    }

    public void setConfig(MinimapOverlayConfig config) {
        this.config = config;
        reloadColors();
        invalidateCache(); // Invalider le cache
    }

    private void loadColors() {
        MapConfig mapConfig = MapConfig.load();
        this.gridColor = mapConfig.gridColor;
        this.crossColor = mapConfig.crossColor;
    }

    public void reloadColors() {
        loadColors();
        invalidateCache(); // Régénérer la texture avec les nouvelles couleurs
    }

    public void updateData(ClaimsMapDataMessage data) {
        this.cachedData = data;
        invalidateCache(); // Invalider le cache quand les données changent
    }

    private void invalidateCache() {
        if (cachedTexture != null) {
            cachedTexture.close();
            cachedTexture = null;
        }
        if (cachedTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(cachedTextureLocation);
            cachedTextureLocation = null;
        }
    }

    // Convertir ARGB (Minecraft) vers ABGR (NativeImage)
    private int argbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public void render(GuiGraphics g, int screenWidth, int screenHeight) {
        if (!config.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int[] pos = config.getScreenPosition(screenWidth, screenHeight);
        int x = pos[0];
        int y = pos[1];

        // Calculer combien de chunks afficher
        int visibleChunks = config.size / config.cellSize;
        if (visibleChunks < 3) visibleChunks = 3; // Minimum 3x3
        int actualGridSize = visibleChunks * config.cellSize;

        // Check si le joueur a bougé de chunk
        int playerChunkX = player.chunkPosition().x;
        int playerChunkZ = player.chunkPosition().z;
        boolean chunkChanged = (playerChunkX != cachedPlayerChunkX || playerChunkZ != cachedPlayerChunkZ);

        // Invalider cache si config a changé
        boolean configChanged = cachedTexture == null ||
                                cachedSize != actualGridSize ||
                                cachedVisibleChunks != visibleChunks ||
                                cachedCellSize != config.cellSize ||
                                cachedRoundShape != config.roundShape;

        // Regénérer texture SEULEMENT si chunk changé OU config changé
        if (chunkChanged || configChanged) {
            cachedPlayerChunkX = playerChunkX;
            cachedPlayerChunkZ = playerChunkZ;
            regenerateTexture(playerChunkX, playerChunkZ, actualGridSize, visibleChunks);
        }

        // Blitter la texture (0 calculs!)
        if (cachedTexture != null && cachedTextureLocation != null) {
            RenderSystem.enableBlend();
            g.blit(cachedTextureLocation, x, y, 0, 0, actualGridSize, actualGridSize, actualGridSize, actualGridSize);
            RenderSystem.disableBlend();
        }

        // Seulement render les éléments dynamiques (flèche + cardinaux)
        renderCardinals(g, x, y, actualGridSize);
        renderPlayerMarker(g, x, y, actualGridSize, player);

        // Render waypoints sur la minimap
        renderWaypoints(g, x, y, actualGridSize, visibleChunks, player);
    }

    private void regenerateTexture(int playerChunkX, int playerChunkZ, int size, int visibleChunks) {
        // Invalider ancienne texture
        invalidateCache();

        // Créer nouvelle texture
        NativeImage image = new NativeImage(size, size, true);

        int halfChunks = visibleChunks / 2;
        int cellSize = config.cellSize;

        // Fond transparent
        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                image.setPixelRGBA(px, py, 0x00000000);
            }
        }

        int bgColor = argbToAbgr(0xAA000000);

        if (config.roundShape) {
            // Fond rond
            int centerX = size / 2;
            int centerY = size / 2;
            int radius = size / 2;
            int radiusSquared = radius * radius;

            for (int py = 0; py < size; py++) {
                for (int px = 0; px < size; px++) {
                    int dx = px - centerX;
                    int dy = py - centerY;
                    if (dx * dx + dy * dy <= radiusSquared) {
                        image.setPixelRGBA(px, py, bgColor);
                    }
                }
            }
        } else {
            // Fond carré
            for (int py = 0; py < size; py++) {
                for (int px = 0; px < size; px++) {
                    image.setPixelRGBA(px, py, bgColor);
                }
            }
        }

        // Render chunks
        for (int dz = -halfChunks; dz <= halfChunks; dz++) {
            for (int dx = -halfChunks; dx <= halfChunks; dx++) {
                // Pour forme ronde, skip chunks hors du cercle
                if (config.roundShape && dx * dx + dz * dz > halfChunks * halfChunks) continue;

                int chunkX = playerChunkX + dx;
                int chunkZ = playerChunkZ + dz;

                String factionId = getChunkFaction(chunkX, chunkZ);
                int color = getFactionColor(factionId);
                int convertedColor = argbToAbgr(color);

                // Render chunk dans texture
                int cx = (dx + halfChunks) * cellSize;
                int cy = (dz + halfChunks) * cellSize;

                for (int py = 0; py < cellSize; py++) {
                    for (int px = 0; px < cellSize; px++) {
                        int imgX = cx + px;
                        int imgY = cy + py;
                        if (imgX >= 0 && imgX < size && imgY >= 0 && imgY < size) {
                            image.setPixelRGBA(imgX, imgY, convertedColor);
                        }
                    }
                }
            }
        }

        // Render grille
        int convertedGridColor = argbToAbgr(gridColor);
        for (int i = 0; i <= visibleChunks; i++) {
            int lx = i * cellSize;
            // Ligne verticale
            for (int py = 0; py < size; py++) {
                if (lx < size) image.setPixelRGBA(lx, py, convertedGridColor);
            }
            // Ligne horizontale
            for (int px = 0; px < size; px++) {
                if (lx < size) image.setPixelRGBA(px, lx, convertedGridColor);
            }
        }

        // Pour forme ronde, ajouter bordure cercle
        if (config.roundShape) {
            int centerX = size / 2;
            int centerY = size / 2;
            int radius = size / 2;
            drawCircleOnImage(image, centerX, centerY, radius, convertedGridColor);
        }

        // Upload texture
        cachedTexture = new DynamicTexture(image);
        cachedTextureLocation = Minecraft.getInstance().getTextureManager().register("minimap_cache", cachedTexture);
        cachedSize = size;
        cachedVisibleChunks = visibleChunks;
        cachedCellSize = cellSize;
        cachedRoundShape = config.roundShape;
    }

    private void drawCircleOnImage(NativeImage image, int centerX, int centerY, int radius, int color) {
        // Algorithme de Bresenham pour dessiner un cercle
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;

        while (y >= x) {
            setCirclePixels(image, centerX, centerY, x, y, color);
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
        }
    }

    private void setCirclePixels(NativeImage image, int cx, int cy, int x, int y, int color) {
        int size = image.getWidth();
        if (cx + x >= 0 && cx + x < size && cy + y >= 0 && cy + y < size) image.setPixelRGBA(cx + x, cy + y, color);
        if (cx - x >= 0 && cx - x < size && cy + y >= 0 && cy + y < size) image.setPixelRGBA(cx - x, cy + y, color);
        if (cx + x >= 0 && cx + x < size && cy - y >= 0 && cy - y < size) image.setPixelRGBA(cx + x, cy - y, color);
        if (cx - x >= 0 && cx - x < size && cy - y >= 0 && cy - y < size) image.setPixelRGBA(cx - x, cy - y, color);
        if (cx + y >= 0 && cx + y < size && cy + x >= 0 && cy + x < size) image.setPixelRGBA(cx + y, cy + x, color);
        if (cx - y >= 0 && cx - y < size && cy + x >= 0 && cy + x < size) image.setPixelRGBA(cx - y, cy + x, color);
        if (cx + y >= 0 && cx + y < size && cy - x >= 0 && cy - x < size) image.setPixelRGBA(cx + y, cy - x, color);
        if (cx - y >= 0 && cx - y < size && cy - x >= 0 && cy - x < size) image.setPixelRGBA(cx - y, cy - x, color);
    }

    private void renderCardinals(GuiGraphics g, int x, int y, int size) {
        Minecraft mc = Minecraft.getInstance();
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int offset = size / 2 + 8; // Distance depuis le centre

        // N (Nord) - en haut
        g.drawCenteredString(mc.font, "N", centerX, y - 10, 0xFFFFFFFF);

        // S (Sud) - en bas
        g.drawCenteredString(mc.font, "S", centerX, y + size + 2, 0xFFFFFFFF);

        // E (Est) - à droite
        g.drawString(mc.font, "E", x + size + 4, centerY - 4, 0xFFFFFFFF, false);

        // W (Ouest) - à gauche
        g.drawString(mc.font, "W", x - 10, centerY - 4, 0xFFFFFFFF, false);
    }

    private void renderPlayerMarker(GuiGraphics g, int x, int y, int size, Player player) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;

        // Obtenir la rotation du joueur (yaw)
        float yaw = player.getYRot(); // 0 = sud, 90 = ouest, 180 = nord, 270 = est
        double angle = Math.toRadians(yaw + 180); // Ajuster pour que 0 = nord

        // Taille de la flèche
        int arrowSize = Math.max(6, config.cellSize);

        // Points de la flèche (triangle pointant vers le haut)
        // Pointe
        double tipX = centerX + Math.sin(angle) * arrowSize;
        double tipY = centerY - Math.cos(angle) * arrowSize;

        // Base gauche
        double leftAngle = angle - Math.toRadians(140);
        double leftX = centerX + Math.sin(leftAngle) * (arrowSize * 0.6);
        double leftY = centerY - Math.cos(leftAngle) * (arrowSize * 0.6);

        // Base droite
        double rightAngle = angle + Math.toRadians(140);
        double rightX = centerX + Math.sin(rightAngle) * (arrowSize * 0.6);
        double rightY = centerY - Math.cos(rightAngle) * (arrowSize * 0.6);

        // Dessiner le triangle (flèche)
        fillTriangle(g, (int)tipX, (int)tipY, (int)leftX, (int)leftY, (int)rightX, (int)rightY, crossColor);
    }

    private void fillTriangle(GuiGraphics g, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        // Algorithme simple de remplissage de triangle
        // Trier les points par Y
        if (y1 > y2) { int t = y1; y1 = y2; y2 = t; t = x1; x1 = x2; x2 = t; }
        if (y1 > y3) { int t = y1; y1 = y3; y3 = t; t = x1; x1 = x3; x3 = t; }
        if (y2 > y3) { int t = y2; y2 = y3; y3 = t; t = x2; x2 = x3; x3 = t; }

        // Remplir le triangle
        for (int y = y1; y <= y3; y++) {
            int xStart, xEnd;

            if (y < y2) {
                // Partie supérieure du triangle
                xStart = y1 == y2 ? x1 : x1 + (x2 - x1) * (y - y1) / (y2 - y1);
                xEnd = y1 == y3 ? x1 : x1 + (x3 - x1) * (y - y1) / (y3 - y1);
            } else {
                // Partie inférieure du triangle
                xStart = y2 == y3 ? x2 : x2 + (x3 - x2) * (y - y2) / (y3 - y2);
                xEnd = y1 == y3 ? x1 : x1 + (x3 - x1) * (y - y1) / (y3 - y1);
            }

            if (xStart > xEnd) {
                int t = xStart;
                xStart = xEnd;
                xEnd = t;
            }

            g.fill(xStart, y, xEnd + 1, y + 1, color);
        }
    }

    private String getChunkFaction(int chunkX, int chunkZ) {
        if (cachedData == null) return "wilderness";

        String key = chunkX + "," + chunkZ;
        return cachedData.claims().getOrDefault(key, "wilderness");
    }

    private int getFactionColor(String factionId) {
        if (factionId == null || factionId.isBlank() || factionId.equals("wilderness")) {
            return 0xFF555555; // Gris foncé pour wilderness (même que FactionMapScreen)
        }

        return factionColorCache.computeIfAbsent(factionId.toLowerCase(java.util.Locale.ROOT), k -> {
            // Même algorithme que FactionMapScreen.colorForOwner()
            int h = k.hashCode();
            int r = 64 + (Math.abs(h) % 192);
            int g = 64 + (Math.abs(h >> 8) % 192);
            int b = 64 + (Math.abs(h >> 16) % 192);
            return 0xFF000000 | (r << 16) | (g << 8) | b; // Opaque
        });
    }

    private void renderWaypoints(GuiGraphics g, int x, int y, int size, int visibleChunks, Player player) {
        WaypointManager waypointManager = fr.eriniumgroup.erinium_faction.client.EFClient.getWaypointManager();
        if (waypointManager == null || player == null) return;

        String dimension = player.level().dimension().location().toString();
        List<Waypoint> waypoints = waypointManager.getVisibleWaypointsForDimension(dimension);

        int playerChunkX = player.chunkPosition().x;
        int playerChunkZ = player.chunkPosition().z;
        int halfChunks = visibleChunks / 2;
        int cellSize = config.cellSize;

        for (Waypoint wp : waypoints) {
            // Convertir position monde vers position chunk
            int wpChunkX = wp.getX() >> 4;
            int wpChunkZ = wp.getZ() >> 4;

            // Position relative au joueur
            int dx = wpChunkX - playerChunkX;
            int dz = wpChunkZ - playerChunkZ;

            // Pour forme ronde, skip waypoints hors du cercle
            if (config.roundShape && dx * dx + dz * dz > halfChunks * halfChunks) continue;

            // Vérifier si visible dans le cadre
            if (Math.abs(dx) > halfChunks || Math.abs(dz) > halfChunks) continue;

            // Position dans la minimap
            int wx = x + (dx + halfChunks) * cellSize + cellSize / 2;
            int wy = y + (dz + halfChunks) * cellSize + cellSize / 2;

            // Dessiner un point
            int dotSize = Math.max(3, cellSize / 2);
            g.fill(wx - dotSize / 2, wy - dotSize / 2, wx + dotSize / 2, wy + dotSize / 2, wp.getColorARGB());

            // Bordure noire pour contraste
            g.fill(wx - dotSize / 2 - 1, wy - dotSize / 2 - 1, wx + dotSize / 2 + 1, wy - dotSize / 2, 0xFF000000);
            g.fill(wx - dotSize / 2 - 1, wy + dotSize / 2, wx + dotSize / 2 + 1, wy + dotSize / 2 + 1, 0xFF000000);
            g.fill(wx - dotSize / 2 - 1, wy - dotSize / 2, wx - dotSize / 2, wy + dotSize / 2, 0xFF000000);
            g.fill(wx + dotSize / 2, wy - dotSize / 2, wx + dotSize / 2 + 1, wy + dotSize / 2, 0xFF000000);
        }
    }
}
