package fr.eriniumgroup.erinium_faction.player.level.client;

import fr.eriniumgroup.erinium_faction.player.level.gui.PlayerStatsMenu;
import fr.eriniumgroup.erinium_faction.player.level.gui.PlayerStatsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Enregistrement côté client pour les écrans du système de niveau
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class PlayerLevelClientInit {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(fr.eriniumgroup.erinium_faction.init.EFMenus.PLAYER_STATS_MENU.get(), PlayerStatsScreen::new);
    }
}