package fr.eriniumgroup.erinium_faction.gui.screens.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache côté client pour stocker les demandes d'alliance validées reçues du serveur
 */
public class AllyRequestsCache {
    private static List<String> requestIds = new ArrayList<>();
    private static List<String> requestNames = new ArrayList<>();
    private static boolean loaded = false;
    private static AllyRequestsPopup activePopup = null;

    public static void set(List<String> ids, List<String> names) {
        requestIds = new ArrayList<>(ids);
        requestNames = new ArrayList<>(names);
        loaded = true;
    }

    public static List<String> getRequestIds() {
        return new ArrayList<>(requestIds);
    }

    public static List<String> getRequestNames() {
        return new ArrayList<>(requestNames);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isEmpty() {
        return requestIds.isEmpty();
    }

    public static void clear() {
        requestIds.clear();
        requestNames.clear();
        loaded = false;
    }

    public static void registerPopup(AllyRequestsPopup popup) {
        activePopup = popup;
    }

    public static void unregisterPopup() {
        activePopup = null;
    }

    public static void notifyPopup() {
        if (activePopup != null && loaded) {
            if (isEmpty()) {
                // Aucune demande valide, fermer le popup
                activePopup.close();
            } else {
                // Charger les demandes validées
                List<Integer> memberCounts = new ArrayList<>();
                for (int i = 0; i < requestIds.size(); i++) {
                    memberCounts.add(0); // On s'en fout du nombre de membres
                }
                activePopup.loadRequests(getRequestIds(), getRequestNames(), memberCounts);
            }
        }
    }
}
