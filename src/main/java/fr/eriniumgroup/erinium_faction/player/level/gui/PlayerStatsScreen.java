package fr.eriniumgroup.erinium_faction.player.level.gui;

import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelData;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
            cachedData = minecraft.player.getData(fr.eriniumgroup.erinium_faction.player.level.PlayerLevelAttachments.PLAYER_LEVEL_DATA);
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

        // Bouton Réinitialiser (centré en bas)
        addRenderableWidget(Button.builder(Component.translatable("player_level.button.reset")
            .withStyle(style -> style.withColor(fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelClientData.hasResetToken() ? 0xEF4444 : 0x555555)),
            btn -> resetAttributes())
            .bounds(this.leftPos + 150, this.topPos + 248, 100, 22)
            .build())
            .active = fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelClientData.hasResetToken();
    }

    private void distributePoint(PlayerLevelManager.AttributeType type) {
        // Envoyer un paquet au serveur pour distribuer le point
        PlayerStatsPacketHandler.sendDistributePoint(type);
    }

    private void resetAttributes() {
        // Envoyer un paquet au serveur pour réinitialiser
        PlayerStatsPacketHandler.sendResetAttributes();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Fond dégradé cyber-astral (gradient #0f0c29 -> #1a1347 -> #24243e)
        fillGradientVertical(guiGraphics, this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight / 2, 0xFF0f0c29, 0xFF1a1347);
        fillGradientVertical(guiGraphics, this.leftPos, this.topPos + this.imageHeight / 2, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF1a1347, 0xFF24243e);

        // Pattern d'étoiles (petits points lumineux)
        renderStarsPattern(guiGraphics);

        // Bordure extérieure lumineuse (gradient cyan-bleu)
        int borderColor1 = 0xFF00d2ff; // Cyan
        int borderColor2 = 0xFF3a47d5; // Bleu

        // Bordure avec effet glow
        drawGlowBorder(guiGraphics, this.leftPos + 4, this.topPos + 4, this.imageWidth - 8, this.imageHeight - 8, 8, borderColor1);

        // Main panel (gradient #1e1e2e -> #2a2a3e)
        fillGradientVertical(guiGraphics, this.leftPos + 8, this.topPos + 8, this.leftPos + this.imageWidth - 8, this.topPos + this.imageHeight - 8, 0xF01e1e2e, 0xF02a2a3e);

        // Bordure du main panel
        drawRoundedRect(guiGraphics, this.leftPos + 8, this.topPos + 8, this.imageWidth - 16, this.imageHeight - 16, 6, 0x4D667eea);

        // Header background
        guiGraphics.fill(this.leftPos + 16, this.topPos + 16, this.leftPos + 16 + 368, this.topPos + 16 + 32, 0xCC1a1a2e);

        // Header border (gradient accent)
        drawRoundedRect(guiGraphics, this.leftPos + 16, this.topPos + 16, 368, 32, 4, borderColor1);

        // Ligne accent en haut du header
        guiGraphics.fill(this.leftPos + 20, this.topPos + 16, this.leftPos + 50, this.topPos + 18, borderColor1);
    }

    private void fillGradientVertical(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        guiGraphics.fillGradient(x1, y1, x2, y2, colorFrom, colorTo);
    }

    private void renderStarsPattern(GuiGraphics guiGraphics) {
        // Dessiner des petites étoiles aléatoires
        int[][] stars = {
            {50, 30, 1, 0x9900d2ff},
            {350, 50, 1, 0xAAa855f7},
            {100, 200, 1, 0x88ffffff},
            {300, 180, 1, 0x9900d2ff},
            {150, 100, 1, 0xAAa855f7},
            {250, 150, 1, 0x88ffffff},
            {80, 240, 1, 0x9900d2ff}
        };

        for (int[] star : stars) {
            int x = this.leftPos + star[0];
            int y = this.topPos + star[1];
            int size = star[2];
            int color = star[3];
            guiGraphics.fill(x, y, x + size, y + size, color);
        }
    }

    private void drawGlowBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int radius, int color) {
        // Haut
        guiGraphics.fill(x + radius, y, x + width - radius, y + 2, color);
        // Bas
        guiGraphics.fill(x + radius, y + height - 2, x + width - radius, y + height, color);
        // Gauche
        guiGraphics.fill(x, y + radius, x + 2, y + height - radius, color);
        // Droite
        guiGraphics.fill(x + width - 2, y + radius, x + width, y + height - radius, color);
    }

    private void drawRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int radius, int color) {
        // Approximation simple d'un rectangle arrondi (bordure)
        // Haut
        guiGraphics.fill(x + radius, y, x + width - radius, y + 1, color);
        // Bas
        guiGraphics.fill(x + radius, y + height - 1, x + width - radius, y + height, color);
        // Gauche
        guiGraphics.fill(x, y + radius, x + 1, y + height - radius, color);
        // Droite
        guiGraphics.fill(x + width - 1, y + radius, x + width, y + height - radius, color);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Ne rien faire ici pour éviter d'afficher les labels par défaut (titre + inventaire)
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (cachedData == null) return;

        // Titre dans le header (y=37, couleur #fbbf24 = jaune doré)
        Component title = Component.translatable("player_level.title").withStyle(style -> style.withColor(0xfbbf24).withBold(true));
        guiGraphics.drawCenteredString(this.font, title, this.leftPos + 200, this.topPos + 28, 0xfbbf24);

        // Informations de niveau (y=66)
        Component levelText = Component.translatable("player_level.level").append(": ").withStyle(style -> style.withColor(0xfbbf24).withBold(true));
        levelText = levelText.copy().append(Component.literal(String.valueOf(cachedData.getLevel())).withStyle(style -> style.withColor(0xfbbf24).withBold(true)));
        guiGraphics.drawString(this.font, levelText, this.leftPos + 24, this.topPos + 58, 0xfbbf24);

        // XP (y=76) - ajusté pour éviter la barre
        Component xpLabel = Component.translatable("player_level.xp").append(": ").withStyle(style -> style.withColor(0xa0a0c0));
        guiGraphics.drawString(this.font, xpLabel, this.leftPos + 24, this.topPos + 68, 0xa0a0c0);

        String xpValue = cachedData.getExperience() + " / " + cachedData.getExperienceToNextLevel();
        guiGraphics.drawString(this.font, xpValue, this.leftPos + 48, this.topPos + 68, 0x10b981);

        // Barre de progression XP (y=90, width=352, height=10)
        int barX = this.leftPos + 24;
        int barY = this.topPos + 82;
        int barWidth = 352;
        int barHeight = 10;

        // Fond de la barre
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF2a2a3e);

        // Progression (gradient vert #10b981 -> #06d6a0)
        float progress = (float) cachedData.getExperience() / cachedData.getExperienceToNextLevel();
        int progressWidth = (int) (barWidth * progress);
        if (progressWidth > 0) {
            guiGraphics.fillGradient(barX, barY, barX + progressWidth, barY + barHeight, 0xFF10b981, 0xFF06d6a0);
        }

        // Bordure de la barre
        drawRoundedRect(guiGraphics, barX, barY, barWidth, barHeight, 5, 0x80667eea);

        // Points disponibles (y=116)
        Component pointsText = Component.translatable("player_level.points_available").append(": ").withStyle(style -> style.withColor(0xfbbf24).withBold(true));
        pointsText = pointsText.copy().append(Component.literal(String.valueOf(cachedData.getAvailablePoints())).withStyle(style -> style.withColor(0xfbbf24).withBold(true)));
        guiGraphics.drawString(this.font, pointsText, this.leftPos + 24, this.topPos + 108, 0xfbbf24);

        // Ligne de séparation (y=126)
        guiGraphics.fill(this.leftPos + 24, this.topPos + 118, this.leftPos + 376, this.topPos + 119, 0x4D667eea);

        // Titre de section "Investir vos points" (y=144)
        Component sectionTitle = Component.translatable("player_level.section.invest").withStyle(style -> style.withColor(0xa0a0c0));
        guiGraphics.drawString(this.font, sectionTitle, this.leftPos + 24, this.topPos + 136, 0xa0a0c0);

        // Afficher les valeurs des attributs dans les petites zones (24x26)
        renderAttributeValues(guiGraphics, cachedData);
    }

    private void renderAttributeValues(GuiGraphics guiGraphics, PlayerLevelData data) {
        // Rangée 1 (y=152)
        int row1Y = this.topPos + 152;

        // Zone Vie (x=172, couleur rouge #ef4444)
        renderAttributeBox(guiGraphics, this.leftPos + 172, row1Y, 24, 26, String.valueOf(data.getHealthPoints()), 0xef4444);

        // Zone Intelligence (x=352, couleur rouge #ef4444)
        renderAttributeBox(guiGraphics, this.leftPos + 352, row1Y, 24, 26, String.valueOf(data.getIntelligencePoints()), 0xef4444);

        // Rangée 2 (y=184)
        int row2Y = this.topPos + 184;

        // Zone Armure (x=172, couleur rouge #ef4444)
        renderAttributeBox(guiGraphics, this.leftPos + 172, row2Y, 24, 26, String.valueOf(data.getArmorPoints()), 0xef4444);

        // Zone Force (x=352, couleur rouge #ef4444)
        renderAttributeBox(guiGraphics, this.leftPos + 352, row2Y, 24, 26, String.valueOf(data.getStrengthPoints()), 0xef4444);

        // Rangée 3 (y=216)
        int row3Y = this.topPos + 216;

        // Zone Vitesse (x=172, couleur cyan #00d2ff)
        renderAttributeBox(guiGraphics, this.leftPos + 172, row3Y, 24, 26, String.valueOf(data.getSpeedPoints()), 0x00d2ff);

        // Zone Chance (x=352, couleur verte #10b981)
        renderAttributeBox(guiGraphics, this.leftPos + 352, row3Y, 24, 26, String.valueOf(data.getLuckPoints()), 0x10b981);
    }

    private void renderAttributeBox(GuiGraphics guiGraphics, int x, int y, int width, int height, String value, int color) {
        // Fond
        guiGraphics.fill(x, y, x + width, y + height, 0xFF1a1a2e);

        // Bordure
        drawRoundedRect(guiGraphics, x, y, width, height, 4, color);

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
