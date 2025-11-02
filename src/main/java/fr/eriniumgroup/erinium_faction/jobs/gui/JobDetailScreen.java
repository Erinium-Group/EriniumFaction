package fr.eriniumgroup.erinium_faction.jobs.gui;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import fr.eriniumgroup.erinium_faction.jobs.JobData;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import fr.eriniumgroup.erinium_faction.jobs.config.JobConfig;
import fr.eriniumgroup.erinium_faction.jobs.config.UnlockingEntry;
import fr.eriniumgroup.erinium_faction.jobs.network.JobsClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private int unlockedCount = 0;
    private int lockedCount = 0;
    private List<UnlockingEntry> recentUnlocks = new ArrayList<>();
    private int lastMouseX = 0;
    private int lastMouseY = 0;

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
        super(Component.translatable("erinium_faction.jobs.detail.title"));
        this.jobData = jobData;
        loadConfigData();
    }

    /**
     * Charge les statistiques depuis la configuration
     */
    private void loadConfigData() {
        JobConfig config = JobsClientConfig.getConfig(jobData.getType());
        if (config != null) {
            // Compter les débloquages
            unlockedCount = 0;
            lockedCount = 0;
            List<UnlockingEntry> allUnlocks = config.getUnlocking();

            for (UnlockingEntry entry : allUnlocks) {
                if (entry.isUnlockedAtLevel(jobData.getLevel())) {
                    unlockedCount++;
                } else {
                    lockedCount++;
                }
            }

            // Obtenir les débloquages récents (derniers 3 débloqués + prochains 2 verrouillés)
            recentUnlocks = getRecentUnlocks(allUnlocks);
        }
    }

    /**
     * Récupère les débloquages récents à afficher
     */
    private List<UnlockingEntry> getRecentUnlocks(List<UnlockingEntry> allUnlocks) {
        List<UnlockingEntry> result = new ArrayList<>();

        // Débloquages récents (triés par niveau décroissant, max 1)
        List<UnlockingEntry> unlocked = allUnlocks.stream()
            .filter(e -> e.isUnlockedAtLevel(jobData.getLevel()))
            .sorted(Comparator.comparingInt(UnlockingEntry::getLevel).reversed())
            .limit(1)
            .toList();
        result.addAll(unlocked);

        // Prochains débloquages (triés par niveau croissant, max 2)
        List<UnlockingEntry> locked = allUnlocks.stream()
            .filter(e -> !e.isUnlockedAtLevel(jobData.getLevel()))
            .sorted(Comparator.comparingInt(UnlockingEntry::getLevel))
            .limit(2)
            .toList();
        result.addAll(locked);

        return result;
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

        // Sauvegarder les positions de la souris pour les sous-renders
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

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
        String levelText = Component.translatable("erinium_faction.jobs.detail.level").getString().toUpperCase();
        g.drawString(font, levelText, leftPos + 43 - font.width(levelText) / 2, topPos + 68, 0x1a1a2e, false);
        g.drawString(font, String.valueOf(jobData.getLevel()), leftPos + 43 - font.width(String.valueOf(jobData.getLevel())) / 2, topPos + 80, 0x1a1a2e, false);

        // XP Info
        g.drawString(font, Component.translatable("erinium_faction.jobs.detail.xp_progress").getString(), leftPos + 78, topPos + 66, 0xffffff, false);
        String xpFormat = String.format(Component.translatable("erinium_faction.jobs.detail.xp_format").getString(), jobData.getExperience(), jobData.getExperienceToNextLevel());
        String xpText = xpFormat + " (" + jobData.getExperiencePercentage() + "%)";
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
        String howToXpText = Component.translatable("erinium_faction.jobs.detail.button.how_to_xp").getString();
        boolean greenButtonHovered = mouseX >= leftPos + 8 && mouseX <= leftPos + 195 && mouseY >= topPos + 106 && mouseY <= topPos + 142;
        TextHelper.drawAutoScrollingText(g, font, howToXpText, leftPos + 102 - Math.min(font.width(howToXpText), 175) / 2, topPos + 116, 175, 0x10b981, false, greenButtonHovered, "button_how_to_xp");
        String xpSourcesText = Component.translatable("erinium_faction.jobs.how_to_xp.subtitle").getString();
        TextHelper.drawAutoScrollingText(g, font, xpSourcesText, leftPos + 102 - Math.min(font.width(xpSourcesText), 175) / 2, topPos + 128, 175, 0x9ca3af, false, greenButtonHovered, "button_xp_subtitle");

        // Unlocked Features button
        ImageRenderer.renderScaledImage(g, BUTTON_ACTION_PURPLE, leftPos + 205, topPos + 106, 187, 36);
        String unlockedText = Component.translatable("erinium_faction.jobs.detail.button.unlocked_features").getString();
        boolean purpleButtonHovered = mouseX >= leftPos + 205 && mouseX <= leftPos + 392 && mouseY >= topPos + 106 && mouseY <= topPos + 142;
        TextHelper.drawAutoScrollingText(g, font, unlockedText, leftPos + 298 - Math.min(font.width(unlockedText), 175) / 2, topPos + 116, 175, 0xa855f7, false, purpleButtonHovered, "button_unlocked");

        // Afficher les stats réelles depuis la config
        String statsText = String.format("%d débloqué • %d verrouillé", unlockedCount, lockedCount);
        TextHelper.drawAutoScrollingText(g, font, statsText, leftPos + 298 - Math.min(font.width(statsText), 175) / 2, topPos + 128, 175, 0x9ca3af, false, purpleButtonHovered, "button_stats");
    }

    private void renderRecentUnlocks(GuiGraphics g) {
        // Section header
        ImageRenderer.renderScaledImage(g, PANEL_SECTION_HEADER, leftPos + 8, topPos + 148, 384, 16);
        g.drawString(font, Component.translatable("erinium_faction.jobs.unlocked_features.your_features").getString(), leftPos + 14, topPos + 155, 0xfbbf24, false);

        // Afficher les débloquages récents depuis la config
        int yOffset = 170;
        for (int i = 0; i < Math.min(3, recentUnlocks.size()); i++) {
            UnlockingEntry entry = recentUnlocks.get(i);
            renderUnlockItem(g, entry, leftPos + 8, topPos + yOffset);
            yOffset += 32;
        }
    }

    /**
     * Affiche un item de débloquage
     */
    private void renderUnlockItem(GuiGraphics g, UnlockingEntry entry, int x, int y) {
        boolean isUnlocked = entry.isUnlockedAtLevel(jobData.getLevel());

        // Card background
        if (isUnlocked) {
            ImageRenderer.renderScaledImage(g, LISTITEM_DETAIL_GREEN, x, y, 384, 28);
            ImageRenderer.renderScaledImage(g, STATUS_CIRCLE_GREEN, x + 6, y + 8, 12, 12);
            g.drawString(font, "✓", x + 12 - font.width("✓") / 2, y + 10, 0x10b981, false);
        } else {
            ImageRenderer.renderScaledImageWithAlpha(g, LISTITEM_DETAIL_GRAY, x, y, 384, 28, 0.5f);
            ImageRenderer.renderScaledImage(g, STATUS_CIRCLE_GRAY, x + 6, y + 8, 12, 12);
            g.drawString(font, "🔒", x + 12 - font.width("🔒") / 2, y + 10, 0x6b7280, false);
        }

        // Name avec auto-scroll
        String name = entry.getDisplayName() != null && !entry.getDisplayName().isEmpty()
            ? entry.getDisplayName()
            : formatUnlockName(entry);
        int nameColor = isUnlocked ? 0xffffff : 0x9ca3af;
        // Détecter le hover sur cet item
        boolean itemHovered = lastMouseX >= x && lastMouseX <= x + 384 && lastMouseY >= y && lastMouseY <= y + 28;
        TextHelper.drawAutoScrollingText(g, font, name, x + 24, y + 7, 320, nameColor, false, itemHovered, "unlock_name_" + entry.getTargetId());

        // Description
        String description = isUnlocked
            ? Component.translatable("erinium_faction.jobs.status.unlocked").getString() + " " + Component.translatable("erinium_faction.jobs.detail.level").getString() + " " + entry.getLevel()
            : String.format(Component.translatable("erinium_faction.jobs.status.requires_level").getString(), entry.getLevel());
        int descColor = isUnlocked ? 0x9ca3af : 0x6b7280;
        g.drawString(font, description, x + 24, y + 17, descColor, false);

        // Level badge
        if (isUnlocked) {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_DETAIL_GREEN, x + 350, y + 6, 32, 16);
        } else {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_DETAIL_RED, x + 350, y + 6, 32, 16);
        }

        String levelText = String.format(Component.translatable("erinium_faction.jobs.badge.level").getString(), entry.getLevel());
        g.drawString(font, levelText, x + 366 - font.width(levelText) / 2, y + 11, 0xffffff, false);
    }

    /**
     * Formate le nom du débloquage
     */
    private String formatUnlockName(UnlockingEntry entry) {
        String[] parts = entry.getTargetId().split(":");
        String itemName = parts.length > 1 ? parts[1] : entry.getTargetId();
        itemName = itemName.replace("_", " ");
        return capitalize(itemName);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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
