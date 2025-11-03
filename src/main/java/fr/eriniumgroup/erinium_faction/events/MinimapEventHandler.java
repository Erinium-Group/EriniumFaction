package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.minimap.WaypointServerManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Gestionnaire d'événements pour la minimap
 */
@EventBusSubscriber(modid = EFC.MODID)
public class MinimapEventHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Synchroniser les waypoints du joueur
            WaypointServerManager manager = WaypointServerManager.getInstance();
            if (manager != null) {
                manager.syncToPlayer(serverPlayer);
            }
        }
    }
}
