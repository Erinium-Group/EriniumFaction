package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.client.waypoint.Waypoint;
import fr.eriniumgroup.erinium_faction.client.waypoint.WaypointManager;
import fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapDataMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ecran simple affichant une carte des claims autour du joueur.
 */
public class FactionMapScreen extends Screen {
    private static ClaimsMapDataMessage lastData;

    public static void onMapData(ClaimsMapDataMessage msg) {
        lastData = msg;
        if (Minecraft.getInstance().screen instanceof FactionMapScreen s) s.refreshFrom(msg);
    }

    private int radius = 12; // par défaut pour la requête au serveur
    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 32;
    private ClaimsMapDataMessage data;


    private long lastRequestMs = 0L;

    // Système de zoom: taille des cellules
    private int cellSize = 16; // Taille par défaut en pixels
    private static final int MIN_CELL_SIZE = 4;
    private static final int MAX_CELL_SIZE = 32;

    // Dimensions MAXIMALES de la zone de carte (CARRE)
    private static final int MAX_MAP_SIZE = 270;

    // Couleurs personnalisables (chargées depuis la config)
    private int gridColor;
    private int crossColor;
    private MapConfig config;

    // Menu contextuel et sélection
    private ChunkContextMenu contextMenu;
    private boolean isDragging = false;
    private int dragStartChunkX, dragStartChunkZ;
    private int dragEndChunkX, dragEndChunkZ;
    private boolean isCtrlDrag = false; // CTRL+drag pour claim
    private boolean isShiftDrag = false; // SHIFT+drag pour unclaim

    // Cache pour optimiser les performances
    private int cachedVisibleChunksX = -1; // Nombre de chunks visibles horizontalement
    private int cachedVisibleChunksZ = -1; // Nombre de chunks visibles verticalement
    private int cachedCell = -1;
    private int cachedMapWidth = -1;  // Taille réelle du cadre
    private int cachedMapHeight = -1;
    private int cachedX0 = -1;
    private int cachedY0 = -1;
    private int cachedWidth = -1;
    private int cachedHeight = -1;
    private boolean layoutDirty = true;

    public FactionMapScreen() {
        super(Component.translatable("erinium_faction.faction.map.title"));
        // Charger la config
        this.config = MapConfig.load();
        this.gridColor = config.gridColor;
        this.crossColor = config.crossColor;
    }

    @Override
    protected void init() {
        super.init();

        // Mettre à jour le layout pour avoir les bonnes coordonnées
        updateLayoutCache();
        int btnSize = 20;

        // Bouton waypoints sur le côté gauche
        int waypointBtnX = cachedX0 - 90;
        int waypointBtnY = cachedY0;
        Button waypointBtn = Button.builder(Component.literal("Waypoints"), b -> {
            WaypointManager manager = fr.eriniumgroup.erinium_faction.client.EFClient.getWaypointManager();
            if (manager != null) {
                this.minecraft.setScreen(new WaypointListScreen(this, manager));
            }
        }).bounds(waypointBtnX, waypointBtnY, 80, 20).build();
        this.addRenderableWidget(waypointBtn);

        // Bouton settings en haut à droite du cadre
        int btnX = cachedX0 + cachedMapWidth - btnSize - 4;
        int btnY = cachedY0 - 24;

        Button settingsBtn = Button.builder(Component.literal("\u2699"), b -> {
            // Ouvrir la popup de settings
            this.minecraft.setScreen(new MapSettingsPopup(this, gridColor, crossColor));
        }).bounds(btnX, btnY, btnSize, btnSize).build();

        this.addRenderableWidget(settingsBtn);
        requestData();
    }


    /**
     * Recalculer le layout seulement si nécessaire (resize ou zoom change)
     * Le cadre scale down si l'écran est trop petit
     */
    private void updateLayoutCache() {
        if (!layoutDirty && cachedWidth == this.width && cachedHeight == this.height && cachedCell == cellSize) {
            return; // Pas de changement
        }

        cachedWidth = this.width;
        cachedHeight = this.height;
        cachedCell = cellSize;

        // Calculer la taille disponible
        int availableWidth = this.width - 100;
        int availableHeight = this.height - 150;

        // CADRE NOIR FIXE - ne change JAMAIS de taille
        int frameSize = Math.min(Math.min(availableWidth, availableHeight), MAX_MAP_SIZE);
        cachedMapWidth = frameSize;
        cachedMapHeight = frameSize;

        // Calculer combien de chunks rentrent dans ce cadre FIXE
        int maxChunks = frameSize / cellSize;

        // Si on atteint ou dépasse 32 chunks, forcer à 32 et ajuster cellSize pour remplir le cadre
        if (maxChunks >= 32) {
            maxChunks = 32;
            cellSize = frameSize / 32; // Les 32 chunks remplissent TOUT le cadre
            cachedCell = cellSize; // Mettre à jour le cache
        }

        cachedVisibleChunksX = maxChunks;
        cachedVisibleChunksZ = maxChunks;

        // Centrer le cadre dans la fenêtre
        cachedX0 = (this.width - cachedMapWidth) / 2;
        cachedY0 = (this.height - cachedMapHeight - 50) / 2; // Offset pour les boutons

        layoutDirty = false;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        layoutDirty = true; // Forcer recalcul au resize
        super.resize(minecraft, width, height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Mettre à jour le cache pour avoir les bonnes coordonnées
        updateLayoutCache();

        // Vérifier que la souris est dans la zone de la carte
        if (mouseX >= cachedX0 && mouseX < cachedX0 + cachedMapWidth &&
            mouseY >= cachedY0 && mouseY < cachedY0 + cachedMapHeight) {

            // Calculer la cellSize minimale pour afficher 32 chunks dans le cadre
            int minCellSizeFor32 = cachedMapWidth / 32;

            // Scroll up = zoom in (cellules plus grandes), scroll down = zoom out (cellules plus petites)
            int delta = scrollY > 0 ? 2 : -2; // Changer par pas de 2 pixels

            // Empêcher de dézoomer au-delà de 32x32 chunks
            int newSize = Math.max(minCellSizeFor32, Math.min(MAX_CELL_SIZE, cellSize + delta));

            if (newSize != cellSize) {
                cellSize = newSize;
                layoutDirty = true; // Forcer recalcul du nombre de chunks visibles
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Menu contextuel en priorité
        if (contextMenu != null && contextMenu.isVisible()) {
            boolean handled = contextMenu.mouseClicked(mouseX, mouseY, button);
            if (handled || button == 0) { // Fermer si clic gauche en dehors
                contextMenu = null;
            }
            return true;
        }

        updateLayoutCache();

        // Vérifier si on est dans la zone de la grille
        int gridWidth = cachedVisibleChunksX * cellSize;
        int gridHeight = cachedVisibleChunksZ * cellSize;
        int gridOffsetX = (cachedMapWidth - gridWidth) / 2;
        int gridOffsetY = (cachedMapHeight - gridHeight) / 2;
        int gridX0 = cachedX0 + gridOffsetX;
        int gridY0 = cachedY0 + gridOffsetY;

        if (mouseX >= gridX0 && mouseX < gridX0 + gridWidth && mouseY >= gridY0 && mouseY < gridY0 + gridHeight) {
            // Calculer le chunk cliqué
            int chunkIndexX = (int) ((mouseX - gridX0) / cellSize);
            int chunkIndexZ = (int) ((mouseY - gridY0) / cellSize);
            int halfVisibleX = cachedVisibleChunksX / 2;
            int halfVisibleZ = cachedVisibleChunksZ / 2;
            int relChunkX = chunkIndexX - halfVisibleX;
            int relChunkZ = chunkIndexZ - halfVisibleZ;
            int absChunkX = (data != null ? data.centerCx() : 0) + relChunkX;
            int absChunkZ = (data != null ? data.centerCz() : 0) + relChunkZ;

            if (button == 1) {
                // Clic droit = menu contextuel
                contextMenu = new ChunkContextMenu((int) mouseX, (int) mouseY, absChunkX, absChunkZ,
                        minecraft.player.level().dimension().location().toString(), this);
                return true;
            } else if (button == 0) {
                // Clic gauche = début de drag
                boolean isCtrl = hasControlDown();
                boolean isShift = hasShiftDown();

                if (isCtrl || isShift) {
                    isDragging = true;
                    isCtrlDrag = isCtrl;
                    isShiftDrag = isShift;
                    dragStartChunkX = absChunkX;
                    dragStartChunkZ = absChunkZ;
                    dragEndChunkX = absChunkX;
                    dragEndChunkZ = absChunkZ;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            updateLayoutCache();

            int gridWidth = cachedVisibleChunksX * cellSize;
            int gridHeight = cachedVisibleChunksZ * cellSize;
            int gridOffsetX = (cachedMapWidth - gridWidth) / 2;
            int gridOffsetY = (cachedMapHeight - gridHeight) / 2;
            int gridX0 = cachedX0 + gridOffsetX;
            int gridY0 = cachedY0 + gridOffsetY;

            if (mouseX >= gridX0 && mouseX < gridX0 + gridWidth && mouseY >= gridY0 && mouseY < gridY0 + gridHeight) {
                int chunkIndexX = (int) ((mouseX - gridX0) / cellSize);
                int chunkIndexZ = (int) ((mouseY - gridY0) / cellSize);
                int halfVisibleX = cachedVisibleChunksX / 2;
                int halfVisibleZ = cachedVisibleChunksZ / 2;
                int relChunkX = chunkIndexX - halfVisibleX;
                int relChunkZ = chunkIndexZ - halfVisibleZ;
                dragEndChunkX = (data != null ? data.centerCx() : 0) + relChunkX;
                dragEndChunkZ = (data != null ? data.centerCz() : 0) + relChunkZ;
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging && button == 0) {
            // Exécuter claim/unclaim sur la zone sélectionnée
            if (isCtrlDrag) {
                claimArea(dragStartChunkX, dragStartChunkZ, dragEndChunkX, dragEndChunkZ);
            } else if (isShiftDrag) {
                unclaimArea(dragStartChunkX, dragStartChunkZ, dragEndChunkX, dragEndChunkZ);
            }

            isDragging = false;
            isCtrlDrag = false;
            isShiftDrag = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void requestData() {
        Player p = Minecraft.getInstance().player;
        if (p == null) return;
        // simple cooldown 200ms
        long now = System.currentTimeMillis();
        if (now - lastRequestMs < 200) return;
        lastRequestMs = now;

        String dim = p.level().dimension().location().toString();
        int cx = p.chunkPosition().x;
        int cz = p.chunkPosition().z;
        PacketDistributor.sendToServer(new ClaimsMapRequestMessage(dim, cx, cz, radius));
    }

    private void refreshFrom(ClaimsMapDataMessage msg) {
        this.data = msg;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(g, mouseX, mouseY, partialTicks);
        super.render(g, mouseX, mouseY, partialTicks);
        RenderSystem.enableBlend();

        // Mettre à jour le cache de layout si nécessaire
        updateLayoutCache();

        // Utiliser les valeurs cachées
        int visibleChunksX = cachedVisibleChunksX;
        int visibleChunksZ = cachedVisibleChunksZ;
        int cell = cachedCell;
        int mapW = cachedMapWidth;  // Taille du cadre noir FIXE
        int mapH = cachedMapHeight; // Taille du cadre noir FIXE
        int x0 = cachedX0;
        int y0 = cachedY0;
        int pad = 4;

        // Calculer la taille réelle de la grille de chunks
        int gridWidth = visibleChunksX * cell;
        int gridHeight = visibleChunksZ * cell;

        // Centrer la grille de chunks dans le cadre noir fixe
        int gridOffsetX = (mapW - gridWidth) / 2;
        int gridOffsetY = (mapH - gridHeight) / 2;
        int gridX0 = x0 + gridOffsetX;
        int gridY0 = y0 + gridOffsetY;

        // rafraîchissement auto si le joueur a bougé de chunk
        Player p = Minecraft.getInstance().player;
        if (p != null && (data == null || p.chunkPosition().x != data.centerCx() || p.chunkPosition().z != data.centerCz())) {
            requestData();
        }

        // fond
        g.fill(x0 - pad, y0 - pad, x0 + mapW + pad, y0 + mapH + pad, 0xAA000000);

        // Coordonnées du centre
        int cx0 = (data != null ? data.centerCx() : (p != null ? p.chunkPosition().x : 0));
        int cz0 = (data != null ? data.centerCz() : (p != null ? p.chunkPosition().z : 0));

        // Offset pour centrer le joueur dans la vue
        final int halfVisibleX = visibleChunksX / 2;
        final int halfVisibleZ = visibleChunksZ / 2;

        // claims
        if (data != null && data.relCx().length == data.relCz().length) {
            for (int i = 0; i < data.relCx().length; i++) {
                int rx = data.relCx()[i]; // Position relative au centre
                int rz = data.relCz()[i];

                // Convertir en position dans le cadre visible
                int gx = rx + halfVisibleX;
                int gz = rz + halfVisibleZ;

                // Vérifier si visible dans le cadre
                if (gx < 0 || gz < 0 || gx >= visibleChunksX || gz >= visibleChunksZ) continue;

                int x = gridX0 + gx * cell;
                int y = gridY0 + gz * cell;
                int color = colorForOwner(data.owners()[i]);
                g.fill(x + 1, y + 1, x + cell - 1, y + cell - 1, 0xFF000000 | color);
            }
        }

        // Grille optimisée: lignes (centrée dans le cadre noir)
        // Lignes verticales
        for (int c = 0; c <= visibleChunksX; c++) {
            int x = gridX0 + c * cell;
            g.fill(x, gridY0, x + 1, gridY0 + gridHeight, gridColor);
        }
        // Lignes horizontales
        for (int r = 0; r <= visibleChunksZ; r++) {
            int y = gridY0 + r * cell;
            g.fill(gridX0, y, gridX0 + gridWidth, y + 1, gridColor);
        }

        // marqueur centre (position du joueur) - CROIX
        int centerChunkX = halfVisibleX;
        int centerChunkZ = halfVisibleZ;
        int px = gridX0 + centerChunkX * cell;
        int py = gridY0 + centerChunkZ * cell;
        int centerX = px + cell / 2;
        int centerY = py + cell / 2;
        // Barre verticale
        g.fill(centerX - 1, py + 2, centerX + 1, py + cell - 2, this.crossColor);
        // Barre horizontale
        g.fill(px + 2, centerY - 1, px + cell - 2, centerY + 1, this.crossColor);

        // légende
        g.drawString(this.font, this.title, x0, y0 - 18, 0xFFFFFF, false);
        g.drawString(this.font, Component.literal("Center: [" + cx0 + ", " + cz0 + "] | Zoom: " + cellSize + "px (" + visibleChunksX + "x" + visibleChunksZ + " chunks)"), x0, y0 + mapH + pad + 2, 0xCCCCCC, false);

        // Affichage nom faction sous le curseur si sur une case claimée
        if (data != null && mouseX >= gridX0 && mouseX < gridX0 + gridWidth && mouseY >= gridY0 && mouseY < gridY0 + gridHeight) {
            // Calculer la position du chunk sous la souris
            int chunkIndexX = (int)((mouseX - gridX0) / cell);
            int chunkIndexZ = (int)((mouseY - gridY0) / cell);

            // Convertir en position relative au centre
            int col = chunkIndexX - halfVisibleX;
            int row = chunkIndexZ - halfVisibleZ;
            int absCx = data.centerCx() + col;
            int absCz = data.centerCz() + row;

            // rechercher dans le tableau
            for (int i = 0; i < data.relCx().length; i++) {
                if (data.relCx()[i] == col && data.relCz()[i] == row) {
                    String ownerName = data.owners()[i];
                    List<Component> lines = new ArrayList<>();
                    if (ownerName != null && !ownerName.isBlank()) {
                        lines.add(Component.literal(ownerName).withStyle(ChatFormatting.GOLD));
                    } else {
                        lines.add(Component.translatable("erinium_faction.wilderness.desc").withStyle(ChatFormatting.GRAY));
                    }
                    lines.add(Component.literal("Chunk: (" + absCx + ", " + absCz + ")").withStyle(ChatFormatting.DARK_GRAY));
                    g.renderComponentTooltip(this.font, lines, mouseX, mouseY);
                    break;
                }
            }
        }

        // Afficher la sélection en cours de drag
        if (isDragging) {
            int minX = Math.min(dragStartChunkX, dragEndChunkX);
            int maxX = Math.max(dragStartChunkX, dragEndChunkX);
            int minZ = Math.min(dragStartChunkZ, dragEndChunkZ);
            int maxZ = Math.max(dragStartChunkZ, dragEndChunkZ);

            int centerCx = (data != null ? data.centerCx() : (p != null ? p.chunkPosition().x : 0));
            int centerCz = (data != null ? data.centerCz() : (p != null ? p.chunkPosition().z : 0));

            for (int cx = minX; cx <= maxX; cx++) {
                for (int cz = minZ; cz <= maxZ; cz++) {
                    int relX = cx - centerCx;
                    int relZ = cz - centerCz;
                    int gx = relX + halfVisibleX;
                    int gz = relZ + halfVisibleZ;

                    if (gx >= 0 && gx < visibleChunksX && gz >= 0 && gz < visibleChunksZ) {
                        int x = gridX0 + gx * cell;
                        int y = gridY0 + gz * cell;
                        int selColor = isCtrlDrag ? 0x5500FF00 : 0x55FF0000; // Vert pour claim, rouge pour unclaim
                        g.fill(x + 1, y + 1, x + cell - 1, y + cell - 1, selColor);
                    }
                }
            }
        }

        // Afficher le menu contextuel
        if (contextMenu != null && contextMenu.isVisible()) {
            contextMenu.render(g, mouseX, mouseY);
        }

        // Render waypoints
        WaypointManager waypointManager = fr.eriniumgroup.erinium_faction.client.EFClient.getWaypointManager();
        if (waypointManager != null && p != null) {
            String dimension = p.level().dimension().location().toString();
            List<Waypoint> waypoints = waypointManager.getVisibleWaypointsForDimension(dimension);

            for (Waypoint wp : waypoints) {
                // Convertir position monde vers position chunk
                int wpChunkX = wp.getX() >> 4;
                int wpChunkZ = wp.getZ() >> 4;

                // Position relative au centre
                int relX = wpChunkX - cx0;
                int relZ = wpChunkZ - cz0;

                // Position dans la grille
                int gx = relX + halfVisibleX;
                int gz = relZ + halfVisibleZ;

                // Vérifier si visible dans le cadre
                if (gx >= 0 && gx < visibleChunksX && gz >= 0 && gz < visibleChunksZ) {
                    int wx = gridX0 + gx * cell + cell / 2;
                    int wy = gridY0 + gz * cell + cell / 2;

                    // Dessiner un point
                    int dotSize = Math.max(4, cell / 3);
                    g.fill(wx - dotSize / 2, wy - dotSize / 2, wx + dotSize / 2, wy + dotSize / 2, wp.getColorARGB());

                    // Bordure noire pour contraste
                    g.fill(wx - dotSize / 2 - 1, wy - dotSize / 2 - 1, wx + dotSize / 2 + 1, wy - dotSize / 2, 0xFF000000);
                    g.fill(wx - dotSize / 2 - 1, wy + dotSize / 2, wx + dotSize / 2 + 1, wy + dotSize / 2 + 1, 0xFF000000);
                    g.fill(wx - dotSize / 2 - 1, wy - dotSize / 2, wx - dotSize / 2, wy + dotSize / 2, 0xFF000000);
                    g.fill(wx + dotSize / 2, wy - dotSize / 2, wx + dotSize / 2 + 1, wy + dotSize / 2, 0xFF000000);
                }
            }
        }

        RenderSystem.disableBlend();
    }

    private static final Map<String, Integer> ownerColorCache = new HashMap<>();
    private static int colorForOwner(String owner) {
        if (owner == null || owner.isBlank()) return 0x555555; // wilderness
        return ownerColorCache.computeIfAbsent(owner.toLowerCase(Locale.ROOT), k -> {
            int h = k.hashCode();
            int r = 64 + (Math.abs(h) % 192);
            int g = 64 + (Math.abs(h >> 8) % 192);
            int b = 64 + (Math.abs(h >> 16) % 192);
            return (r << 16) | (g << 8) | b;
        });
    }

    // Méthodes pour mettre à jour les couleurs depuis la popup
    public void setGridColor(int color) {
        this.gridColor = color;
        this.config.gridColor = color;
        this.config.save(); // Sauvegarder automatiquement

        // Mettre à jour la minimap overlay si active
        fr.eriniumgroup.erinium_faction.client.EFClient.reloadMinimapColors();
    }

    public void setCrossColor(int color) {
        this.crossColor = color;
        this.config.crossColor = color;
        this.config.save(); // Sauvegarder automatiquement

        // Mettre à jour la minimap overlay si active
        fr.eriniumgroup.erinium_faction.client.EFClient.reloadMinimapColors();
    }

    // Méthodes claim/unclaim
    public void claimChunk(int chunkX, int chunkZ, String dimension) {
        // Envoyer un paquet au serveur
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
            new fr.eriniumgroup.erinium_faction.common.network.packets.ChunkClaimPacket(dimension, chunkX, chunkZ, true)
        );
        // Rafraîchir la carte après un petit délai
        minecraft.execute(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}
            requestData();
        });
    }

    public void unclaimChunk(int chunkX, int chunkZ, String dimension) {
        // Envoyer un paquet au serveur
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
            new fr.eriniumgroup.erinium_faction.common.network.packets.ChunkClaimPacket(dimension, chunkX, chunkZ, false)
        );
        // Rafraîchir la carte après un petit délai
        minecraft.execute(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}
            requestData();
        });
    }

    private void claimArea(int x1, int z1, int x2, int z2) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        String dimension = minecraft.player.level().dimension().location().toString();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new fr.eriniumgroup.erinium_faction.common.network.packets.ChunkClaimPacket(dimension, x, z, true)
                );
            }
        }

        // Rafraîchir la carte
        minecraft.execute(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
            requestData();
        });
    }

    private void unclaimArea(int x1, int z1, int x2, int z2) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        String dimension = minecraft.player.level().dimension().location().toString();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new fr.eriniumgroup.erinium_faction.common.network.packets.ChunkClaimPacket(dimension, x, z, false)
                );
            }
        }

        // Rafraîchir la carte
        minecraft.execute(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
            requestData();
        });
    }
}
