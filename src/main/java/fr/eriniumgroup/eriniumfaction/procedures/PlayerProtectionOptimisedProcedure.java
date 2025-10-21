package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

public class PlayerProtectionOptimisedProcedure {

    // === SYSTÈME DE CACHE ===
    private static final ConcurrentHashMap<String, CachedFactionData> factionCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CachedChunkData> chunkCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 5000; // 5 secondes
    private static final Gson GSON = new Gson();

    // Classes pour le cache
    private static class CachedFactionData {
        final JsonObject data;
        final long timestamp;

        CachedFactionData(JsonObject data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }

    private static class CachedChunkData {
        final JsonObject data;
        final long timestamp;

        CachedChunkData(JsonObject data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }

    // Méthode pour nettoyer le cache périodiquement (appelé depuis un event tick)
    public static void cleanExpiredCache() {
        factionCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        chunkCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public static boolean execute(LevelAccessor world, double x, double z, Entity entity, String perm) {
        if (entity == null || perm == null)
            return false;

        // Récupérer l'ID de faction du chunk
        String tempId = CurrentChunkFactionIdProcedure.execute(world, x, z);

        // === VÉRIFICATIONS RAPIDES (sans I/O) ===

        // 1. Bypass admin
        if (entity instanceof ServerPlayer) {
            if (entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction_bypass) {
                return true;
            }

            // 2. Vérification de faction
            String playerFaction = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction;
            if (!tempId.equals(playerFaction)) {
                return false;
            }
        }

        // 3. Zones spéciales
        if ("safezone".equals(tempId) || "warzone".equals(tempId)) {
            return !("canBreak".equals(perm) || "canPlace".equals(perm));
        }

        if ("wilderness".equals(tempId)) {
            return true;
        }

        // === VÉRIFICATIONS AVEC FICHIERS (avec cache) ===

        try {
            // Charger les données de faction (avec cache)
            JsonObject factionData = loadFactionData(tempId);
            if (factionData == null) {
                return false; // Faction introuvable
            }

            // Vérifier si c'est une faction admin/warzone/safezone
            if (isSpecialFaction(factionData)) {
                return false;
            }

            if (!(entity instanceof ServerPlayer)) {
                return false;
            }

            String playerUUID = entity.getStringUUID();

            // Vérifier si le joueur est le propriétaire
            if (factionData.has("owner") && factionData.get("owner").getAsString().equals(playerUUID)) {
                return true;
            }

            // Extraire le rang du joueur
            String memberList = factionData.has("memberList") ? factionData.get("memberList").getAsString() : "";
            String factionRank = extractRankFromMemberList(playerUUID, memberList);

            if (factionRank == null || "null".equals(factionRank)) {
                return false; // Joueur pas dans la faction
            }

            // Vérifier les permissions du rang
            if (factionData.has(factionRank)) {
                String rankPerms = factionData.get(factionRank).getAsString();
                if (rankPerms.contains(perm)) {
                    return true;
                }
            }

            // Vérifier les permissions spécifiques du chunk
            String chunkKey = getChunkKey(world, x, z);
            JsonObject chunkData = loadChunkData(chunkKey, world, x, z);

            if (chunkData != null && chunkData.has(perm)) {
                String chunkPerm = chunkData.get(perm).getAsString();
                return chunkPerm.contains(factionRank);
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // === MÉTHODES UTILITAIRES ===

    private static JsonObject loadFactionData(String factionId) {
        // Vérifier le cache
        CachedFactionData cached = factionCache.get(factionId);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }

        // Charger depuis le fichier
        File file = FactionFileByIdProcedure.execute(factionId);
        if (file == null || !file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JsonObject data = GSON.fromJson(jsonBuilder.toString(), JsonObject.class);

            // Mettre en cache
            factionCache.put(factionId, new CachedFactionData(data));

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonObject loadChunkData(String chunkKey, LevelAccessor world, double x, double z) {
        // Vérifier le cache
        CachedChunkData cached = chunkCache.get(chunkKey);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }

        // Charger depuis le fichier
        File file = CurrentChunkFileProcedure.execute(world, x, z);
        if (file == null || !file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JsonObject data = GSON.fromJson(jsonBuilder.toString(), JsonObject.class);

            // Mettre en cache
            chunkCache.put(chunkKey, new CachedChunkData(data));

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isSpecialFaction(JsonObject factionData) {
        return (factionData.has("isAdminFaction") && factionData.get("isAdminFaction").getAsBoolean()) ||
                (factionData.has("isWarzone") && factionData.get("isWarzone").getAsBoolean()) ||
                (factionData.has("isSafezone") && factionData.get("isSafezone").getAsBoolean());
    }

    private static String extractRankFromMemberList(String playerUUID, String memberList) {
        if (memberList == null || memberList.isEmpty() || !memberList.contains(":") || !memberList.contains(",")) {
            return null;
        }

        String[] members = memberList.split("\\s*,\\s*");
        for (String member : members) {
            String[] parts = member.split(":", 2);
            if (parts.length == 2 && parts[0].trim().equals(playerUUID)) {
                return parts[1].trim();
            }
        }

        return null;
    }

    private static String getChunkKey(LevelAccessor world, double x, double z) {
        int chunkX = ((int) Math.floor(x)) >> 4;
        int chunkZ = ((int) Math.floor(z)) >> 4;

        // Cast vers Level pour obtenir la dimension
        if (world instanceof net.minecraft.world.level.Level level) {
            return level.dimension().location().toString() + "_" + chunkX + "_" + chunkZ;
        }

        // Fallback
        return "world_" + chunkX + "_" + chunkZ;
    }

    // Méthode pour invalider le cache d'une faction (à appeler lors de modifications)
    public static void invalidateFactionCache(String factionId) {
        factionCache.remove(factionId);
    }

    // Méthode pour invalider le cache d'un chunk (à appeler lors de modifications)
    public static void invalidateChunkCache(LevelAccessor world, double x, double z) {
        String key = getChunkKey(world, x, z);
        chunkCache.remove(key);
    }

    // Méthode pour vider tout le cache
    public static void clearAllCache() {
        factionCache.clear();
        chunkCache.clear();
    }
}