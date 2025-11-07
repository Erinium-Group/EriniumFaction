package fr.eriniumgroup.erinium_faction.features.vanish;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Données côté client pour gérer l'état vanish des joueurs.
 * Permet au client de savoir quels joueurs sont en vanish pour cacher leurs nameplates, etc.
 */
public class VanishClientData {

    // Set des UUIDs des joueurs en vanish (côté client)
    private static final Set<UUID> vanishedPlayers = new HashSet<>();

    /**
     * Définit l'état vanish d'un joueur côté client
     */
    public static void setVanished(UUID uuid, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(uuid);
        } else {
            vanishedPlayers.remove(uuid);
        }
    }

    /**
     * Vérifie si un joueur est en vanish côté client
     */
    public static boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    /**
     * Nettoie toutes les données (appelé lors de la déconnexion)
     */
    public static void clear() {
        vanishedPlayers.clear();
    }
}
