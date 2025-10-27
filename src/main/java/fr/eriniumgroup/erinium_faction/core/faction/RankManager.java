package fr.eriniumgroup.erinium_faction.core.faction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestionnaire utilitaire pour manipuler les rangs d'une Faction et leurs permissions.
 * - Création / suppression de rangs
 * - Ajout / retrait / remplacement de permissions
 * - Listes triées des rangs et des membres par rang
 */
public final class RankManager {
    private final Faction faction;

    public RankManager(Faction faction) { this.faction = Objects.requireNonNull(faction); }

    private static String norm(String id) { return id == null ? "" : id.toLowerCase(Locale.ROOT); }

    public boolean exists(String id) { return faction.getRanks().containsKey(norm(id)); }

    public Rank get(String id) {
        String k = norm(id);
        if (!faction.getRanks().containsKey(k)) return null;
        return new Rank(k, faction);
    }

    public List<Rank> listByPriorityDesc() {
        return faction.getRanks().values().stream()
                .sorted(Comparator.comparingInt((Faction.RankDef r) -> r.priority).reversed())
                .map(r -> new Rank(r.id, faction))
                .collect(Collectors.toList());
    }

    public Rank create(String id, String display, int priority) {
        if (id == null || id.isBlank()) return null;
        String k = norm(id);
        if (exists(k)) return null;
        boolean ok = faction.addRank(k, display == null ? k : display, priority);
        if (!ok) return null;
        fr.eriniumgroup.erinium_faction.core.faction.FactionManager.markDirty();
        return new Rank(k, faction);
    }

    public boolean delete(String id) {
        String k = norm(id);
        if (!exists(k)) return false;
        // Protéger les rangs "noyau"
        if ("leader".equals(k)) return false;
        // Réassigner les membres possédant ce rang vers "member" si possible
        for (Map.Entry<UUID, Faction.Member> e : faction.getMembers().entrySet()) {
            if (k.equals(e.getValue().rankId)) {
                e.getValue().rankId = faction.getRanks().containsKey("member") ? "member" : "recruit";
            }
        }
        // Supprimer le rang
        Faction.RankDef removed = faction.getRanks().remove(k);
        if (removed != null) {
            fr.eriniumgroup.erinium_faction.core.faction.FactionManager.markDirty();
            return true;
        }
        return false;
    }

    public Set<String> getPermissions(String id) {
        String k = norm(id);
        Faction.RankDef def = faction.getRanks().get(k);
        if (def == null) return Collections.emptySet();
        return Collections.unmodifiableSet(def.perms);
    }

    public boolean addPermission(String id, String perm) {
        if (perm == null || perm.isBlank()) return false;
        boolean ok = faction.addRankPerm(norm(id), perm.trim());
        if (ok) fr.eriniumgroup.erinium_faction.core.faction.FactionManager.markDirty();
        return ok;
    }

    public boolean removePermission(String id, String perm) {
        if (perm == null || perm.isBlank()) return false;
        boolean ok = faction.removeRankPerm(norm(id), perm.trim());
        if (ok) fr.eriniumgroup.erinium_faction.core.faction.FactionManager.markDirty();
        return ok;
    }

    public boolean setPermissions(String id, Collection<String> perms) {
        String k = norm(id);
        Faction.RankDef def = faction.getRanks().get(k);
        if (def == null) return false;
        def.perms.clear();
        if (perms != null) {
            for (String p : perms) {
                if (p != null && !p.isBlank()) def.perms.add(p.trim());
            }
        }
        fr.eriniumgroup.erinium_faction.core.faction.FactionManager.markDirty();
        return true;
    }

    /** Vérifie un droit au niveau du rang (sans tenir compte du status owner). */
    public boolean hasPermissionOnRank(String id, String node) {
        String k = norm(id);
        Faction.RankDef def = faction.getRanks().get(k);
        if (def == null || node == null || node.isBlank()) return false;
        return matches(def.perms, node);
    }

    /** Retourne la liste des joueurs (UUID) qui ont exactement ce rang. */
    public List<UUID> listMembersWithRank(String id) {
        String k = norm(id);
        List<UUID> out = new ArrayList<>();
        for (Map.Entry<UUID, Faction.Member> e : faction.getMembers().entrySet()) {
            if (k.equals(e.getValue().rankId)) out.add(e.getKey());
        }
        return out;
    }

    private static boolean matches(Set<String> perms, String node) {
        if (perms.contains("*")) return true;
        if (perms.contains(node)) return true;
        // hiérarchie: faction.manage.settings -> faction.manage.* -> faction.*
        String cur = node;
        while (true) {
            int i = cur.lastIndexOf('.');
            if (i < 0) break;
            cur = cur.substring(0, i) + ".*";
            if (perms.contains(cur)) return true;
            // retire le .* que nous venons d'ajouter pour remonter d'un cran
            cur = cur.substring(0, cur.length() - 2);
        }
        return false;
    }
}
