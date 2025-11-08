package fr.eriniumgroup.erinium_faction.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.client.data.BannerImageCache;
import fr.eriniumgroup.erinium_faction.common.network.packets.SaveBannerPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Écran d'édition de bannière faction
 * Canvas 32 largeur x 64 hauteur avec sélecteur de couleur
 * Taille GUI max: 400x270
 */
public class BannerEditorScreen extends Screen {

    private static final int CANVAS_WIDTH = 32;  // Largeur de la bannière
    private static final int CANVAS_HEIGHT = 64; // Hauteur de la bannière
    private static final int GUI_MAX_WIDTH = 400;
    private static final int GUI_MAX_HEIGHT = 270;

    private int guiWidth;
    private int guiHeight;
    private int guiLeft;
    private int guiTop;
    private float scale = 1.0f;
    private int pixelSize; // Taille d'un pixel de bannière à l'écran

    // Palette de couleurs (16 couleurs Minecraft)
    private static final int[] COLOR_PALETTE = {
        0xFFFFFFFF, // Blanc
        0xFFFF6B00, // Orange
        0xFFD500FF, // Magenta
        0xFF5DACFF, // Bleu clair
        0xFFFFFF00, // Jaune
        0xFF00FF00, // Vert citron
        0xFFFFB0D9, // Rose
        0xFF4C4C4C, // Gris foncé
        0xFFC0C0C0, // Gris clair
        0xFF00C8C8, // Cyan
        0xFF8000FF, // Violet
        0xFF0000FF, // Bleu
        0xFF8B4513, // Marron
        0xFF00AA00, // Vert
        0xFFFF0000, // Rouge
        0xFF000000  // Noir
    };

    private final int[][] pixels = new int[CANVAS_WIDTH][CANVAS_HEIGHT];
    private int currentColor = 0xFFFFFFFF; // Blanc par défaut
    private int selectedPaletteIndex = 0;

    private int canvasX, canvasY;
    private int paletteX, paletteY;
    private static final int PALETTE_SIZE = 20;

    private boolean isDrawing = false;

    // Système undo/redo
    private final List<int[][]> undoStack = new ArrayList<>();
    private final List<int[][]> redoStack = new ArrayList<>();
    private static final int MAX_UNDO_STEPS = 50;

    // Zoom
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 8.0;
    private static final double ZOOM_STEP = 0.2;

    // Pan (déplacement dans le canvas zoomé)
    private double panOffsetX = 0.0;
    private double panOffsetY = 0.0;
    private boolean isPanning = false;
    private double lastPanMouseX = 0.0;
    private double lastPanMouseY = 0.0;

    // Pour l'interpolation
    private int lastDrawX = -1;
    private int lastDrawY = -1;

    public BannerEditorScreen() {
        super(Component.translatable("erinium_faction.gui.banner_editor.title"));
    }

    @Override
    protected void init() {
        super.init();

        // Calculer la taille du GUI avec scale si nécessaire
        guiWidth = GUI_MAX_WIDTH;
        guiHeight = GUI_MAX_HEIGHT;

        // Si l'écran est plus petit, calculer le scale
        if (width < guiWidth || height < guiHeight) {
            float scaleX = (float) width / guiWidth;
            float scaleY = (float) height / guiHeight;
            scale = Math.min(scaleX, scaleY) * 0.9f; // 0.9 pour laisser un peu de marge
            guiWidth = (int) (guiWidth * scale);
            guiHeight = (int) (guiHeight * scale);
        }

        // Centrer le GUI
        guiLeft = (width - guiWidth) / 2;
        guiTop = (height - guiHeight) / 2;

        // Calculer la taille du pixel pour que le canvas rentre bien
        // Espace disponible : guiHeight - titre(30) - palette(25) - boutons(50) - marges(20) = ~145 pixels
        int availableHeight = guiHeight - 125;
        this.pixelSize = Math.min(availableHeight / CANVAS_HEIGHT, guiWidth / CANVAS_WIDTH);
        this.pixelSize = Math.max(1, this.pixelSize); // Au moins 1 pixel

        int canvasDisplayWidth = CANVAS_WIDTH * this.pixelSize;
        int canvasDisplayHeight = CANVAS_HEIGHT * this.pixelSize;

        // Positions du canvas (centré dans le GUI)
        canvasX = guiLeft + (guiWidth - canvasDisplayWidth) / 2;
        canvasY = guiTop + 35;

        // Palette en bas du canvas
        paletteX = guiLeft + (guiWidth - (int)(COLOR_PALETTE.length * (PALETTE_SIZE + 2) * scale)) / 2;
        paletteY = canvasY + canvasDisplayHeight + 15;

        // Initialiser avec image existante si disponible
        BufferedImage existing = BannerImageCache.getImage();
        if (existing != null) {
            for (int x = 0; x < CANVAS_WIDTH; x++) {
                for (int y = 0; y < CANVAS_HEIGHT; y++) {
                    pixels[x][y] = existing.getRGB(x, y);
                }
            }
        } else {
            // Canvas blanc par défaut
            for (int x = 0; x < CANVAS_WIDTH; x++) {
                for (int y = 0; y < CANVAS_HEIGHT; y++) {
                    pixels[x][y] = 0xFFFFFFFF;
                }
            }
        }

        // Boutons
        int buttonY = paletteY + (int)(PALETTE_SIZE * scale) + 10;
        int buttonWidth = (int)(70 * scale);
        int buttonHeight = (int)(20 * scale);

        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.banner_editor.button.save"), btn -> save())
            .bounds(guiLeft + guiWidth / 2 - buttonWidth - 5, buttonY, buttonWidth, buttonHeight)
            .build());

        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.banner_editor.button.clear"), btn -> clear())
            .bounds(guiLeft + guiWidth / 2 + 5, buttonY, buttonWidth, buttonHeight)
            .build());

        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.banner_editor.button.cancel"), btn -> onClose())
            .bounds(guiLeft + (guiWidth - buttonWidth) / 2, buttonY + buttonHeight + 5, buttonWidth, buttonHeight)
            .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background sombre - PAS de renderBackground() qui cause le flou
        graphics.fill(0, 0, width, height, 0xC0101010);

        // Fond du GUI
        graphics.fill(guiLeft, guiTop, guiLeft + guiWidth, guiTop + guiHeight, 0xFF1a1a1a);

        // Bordure du GUI
        graphics.fill(guiLeft, guiTop, guiLeft + guiWidth, guiTop + 1, 0xFF00d2ff);
        graphics.fill(guiLeft, guiTop + guiHeight - 1, guiLeft + guiWidth, guiTop + guiHeight, 0xFF00d2ff);
        graphics.fill(guiLeft, guiTop, guiLeft + 1, guiTop + guiHeight, 0xFF00d2ff);
        graphics.fill(guiLeft + guiWidth - 1, guiTop, guiLeft + guiWidth, guiTop + guiHeight, 0xFF00d2ff);

        // Titre
        graphics.drawCenteredString(font, title, guiLeft + guiWidth / 2, guiTop + 10, 0xFFFFFF);

        // Rendu du canvas
        renderCanvas(graphics);

        // Rendu de la palette
        renderPalette(graphics, mouseX, mouseY);

        // Couleur actuelle
        graphics.drawString(font, Component.translatable("erinium_faction.gui.banner_editor.color"), paletteX, paletteY - 15, 0xFFFFFF);

        // Appeler super.render() SANS renderBackground
        // On rend juste les widgets (boutons)
        for (var widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderCanvas(GuiGraphics graphics) {
        int scaledWidth = CANVAS_WIDTH * pixelSize;
        int scaledHeight = CANVAS_HEIGHT * pixelSize;

        // Fond du canvas (bordure) - taille fixe
        graphics.fill(canvasX - 2, canvasY - 2, canvasX + scaledWidth + 2, canvasY + scaledHeight + 2, 0xFF000000);

        // Activer le scissor pour clipper en dehors du canvas
        graphics.enableScissor(canvasX, canvasY, canvasX + scaledWidth, canvasY + scaledHeight);

        // Calculer la taille réelle d'un pixel avec zoom
        int zoomedPixelSize = (int)(pixelSize * zoomLevel);

        // Pixels avec zoom et pan
        for (int x = 0; x < CANVAS_WIDTH; x++) {
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                int screenX = (int)(canvasX + x * zoomedPixelSize + panOffsetX);
                int screenY = (int)(canvasY + y * zoomedPixelSize + panOffsetY);

                // Ne rendre que les pixels visibles
                if (screenX + zoomedPixelSize >= canvasX && screenX <= canvasX + scaledWidth &&
                    screenY + zoomedPixelSize >= canvasY && screenY <= canvasY + scaledHeight) {
                    graphics.fill(screenX, screenY, screenX + zoomedPixelSize, screenY + zoomedPixelSize, pixels[x][y]);
                }
            }
        }

        // Désactiver le scissor
        graphics.disableScissor();
    }

    private void renderPalette(GuiGraphics graphics, int mouseX, int mouseY) {
        int paletteSize = (int)(PALETTE_SIZE * scale);
        int spacing = (int)(2 * scale);

        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            int x = paletteX + i * (paletteSize + spacing);
            int y = paletteY;

            // Bordure si sélectionné
            if (i == selectedPaletteIndex) {
                graphics.fill(x - 2, y - 2, x + paletteSize + 2, y + paletteSize + 2, 0xFFFFFFFF);
            }

            // Couleur
            graphics.fill(x, y, x + paletteSize, y + paletteSize, COLOR_PALETTE[i]);

            // Bordure noire
            graphics.fill(x, y, x + paletteSize, y + 1, 0xFF000000); // Top
            graphics.fill(x, y + paletteSize - 1, x + paletteSize, y + paletteSize, 0xFF000000); // Bottom
            graphics.fill(x, y, x + 1, y + paletteSize, 0xFF000000); // Left
            graphics.fill(x + paletteSize - 1, y, x + paletteSize, y + paletteSize, 0xFF000000); // Right
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Vérifier d'abord les widgets (boutons)
        for (var child : this.children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        int paletteSize = (int)(PALETTE_SIZE * scale);
        int spacing = (int)(2 * scale);

        // Clic sur la palette
        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            int x = paletteX + i * (paletteSize + spacing);
            int y = paletteY;
            if (mouseX >= x && mouseX < x + paletteSize && mouseY >= y && mouseY < y + paletteSize) {
                selectedPaletteIndex = i;
                currentColor = COLOR_PALETTE[i];
                return true;
            }
        }

        // Clic droit pour pan (déplacer la vue)
        if (button == 1) { // Clic droit
            isPanning = true;
            lastPanMouseX = mouseX;
            lastPanMouseY = mouseY;
            return true;
        }

        // Dessin sur le canvas
        if (button == 0) { // Clic gauche
            saveStateForUndo(); // Sauvegarder l'état avant de dessiner
            drawPixel(mouseX, mouseY);
            isDrawing = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Pan (déplacement de la vue avec clic droit)
        if (isPanning && button == 1) {
            double deltaX = mouseX - lastPanMouseX;
            double deltaY = mouseY - lastPanMouseY;
            panOffsetX += deltaX;
            panOffsetY += deltaY;
            lastPanMouseX = mouseX;
            lastPanMouseY = mouseY;
            return true;
        }

        // Dessin avec clic gauche
        if (isDrawing && button == 0) {
            drawPixel(mouseX, mouseY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Gérer les widgets
        for (var child : this.children()) {
            child.mouseReleased(mouseX, mouseY, button);
        }

        if (button == 0) {
            isDrawing = false;
            lastDrawX = -1; // Réinitialiser pour la prochaine ligne
            lastDrawY = -1;
        }

        if (button == 1) {
            isPanning = false;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Zoom avec la molette
        if (scrollY != 0) {
            int scaledWidth = CANVAS_WIDTH * pixelSize;
            int scaledHeight = CANVAS_HEIGHT * pixelSize;

            // Vérifier si la souris est sur le canvas
            if (mouseX >= canvasX && mouseX <= canvasX + scaledWidth &&
                mouseY >= canvasY && mouseY <= canvasY + scaledHeight) {

                double oldZoom = zoomLevel;

                // Ajuster le zoom
                if (scrollY > 0) {
                    zoomLevel = Math.min(zoomLevel + ZOOM_STEP, MAX_ZOOM);
                } else {
                    zoomLevel = Math.max(zoomLevel - ZOOM_STEP, MIN_ZOOM);
                }

                // Ajuster le pan pour zoomer vers la position de la souris
                double zoomRatio = zoomLevel / oldZoom;
                double canvasMouseX = mouseX - canvasX;
                double canvasMouseY = mouseY - canvasY;

                panOffsetX = (panOffsetX - canvasMouseX) * zoomRatio + canvasMouseX;
                panOffsetY = (panOffsetY - canvasMouseY) * zoomRatio + canvasMouseY;

                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // CTRL+Z pour undo
        if (keyCode == 90 && (modifiers & 2) != 0) { // 2 = CTRL
            undo();
            return true;
        }

        // CTRL+Y pour redo
        if (keyCode == 89 && (modifiers & 2) != 0) { // 2 = CTRL
            redo();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawPixel(double mouseX, double mouseY) {
        int zoomedPixelSize = (int)(pixelSize * zoomLevel);

        // Convertir les coordonnées de la souris en coordonnées canvas en tenant compte du pan
        int pixelX = (int)((mouseX - canvasX - panOffsetX) / zoomedPixelSize);
        int pixelY = (int)((mouseY - canvasY - panOffsetY) / zoomedPixelSize);

        if (pixelX >= 0 && pixelX < CANVAS_WIDTH && pixelY >= 0 && pixelY < CANVAS_HEIGHT) {
            // Interpolation pour éviter les trous
            if (lastDrawX != -1 && lastDrawY != -1) {
                interpolateLine(lastDrawX, lastDrawY, pixelX, pixelY);
            } else {
                pixels[pixelX][pixelY] = currentColor;
            }
            lastDrawX = pixelX;
            lastDrawY = pixelY;
        }
    }

    /**
     * Interpolation linéaire entre deux points pour éviter les trous lors du dessin rapide
     */
    private void interpolateLine(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            pixels[x0][y0] = currentColor;

            if (x0 == x1 && y0 == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    /**
     * Sauvegarde l'état actuel pour undo
     */
    private void saveStateForUndo() {
        // Copier l'état actuel
        int[][] state = new int[CANVAS_WIDTH][CANVAS_HEIGHT];
        for (int x = 0; x < CANVAS_WIDTH; x++) {
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                state[x][y] = pixels[x][y];
            }
        }

        // Ajouter à la stack
        undoStack.add(state);

        // Limiter la taille
        if (undoStack.size() > MAX_UNDO_STEPS) {
            undoStack.remove(0);
        }

        // Vider la redo stack
        redoStack.clear();
    }

    /**
     * Undo - Annuler la dernière action
     */
    private void undo() {
        if (undoStack.isEmpty()) return;

        // Sauvegarder l'état actuel dans redo
        int[][] currentState = new int[CANVAS_WIDTH][CANVAS_HEIGHT];
        for (int x = 0; x < CANVAS_WIDTH; x++) {
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                currentState[x][y] = pixels[x][y];
            }
        }
        redoStack.add(currentState);

        // Restaurer l'état précédent
        int[][] previousState = undoStack.remove(undoStack.size() - 1);
        for (int x = 0; x < CANVAS_WIDTH; x++) {
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                pixels[x][y] = previousState[x][y];
            }
        }
    }

    /**
     * Redo - Refaire la dernière action annulée
     */
    private void redo() {
        if (redoStack.isEmpty()) return;

        // Sauvegarder l'état actuel dans undo
        int[][] currentState = new int[CANVAS_WIDTH][CANVAS_HEIGHT];
        for (int x = 0; x < CANVAS_WIDTH; x++) {
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                currentState[x][y] = pixels[x][y];
            }
        }
        undoStack.add(currentState);

        // Restaurer l'état redo
        int[][] redoState = redoStack.remove(redoStack.size() - 1);
        for (int x = 0; x < CANVAS_WIDTH; x++) {
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                pixels[x][y] = redoState[x][y];
            }
        }
    }

    private void save() {
        // Convertir en tableau 1D
        int[] pixelData = new int[CANVAS_WIDTH * CANVAS_HEIGHT];
        for (int y = 0; y < CANVAS_HEIGHT; y++) {
            for (int x = 0; x < CANVAS_WIDTH; x++) {
                pixelData[y * CANVAS_WIDTH + x] = pixels[x][y];
            }
        }

        // Sauvegarder dans le cache local
        BannerImageCache.setImage(pixelData);

        // Envoyer au serveur
        PacketDistributor.sendToServer(new SaveBannerPacket(pixelData));

        // Fermer l'écran
        onClose();
    }

    private void clear() {
        for (int x = 0; x < CANVAS_WIDTH; x++) {
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                pixels[x][y] = 0xFFFFFFFF; // Blanc
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
