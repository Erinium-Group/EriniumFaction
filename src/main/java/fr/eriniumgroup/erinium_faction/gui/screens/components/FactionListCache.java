package fr.eriniumgroup.erinium_faction.gui.screens.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache côté client pour stocker la liste des factions reçue du serveur
 */
public class FactionListCache {
    private static List<String> factionIds = new ArrayList<>();
    private static List<String> factionNames = new ArrayList<>();
    private static List<Integer> memberCounts = new ArrayList<>();
    private static boolean loaded = false;
    private static AddAlliancePopup activePopup = null;

    public static void set(List<String> ids, List<String> names, List<Integer> counts) {
        factionIds = new ArrayList<>(ids);
        factionNames = new ArrayList<>(names);
        memberCounts = new ArrayList<>(counts);
        loaded = true;
    }

    public static List<String> getFactionIds() {
        return new ArrayList<>(factionIds);
    }

    public static List<String> getFactionNames() {
        return new ArrayList<>(factionNames);
    }

    public static List<Integer> getMemberCounts() {
        return new ArrayList<>(memberCounts);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void clear() {
        factionIds.clear();
        factionNames.clear();
        memberCounts.clear();
        loaded = false;
    }

    public static void registerPopup(AddAlliancePopup popup) {
        activePopup = popup;
    }

    public static void unregisterPopup() {
        activePopup = null;
    }

    public static void notifyPopup() {
        if (activePopup != null && loaded) {
            activePopup.loadFactions(getFactionIds(), getFactionNames(), getMemberCounts());
        }
    }
}
