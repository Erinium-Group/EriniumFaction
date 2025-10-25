package fr.eriniumgroup.erinium_faction.core.rank;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.NbtIo;
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
 * - Persistance: ${GAMEDIR}/erinium_faction/ranks/{ranks.dat, players.dat}
 * - Un rank par joueur (simple, extensible plus tard si besoin)
 * - Permissions: chaînes style "module.action"; le joker "*" donne tous les droits.
 */
public class EFRManager {

    private static final Path DATA_DIR = FMLPaths.GAMEDIR.get().resolve(EFC.MOD_ID).resolve("ranks");
    private static final Path RANKS_FILE = DATA_DIR.resolve("ranks.dat");
    private static final Path PLAYERS_FILE = DATA_DIR.resolve("players.dat");

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
            try {
                CompoundTag root = NbtIo.read(RANKS_FILE);
                if (root != null) {
                    ListTag list = root.getList("ranks", 10); // 10 = CompoundTag
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag rt = list.getCompound(i);
                        Rank r = new Rank();
                        r.id = rt.getString("id");
                        if (r.id == null || r.id.isBlank()) continue;
                        r.id = r.id.toLowerCase(Locale.ROOT);
                        r.displayName = rt.getString("display");
                        r.priority = rt.getInt("priority");
                        r.prefix = rt.getString("prefix");
                        r.suffix = rt.getString("suffix");
                        r.permissions = new HashSet<>();
                        ListTag perms = rt.getList("perms", 8); // 8 = StringTag
                        for (int j = 0; j < perms.size(); j++) r.permissions.add(perms.getString(j));
                        ranks.put(r.id, r);
                    }
                }
            } catch (Exception ex) {
                EFC.log.error("Ranks", "§cErreur lecture ranks.dat: {}", ex.toString());
            }
        }

        // Players
        playerRanks.clear();
        if (Files.exists(PLAYERS_FILE)) {
            try {
                CompoundTag root = NbtIo.read(PLAYERS_FILE);
                if (root != null) {
                    CompoundTag players = root.getCompound("players");
                    for (String key : players.getAllKeys()) {
                        try {
                            UUID uuid = UUID.fromString(key);
                            String rankId = players.getString(key);
                            if (rankId != null && !rankId.isBlank()) {
                                playerRanks.put(uuid, rankId.toLowerCase(Locale.ROOT));
                            }
                        } catch (Exception ignored) { }
                    }
                }
            } catch (Exception ex) {
                EFC.log.error("Ranks", "§cErreur lecture players.dat: {}", ex.toString());
            }
        }

        // Si aucune définition, créer des ranks par défaut
        if (ranks.isEmpty()) {
            Rank def = new Rank("default", "§7Joueur", 0);
            // Permissions de base pour ne pas bloquer l'expérience vanilla
            def.permissions.add("player.*");
            def.permissions.add("server.command.*");
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
            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();
            for (Rank r : ranks.values()) {
                CompoundTag rt = new CompoundTag();
                rt.putString("id", r.id);
                rt.putString("display", r.displayName == null ? "" : r.displayName);
                rt.putInt("priority", r.priority);
                rt.putString("prefix", r.prefix == null ? "" : r.prefix);
                rt.putString("suffix", r.suffix == null ? "" : r.suffix);
                ListTag perms = new ListTag();
                if (r.permissions != null) for (String p : r.permissions) perms.add(StringTag.valueOf(p));
                rt.put("perms", perms);
                list.add(rt);
            }
            root.put("ranks", list);
            NbtIo.write(root, RANKS_FILE);
        } catch (Exception e) {
            EFC.log.error("Ranks", "§cErreur écriture ranks.dat: {}", e.toString());
        }
    }

    private void savePlayers() {
        try {
            Files.createDirectories(DATA_DIR);
            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, String> e : playerRanks.entrySet()) {
                players.putString(e.getKey().toString(), e.getValue());
            }
            root.put("players", players);
            NbtIo.write(root, PLAYERS_FILE);
        } catch (Exception e) {
            EFC.log.error("Ranks", "§cErreur écriture players.dat: {}", e.toString());
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

