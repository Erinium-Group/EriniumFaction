package fr.eriniumgroup.erinium_faction.core.rank;

import com.google.gson.*;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * EFRManager – Gestionnaire des ranks et permissions côté serveur.
 * - Persistance: ${GAMEDIR}/erinium_faction/ranks/{ranks.json, players.json}
 * - Un rank par joueur (simple, extensible plus tard si besoin)
 * - Permissions: chaînes style "module.action"; le joker "*" donne tous les droits.
 */
public class EFRManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path DATA_DIR = FMLPaths.GAMEDIR.get().resolve(EFC.MOD_ID).resolve("ranks");
    private static final Path RANKS_FILE = DATA_DIR.resolve("ranks.json");
    private static final Path PLAYERS_FILE = DATA_DIR.resolve("players.json");

    private static final EFRManager INSTANCE = new EFRManager();

    public static EFRManager get() {
        return INSTANCE;
    }

    public static class Rank {
        public String id;                // identifiant unique (ex: "default", "vip")
        public String displayName;       // nom visible (ex: "VIP")
        public int priority;             // plus grand = plus haut
        public Set<String> permissions;  // ex: ["ef.rank.manage", "kit.vip"]
        public String prefix;            // optionnel pour futur affichage
        public String suffix;            // optionnel

        public Rank() {
        }

        public Rank(String id, String displayName, int priority) {
            this.id = id.toLowerCase(Locale.ROOT);
            this.displayName = displayName;
            this.priority = priority;
            this.permissions = new HashSet<>();
            this.prefix = "";
            this.suffix = "";
        }
    }

    private final Map<String, Rank> ranks = new ConcurrentHashMap<>(); // id -> Rank
    private final Map<UUID, String> playerRanks = new ConcurrentHashMap<>(); // uuid -> rankId

    private EFRManager() {
    }

    // Chargement/Sauvegarde ---------------------------------------------------

    public synchronized void load() {
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            EFC.log.error("Ranks", "§cImpossible de créer le dossier des ranks: {}", e.toString());
        }

        // Ranks
        ranks.clear();
        if (Files.exists(RANKS_FILE)) {
            try (BufferedReader r = new BufferedReader(new FileReader(RANKS_FILE.toFile()))) {
                JsonElement root = JsonParser.parseReader(r);
                if (root != null && root.isJsonObject()) {
                    JsonObject obj = root.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                        Rank rank = GSON.fromJson(e.getValue(), Rank.class);
                        if (rank != null && rank.id != null && !rank.id.isBlank()) {
                            rank.id = rank.id.toLowerCase(Locale.ROOT);
                            if (rank.permissions == null) rank.permissions = new HashSet<>();
                            ranks.put(rank.id, rank);
                        }
                    }
                }
            } catch (Exception ex) {
                EFC.log.error("Ranks", "§cErreur lecture ranks.json: {}", ex.toString());
            }
        }

        // Players
        playerRanks.clear();
        if (Files.exists(PLAYERS_FILE)) {
            try (BufferedReader r = new BufferedReader(new FileReader(PLAYERS_FILE.toFile()))) {
                JsonElement root = JsonParser.parseReader(r);
                if (root != null && root.isJsonObject()) {
                    JsonObject obj = root.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                        try {
                            UUID uuid = UUID.fromString(e.getKey());
                            String rankId = e.getValue().getAsString();
                            if (rankId != null && !rankId.isBlank()) {
                                playerRanks.put(uuid, rankId.toLowerCase(Locale.ROOT));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ex) {
                EFC.log.error("Ranks", "§cErreur lecture players.json: {}", ex.toString());
            }
        }

        // Si aucune définition, créer des ranks par défaut
        if (ranks.isEmpty()) {
            Rank def = new Rank("default", "§7Joueur", 0);
            Rank vip = new Rank("vip", "§6VIP", 10);
            vip.permissions.add("efr.example.kit.vip");
            ranks.put(def.id, def);
            ranks.put(vip.id, vip);
            saveRanks();
            EFC.log.info("Ranks", "§aRanks par défaut créés (default, vip)");
        }

        EFC.log.info("Ranks", "§aDonnées charges: §e{} ranks§7, §e{} joueurs§7.", ranks.size(), playerRanks.size());
    }

    public synchronized void save() {
        saveRanks();
        savePlayers();
    }

    private void saveRanks() {
        try {
            Files.createDirectories(DATA_DIR);
            JsonObject obj = new JsonObject();
            for (Rank r : ranks.values()) {
                obj.add(r.id, GSON.toJsonTree(r));
            }
            try (BufferedWriter w = new BufferedWriter(new FileWriter(RANKS_FILE.toFile()))) {
                GSON.toJson(obj, w);
            }
        } catch (Exception e) {
            EFC.log.error("Ranks", "§cErreur écriture ranks.json: {}", e.toString());
        }
    }

    private void savePlayers() {
        try {
            Files.createDirectories(DATA_DIR);
            JsonObject obj = new JsonObject();
            for (Map.Entry<UUID, String> e : playerRanks.entrySet()) {
                obj.addProperty(e.getKey().toString(), e.getValue());
            }
            try (BufferedWriter w = new BufferedWriter(new FileWriter(PLAYERS_FILE.toFile()))) {
                GSON.toJson(obj, w);
            }
        } catch (Exception e) {
            EFC.log.error("Ranks", "§cErreur écriture players.json: {}", e.toString());
        }
    }

    // API Ranks ---------------------------------------------------------------

    public synchronized boolean createRank(String id, String displayName, int priority) {
        if (id == null || id.isBlank()) return false;
        String key = id.toLowerCase(Locale.ROOT);
        if (ranks.containsKey(key)) return false;
        ranks.put(key, new Rank(key, displayName, priority));
        saveRanks();
        return true;
    }

    public synchronized boolean deleteRank(String id) {
        if (id == null) return false;
        String key = id.toLowerCase(Locale.ROOT);
        if (ranks.remove(key) != null) {
            // retirer l'assignation des joueurs sur ce rank
            playerRanks.entrySet().removeIf(e -> e.getValue().equals(key));
            save();
            return true;
        }
        return false;
    }

    public synchronized boolean addPermission(String rankId, String perm) {
        Rank r = getRank(rankId);
        if (r == null || perm == null || perm.isBlank()) return false;
        boolean changed = r.permissions.add(perm);
        if (changed) saveRanks();
        return changed;
    }

    public synchronized boolean removePermission(String rankId, String perm) {
        Rank r = getRank(rankId);
        if (r == null || perm == null || perm.isBlank()) return false;
        boolean changed = r.permissions.remove(perm);
        if (changed) saveRanks();
        return changed;
    }

    public synchronized Rank getRank(String id) {
        return id == null ? null : ranks.get(id.toLowerCase(Locale.ROOT));
    }

    public synchronized List<Rank> listRanksSorted() {
        return ranks.values().stream().sorted(Comparator.comparingInt((Rank r) -> r.priority).reversed().thenComparing(r -> r.id)).collect(Collectors.toList());
    }

    // API Joueurs -------------------------------------------------------------

    public synchronized boolean setPlayerRank(UUID uuid, String rankId) {
        if (uuid == null || rankId == null) return false;
        String key = rankId.toLowerCase(Locale.ROOT);
        if (!ranks.containsKey(key)) return false;
        playerRanks.put(uuid, key);
        savePlayers();
        return true;
    }

    public synchronized String getPlayerRankId(UUID uuid) {
        return uuid == null ? null : playerRanks.get(uuid);
    }

    public synchronized Rank getPlayerRank(UUID uuid) {
        String id = getPlayerRankId(uuid);
        return id == null ? null : getRank(id);
    }

    public boolean hasPermission(ServerPlayer player, String node) {
        if (player == null || node == null || node.isBlank()) return false;
        // OP a tous les droits
        if (player.hasPermissions(2)) return true;
        Rank r;
        synchronized (this) {
            r = getPlayerRank(player.getUUID());
        }
        if (r == null) return false;
        if (r.permissions.contains("*")) return true;
        if (r.permissions.contains(node)) return true;
        // support simple des préfixes (ex: "efr.*")
        int dot;
        String current = node;
        while ((dot = current.lastIndexOf('.')) > 0) {
            current = current.substring(0, dot) + ".*";
            if (r.permissions.contains(current)) return true;
            current = current.substring(0, current.length() - 2); // retire .* pour la boucle
        }
        return false;
    }

    // Utilitaires -------------------------------------------------------------

    public static Path getDataDir() {
        return DATA_DIR;
    }

    public static File getRanksFile() {
        return RANKS_FILE.toFile();
    }

    public static File getPlayersFile() {
        return PLAYERS_FILE.toFile();
    }
}