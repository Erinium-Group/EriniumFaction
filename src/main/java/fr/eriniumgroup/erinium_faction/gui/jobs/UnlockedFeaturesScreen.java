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
 * Écran "Unlocked Features" - Liste des features débloquées et verrouillées
 */
public class UnlockedFeaturesScreen extends Screen {

    private static final ResourceLocation PANEL_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-header.png");
    private static final ResourceLocation PANEL_CARD_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-card-medium.png");
    private static final ResourceLocation PANEL_TITLE_BAR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-title-bar.png");
    private static final ResourceLocation PANEL_SCROLL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-scroll-container.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-close.png");
    private static final ResourceLocation BUTTON_BACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-back.png");
    private static final ResourceLocation LEVEL_BADGE_SMALL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/level-badge-small.png");
    private static final ResourceLocation ICON_UNLOCKED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/icon-circle-status-unlocked.png");
    private static final ResourceLocation ICON_LOCKED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/icon-circle-status-locked.png");
    private static final ResourceLocation SCROLLBAR_TRACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/scrollbar-track.png");
    private static final ResourceLocation SCROLLBAR_THUMB = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/scrollbar-thumb.png");

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    private int guiLeft;
    private int guiTop;

    private final String jobName;
    private final int level;
    private final int xp;
    private final int maxXP;
    private final JobType jobType;

    private ScrollList<Feature> featuresList;

    public static class Feature {
        public String name;
        public String desc;
        public int requiredLevel;
        public boolean unlocked;

        public Feature(String name, String desc, int requiredLevel, boolean unlocked) {
            this.name = name;
            this.desc = desc;
            this.requiredLevel = requiredLevel;
            this.unlocked = unlocked;
        }
    }

    public UnlockedFeaturesScreen(String jobName, int level, int xp, int maxXP, JobType jobType) {
        super(Component.literal(jobName + " - Unlocked Features"));
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

        // Initialiser la scroll list
        featuresList = new ScrollList<>(this.font, this::renderFeature, 30);
        featuresList.setBounds(guiLeft + 8, guiTop + 110, 384, 148);

        // Ajouter les features d'exemple
        List<Feature> features = createExampleFeatures();
        featuresList.setItems(features);
    }

    private List<Feature> createExampleFeatures() {
        List<Feature> features = new ArrayList<>();

        // Récupérer les features depuis l'enum pour ce job
        JobFeature[] jobFeatures = JobFeature.getFeaturesForJob(jobType);
        for (JobFeature jobFeature : jobFeatures) {
            features.add(new Feature(
                jobFeature.getName(),
                jobFeature.getFullDescription(level),
                jobFeature.getRequiredLevel(),
                jobFeature.isUnlocked(level)
            ));
        }

        return features;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        ImageRenderer.renderScaledImage(g, jobType.getBackground(), guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);
        ImageRenderer.renderScaledImage(g, jobType.getBorder(), guiLeft + 2, guiTop + 2, 396, 266);

        // Header
        ImageRenderer.renderScaledImage(g, PANEL_HEADER, guiLeft + 8, guiTop + 8, 384, 40);
        ImageRenderer.renderScaledImage(g, BUTTON_BACK, guiLeft + 14, guiTop + 16, 24, 24);
        g.drawCenteredString(this.font, "‹", guiLeft + 26, guiTop + 26, jobType.getColor());

        g.fill(guiLeft + 58 - 14, guiTop + 28 - 14, guiLeft + 58 + 14, guiTop + 28 + 14, jobType.getColor());
        g.drawString(this.font, jobName.toUpperCase() + " - Unlocked Features", guiLeft + 80, guiTop + 20, jobType.getColor(), false);
        g.drawString(this.font, "Perks and abilities you've earned", guiLeft + 80, guiTop + 32, 0x9ca3af, false);
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, guiLeft + 372, guiTop + 16, 14, 14);

        // Level bar
        ImageRenderer.renderScaledImage(g, PANEL_CARD_MEDIUM, guiLeft + 8, guiTop + 54, 384, 30);
        ImageRenderer.renderScaledImage(g, LEVEL_BADGE_SMALL, guiLeft + 18, guiTop + 60, 28, 18);
        g.drawCenteredString(this.font, String.valueOf(level), guiLeft + 32, guiTop + 68, 0xFFFFFF);
        g.drawString(this.font, "Level " + level + " " + jobName, guiLeft + 54, guiTop + 67, 0xFFFFFF, false);

        // Stats
        int unlockedCount = (int) featuresList.getItems().stream().filter(f -> f.unlocked).count();
        int lockedCount = featuresList.getItems().size() - unlockedCount;
        g.drawString(this.font, unlockedCount + " features unlocked • " + lockedCount + " locked",
                     guiLeft + 230, guiTop + 67, 0x10b981, false);

        // Title
        ImageRenderer.renderScaledImage(g, PANEL_TITLE_BAR, guiLeft + 8, guiTop + 90, 384, 16);
        g.drawString(this.font, "YOUR UNLOCKED FEATURES", guiLeft + 14, guiTop + 97, 0xfbbf24, false);

        // Render features scroll list
        if (featuresList != null) {
            featuresList.render(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderFeature(GuiGraphics g, Feature feature, int x, int y, int width, int height, boolean hovered, net.minecraft.client.gui.Font font, int mouseX, int mouseY) {
        float alpha = feature.unlocked ? 1.0f : 0.6f;
        int borderColor = feature.unlocked ? 0x10b981 : 0xef4444;

        // Card
        int bgColor = hovered ? 0xE01e1e2e : 0xFF1e1e2e;
        g.fill(x + 6, y + 2, x + width - 8, y + height - 2, bgColor);

        // Status icon
        ResourceLocation icon = feature.unlocked ? ICON_UNLOCKED : ICON_LOCKED;
        ImageRenderer.renderScaledImageWithAlpha(g, icon, x + 20 - 7, y + 14 - 7, 14, 14, alpha);

        // Name & desc
        int textColor = feature.unlocked ? 0x10b981 : 0x9ca3af;
        g.drawString(font, feature.name, x + 34, y + 8, textColor, false);

        int descColor = feature.unlocked ? 0x9ca3af : 0xef4444;
        g.drawString(font, feature.desc, x + 34, y + 18, descColor, false);

        // Level badge
        int badgeColor = feature.unlocked ? 0x10b981 : 0xef4444;
        g.fill(x + 322, y + 8, x + 357, y + 21, badgeColor);
        g.drawCenteredString(font, "LVL " + feature.requiredLevel, x + 339, y + 14, 0xFFFFFF);
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
            // Si c'est le Global Job, retourner au menu principal
            if (jobName.equals("Global Job")) {
                minecraft.setScreen(new JobsScreen());
            } else {
                minecraft.setScreen(new JobDetailScreen(jobName, level, xp, maxXP, jobType));
            }
            return true;
        }

        // Features list
        if (featuresList != null && featuresList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (featuresList != null && featuresList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (featuresList != null && featuresList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (featuresList != null && featuresList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
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
