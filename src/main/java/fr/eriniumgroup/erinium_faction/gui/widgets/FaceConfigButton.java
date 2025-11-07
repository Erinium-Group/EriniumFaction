package fr.eriniumgroup.erinium_faction.gui.widgets;

import fr.eriniumgroup.erinium_faction.common.block.entity.FaceConfiguration;
import fr.eriniumgroup.erinium_faction.gui.screens.FaceConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Bouton pour ouvrir la configuration des faces d'une machine
 */
public class FaceConfigButton extends Button {
    private final BlockPos machinePos;
    private final FaceConfiguration config;
    private final Screen parentScreen;

    // Icône simple - on peut la remplacer par une texture plus tard
    private static final int ICON_SIZE = 16;

    public FaceConfigButton(int x, int y, BlockPos machinePos, FaceConfiguration config, Screen parentScreen) {
        super(x, y, 20, 20, Component.translatable("gui.erinium_faction.face_config"),
                btn -> {
                    Minecraft.getInstance().setScreen(new FaceConfigScreen(parentScreen, machinePos, config));
                },
                DEFAULT_NARRATION);
        this.machinePos = machinePos;
        this.config = config;
        this.parentScreen = parentScreen;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fond du bouton
        int bgColor = this.isHovered() ? 0xFF4A4A4A : 0xFF2A2A2A;
        graphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, bgColor);

        // Bordure
        int borderColor = this.isHovered() ? 0xFFFFFFFF : 0xFF808080;
        graphics.fill(getX(), getY(), getX() + this.width, getY() + 1, borderColor);
        graphics.fill(getX(), getY() + this.height - 1, getX() + this.width, getY() + this.height, borderColor);
        graphics.fill(getX(), getY(), getX() + 1, getY() + this.height, borderColor);
        graphics.fill(getX() + this.width - 1, getY(), getX() + this.width, getY() + this.height, borderColor);

        // Icône simple (grille 2x2)
        int iconX = getX() + (this.width - ICON_SIZE) / 2;
        int iconY = getY() + (this.height - ICON_SIZE) / 2;

        // Dessiner une petite grille pour représenter les faces
        graphics.fill(iconX + 1, iconY + 1, iconX + 7, iconY + 7, 0xFF4169E1); // Bleu
        graphics.fill(iconX + 9, iconY + 1, iconX + 15, iconY + 7, 0xFFFF4500); // Orange
        graphics.fill(iconX + 1, iconY + 9, iconX + 7, iconY + 15, 0xFF9370DB); // Violet
        graphics.fill(iconX + 9, iconY + 9, iconX + 15, iconY + 15, 0xFFFFD700); // Or

        // Tooltip au survol
        if (this.isHovered()) {
            // Le tooltip sera affiché par le Screen parent
        }
    }

    @Override
    protected void renderScrollingString(GuiGraphics graphics, net.minecraft.client.gui.Font font, int i, int j) {
        // Ne pas afficher de texte sur le bouton
    }
}

