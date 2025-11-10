package fr.eriniumgroup.erinium_faction.client;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.screens.PlayerStatsScreen;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Enregistrement côté client pour les écrans du système de niveau
 */
@EventBusSubscriber(modid = EFC.MODID, value = Dist.CLIENT)
public class PlayerLevelClientInit {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(EFMenus.PLAYER_STATS_MENU.get(), PlayerStatsScreen::new);
    }
}