package fr.eriniumgroup.erinium_faction.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = EFC.MODID, value = Dist.CLIENT)
public class FactionTitleOverlay extends Overlay {

    // Etat courant du titre à afficher
    private static Component title = null;
    private static Component subtitle = null;
    private static long startMs = 0L;
    private static int fadeInMs = 500;  // par défaut
    private static int stayMs = 1500;   // par défaut
    private static int fadeOutMs = 500; // par défaut

    // Marges internes du cadre
    private static final int padLeft = 16, padRight = 16, padTop = 12, padBottom = 12;

    // Template de cadre personnalisable
    public static class FrameTemplate {
        public int bgColor;
        public int borderThickness;
        public int borderRadius;
        public boolean animatedBorder;
        public float rainbowSpeed;
        public int borderColor;
        public FrameTemplate(int bgColor, int borderThickness, int borderRadius, boolean animatedBorder, float rainbowSpeed, int borderColor) {
            this.bgColor = bgColor;
            this.borderThickness = borderThickness;
            this.borderRadius = borderRadius;
            this.animatedBorder = animatedBorder;
            this.rainbowSpeed = rainbowSpeed;
            this.borderColor = borderColor;
        }
    }

    private static final Map<String, FrameTemplate> frameTemplates = new HashMap<>();
    private static String currentFrameKey = "default";

    static {
        // Template par défaut : arrondi très visible
        frameTemplates.put("default", new FrameTemplate(0x222244, 4, 40, true, 0.008f, 0xFFFFFF));
        frameTemplates.put("warzone", new FrameTemplate(0x442222, 4, 40, true, 0.012f, 0xFF4444));
        frameTemplates.put("wilderness", new FrameTemplate(0x224422, 4, 40, false, 0.0f, 0x44FF44));
    }

    public static void setCurrentFrame(String key) {
        if (frameTemplates.containsKey(key)) currentFrameKey = key;
        else currentFrameKey = "default";
    }

    public static void addFrameTemplate(String key, FrameTemplate template) {
        frameTemplates.put(key, template);
    }

    /**
     * Déclenche l’affichage d’un titre au centre de l’écran.
     *
     * @param titleText    texte principal (nul/blank = rien)
     * @param subtitleText sous-titre optionnel (peut être nul ou vide)
     * @param fadeIn       durée fade-in en ms
     * @param stay         durée affichage plein en ms
     * @param fadeOut      durée fade-out en ms
     */
    public static void showTitle(String titleText, String subtitleText, int fadeIn, int stay, int fadeOut) {
        if (titleText == null || titleText.isBlank()) {
            clear();
            return;
        }
        // Appliquer la conversion & -> § uniquement pour codes valides
        String parsedTitle = applyAmpersandFormatting(titleText);
        String parsedSubtitle = (subtitleText != null) ? applyAmpersandFormatting(subtitleText) : null;
        title = Component.literal(parsedTitle);
        subtitle = (parsedSubtitle != null && !parsedSubtitle.isBlank()) ? Component.literal(parsedSubtitle) : null;
        fadeInMs = Math.max(0, fadeIn);
        stayMs = Math.max(0, stay);
        fadeOutMs = Math.max(0, fadeOut);
        startMs = System.currentTimeMillis();
    }

    /**
     * Efface le titre courant.
     */
    public static void clear() {
        title = null;
        subtitle = null;
        startMs = 0L;
    }

    private static float computeAlpha(long nowMs) {
        int total = fadeInMs + stayMs + fadeOutMs;
        if (total <= 0) return 0f;
        long elapsed = nowMs - startMs;
        if (elapsed < 0) return 0f;
        if (elapsed <= fadeInMs) {
            return fadeInMs == 0 ? 1f : (elapsed / (float) fadeInMs);
        }
        if (elapsed <= (long) fadeInMs + stayMs) {
            return 1f;
        }
        long outElapsed = elapsed - fadeInMs - stayMs;
        if (outElapsed <= fadeOutMs) {
            return fadeOutMs == 0 ? 0f : (1f - (outElapsed / (float) fadeOutMs));
        }
        return 0f;
    }

    // Dessine un rectangle à coins arrondis (approximation pixel) de couleur ARGB
    private static void drawRoundedRect(GuiGraphics g, int x, int y, int w, int h, int radius, int argb) {
        radius = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (radius == 0) {
            g.fill(x, y, x + w, y + h, argb);
            return;
        }
        int left = x, right = x + w - 1, top = y, bottom = y + h - 1;
        // bandes horizontales arrondies en haut et bas
        for (int i = 0; i < radius; i++) {
            int inset = radius - i - 1;
            // ligne du haut
            g.fill(left + inset, top + i, right - inset + 1, top + i + 1, argb);
            // ligne du bas
            g.fill(left + inset, bottom - i, right - inset + 1, bottom - i + 1, argb);
        }
        // zone centrale
        g.fill(left, top + radius, right + 1, bottom - radius + 1, argb);
    }

    // Conversion des codes & en § pour les couleurs Minecraft
    private static String applyAmpersandFormatting(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder out = new StringBuilder(input.length());
        int i = 0, n = input.length();
        while (i < n) {
            char c = input.charAt(i);
            if (c == '&' && i + 1 < n) {
                char code = input.charAt(i + 1);
                char lower = Character.toLowerCase(code);
                boolean isColor = (lower >= '0' && lower <= '9') || (lower >= 'a' && lower <= 'f');
                boolean isFormat = (lower == 'k' || lower == 'l' || lower == 'm' || lower == 'n' || lower == 'o' || lower == 'r');
                if (isColor || isFormat) {
                    out.append('§').append(lower);
                    i += 2;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        if (mc.screen != null) return;
        if (title == null) return;

        long now = System.currentTimeMillis();
        float alpha = computeAlpha(now);
        if (alpha <= 0f) {
            clear();
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        int a = Math.min(255, Math.max(0, (int) (alpha * 255f)));
        int fgTitle = (a << 24) | 0xFFFFFF;
        int fgSub = (a << 24) | 0xE0E0E0; // un peu moins vif pour le sous-titre

        var font = mc.font;
        int spacing = 6;
        boolean hasSubtitle = subtitle != null && !subtitle.getString().isEmpty();

        // Calcul du texte et du cadre
        int minWidth = 220;
        int minHeight = 48;
        int maxTextWidth = w / 2;
        java.util.List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal(title.getString()), maxTextWidth);
        if (lines.isEmpty()) lines.add(net.minecraft.util.FormattedCharSequence.EMPTY);
        java.util.List<net.minecraft.util.FormattedCharSequence> subLines = hasSubtitle ? font.split(Component.literal(subtitle.getString()), maxTextWidth) : java.util.List.of();
        int totalTextHeight = lines.size() * font.lineHeight + (lines.size() - 1) * spacing;
        int subTextHeight = hasSubtitle ? (subLines.size() * font.lineHeight + (subLines.size() - 1) * spacing + spacing) : 0;
        int textBlockHeight = totalTextHeight + subTextHeight;
        int maxLineWidth = 0;
        for (var l : lines) maxLineWidth = Math.max(maxLineWidth, font.width(l));
        for (var l : subLines) maxLineWidth = Math.max(maxLineWidth, font.width(l));
        int boxW = Math.max(minWidth, maxLineWidth + padLeft + padRight);
        int boxH = Math.max(minHeight, textBlockHeight + padTop + padBottom);
        int boxLeft = (w - boxW) / 2;
        int boxTop = (h - boxH) / 2;

        // Récupère le template de cadre courant
        FrameTemplate frame = frameTemplates.getOrDefault(currentFrameKey, frameTemplates.get("default"));

        // Fond arrondi semi-transparent
        int bg = (a << 24) | (frame.bgColor & 0xFFFFFF);
        drawRoundedRect(g, boxLeft, boxTop, boxW, boxH, frame.borderRadius, bg);

        // Bords animés ou fixes selon le template
        if (frame.animatedBorder) {
            float rainbowSpeed = frame.rainbowSpeed;
            long t = System.currentTimeMillis();
            int r = frame.borderRadius;
            // Bord haut arrondi
            for (int i = 0; i < boxW; i++) {
                int y0, y1;
                if (i < r) {
                    int inset = r - i - 1;
                    y0 = boxTop + inset;
                    y1 = boxTop + frame.borderThickness + inset;
                } else if (i >= boxW - r) {
                    int inset = i - (boxW - r);
                    y0 = boxTop + inset;
                    y1 = boxTop + frame.borderThickness + inset;
                } else {
                    y0 = boxTop;
                    y1 = boxTop + frame.borderThickness;
                }
                float hue = ((t * rainbowSpeed) + i * 0.01f) % 1.0f;
                int color = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | (a << 24);
                g.fill(boxLeft + i, y0, boxLeft + i + 1, y1, color);
            }
            // Bord bas arrondi
            for (int i = 0; i < boxW; i++) {
                int y0, y1;
                if (i < r) {
                    int inset = r - i - 1;
                    y0 = boxTop + boxH - frame.borderThickness - inset;
                    y1 = boxTop + boxH - inset;
                } else if (i >= boxW - r) {
                    int inset = i - (boxW - r);
                    y0 = boxTop + boxH - frame.borderThickness - inset;
                    y1 = boxTop + boxH - inset;
                } else {
                    y0 = boxTop + boxH - frame.borderThickness;
                    y1 = boxTop + boxH;
                }
                float hue = ((t * rainbowSpeed) + i * 0.01f) % 1.0f;
                int color = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | (a << 24);
                g.fill(boxLeft + i, y0, boxLeft + i + 1, y1, color);
            }
            // Bord gauche arrondi
            for (int i = 0; i < boxH; i++) {
                int x0, x1;
                if (i < r) {
                    int inset = r - i - 1;
                    x0 = boxLeft + inset;
                    x1 = boxLeft + frame.borderThickness + inset;
                } else if (i >= boxH - r) {
                    int inset = i - (boxH - r);
                    x0 = boxLeft + inset;
                    x1 = boxLeft + frame.borderThickness + inset;
                } else {
                    x0 = boxLeft;
                    x1 = boxLeft + frame.borderThickness;
                }
                float hue = ((t * rainbowSpeed) + i * 0.01f) % 1.0f;
                int color = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | (a << 24);
                g.fill(x0, boxTop + i, x1, boxTop + i + 1, color);
            }
            // Bord droit arrondi
            for (int i = 0; i < boxH; i++) {
                int x0, x1;
                if (i < r) {
                    int inset = r - i - 1;
                    x0 = boxLeft + boxW - frame.borderThickness - inset;
                    x1 = boxLeft + boxW - inset;
                } else if (i >= boxH - r) {
                    int inset = i - (boxH - r);
                    x0 = boxLeft + boxW - frame.borderThickness - inset;
                    x1 = boxLeft + boxW - inset;
                } else {
                    x0 = boxLeft + boxW - frame.borderThickness;
                    x1 = boxLeft + boxW;
                }
                float hue = ((t * rainbowSpeed) + i * 0.01f) % 1.0f;
                int color = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | (a << 24);
                g.fill(x0, boxTop + i, x1, boxTop + i + 1, color);
            }
        } else {
            int borderColor = (a << 24) | (frame.borderColor & 0xFFFFFF);
            // Bord haut
            g.fill(boxLeft, boxTop, boxLeft + boxW, boxTop + frame.borderThickness, borderColor);
            // Bord bas
            g.fill(boxLeft, boxTop + boxH - frame.borderThickness, boxLeft + boxW, boxTop + boxH, borderColor);
            // Bord gauche
            g.fill(boxLeft, boxTop, boxLeft + frame.borderThickness, boxTop + boxH, borderColor);
            // Bord droit
            g.fill(boxLeft + boxW - frame.borderThickness, boxTop, boxLeft + boxW, boxTop + boxH, borderColor);
        }

        // Texte centré verticalement et horizontalement
        int textY = boxTop + padTop + (boxH - padTop - padBottom - textBlockHeight) / 2;
        int lineY = textY;
        for (var line : lines) {
            int lineWidth = font.width(line);
            int lineX = boxLeft + padLeft + Math.max(0, (boxW - padLeft - padRight - lineWidth) / 2);
            g.drawString(font, line, lineX, lineY, fgTitle, true);
            lineY += font.lineHeight + spacing;
        }
        // Sous-titre (si présent, centré sous le texte)
        if (hasSubtitle) {
            for (var subLine : subLines) {
                int subLineWidth = font.width(subLine);
                int subLineX = boxLeft + padLeft + Math.max(0, (boxW - padLeft - padRight - subLineWidth) / 2);
                g.drawString(font, subLine, subLineX, lineY, fgSub, true);
                lineY += font.lineHeight + spacing;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int i1, float v) {
        // Rendu via l’event
    }
}
