package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/**
 * Toggle/Switch stylisé pour le GUI de faction
 */
public class StyledToggle {
    private final Font font;
    private String label;
    private boolean state;
    private Consumer<Boolean> onChange;

    private int x, y;
    private boolean enabled = true;

    private static final int WIDTH = 40;
    private static final int HEIGHT = 20;

    public StyledToggle(Font font, boolean initialState) {
        this.font = font;
        this.state = initialState;
        this.label = "";
    }

    public StyledToggle(Font font, String label, boolean initialState, Consumer<Boolean> onChange) {
        this.font = font;
        this.label = label;
        this.state = initialState;
        this.onChange = onChange;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setBounds(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean getState() {
        return state;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOnChange(Consumer<Boolean> callback) {
        this.onChange = callback;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        boolean hovered = enabled && mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;

        // Label before toggle if present
        if (label != null && !label.isEmpty()) {
            g.drawString(font, label, x + WIDTH + 8, y + 6, 0xFFffffff, false);
        }

        // Background
        int bgColor = state ? 0xFF10b981 : 0xFFef4444;
        if (!enabled) bgColor = 0xFF4a4a5e;
        if (hovered && enabled) {
            // Slightly brighter when hovered
            int r = (bgColor >> 16) & 0xFF;
            int gr = (bgColor >> 8) & 0xFF;
            int b = bgColor & 0xFF;
            r = Math.min(255, r + 20);
            gr = Math.min(255, gr + 20);
            b = Math.min(255, b + 20);
            bgColor = 0xFF000000 | (r << 16) | (gr << 8) | b;
        }

        g.fill(x, y, x + WIDTH, y + HEIGHT, bgColor);

        // Thumb
        int thumbX = state ? x + WIDTH - 18 : x + 2;
        int thumbY = y + 2;
        g.fill(thumbX, thumbY, thumbX + 16, thumbY + 16, 0xFFffffff);

        // ON/OFF text inside toggle
        String toggleText = state ? "ON" : "OFF";
        int labelX = state ? x + 4 : x + WIDTH - 20;
        g.drawCenteredString(font, toggleText, labelX + 8, y + 6, 0xFFffffff);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && enabled) {
            boolean inside = mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;
            if (inside) {
                state = !state;
                if (onChange != null) onChange.accept(state);
                return true;
            }
        }
        return false;
    }
}
