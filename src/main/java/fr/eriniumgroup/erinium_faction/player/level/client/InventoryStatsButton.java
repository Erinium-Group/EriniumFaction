package fr.eriniumgroup.erinium_faction.player.level.client;

import fr.eriniumgroup.erinium_faction.jobs.gui.JobsMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.awt.*;

/**
 * Ajoute des boutons au-dessus de l'inventaire du joueur pour ouvrir les stats et les jobs
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class InventoryStatsButton {

    private static int statsButtonX;
    private static int statsButtonY;
    private static int jobsButtonX;
    private static int jobsButtonY;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Minecraft mc = Minecraft.getInstance();

            // Position des boutons (au-dessus de l'inventaire, centrés)
            int leftPos = (inventoryScreen.width - 176) / 2;
            int topPos = (inventoryScreen.height - 166) / 2;

            // Stats button (à gauche)
            statsButtonX = leftPos + (176 - (BUTTON_WIDTH * 2 + BUTTON_SPACING)) / 2;
            statsButtonY = topPos - BUTTON_HEIGHT - 6;

            // Jobs button (à droite)
            jobsButtonX = statsButtonX + BUTTON_WIDTH + BUTTON_SPACING;
            jobsButtonY = statsButtonY;

            // Vérifier si la souris est sur les boutons
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            boolean isStatsHovered = mouseX >= statsButtonX && mouseX <= statsButtonX + BUTTON_WIDTH
                                   && mouseY >= statsButtonY && mouseY <= statsButtonY + BUTTON_HEIGHT;
            boolean isJobsHovered = mouseX >= jobsButtonX && mouseX <= jobsButtonX + BUTTON_WIDTH
                                  && mouseY >= jobsButtonY && mouseY <= jobsButtonY + BUTTON_HEIGHT;

            // Dessiner le bouton Stats
            int statsColor = isStatsHovered ? 0xFF4a90e2 : 0xFF2a5a8a;
            int statsBorderColor = isStatsHovered ? 0xFF6ab0ff : 0xFF4a7ac2;
            guiGraphics.fill(statsButtonX, statsButtonY, statsButtonX + BUTTON_WIDTH, statsButtonY + BUTTON_HEIGHT, statsBorderColor);
            guiGraphics.fill(statsButtonX + 1, statsButtonY + 1, statsButtonX + BUTTON_WIDTH - 1, statsButtonY + BUTTON_HEIGHT - 1, statsColor);

            Component statsText = Component.translatable("player_level.button.stats");
            int statsTextX = statsButtonX + (BUTTON_WIDTH - mc.font.width(statsText)) / 2;
            int statsTextY = statsButtonY + (BUTTON_HEIGHT - 8) / 2;
            guiGraphics.drawString(mc.font, statsText, statsTextX, statsTextY, 0xFFFFFF);

            // Dessiner le bouton Jobs (couleur dorée)
            int jobsColor = isJobsHovered ? 0xFFfcd34d : 0xFFfbbf24;
            int jobsBorderColor = isJobsHovered ? 0xFFfde68a : 0xFFfcd34d;
            guiGraphics.fill(jobsButtonX, jobsButtonY, jobsButtonX + BUTTON_WIDTH, jobsButtonY + BUTTON_HEIGHT, jobsBorderColor);
            guiGraphics.fill(jobsButtonX + 1, jobsButtonY + 1, jobsButtonX + BUTTON_WIDTH - 1, jobsButtonY + BUTTON_HEIGHT - 1, jobsColor);

            Component jobsText = Component.literal("Jobs");
            int jobsTextX = jobsButtonX + (BUTTON_WIDTH - mc.font.width(jobsText)) / 2;
            int jobsTextY = jobsButtonY + (BUTTON_HEIGHT - 8) / 2;
            guiGraphics.drawString(mc.font, jobsText, jobsTextX, jobsTextY, Color.WHITE.getRGB());
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof InventoryScreen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            Minecraft mc = Minecraft.getInstance();

            // Vérifier si le clic est sur le bouton Stats
            if (mouseX >= statsButtonX && mouseX <= statsButtonX + BUTTON_WIDTH
                && mouseY >= statsButtonY && mouseY <= statsButtonY + BUTTON_HEIGHT) {

                // Envoyer un paquet au serveur pour ouvrir le menu
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new fr.eriniumgroup.erinium_faction.player.level.network.OpenStatsMenuPacket()
                );

                event.setCanceled(true);
            }

            // Vérifier si le clic est sur le bouton Jobs
            if (mouseX >= jobsButtonX && mouseX <= jobsButtonX + BUTTON_WIDTH
                && mouseY >= jobsButtonY && mouseY <= jobsButtonY + BUTTON_HEIGHT) {

                // Ouvrir directement l'écran Jobs (côté client uniquement)
                if (mc != null) {
                    mc.setScreen(new JobsMenuScreen());
                }

                event.setCanceled(true);
            }
        }
    }
}

