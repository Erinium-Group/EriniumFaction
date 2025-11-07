package fr.eriniumgroup.erinium_faction.client.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache côté client pour stocker les noms de faction des joueurs
 * Mis à jour par des packets réseau
 */
public class PlayerFactionCache {
    private static final Map<UUID, String> factionNames = new ConcurrentHashMap<>();

    /**
     * Met à jour le nom de faction d'un joueur
     */
    public static void setFactionName(UUID playerUUID, String factionName) {
        if (factionName == null || factionName.isEmpty()) {
            factionNames.remove(playerUUID);
        } else {
            factionNames.put(playerUUID, factionName);
        }
    }

    /**
     * Récupère le nom de faction d'un joueur
     */
    public static String getFactionName(UUID playerUUID) {
        return factionNames.get(playerUUID);
    }

    /**
     * Retire un joueur du cache
     */
    public static void remove(UUID playerUUID) {
        factionNames.remove(playerUUID);
    }

    /**
     * Efface tout le cache (déconnexion du serveur)
     */
    public static void clear() {
        factionNames.clear();
    }
}
