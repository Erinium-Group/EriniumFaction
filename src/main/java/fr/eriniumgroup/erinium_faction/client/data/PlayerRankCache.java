package fr.eriniumgroup.erinium_faction.client.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache côté client des rangs des joueurs
 */
public class PlayerRankCache {

    private static final Map<UUID, RankData> rankCache = new ConcurrentHashMap<>();

    public static class RankData {
        public String rankId;
        public String displayName;
        public String prefix;
        public String suffix;
        public int priority;

        public RankData(String rankId, String displayName, String prefix, String suffix, int priority) {
            this.rankId = rankId;
            this.displayName = displayName;
            this.prefix = prefix;
            this.suffix = suffix;
            this.priority = priority;
        }
    }

    /**
     * Met à jour le rang d'un joueur dans le cache
     */
    public static void updateRank(UUID playerUUID, String rankId, String displayName, String prefix, String suffix, int priority) {
        if (playerUUID == null) return;

        if (rankId == null || rankId.isEmpty()) {
            rankCache.remove(playerUUID);
        } else {
            rankCache.put(playerUUID, new RankData(rankId, displayName, prefix, suffix, priority));
        }
    }

    /**
     * Récupère les données de rang d'un joueur
     */
    public static RankData getRank(UUID playerUUID) {
        return rankCache.get(playerUUID);
    }

    /**
     * Récupère le préfixe d'un joueur
     */
    public static String getPrefix(UUID playerUUID) {
        RankData data = rankCache.get(playerUUID);
        return data != null ? data.prefix : "";
    }

    /**
     * Récupère le nom d'affichage du rang d'un joueur
     */
    public static String getDisplayName(UUID playerUUID) {
        RankData data = rankCache.get(playerUUID);
        return data != null ? data.displayName : "";
    }

    /**
     * Nettoie le cache
     */
    public static void clear() {
        rankCache.clear();
    }

    /**
     * Retire un joueur du cache
     */
    public static void removePlayer(UUID playerUUID) {
        rankCache.remove(playerUUID);
    }
}
