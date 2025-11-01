package fr.eriniumgroup.erinium_faction.gui.jobs.client;

import fr.eriniumgroup.erinium_faction.gui.jobs.JobsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Ajoute un bouton dans l'inventaire du joueur pour ouvrir le Jobs System
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class InventoryJobsButton {

    private static int buttonX;
    private static int buttonY;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Minecraft mc = Minecraft.getInstance();

            // Position du bouton (en haut à droite de l'inventaire, sous le bouton Stats)
            int leftPos = (inventoryScreen.width - 176) / 2;
            int topPos = (inventoryScreen.height - 166) / 2;

            buttonX = leftPos + 180;
            buttonY = topPos + 35; // 25 pixels sous le bouton Stats (10 + 20 + 5)

            // Vérifier si la souris est sur le bouton
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH
                             && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;

            // Couleur du bouton (jaune/or pour le Jobs System)
            int color = isHovered ? 0xFFfbbf24 : 0xFFd97706;
            int borderColor = isHovered ? 0xFFfcd34d : 0xFFfbbf24;

            // Dessiner le bouton
            guiGraphics.fill(buttonX, buttonY, buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT, borderColor);
            guiGraphics.fill(buttonX + 1, buttonY + 1, buttonX + BUTTON_WIDTH - 1, buttonY + BUTTON_HEIGHT - 1, color);

            // Texte du bouton
            Component text = Component.literal("Jobs");
            int textX = buttonX + (BUTTON_WIDTH - mc.font.width(text)) / 2;
            int textY = buttonY + (BUTTON_HEIGHT - 8) / 2;
            guiGraphics.drawString(mc.font, text, textX, textY, 0x000000); // Texte noir pour contraste avec fond jaune
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

                // Ouvrir directement l'écran côté client
                Minecraft.getInstance().setScreen(new JobsScreen());

                event.setCanceled(true);
            }
        }
    }
}
