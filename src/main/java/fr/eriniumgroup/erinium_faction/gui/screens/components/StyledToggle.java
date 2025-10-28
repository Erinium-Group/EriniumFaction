package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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

    // Textures pour les toggles
    private static final ResourceLocation TOGGLE_OFF = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/settings/toggle-off.png");
    private static final ResourceLocation TOGGLE_ON = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/settings/toggle-on.png");

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

        // Utiliser les images au lieu de g.fill
        ResourceLocation toggleTexture = state ? TOGGLE_ON : TOGGLE_OFF;
        if (enabled) {
            ImageRenderer.renderScaledImage(g, toggleTexture, x, y, WIDTH, HEIGHT);
        } else {
            ImageRenderer.renderScaledImageWithAlpha(g, TOGGLE_OFF, x, y, WIDTH, HEIGHT, 0.5f);
        }
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
