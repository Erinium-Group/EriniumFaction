package fr.eriniumgroup.erinium_faction.client.gui.bounty;

import fr.eriniumgroup.erinium_faction.common.network.packets.PlaceBountyPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * GUI pour créer/placer une bounty
 * Taille: 300x250 max avec scale
 */
public class BountyCreatorScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/bounty/little_menu.png");
    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 250;

    private int leftPos;
    private int topPos;
    private float scale = 1.0f;

    private EditBox amountBox;
    private UUID selectedPlayerId;
    private String selectedPlayerName;
    private Button selectPlayerButton;

    public BountyCreatorScreen() {
        super(Component.translatable("erinium_faction.gui.bounty.creator.title"));
        this.selectedPlayerId = null;
        this.selectedPlayerName = null;
    }

    public BountyCreatorScreen(UUID playerId, String playerName) {
        super(Component.translatable("erinium_faction.gui.bounty.creator.title"));
        this.selectedPlayerId = playerId;
        this.selectedPlayerName = playerName;
    }

    @Override
    protected void init() {
        // Calculer la position et le scale
        calculateScaleAndPosition();

        // Position centrée
        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;

        // Champs de saisie au centre
        int fieldWidth = 200;
        int fieldHeight = 20;
        int centerX = leftPos + (GUI_WIDTH / 2) - (fieldWidth / 2);
        int startY = topPos + 60;

        // Bouton "Sélectionner un joueur"
        selectPlayerButton = Button.builder(
                selectedPlayerName != null ?
                    Component.literal("§a" + selectedPlayerName) :
                    Component.translatable("erinium_faction.gui.bounty.creator.select_player"),
                btn -> minecraft.setScreen(new PlayerSelectionScreen((uuid, name) -> {
                    selectedPlayerId = uuid;
                    selectedPlayerName = name;
                }))
        ).bounds(centerX, startY, fieldWidth, fieldHeight).build();
        addRenderableWidget(selectPlayerButton);

        // Champ "Montant"
        amountBox = new EditBox(font, centerX, startY + 35, fieldWidth, fieldHeight,
                Component.translatable("erinium_faction.gui.bounty.creator.amount"));
        amountBox.setMaxLength(16);
        amountBox.setValue("1000");
        addRenderableWidget(amountBox);

        // Boutons
        int buttonWidth = 90;
        int buttonHeight = 25;

        // Bouton "Placer la prime"
        addRenderableWidget(Button.builder(
                Component.translatable("erinium_faction.gui.bounty.creator.place"),
                btn -> placeBounty()
        ).bounds(centerX, startY + 80, buttonWidth, buttonHeight).build());

        // Bouton "Retour"
        addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                btn -> minecraft.setScreen(new BountyMainMenuScreen())
        ).bounds(centerX + buttonWidth + 20, startY + 80, buttonWidth, buttonHeight).build());
    }

    private void calculateScaleAndPosition() {
        float scaleW = (float) this.width / GUI_WIDTH;
        float scaleH = (float) this.height / GUI_HEIGHT;
        scale = Math.min(1.0f, Math.min(scaleW, scaleH));
    }

    private void placeBounty() {
        // Vérifier qu'un joueur est sélectionné
        if (selectedPlayerId == null || selectedPlayerName == null) {
            minecraft.gui.getChat().addMessage(Component.translatable("erinium_faction.gui.bounty.creator.error.no_player"));
            return;
        }

        String amountStr = amountBox.getValue().trim();

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            minecraft.gui.getChat().addMessage(Component.translatable("erinium_faction.gui.bounty.creator.error.invalid_amount"));
            return;
        }

        // Envoyer le packet au serveur
        PacketDistributor.sendToServer(new PlaceBountyPacket(selectedPlayerId, selectedPlayerName, amount));

        // Fermer la GUI
        minecraft.setScreen(null);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - désactive le flou du background par défaut
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dessiner le background
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        // Rendre les widgets
        super.render(graphics, mouseX, mouseY, partialTick);

        // Titre (après les widgets)
        Component title = Component.translatable("erinium_faction.gui.bounty.creator.title");
        int titleX = leftPos + (GUI_WIDTH / 2) - (font.width(title) / 2);
        graphics.drawString(font, title, titleX, topPos + 20, 0xFFFFFF);

        // Labels
        graphics.drawString(font, Component.translatable("erinium_faction.gui.bounty.creator.amount_label"),
                leftPos + 50, topPos + 85, 0xFFFFFF);

        // Info commission
        double commission = fr.eriniumgroup.erinium_faction.features.bounty.BountyConfig.get().getCommissionRate() * 100;
        Component commissionText = Component.literal("§7Commission: §e" + String.format("%.0f", commission) + "%");
        graphics.drawString(font, commissionText, leftPos + 50, topPos + 115, 0xAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
