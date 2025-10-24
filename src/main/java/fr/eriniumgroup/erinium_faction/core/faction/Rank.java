package fr.eriniumgroup.erinium_faction.core.faction;

import java.util.Objects;

/**
 * Wrapper léger pour exposer le rang d’un membre côté UI/commandes.
 */
public class Rank {
    private final String id;
    private final Faction faction;

    public Rank(String id, Faction faction) {
        this.id = id;
        this.faction = faction;
    }

    public String getId() {
        return id;
    }

    public boolean canManageSettings() {
        // Autoriser si le rang possède la permission dédiée ou wildcard
        return faction != null && (faction.hasPermission(faction.getOwner(), "*") // owner
                || faction.getRanks().getOrDefault(id, null) != null && (faction.getRanks().get(id).perms.contains("faction.manage.*") || faction.getRanks().get(id).perms.contains("faction.manage.settings") || faction.getRanks().get(id).perms.contains("*")));
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rank)) return false;
        Rank rank = (Rank) o;
        return Objects.equals(id, rank.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

