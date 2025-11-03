package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.client.EFClient;
import fr.eriniumgroup.erinium_faction.gui.MinimapOverlayConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Écran de configuration de la minimap overlay
 */
public class MinimapOverlaySettingsScreen extends Screen {
    private final MinimapOverlayConfig config;

    // Sliders
    private int sizeSliderX, sizeSliderY;
    private boolean draggingSize = false;

    public MinimapOverlaySettingsScreen() {
        super(Component.literal("Minimap Overlay Settings"));
        this.config = EFClient.getMinimapConfig();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int startY = 60;

        // Boutons de position (haut gauche, haut droit, bas gauche, bas droit)
        int btnWidth = 80;
        int btnHeight = 20;
        int spacing = 10;

        addRenderableWidget(Button.builder(Component.literal("↖ Top Left"), b -> {
            config.position = MinimapOverlayConfig.Position.TOP_LEFT;
            config.save();
        }).bounds(centerX - btnWidth - spacing / 2, startY, btnWidth, btnHeight).build());

        addRenderableWidget(Button.builder(Component.literal("↗ Top Right"), b -> {
            config.position = MinimapOverlayConfig.Position.TOP_RIGHT;
            config.save();
        }).bounds(centerX + spacing / 2, startY, btnWidth, btnHeight).build());

        addRenderableWidget(Button.builder(Component.literal("↙ Bottom Left"), b -> {
            config.position = MinimapOverlayConfig.Position.BOTTOM_LEFT;
            config.save();
        }).bounds(centerX - btnWidth - spacing / 2, startY + btnHeight + 5, btnWidth, btnHeight).build());

        addRenderableWidget(Button.builder(Component.literal("↘ Bottom Right"), b -> {
            config.position = MinimapOverlayConfig.Position.BOTTOM_RIGHT;
            config.save();
        }).bounds(centerX + spacing / 2, startY + btnHeight + 5, btnWidth, btnHeight).build());

        // Toggle forme (carré/rond)
        startY += (btnHeight + 5) * 2 + 20;
        addRenderableWidget(Button.builder(
            Component.literal(config.roundShape ? "Shape: Round" : "Shape: Square"),
            b -> {
                config.roundShape = !config.roundShape;
                config.save();
                b.setMessage(Component.literal(config.roundShape ? "Shape: Round" : "Shape: Square"));
            }
        ).bounds(centerX - btnWidth, startY, btnWidth * 2, btnHeight).build());

        // Slider de taille
        sizeSliderX = centerX - 90;
        sizeSliderY = startY + btnHeight + 20;

        // Bouton Done
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            EFClient.reloadMinimapConfig();
            minecraft.setScreen(null);
        }).bounds(centerX - 50, height - 40, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - pas de fond, pas de flou!
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Render les widgets (boutons)
        super.render(g, mouseX, mouseY, partialTick);

        // Titre avec fond semi-transparent juste pour le texte
        String titleStr = title.getString();
        int titleWidth = font.width(titleStr);
        int titleX = (width - titleWidth) / 2;
        int titleY = 20;
        g.fill(titleX - 5, titleY - 2, titleX + titleWidth + 5, titleY + font.lineHeight + 2, 0xAA000000);
        g.drawCenteredString(font, title, width / 2, titleY, 0xFFFFFF);

        // Label position avec fond
        String posLabel = "Position:";
        int posWidth = font.width(posLabel);
        int posX = (width - posWidth) / 2;
        int posY = 45;
        g.fill(posX - 5, posY - 2, posX + posWidth + 5, posY + font.lineHeight + 2, 0xAA000000);
        g.drawCenteredString(font, posLabel, width / 2, posY, 0xCCCCCC);

        // Label forme avec fond
        String shapeLabel = "Shape:";
        int shapeWidth = font.width(shapeLabel);
        int shapeX = (width - shapeWidth) / 2;
        int shapeY = sizeSliderY - 55;
        g.fill(shapeX - 5, shapeY - 2, shapeX + shapeWidth + 5, shapeY + font.lineHeight + 2, 0xAA000000);
        g.drawCenteredString(font, shapeLabel, width / 2, shapeY, 0xCCCCCC);

        // Slider de taille
        renderSizeSlider(g, mouseX, mouseY);
    }

    private void renderSizeSlider(GuiGraphics g, int mouseX, int mouseY) {
        int sliderWidth = 180;
        int sliderHeight = 8;

        // Label "Size:" avec fond
        String sizeLabel = "Size:";
        int labelX = sizeSliderX - 30;
        int labelY = sizeSliderY + 2;
        int labelWidth = font.width(sizeLabel);
        g.fill(labelX - 2, labelY - 2, labelX + labelWidth + 2, labelY + font.lineHeight, 0xAA000000);
        g.drawString(font, sizeLabel, labelX, labelY, 0xCCCCCC, false);

        // Slider background
        g.fill(sizeSliderX, sizeSliderY, sizeSliderX + sliderWidth, sizeSliderY + sliderHeight, 0xFF555555);

        // Value bar (64 à 256)
        int minSize = 64;
        int maxSize = 256;
        int barWidth = (int) (((config.size - minSize) / (float) (maxSize - minSize)) * sliderWidth);
        g.fill(sizeSliderX, sizeSliderY, sizeSliderX + barWidth, sizeSliderY + sliderHeight, 0xFF00FF00);

        // Valeur numérique avec fond
        String valueStr = String.valueOf(config.size);
        int valueX = sizeSliderX + sliderWidth + 10;
        int valueY = sizeSliderY + 1;
        int valueWidth = font.width(valueStr);
        g.fill(valueX - 2, valueY - 2, valueX + valueWidth + 2, valueY + font.lineHeight, 0xAA000000);
        g.drawString(font, valueStr, valueX, valueY, 0xFFFFFF, false);

        // Handle dragging
        if (draggingSize) {
            int relX = mouseX - sizeSliderX;
            relX = Math.max(0, Math.min(sliderWidth, relX));
            config.size = (int) (minSize + (relX / (float) sliderWidth) * (maxSize - minSize));
            config.size = Math.max(minSize, Math.min(maxSize, config.size));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int sliderWidth = 180;
        int sliderHeight = 8;

        // Clic sur le slider de taille
        if (mouseX >= sizeSliderX && mouseX < sizeSliderX + sliderWidth &&
            mouseY >= sizeSliderY && mouseY < sizeSliderY + sliderHeight) {
            draggingSize = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingSize) {
            draggingSize = false;
            config.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingSize) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void removed() {
        super.removed();
        config.save();
        EFClient.reloadMinimapConfig();
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Ne pas mettre en pause le jeu
    }
}
