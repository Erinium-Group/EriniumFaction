package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Un toast notification individuel avec icône, titre et message
 */
public class ToastNotification {
    private final Font font;
    private final ToastType type;
    private final Component title;
    private final Component message;
    private final long creationTime;
    private final int duration; // en millisecondes

    private int targetX, targetY;
    private float currentX, currentY;
    private float alpha = 0f;
    private boolean closing = false;
    private long closeStartTime = 0;

    private static final int TOAST_WIDTH = 300;
    private static final int MIN_TOAST_HEIGHT = 70;
    private static final int ICON_SIZE = 24;
    private static final int ICON_BG_SIZE = 40;
    private static final int CLOSE_ICON_SIZE = 16;
    private static final int CLOSE_BUTTON_SIZE = 24;
    private static final int PADDING = 12;
    private static final int PROGRESS_BAR_WIDTH = 276;
    private static final int PROGRESS_BAR_HEIGHT = 3;
    private static final int TEXT_PADDING = 4;
    private static final int LINE_SPACING = 2;

    // Hauteur calculée dynamiquement
    private final int toastHeight;

    // Lignes de texte formatées pour le word wrapping
    private final List<FormattedCharSequence> messageLines;

    private static final int ANIMATION_DURATION = 300; // ms pour l'animation d'entrée/sortie

    // Textures communes
    private static final ResourceLocation CLOSE_ICON = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/toast/toast_close.png");
    private static final ResourceLocation CLOSE_BUTTON_BG = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/toast/toast_close_button_bg.png");
    private static final ResourceLocation ICON_BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/toast/toast_icon_background.png");
    private static final ResourceLocation PROGRESS_BAR_BG = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/toast/toast_progress_bar_bg.png");
    private static final ResourceLocation PROGRESS_BAR_FILL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/toast/toast_progress_bar_fill.png");

    private boolean closeHovered = false;

    public ToastNotification(Font font, ToastType type, Component title, Component message, int duration) {
        this.font = font;
        this.type = type;
        this.title = title;
        this.message = message;
        this.duration = duration;
        this.creationTime = System.currentTimeMillis();

        // Calculer la largeur disponible pour le message (en tenant compte de l'icône et du padding)
        int textWidth = TOAST_WIDTH - (PADDING + ICON_BG_SIZE + 12 + PADDING + CLOSE_BUTTON_SIZE + 8);

        // Word wrap du message
        this.messageLines = font.split(message, textWidth);

        // Calculer la hauteur nécessaire
        int titleHeight = font.lineHeight;
        int messageHeight = messageLines.size() * font.lineHeight + (messageLines.size() - 1) * LINE_SPACING;
        int contentHeight = PADDING + TEXT_PADDING + titleHeight + TEXT_PADDING + messageHeight + TEXT_PADDING + PROGRESS_BAR_HEIGHT + PADDING;

        // S'assurer d'une hauteur minimale (pour que l'icône ne soit pas coupée)
        this.toastHeight = Math.max(MIN_TOAST_HEIGHT, contentHeight);
    }

    /**
     * Définit la position cible (pour l'animation)
     */
    public void setTargetPosition(int x, int y) {
        this.targetX = x;
        this.targetY = y;

        // Initialiser la position de départ (hors écran à droite)
        if (currentX == 0 && currentY == 0) {
            currentX = x + TOAST_WIDTH + 20;
            currentY = y;
        }
    }

    /**
     * Mettre à jour l'animation
     */
    public void tick() {
        long elapsed = System.currentTimeMillis() - creationTime;

        // Animation d'apparition
        if (!closing && elapsed < ANIMATION_DURATION) {
            float progress = elapsed / (float) ANIMATION_DURATION;
            alpha = easeOutCubic(progress);
            currentX = lerp(targetX + TOAST_WIDTH + 20, targetX, easeOutBack(progress));
            currentY = lerp(currentY, targetY, progress);
        }
        // État visible
        else if (!closing && elapsed < duration - ANIMATION_DURATION) {
            alpha = 1f;
            currentX = lerp(currentX, targetX, 0.15f);
            currentY = lerp(currentY, targetY, 0.15f);
        }
        // Auto-fermeture
        else if (!closing && elapsed >= duration - ANIMATION_DURATION) {
            startClosing();
        }

        // Animation de fermeture
        if (closing) {
            long closeElapsed = System.currentTimeMillis() - closeStartTime;
            float progress = Math.min(1f, closeElapsed / (float) ANIMATION_DURATION);
            alpha = 1f - easeInCubic(progress);
            currentX = lerp(targetX, targetX + TOAST_WIDTH + 20, easeInBack(progress));
        }
    }

    /**
     * Commence l'animation de fermeture
     */
    public void startClosing() {
        if (!closing) {
            closing = true;
            closeStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Vérifie si le toast doit être supprimé
     */
    public boolean shouldRemove() {
        if (!closing) return false;
        long closeElapsed = System.currentTimeMillis() - closeStartTime;
        return closeElapsed >= ANIMATION_DURATION;
    }

    /**
     * Rendu du toast
     */
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (alpha <= 0) return;

        int x = (int) currentX;
        int y = (int) currentY;

        graphics.pose().pushPose();

        // Appliquer l'alpha
        int alphaInt = (int) (alpha * 255);
        float alphaF = alpha;

        // 1. Background avec texture
        renderBackground(graphics, x, y, alphaInt);

        // 2. Icon background
        int iconBgX = x + PADDING;
        int iconBgY = y + (toastHeight - ICON_BG_SIZE) / 2;
        renderIconBackground(graphics, iconBgX, iconBgY, alphaF);

        // 3. Icône du type (centrée dans le background)
        int iconX = iconBgX + (ICON_BG_SIZE - ICON_SIZE) / 2;
        int iconY = iconBgY + (ICON_BG_SIZE - ICON_SIZE) / 2;
        renderIcon(graphics, iconX, iconY, alphaF);

        // 4. Textes
        int textX = x + PADDING + ICON_BG_SIZE + 12;
        int titleY = y + PADDING + TEXT_PADDING;
        graphics.drawString(font, title, textX, titleY, colorWithAlpha(0xFFFFFF, alphaInt), false);

        // Afficher les lignes du message avec word wrapping
        int messageY = titleY + font.lineHeight + TEXT_PADDING;
        for (FormattedCharSequence line : messageLines) {
            graphics.drawString(font, line, textX, messageY, colorWithAlpha(0xAAAAAA, alphaInt), false);
            messageY += font.lineHeight + LINE_SPACING;
        }

        // 5. Barre de progression (en bas)
        int progressX = x + PADDING;
        int progressY = y + toastHeight - PADDING - PROGRESS_BAR_HEIGHT;
        renderProgressBar(graphics, progressX, progressY, alphaF);

        // 6. Bouton de fermeture (en haut à droite)
        int closeX = x + TOAST_WIDTH - PADDING - CLOSE_BUTTON_SIZE;
        int closeY = y + PADDING;
        closeHovered = mouseX >= closeX && mouseX <= closeX + CLOSE_BUTTON_SIZE &&
                       mouseY >= closeY && mouseY <= closeY + CLOSE_BUTTON_SIZE;

        renderCloseButton(graphics, closeX, closeY, alphaF);

        graphics.pose().popPose();
    }

    /**
     * Gère le clic de souris
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int x = (int) currentX;
        int y = (int) currentY;
        int closeX = x + TOAST_WIDTH - PADDING - CLOSE_BUTTON_SIZE;
        int closeY = y + PADDING;

        if (mouseX >= closeX && mouseX <= closeX + CLOSE_BUTTON_SIZE &&
            mouseY >= closeY && mouseY <= closeY + CLOSE_BUTTON_SIZE) {
            startClosing();
            return true;
        }

        return false;
    }

    private void renderBackground(GuiGraphics graphics, int x, int y, int alpha) {
        ResourceLocation bgTexture = type.getBackgroundTexture();
        float alphaF = alpha / 255f;

        graphics.pose().pushPose();
        graphics.setColor(1f, 1f, 1f, alphaF);

        // Utiliser toastHeight au lieu de TOAST_HEIGHT pour s'adapter à la hauteur dynamique
        graphics.blit(bgTexture, x, y, 0, 0, TOAST_WIDTH, toastHeight, TOAST_WIDTH, toastHeight);

        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.pose().popPose();
    }

    private void renderIconBackground(GuiGraphics graphics, int x, int y, float alpha) {
        graphics.pose().pushPose();
        graphics.setColor(1f, 1f, 1f, alpha);

        graphics.blit(ICON_BACKGROUND, x, y, 0, 0, ICON_BG_SIZE, ICON_BG_SIZE, ICON_BG_SIZE, ICON_BG_SIZE);

        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.pose().popPose();
    }

    private void renderIcon(GuiGraphics graphics, int x, int y, float alpha) {
        ResourceLocation iconTexture = type.getIconTexture();
        graphics.pose().pushPose();

        graphics.setColor(1f, 1f, 1f, alpha);
        graphics.blit(iconTexture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.pose().popPose();
    }

    private void renderProgressBar(GuiGraphics graphics, int x, int y, float alpha) {
        // Calculer le pourcentage de temps restant
        long elapsed = System.currentTimeMillis() - creationTime;
        float progress = 1f - Math.min(1f, elapsed / (float) duration);

        graphics.pose().pushPose();

        // Fond de la barre
        graphics.setColor(1f, 1f, 1f, alpha * 0.5f);
        graphics.blit(PROGRESS_BAR_BG, x, y, 0, 0, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT,
                     PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);

        // Remplissage proportionnel
        if (progress > 0) {
            int fillWidth = (int) (PROGRESS_BAR_WIDTH * progress);

            // Couleur selon le type
            float[] color = type.getProgressBarColor();
            graphics.setColor(color[0], color[1], color[2], alpha);

            // Utiliser scissor pour couper la texture
            graphics.enableScissor(x, y, x + fillWidth, y + PROGRESS_BAR_HEIGHT);
            graphics.blit(PROGRESS_BAR_FILL, x, y, 0, 0, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT,
                         PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
            graphics.disableScissor();
        }

        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.pose().popPose();
    }

    private void renderCloseButton(GuiGraphics graphics, int x, int y, float alpha) {
        graphics.pose().pushPose();

        // Fond du bouton si hover
        if (closeHovered) {
            graphics.setColor(1f, 1f, 1f, alpha);
            graphics.blit(CLOSE_BUTTON_BG, x, y, 0, 0, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE,
                         CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
        }

        // Icône de fermeture (centrée)
        int iconX = x + (CLOSE_BUTTON_SIZE - CLOSE_ICON_SIZE) / 2;
        int iconY = y + (CLOSE_BUTTON_SIZE - CLOSE_ICON_SIZE) / 2;

        if (closeHovered) {
            graphics.setColor(1f, 1f, 1f, alpha);
        } else {
            graphics.setColor(0.6f, 0.6f, 0.6f, alpha);
        }

        graphics.blit(CLOSE_ICON, iconX, iconY, 0, 0, CLOSE_ICON_SIZE, CLOSE_ICON_SIZE,
                     CLOSE_ICON_SIZE, CLOSE_ICON_SIZE);

        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.pose().popPose();
    }

    private int colorWithAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    // Fonctions d'easing pour animations fluides
    private float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }

    private float easeInCubic(float t) {
        return t * t * t;
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private float easeInBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return c3 * t * t * t - c1 * t * t;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public int getHeight() {
        return toastHeight;
    }

    public int getWidth() {
        return TOAST_WIDTH;
    }

    /**
     * Types de toast avec leurs couleurs, icônes et backgrounds
     */
    public enum ToastType {
        SUCCESS(0x10B981, "toast_success", new float[]{0.063f, 0.725f, 0.506f}),    // #10B981
        ERROR(0xEF4444, "toast_error", new float[]{0.937f, 0.267f, 0.267f}),        // #EF4444
        WARNING(0xF59E0B, "toast_warning", new float[]{0.961f, 0.620f, 0.043f}),    // #F59E0B
        INFO(0x3B82F6, "toast_info", new float[]{0.231f, 0.510f, 0.965f});          // #3B82F6

        private final int color;
        private final ResourceLocation iconTexture;
        private final ResourceLocation backgroundTexture;
        private final float[] progressBarColor;

        ToastType(int color, String baseName, float[] progressBarColor) {
            this.color = color;
            this.iconTexture = ResourceLocation.fromNamespaceAndPath("erinium_faction",
                "textures/gui/components/toast/" + baseName + ".png");
            this.backgroundTexture = ResourceLocation.fromNamespaceAndPath("erinium_faction",
                "textures/gui/components/toast/" + baseName.replace("toast_", "toast_background_") + ".png");
            this.progressBarColor = progressBarColor;
        }

        public int getColor() {
            return color;
        }

        public ResourceLocation getIconTexture() {
            return iconTexture;
        }

        public ResourceLocation getBackgroundTexture() {
            return backgroundTexture;
        }

        public float[] getProgressBarColor() {
            return progressBarColor;
        }
    }
}
