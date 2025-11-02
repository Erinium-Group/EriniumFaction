package fr.eriniumgroup.erinium_faction.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Bouton stylisé avec icône pour les attributs de joueur
 */
public class StatAttributeButton extends Button {
    private final ResourceLocation iconTexture;
    private boolean hovered = false;

    public StatAttributeButton(int x, int y, int width, int height, ResourceLocation iconTexture, Component tooltip, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.iconTexture = iconTexture;
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        // Fond du bouton avec style cyber-astral
        int bgColor = this.hovered ? 0xFF5a5a6e : 0xFF4a4a5e;
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);

        // Bordure
        int borderColor = this.hovered ? 0xFF00d2ff : 0x80667eea;
        // Haut
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, borderColor);
        // Bas
        guiGraphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, borderColor);
        // Gauche
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, borderColor);
        // Droite
        guiGraphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);

        // Icône à gauche
        RenderSystem.setShaderTexture(0, iconTexture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int iconSize = 16;
        int iconX = this.getX() + 8; // Décalé à gauche
        int iconY = this.getY() + (this.height - iconSize) / 2;

        guiGraphics.blit(iconTexture, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);

        RenderSystem.disableBlend();

        // Texte "+" au centre
        Font font = Minecraft.getInstance().font;
        String plusText = "+";
        int textColor = this.hovered ? 0xFF00ff88 : 0xFFffffff;
        int textX = this.getX() + (this.width - font.width(plusText)) / 2;
        int textY = this.getY() + (this.height - font.lineHeight) / 2;
        guiGraphics.drawString(font, plusText, textX, textY, textColor, false);
    }
}
