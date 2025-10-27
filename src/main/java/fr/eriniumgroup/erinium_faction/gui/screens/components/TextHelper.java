package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilitaire pour gérer les textes longs dans les GUI
 * - Text wrapping pour ScrollList
 * - Text scaling pour éviter les dépassements
 * - Hover scroll pour textes trop longs (comme Minecraft vanilla)
 */
public class TextHelper {

    // Tracker pour les animations de scroll
    private static final Map<String, ScrollState> scrollStates = new HashMap<>();
    private static final float SCROLL_SPEED = 0.15f; // pixels par tick (très lent)
    private static final int SCROLL_PAUSE_TICKS = 100; // pause au début/fin (longue pause)

    private static class ScrollState {
        float offset = 0;
        int pauseTicks = SCROLL_PAUSE_TICKS;
        boolean scrollingRight = false; // true = vers la droite (retour), false = vers la gauche
    }

    /**
     * Découpe un texte en plusieurs lignes pour tenir dans une largeur donnée
     * @param font Font utilisée
     * @param text Texte à découper
     * @param maxWidth Largeur maximale
     * @return Liste de lignes
     */
    public static List<String> wrapText(Font font, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;

            if (font.width(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Mot trop long, le couper
                    lines.add(word);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Dessine un texte avec wrapping sur plusieurs lignes
     * @param g GuiGraphics
     * @param font Font
     * @param text Texte
     * @param x Position X
     * @param y Position Y
     * @param maxWidth Largeur max
     * @param lineHeight Hauteur entre lignes
     * @param color Couleur
     * @param shadow Ombre
     * @return Hauteur totale occupée
     */
    public static int drawWrappedText(GuiGraphics g, Font font, String text, int x, int y, int maxWidth, int lineHeight, int color, boolean shadow) {
        List<String> lines = wrapText(font, text, maxWidth);
        int currentY = y;

        for (String line : lines) {
            g.drawString(font, line, x, currentY, color, shadow);
            currentY += lineHeight;
        }

        return lines.size() * lineHeight;
    }

    /**
     * Calcule le scale factor pour qu'un texte tienne dans une largeur donnée
     * @param font Font
     * @param text Texte
     * @param maxWidth Largeur max
     * @return Scale factor (1.0 = pas de scale, < 1.0 = réduit)
     */
    public static float getScaleFactor(Font font, String text, int maxWidth) {
        int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            return 1.0f;
        }
        return (float) maxWidth / textWidth;
    }

    /**
     * Dessine un texte avec scaling automatique si trop long
     * @param g GuiGraphics
     * @param font Font
     * @param text Texte
     * @param x Position X
     * @param y Position Y
     * @param maxWidth Largeur max
     * @param color Couleur
     * @param shadow Ombre
     */
    public static void drawScaledText(GuiGraphics g, Font font, String text, int x, int y, int maxWidth, int color, boolean shadow) {
        float scale = getScaleFactor(font, text, maxWidth);

        if (scale >= 1.0f) {
            g.drawString(font, text, x, y, color, shadow);
        } else {
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(scale, scale, 1.0f);
            g.drawString(font, text, 0, 0, color, shadow);
            g.pose().popPose();
        }
    }

    /**
     * Dessine un texte centré avec scaling automatique
     */
    public static void drawCenteredScaledText(GuiGraphics g, Font font, String text, int centerX, int y, int maxWidth, int color) {
        float scale = getScaleFactor(font, text, maxWidth);

        if (scale >= 1.0f) {
            g.drawCenteredString(font, text, centerX, y, color);
        } else {
            int textWidth = (int)(font.width(text) * scale);
            int startX = centerX - textWidth / 2;

            g.pose().pushPose();
            g.pose().translate(startX, y, 0);
            g.pose().scale(scale, scale, 1.0f);
            g.drawString(font, text, 0, 0, color, false);
            g.pose().popPose();
        }
    }

    /**
     * Dessine un texte avec scroll horizontal au hover
     * @param g GuiGraphics
     * @param font Font
     * @param text Texte
     * @param x Position X
     * @param y Position Y
     * @param maxWidth Largeur max
     * @param color Couleur
     * @param shadow Ombre
     * @param hovered Si la zone est survolée
     * @param scrollOffset Offset de scroll (géré par l'appelant)
     */
    public static void drawScrollingText(GuiGraphics g, Font font, String text, int x, int y, int maxWidth, int color, boolean shadow, boolean hovered, int scrollOffset) {
        int textWidth = font.width(text);

        if (textWidth <= maxWidth) {
            g.drawString(font, text, x, y, color, shadow);
            return;
        }

        // Activer le scissor pour clipper le texte
        g.enableScissor(x, y - 2, x + maxWidth, y + font.lineHeight + 2);

        if (hovered) {
            // Afficher avec offset de scroll
            g.drawString(font, text, x - scrollOffset, y, color, shadow);
        } else {
            // Afficher normalement (début du texte)
            g.drawString(font, text, x, y, color, shadow);
        }

        g.disableScissor();
    }

    /**
     * Calcule la hauteur nécessaire pour un texte avec wrapping
     */
    public static int getWrappedTextHeight(Font font, String text, int maxWidth, int lineHeight) {
        List<String> lines = wrapText(font, text, maxWidth);
        return lines.size() * lineHeight;
    }

    /**
     * Tronque un texte avec "..." si trop long
     */
    public static String truncateText(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);

        for (int i = text.length() - 1; i > 0; i--) {
            String truncated = text.substring(0, i) + ellipsis;
            if (font.width(truncated) <= maxWidth) {
                return truncated;
            }
        }

        return ellipsis;
    }

    /**
     * Dessine un texte avec scroll automatique au hover (comme Minecraft vanilla)
     * Le texte défile progressivement vers la gauche pour révéler le contenu
     *
     * @param g GuiGraphics
     * @param font Font
     * @param text Texte à afficher
     * @param x Position X
     * @param y Position Y
     * @param maxWidth Largeur maximale
     * @param color Couleur du texte
     * @param shadow Ombre
     * @param hovered Si la zone est survolée
     * @param id Identifiant unique pour tracker l'animation
     */
    public static void drawAutoScrollingText(GuiGraphics g, Font font, String text, int x, int y, int maxWidth, int color, boolean shadow, boolean hovered, String id) {
        int textWidth = font.width(text);

        // Si le texte tient dans la largeur, pas besoin de scroll
        if (textWidth <= maxWidth) {
            g.drawString(font, text, x, y, color, shadow);
            scrollStates.remove(id); // Nettoyer l'état si existe
            return;
        }

        // Obtenir ou créer l'état de scroll
        ScrollState state = scrollStates.computeIfAbsent(id, k -> new ScrollState());

        // Calculer le scroll max (combien de pixels on peut scroller)
        float maxScroll = textWidth - maxWidth;

        // Si pas hover, reset progressivement
        if (!hovered) {
            if (state.offset > 0) {
                state.offset = Math.max(0, state.offset - SCROLL_SPEED * 2); // Reset plus rapide
            }
            state.pauseTicks = SCROLL_PAUSE_TICKS;
            state.scrollingRight = false;
        } else {
            // Hover actif - gérer la pause et le scroll
            if (state.pauseTicks > 0) {
                state.pauseTicks--;
            } else {
                // Scroll actif
                if (!state.scrollingRight) {
                    // Scroll vers la gauche
                    state.offset += SCROLL_SPEED;
                    if (state.offset >= maxScroll) {
                        state.offset = maxScroll;
                        state.scrollingRight = true;
                        state.pauseTicks = SCROLL_PAUSE_TICKS;
                    }
                } else {
                    // Scroll vers la droite (retour)
                    state.offset -= SCROLL_SPEED;
                    if (state.offset <= 0) {
                        state.offset = 0;
                        state.scrollingRight = false;
                        state.pauseTicks = SCROLL_PAUSE_TICKS;
                    }
                }
            }
        }

        // Activer le scissor pour clipper le texte
        g.enableScissor(x, y - 2, x + maxWidth, y + font.lineHeight + 2);

        // Dessiner le texte avec l'offset de scroll
        g.drawString(font, text, x - (int)state.offset, y, color, shadow);

        g.disableScissor();
    }

    /**
     * Tick pour mettre à jour les animations - à appeler depuis la page
     */
    public static void tick() {
        // Les animations sont mises à jour dans drawAutoScrollingText
        // Cette méthode peut être utilisée pour nettoyer les états inactifs
    }

    /**
     * Nettoie les états de scroll pour libérer la mémoire
     */
    public static void clearScrollStates() {
        scrollStates.clear();
    }

    /**
     * Vérifie si un point est dans une zone rectangulaire
     */
    public static boolean isPointInBounds(double pointX, double pointY, int x, int y, int width, int height) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }
}
