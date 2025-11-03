package fr.eriniumgroup.erinium_faction.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Popup de paramètres pour la carte des factions
 */
public class MapSettingsPopup extends Screen {
    private final FactionMapScreen parent;
    private int gridColor;
    private int crossColor;

    // Sliders RGB pour la grille
    private int gridR, gridG, gridB, gridA;
    // Sliders RGB pour la croix
    private int crossR, crossG, crossB, crossA;

    private static final int POPUP_WIDTH = 300;
    private static final int POPUP_HEIGHT = 250;

    public MapSettingsPopup(FactionMapScreen parent, int gridColor, int crossColor) {
        super(Component.literal("Map Settings"));
        this.parent = parent;
        this.gridColor = gridColor;
        this.crossColor = crossColor;

        // Extraire ARGB de gridColor
        this.gridA = (gridColor >> 24) & 0xFF;
        this.gridR = (gridColor >> 16) & 0xFF;
        this.gridG = (gridColor >> 8) & 0xFF;
        this.gridB = gridColor & 0xFF;

        // Extraire ARGB de crossColor
        this.crossA = (crossColor >> 24) & 0xFF;
        this.crossR = (crossColor >> 16) & 0xFF;
        this.crossG = (crossColor >> 8) & 0xFF;
        this.crossB = crossColor & 0xFF;
    }

    @Override
    protected void init() {
        super.init();

        int x0 = (this.width - POPUP_WIDTH) / 2;
        int y0 = (this.height - POPUP_HEIGHT) / 2;

        // Bouton de fermeture
        Button closeBtn = Button.builder(Component.literal("Close"), b -> {
            this.onClose();
        }).bounds(x0 + POPUP_WIDTH / 2 - 40, y0 + POPUP_HEIGHT - 30, 80, 20).build();

        this.addRenderableWidget(closeBtn);
    }

    @Override
    public void onClose() {
        // Appliquer les couleurs
        parent.setGridColor((gridA << 24) | (gridR << 16) | (gridG << 8) | gridB);
        parent.setCrossColor((crossA << 24) | (crossR << 16) | (crossG << 8) | crossB);

        // Retourner à l'écran parent
        this.minecraft.setScreen(parent);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - pas de flou
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        // Assombrir l'arrière-plan
        g.fill(0, 0, this.width, this.height, 0xAA000000);

        int x0 = (this.width - POPUP_WIDTH) / 2;
        int y0 = (this.height - POPUP_HEIGHT) / 2;

        // Fond de la popup
        g.fill(x0, y0, x0 + POPUP_WIDTH, y0 + POPUP_HEIGHT, 0xFF2B2B2B);
        g.fill(x0 + 2, y0 + 2, x0 + POPUP_WIDTH - 2, y0 + POPUP_HEIGHT - 2, 0xFF1A1A1A);

        // Titre
        g.drawString(this.font, this.title, x0 + 10, y0 + 10, 0xFFFFFF, false);

        int yOffset = y0 + 30;

        // Section Grid Color
        g.drawString(this.font, "Grid Color:", x0 + 10, yOffset, 0xFFFFFF, false);

        // Preview de la grille à droite
        int previewColor = (gridA << 24) | (gridR << 16) | (gridG << 8) | gridB;
        g.fill(x0 + 220, yOffset, x0 + 270, yOffset + 50, 0xFF000000);
        g.fill(x0 + 221, yOffset + 1, x0 + 269, yOffset + 49, previewColor);

        yOffset += 15;
        yOffset = renderColorSliders(g, x0, yOffset, mouseX, mouseY, true);

        yOffset += 5;

        // Section Cross Color
        g.drawString(this.font, "Cross Color:", x0 + 10, yOffset, 0xFFFFFF, false);

        // Preview de la croix à droite
        int crossPreviewColor = (crossA << 24) | (crossR << 16) | (crossG << 8) | crossB;
        g.fill(x0 + 220, yOffset, x0 + 270, yOffset + 50, 0xFF000000);
        // Dessiner une croix
        int crossCenterX = x0 + 245;
        int crossCenterY = yOffset + 25;
        g.fill(crossCenterX - 1, yOffset + 5, crossCenterX + 1, yOffset + 45, crossPreviewColor);
        g.fill(x0 + 225, crossCenterY - 1, x0 + 265, crossCenterY + 1, crossPreviewColor);

        yOffset += 15;
        yOffset = renderColorSliders(g, x0, yOffset, mouseX, mouseY, false);

        super.render(g, mouseX, mouseY, partialTicks);
    }

    private int renderColorSliders(GuiGraphics g, int x0, int yOffset, int mouseX, int mouseY, boolean isGrid) {
        int sliderWidth = 180;
        int sliderHeight = 8;

        String[] labels = {"A:", "R:", "G:", "B:"};
        int[] values = isGrid ? new int[]{gridA, gridR, gridG, gridB} : new int[]{crossA, crossR, crossG, crossB};

        for (int i = 0; i < 4; i++) {
            g.drawString(this.font, labels[i], x0 + 10, yOffset + 2, 0xCCCCCC, false);

            // Fond du slider
            g.fill(x0 + 30, yOffset, x0 + 30 + sliderWidth, yOffset + sliderHeight, 0xFF555555);

            // Barre de valeur
            int barWidth = (int) ((values[i] / 255.0) * sliderWidth);
            g.fill(x0 + 30, yOffset, x0 + 30 + barWidth, yOffset + sliderHeight, 0xFF00FF00);

            // Valeur numérique
            g.drawString(this.font, String.valueOf(values[i]), x0 + 30 + sliderWidth + 5, yOffset + 1, 0xFFFFFF, false);

            yOffset += 15;
        }

        return yOffset;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x0 = (this.width - POPUP_WIDTH) / 2;
        int y0 = (this.height - POPUP_HEIGHT) / 2;
        int sliderWidth = 180;
        int sliderHeight = 8;

        // Grid sliders (y0 + 30 title, +15 offset = 45)
        int yOffset = y0 + 45;
        if (handleSliderClick(mouseX, mouseY, x0, yOffset, sliderWidth, sliderHeight, true, 0)) return true;
        yOffset += 15;
        if (handleSliderClick(mouseX, mouseY, x0, yOffset, sliderWidth, sliderHeight, true, 1)) return true;
        yOffset += 15;
        if (handleSliderClick(mouseX, mouseY, x0, yOffset, sliderWidth, sliderHeight, true, 2)) return true;
        yOffset += 15;
        if (handleSliderClick(mouseX, mouseY, x0, yOffset, sliderWidth, sliderHeight, true, 3)) return true;

        // Cross sliders (after 4 sliders * 15 + 5 gap + 15 title = y0 + 45 + 60 + 5 + 15 = y0 + 125)
        yOffset = y0 + 125;
        if (handleSliderClick(mouseX, mouseY, x0, yOffset, sliderWidth, sliderHeight, false, 0)) return true;
        yOffset += 15;
        if (handleSliderClick(mouseX, mouseY, x0, yOffset, sliderWidth, sliderHeight, false, 1)) return true;
        yOffset += 15;
        if (handleSliderClick(mouseX, mouseY, x0, yOffset, sliderWidth, sliderHeight, false, 2)) return true;
        yOffset += 15;
        if (handleSliderClick(mouseX, mouseY, x0, yOffset, sliderWidth, sliderHeight, false, 3)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleSliderClick(double mouseX, double mouseY, int x0, int yOffset, int sliderWidth, int sliderHeight, boolean isGrid, int component) {
        int sliderX = x0 + 30;
        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= yOffset && mouseY <= yOffset + sliderHeight) {
            int value = (int) (((mouseX - sliderX) / sliderWidth) * 255);
            value = Math.max(0, Math.min(255, value));

            if (isGrid) {
                switch (component) {
                    case 0 -> gridA = value;
                    case 1 -> gridR = value;
                    case 2 -> gridG = value;
                    case 3 -> gridB = value;
                }
            } else {
                switch (component) {
                    case 0 -> crossA = value;
                    case 1 -> crossR = value;
                    case 2 -> crossG = value;
                    case 3 -> crossB = value;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Gérer le drag sur les sliders
        mouseClicked(mouseX, mouseY, button);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
