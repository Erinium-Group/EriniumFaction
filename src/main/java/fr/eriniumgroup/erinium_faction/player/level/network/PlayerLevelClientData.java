package fr.eriniumgroup.erinium_faction.player.level.network;

/**
 * Données côté client pour le système de niveau
 */
public class PlayerLevelClientData {
    private static boolean hasResetToken = false;

    public static boolean hasResetToken() {
        return hasResetToken;
    }

    public static void setHasResetToken(boolean has) {
        hasResetToken = has;
    }
}

