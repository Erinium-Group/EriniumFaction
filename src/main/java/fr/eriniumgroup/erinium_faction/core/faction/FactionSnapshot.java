package fr.eriniumgroup.erinium_faction.core.faction;

import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Petit snapshot sérialisable pour UI client.
 */
public class FactionSnapshot {
    public String name;
    public String displayName;
    public int claims;
    public int maxClaims;
    public int membersCount;
    public int maxPlayers;
    public int level;
    public int xp;
    public int xpRequired;
    public int currentPower;
    public int maxPower;

    public boolean admin;
    public boolean warzone;
    public boolean safezone;
    public String mode;
    public String description;
    public int warpsCount;
    public int maxWarps;
    public int bank; // arrondi pour affichage

    public Map<UUID, String> membersRank = new HashMap<>();
    public Map<UUID, String> memberNames = new HashMap<>();

    public static FactionSnapshot of(Faction f) {
        FactionSnapshot s = new FactionSnapshot();
        s.name = f.getName();
        s.displayName = f.getName();
        // claims depuis SavedData
        s.claims = FactionManager.countClaims(f.getId());
        s.maxClaims = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_MAX_CLAIMS.get();
        s.membersCount = f.getMembers().size();
        s.maxPlayers = FactionManager.getMaxMembersFor(f);
        s.level = f.getLevel();
        s.xp = f.getXp();
        s.xpRequired = f.xpNeededForNextLevel();
        s.currentPower = (int) Math.round(f.getPower());
        s.maxPower = (int) Math.round(f.getMaxPower());
        s.admin = f.isAdminFaction();
        s.warzone = f.isWarzone();
        s.safezone = f.isSafezone();
        s.mode = f.getMode().name();
        s.description = f.getDescription();
        s.warpsCount = f.getWarps().size();
        s.maxWarps = f.getMaxWarps();
        s.bank = (int) Math.round(f.getBankBalance());

        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        var profileCache = server != null ? server.getProfileCache() : null;

        for (var e : f.getMembers().entrySet()) {
            UUID uuid = e.getKey();
            s.membersRank.put(uuid, e.getValue().rankId);
            String name = e.getValue().nameCached;
            if (name == null || name.isBlank()) {
                if (server != null) {
                    var sp = server.getPlayerList().getPlayer(uuid);
                    if (sp != null) {
                        name = sp.getGameProfile().getName();
                    } else if (profileCache != null) {
                        var opt = profileCache.get(uuid);
                        if (opt.isPresent()) name = opt.get().getName();
                    }
                }
            }
            if (name == null || name.isBlank()) name = uuid.toString();
            s.memberNames.put(uuid, name);
        }
        return s;
    }

    public static void write(FactionSnapshot s, FriendlyByteBuf buf) {
        buf.writeUtf(s.name == null ? "" : s.name);
        buf.writeUtf(s.displayName == null ? "" : s.displayName);
        buf.writeVarInt(s.claims);
        buf.writeVarInt(s.maxClaims);
        buf.writeVarInt(s.membersCount);
        buf.writeVarInt(s.maxPlayers);
        buf.writeVarInt(s.level);
        buf.writeVarInt(s.xp);
        buf.writeVarInt(s.xpRequired);
        buf.writeVarInt(s.currentPower);
        buf.writeVarInt(s.maxPower);
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
            buf.writeUUID(e.getKey());
            buf.writeUtf(e.getValue());
            buf.writeUtf(s.memberNames.getOrDefault(e.getKey(), e.getKey().toString()));
        }
    }

    public static FactionSnapshot read(FriendlyByteBuf buf) {
        FactionSnapshot s = new FactionSnapshot();
        s.name = buf.readUtf();
        s.displayName = buf.readUtf();
        s.claims = buf.readVarInt();
        s.maxClaims = buf.readVarInt();
        s.membersCount = buf.readVarInt();
        s.maxPlayers = buf.readVarInt();
        s.level = buf.readVarInt();
        s.xp = buf.readVarInt();
        s.xpRequired = buf.readVarInt();
        s.currentPower = buf.readVarInt();
        s.maxPower = buf.readVarInt();
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
            s.membersRank.put(id, rank);
            s.memberNames.put(id, name);
        }
        return s;
    }
}
