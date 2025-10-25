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

    private static Component title = null;
    private static Component subtitle = null;
    private static long startMs = 0L;
    private static int fadeInMs = 500;  // par défaut
    private static int stayMs = 1500;   // par défaut
    private static int fadeOutMs = 500; // par défaut

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
        title = Component.literal(titleText);
        subtitle = (subtitleText != null && !subtitleText.isBlank()) ? Component.literal(subtitleText) : null;
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

        // Couleur blanche avec alpha
        int a = Math.min(255, Math.max(0, (int) (alpha * 255f)));
        int color = (a << 24) | 0xFFFFFF; // ARGB

        // Dessin titre
        var font = mc.font;
        String t = title.getString();
        int tw = font.width(t);
        int tx = (w - tw) / 2;
        int ty = (int) (h * 0.28f);

        RenderSystem.enableBlend();
        g.drawString(font, t, tx + 1, ty + 1, (a << 24), false);
        g.drawString(font, t, tx, ty, color, false);

        if (subtitle != null) {
            String st = subtitle.getString();
            int stw = font.width(st);
            int sx = (w - stw) / 2;
            int sy = ty + font.lineHeight + 6;

            g.drawString(font, st, sx + 1, sy + 1, (a << 24), false);
            g.drawString(font, st, sx, sy, color, false);
        }
        RenderSystem.disableBlend();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int i1, float v) {
    }
}
