package fr.eriniumgroup.erinium_faction.client.events;

import fr.eriniumgroup.erinium_faction.client.data.PlayerFactionCache;
import fr.eriniumgroup.erinium_faction.client.data.PlayerLevelCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Event handler côté client pour les nameplates
 * Nettoie les caches quand le joueur se déconnecte
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class ClientNameplateEventHandler {

    /**
     * Nettoyer tous les caches quand le joueur se déconnecte du serveur
     */
    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        // Nettoyer tous les caches
        PlayerFactionCache.clear();
        PlayerLevelCache.clear();
    }
}
