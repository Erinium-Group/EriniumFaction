package fr.eriniumgroup.erinium_faction.gui.jobs;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Écran de détail d'un job spécifique
 * Affiche niveau, XP, et deux boutons: "How to gain XP" et "Unlocked Features"
 */
public class JobDetailScreen extends Screen {

    private static final ResourceLocation PANEL_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-header.png");
    private static final ResourceLocation PANEL_CARD_LARGE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-card-large.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-close.png");
    private static final ResourceLocation BUTTON_BACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-back.png");
    private static final ResourceLocation BUTTON_LARGE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-large.png");
    private static final ResourceLocation LEVEL_BADGE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/level-badge-large.png");
    private static final ResourceLocation PROGRESSBAR_TRACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-track-large.png");
    private static final ResourceLocation PANEL_SCROLL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-scroll-container.png");

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    private int guiLeft;
    private int guiTop;

    private final String jobName;
    private final int level;
    private final int xp;
    private final int maxXP;
    private final JobType jobType;

    public JobDetailScreen(String jobName, int level, int xp, int maxXP, JobType jobType) {
        super(Component.literal(jobName));
        this.jobName = jobName;
        this.level = level;
        this.xp = xp;
        this.maxXP = maxXP;
        this.jobType = jobType;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        ImageRenderer.renderScaledImage(g, jobType.getBackground(), guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

        // Border
        ImageRenderer.renderScaledImage(g, jobType.getBorder(), guiLeft + 2, guiTop + 2, 396, 266);

        // Header
        ImageRenderer.renderScaledImage(g, PANEL_HEADER, guiLeft + 8, guiTop + 8, 384, 40);

        // Back button
        ImageRenderer.renderScaledImage(g, BUTTON_BACK, guiLeft + 14, guiTop + 16, 24, 24);
        g.drawCenteredString(this.font, "‹", guiLeft + 26, guiTop + 26, jobType.getColor());

        // Job icon (cercle)
        g.fill(guiLeft + 58 - 14, guiTop + 28 - 14, guiLeft + 58 + 14, guiTop + 28 + 14, jobType.getColor());

        // Title
        g.drawString(this.font, jobName.toUpperCase(), guiLeft + 80, guiTop + 20, jobType.getColor(), false);
        g.drawString(this.font, jobType.getSubtitle(), guiLeft + 80, guiTop + 32, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, guiLeft + 372, guiTop + 16, 14, 14);

        // Level & XP Section
        ImageRenderer.renderScaledImage(g, PANEL_CARD_LARGE, guiLeft + 8, guiTop + 54, 384, 48);

        // Level badge
        ImageRenderer.renderScaledImage(g, LEVEL_BADGE, guiLeft + 18, guiTop + 62, 42, 30);
        g.drawCenteredString(this.font, "LVL", guiLeft + 39, guiTop + 69, 0xFFFFFF);
        g.drawCenteredString(this.font, String.valueOf(level), guiLeft + 39, guiTop + 81, 0xFFFFFF);

        // Level info
        g.drawString(this.font, "Level " + level, guiLeft + 70, guiTop + 65, 0xFFFFFF, false);
        int xpToNext = maxXP - xp;
        g.drawString(this.font, xpToNext + " XP to next level", guiLeft + 70, guiTop + 76, 0x9ca3af, false);

        // XP bar (barre à y=85, hauteur=9, donc centre à y=85+4.5 ≈ 89)
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_TRACK, guiLeft + 70, guiTop + 85, 315, 9);
        int xpWidth = (int) ((float) xp / maxXP * 315);
        ImageRenderer.renderScaledImage(g, jobType.getProgressFill(), guiLeft + 70, guiTop + 85, xpWidth, 9);
        String xpText = xp + " / " + maxXP + " XP";
        // Texte centré: barre commence à 85, +4 pour centrer (font height ~9)
        g.drawCenteredString(this.font, xpText, guiLeft + 227, guiTop + 89, 0xFFFFFF);

        // Action buttons
        ImageRenderer.renderScaledImage(g, BUTTON_LARGE, guiLeft + 8, guiTop + 108, 188, 32);
        g.drawCenteredString(this.font, "How to gain XP", guiLeft + 102, guiTop + 124, 0xFFFFFF);

        ImageRenderer.renderScaledImage(g, BUTTON_LARGE, guiLeft + 204, guiTop + 108, 188, 32);
        g.drawCenteredString(this.font, "Unlocked Features", guiLeft + 298, guiTop + 124, 0xFFFFFF);

        // Info section (placeholder)
        ImageRenderer.renderScaledImage(g, PANEL_SCROLL, guiLeft + 8, guiTop + 146, 384, 112);
        g.drawCenteredString(this.font, "Click a button above", guiLeft + 200, guiTop + 195, jobType.getColor());
        g.drawCenteredString(this.font, "to view job details", guiLeft + 200, guiTop + 210, 0x9ca3af);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Close button
        if (isMouseOver(mouseX, mouseY, guiLeft + 372, guiTop + 16, 14, 14)) {
            this.onClose();
            return true;
        }

        // Back button
        if (isMouseOver(mouseX, mouseY, guiLeft + 14, guiTop + 16, 24, 24)) {
            minecraft.setScreen(new JobsScreen());
            return true;
        }

        // How to gain XP button
        if (isMouseOver(mouseX, mouseY, guiLeft + 8, guiTop + 108, 188, 32)) {
            minecraft.setScreen(new HowToXPScreen(jobName, level, xp, maxXP, jobType));
            return true;
        }

        // Unlocked Features button
        if (isMouseOver(mouseX, mouseY, guiLeft + 204, guiTop + 108, 188, 32)) {
            minecraft.setScreen(new UnlockedFeaturesScreen(jobName, level, xp, maxXP, jobType));
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
