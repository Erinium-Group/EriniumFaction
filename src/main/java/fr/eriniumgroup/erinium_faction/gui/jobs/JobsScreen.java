package fr.eriniumgroup.erinium_faction.gui.jobs;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelAttachments;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Écran principal du système Jobs simplifié
 * Affiche le niveau du joueur et deux boutons: Unlocked Features et How to gain XP
 */
public class JobsScreen extends Screen {

    // Textures
    private static final ResourceLocation BG_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/bg-full-main.png");
    private static final ResourceLocation BORDER_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/border-main.png");
    private static final ResourceLocation PANEL_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-header.png");
    private static final ResourceLocation PANEL_CARD_LARGE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-card-large.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-close.png");
    private static final ResourceLocation BUTTON_LARGE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-large.png");
    private static final ResourceLocation PROGRESSBAR_TRACK_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-track-medium.png");
    private static final ResourceLocation PROGRESSBAR_FILL_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-fill-main.png");
    private static final ResourceLocation LEVEL_BADGE_LARGE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/level-badge-large.png");

    // Dimensions
    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    private int guiLeft;
    private int guiTop;

    // Player data (récupéré côté client depuis l'attachment)
    private int level = 1;
    private int xp = 0;
    private int maxXP = 100;

    public JobsScreen() {
        super(Component.literal("Jobs System"));
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        refreshData();
    }

    /**
     * Rafraîchit les données du joueur
     */
    private void refreshData() {
        if (minecraft != null && minecraft.player != null) {
            PlayerLevelData data = minecraft.player.getData(PlayerLevelAttachments.PLAYER_LEVEL_DATA);
            this.level = data.getLevel();
            this.xp = data.getExperience();
            this.maxXP = data.getExperienceToNextLevel();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Rafraîchir les données à chaque frame pour s'assurer qu'elles sont à jour
        refreshData();

        // Background
        ImageRenderer.renderScaledImage(g, BG_MAIN, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

        // Border
        ImageRenderer.renderScaledImage(g, BORDER_MAIN, guiLeft + 2, guiTop + 2, 396, 266);

        // Header
        ImageRenderer.renderScaledImage(g, PANEL_HEADER, guiLeft + 8, guiTop + 8, 384, 40);
        g.drawString(this.font, "JOBS SYSTEM", guiLeft + 50, guiTop + 20, 0xfbbf24, false);
        g.drawString(this.font, "Progress through experience and unlock features", guiLeft + 50, guiTop + 32, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, guiLeft + 372, guiTop + 16, 14, 14);

        // Main Panel - Player Level
        ImageRenderer.renderScaledImage(g, PANEL_CARD_LARGE, guiLeft + 8, guiTop + 54, 384, 80);

        // Level badge (grande taille, centré)
        int badgeCenterX = guiLeft + 200;
        int badgeCenterY = guiTop + 78;
        ImageRenderer.renderScaledImage(g, LEVEL_BADGE_LARGE, badgeCenterX - 30, badgeCenterY - 24, 60, 48);
        g.drawCenteredString(this.font, "LEVEL", badgeCenterX, badgeCenterY - 8, 0xFFFFFF);
        g.drawCenteredString(this.font, String.valueOf(level), badgeCenterX, badgeCenterY + 6, 0xfbbf24);

        // XP bar
        int barWidth = 300;
        int barX = guiLeft + (GUI_WIDTH - barWidth) / 2;
        int barY = guiTop + 110;
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_TRACK_MEDIUM, barX, barY, barWidth, 12);

        // XP fill
        int xpWidth = maxXP > 0 ? (int) ((float) xp / maxXP * barWidth) : 0;
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_FILL_MAIN, barX, barY, xpWidth, 12);

        // XP text
        String xpText = xp + " / " + maxXP + " XP";
        g.drawCenteredString(this.font, xpText, barX + barWidth / 2, barY + 2, 0xFFFFFF);

        // Buttons section
        int buttonY = guiTop + 150;
        int buttonWidth = 180;
        int buttonHeight = 40;
        int buttonSpacing = 12;
        int button1X = guiLeft + (GUI_WIDTH - buttonWidth * 2 - buttonSpacing) / 2;
        int button2X = button1X + buttonWidth + buttonSpacing;

        // Button 1 - Unlocked Features
        boolean button1Hovered = isMouseOver(mouseX, mouseY, button1X, buttonY, buttonWidth, buttonHeight);
        renderButton(g, button1X, buttonY, buttonWidth, buttonHeight, button1Hovered, "Unlocked Features", "View your unlocked abilities");

        // Button 2 - How to gain XP
        boolean button2Hovered = isMouseOver(mouseX, mouseY, button2X, buttonY, buttonWidth, buttonHeight);
        renderButton(g, button2X, buttonY, buttonWidth, buttonHeight, button2Hovered, "How to gain XP", "See all XP-giving actions");

        // Stats footer
        int unlockedCount = 0;
        int totalFeatures = JobFeature.values().length;
        for (JobFeature feature : JobFeature.values()) {
            if (feature.isUnlocked(level)) {
                unlockedCount++;
            }
        }
        String statsText = unlockedCount + " / " + totalFeatures + " features unlocked";
        g.drawCenteredString(this.font, statsText, guiLeft + GUI_WIDTH / 2, guiTop + 220, 0x10b981);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderButton(GuiGraphics g, int x, int y, int width, int height, boolean hovered, String title, String subtitle) {
        // Button background
        int bgColor = hovered ? 0xE01e1e2e : 0xCC1e1e2e;
        g.fill(x, y, x + width, y + height, bgColor);

        // Border si hover
        if (hovered) {
            g.fill(x, y, x + width, y + 1, 0xfbbf24);
            g.fill(x, y + height - 1, x + width, y + height, 0xfbbf24);
        }

        // Title
        g.drawCenteredString(this.font, title, x + width / 2, y + 12, 0xFFFFFF);

        // Subtitle
        g.drawCenteredString(this.font, subtitle, x + width / 2, y + 24, 0x9ca3af);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Close button
        if (isMouseOver(mouseX, mouseY, guiLeft + 372, guiTop + 16, 14, 14)) {
            this.onClose();
            return true;
        }

        int buttonY = guiTop + 150;
        int buttonWidth = 180;
        int buttonHeight = 40;
        int buttonSpacing = 12;
        int button1X = guiLeft + (GUI_WIDTH - buttonWidth * 2 - buttonSpacing) / 2;
        int button2X = button1X + buttonWidth + buttonSpacing;

        // Button 1 - Unlocked Features
        if (isMouseOver(mouseX, mouseY, button1X, buttonY, buttonWidth, buttonHeight)) {
            minecraft.setScreen(new UnlockedFeaturesScreen(level, xp, maxXP));
            return true;
        }

        // Button 2 - How to gain XP
        if (isMouseOver(mouseX, mouseY, button2X, buttonY, buttonWidth, buttonHeight)) {
            minecraft.setScreen(new HowToXPScreen(level, xp, maxXP));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - pas de background blur
    }
}
