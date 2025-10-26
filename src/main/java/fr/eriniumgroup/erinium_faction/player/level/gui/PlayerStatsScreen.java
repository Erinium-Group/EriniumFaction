package fr.eriniumgroup.erinium_faction.player.level.gui;

import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelData;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

/**
 * Écran client pour l'interface de distribution des points
 */
public class PlayerStatsScreen extends AbstractContainerScreen<PlayerStatsMenu> {

    private PlayerLevelData cachedData;

    public PlayerStatsScreen(PlayerStatsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();

        // Récupérer les données depuis le joueur client
        if (minecraft != null && minecraft.player != null) {
            cachedData = minecraft.player.getData(fr.eriniumgroup.erinium_faction.player.level.PlayerLevelAttachments.PLAYER_LEVEL_DATA);
        }

        int startX = this.leftPos + 20;
        int startY = this.topPos + 80;
        int buttonWidth = 100;
        int buttonHeight = 20;
        int spacing = 25;

        // Boutons pour chaque attribut (colonne gauche)
        addRenderableWidget(Button.builder(Component.translatable("player_level.button.health"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.HEALTH))
            .bounds(startX, startY, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("player_level.button.armor"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.ARMOR))
            .bounds(startX, startY + spacing, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("player_level.button.speed"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.SPEED))
            .bounds(startX, startY + spacing * 2, buttonWidth, buttonHeight).build());

        // Colonne droite
        addRenderableWidget(Button.builder(Component.translatable("player_level.button.intelligence"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.INTELLIGENCE))
            .bounds(startX + buttonWidth + 20, startY, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("player_level.button.strength"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.STRENGTH))
            .bounds(startX + buttonWidth + 20, startY + spacing, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("player_level.button.luck"),
            btn -> distributePoint(PlayerLevelManager.AttributeType.LUCK))
            .bounds(startX + buttonWidth + 20, startY + spacing * 2, buttonWidth, buttonHeight).build());

        // Bouton réinitialiser
        addRenderableWidget(Button.builder(Component.translatable("player_level.button.reset")
            .withStyle(style -> style.withColor(fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelClientData.hasResetToken() ? 0xFF5555 : 0x555555)),
            btn -> resetAttributes())
            .bounds(startX + 70, startY + spacing * 3 + 10, buttonWidth, buttonHeight)
            .build())
            .active = fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelClientData.hasResetToken(); // Désactiver le bouton si pas de token
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
        // Fond
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xC0101010);
        guiGraphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2, this.topPos + this.imageHeight - 2, 0xFF1a1a1a);
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (cachedData == null) return;

        // Titre
        Component title = Component.translatable("player_level.title").withStyle(style -> style.withColor(0xFFAA00).withBold(true));
        guiGraphics.drawCenteredString(this.font, title, this.leftPos + this.imageWidth / 2, this.topPos + 10, 0xFFFFFF);

        // Informations de niveau
        int infoX = this.leftPos + 20;
        int infoY = this.topPos + 30;

        Component levelText = Component.translatable("player_level.level").append(": §6" + cachedData.getLevel());
        Component xpText = Component.translatable("player_level.xp").append(": §a" + cachedData.getExperience() + " §7/ §a" + cachedData.getExperienceToNextLevel());
        Component pointsText = Component.translatable("player_level.points_available").append(": §d" + cachedData.getAvailablePoints());

        guiGraphics.drawString(this.font, levelText, infoX, infoY, 0xFFFFFF);
        guiGraphics.drawString(this.font, xpText, infoX, infoY + 12, 0xFFFFFF);
        guiGraphics.drawString(this.font, pointsText, infoX, infoY + 24, 0xFFFFFF);

        // Barre de progression XP
        int barY = infoY + 40;
        int barWidth = this.imageWidth - 40;
        int barHeight = 10;

        // Fond de la barre
        guiGraphics.fill(infoX, barY, infoX + barWidth, barY + barHeight, 0xFF3a3a3a);

        // Progression
        float progress = (float) cachedData.getExperience() / cachedData.getExperienceToNextLevel();
        int progressWidth = (int) (barWidth * progress);
        guiGraphics.fill(infoX, barY, infoX + progressWidth, barY + barHeight, 0xFF00AA00);

        // Afficher les valeurs des attributs à côté des boutons
        int attrX = this.leftPos + 130;
        int attrY = this.topPos + 85;
        int attrSpacing = 25;

        guiGraphics.drawString(this.font, "§c" + cachedData.getHealthPoints(), attrX, attrY, 0xFFFFFF);
        guiGraphics.drawString(this.font, "§7" + cachedData.getArmorPoints(), attrX, attrY + attrSpacing, 0xFFFFFF);
        guiGraphics.drawString(this.font, "§b" + cachedData.getSpeedPoints(), attrX, attrY + attrSpacing * 2, 0xFFFFFF);

        attrX = this.leftPos + 250;
        guiGraphics.drawString(this.font, "§d" + cachedData.getIntelligencePoints(), attrX, attrY, 0xFFFFFF);
        guiGraphics.drawString(this.font, "§4" + cachedData.getStrengthPoints(), attrX, attrY + attrSpacing, 0xFFFFFF);
        guiGraphics.drawString(this.font, "§a" + cachedData.getLuckPoints(), attrX, attrY + attrSpacing * 2, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
