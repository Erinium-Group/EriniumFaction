package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;

/**
 * Stockage côté client des données de faction
 * Utilisé par les pages du GUI pour afficher les informations
 */
public class FactionClientData {
    private static FactionSnapshot currentFactionData = null;

    /**
     * Définit les données actuelles de la faction
     */
    public static void setFactionData(FactionSnapshot data) {
        currentFactionData = data;
    }

    /**
     * Récupère les données de la faction
     * @return FactionSnapshot ou null si aucune donnée
     */
    public static FactionSnapshot getFactionData() {
        return currentFactionData;
    }

    /**
     * Efface les données de faction
     */
    public static void clear() {
        currentFactionData = null;
    }

    /**
     * Vérifie si des données sont disponibles
     */
    public static boolean hasData() {
        return currentFactionData != null;
    }
}
