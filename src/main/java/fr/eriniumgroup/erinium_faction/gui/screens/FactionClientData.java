package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.common.network.packets.SyncTransactionHistoryMessage;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Stockage côté client des données de faction
 * Utilisé par les pages du GUI pour afficher les informations
 */
public class FactionClientData {
    private static FactionSnapshot currentFactionData = null;
    private static List<SyncTransactionHistoryMessage.TransactionData> transactionHistory = new ArrayList<>();
    private static double playerBalance = 0.0;

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
     * Définit l'historique des transactions
     */
    public static void setTransactionHistory(List<SyncTransactionHistoryMessage.TransactionData> history) {
        transactionHistory = new ArrayList<>(history);
    }

    /**
     * Récupère l'historique des transactions
     */
    public static List<SyncTransactionHistoryMessage.TransactionData> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    /**
     * Définit le solde du joueur
     */
    public static void setPlayerBalance(double balance) {
        playerBalance = balance;
    }

    /**
     * Récupère le solde du joueur
     */
    public static double getPlayerBalance() {
        return playerBalance;
    }

    /**
     * Efface les données de faction
     */
    public static void clear() {
        currentFactionData = null;
        transactionHistory.clear();
        playerBalance = 0.0;
    }

    /**
     * Vérifie si des données sont disponibles
     */
    public static boolean hasData() {
        return currentFactionData != null;
    }
}
