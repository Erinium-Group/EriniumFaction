package fr.eriniumgroup.erinium_faction.jobs.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Overlay pour afficher les notifications toast des jobs
 * Affiche les toasts en haut à gauche de l'écran
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class JobToastOverlay {

    private static final List<JobToast> TOASTS = new ArrayList<>();
    private static final int MAX_TOASTS = 5; // Maximum de toasts affichés

    // Dimensions du toast (base avant scaling)
    private static final int TOAST_WIDTH = 280;
    private static final int TOAST_HEIGHT = 60;
    private static final int TOAST_SPACING = 8; // Espacement entre les toasts
    private static final int TOAST_MARGIN_X = 10; // Marge à droite
    private static final int TOAST_MARGIN_Y = 10; // Marge en haut
    private static final float MAX_SCREEN_WIDTH_PERCENT = 0.30f; // 30% de l'écran max

    // Assets
    private static final ResourceLocation TOAST_BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/toast-background.png");
    private static final ResourceLocation TOAST_ICON_FRAME = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/toast-icon-frame.png");
    private static final ResourceLocation TOAST_PROGRESSBAR_TRACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/toast-progressbar-track.png");

    /**
     * Ajoute un toast à la file d'attente
     * Si un toast pour ce métier existe déjà, met à jour ses valeurs
     * Sinon crée un nouveau toast (max 6 métiers de toute façon)
     */
    public static void addToast(JobToast toast) {
        // Chercher si un toast existe déjà pour ce métier
        JobToast existingToast = null;
        for (JobToast t : TOASTS) {
            if (t.getJobType() == toast.getJobType()) {
                existingToast = t;
                break;
            }
        }

        if (existingToast != null) {
            // Mettre à jour le toast existant
            existingToast.updateValues(
                toast.getLevel(),
                toast.getXpGained(),
                toast.getActionDescription(),
                toast.getCurrentXp(),
                toast.getXpToNextLevel(),
                toast.isLevelUp()
            );
        } else {
            // Ajouter un nouveau toast pour ce métier
            TOASTS.add(toast);
        }
    }

    /**
     * Crée et ajoute un toast pour un gain d'XP
     */
    public static void showXpGain(JobType jobType, int level, int xpGained, String actionDescription, int currentXp, int xpToNextLevel) {
        JobToast toast = new JobToast(
            jobType,
            jobType.getDisplayName(),
            level,
            xpGained,
            actionDescription,
            currentXp,
            xpToNextLevel,
            false
        );
        addToast(toast);
    }

    /**
     * Crée et ajoute un toast pour un level up
     */
    public static void showLevelUp(JobType jobType, int newLevel, int currentXp, int xpToNextLevel) {
        JobToast toast = new JobToast(
            jobType,
            jobType.getDisplayName(),
            newLevel,
            0,
            Component.translatable("erinium_faction.jobs.toast.levelup", newLevel).getString(),
            currentXp,
            xpToNextLevel,
            true
        );
        addToast(toast);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "hotbar"))) {
            renderToasts(event.getGuiGraphics());
        }
    }

    private static void renderToasts(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Calculer le scale pour limiter à 30% de l'écran
        float scale = Math.min(1.0f, (screenWidth * MAX_SCREEN_WIDTH_PERCENT) / TOAST_WIDTH);

        // Mettre à jour et supprimer les toasts expirés
        Iterator<JobToast> iterator = TOASTS.iterator();
        while (iterator.hasNext()) {
            JobToast toast = iterator.next();
            toast.updateState();
            if (!toast.isVisible()) {
                iterator.remove();
            }
        }

        // Render les toasts actifs
        int scaledWidth = (int) (TOAST_WIDTH * scale);
        int scaledHeight = (int) (TOAST_HEIGHT * scale);
        int scaledSpacing = (int) (TOAST_SPACING * scale);
        int yOffset = TOAST_MARGIN_Y;

        // Position X en haut à droite
        int xPos = screenWidth - scaledWidth - TOAST_MARGIN_X;

        for (JobToast toast : TOASTS) {
            renderToast(guiGraphics, mc, toast, xPos, yOffset, scale, screenWidth);
            yOffset += scaledHeight + scaledSpacing;
        }
    }

    private static void renderToast(GuiGraphics g, Minecraft mc, JobToast toast, int baseX, int baseY, float scale, int screenWidth) {
        float opacity = toast.getOpacity();
        float animProgress = toast.getAnimationProgress();

        // Calculer les offsets d'animation
        int scaledWidth = (int) (TOAST_WIDTH * scale);
        int scaledHeight = (int) (TOAST_HEIGHT * scale);

        int animOffsetX = 0;
        int animOffsetY = 0;

        switch (toast.getAnimationState()) {
            case ENTERING:
                // Tomber depuis le haut (easing out)
                float dropEase = 1.0f - (float) Math.pow(1.0f - animProgress, 3);
                animOffsetY = -(int) ((1.0f - dropEase) * scaledHeight);
                break;
            case LEAVING:
                // Glisser vers la droite hors écran (easing in)
                float slideEase = (float) Math.pow(animProgress, 2);
                animOffsetX = (int) (slideEase * (screenWidth - baseX + scaledWidth));
                break;
            case VISIBLE:
            default:
                // Pas d'offset
                break;
        }

        int x = baseX + animOffsetX;
        int y = baseY + animOffsetY;

        // Appliquer le scaling et l'opacité
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        // Background
        ImageRenderer.renderScaledImage(g, TOAST_BACKGROUND, 0, 0, TOAST_WIDTH, TOAST_HEIGHT);

        // Barre d'accent colorée (3x48)
        ResourceLocation accentBar = ResourceLocation.fromNamespaceAndPath("erinium_faction",
            "textures/gui/jobs/toast-accent-bar-" + toast.getJobType().getColorName() + ".png");
        ImageRenderer.renderScaledImage(g, accentBar, 6, 6, 3, 48);

        // Icône du job
        ImageRenderer.renderScaledImage(g, TOAST_ICON_FRAME, 14, 14, 32, 32);

        // Emoji du job - CENTRÉ VERTICALEMENT
        g.drawString(mc.font, toast.getJobType().getEmoji(),
            30 - mc.font.width(toast.getJobType().getEmoji()) / 2,
            26, toast.getJobType().getColor(), false);

        // Nom du job - REMONTÉ
        String jobName = toast.getJobName();
        g.drawString(mc.font, jobName, 54, 16, toast.getJobType().getColor(), false);

        // Calculer la position du badge de niveau (dynamique selon la largeur du texte)
        int jobNameWidth = mc.font.width(jobName);
        int badgeX = 54 + jobNameWidth + 6;

        // Badge de niveau - CENTRÉ VERTICALEMENT
        ResourceLocation levelBadge = ResourceLocation.fromNamespaceAndPath("erinium_faction",
            "textures/gui/jobs/toast-level-badge-" + toast.getJobType().getColorName() + ".png");
        ImageRenderer.renderScaledImage(g, levelBadge, badgeX, 15, 28, 14);

        String levelText = String.valueOf(toast.getLevel());
        g.drawString(mc.font, levelText,
            badgeX + 14 - mc.font.width(levelText) / 2,
            19, toast.getJobType().getColor(), false);

        // XP gagné - REMONTÉ
        if (toast.isLevelUp()) {
            g.drawString(mc.font, Component.translatable("erinium_faction.jobs.toast.levelup_title").getString(),
                54, 30, 0xfbbf24, false);
        } else {
            g.drawString(mc.font, Component.translatable("erinium_faction.jobs.toast.xp_gained", toast.getXpGained()).getString(),
                54, 30, 0x10b981, false);
        }

        // Description de l'action - REMONTÉ
        g.drawString(mc.font, toast.getActionDescription(), 54, 42, 0x9ca3af, false);

        // Calculer la position de la barre de progression (dynamique)
        int progressBarX = badgeX + 28 + 8; // Badge + marge
        int progressBarWidth = Math.max(80, (TOAST_WIDTH - 8) - progressBarX); // Width dynamique

        // Barre de progression - track - CENTRÉE VERTICALEMENT
        ImageRenderer.renderScaledImage(g, TOAST_PROGRESSBAR_TRACK, progressBarX, 18, progressBarWidth, 6);

        // Barre de progression - fill
        int fillWidth = (int) (progressBarWidth * toast.getProgressPercentage());
        if (fillWidth > 0) {
            ResourceLocation progressFill = ResourceLocation.fromNamespaceAndPath("erinium_faction",
                "textures/gui/jobs/toast-progressbar-fill-" + toast.getJobType().getColorName() + ".png");

            // Clipper la barre selon le pourcentage
            int scissorX = (int) ((progressBarX) * scale) + x;
            int scissorY = (int) (18 * scale) + y;
            int scissorX2 = (int) ((progressBarX + fillWidth) * scale) + x;
            int scissorY2 = (int) (24 * scale) + y;

            g.enableScissor(scissorX, scissorY, scissorX2, scissorY2);
            ImageRenderer.renderScaledImage(g, progressFill, progressBarX, 18, progressBarWidth, 6);
            g.disableScissor();
        }

        // Texte de pourcentage XP - REMONTÉ
        if (!toast.isLevelUp()) {
            int percentage = (int) (toast.getProgressPercentage() * 100);
            String xpText = toast.getCurrentXp() + "/" + toast.getXpToNextLevel() + " (" + percentage + "%)";
            int xpTextWidth = mc.font.width(xpText);
            int xpTextX = progressBarX + (progressBarWidth / 2) - (xpTextWidth / 2);
            g.drawString(mc.font, xpText, xpTextX, 30, 0x9ca3af, false);
        } else {
            String maxXpText = Component.translatable("erinium_faction.jobs.toast.max_xp").getString();
            int maxXpWidth = mc.font.width(maxXpText);
            int maxXpX = progressBarX + (progressBarWidth / 2) - (maxXpWidth / 2);
            g.drawString(mc.font, maxXpText, maxXpX, 30, 0x10b981, false);
        }

        // Restaurer la couleur et le pose
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        g.pose().popPose();
    }
}
