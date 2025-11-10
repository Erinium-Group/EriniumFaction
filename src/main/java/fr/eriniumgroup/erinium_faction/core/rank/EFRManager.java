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
        public Set<String> permissions;  // ex: ["ef.rank.manage", "kit.vip", "ef.home.max:3"]
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
    // Overrides de permissions au niveau joueur (en plus du rank)
    private final Map<UUID, Set<String>> playerOverrides = new ConcurrentHashMap<>(); // uuid -> perms

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
        playerOverrides.clear();
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
                    // Overrides de permissions au niveau joueur (facultatif)
                    if (root.contains("perm_overrides", 10)) { // 10 = Compound
                        CompoundTag ov = root.getCompound("perm_overrides");
                        for (String key : ov.getAllKeys()) {
                            try {
                                UUID uuid = UUID.fromString(key);
                                ListTag list = ov.getList(key, 8); // 8 = StringTag
                                Set<String> set = new HashSet<>();
                                for (int i = 0; i < list.size(); i++) set.add(list.getString(i));
                                if (!set.isEmpty()) playerOverrides.put(uuid, set);
                            } catch (Exception ignored) { }
                        }
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
            def.permissions.add("player.*");              // Actions de base (break, place, interact)
            def.permissions.add("server.command.*");      // Accès aux commandes
            def.permissions.add("ef.faction.*");          // Accès aux commandes de faction
            Rank vip = new Rank("vip", "§6VIP", 10);
            vip.permissions.add("player.*");
            vip.permissions.add("server.command.*");
            vip.permissions.add("ef.faction.*");
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
            // Sauvegarder overrides
            if (!playerOverrides.isEmpty()) {
                CompoundTag ov = new CompoundTag();
                for (Map.Entry<UUID, Set<String>> e : playerOverrides.entrySet()) {
                    ListTag list = new ListTag();
                    for (String p : e.getValue()) list.add(StringTag.valueOf(p));
                    ov.put(e.getKey().toString(), list);
                }
                root.put("perm_overrides", ov);
            }
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

        // Synchroniser vers tous les clients
        syncPlayerRankToClients(uuid);

        return true;
    }

    /**
     * Synchronise le rang d'un joueur vers tous les clients connectés
     */
    private void syncPlayerRankToClients(UUID playerUUID) {
        try {
            // Trouver le joueur par son UUID
            net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                if (player != null) {
                    fr.eriniumgroup.erinium_faction.events.RankSyncEventHandler.syncPlayerRankToAll(player);
                }
            }
        } catch (Exception e) {
            // Ignore si le serveur n'est pas disponible ou si le joueur n'est pas connecté
        }
    }

    public synchronized String getPlayerRankId(UUID uuid) {
        return uuid == null ? null : playerRanks.get(uuid);
    }

    public synchronized Rank getPlayerRank(UUID uuid) {
        String id = getPlayerRankId(uuid);
        return id == null ? null : getRank(id);
    }

    // Overrides par joueur ----------------------------------------------------

    public synchronized boolean addPlayerPermission(UUID uuid, String perm) {
        if (uuid == null || perm == null || perm.isBlank()) return false;
        Set<String> set = playerOverrides.computeIfAbsent(uuid, k -> new HashSet<>());
        boolean changed = set.add(perm);
        if (changed) savePlayers();
        return changed;
    }

    public synchronized boolean removePlayerPermission(UUID uuid, String perm) {
        if (uuid == null || perm == null || perm.isBlank()) return false;
        Set<String> set = playerOverrides.get(uuid);
        if (set == null) return false;
        boolean changed = set.remove(perm);
        if (changed) savePlayers();
        if (set.isEmpty()) playerOverrides.remove(uuid);
        return changed;
    }

    public synchronized Set<String> listPlayerPermissions(UUID uuid) {
        Set<String> set = playerOverrides.get(uuid);
        return set == null ? Collections.emptySet() : new HashSet<>(set);
    }

    public boolean hasPermission(ServerPlayer player, String node) {
        if (player == null || node == null || node.isBlank()) return false;
        // OP a tous les droits
        if (player.hasPermissions(2)) return true;
        Rank r;
        Set<String> overrides;
        synchronized (this) {
            r = getPlayerRank(player.getUUID());
            overrides = listPlayerPermissions(player.getUUID());
        }
        // Check overrides joueur
        if (!overrides.isEmpty()) {
            if (overrides.contains("*")) return true;
            if (overrides.contains(node)) return true;
            if (hasPermissionWithValue(overrides, node)) return true;
            if (matchWildcard(overrides, node)) return true;
        }
        // Check rank
        if (r == null) return false;
        if (r.permissions.contains("*")) return true;
        if (r.permissions.contains(node)) return true;
        if (hasPermissionWithValue(r.permissions, node)) return true;
        if (matchWildcard(r.permissions, node)) return true;
        return false;
    }

    /**
     * Vérifie si une permission avec une valeur existe dans l'ensemble de permissions.
     * Par exemple, si perms contient "ef.home.max:3" et node est "ef.home.max", retourne true.
     */
    private boolean hasPermissionWithValue(Set<String> perms, String node) {
        for (String perm : perms) {
            if (perm.startsWith(node + ":")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Récupère la valeur d'une permission sous forme de Double.
     * Retourne null si la permission n'existe pas ou n'a pas de valeur.
     *
     * @param player Le joueur
     * @param node Le nœud de permission (ex: "ef.home.max")
     * @return La valeur en Double, ou null si non trouvée
     */
    public Double getPermissionValueDouble(ServerPlayer player, String node) {
        if (player == null || node == null || node.isBlank()) return null;

        Rank r;
        Set<String> overrides;
        synchronized (this) {
            r = getPlayerRank(player.getUUID());
            overrides = listPlayerPermissions(player.getUUID());
        }

        // Check overrides joueur d'abord (priorité)
        if (!overrides.isEmpty()) {
            Double val = extractPermissionValue(overrides, node);
            if (val != null) return val;
        }

        // Check rank
        if (r != null) {
            Double val = extractPermissionValue(r.permissions, node);
            if (val != null) return val;
        }

        return null;
    }

    /**
     * Récupère la valeur d'une permission sous forme d'Integer.
     * Retourne null si la permission n'existe pas ou n'a pas de valeur.
     *
     * @param player Le joueur
     * @param node Le nœud de permission (ex: "ef.home.max")
     * @return La valeur en Integer, ou null si non trouvée
     */
    public Integer getPermissionValueInt(ServerPlayer player, String node) {
        Double val = getPermissionValueDouble(player, node);
        return val == null ? null : val.intValue();
    }

    /**
     * Extrait la valeur numérique d'une permission depuis un ensemble de permissions.
     * Cherche une permission au format "node:valeur" (ex: "ef.home.max:3")
     */
    private Double extractPermissionValue(Set<String> perms, String node) {
        for (String perm : perms) {
            if (perm.startsWith(node + ":")) {
                String valuePart = perm.substring(node.length() + 1);
                try {
                    return Double.parseDouble(valuePart);
                } catch (NumberFormatException e) {
                    // Ignorer les valeurs non numériques
                }
            }
        }
        return null;
    }

    private boolean matchWildcard(Set<String> perms, String node) {
        int dot;
        String current = node;
        while ((dot = current.lastIndexOf('.')) > 0) {
            current = current.substring(0, dot) + ".*";
            if (perms.contains(current)) return true;
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

    // Collecte de permissions connues (ranks + overrides existants)
    public synchronized Set<String> getKnownPermissions() {
        Set<String> set = new HashSet<>();
        for (Rank r : ranks.values()) if (r.permissions != null) set.addAll(r.permissions);
        for (Set<String> s : playerOverrides.values()) set.addAll(s);

        // Permissions de base joueur
        set.add("player.place");
        set.add("player.break");
        set.add("player.interact");
        set.add("player.*");

        // Permissions de commandes serveur
        set.add("server.command.*");

        // Permissions EF Admin
        set.add("ef.admin");
        set.add("ef.rank.admin");
        set.add("ef.economy.admin");
        set.add("ef.playerlevel.admin");
        set.add("ef.topluck.admin");
        set.add("ef.topluck.config");
        set.add("ef.antixray.admin");
        set.add("ef.kit.admin");

        // Permissions faction
        set.add("ef.faction.*");
        set.add("ef.faction.desc");
        set.add("ef.faction.bank.deposit");
        set.add("ef.faction.bank.withdraw");
        set.add("ef.faction.home.tp");
        set.add("ef.faction.home.set");
        set.add("ef.faction.warp.tp");
        set.add("ef.faction.warp.add");
        set.add("ef.faction.warp.del");

        // Permissions avec valeurs (exemples)
        set.add("ef.home.max:3");
        set.add("ef.warp.max:5");
        set.add("ef.xp.multiplier:1.5");

        // Permissions exemple kits
        set.add("efr.example.kit.vip");

        return set;
    }
}
