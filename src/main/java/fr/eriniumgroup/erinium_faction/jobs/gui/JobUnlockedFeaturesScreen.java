package fr.eriniumgroup.erinium_faction.jobs.gui;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.jobs.JobData;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Écran "Fonctionnalités Débloquées"
 * Liste toutes les fonctionnalités débloquées et verrouillées par niveau
 */
public class JobUnlockedFeaturesScreen extends Screen {

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    private final JobData jobData;
    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;

    // Assets
    private static final ResourceLocation BG_GRADIENT = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/bg-gradient.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/button-close.png");
    private static final ResourceLocation PANEL_LEVEL_INDICATOR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/panel-level-indicator.png");
    private static final ResourceLocation PANEL_SECTION_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/panel-section-header.png");
    private static final ResourceLocation PANEL_DARK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/panel-dark.png");
    private static final ResourceLocation ICON_PLACEHOLDER_16 = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/icon-placeholder-16x16.png");
    private static final ResourceLocation LISTITEM_SMALL_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/listitem-small-green.png");
    private static final ResourceLocation LISTITEM_SMALL_RED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/listitem-small-red.png");
    private static final ResourceLocation BADGE_LEVEL_TINY_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-tiny-green.png");
    private static final ResourceLocation BADGE_LEVEL_TINY_RED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-tiny-red.png");
    private static final ResourceLocation BADGE_LEVEL_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-medium-gold.png");
    private static final ResourceLocation SCROLLBAR_TRACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/scrollbar-track-142.png");
    private static final ResourceLocation SCROLLBAR_THUMB = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/scrollbar-thumb-purple.png");

    // Données d'exemple des fonctionnalités
    private static final Feature[] EXAMPLE_FEATURES = {
        new Feature("⛏️", "Iron Pickaxe", "Craft and use iron tier tools", 10, true),
        new Feature("💎", "Diamond Pickaxe", "Craft and use diamond tier tools", 15, true),
        new Feature("✨", "Efficiency I", "Mine blocks 30% faster", 5, true),
        new Feature("🔒", "Fortune II", "Requires Level 25 • 10 levels to go", 25, false),
        new Feature("🔒", "Netherite Upgrade", "Requires Level 30 • 15 levels to go", 30, false),
    };

    public JobUnlockedFeaturesScreen(JobData jobData) {
        super(Component.literal("Unlocked Features"));
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

        // Border purple
        ResourceLocation border = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/border-purple.png");
        ImageRenderer.renderScaledImage(g, border, leftPos, topPos, GUI_WIDTH, GUI_HEIGHT);

        // Header
        ResourceLocation headerBg = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/bg-header-section-purple.png");
        ImageRenderer.renderScaledImage(g, headerBg, leftPos, topPos, 400, 48);

        // Back button
        ResourceLocation backButton = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/button-back-purple.png");
        ImageRenderer.renderScaledImage(g, backButton, leftPos + 14, topPos + 16, 24, 24);
        g.drawString(font, "‹", leftPos + 26 - font.width("‹") / 2, topPos + 26, 0xa855f7, false);

        // Title
        g.drawString(font, "UNLOCKED FEATURES - " + type.getDisplayName().toUpperCase(), leftPos + 46, topPos + 18, 0xa855f7, false);
        g.drawString(font, "Perks and abilities you've earned", leftPos + 46, topPos + 32, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, leftPos + 372, topPos + 16, 14, 14);

        // Level indicator
        ImageRenderer.renderScaledImage(g, PANEL_LEVEL_INDICATOR, leftPos + 8, topPos + 54, 384, 28);
        ImageRenderer.renderScaledImage(g, BADGE_LEVEL_MEDIUM, leftPos + 18, topPos + 60, 40, 16);
        g.drawString(font, "LVL " + jobData.getLevel(), leftPos + 38 - font.width("LVL " + jobData.getLevel()) / 2, topPos + 67, 0x1a1a2e, false);
        g.drawString(font, "Current Level: " + jobData.getLevel(), leftPos + 66, topPos + 67, 0xffffff, false);

        // Stats débloquées
        int unlockedCount = (int) java.util.Arrays.stream(EXAMPLE_FEATURES).filter(f -> f.unlocked).count();
        int lockedCount = EXAMPLE_FEATURES.length - unlockedCount;
        g.drawString(font, unlockedCount + " unlocked • " + lockedCount + " locked", leftPos + 250, topPos + 67, 0x10b981, false);

        // Section header
        ImageRenderer.renderScaledImage(g, PANEL_SECTION_HEADER, leftPos + 8, topPos + 88, 384, 16);
        g.drawString(font, "YOUR UNLOCKED FEATURES", leftPos + 14, topPos + 95, 0xa855f7, false);

        // Scroll container
        ImageRenderer.renderScaledImage(g, PANEL_DARK, leftPos + 8, topPos + 110, 384, 148);

        // Render features list
        renderFeaturesList(g);

        // Scrollbar
        renderScrollbar(g);

        // Progress summary at bottom (si on veut)
        if (lockedCount > 0) {
            Feature nextFeature = java.util.Arrays.stream(EXAMPLE_FEATURES).filter(f -> !f.unlocked).findFirst().orElse(null);
            if (nextFeature != null) {
                g.drawString(font, "Next unlock at Level " + nextFeature.requiredLevel + ": " + nextFeature.name,
                    leftPos + 200 - font.width("Next unlock at Level " + nextFeature.requiredLevel + ": " + nextFeature.name) / 2,
                    topPos + 264, 0x9ca3af, false);
            }
        }
    }

    private void renderFeaturesList(GuiGraphics g) {
        int startY = topPos + 116;
        int itemHeight = 28;
        int spacing = 4;

        g.enableScissor(leftPos + 14, topPos + 116, leftPos + 386, topPos + 258);

        for (int i = 0; i < EXAMPLE_FEATURES.length; i++) {
            int yOffset = startY + (i * (itemHeight + spacing)) - scrollOffset;

            if (yOffset + itemHeight >= topPos + 116 && yOffset < topPos + 258) {
                renderFeature(g, EXAMPLE_FEATURES[i], leftPos + 14, yOffset);
            }
        }

        g.disableScissor();
    }

    private void renderFeature(GuiGraphics g, Feature feature, int x, int y) {
        // Card background
        if (feature.unlocked) {
            ImageRenderer.renderScaledImage(g, LISTITEM_SMALL_GREEN, x, y, 372, 28);
        } else {
            ImageRenderer.renderScaledImageWithAlpha(g, LISTITEM_SMALL_RED, x, y, 372, 28, 0.6f);
        }

        // Icon placeholder
        ImageRenderer.renderScaledImage(g, ICON_PLACEHOLDER_16, x + 6, y + 6, 16, 16);

        // Icône (emoji)
        int iconColor = feature.unlocked ? (feature.icon.equals("🔒") ? 0x10b981 : 0x10b981) : 0x6b7280;
        g.drawString(font, feature.unlocked ? feature.icon : "🔒", x + 14 - font.width(feature.unlocked ? feature.icon : "🔒") / 2, y + 11, iconColor, false);

        // Name
        int nameColor = feature.unlocked ? 0x10b981 : 0x9ca3af;
        g.drawString(font, feature.name, x + 28, y + 10, nameColor, false);

        // Description
        int descColor = feature.unlocked ? 0x9ca3af : 0xef4444;
        g.drawString(font, feature.description, x + 28, y + 20, descColor, false);

        // Level badge
        int badgeX = x + 326;
        int badgeY = y + 8;

        if (feature.unlocked) {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_TINY_GREEN, badgeX, badgeY, 36, 12);
        } else {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_TINY_RED, badgeX, badgeY, 36, 12);
        }

        String levelText = "LVL " + feature.requiredLevel;
        g.drawString(font, levelText, badgeX + 18 - font.width(levelText) / 2, badgeY + 4, 0xffffff, false);
    }

    private void renderScrollbar(GuiGraphics g) {
        int scrollbarX = leftPos + 390;
        int scrollbarY = topPos + 116;

        ImageRenderer.renderScaledImage(g, SCROLLBAR_TRACK, scrollbarX, scrollbarY, 4, 142);

        int totalHeight = EXAMPLE_FEATURES.length * 32;
        int visibleHeight = 142;
        int thumbHeight = Math.max(20, (visibleHeight * visibleHeight) / totalHeight);
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        int thumbY = scrollbarY + (maxScroll > 0 ? (scrollOffset * (visibleHeight - thumbHeight)) / maxScroll : 0);

        g.enableScissor(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight);
        ImageRenderer.renderScaledImage(g, SCROLLBAR_THUMB, scrollbarX, scrollbarY, 4, 142);
        g.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalHeight = EXAMPLE_FEATURES.length * 32;
        int visibleHeight = 142;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);

        scrollOffset -= (int) (scrollY * 20);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Back button
        if (mouseX >= leftPos + 14 && mouseX <= leftPos + 38 &&
            mouseY >= topPos + 16 && mouseY <= topPos + 40) {
            if (minecraft != null) {
                minecraft.setScreen(new JobDetailScreen(jobData));
            }
            return true;
        }

        // Close button
        if (mouseX >= leftPos + 372 && mouseX <= leftPos + 386 &&
            mouseY >= topPos + 16 && mouseY <= topPos + 30) {
            this.onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Classe interne pour les fonctionnalités
    private static class Feature {
        String icon;
        String name;
        String description;
        int requiredLevel;
        boolean unlocked;

        Feature(String icon, String name, String description, int requiredLevel, boolean unlocked) {
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.requiredLevel = requiredLevel;
            this.unlocked = unlocked;
        }
    }
}
