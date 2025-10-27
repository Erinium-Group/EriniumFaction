package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Bouton stylisé pour le GUI de faction
 */
public class StyledButton {
    private final Font font;
    private String label;
    private final Runnable onClick;

    private int x, y, width, height;
    private boolean primary;
    private boolean enabled = true;
    private boolean hovered = false;

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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setText(String label) {
        this.label = label;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        hovered = enabled && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        int bgColor;
        if (!enabled) {
            bgColor = 0x801a1a1e;
        } else if (primary) {
            bgColor = hovered ? 0xFF7a8eee : 0xFF667eea;
        } else {
            bgColor = hovered ? 0xFF3a3a4e : 0xCC2a2a3e;
        }

        // Background
        g.fill(x, y, x + width, y + height, bgColor);

        // Top border
        int borderColor = primary ? 0x8000d2ff : 0x50667eea;
        g.fill(x, y, x + width, y + 1, borderColor);

        // Text
        int textColor = enabled ? 0xFFffffff : 0xFF6a6a7e;
        int textX = x + width / 2 - font.width(label) / 2;
        int textY = y + height / 2 - 4;
        g.drawString(font, label, textX, textY, textColor, primary && enabled);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && enabled && hovered) {
            onClick.run();
            return true;
        }
        return false;
    }
}
