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

    public BannerEditorScreen() {
        super(Component.literal("Éditeur de Bannière"));
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

        addRenderableWidget(Button.builder(Component.literal("Sauvegarder"), btn -> save())
            .bounds(guiLeft + guiWidth / 2 - buttonWidth - 5, buttonY, buttonWidth, buttonHeight)
            .build());

        addRenderableWidget(Button.builder(Component.literal("Effacer"), btn -> clear())
            .bounds(guiLeft + guiWidth / 2 + 5, buttonY, buttonWidth, buttonHeight)
            .build());

        addRenderableWidget(Button.builder(Component.literal("Annuler"), btn -> onClose())
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
        graphics.drawString(font, "Couleur:", paletteX, paletteY - 15, 0xFFFFFF);

        // Appeler super.render() SANS renderBackground
        // On rend juste les widgets (boutons)
        for (var widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderCanvas(GuiGraphics graphics) {
        int scaledWidth = CANVAS_WIDTH * pixelSize;
        int scaledHeight = CANVAS_HEIGHT * pixelSize;

        // Fond du canvas (bordure)
        graphics.fill(canvasX - 2, canvasY - 2, canvasX + scaledWidth + 2, canvasY + scaledHeight + 2, 0xFF000000);

        // Pixels
        for (int x = 0; x < CANVAS_WIDTH; x++) {
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                int screenX = canvasX + x * pixelSize;
                int screenY = canvasY + y * pixelSize;
                graphics.fill(screenX, screenY, screenX + pixelSize, screenY + pixelSize, pixels[x][y]);
            }
        }
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

        // Dessin sur le canvas
        if (button == 0) { // Clic gauche
            drawPixel(mouseX, mouseY);
            isDrawing = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
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
        }
        return false;
    }

    private void drawPixel(double mouseX, double mouseY) {
        int pixelX = ((int) mouseX - canvasX) / pixelSize;
        int pixelY = ((int) mouseY - canvasY) / pixelSize;

        if (pixelX >= 0 && pixelX < CANVAS_WIDTH && pixelY >= 0 && pixelY < CANVAS_HEIGHT) {
            pixels[pixelX][pixelY] = currentColor;
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
