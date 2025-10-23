package fr.eriniumgroup.erinium_faction.core.data;

import fr.eriniumgroup.erinium_faction.core.faction.Faction;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root data structure for faction persistence
 */
public class FactionData {
    private Map<String, Faction> factions = new LinkedHashMap<>();

    public Map<String, Faction> getFactions() {
        return factions;
    }

    public void setFactions(Map<String, Faction> factions) {
        this.factions = factions;
    }
}

