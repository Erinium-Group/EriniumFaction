package fr.eriniumgroup.erinium_faction.jobs.gui;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.jobs.JobData;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Écran "Comment gagner de l'XP"
 * Liste toutes les actions qui donnent de l'XP avec leurs niveaux requis
 */
public class JobHowToXPScreen extends Screen {

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
    private static final ResourceLocation LISTITEM_SMALL_PLAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/listitem-small-plain.png");
    private static final ResourceLocation LISTITEM_SMALL_DIMMED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/listitem-small-dimmed.png");
    private static final ResourceLocation BADGE_LEVEL_SMALL_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-small-green.png");
    private static final ResourceLocation BADGE_LEVEL_SMALL_RED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-small-red.png");
    private static final ResourceLocation BADGE_LEVEL_SMALL_GRAY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-small-gray.png");
    private static final ResourceLocation BADGE_LEVEL_SMALL_PURPLE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-small-purple.png");
    private static final ResourceLocation BADGE_LEVEL_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/badge-level-medium-gold.png");
    private static final ResourceLocation SCROLLBAR_TRACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/scrollbar-track-142.png");
    private static final ResourceLocation SCROLLBAR_THUMB = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/scrollbar-thumb-green.png");

    // Données d'exemple des actions XP
    private static final XPAction[] EXAMPLE_ACTIONS = {
        new XPAction("💎", "Mine Diamond Ore", "Mine diamond ore blocks", 10, 50, 50, true),
        new XPAction("⛰️", "Mine Stone", "Mine basic stone blocks", 1, 20, 5, true),
        new XPAction("🔷", "Mine Ancient Debris", "Requires Level 30 • 15 levels to go", 30, 100, 100, false),
        new XPAction("⛏️", "Mine Coal Ore", "No longer available (max level 10)", 1, 10, 3, false),
        new XPAction("⭐", "Mine Emerald Ore", "Mine rare emerald ore", -1, -1, 80, true), // -1 = All Levels
    };

    public JobHowToXPScreen(JobData jobData) {
        super(Component.literal("How to Gain XP"));
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

        // Border green
        ResourceLocation border = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/border-green.png");
        ImageRenderer.renderScaledImage(g, border, leftPos, topPos, GUI_WIDTH, GUI_HEIGHT);

        // Header
        ResourceLocation headerBg = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/bg-header-section-green.png");
        ImageRenderer.renderScaledImage(g, headerBg, leftPos, topPos, 400, 48);

        // Back button
        ResourceLocation backButton = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/button-back-green.png");
        ImageRenderer.renderScaledImage(g, backButton, leftPos + 14, topPos + 16, 24, 24);
        g.drawString(font, "‹", leftPos + 26 - font.width("‹") / 2, topPos + 26, 0x10b981, false);

        // Title
        g.drawString(font, "HOW TO GAIN XP - " + type.getDisplayName().toUpperCase(), leftPos + 46, topPos + 18, 0x10b981, false);
        g.drawString(font, "Actions that give " + type.getDisplayName().toLowerCase() + " experience", leftPos + 46, topPos + 32, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, leftPos + 372, topPos + 16, 14, 14);

        // Level indicator
        ImageRenderer.renderScaledImage(g, PANEL_LEVEL_INDICATOR, leftPos + 8, topPos + 54, 384, 28);
        ImageRenderer.renderScaledImage(g, BADGE_LEVEL_MEDIUM, leftPos + 18, topPos + 60, 40, 16);
        g.drawString(font, "LVL " + jobData.getLevel(), leftPos + 38 - font.width("LVL " + jobData.getLevel()) / 2, topPos + 67, 0x1a1a2e, false);
        g.drawString(font, "Current Level: " + jobData.getLevel(), leftPos + 66, topPos + 67, 0xffffff, false);
        g.drawString(font, "12 actions available", leftPos + 280, topPos + 67, 0x10b981, false);

        // Section header
        ImageRenderer.renderScaledImage(g, PANEL_SECTION_HEADER, leftPos + 8, topPos + 88, 384, 16);
        g.drawString(font, "XP SOURCES", leftPos + 14, topPos + 95, 0x10b981, false);

        // Scroll container
        ImageRenderer.renderScaledImage(g, PANEL_DARK, leftPos + 8, topPos + 110, 384, 148);

        // Render XP actions list
        renderXPActionsList(g);

        // Scrollbar
        renderScrollbar(g);
    }

    private void renderXPActionsList(GuiGraphics g) {
        int startY = topPos + 116;
        int itemHeight = 28;
        int spacing = 4;

        g.enableScissor(leftPos + 14, topPos + 116, leftPos + 386, topPos + 258);

        for (int i = 0; i < EXAMPLE_ACTIONS.length; i++) {
            int yOffset = startY + (i * (itemHeight + spacing)) - scrollOffset;

            if (yOffset + itemHeight >= topPos + 116 && yOffset < topPos + 258) {
                renderXPAction(g, EXAMPLE_ACTIONS[i], leftPos + 14, yOffset);
            }
        }

        g.disableScissor();
    }

    private void renderXPAction(GuiGraphics g, XPAction action, int x, int y) {
        int playerLevel = jobData.getLevel();
        boolean isAvailable = action.isAvailable(playerLevel);
        boolean isLocked = action.minLevel > playerLevel;
        boolean isExpired = !isAvailable && !isLocked && action.maxLevel > 0;

        // Card background
        if (isAvailable) {
            ImageRenderer.renderScaledImage(g, LISTITEM_SMALL_PLAIN, x, y, 372, 28);
        } else {
            ImageRenderer.renderScaledImageWithAlpha(g, LISTITEM_SMALL_DIMMED, x, y, 372, 28, 0.4f);
        }

        // Icon placeholder
        ImageRenderer.renderScaledImage(g, ICON_PLACEHOLDER_16, x + 6, y + 6, 16, 16);
        g.drawString(font, action.icon, x + 14 - font.width(action.icon) / 2, y + 11, isAvailable ? 0x9ca3af : 0x6b7280, false);

        // Name
        int nameColor = isAvailable ? 0xffffff : 0x6b7280;
        g.drawString(font, action.name, x + 28, y + 10, nameColor, false);

        // Description
        int descColor = isAvailable ? 0x9ca3af : (isLocked ? 0xef4444 : 0x9ca3af);
        g.drawString(font, action.description, x + 28, y + 20, descColor, false);

        // Level badge
        int badgeX = x + 226;
        int badgeY = y + 8;

        if (action.minLevel == -1) {
            // All Levels
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_SMALL_PURPLE, badgeX, badgeY, 50, 12);
            g.drawString(font, "All Levels", badgeX + 25 - font.width("All Levels") / 2, badgeY + 4, 0xffffff, false);
        } else if (isLocked) {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_SMALL_RED, badgeX, badgeY, 50, 12);
            String levelText = "Lvl " + action.minLevel + "+";
            g.drawString(font, levelText, badgeX + 25 - font.width(levelText) / 2, badgeY + 4, 0xffffff, false);
        } else if (isExpired) {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_SMALL_GRAY, badgeX, badgeY, 50, 12);
            String levelText = "Lvl " + action.minLevel + "-" + action.maxLevel;
            g.drawString(font, levelText, badgeX + 25 - font.width(levelText) / 2, badgeY + 4, 0xffffff, false);
        } else {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_SMALL_GREEN, badgeX, badgeY, 50, 12);
            String levelText = action.maxLevel > 0 ? "Lvl " + action.minLevel + "-" + action.maxLevel : "Lvl " + action.minLevel + "+";
            g.drawString(font, levelText, badgeX + 25 - font.width(levelText) / 2, badgeY + 4, 0xffffff, false);
        }

        // XP value
        String xpText = "+" + action.xpValue + " XP";
        int xpColor = isAvailable ? 0xfbbf24 : 0x6b7280;
        g.drawString(font, xpText, x + 364 - font.width(xpText), y + 15, xpColor, false);
    }

    private void renderScrollbar(GuiGraphics g) {
        int scrollbarX = leftPos + 390;
        int scrollbarY = topPos + 116;

        ImageRenderer.renderScaledImage(g, SCROLLBAR_TRACK, scrollbarX, scrollbarY, 4, 142);

        int totalHeight = EXAMPLE_ACTIONS.length * 32;
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
        int totalHeight = EXAMPLE_ACTIONS.length * 32;
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

    // Classe interne pour les actions XP
    private static class XPAction {
        String icon;
        String name;
        String description;
        int minLevel; // -1 pour "All Levels"
        int maxLevel; // -1 pour illimité
        int xpValue;
        boolean available;

        XPAction(String icon, String name, String description, int minLevel, int maxLevel, int xpValue, boolean available) {
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.xpValue = xpValue;
            this.available = available;
        }

        boolean isAvailable(int playerLevel) {
            if (minLevel == -1) return true; // All levels
            if (minLevel > playerLevel) return false; // Trop bas
            if (maxLevel > 0 && playerLevel > maxLevel) return false; // Trop haut
            return available;
        }
    }
}
