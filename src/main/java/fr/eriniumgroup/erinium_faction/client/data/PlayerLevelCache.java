package fr.eriniumgroup.erinium_faction.client.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache côté client pour stocker les niveaux des joueurs
 * Mis à jour par des packets réseau
 */
public class PlayerLevelCache {
    private static final Map<UUID, Integer> levels = new ConcurrentHashMap<>();

    /**
     * Met à jour le niveau d'un joueur
     */
    public static void setLevel(UUID playerUUID, int level) {
        if (level <= 0) {
            levels.remove(playerUUID);
        } else {
            levels.put(playerUUID, level);
        }
    }

    /**
     * Récupère le niveau d'un joueur
     */
    public static int getLevel(UUID playerUUID) {
        return levels.getOrDefault(playerUUID, 0);
    }

    /**
     * Retire un joueur du cache
     */
    public static void remove(UUID playerUUID) {
        levels.remove(playerUUID);
    }

    /**
     * Efface tout le cache (déconnexion du serveur)
     */
    public static void clear() {
        levels.clear();
    }
}
