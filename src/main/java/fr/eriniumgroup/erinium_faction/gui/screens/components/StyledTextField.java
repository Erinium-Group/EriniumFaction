package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Champ de texte stylisé pour le GUI de faction
 */
public class StyledTextField {
    private final Font font;
    private String text = "";
    private String placeholder = "";
    private Consumer<String> onChange;

    private int x, y, width, height;
    private boolean focused = false;
    private boolean enabled = true;
    private int maxLength = 32;

    private int cursorPosition = 0;
    private int cursorBlinkTimer = 0;

    public StyledTextField(Font font) {
        this.font = font;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void setText(String text) {
        this.text = text.substring(0, Math.min(text.length(), maxLength));
        this.cursorPosition = this.text.length();
    }

    public String getText() {
        return text;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) focused = false;
    }

    public void setOnChange(Consumer<String> callback) {
        this.onChange = callback;
    }

    public void tick() {
        if (focused) {
            cursorBlinkTimer++;
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        boolean hovered = enabled && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        // Background
        int bgColor = enabled ? 0x802a2a3e : 0x401a1a1e;
        g.fill(x, y, x + width, y + height, bgColor);

        // Border
        int borderColor = focused ? 0xFF00d2ff : (hovered ? 0x80667eea : 0x50667eea);
        g.fill(x, y, x + width, y + 1, borderColor);
        g.fill(x, y + height - 1, x + width, y + height, borderColor);

        // Text
        g.enableScissor(x + 4, y, x + width - 4, y + height);

        String displayText = text.isEmpty() ? placeholder : text;
        int textColor = text.isEmpty() ? 0xFF6a6a7e : 0xFFb8b8d0;
        int textY = y + height / 2 - 4;

        g.drawString(font, displayText, x + 6, textY, textColor, false);

        // Cursor
        if (focused && enabled && (cursorBlinkTimer / 10) % 2 == 0) {
            int cursorX = x + 6 + font.width(text.substring(0, cursorPosition));
            g.fill(cursorX, y + 4, cursorX + 1, y + height - 4, 0xFFffffff);
        }

        g.disableScissor();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean wasInside = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
            focused = enabled && wasInside;

            if (focused) {
                // Calculate cursor position based on click
                int relX = (int) mouseX - x - 6;
                cursorPosition = 0;
                int totalWidth = 0;

                for (int i = 0; i < text.length(); i++) {
                    int charWidth = font.width(text.substring(i, i + 1));
                    if (totalWidth + charWidth / 2 > relX) {
                        break;
                    }
                    totalWidth += charWidth;
                    cursorPosition++;
                }
            }

            return wasInside;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused || !enabled) return false;

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursorPosition > 0) {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
                if (onChange != null) onChange.accept(text);
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (cursorPosition < text.length()) {
                text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                if (onChange != null) onChange.accept(text);
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            cursorPosition = Math.max(0, cursorPosition - 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            cursorPosition = Math.min(text.length(), cursorPosition + 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            cursorPosition = 0;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            cursorPosition = text.length();
            return true;
        }

        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!focused || !enabled) return false;

        if (Character.isLetterOrDigit(codePoint) || Character.isWhitespace(codePoint) || isPrintable(codePoint)) {
            if (text.length() < maxLength) {
                text = text.substring(0, cursorPosition) + codePoint + text.substring(cursorPosition);
                cursorPosition++;
                if (onChange != null) onChange.accept(text);
                return true;
            }
        }

        return false;
    }

    private boolean isPrintable(char c) {
        return c >= 32 && c < 127;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused && enabled;
    }
}
