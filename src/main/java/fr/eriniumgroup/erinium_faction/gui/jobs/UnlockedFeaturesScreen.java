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

    private static final ResourceLocation BG_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/bg-full-main.png");
    private static final ResourceLocation BORDER_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/border-main.png");
    private static final ResourceLocation PANEL_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-header.png");
    private static final ResourceLocation PANEL_CARD_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-card-medium.png");
    private static final ResourceLocation PANEL_TITLE_BAR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-title-bar.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-close.png");
    private static final ResourceLocation BUTTON_BACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-back.png");
    private static final ResourceLocation LEVEL_BADGE_SMALL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/level-badge-small.png");
    private static final ResourceLocation ICON_UNLOCKED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/icon-circle-status-unlocked.png");
    private static final ResourceLocation ICON_LOCKED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/icon-circle-status-locked.png");

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    private int guiLeft;
    private int guiTop;

    private final int level;
    private final int xp;
    private final int maxXP;

    private ScrollList<FeatureEntry> featuresList;

    public static class FeatureEntry {
        public String name;
        public String desc; // Pour affichage dans la scroll list (texte rouge si locked)
        public String fullDescription; // Vraie description pour le tooltip
        public int requiredLevel;
        public boolean unlocked;
        public net.minecraft.world.item.ItemStack displayItem;

        public FeatureEntry(String name, String desc, String fullDescription, int requiredLevel, boolean unlocked, net.minecraft.world.item.ItemStack displayItem) {
            this.name = name;
            this.desc = desc;
            this.fullDescription = fullDescription;
            this.requiredLevel = requiredLevel;
            this.unlocked = unlocked;
            this.displayItem = displayItem;
        }
    }

    public UnlockedFeaturesScreen(int level, int xp, int maxXP) {
        super(Component.literal("Unlocked Features"));
        this.level = level;
        this.xp = xp;
        this.maxXP = maxXP;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        // Initialiser la scroll list
        featuresList = new ScrollList<>(this.font, this::renderFeature, 30);
        featuresList.setBounds(guiLeft + 8, guiTop + 110, 384, 148);

        // Ajouter les features depuis l'enum
        List<FeatureEntry> features = createFeatures();
        featuresList.setItems(features);
    }

    private List<FeatureEntry> createFeatures() {
        List<FeatureEntry> features = new ArrayList<>();

        for (JobFeature feature : JobFeature.values()) {
            // Pour la scroll list: utiliser getFullDescription (affiche "Requires Level X" en rouge si locked)
            // La vraie description sera affichée dans le tooltip
            features.add(new FeatureEntry(
                feature.getName(),
                feature.getFullDescription(level), // Texte pour la scroll list (rouge si locked)
                feature.getDescription(), // Vraie description pour le tooltip
                feature.getRequiredLevel(),
                feature.isUnlocked(level),
                new net.minecraft.world.item.ItemStack(feature.getDisplayItem())
            ));
        }

        return features;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        ImageRenderer.renderScaledImage(g, BG_MAIN, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);
        ImageRenderer.renderScaledImage(g, BORDER_MAIN, guiLeft + 2, guiTop + 2, 396, 266);

        // Header
        ImageRenderer.renderScaledImage(g, PANEL_HEADER, guiLeft + 8, guiTop + 8, 384, 40);
        ImageRenderer.renderScaledImage(g, BUTTON_BACK, guiLeft + 14, guiTop + 16, 24, 24);
        g.drawCenteredString(this.font, "‹", guiLeft + 26, guiTop + 26, 0xfbbf24);

        g.drawString(this.font, "UNLOCKED FEATURES", guiLeft + 50, guiTop + 20, 0xfbbf24, false);
        g.drawString(this.font, "Perks and abilities you've earned", guiLeft + 50, guiTop + 32, 0x9ca3af, false);
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, guiLeft + 372, guiTop + 16, 14, 14);

        // Level bar
        ImageRenderer.renderScaledImage(g, PANEL_CARD_MEDIUM, guiLeft + 8, guiTop + 54, 384, 30);
        ImageRenderer.renderScaledImage(g, LEVEL_BADGE_SMALL, guiLeft + 18, guiTop + 60, 28, 18);
        g.drawCenteredString(this.font, String.valueOf(level), guiLeft + 32, guiTop + 68, 0xFFFFFF);
        g.drawString(this.font, "Level " + level, guiLeft + 54, guiTop + 67, 0xFFFFFF, false);

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

    private void renderFeature(GuiGraphics g, FeatureEntry feature, int x, int y, int width, int height, boolean hovered, net.minecraft.client.gui.Font font, int mouseX, int mouseY) {
        float alpha = feature.unlocked ? 1.0f : 0.6f;

        // Card
        int bgColor = hovered ? 0xE01e1e2e : 0xFF1e1e2e;
        g.fill(x + 6, y + 2, x + width - 8, y + height - 2, bgColor);

        // Item icon
        if (feature.displayItem != null && !feature.displayItem.isEmpty()) {
            g.renderItem(feature.displayItem, x + 12, y + 7);
        }

        // Name & desc
        int textColor = feature.unlocked ? 0x10b981 : 0x9ca3af;
        g.drawString(font, feature.name, x + 34, y + 8, textColor, false);

        int descColor = feature.unlocked ? 0x9ca3af : 0xef4444;
        g.drawString(font, feature.desc, x + 34, y + 18, descColor, false);

        // Level badge
        int badgeColor = feature.unlocked ? 0x10b981 : 0xef4444;
        g.fill(x + 322, y + 8, x + 357, y + 21, badgeColor);
        g.drawCenteredString(font, "LVL " + feature.requiredLevel, x + 339, y + 14, 0xFFFFFF);

        // Tooltip si hover
        if (hovered) {
            List<Component> tooltipComponents = new ArrayList<>();
            tooltipComponents.add(Component.literal(feature.name).withStyle(style -> style.withColor(feature.unlocked ? 0x10b981 : 0xef4444).withBold(true)));
            tooltipComponents.add(Component.literal(feature.fullDescription).withStyle(style -> style.withColor(0x9ca3af)));
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal("Required Level: " + feature.requiredLevel).withStyle(style -> style.withColor(0xfbbf24)));

            if (!feature.unlocked) {
                int levelsToGo = feature.requiredLevel - level;
                tooltipComponents.add(Component.literal(levelsToGo + " level" + (levelsToGo > 1 ? "s" : "") + " to unlock").withStyle(style -> style.withColor(0xef4444)));
            }

            // Convertir en FormattedCharSequence
            List<net.minecraft.util.FormattedCharSequence> tooltip = tooltipComponents.stream()
                .map(c -> c.getVisualOrderText())
                .toList();

            g.renderTooltip(font, tooltip, (int)mouseX, (int)mouseY);
        }
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
