package fr.eriniumgroup.erinium_faction.jobs.gui;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import fr.eriniumgroup.erinium_faction.jobs.JobData;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import fr.eriniumgroup.erinium_faction.jobs.config.JobConfig;
import fr.eriniumgroup.erinium_faction.jobs.config.XpEarningEntry;
import fr.eriniumgroup.erinium_faction.jobs.network.JobsClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

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
    private List<XpEarningEntry> xpEntries = new ArrayList<>();
    private int availableCount = 0;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

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

    public JobHowToXPScreen(JobData jobData) {
        super(Component.translatable("erinium_faction.jobs.how_to_xp.title"));
        this.jobData = jobData;
        loadXpEntries();
    }

    /**
     * Charge les entrées XP depuis la configuration
     */
    private void loadXpEntries() {
        JobConfig config = JobsClientConfig.getConfig(jobData.getType());
        System.out.println("[DEBUG] JobHowToXPScreen - Config for " + jobData.getType() + ": " + config);
        if (config != null) {
            xpEntries = config.getXpEarning();
            System.out.println("[DEBUG] JobHowToXPScreen - XP Entries count: " + xpEntries.size());
            // Compter combien sont disponibles au niveau actuel
            availableCount = 0;
            for (XpEarningEntry entry : xpEntries) {
                if (entry.isAvailableAtLevel(jobData.getLevel())) {
                    availableCount++;
                }
            }
        } else {
            System.out.println("[DEBUG] JobHowToXPScreen - Config is NULL!");
        }
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

        // Sauvegarder les positions de la souris
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

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
        g.drawString(font, Component.translatable("erinium_faction.jobs.how_to_xp.title").getString() + " - " + type.getDisplayName().toUpperCase(), leftPos + 46, topPos + 18, 0x10b981, false);
        g.drawString(font, Component.translatable("erinium_faction.jobs.how_to_xp.subtitle").getString(), leftPos + 46, topPos + 32, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, leftPos + 372, topPos + 16, 14, 14);

        // Level indicator
        ImageRenderer.renderScaledImage(g, PANEL_LEVEL_INDICATOR, leftPos + 8, topPos + 54, 384, 28);
        ImageRenderer.renderScaledImage(g, BADGE_LEVEL_MEDIUM, leftPos + 18, topPos + 60, 40, 16);
        String lvlText = String.format(Component.translatable("erinium_faction.jobs.badge.level").getString(), jobData.getLevel());
        g.drawString(font, lvlText, leftPos + 38 - font.width(lvlText) / 2, topPos + 67, 0x1a1a2e, false);
        String currentLevelText = String.format(Component.translatable("erinium_faction.jobs.detail.current_level").getString(), jobData.getLevel());
        g.drawString(font, currentLevelText, leftPos + 66, topPos + 67, 0xffffff, false);

        String availableText = availableCount + " action" + (availableCount != 1 ? "s" : "") + " available";
        g.drawString(font, availableText, leftPos + 280, topPos + 67, 0x10b981, false);

        // Section header
        ImageRenderer.renderScaledImage(g, PANEL_SECTION_HEADER, leftPos + 8, topPos + 88, 384, 16);
        g.drawString(font, Component.translatable("erinium_faction.jobs.how_to_xp.your_actions").getString(), leftPos + 14, topPos + 95, 0x10b981, false);

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

        for (int i = 0; i < xpEntries.size(); i++) {
            int yOffset = startY + (i * (itemHeight + spacing)) - scrollOffset;

            if (yOffset + itemHeight >= topPos + 116 && yOffset < topPos + 258) {
                renderXPAction(g, xpEntries.get(i), leftPos + 14, yOffset);
            }
        }

        g.disableScissor();
    }

    private void renderXPAction(GuiGraphics g, XpEarningEntry entry, int x, int y) {
        int playerLevel = jobData.getLevel();
        boolean isAvailable = entry.isAvailableAtLevel(playerLevel);
        boolean isLocked = entry.getMinLevel() != -1 && entry.getMinLevel() > playerLevel;
        boolean isExpired = !isAvailable && !isLocked && entry.getMaxLevel() > 0;

        // Card background
        if (isAvailable) {
            ImageRenderer.renderScaledImage(g, LISTITEM_SMALL_PLAIN, x, y, 372, 28);
        } else {
            ImageRenderer.renderScaledImageWithAlpha(g, LISTITEM_SMALL_DIMMED, x, y, 372, 28, 0.4f);
        }

        // Icon - charger l'item depuis entry.getIconItem()
        if (entry.getIconItem() != null && !entry.getIconItem().isEmpty()) {
            try {
                net.minecraft.world.item.ItemStack itemStack = new net.minecraft.world.item.ItemStack(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        net.minecraft.resources.ResourceLocation.parse(entry.getIconItem())
                    )
                );
                g.renderItem(itemStack, x + 6, y + 6);
            } catch (Exception e) {
                // Si l'item n'existe pas, utiliser l'emoji par défaut
                ImageRenderer.renderScaledImage(g, ICON_PLACEHOLDER_16, x + 6, y + 6, 16, 16);
                String icon = getIconForAction(entry);
                g.drawString(font, icon, x + 14 - font.width(icon) / 2, y + 11, isAvailable ? 0x9ca3af : 0x6b7280, false);
            }
        } else {
            ImageRenderer.renderScaledImage(g, ICON_PLACEHOLDER_16, x + 6, y + 6, 16, 16);
            String icon = getIconForAction(entry);
            g.drawString(font, icon, x + 14 - font.width(icon) / 2, y + 11, isAvailable ? 0x9ca3af : 0x6b7280, false);
        }

        // Name - remonté de 2 pixels (y + 8 au lieu de y + 10) avec auto-scroll
        int nameColor = isAvailable ? 0xffffff : 0x6b7280;
        String name = formatActionName(entry);
        boolean itemHovered = lastMouseX >= x && lastMouseX <= x + 372 && lastMouseY >= y && lastMouseY <= y + 28;
        TextHelper.drawAutoScrollingText(g, font, name, x + 28, y + 8, 190, nameColor, false, itemHovered, "action_name_" + entry.getActionType() + "_" + entry.getTargetId());

        // Description - remonté de 2 pixels (y + 18 au lieu de y + 20)
        int descColor = isAvailable ? 0x9ca3af : (isLocked ? 0xef4444 : 0x9ca3af);
        String description = formatActionDescription(entry, playerLevel);
        g.drawString(font, description, x + 28, y + 18, descColor, false);

        // Level badge
        int badgeX = x + 226;
        int badgeY = y + 8;
        int badgeWidth = 50;

        // Déterminer le texte du badge
        String levelText;
        ResourceLocation badgeTexture;

        if (entry.getMinLevel() == -1 && entry.getMaxLevel() == -1) {
            // All Levels
            badgeTexture = BADGE_LEVEL_SMALL_PURPLE;
            levelText = Component.translatable("erinium_faction.jobs.status.all_levels").getString();
        } else if (isLocked) {
            badgeTexture = BADGE_LEVEL_SMALL_RED;
            if (entry.getMaxLevel() > 0) {
                levelText = entry.getMinLevel() + "-" + entry.getMaxLevel();
            } else {
                levelText = entry.getMinLevel() + "+";
            }
        } else if (isExpired) {
            badgeTexture = BADGE_LEVEL_SMALL_GRAY;
            levelText = entry.getMinLevel() + "-" + entry.getMaxLevel();
        } else {
            badgeTexture = BADGE_LEVEL_SMALL_GREEN;
            if (entry.getMaxLevel() > 0) {
                levelText = entry.getMinLevel() + "-" + entry.getMaxLevel();
            } else {
                levelText = entry.getMinLevel() + "+";
            }
        }

        ImageRenderer.renderScaledImage(g, badgeTexture, badgeX, badgeY, badgeWidth, 12);

        // Utiliser auto-scroll si le texte dépasse
        boolean badgeHovered = lastMouseX >= badgeX && lastMouseX <= badgeX + badgeWidth && lastMouseY >= badgeY && lastMouseY <= badgeY + 12;
        TextHelper.drawAutoScrollingText(g, font, levelText, badgeX + badgeWidth / 2 - Math.min(font.width(levelText), badgeWidth - 4) / 2, badgeY + 4, badgeWidth - 4, 0xffffff, false, badgeHovered, "badge_level_" + entry.getTargetId());

        // XP value avec traduction
        String xpText = String.format(Component.translatable("erinium_faction.jobs.reward.xp").getString(), entry.getXpEarned());
        int xpColor = isAvailable ? 0xfbbf24 : 0x6b7280;
        g.drawString(font, xpText, x + 364 - font.width(xpText), y + 15, xpColor, false);
    }

    /**
     * Obtient une icône emoji pour l'action
     */
    private String getIconForAction(XpEarningEntry entry) {
        return switch (entry.getActionType()) {
            case BREAK -> "⛏";
            case PLACE -> "🧱";
            case CRAFT -> "🔨";
            case SMELT -> "🔥";
            case KILL -> "⚔";
            case FISHING -> "🎣";
            case DRINK -> "🥤";
            case EAT -> "🍖";
            case USE -> "✋";
            case THROW -> "🎯";
            case OTHER -> "📦";
            case CUSTOM -> "⭐";
        };
    }

    /**
     * Formate le nom de l'action
     */
    private String formatActionName(XpEarningEntry entry) {
        String[] parts = entry.getTargetId().split(":");
        String itemName = parts.length > 1 ? parts[1] : entry.getTargetId();
        itemName = itemName.replace("_", " ");
        return entry.getActionType().name() + " " + capitalize(itemName);
    }

    /**
     * Formate la description de l'action
     */
    private String formatActionDescription(XpEarningEntry entry, int playerLevel) {
        if (entry.getMinLevel() != -1 && playerLevel < entry.getMinLevel()) {
            int levelsToGo = entry.getMinLevel() - playerLevel;
            return "Requires Level " + entry.getMinLevel() + " • " + levelsToGo + " level" + (levelsToGo != 1 ? "s" : "") + " to go";
        }
        if (entry.getMaxLevel() != -1 && playerLevel > entry.getMaxLevel()) {
            return "No longer available (max level " + entry.getMaxLevel() + ")";
        }
        return entry.getActionType().name().toLowerCase() + " " + entry.getTargetId();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void renderScrollbar(GuiGraphics g) {
        int scrollbarX = leftPos + 390;
        int scrollbarY = topPos + 116;

        ImageRenderer.renderScaledImage(g, SCROLLBAR_TRACK, scrollbarX, scrollbarY, 4, 142);

        // Ne pas afficher le thumb si la liste est vide
        if (xpEntries.isEmpty()) {
            return;
        }

        int totalHeight = xpEntries.size() * 32;
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
        int totalHeight = xpEntries.size() * 32;
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
}
