package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
    private int scrollOffset = 0; // Offset de scroll horizontal pour les longs textes

    // Selection
    private int selectionStart = -1;
    private int selectionEnd = -1;

    // Valid Minecraft color codes
    private static final String VALID_COLOR_CODES = "0123456789abcdefklmnor";

    // Textures pour les input fields
    private static final ResourceLocation INPUT_FIELD_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/settings/input-field-normal.png");
    private static final ResourceLocation INPUT_FIELD_FOCUS = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/settings/input-field-focus.png");

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
        this.scrollOffset = 0; // Reset scroll when setting text
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

        // Utiliser les images au lieu de g.fill
        ResourceLocation fieldTexture = focused ? INPUT_FIELD_FOCUS : INPUT_FIELD_NORMAL;
        if (enabled) {
            ImageRenderer.renderScaledImage(g, fieldTexture, x, y, width, height);
        } else {
            ImageRenderer.renderScaledImageWithAlpha(g, INPUT_FIELD_NORMAL, x, y, width, height, 0.5f);
        }

        // Ajuster le scroll pour garder le curseur visible
        if (focused) {
            int cursorX = font.width(text.substring(0, cursorPosition));
            int visibleWidth = width - 12; // Largeur visible (avec marges)

            // Si le curseur est à droite de la zone visible, scroller vers la droite
            if (cursorX - scrollOffset > visibleWidth) {
                scrollOffset = cursorX - visibleWidth;
            }
            // Si le curseur est à gauche de la zone visible, scroller vers la gauche
            else if (cursorX - scrollOffset < 0) {
                scrollOffset = cursorX;
            }
        }

        // Text avec scissor pour le clipping
        g.enableScissor(x + 4, y, x + width - 4, y + height);

        String displayText = text.isEmpty() ? placeholder : text;
        int textY = y + height / 2 - 4;

        // Appliquer l'offset de scroll au texte
        int textX = x + 6 - scrollOffset;

        // Render selection highlight
        if (focused && hasSelection()) {
            int min = getSelectionMin();
            int max = getSelectionMax();
            int selStartX = textX + font.width(text.substring(0, min));
            int selEndX = textX + font.width(text.substring(0, max));
            g.fill(selStartX, y + 4, selEndX, y + height - 4, 0xFF0060C0); // Bleu sélection
        }

        // Si le champ est vide (placeholder) ou focusé (mode édition avec &), utiliser une couleur fixe
        // Sinon (mode affichage avec §), activer le rendu des codes couleur
        if (text.isEmpty()) {
            g.drawString(font, displayText, textX, textY, 0xFF6a6a7e, false);
        } else if (focused) {
            // Mode édition: afficher & sans interpréter les couleurs
            g.drawString(font, displayText, textX, textY, 0xFFb8b8d0, false);
        } else {
            // Mode affichage: interpréter les codes couleur §
            g.drawString(font, displayText, textX, textY, 0xFFb8b8d0, true);
        }

        // Cursor
        if (focused && enabled && !hasSelection() && (cursorBlinkTimer / 10) % 2 == 0) {
            int cursorX = textX + font.width(text.substring(0, cursorPosition));
            g.fill(cursorX, y + 4, cursorX + 1, y + height - 4, 0xFFffffff);
        }

        g.disableScissor();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean wasInside = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

            // Utiliser setFocused() au lieu de modifier directement focused
            // pour déclencher la conversion des codes couleur
            setFocused(enabled && wasInside);

            if (focused) {
                // Calculate cursor position based on click (prendre en compte le scroll)
                int relX = (int) mouseX - x - 6 + scrollOffset;
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

                // Effacer la sélection lors d'un clic
                clearSelection();
            }

            return wasInside;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused || !enabled) return false;

        boolean shiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean ctrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

        // CTRL+A: Sélectionner tout
        if (ctrlPressed && keyCode == GLFW.GLFW_KEY_A) {
            selectAll();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (hasSelection()) {
                deleteSelection();
                if (onChange != null) onChange.accept(text);
            } else if (cursorPosition > 0) {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
                if (onChange != null) onChange.accept(text);
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (hasSelection()) {
                deleteSelection();
                if (onChange != null) onChange.accept(text);
            } else if (cursorPosition < text.length()) {
                text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                if (onChange != null) onChange.accept(text);
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (shiftPressed) {
                // Shift+Left: Étendre/réduire la sélection vers la gauche
                if (!hasSelection()) {
                    selectionStart = cursorPosition;
                }
                cursorPosition = Math.max(0, cursorPosition - 1);
                selectionEnd = cursorPosition;
            } else {
                // Left sans shift: Déplacer le curseur
                if (hasSelection()) {
                    cursorPosition = getSelectionMin();
                    clearSelection();
                } else {
                    cursorPosition = Math.max(0, cursorPosition - 1);
                }
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (shiftPressed) {
                // Shift+Right: Étendre/réduire la sélection vers la droite
                if (!hasSelection()) {
                    selectionStart = cursorPosition;
                }
                cursorPosition = Math.min(text.length(), cursorPosition + 1);
                selectionEnd = cursorPosition;
            } else {
                // Right sans shift: Déplacer le curseur
                if (hasSelection()) {
                    cursorPosition = getSelectionMax();
                    clearSelection();
                } else {
                    cursorPosition = Math.min(text.length(), cursorPosition + 1);
                }
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            if (shiftPressed) {
                // Shift+Home: Sélectionner jusqu'au début
                if (!hasSelection()) {
                    selectionStart = cursorPosition;
                }
                cursorPosition = 0;
                selectionEnd = cursorPosition;
            } else {
                cursorPosition = 0;
                clearSelection();
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            if (shiftPressed) {
                // Shift+End: Sélectionner jusqu'à la fin
                if (!hasSelection()) {
                    selectionStart = cursorPosition;
                }
                cursorPosition = text.length();
                selectionEnd = cursorPosition;
            } else {
                cursorPosition = text.length();
                clearSelection();
            }
            return true;
        }

        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!focused || !enabled) return false;

        if (Character.isLetterOrDigit(codePoint) || Character.isWhitespace(codePoint) || isPrintable(codePoint)) {
            // Si du texte est sélectionné, le remplacer
            if (hasSelection()) {
                deleteSelection();
            }

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

    /**
     * Vérifie si du texte est sélectionné
     */
    private boolean hasSelection() {
        return selectionStart >= 0 && selectionEnd >= 0 && selectionStart != selectionEnd;
    }

    /**
     * Obtient le début de la sélection (le plus petit index)
     */
    private int getSelectionMin() {
        return Math.min(selectionStart, selectionEnd);
    }

    /**
     * Obtient la fin de la sélection (le plus grand index)
     */
    private int getSelectionMax() {
        return Math.max(selectionStart, selectionEnd);
    }

    /**
     * Supprime la sélection actuelle
     */
    private void deleteSelection() {
        if (hasSelection()) {
            int min = getSelectionMin();
            int max = getSelectionMax();
            text = text.substring(0, min) + text.substring(max);
            cursorPosition = min;
            clearSelection();
        }
    }

    /**
     * Efface la sélection
     */
    private void clearSelection() {
        selectionStart = -1;
        selectionEnd = -1;
    }

    /**
     * Sélectionne tout le texte
     */
    private void selectAll() {
        selectionStart = 0;
        selectionEnd = text.length();
        cursorPosition = text.length();
    }

    /**
     * Convertit les codes couleur & en § pour l'affichage
     */
    private String convertToDisplayFormat(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '&' && i + 1 < input.length()) {
                char next = Character.toLowerCase(input.charAt(i + 1));
                if (VALID_COLOR_CODES.indexOf(next) >= 0) {
                    result.append('§').append(next);
                    i++; // Skip the next character
                    continue;
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Convertit les codes couleur § en & pour l'édition
     */
    private String convertToEditFormat(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '§' && i + 1 < input.length()) {
                char next = Character.toLowerCase(input.charAt(i + 1));
                if (VALID_COLOR_CODES.indexOf(next) >= 0) {
                    result.append('&').append(input.charAt(i + 1)); // Preserve original case
                    i++; // Skip the next character
                    continue;
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        boolean wasFocused = this.focused;
        this.focused = focused && enabled;

        // Convert color codes when focus changes
        if (wasFocused && !this.focused) {
            // Lost focus: convert & to § for display
            text = convertToDisplayFormat(text);
        } else if (!wasFocused && this.focused) {
            // Gained focus: convert § to & for editing
            text = convertToEditFormat(text);
        }
    }
}
