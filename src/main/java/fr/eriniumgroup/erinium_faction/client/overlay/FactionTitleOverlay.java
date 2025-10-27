package fr.eriniumgroup.erinium_faction.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

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

    /**
     * Convertit les codes de style "&" en codes format Minecraft "§" uniquement si valides.
     * Pris en charge: couleurs 0-9, a-f et formats k, l, m, n, o, r (insensible à la casse).
     */
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
                    out.append('\u00A7').append(lower);
                    i += 2;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    /**
     * Déclenche l’affichage d’un titre au centre de l’écran.
     * @param titleText   texte principal (nul/blank = rien)
     * @param subtitleText sous-titre optionnel (peut être nul ou vide)
     * @param fadeIn durée fade-in en ms
     * @param stay   durée affichage plein en ms
     * @param fadeOut durée fade-out en ms
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

    /** Efface le titre courant. */
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

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        if (mc.screen != null) return;
        if (title == null) return;

        long now = System.currentTimeMillis();
        float alpha = computeAlpha(now);
        if (alpha <= 0f) { clear(); return; }

        GuiGraphics g = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        int a = Math.min(255, Math.max(0, (int) (alpha * 255f)));
        int fgTitle = (a << 24) | 0xFFFFFF;
        int fgSub = (a << 24) | 0xE0E0E0; // un peu moins vif pour le sous-titre

        var font = mc.font;

        String t = title.getString();
        int tw = font.width(t);
        int tx = (w - tw) / 2;
        int ty = (int) (h * 0.28f);

        String st = subtitle != null ? subtitle.getString() : null;
        int stw = st != null ? font.width(st) : 0;

        int padX = 8;
        int padY = 4;
        int spacing = 6;
        boolean hasSubtitle = (st != null && !st.isEmpty());
        int contentW = Math.max(tw, stw) + 1; // +1 couvre l’ombre à droite
        int boxW = contentW + padX * 2;
        int boxLeft = (w - boxW) / 2;
        int boxTop = ty - padY;
        int boxBottom = hasSubtitle
                ? (ty + font.lineHeight + spacing + font.lineHeight + padY + 1)
                : (ty + font.lineHeight + padY + 1);
        int boxH = boxBottom - boxTop;

        RenderSystem.enableBlend();
        int bg = (a << 24) | 0x000000; // fond noir translucide dépendant du fade
        drawRoundedRect(g, boxLeft, boxTop, boxW, boxH, 6, bg);

        // Titre + sous-titre
        g.drawString(font, t, tx, ty, fgTitle, true);
        if (hasSubtitle) {
            int sx = (w - stw) / 2;
            int sy = ty + font.lineHeight + spacing;
            g.drawString(font, st, sx, sy, fgSub, true);
        }
        RenderSystem.disableBlend();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int i1, float v) {
        // Rendu via l’event
    }
}
