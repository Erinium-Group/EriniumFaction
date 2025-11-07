package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.client.data.PlayerFactionCache;
import fr.eriniumgroup.erinium_faction.client.data.PlayerLevelCache;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.UUID;

/**
 * Handler côté client uniquement pour les nameplates
 * Cette classe ne sera chargée QUE côté client
 */
@OnlyIn(Dist.CLIENT)
public class ClientNameplateHandler {

    /**
     * Met à jour les caches côté client avec les données reçues
     */
    public static void updateCaches(UUID playerUUID, String factionName, int level) {
        PlayerFactionCache.setFactionName(playerUUID, factionName);
        PlayerLevelCache.setLevel(playerUUID, level);
    }
}
