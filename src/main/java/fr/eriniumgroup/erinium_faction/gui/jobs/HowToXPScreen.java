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

    private static final ResourceLocation BG_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/bg-full-main.png");
    private static final ResourceLocation BORDER_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/border-main.png");
    private static final ResourceLocation PANEL_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-header.png");
    private static final ResourceLocation PANEL_CARD_MEDIUM = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-card-medium.png");
    private static final ResourceLocation PANEL_TITLE_BAR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/panel-title-bar.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-close.png");
    private static final ResourceLocation BUTTON_BACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-back.png");
    private static final ResourceLocation LEVEL_BADGE_SMALL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/level-badge-small.png");
    private static final ResourceLocation PROGRESSBAR_TRACK_SMALL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-track-small.png");
    private static final ResourceLocation PROGRESSBAR_FILL_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/progressbar-fill-main.png");

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    private int guiLeft;
    private int guiTop;

    private final int level;
    private final int xp;
    private final int maxXP;

    private ScrollList<XPActionEntry> xpActionsList;

    public static class XPActionEntry {
        public String name;
        public String desc; // Texte court pour la scroll list (niveau requis en rouge si pas disponible)
        public String fullDescription; // Vraie description pour le tooltip
        public int xpAmount;
        public int minLevel;
        public int maxLevel;
        public int color;
        public net.minecraft.world.item.ItemStack displayItem;

        public XPActionEntry(String name, String desc, String fullDescription, int xpAmount, int minLevel, int maxLevel, int color, net.minecraft.world.item.ItemStack displayItem) {
            this.name = name;
            this.desc = desc;
            this.fullDescription = fullDescription;
            this.xpAmount = xpAmount;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.color = color;
            this.displayItem = displayItem;
        }

        public boolean isAvailable(int playerLevel) {
            boolean aboveMin = minLevel == -1 || playerLevel >= minLevel;
            boolean belowMax = maxLevel == -1 || playerLevel <= maxLevel;
            return aboveMin && belowMax;
        }

        public String getDisplayDescription(int playerLevel) {
            // Si pas disponible, afficher le niveau requis en rouge
            if (!isAvailable(playerLevel)) {
                if (minLevel != -1 && playerLevel < minLevel) {
                    int levelsToGo = minLevel - playerLevel;
                    return "Requires Level " + minLevel + " • " + levelsToGo + " level" + (levelsToGo > 1 ? "s" : "") + " to go";
                } else if (maxLevel != -1 && playerLevel > maxLevel) {
                    return "No longer available (max level " + maxLevel + ")";
                }
            }

            // Si disponible, montrer juste le range de niveau de façon courte
            if (minLevel == -1 && maxLevel == -1) {
                return "All levels";
            } else if (minLevel == -1) {
                return "Up to level " + maxLevel;
            } else if (maxLevel == -1) {
                return "Level " + minLevel + "+";
            } else {
                return "Level " + minLevel + "-" + maxLevel;
            }
        }
    }

    public HowToXPScreen(int level, int xp, int maxXP) {
        super(Component.literal("How to gain XP"));
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
        xpActionsList = new ScrollList<>(this.font, this::renderXPAction, 30);
        xpActionsList.setBounds(guiLeft + 8, guiTop + 110, 384, 148);

        // Ajouter les actions depuis l'enum
        List<XPActionEntry> actions = createActions();
        xpActionsList.setItems(actions);
    }

    private List<XPActionEntry> createActions() {
        List<XPActionEntry> actions = new ArrayList<>();

        for (JobXPAction action : JobXPAction.values()) {
            // Convertir Block/EntityType/Item en ItemStack pour l'affichage
            net.minecraft.world.item.ItemStack displayItem = getDisplayItemStack(action);

            actions.add(new XPActionEntry(
                action.getName(),
                "", // sera calculé dynamiquement avec getDisplayDescription()
                action.getDescription(), // Vraie description pour le tooltip
                action.getXpAmount(),
                action.getMinLevel(),
                action.getMaxLevel(),
                action.getColor(),
                displayItem
            ));
        }

        return actions;
    }

    /**
     * Convertit la cible d'une action en ItemStack pour l'affichage
     */
    private net.minecraft.world.item.ItemStack getDisplayItemStack(JobXPAction action) {
        Object target = action.getTarget();

        // Si c'est un Item, on l'utilise directement
        if (target instanceof net.minecraft.world.item.Item item) {
            return new net.minecraft.world.item.ItemStack(item);
        }

        // Si c'est un Block, on utilise son item
        if (target instanceof net.minecraft.world.level.block.Block block) {
            return new net.minecraft.world.item.ItemStack(block.asItem());
        }

        // Si c'est un EntityType, on utilise son spawn egg (ou un item par défaut)
        if (target instanceof net.minecraft.world.entity.EntityType<?> entityType) {
            // Essayer de trouver le spawn egg correspondant
            net.minecraft.world.item.Item spawnEgg = net.minecraft.world.item.Items.EGG; // Fallback

            // Les spawn eggs sont nommés comme {entity_type}_spawn_egg
            String entityName = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath();
            net.minecraft.resources.ResourceLocation eggId = net.minecraft.resources.ResourceLocation.parse("minecraft:" + entityName + "_spawn_egg");

            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(eggId)) {
                spawnEgg = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(eggId);
            }

            return new net.minecraft.world.item.ItemStack(spawnEgg);
        }

        // Fallback
        return net.minecraft.world.item.ItemStack.EMPTY;
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

        g.drawString(this.font, "HOW TO GAIN XP", guiLeft + 50, guiTop + 20, 0xfbbf24, false);
        g.drawString(this.font, "Actions that give experience", guiLeft + 50, guiTop + 32, 0x9ca3af, false);
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, guiLeft + 372, guiTop + 16, 14, 14);

        // Level bar
        ImageRenderer.renderScaledImage(g, PANEL_CARD_MEDIUM, guiLeft + 8, guiTop + 54, 384, 30);
        ImageRenderer.renderScaledImage(g, LEVEL_BADGE_SMALL, guiLeft + 18, guiTop + 60, 28, 18);
        g.drawCenteredString(this.font, String.valueOf(level), guiLeft + 32, guiTop + 68, 0xFFFFFF);
        g.drawString(this.font, "Level " + level, guiLeft + 54, guiTop + 67, 0xFFFFFF, false);

        // Mini XP bar
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_TRACK_SMALL, guiLeft + 160, guiTop + 67, 115, 6);
        int xpWidth = maxXP > 0 ? (int) ((float) xp / maxXP * 115) : 0;
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_FILL_MAIN, guiLeft + 160, guiTop + 67, xpWidth, 6);

        // Title
        ImageRenderer.renderScaledImage(g, PANEL_TITLE_BAR, guiLeft + 8, guiTop + 90, 384, 16);
        g.drawString(this.font, "ACTIONS THAT GIVE XP", guiLeft + 14, guiTop + 97, 0xfbbf24, false);

        // Render XP actions scroll list
        if (xpActionsList != null) {
            xpActionsList.render(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderXPAction(GuiGraphics g, XPActionEntry action, int x, int y, int width, int height, boolean hovered, net.minecraft.client.gui.Font font, int mouseX, int mouseY) {
        boolean available = action.isAvailable(level);
        float alpha = available ? 1.0f : 0.4f;

        // Card
        int bgColor = available ? (hovered ? 0xE01e1e2e : 0xFF1e1e2e) : 0x661e1e2e;
        g.fill(x + 6, y + 2, x + width - 8, y + height - 2, bgColor);

        // Item icon
        if (action.displayItem != null && !action.displayItem.isEmpty()) {
            g.renderItem(action.displayItem, x + 12, y + 7);
        }

        // Name & desc
        g.drawString(font, action.name, x + 34, y + 8, available ? 0xFFFFFF : 0x999ca3af, false);
        String displayDesc = action.getDisplayDescription(level);
        g.drawString(font, displayDesc, x + 34, y + 18, available ? 0x9ca3af : 0xef4444, false);

        // Level range
        String levelRange;
        if (action.minLevel == -1 && action.maxLevel == -1) {
            levelRange = "All Levels";
        } else if (action.minLevel == -1) {
            levelRange = "Lvl ≤" + action.maxLevel;
        } else if (action.maxLevel == -1) {
            levelRange = "Lvl " + action.minLevel + "+";
        } else {
            levelRange = "Lvl " + action.minLevel + "-" + action.maxLevel;
        }

        int badgeColor = available ? (action.color == 0xef4444 ? 0xef4444 : 0x10b981) : 0x6b7280;
        g.fill(x + 210, y + 8, x + 260, y + 21, badgeColor);
        g.drawCenteredString(font, levelRange, x + 235, y + 14, 0xFFFFFF);

        // XP amount (aligné à droite, avec marge)
        String xpText = "+" + action.xpAmount + " XP";
        int xpWidth = font.width(xpText);
        g.drawString(font, xpText, x + width - xpWidth - 20, y + 13,
                     available ? action.color : 0x999ca3af, true);

        // Tooltip si hover
        if (hovered && available) {
            List<Component> tooltipComponents = new ArrayList<>();
            tooltipComponents.add(Component.literal(action.name).withStyle(style -> style.withColor(action.color).withBold(true)));
            tooltipComponents.add(Component.literal(action.fullDescription).withStyle(style -> style.withColor(0x9ca3af)));
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal("XP Reward: +" + action.xpAmount).withStyle(style -> style.withColor(0x10b981)));

            // Level range
            if (action.minLevel != -1 || action.maxLevel != -1) {
                String levelInfo = "Available: ";
                if (action.minLevel == -1) {
                    levelInfo += "Level ≤" + action.maxLevel;
                } else if (action.maxLevel == -1) {
                    levelInfo += "Level " + action.minLevel + "+";
                } else {
                    levelInfo += "Level " + action.minLevel + "-" + action.maxLevel;
                }
                tooltipComponents.add(Component.literal(levelInfo).withStyle(style -> style.withColor(0xfbbf24)));
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
