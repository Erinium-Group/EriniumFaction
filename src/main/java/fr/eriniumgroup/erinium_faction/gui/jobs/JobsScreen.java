package fr.eriniumgroup.erinium_faction.gui.jobs;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran principal du système Jobs
 * Affiche la liste de tous les jobs disponibles avec scroll fonctionnel
 */
public class JobsScreen extends Screen {

    // Textures
    private static final ResourceLocation BG_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/bg-full-main.png");
    private static final ResourceLocation BORDER_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/border-main.png");
    private static final ResourceLocation PANEL_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-header.png");
    private static final ResourceLocation PANEL_CARD_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-card-medium.png");
    private static final ResourceLocation PANEL_TITLE_BAR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-title-bar.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-close.png");
    private static final ResourceLocation BUTTON_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-medium.png");
    private static final ResourceLocation PROGRESSBAR_TRACK_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-track-medium.png");
    private static final ResourceLocation PROGRESSBAR_FILL_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-fill-main.png");
    private static final ResourceLocation PROGRESSBAR_TRACK_SMALL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-track-small.png");

    // Dimensions
    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    private int guiLeft;
    private int guiTop;

    // Data exemple
    private int mainJobLevel = 42;
    private int mainJobXP = 42500;
    private int mainJobMaxXP = 50000;

    // ScrollList
    private ScrollList<JobData> jobsList;

    // Job data class
    public static class JobData {
        public String name;
        public int level;
        public int xp;
        public int maxXP;
        public JobType type;
        public ResourceLocation progressFill;

        public JobData(String name, int level, int xp, int maxXP, JobType type, ResourceLocation progressFill) {
            this.name = name;
            this.level = level;
            this.xp = xp;
            this.maxXP = maxXP;
            this.type = type;
            this.progressFill = progressFill;
        }
    }

    public JobsScreen() {
        super(Component.literal("Jobs System"));
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        // Initialiser la scroll list
        jobsList = new ScrollList<>(this.font, this::renderJobCard, 36);
        jobsList.setBounds(guiLeft + 8, guiTop + 138, 384, 120);

        // Ajouter les jobs d'exemple
        List<JobData> jobs = createExampleJobs();
        jobsList.setItems(jobs);

        // Callback pour clic sur job
        jobsList.setOnItemClick(job -> {
            minecraft.setScreen(new JobDetailScreen(job.name, job.level, job.xp, job.maxXP, job.type));
        });
    }

    private List<JobData> createExampleJobs() {
        List<JobData> jobs = new ArrayList<>();
        jobs.add(new JobData("Miner", 38, 15200, 20000, JobType.MINER,
                ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-fill-miner.png")));
        jobs.add(new JobData("Farmer", 35, 9000, 20000, JobType.FARMER,
                ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-fill-farmer.png")));
        jobs.add(new JobData("Lumberjack", 40, 12000, 20000, JobType.LUMBERJACK,
                ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-fill-lumberjack.png")));
        jobs.add(new JobData("Hunter", 33, 7600, 20000, JobType.HUNTER,
                ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-fill-hunter.png")));
        jobs.add(new JobData("Fisher", 36, 10000, 20000, JobType.FISHER,
                ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-fill-fisher.png")));
        return jobs;
    }

    private void renderJobCard(GuiGraphics g, JobData job, int x, int y, int width, int height, boolean hovered, net.minecraft.client.gui.Font font, int mouseX, int mouseY) {
        // Card background (légèrement plus visible si hover)
        int bgColor = hovered ? 0xE01e1e2e : 0xCC1e1e2e;
        g.fill(x + 6, y + 2, x + width - 2, y + height - 2, bgColor);

        // Border si hover
        if (hovered) {
            g.fill(x + 6, y + 2, x + width - 2, y + 3, job.type.getColor());
        }

        // Job icon (cercle)
        g.fill(x + 23 - 9, y + 18 - 9, x + 23 + 9, y + 18 + 9, job.type.getColor());

        // Job name & subtitle
        g.drawString(font, job.name, x + 41, y + 12, 0xFFFFFF, false);
        g.drawString(font, "Level " + job.level + " • " + job.name + " profession", x + 41, y + 25, 0x9ca3af, false);

        // XP bar
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_TRACK_SMALL, x + 172, y + 15, 115, 6);
        int xpWidth = (int) ((float) job.xp / job.maxXP * 115);
        ImageRenderer.renderScaledImage(g, job.progressFill, x + 172, y + 15, xpWidth, 6);

        // Level display
        g.drawString(font, String.valueOf(job.level), x + 302, y + 21, job.type.getColor(), true);

        // Arrow
        g.drawString(font, "›", x + 352, y + 22, hovered ? 0xFFFFFF : 0x6b7280, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        ImageRenderer.renderScaledImage(g, BG_MAIN, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

        // Border
        ImageRenderer.renderScaledImage(g, BORDER_MAIN, guiLeft + 2, guiTop + 2, 396, 266);

        // Header
        ImageRenderer.renderScaledImage(g, PANEL_HEADER, guiLeft + 8, guiTop + 8, 384, 40);
        g.drawString(this.font, "JOBS SYSTEM", guiLeft + 50, guiTop + 20, 0xfbbf24, false);
        g.drawString(this.font, "Progress through specialized professions", guiLeft + 50, guiTop + 32, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, guiLeft + 372, guiTop + 16, 14, 14);

        // Global Job Panel
        ImageRenderer.renderScaledImage(g, PANEL_CARD_MEDIUM, guiLeft + 8, guiTop + 54, 384, 56);

        // Global level badge (cercle avec gradient effet)
        g.fill(guiLeft + 35 - 18, guiTop + 82 - 18, guiLeft + 35 + 18, guiTop + 82 + 18, 0xFFa855f7);
        g.drawCenteredString(this.font, "GLOBAL", guiLeft + 35, guiTop + 74, 0xFFFFFF);
        g.drawCenteredString(this.font, String.valueOf(mainJobLevel), guiLeft + 35, guiTop + 86, 0xFFFFFF);

        // Global job info
        g.drawString(this.font, "Global Job Level", guiLeft + 62, guiTop + 70, 0xFFFFFF, false);
        g.drawString(this.font, "Sum of all specialized job levels", guiLeft + 62, guiTop + 82, 0x9ca3af, false);

        // Unlocked button
        ImageRenderer.renderScaledImage(g, BUTTON_MEDIUM, guiLeft + 275, guiTop + 70, 110, 28);
        g.drawCenteredString(this.font, "Unlocked Features", guiLeft + 330, guiTop + 84, 0xFFFFFF);

        // Jobs list header
        ImageRenderer.renderScaledImage(g, PANEL_TITLE_BAR, guiLeft + 8, guiTop + 116, 384, 16);
        g.drawString(this.font, "SPECIALIZED JOBS", guiLeft + 14, guiTop + 123, 0xfbbf24, false);
        g.drawString(this.font, "Click for details", guiLeft + 378 - font.width("Click for details"), guiTop + 123, 0x9ca3af, false);

        // Render jobs scroll list
        if (jobsList != null) {
            jobsList.render(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Close button
        if (isMouseOver(mouseX, mouseY, guiLeft + 372, guiTop + 16, 14, 14)) {
            this.onClose();
            return true;
        }

        // Unlocked Features button (global job)
        if (isMouseOver(mouseX, mouseY, guiLeft + 275, guiTop + 70, 110, 28)) {
            minecraft.setScreen(new UnlockedFeaturesScreen("Global Job", mainJobLevel, 0, 0, JobType.GLOBAL));
            return true;
        }

        // Jobs list
        if (jobsList != null && jobsList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (jobsList != null && jobsList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (jobsList != null && jobsList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (jobsList != null && jobsList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
