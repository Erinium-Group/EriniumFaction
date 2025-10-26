package fr.eriniumgroup.erinium_faction.client;

import com.mojang.blaze3d.platform.Window;
import fr.eriniumgroup.erinium_faction.common.config.EFClientConfig;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = EFC.MODID, value = Dist.CLIENT)
public class WindowSizeChecker {

    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && EFClientConfig.AUTO_SCREEN_RESIZE.get()) { // Seulement en jeu
            Window window = mc.getWindow();

            int currentWidth = window.getWidth();
            int currentHeight = window.getHeight();

            if (currentWidth < MIN_WIDTH || currentHeight < MIN_HEIGHT) {
                window.setWindowed(
                        Math.max(currentWidth, MIN_WIDTH),
                        Math.max(currentHeight, MIN_HEIGHT)
                );

                // Message au joueur
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                            Component.translatable("erinium_faction.client.resized.message", MIN_WIDTH, MIN_HEIGHT),
                            true // actionbar
                    );
                }
            }
        }
    }
}