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
 * Écran "How to gain XP" - Liste des actions qui donnent de l'XP avec niveaux min/max
 */
public class HowToXPScreen extends Screen {

    private static final ResourceLocation PANEL_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-header.png");
    private static final ResourceLocation PANEL_CARD_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-card-medium.png");
    private static final ResourceLocation PANEL_TITLE_BAR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-title-bar.png");
    private static final ResourceLocation PANEL_SCROLL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-scroll-container.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-close.png");
    private static final ResourceLocation BUTTON_BACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-back.png");
    private static final ResourceLocation LEVEL_BADGE_SMALL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/level-badge-small.png");
    private static final ResourceLocation PROGRESSBAR_TRACK_SMALL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-track-small.png");
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

    private ScrollList<XPAction> xpActionsList;

    // Données d'exemple
    public static class XPAction {
        public String name;
        public String desc;
        public int xpAmount;
        public int minLevel;
        public int maxLevel;
        public int color;

        public XPAction(String name, String desc, int xpAmount, int minLevel, int maxLevel, int color) {
            this.name = name;
            this.desc = desc;
            this.xpAmount = xpAmount;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.color = color;
        }

        public boolean isAvailable(int playerLevel) {
            return playerLevel >= minLevel && playerLevel <= maxLevel;
        }
    }

    public HowToXPScreen(String jobName, int level, int xp, int maxXP, JobType jobType) {
        super(Component.literal(jobName + " - How to gain XP"));
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
        xpActionsList = new ScrollList<>(this.font, this::renderXPAction, 30);
        xpActionsList.setBounds(guiLeft + 8, guiTop + 110, 384, 148);

        // Ajouter les actions d'exemple
        List<XPAction> actions = createExampleActions();
        xpActionsList.setItems(actions);
    }

    private List<XPAction> createExampleActions() {
        List<XPAction> actions = new ArrayList<>();

        // Récupérer les actions depuis l'enum pour ce job
        JobXPAction[] jobActions = JobXPAction.getActionsForJob(jobType);
        for (JobXPAction jobAction : jobActions) {
            actions.add(new XPAction(
                jobAction.getName(),
                jobAction.getDescription(),
                jobAction.getXpAmount(),
                jobAction.getMinLevel(),
                jobAction.getMaxLevel(),
                jobAction.getColor()
            ));
        }

        return actions;
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
        g.drawString(this.font, jobName.toUpperCase() + " - How to gain XP", guiLeft + 80, guiTop + 20, jobType.getColor(), false);
        g.drawString(this.font, "Actions that give experience", guiLeft + 80, guiTop + 32, 0x9ca3af, false);
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, guiLeft + 372, guiTop + 16, 14, 14);

        // Level bar
        ImageRenderer.renderScaledImage(g, PANEL_CARD_MEDIUM, guiLeft + 8, guiTop + 54, 384, 30);
        ImageRenderer.renderScaledImage(g, LEVEL_BADGE_SMALL, guiLeft + 18, guiTop + 60, 28, 18);
        g.drawCenteredString(this.font, String.valueOf(level), guiLeft + 32, guiTop + 68, 0xFFFFFF);
        g.drawString(this.font, "Level " + level + " " + jobName, guiLeft + 54, guiTop + 67, 0xFFFFFF, false);

        // Mini XP bar
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_TRACK_SMALL, guiLeft + 160, guiTop + 67, 115, 6);
        int xpWidth = (int) ((float) xp / maxXP * 115);
        ImageRenderer.renderScaledImage(g, jobType.getProgressFill(), guiLeft + 160, guiTop + 67, xpWidth, 6);

        // Title
        ImageRenderer.renderScaledImage(g, PANEL_TITLE_BAR, guiLeft + 8, guiTop + 90, 384, 16);
        g.drawString(this.font, "ACTIONS THAT GIVE XP", guiLeft + 14, guiTop + 97, 0xfbbf24, false);

        // Render XP actions scroll list
        if (xpActionsList != null) {
            xpActionsList.render(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderXPAction(GuiGraphics g, XPAction action, int x, int y, int width, int height, boolean hovered, net.minecraft.client.gui.Font font, int mouseX, int mouseY) {
        boolean available = action.isAvailable(level);
        float alpha = available ? 1.0f : 0.4f;

        // Card
        int bgColor = available ? (hovered ? 0xE01e1e2e : 0xFF1e1e2e) : 0x661e1e2e;
        g.fill(x + 6, y + 2, x + width - 8, y + height - 2, bgColor);

        // Icon circle (placeholder - pourrait afficher l'item/block/entity icon)
        g.fill(x + 20 - 8, y + 14 - 8, x + 20 + 8, y + 14 + 8,
               available ? action.color : 0x666b7280);

        // Name & desc
        g.drawString(font, action.name, x + 34, y + 8, available ? 0xFFFFFF : 0x999ca3af, false);
        g.drawString(font, action.desc, x + 34, y + 18, available ? 0x9ca3af : 0x666b7280, false);

        // Level range
        String levelRange = "Lvl " + action.minLevel + "-" + action.maxLevel;
        int badgeColor = available ? (action.color == 0xef4444 ? 0xef4444 : 0x10b981) : 0x6b7280;
        g.fill(x + 210, y + 8, x + 260, y + 21, badgeColor);
        g.drawCenteredString(font, levelRange, x + 235, y + 14, 0xFFFFFF);

        // XP amount (aligné à droite, avec marge)
        String xpText = "+" + action.xpAmount + " XP";
        int xpWidth = font.width(xpText);
        g.drawString(font, xpText, x + width - xpWidth - 20, y + 13,
                     available ? action.color : 0x999ca3af, true);
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
            minecraft.setScreen(new JobDetailScreen(jobName, level, xp, maxXP, jobType));
            return true;
        }

        // XP actions list
        if (xpActionsList != null && xpActionsList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (xpActionsList != null && xpActionsList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (xpActionsList != null && xpActionsList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (xpActionsList != null && xpActionsList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
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
