package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Bouton stylisé pour le GUI de faction
 */
public class StyledButton {
    private final Font font;
    private String label;
    private final Runnable onClick;

    private int x, y, width, height;
    private boolean primary;
    private boolean danger;
    private boolean enabled = true;
    private boolean hovered = false;

    // Textures pour les boutons
    private static final ResourceLocation BUTTON_PRIMARY_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-primary-normal.png");
    private static final ResourceLocation BUTTON_PRIMARY_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-primary-hover.png");
    private static final ResourceLocation BUTTON_SECONDARY_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-secondary-normal.png");
    private static final ResourceLocation BUTTON_SECONDARY_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-secondary-hover.png");
    private static final ResourceLocation BUTTON_DANGER_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-danger-normal.png");
    private static final ResourceLocation BUTTON_DANGER_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-danger-hover.png");

    public StyledButton(Font font, String label, Runnable onClick) {
        this.font = font;
        this.label = label;
        this.onClick = onClick;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public void setDanger(boolean danger) {
        this.danger = danger;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setText(String label) {
        this.label = label;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        hovered = enabled && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        // Utiliser les images au lieu de g.fill
        if (enabled) {
            ResourceLocation buttonTexture;
            if (danger) {
                buttonTexture = hovered ? BUTTON_DANGER_HOVER : BUTTON_DANGER_NORMAL;
            } else if (primary) {
                buttonTexture = hovered ? BUTTON_PRIMARY_HOVER : BUTTON_PRIMARY_NORMAL;
            } else {
                buttonTexture = hovered ? BUTTON_SECONDARY_HOVER : BUTTON_SECONDARY_NORMAL;
            }
            ImageRenderer.renderScaledImage(g, buttonTexture, x, y, width, height);
        } else {
            // Bouton désactivé : utiliser une version semi-transparente
            ResourceLocation buttonTexture;
            if (danger) {
                buttonTexture = BUTTON_DANGER_NORMAL;
            } else if (primary) {
                buttonTexture = BUTTON_PRIMARY_NORMAL;
            } else {
                buttonTexture = BUTTON_SECONDARY_NORMAL;
            }
            ImageRenderer.renderScaledImageWithAlpha(g, buttonTexture, x, y, width, height, 0.5f);
        }

        // Text avec troncature si trop long
        int textColor = enabled ? 0xFFffffff : 0xFF6a6a7e;
        int maxTextWidth = width - 8; // Marge de 4px de chaque côté
        int textY = y + height / 2 - 4;

        // Utiliser TextHelper pour centrer et tronquer le texte
        TextHelper.drawCenteredScaledText(g, font, label, x + width / 2, textY, maxTextWidth, textColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && enabled && hovered) {
            onClick.run();
            return true;
        }
        return false;
    }
}
