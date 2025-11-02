package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelAttachments;
import fr.eriniumgroup.erinium_faction.gui.menus.PlayerStatsMenu;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelData;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelManager;
import fr.eriniumgroup.erinium_faction.common.network.packets.PlayerStatsPacketHandler;
import fr.eriniumgroup.erinium_faction.gui.widgets.StatAttributeButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

/**
 * Écran client pour l'interface de distribution des points
 */
public class PlayerStatsScreen extends AbstractContainerScreen<PlayerStatsMenu> {

    private PlayerLevelData cachedData;

    // Textures principales
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/player-stats/playerstats-background.png");
    private static final ResourceLocation XP_BAR_EMPTY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/player-stats/xp-bar-empty.png");
    private static final ResourceLocation XP_BAR_FILLED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/player-stats/xp-bar-filled.png");
    private static final ResourceLocation ATTRIBUTE_BOX_RED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/player-stats/attribute-box-red.png");
    private static final ResourceLocation ATTRIBUTE_BOX_CYAN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/player-stats/attribute-box-cyan.png");
    private static final ResourceLocation ATTRIBUTE_BOX_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/player-stats/attribute-box-green.png");

    // Textures des icônes
    private static final ResourceLocation ICON_HEALTH = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/screens/player_stats_health.png");
    private static final ResourceLocation ICON_ARMOR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/screens/player_stats_armor.png");
    private static final ResourceLocation ICON_SPEED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/screens/player_stats_speed.png");
    private static final ResourceLocation ICON_INTELLIGENCE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/screens/player_stats_intelligence.png");
    private static final ResourceLocation ICON_STRENGTH = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/screens/player_stats_strength.png");
    private static final ResourceLocation ICON_LUCK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/screens/player_stats_luck.png");

    public PlayerStatsScreen(PlayerStatsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 400;
        this.imageHeight = 270;
    }

    @Override
    protected void init() {
        super.init();

        // Récupérer les données depuis le joueur client
        if (minecraft != null && minecraft.player != null) {
            cachedData = minecraft.player.getData(PlayerLevelAttachments.PLAYER_LEVEL_DATA);
        }

        // Template dimensions selon le SVG
        int buttonWidth = 142;
        int buttonHeight = 26;
        int rowSpacing = 32; // Espacement entre les rangées

        // Rangée 1 (y=152 du template)
        int row1Y = this.topPos + 152;
        // Bouton Vie avec icône et tooltip
        addRenderableWidget(new StatAttributeButton(
            this.leftPos + 24, row1Y, buttonWidth, buttonHeight,
            ICON_HEALTH,
            Component.translatable("player_level.tooltip.health"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.HEALTH)));

        // Bouton Intelligence avec icône et tooltip
        addRenderableWidget(new StatAttributeButton(
            this.leftPos + 204, row1Y, buttonWidth, buttonHeight,
            ICON_INTELLIGENCE,
            Component.translatable("player_level.tooltip.intelligence"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.INTELLIGENCE)));

        // Rangée 2 (y=184 du template)
        int row2Y = this.topPos + 184;
        // Bouton Armure avec icône et tooltip
        addRenderableWidget(new StatAttributeButton(
            this.leftPos + 24, row2Y, buttonWidth, buttonHeight,
            ICON_ARMOR,
            Component.translatable("player_level.tooltip.armor"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.ARMOR)));

        // Bouton Force avec icône et tooltip
        addRenderableWidget(new StatAttributeButton(
            this.leftPos + 204, row2Y, buttonWidth, buttonHeight,
            ICON_STRENGTH,
            Component.translatable("player_level.tooltip.strength"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.STRENGTH)));

        // Rangée 3 (y=216 du template)
        int row3Y = this.topPos + 216;
        // Bouton Vitesse avec icône et tooltip
        addRenderableWidget(new StatAttributeButton(
            this.leftPos + 24, row3Y, buttonWidth, buttonHeight,
            ICON_SPEED,
            Component.translatable("player_level.tooltip.speed"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.SPEED)));

        // Bouton Chance avec icône et tooltip
        addRenderableWidget(new StatAttributeButton(
            this.leftPos + 204, row3Y, buttonWidth, buttonHeight,
            ICON_LUCK,
            Component.translatable("player_level.tooltip.luck"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.LUCK)));
    }

    private void distributePoint(PlayerLevelManager.AttributeType type) {
        // Envoyer un paquet au serveur pour distribuer le point
        PlayerStatsPacketHandler.sendDistributePoint(type);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Utiliser l'image du background principal
        ImageRenderer.renderScaledImage(guiGraphics, BACKGROUND, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }


    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Ne rien faire ici pour éviter d'afficher les labels par défaut (titre + inventaire)
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Rafraîchir les données à chaque frame pour capturer les mises à jour du serveur
        if (minecraft != null && minecraft.player != null) {
            cachedData = minecraft.player.getData(PlayerLevelAttachments.PLAYER_LEVEL_DATA);
        }

        if (cachedData == null) return;

        // Titre dans le header (y=37, couleur #fbbf24 = jaune doré)
        Component title = Component.translatable("player_level.title").withStyle(style -> style.withColor(0xfbbf24).withBold(true));
        guiGraphics.drawCenteredString(this.font, title, this.leftPos + 200, this.topPos + 28, 0xfbbf24);

        // Informations de niveau (y=66)
        Component levelText = Component.translatable("player_level.level").append(": ").withStyle(style -> style.withColor(0xfbbf24).withBold(true));
        levelText = levelText.copy().append(Component.literal(String.valueOf(cachedData.getLevel())).withStyle(style -> style.withColor(0xfbbf24).withBold(true)));
        guiGraphics.drawString(this.font, levelText, this.leftPos + 24, this.topPos + 58, 0xfbbf24);

        // Note: XP system has been removed - only level-based progression now

        // Points disponibles (y=116)
        Component pointsText = Component.translatable("player_level.points_available").append(": ").withStyle(style -> style.withColor(0xfbbf24).withBold(true));
        pointsText = pointsText.copy().append(Component.literal(String.valueOf(cachedData.getAvailablePoints())).withStyle(style -> style.withColor(0xfbbf24).withBold(true)));
        guiGraphics.drawString(this.font, pointsText, this.leftPos + 24, this.topPos + 108, 0xfbbf24);

        // Note: Ligne de séparation maintenant dans l'image de background

        // Titre de section "Investir vos points" (y=144)
        Component sectionTitle = Component.translatable("player_level.section.invest").withStyle(style -> style.withColor(0xa0a0c0));
        guiGraphics.drawString(this.font, sectionTitle, this.leftPos + 24, this.topPos + 136, 0xa0a0c0);

        // Afficher les valeurs des attributs dans les petites zones (24x26)
        renderAttributeValues(guiGraphics, cachedData);
    }

    private void renderAttributeValues(GuiGraphics guiGraphics, PlayerLevelData data) {
        // Rangée 1 (y=152)
        int row1Y = this.topPos + 152;

        // Zone Vie (x=172, rouge)
        renderAttributeBox(guiGraphics, this.leftPos + 172, row1Y, 24, 26, String.valueOf(data.getHealthPoints()), 0xef4444, ATTRIBUTE_BOX_RED);

        // Zone Intelligence (x=352, rouge)
        renderAttributeBox(guiGraphics, this.leftPos + 352, row1Y, 24, 26, String.valueOf(data.getIntelligencePoints()), 0xef4444, ATTRIBUTE_BOX_RED);

        // Rangée 2 (y=184)
        int row2Y = this.topPos + 184;

        // Zone Armure (x=172, rouge)
        renderAttributeBox(guiGraphics, this.leftPos + 172, row2Y, 24, 26, String.valueOf(data.getArmorPoints()), 0xef4444, ATTRIBUTE_BOX_RED);

        // Zone Force (x=352, rouge)
        renderAttributeBox(guiGraphics, this.leftPos + 352, row2Y, 24, 26, String.valueOf(data.getStrengthPoints()), 0xef4444, ATTRIBUTE_BOX_RED);

        // Rangée 3 (y=216)
        int row3Y = this.topPos + 216;

        // Zone Vitesse (x=172, cyan)
        renderAttributeBox(guiGraphics, this.leftPos + 172, row3Y, 24, 26, String.valueOf(data.getSpeedPoints()), 0x00d2ff, ATTRIBUTE_BOX_CYAN);

        // Zone Chance (x=352, verte)
        renderAttributeBox(guiGraphics, this.leftPos + 352, row3Y, 24, 26, String.valueOf(data.getLuckPoints()), 0x10b981, ATTRIBUTE_BOX_GREEN);
    }

    private void renderAttributeBox(GuiGraphics guiGraphics, int x, int y, int width, int height, String value, int color, ResourceLocation boxTexture) {
        // Utiliser l'image de la box
        ImageRenderer.renderScaledImage(guiGraphics, boxTexture, x, y, width, height);

        // Texte centré
        int textWidth = this.font.width(value);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - this.font.lineHeight) / 2 + 2;
        guiGraphics.drawString(this.font, value, textX, textY, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
