package fr.eriniumgroup.erinium_faction.gui.jobs;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Ajoute un bouton dans l'inventaire du joueur pour ouvrir le menu Jobs
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class InventoryJobsButton {

    private static final ResourceLocation BUTTON_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/jobs/button-large.png");

    private static int buttonX;
    private static int buttonY;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Minecraft mc = Minecraft.getInstance();

            // Position du bouton (en haut à droite de l'inventaire, sous le bouton stats)
            int leftPos = (inventoryScreen.width - 176) / 2;
            int topPos = (inventoryScreen.height - 166) / 2;

            buttonX = leftPos + 180;
            buttonY = topPos + 35; // 35 = sous le bouton stats (10 + 20 + 5)

            // Vérifier si la souris est sur le bouton
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH
                             && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;

            // Dessiner le bouton avec texture
            ImageRenderer.renderScaledImageWithAlpha(
                guiGraphics,
                BUTTON_NORMAL,
                buttonX,
                buttonY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                isHovered ? 1.0f : 0.9f
            );

            // Texte du bouton
            Component text = Component.literal("Jobs");
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

                // Ouvrir l'écran Jobs côté client
                Minecraft.getInstance().setScreen(new JobsScreen());
                event.setCanceled(true);
            }
        }
    }
}
