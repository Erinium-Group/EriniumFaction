package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.features.jobs.type.UnlockType;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import fr.eriniumgroup.erinium_faction.features.jobs.data.JobData;
import fr.eriniumgroup.erinium_faction.features.jobs.type.JobType;
import fr.eriniumgroup.erinium_faction.common.config.JobConfig;
import fr.eriniumgroup.erinium_faction.features.jobs.type.UnlockingEntry;
import fr.eriniumgroup.erinium_faction.common.network.packets.JobsClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

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
    private List<UnlockingEntry> unlockingEntries = new ArrayList<>();
    private int unlockedCount = 0;
    private int lockedCount = 0;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

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

    public JobUnlockedFeaturesScreen(JobData jobData) {
        super(Component.translatable("erinium_faction.jobs.unlocked_features.title"));
        this.jobData = jobData;
        loadUnlockingEntries();
    }

    /**
     * Charge les entrées de débloquage depuis la configuration
     */
    private void loadUnlockingEntries() {
        JobConfig config = JobsClientConfig.getConfig(jobData.getType());
        if (config != null) {
            unlockingEntries = config.getUnlocking();
            // Compter combien sont débloquées/verrouillées au niveau actuel
            unlockedCount = 0;
            lockedCount = 0;
            for (UnlockingEntry entry : unlockingEntries) {
                if (entry.isUnlockedAtLevel(jobData.getLevel())) {
                    unlockedCount++;
                } else {
                    lockedCount++;
                }
            }
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
        g.drawString(font, Component.translatable("erinium_faction.jobs.unlocked_features.title").getString() + " - " + type.getDisplayName().toUpperCase(), leftPos + 46, topPos + 18, 0xa855f7, false);
        g.drawString(font, Component.translatable("erinium_faction.jobs.unlocked_features.subtitle").getString(), leftPos + 46, topPos + 32, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, leftPos + 372, topPos + 16, 14, 14);

        // Level indicator
        ImageRenderer.renderScaledImage(g, PANEL_LEVEL_INDICATOR, leftPos + 8, topPos + 54, 384, 28);
        ImageRenderer.renderScaledImage(g, BADGE_LEVEL_MEDIUM, leftPos + 18, topPos + 60, 40, 16);
        String lvlText = String.format(Component.translatable("erinium_faction.jobs.badge.level").getString(), jobData.getLevel());
        g.drawString(font, lvlText, leftPos + 38 - font.width(lvlText) / 2, topPos + 67, 0x1a1a2e, false);
        String currentLevelText = String.format(Component.translatable("erinium_faction.jobs.unlocked_features.current_level").getString(), jobData.getLevel());
        g.drawString(font, currentLevelText, leftPos + 66, topPos + 67, 0xffffff, false);

        // Stats débloquées
        String statsText = String.format(Component.translatable("erinium_faction.jobs.unlocked_features.stats").getString(), unlockedCount, lockedCount);
        g.drawString(font, statsText, leftPos + 250, topPos + 67, 0x10b981, false);

        // Section header
        ImageRenderer.renderScaledImage(g, PANEL_SECTION_HEADER, leftPos + 8, topPos + 88, 384, 16);
        g.drawString(font, Component.translatable("erinium_faction.jobs.unlocked_features.your_features").getString(), leftPos + 14, topPos + 95, 0xa855f7, false);

        // Scroll container
        ImageRenderer.renderScaledImage(g, PANEL_DARK, leftPos + 8, topPos + 110, 384, 148);

        // Render features list
        renderFeaturesList(g);

        // Scrollbar
        renderScrollbar(g);

        // Progress summary at bottom (si on veut)
        if (lockedCount > 0) {
            UnlockingEntry nextUnlock = getNextUnlock();
            if (nextUnlock != null) {
                String nextUnlockText = String.format(Component.translatable("erinium_faction.jobs.unlocked_features.next_unlock").getString(), nextUnlock.getLevel(), nextUnlock.getDisplayName());
                g.drawString(font, nextUnlockText,
                    leftPos + 200 - font.width(nextUnlockText) / 2,
                    topPos + 264, 0x9ca3af, false);
            }
        }
    }

    private void renderFeaturesList(GuiGraphics g) {
        int startY = topPos + 116;
        int itemHeight = 28;
        int spacing = 4;

        g.enableScissor(leftPos + 14, topPos + 116, leftPos + 386, topPos + 258);

        for (int i = 0; i < unlockingEntries.size(); i++) {
            int yOffset = startY + (i * (itemHeight + spacing)) - scrollOffset;

            if (yOffset + itemHeight >= topPos + 116 && yOffset < topPos + 258) {
                renderFeature(g, unlockingEntries.get(i), leftPos + 14, yOffset);
            }
        }

        g.disableScissor();
    }

    private void renderFeature(GuiGraphics g, UnlockingEntry entry, int x, int y) {
        int playerLevel = jobData.getLevel();
        boolean isUnlocked = entry.isUnlockedAtLevel(playerLevel);

        // Card background
        if (isUnlocked) {
            ImageRenderer.renderScaledImage(g, LISTITEM_SMALL_GREEN, x, y, 372, 28);
        } else {
            ImageRenderer.renderScaledImageWithAlpha(g, LISTITEM_SMALL_RED, x, y, 372, 28, 0.6f);
        }

        // Icon - afficher l'item si c'est un ITEM ou BLOCK
        if (entry.getType() == UnlockType.ITEM ||
            entry.getType() == UnlockType.BLOCK) {
            try {
                net.minecraft.world.item.ItemStack itemStack = new net.minecraft.world.item.ItemStack(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        net.minecraft.resources.ResourceLocation.parse(entry.getTargetId())
                    )
                );
                g.renderItem(itemStack, x + 6, y + 6);
            } catch (Exception e) {
                // Si l'item n'existe pas, utiliser l'emoji par défaut
                ImageRenderer.renderScaledImage(g, ICON_PLACEHOLDER_16, x + 6, y + 6, 16, 16);
                String icon = isUnlocked ? getIconForUnlock(entry) : "🔒";
                int iconColor = isUnlocked ? 0x10b981 : 0x6b7280;
                g.drawString(font, icon, x + 14 - font.width(icon) / 2, y + 11, iconColor, false);
            }
        } else {
            // Pour DIMENSION et CUSTOM, utiliser l'emoji
            ImageRenderer.renderScaledImage(g, ICON_PLACEHOLDER_16, x + 6, y + 6, 16, 16);
            String icon = isUnlocked ? getIconForUnlock(entry) : "🔒";
            int iconColor = isUnlocked ? 0x10b981 : 0x6b7280;
            g.drawString(font, icon, x + 14 - font.width(icon) / 2, y + 11, iconColor, false);
        }

        // Name - remonté de 2 pixels (y + 8 au lieu de y + 10) avec auto-scroll
        int nameColor = isUnlocked ? 0x10b981 : 0x9ca3af;
        String name = entry.getDisplayName() != null && !entry.getDisplayName().isEmpty() ? entry.getDisplayName() : formatUnlockName(entry);
        boolean itemHovered = lastMouseX >= x && lastMouseX <= x + 372 && lastMouseY >= y && lastMouseY <= y + 28;
        TextHelper.drawAutoScrollingText(g, font, name, x + 28, y + 8, 290, nameColor, false, itemHovered, "feature_name_" + entry.getType() + "_" + entry.getTargetId());

        // Description - remonté de 2 pixels (y + 18 au lieu de y + 20)
        int descColor = isUnlocked ? 0x9ca3af : 0xef4444;
        String description = formatUnlockDescription(entry, playerLevel);
        g.drawString(font, description, x + 28, y + 18, descColor, false);

        // Level badge
        int badgeX = x + 326;
        int badgeY = y + 8;

        if (isUnlocked) {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_TINY_GREEN, badgeX, badgeY, 36, 12);
        } else {
            ImageRenderer.renderScaledImage(g, BADGE_LEVEL_TINY_RED, badgeX, badgeY, 36, 12);
        }

        String levelText = String.format(Component.translatable("erinium_faction.jobs.badge.level").getString(), entry.getLevel());
        g.drawString(font, levelText, badgeX + 18 - font.width(levelText) / 2, badgeY + 4, 0xffffff, false);
    }

    /**
     * Obtient une icône emoji pour le débloquage
     */
    private String getIconForUnlock(UnlockingEntry entry) {
        return switch (entry.getType()) {
            case ITEM -> "📦";
            case BLOCK -> "🧱";
            case DIMENSION -> "🌍";
            case CUSTOM -> "⭐";
        };
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

    /**
     * Formate la description du débloquage
     */
    private String formatUnlockDescription(UnlockingEntry entry, int playerLevel) {
        if (playerLevel < entry.getLevel()) {
            int levelsToGo = entry.getLevel() - playerLevel;
            String levelsPart = String.format(Component.translatable("erinium_faction.jobs.status.levels_to_go").getString(), levelsToGo, levelsToGo != 1 ? "s" : "", levelsToGo != 1 ? "s" : "");
            String requiresLevel = String.format(Component.translatable("erinium_faction.jobs.status.requires_level").getString(), entry.getLevel());
            return requiresLevel + " • " + levelsPart;
        }

        // Si débloqué, utiliser la description de la config ou une description par défaut
        if (entry.getDescription() != null && !entry.getDescription().isEmpty()) {
            return entry.getDescription();
        }
        return entry.getType().name() + ": " + entry.getTargetId();
    }

    /**
     * Obtient le prochain débloquage
     */
    private UnlockingEntry getNextUnlock() {
        UnlockingEntry next = null;
        int minLevel = Integer.MAX_VALUE;

        for (UnlockingEntry entry : unlockingEntries) {
            if (!entry.isUnlockedAtLevel(jobData.getLevel()) && entry.getLevel() < minLevel) {
                next = entry;
                minLevel = entry.getLevel();
            }
        }

        return next;
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
        if (unlockingEntries.isEmpty()) {
            return;
        }

        int totalHeight = unlockingEntries.size() * 32;
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
        int totalHeight = unlockingEntries.size() * 32;
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
