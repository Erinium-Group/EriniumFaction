package fr.eriniumgroup.erinium_faction.core.faction;

import net.minecraft.network.FriendlyByteBuf;

import java.util.*;

/**
 * Petit snapshot sérialisable pour UI client.
 */
public class FactionSnapshot {
    public String id;
    public String name;
    public String displayName;
    public UUID ownerUUID;
    public int claims;
    public int maxClaims;
    public int membersCount;
    public int maxPlayers;
    public int level;
    public int xp;
    public int xpRequired;
    public double currentPower;
    public double maxPower;

    public boolean admin;
    public boolean warzone;
    public boolean safezone;
    public String mode;
    public String description;
    public int warpsCount;
    public int maxWarps;
    public int bank; // arrondi pour affichage

    public int factionChestSize;

    public Map<UUID, String> membersRank = new HashMap<>();
    public Map<UUID, String> memberNames = new HashMap<>();
    public Map<UUID, Double> membersPower = new HashMap<>(); // Power de chaque membre
    public Map<UUID, Double> membersMaxPower = new HashMap<>(); // Max power de chaque membre
    public Map<UUID, Boolean> membersOnline = new HashMap<>(); // Status en ligne de chaque membre

    // Player power
    public double playerPower;
    public double playerMaxPower;

    // Allies
    public List<String> allies = new ArrayList<>();
    public List<String> allyRequests = new ArrayList<>(); // Factions qui ont demandé une alliance

    // Ranks definitions
    public List<RankInfo> ranks = new ArrayList<>();

    // Claims list
    public List<ClaimInfo> claimsList = new ArrayList<>();

    // Config limits
    public int nameMinLength;
    public int nameMaxLength;

    public static class RankInfo {
        public String id;
        public String display;
        public int priority;
        public List<String> perms = new ArrayList<>();

        public RankInfo() {}

        public RankInfo(String id, String display, int priority, Set<String> perms) {
            this.id = id;
            this.display = display;
            this.priority = priority;
            this.perms.addAll(perms);
        }
    }

    public static class ClaimInfo {
        public String dimension;
        public int chunkX;
        public int chunkZ;

        public ClaimInfo() {}

        public ClaimInfo(String dimension, int chunkX, int chunkZ) {
            this.dimension = dimension;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

    public static FactionSnapshot of(Faction f) {
        return of(f, null);
    }

    public static FactionSnapshot of(Faction f, net.minecraft.server.level.ServerPlayer viewer) {
        FactionSnapshot s = new FactionSnapshot();
        s.id = f.getId();
        s.name = f.getName();
        s.displayName = f.getName();
        s.ownerUUID = f.getOwner();
        // claims depuis SavedData
        s.claims = FactionManager.countClaims(f.getId());
        // maxClaims = nombre de membres * power max par joueur
        int memberCount = f.getMembers().size();
        double powerPerPlayer = fr.eriniumgroup.erinium_faction.common.config.EFConfig.PLAYER_BASE_MAX_POWER.get();
        s.maxClaims = (int) Math.floor(memberCount * powerPerPlayer);
        s.membersCount = memberCount;
        s.maxPlayers = FactionManager.getMaxMembersFor(f);
        s.level = f.getLevel();
        s.xp = f.getXp();
        s.xpRequired = f.xpNeededForNextLevel();
        // Garder les power en double pour l'affichage précis
        s.currentPower = f.getPower();
        s.maxPower = f.getMaxPower();
        s.admin = f.isAdminFaction();
        s.warzone = f.isWarzone();
        s.safezone = f.isSafezone();
        s.mode = f.getMode().name();
        s.description = f.getDescription();
        s.warpsCount = f.getWarps().size();
        s.maxWarps = f.getMaxWarps();
        s.bank = (int) Math.round(f.getBankBalance());
        s.factionChestSize = f.getChestSize();

        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        var profileCache = server != null ? server.getProfileCache() : null;

        // Player power (si viewer fourni)
        if (viewer != null) {
            var playerPower = fr.eriniumgroup.erinium_faction.core.power.PowerManager.get(viewer);
            s.playerPower = playerPower.getPower();
            s.playerMaxPower = playerPower.getMaxPower();
        }

        // Allies
        s.allies.addAll(f.getAllies());
        s.allyRequests.addAll(f.getAllyRequests());

        // Ranks definitions
        for (var rank : f.getRanks().values()) {
            s.ranks.add(new RankInfo(rank.id, rank.display, rank.priority, rank.perms));
        }

        // Claims list
        var claimKeys = FactionManager.getClaimsOfFaction(f.getId());
        for (var claimKey : claimKeys) {
            s.claimsList.add(new ClaimInfo(claimKey.dimension(), claimKey.chunkX(), claimKey.chunkZ()));
        }

        // Config limits
        s.nameMinLength = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_NAME_MIN.get();
        s.nameMaxLength = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_NAME_MAX.get();

        for (var e : f.getMembers().entrySet()) {
            UUID uuid = e.getKey();
            s.membersRank.put(uuid, e.getValue().rankId);
            String name = e.getValue().nameCached;
            boolean online = false;

            if (name == null || name.isBlank()) {
                if (server != null) {
                    var sp = server.getPlayerList().getPlayer(uuid);
                    if (sp != null) {
                        name = sp.getGameProfile().getName();
                        online = true;
                        // Récupérer le power du membre
                        var memberPower = fr.eriniumgroup.erinium_faction.core.power.PowerManager.get(sp);
                        s.membersPower.put(uuid, memberPower.getPower());
                        s.membersMaxPower.put(uuid, memberPower.getMaxPower());
                    } else if (profileCache != null) {
                        var opt = profileCache.get(uuid);
                        if (opt.isPresent()) name = opt.get().getName();
                    }
                }
            } else if (server != null) {
                // Vérifier si le joueur est en ligne même si on a un nom en cache
                var sp = server.getPlayerList().getPlayer(uuid);
                if (sp != null) {
                    online = true;
                    // Récupérer le power du membre
                    var memberPower = fr.eriniumgroup.erinium_faction.core.power.PowerManager.get(sp);
                    s.membersPower.put(uuid, memberPower.getPower());
                    s.membersMaxPower.put(uuid, memberPower.getMaxPower());
                }
            }

            // Si offline, utiliser des valeurs par défaut pour le power
            if (!online) {
                s.membersPower.put(uuid, 0.0);
                s.membersMaxPower.put(uuid, 0.0);
            }

            if (name == null || name.isBlank()) name = uuid.toString();
            s.memberNames.put(uuid, name);
            s.membersOnline.put(uuid, online);
        }
        return s;
    }

    public static void write(FactionSnapshot s, FriendlyByteBuf buf) {
        buf.writeUtf(s.id == null ? "" : s.id);
        buf.writeUtf(s.name == null ? "" : s.name);
        buf.writeUtf(s.displayName == null ? "" : s.displayName);
        buf.writeBoolean(s.ownerUUID != null);
        if (s.ownerUUID != null) {
            buf.writeUUID(s.ownerUUID);
        }
        buf.writeVarInt(s.claims);
        buf.writeVarInt(s.maxClaims);
        buf.writeVarInt(s.membersCount);
        buf.writeVarInt(s.maxPlayers);
        buf.writeVarInt(s.level);
        buf.writeVarInt(s.xp);
        buf.writeVarInt(s.xpRequired);
        buf.writeDouble(s.currentPower);
        buf.writeDouble(s.maxPower);
        buf.writeBoolean(s.admin);
        buf.writeBoolean(s.warzone);
        buf.writeBoolean(s.safezone);
        buf.writeUtf(s.mode == null ? "INVITE_ONLY" : s.mode);
        buf.writeUtf(s.description == null ? "" : s.description);
        buf.writeVarInt(s.warpsCount);
        buf.writeVarInt(s.maxWarps);
        buf.writeVarInt(s.bank);
        buf.writeVarInt(s.membersRank.size());
        for (var e : s.membersRank.entrySet()) {
            UUID uuid = e.getKey();
            buf.writeUUID(uuid);
            buf.writeUtf(e.getValue());
            buf.writeUtf(s.memberNames.getOrDefault(uuid, uuid.toString()));
            buf.writeDouble(s.membersPower.getOrDefault(uuid, 0.0));
            buf.writeDouble(s.membersMaxPower.getOrDefault(uuid, 0.0));
            buf.writeBoolean(s.membersOnline.getOrDefault(uuid, false));
        }
        buf.writeInt(s.factionChestSize);

        // Player power
        buf.writeDouble(s.playerPower);
        buf.writeDouble(s.playerMaxPower);

        // Allies
        buf.writeVarInt(s.allies.size());
        for (String ally : s.allies) {
            buf.writeUtf(ally);
        }

        // Ally requests
        buf.writeVarInt(s.allyRequests.size());
        for (String allyRequest : s.allyRequests) {
            buf.writeUtf(allyRequest);
        }

        // Ranks definitions
        buf.writeVarInt(s.ranks.size());
        for (RankInfo rank : s.ranks) {
            buf.writeUtf(rank.id);
            buf.writeUtf(rank.display);
            buf.writeVarInt(rank.priority);
            buf.writeVarInt(rank.perms.size());
            for (String perm : rank.perms) {
                buf.writeUtf(perm);
            }
        }

        // Claims list
        buf.writeVarInt(s.claimsList.size());
        for (ClaimInfo claim : s.claimsList) {
            buf.writeUtf(claim.dimension);
            buf.writeVarInt(claim.chunkX);
            buf.writeVarInt(claim.chunkZ);
        }

        // Config limits
        buf.writeVarInt(s.nameMinLength);
        buf.writeVarInt(s.nameMaxLength);
    }

    public static FactionSnapshot read(FriendlyByteBuf buf) {
        FactionSnapshot s = new FactionSnapshot();
        s.id = buf.readUtf();
        s.name = buf.readUtf();
        s.displayName = buf.readUtf();
        boolean hasOwner = buf.readBoolean();
        if (hasOwner) {
            s.ownerUUID = buf.readUUID();
        }
        s.claims = buf.readVarInt();
        s.maxClaims = buf.readVarInt();
        s.membersCount = buf.readVarInt();
        s.maxPlayers = buf.readVarInt();
        s.level = buf.readVarInt();
        s.xp = buf.readVarInt();
        s.xpRequired = buf.readVarInt();
        s.currentPower = buf.readDouble();
        s.maxPower = buf.readDouble();
        s.admin = buf.readBoolean();
        s.warzone = buf.readBoolean();
        s.safezone = buf.readBoolean();
        s.mode = buf.readUtf();
        s.description = buf.readUtf();
        s.warpsCount = buf.readVarInt();
        s.maxWarps = buf.readVarInt();
        s.bank = buf.readVarInt();
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            UUID id = buf.readUUID();
            String rank = buf.readUtf();
            String name = buf.readUtf();
            double power = buf.readDouble();
            double maxPower = buf.readDouble();
            boolean online = buf.readBoolean();
            s.membersRank.put(id, rank);
            s.memberNames.put(id, name);
            s.membersPower.put(id, power);
            s.membersMaxPower.put(id, maxPower);
            s.membersOnline.put(id, online);
        }
        s.factionChestSize = buf.readInt();

        // Player power
        s.playerPower = buf.readDouble();
        s.playerMaxPower = buf.readDouble();

        // Allies
        int alliesCount = buf.readVarInt();
        for (int i = 0; i < alliesCount; i++) {
            s.allies.add(buf.readUtf());
        }

        // Ally requests
        int allyRequestsCount = buf.readVarInt();
        for (int i = 0; i < allyRequestsCount; i++) {
            s.allyRequests.add(buf.readUtf());
        }

        // Ranks definitions
        int ranksCount = buf.readVarInt();
        for (int i = 0; i < ranksCount; i++) {
            RankInfo rank = new RankInfo();
            rank.id = buf.readUtf();
            rank.display = buf.readUtf();
            rank.priority = buf.readVarInt();
            int permsCount = buf.readVarInt();
            for (int j = 0; j < permsCount; j++) {
                rank.perms.add(buf.readUtf());
            }
            s.ranks.add(rank);
        }

        // Claims list
        int claimsCount = buf.readVarInt();
        for (int i = 0; i < claimsCount; i++) {
            ClaimInfo claim = new ClaimInfo();
            claim.dimension = buf.readUtf();
            claim.chunkX = buf.readVarInt();
            claim.chunkZ = buf.readVarInt();
            s.claimsList.add(claim);
        }

        // Config limits
        s.nameMinLength = buf.readVarInt();
        s.nameMaxLength = buf.readVarInt();

        return s;
    }

    // ===== Permission checks =====

    /**
     * Vérifie si un joueur a une permission dans la faction
     * @param playerUUID UUID du joueur
     * @param permission Enum Permission
     * @return true si le joueur a la permission
     */
    public boolean hasPermission(UUID playerUUID, Permission permission) {
        return hasPermission(playerUUID, permission.getServerKey());
    }

    /**
     * Vérifie si un joueur a une permission dans la faction
     * @param playerUUID UUID du joueur
     * @param permission Clé de permission (string)
     * @return true si le joueur a la permission
     */
    public boolean hasPermission(UUID playerUUID, String permission) {
        // Vérifier si le joueur est le owner (a toutes les permissions)
        if (ownerUUID != null && ownerUUID.equals(playerUUID)) {
            return true;
        }

        // Récupérer le rank du joueur
        String rankId = membersRank.get(playerUUID);
        if (rankId == null) {
            return false; // Joueur pas dans la faction
        }

        // Chercher le rank dans les définitions
        for (RankInfo rank : ranks) {
            if (rank.id.equals(rankId)) {
                // Vérifier si le rank a la permission
                return rank.perms.contains(permission);
            }
        }

        return false; // Rank introuvable
    }
}
