package fr.eriniumgroup.erinium_faction.core.faction;

import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Snapshot immuable de données de faction pour l'affichage client.
 */
public class FactionSnapshot {
    public final String name;
    public final String displayName;
    public final int claims;
    public final int maxClaims;
    public final int membersCount;
    public final int maxPlayers;
    public final int level;
    public final int xp;
    public final int xpRequired;
    public final int currentPower;
    public final int maxPower;
    public final Map<UUID, String> membersRank; // UUID -> Rank name
    public final Map<UUID, String> memberNames; // UUID -> display name

    public FactionSnapshot(String name,
                           String displayName,
                           int claims,
                           int maxClaims,
                           int membersCount,
                           int maxPlayers,
                           int level,
                           int xp,
                           int xpRequired,
                           int currentPower,
                           int maxPower,
                           Map<UUID, String> membersRank,
                           Map<UUID, String> memberNames) {
        this.name = name;
        this.displayName = displayName;
        this.claims = claims;
        this.maxClaims = maxClaims;
        this.membersCount = membersCount;
        this.maxPlayers = maxPlayers;
        this.level = level;
        this.xp = xp;
        this.xpRequired = xpRequired;
        this.currentPower = currentPower;
        this.maxPower = maxPower;
        this.membersRank = membersRank != null ? Map.copyOf(membersRank) : Map.of();
        this.memberNames = memberNames != null ? Map.copyOf(memberNames) : Map.of();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name != null ? name : "");
        buf.writeUtf(displayName != null ? displayName : "");
        buf.writeInt(claims);
        buf.writeInt(maxClaims);
        buf.writeInt(membersCount);
        buf.writeInt(maxPlayers);
        buf.writeInt(level);
        buf.writeInt(xp);
        buf.writeInt(xpRequired);
        buf.writeInt(currentPower);
        buf.writeInt(maxPower);
        buf.writeVarInt(membersRank.size());
        for (var e : membersRank.entrySet()) {
            buf.writeUUID(e.getKey());
            buf.writeUtf(e.getValue());
        }
        buf.writeVarInt(memberNames.size());
        for (var e : memberNames.entrySet()) {
            buf.writeUUID(e.getKey());
            buf.writeUtf(e.getValue());
        }
    }

    public static FactionSnapshot read(FriendlyByteBuf buf) {
        String name = buf.readUtf(32767);
        String displayName = buf.readUtf(32767);
        int claims = buf.readInt();
        int maxClaims = buf.readInt();
        int membersCount = buf.readInt();
        int maxPlayers = buf.readInt();
        int level = buf.readInt();
        int xp = buf.readInt();
        int xpRequired = buf.readInt();
        int currentPower = buf.readInt();
        int maxPower = buf.readInt();
        int size = buf.readVarInt();
        Map<UUID, String> membersRank = new LinkedHashMap<>(Math.max(1, size));
        for (int i = 0; i < size; i++) {
            UUID id = buf.readUUID();
            String rank = buf.readUtf(32767);
            membersRank.put(id, rank);
        }
        int sizeNames = buf.readVarInt();
        Map<UUID, String> memberNames = new LinkedHashMap<>(Math.max(1, sizeNames));
        for (int i = 0; i < sizeNames; i++) {
            UUID id = buf.readUUID();
            String nameStr = buf.readUtf(32767);
            memberNames.put(id, nameStr);
        }
        return new FactionSnapshot(name, displayName, claims, maxClaims, membersCount, maxPlayers, level, xp, xpRequired, currentPower, maxPower, membersRank, memberNames);
    }
}
