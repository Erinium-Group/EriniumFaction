package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.features.minimap.MinimapConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Écran de configuration de la minimap.
 * Rend la minimap manuellement pour permettre le drag & drop.
 */
public class MinimapConfigScreen extends Screen {
    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 240; // Augmenté pour plus d'espace

    private int guiLeft;
    private int guiTop;

    private final Screen parent;

    public MinimapConfigScreen(Screen parent) {
        super(Component.translatable("gui.erinium_faction.minimap.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        guiLeft = (width - GUI_WIDTH) / 2;
        guiTop = (height - GUI_HEIGHT) / 2;

        int currentY = guiTop + 35; // Y de départ pour les boutons

        // === SECTION 1: SIZE CONTROLS ===
        // Bouton pour augmenter la taille
        addRenderableWidget(Button.builder(Component.literal("Size -"), btn -> {
            MinimapConfig.decreaseSize();
        }).bounds(guiLeft + 20, currentY, 60, 18).build());

        // Afficher taille actuelle
        addRenderableWidget(Button.builder(Component.literal(MinimapConfig.MINIMAP_FRAME_SIZE + "px"), btn -> {
            // Read-only button
        }).bounds(guiLeft + 90, currentY, 60, 18).build());

        // Bouton pour diminuer la taille
        addRenderableWidget(Button.builder(Component.literal("Size +"), btn -> {
            MinimapConfig.increaseSize();
        }).bounds(guiLeft + 160, currentY, 60, 18).build());

        // Bouton pour réinitialiser la taille
        addRenderableWidget(Button.builder(Component.literal("Reset"), btn -> {
            MinimapConfig.setMinimapSize(MinimapConfig.DEFAULT_SIZE);
            MinimapConfig.savePosition();
            minecraft.player.sendSystemMessage(Component.literal("§aMinimap size reset!"));
        }).bounds(guiLeft + 230, currentY, 50, 18).build());

        currentY += 28;

        // === SECTION 2: POSITION CONTROLS (ARROWS) ===
        int arrowCenterX = guiLeft + GUI_WIDTH / 2;
        int moveAmount = 5;

        // Flèche HAUT
        addRenderableWidget(Button.builder(Component.literal("↑"), btn -> {
            MinimapConfig.minimapY = Math.max(0, MinimapConfig.minimapY - moveAmount);
            MinimapConfig.savePosition();
        }).bounds(arrowCenterX - 10, currentY, 20, 20).build());

        // Flèche GAUCHE et DROITE
        addRenderableWidget(Button.builder(Component.literal("←"), btn -> {
            MinimapConfig.minimapX = Math.max(0, MinimapConfig.minimapX - moveAmount);
            MinimapConfig.savePosition();
        }).bounds(arrowCenterX - 30, currentY + 22, 20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("→"), btn -> {
            MinimapConfig.minimapX = Math.min(width - MinimapConfig.MINIMAP_FRAME_SIZE, MinimapConfig.minimapX + moveAmount);
            MinimapConfig.savePosition();
        }).bounds(arrowCenterX + 10, currentY + 22, 20, 20).build());

        // Flèche BAS
        addRenderableWidget(Button.builder(Component.literal("↓"), btn -> {
            MinimapConfig.minimapY = Math.min(height - MinimapConfig.MINIMAP_FRAME_SIZE, MinimapConfig.minimapY + moveAmount);
            MinimapConfig.savePosition();
        }).bounds(arrowCenterX - 10, currentY + 44, 20, 20).build());

        // Reset position button
        addRenderableWidget(Button.builder(Component.literal("Reset Pos"), btn -> {
            MinimapConfig.minimapX = 10;
            MinimapConfig.minimapY = 10;
            MinimapConfig.savePosition();
            minecraft.player.sendSystemMessage(Component.literal("§aMinimap position reset!"));
        }).bounds(guiLeft + 20, currentY + 22, 70, 20).build());

        currentY += 75;

        // === SECTION 3: UPDATE RADIUS ===
        addRenderableWidget(Button.builder(Component.literal("Radius -"), btn -> {
            MinimapConfig.CHUNK_UPDATE_RADIUS = Math.max(4, MinimapConfig.CHUNK_UPDATE_RADIUS - 2);
            MinimapConfig.savePosition();
            minecraft.player.sendSystemMessage(Component.literal("§aUpdate radius: " + MinimapConfig.CHUNK_UPDATE_RADIUS));
        }).bounds(guiLeft + 20, currentY, 70, 18).build());

        addRenderableWidget(Button.builder(Component.literal(MinimapConfig.CHUNK_UPDATE_RADIUS + " chunks"), btn -> {
            // Read-only button
        }).bounds(guiLeft + 100, currentY, 80, 18).build());

        addRenderableWidget(Button.builder(Component.literal("Radius +"), btn -> {
            MinimapConfig.CHUNK_UPDATE_RADIUS = Math.min(20, MinimapConfig.CHUNK_UPDATE_RADIUS + 2);
            MinimapConfig.savePosition();
            minecraft.player.sendSystemMessage(Component.literal("§aUpdate radius: " + MinimapConfig.CHUNK_UPDATE_RADIUS));
        }).bounds(guiLeft + 190, currentY, 70, 18).build());

        currentY += 28;

        // === BOUTON DONE EN BAS ===
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> {
            onClose();
        }).bounds(guiLeft + GUI_WIDTH / 2 - 50, guiTop + GUI_HEIGHT - 30, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - on veut voir la minimap en dessous, pas de flou !
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // NOTE: L'overlay affiche déjà la minimap automatiquement pour MinimapConfigScreen
        // (voir MinimapOverlay lignes 64-71)

        // GUI Background (semi-transparent pour voir la minimap)
        graphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xDD1a1a1a);

        // Border
        graphics.fill(guiLeft - 2, guiTop - 2, guiLeft + GUI_WIDTH + 2, guiTop, 0xFFd4af37);
        graphics.fill(guiLeft - 2, guiTop + GUI_HEIGHT, guiLeft + GUI_WIDTH + 2, guiTop + GUI_HEIGHT + 2, 0xFFd4af37);
        graphics.fill(guiLeft - 2, guiTop, guiLeft, guiTop + GUI_HEIGHT, 0xFFd4af37);
        graphics.fill(guiLeft + GUI_WIDTH, guiTop, guiLeft + GUI_WIDTH + 2, guiTop + GUI_HEIGHT, 0xFFd4af37);

        // Title
        graphics.drawCenteredString(font, title, guiLeft + GUI_WIDTH / 2, guiTop + 10, 0xFFffd700);

        // Instructions
        int instructY = guiTop + 165;
        graphics.drawCenteredString(font, "§7Use arrows to move minimap", guiLeft + GUI_WIDTH / 2, instructY, 0xFFAAAAAA);
        graphics.drawCenteredString(font, "§7Position: " + MinimapConfig.minimapX + ", " + MinimapConfig.minimapY,
            guiLeft + GUI_WIDTH / 2, instructY + 12, 0xFF888888);

        // Render widgets
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
