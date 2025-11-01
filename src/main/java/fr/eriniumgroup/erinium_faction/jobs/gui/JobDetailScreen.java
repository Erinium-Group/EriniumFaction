package fr.eriniumgroup.erinium_faction.jobs.gui;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.jobs.JobData;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Écran de détail d'un métier
 * Affiche les informations détaillées, niveau, XP et boutons d'action
 */
public class JobDetailScreen extends Screen {

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    private final JobData jobData;
    private int leftPos;
    private int topPos;

    // Assets communs
    private static final ResourceLocation BG_GRADIENT = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/bg-gradient.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/button-close.png");
    private static final ResourceLocation ICON_PLACEHOLDER_28 = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/icon-placeholder-28x28.png");
    private static final ResourceLocation PANEL_CARD = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/panel-card.png");
    private static final ResourceLocation PROGRESSBAR_TRACK_LARGE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/progressbar-track-large.png");
    private static final ResourceLocation BUTTON_ACTION_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/button-action-green.png");
    private static final ResourceLocation BUTTON_ACTION_PURPLE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/button-action-purple.png");
    private static final ResourceLocation PANEL_SECTION_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/panel-section-header.png");
    private static final ResourceLocation LISTITEM_DETAIL_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/listitem-detail-green.png");
    private static final ResourceLocation LISTITEM_DETAIL_GRAY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/listitem-detail-gray.png");
    private static final ResourceLocation BADGE_LEVEL_DETAIL_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-detail-green.png");
    private static final ResourceLocation BADGE_LEVEL_DETAIL_RED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-detail-red.png");
    private static final ResourceLocation STATUS_CIRCLE_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/status-circle-green.png");
    private static final ResourceLocation STATUS_CIRCLE_GRAY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/status-circle-gray.png");

    public JobDetailScreen(JobData jobData) {
        super(Component.literal(jobData.getType().getDisplayName()));
        this.jobData = jobData;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        JobType type = jobData.getType();

        // Background
        ImageRenderer.renderScaledImage(g, BG_GRADIENT, leftPos, topPos, GUI_WIDTH, GUI_HEIGHT);

        // Border colorée selon le métier
        ResourceLocation border = ResourceLocation.fromNamespaceAndPath("erinium_faction",
            "textures/gui/jobs/border-" + type.getColorName() + ".png");
        ImageRenderer.renderScaledImage(g, border, leftPos, topPos, GUI_WIDTH, GUI_HEIGHT);

        // Header avec gradient coloré
        ResourceLocation headerBg = ResourceLocation.fromNamespaceAndPath("erinium_faction",
            "textures/gui/jobs/panel-header-" + type.getColorName() + ".png");
        ImageRenderer.renderScaledImage(g, headerBg, leftPos + 8, topPos + 8, 384, 40);

        // Back button
        ResourceLocation backButton = ResourceLocation.fromNamespaceAndPath("erinium_faction",
            "textures/gui/jobs/button-back-" + type.getColorName() + ".png");
        ImageRenderer.renderScaledImage(g, backButton, leftPos + 14, topPos + 16, 24, 24);
        g.drawString(font, "‹", leftPos + 26 - font.width("‹") / 2, topPos + 26, type.getColor(), false);

        // Job icon 28x28
        ImageRenderer.renderScaledImage(g, ICON_PLACEHOLDER_28, leftPos + 46, topPos + 14, 28, 28);
        g.drawString(font, type.getEmoji(), leftPos + 60 - font.width(type.getEmoji()) / 2, topPos + 28, type.getColor(), false);

        // Title
        g.drawString(font, type.getDisplayName().toUpperCase(), leftPos + 82, topPos + 18, type.getColor(), false);
        g.drawString(font, type.getDescription(), leftPos + 82, topPos + 32, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, leftPos + 372, topPos + 16, 14, 14);

        // Level Card
        ImageRenderer.renderScaledImage(g, PANEL_CARD, leftPos + 8, topPos + 54, 384, 46);

        // Card border (couleur du métier)
        ResourceLocation cardBorder = ResourceLocation.fromNamespaceAndPath("erinium_faction",
            "textures/gui/jobs/card-border-" + type.getColorName() + ".png");
        ImageRenderer.renderScaledImage(g, cardBorder, leftPos + 8, topPos + 54, 384, 46);

        // Level badge (couleur du métier)
        ResourceLocation levelBadge = ResourceLocation.fromNamespaceAndPath("erinium_faction",
            "textures/gui/jobs/badge-level-large-" + type.getColorName() + ".png");
        ImageRenderer.renderScaledImage(g, levelBadge, leftPos + 18, topPos + 64, 50, 26);
        g.drawString(font, "LEVEL", leftPos + 43 - font.width("LEVEL") / 2, topPos + 68, 0x1a1a2e, false);
        g.drawString(font, String.valueOf(jobData.getLevel()), leftPos + 43 - font.width(String.valueOf(jobData.getLevel())) / 2, topPos + 80, 0x1a1a2e, false);

        // XP Info
        g.drawString(font, "Experience", leftPos + 78, topPos + 66, 0xffffff, false);
        String xpText = jobData.getExperience() + " / " + jobData.getExperienceToNextLevel() + " XP (" + jobData.getExperiencePercentage() + "%)";
        g.drawString(font, xpText, leftPos + 78, topPos + 80, 0x9ca3af, false);

        // XP Progress Bar
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_TRACK_LARGE, leftPos + 180, topPos + 68, 200, 16);

        // XP Bar fill avec gradient (couleur du métier)
        int fillWidth = (int) (200 * jobData.getExperienceProgress());
        if (fillWidth > 0) {
            ResourceLocation fillTexture = ResourceLocation.fromNamespaceAndPath("erinium_faction",
                "textures/gui/jobs/progressbar-fill-" + type.getColorName() + ".png");
            g.enableScissor(leftPos + 180, topPos + 68, leftPos + 180 + fillWidth, topPos + 84);
            ImageRenderer.renderScaledImage(g, fillTexture, leftPos + 180, topPos + 68, 200, 16);
            g.disableScissor();
        }

        // Percentage text on bar
        String percentText = jobData.getExperiencePercentage() + "%";
        g.drawString(font, percentText, leftPos + 280 - font.width(percentText) / 2, topPos + 73, 0x1a1a2e, false);

        // Action Buttons
        renderActionButtons(g, mouseX, mouseY);

        // Recent Unlocks Section
        renderRecentUnlocks(g);
    }

    private void renderActionButtons(GuiGraphics g, int mouseX, int mouseY) {
        // How to gain XP button
        ImageRenderer.renderScaledImage(g, BUTTON_ACTION_GREEN, leftPos + 8, topPos + 106, 187, 36);
        g.drawString(font, "How to gain XP", leftPos + 102 - font.width("How to gain XP") / 2, topPos + 116, 0x10b981, false);
        g.drawString(font, "View all XP sources", leftPos + 102 - font.width("View all XP sources") / 2, topPos + 128, 0x9ca3af, false);

        // Unlocked Features button
        ImageRenderer.renderScaledImage(g, BUTTON_ACTION_PURPLE, leftPos + 205, topPos + 106, 187, 36);
        g.drawString(font, "Unlocked Features", leftPos + 298 - font.width("Unlocked Features") / 2, topPos + 116, 0xa855f7, false);
        g.drawString(font, "3 unlocked • 7 locked", leftPos + 298 - font.width("3 unlocked • 7 locked") / 2, topPos + 128, 0x9ca3af, false);
    }

    private void renderRecentUnlocks(GuiGraphics g) {
        // Section header
        ImageRenderer.renderScaledImage(g, PANEL_SECTION_HEADER, leftPos + 8, topPos + 148, 384, 16);
        g.drawString(font, "RECENT UNLOCKS", leftPos + 14, topPos + 155, 0xfbbf24, false);

        // Unlocked item 1
        ImageRenderer.renderScaledImage(g, LISTITEM_DETAIL_GREEN, leftPos + 8, topPos + 170, 384, 28);
        ImageRenderer.renderScaledImage(g, STATUS_CIRCLE_GREEN, leftPos + 14, topPos + 178, 12, 12);
        g.drawString(font, "✓", leftPos + 20 - font.width("✓") / 2, topPos + 180, 0x10b981, false);
        g.drawString(font, "Diamond Pickaxe", leftPos + 32, topPos + 177, 0xffffff, false);
        g.drawString(font, "Unlocked at Level " + jobData.getLevel(), leftPos + 32, topPos + 187, 0x9ca3af, false);
        ImageRenderer.renderScaledImage(g, BADGE_LEVEL_DETAIL_GREEN, leftPos + 358, topPos + 176, 32, 16);
        g.drawString(font, "LVL " + jobData.getLevel(), leftPos + 374 - font.width("LVL " + jobData.getLevel()) / 2, topPos + 181, 0xffffff, false);

        // Locked item 1
        ImageRenderer.renderScaledImageWithAlpha(g, LISTITEM_DETAIL_GRAY, leftPos + 8, topPos + 202, 384, 28, 0.5f);
        ImageRenderer.renderScaledImage(g, STATUS_CIRCLE_GRAY, leftPos + 14, topPos + 210, 12, 12);
        g.drawString(font, "🔒", leftPos + 20 - font.width("🔒") / 2, topPos + 212, 0x6b7280, false);
        g.drawString(font, "Netherite Upgrade", leftPos + 32, topPos + 209, 0x9ca3af, false);
        g.drawString(font, "Requires Level 30", leftPos + 32, topPos + 219, 0x6b7280, false);
        ImageRenderer.renderScaledImage(g, BADGE_LEVEL_DETAIL_RED, leftPos + 358, topPos + 208, 32, 16);
        g.drawString(font, "LVL 30", leftPos + 374 - font.width("LVL 30") / 2, topPos + 213, 0xffffff, false);

        // Locked item 2
        ImageRenderer.renderScaledImageWithAlpha(g, LISTITEM_DETAIL_GRAY, leftPos + 8, topPos + 234, 384, 28, 0.5f);
        ImageRenderer.renderScaledImage(g, STATUS_CIRCLE_GRAY, leftPos + 14, topPos + 242, 12, 12);
        g.drawString(font, "🔒", leftPos + 20 - font.width("🔒") / 2, topPos + 244, 0x6b7280, false);
        g.drawString(font, "Fortune III", leftPos + 32, topPos + 241, 0x9ca3af, false);
        g.drawString(font, "Requires Level 50", leftPos + 32, topPos + 251, 0x6b7280, false);
        ImageRenderer.renderScaledImage(g, BADGE_LEVEL_DETAIL_RED, leftPos + 358, topPos + 240, 32, 16);
        g.drawString(font, "LVL 50", leftPos + 374 - font.width("LVL 50") / 2, topPos + 245, 0xffffff, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Back button
        if (mouseX >= leftPos + 14 && mouseX <= leftPos + 38 &&
            mouseY >= topPos + 16 && mouseY <= topPos + 40) {
            if (minecraft != null) {
                minecraft.setScreen(new JobsMenuScreen());
            }
            return true;
        }

        // Close button
        if (mouseX >= leftPos + 372 && mouseX <= leftPos + 386 &&
            mouseY >= topPos + 16 && mouseY <= topPos + 30) {
            this.onClose();
            return true;
        }

        // How to gain XP button
        if (mouseX >= leftPos + 8 && mouseX <= leftPos + 195 &&
            mouseY >= topPos + 106 && mouseY <= topPos + 142) {
            if (minecraft != null) {
                minecraft.setScreen(new JobHowToXPScreen(jobData));
            }
            return true;
        }

        // Unlocked Features button
        if (mouseX >= leftPos + 205 && mouseX <= leftPos + 392 &&
            mouseY >= topPos + 106 && mouseY <= topPos + 142) {
            if (minecraft != null) {
                minecraft.setScreen(new JobUnlockedFeaturesScreen(jobData));
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
