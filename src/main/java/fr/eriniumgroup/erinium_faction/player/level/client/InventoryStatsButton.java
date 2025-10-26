package fr.eriniumgroup.erinium_faction.player.level.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Ajoute un bouton dans l'inventaire du joueur pour ouvrir les statistiques
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class InventoryStatsButton {

    private static int buttonX;
    private static int buttonY;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Minecraft mc = Minecraft.getInstance();

            // Position du bouton (en haut à droite de l'inventaire)
            int leftPos = (inventoryScreen.width - 176) / 2;
            int topPos = (inventoryScreen.height - 166) / 2;

            buttonX = leftPos + 180;
            buttonY = topPos + 10;

            // Vérifier si la souris est sur le bouton
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH
                             && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;

            // Couleur du bouton
            int color = isHovered ? 0xFF4a90e2 : 0xFF2a5a8a;
            int borderColor = isHovered ? 0xFF6ab0ff : 0xFF4a7ac2;

            // Dessiner le bouton
            guiGraphics.fill(buttonX, buttonY, buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT, borderColor);
            guiGraphics.fill(buttonX + 1, buttonY + 1, buttonX + BUTTON_WIDTH - 1, buttonY + BUTTON_HEIGHT - 1, color);

            // Texte du bouton
            Component text = Component.translatable("player_level.button.stats");
            int textX = buttonX + (BUTTON_WIDTH - mc.font.width(text)) / 2;
            int textY = buttonY + (BUTTON_HEIGHT - 8) / 2;
            guiGraphics.drawString(mc.font, text, textX, textY, 0xFFFFFF);
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof InventoryScreen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();

            // Vérifier si le clic est sur le bouton
            if (mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH
                && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT) {

                // Envoyer un paquet au serveur pour ouvrir le menu
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new fr.eriniumgroup.erinium_faction.player.level.network.OpenStatsMenuPacket()
                );

                event.setCanceled(true);
            }
        }
    }
}

