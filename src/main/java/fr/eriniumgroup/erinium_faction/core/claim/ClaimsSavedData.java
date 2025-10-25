package fr.eriniumgroup.erinium_faction.core.claim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class ClaimsSavedData extends SavedData {
    public static final String ID = "erinium_faction_claims";

    // key -> factionId (lowercase id)
    private final Map<ClaimKey, String> claims = new HashMap<>();

    public static ClaimsSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(ClaimsSavedData::new, ClaimsSavedData::load, null), ID);
    }

    public static ClaimsSavedData load(CompoundTag nbt, HolderLookup.Provider lookup) {
        ClaimsSavedData data = new ClaimsSavedData();
        ListTag list = nbt.getList("claims", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag ct = list.getCompound(i);
            String dim = ct.getString("dim");
            int cx = ct.getInt("cx");
            int cz = ct.getInt("cz");
            String owner = ct.getString("owner");
            data.claims.put(new ClaimKey(dim, cx, cz), owner);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<ClaimKey, String> e : claims.entrySet()) {
            CompoundTag ct = new CompoundTag();
            ct.putString("dim", e.getKey().dimension());
            ct.putInt("cx", e.getKey().chunkX());
            ct.putInt("cz", e.getKey().chunkZ());
            ct.putString("owner", e.getValue());
            list.add(ct);
        }
        nbt.put("claims", list);
        return nbt;
    }

    public boolean isClaimed(ClaimKey key) { return claims.containsKey(key); }
    public String getOwner(ClaimKey key) { return claims.get(key); }

    public int countClaims(String factionId) {
        int c = 0;
        for (String v : claims.values()) if (Objects.equals(v, factionId)) c++;
        return c;
    }

    public boolean claim(ClaimKey key, String factionId) {
        if (claims.containsKey(key)) return false;
        claims.put(key, factionId);
        setDirty();
        return true;
    }

    public boolean unclaim(ClaimKey key, String factionId) {
        if (!Objects.equals(claims.get(key), factionId)) return false;
        claims.remove(key);
        setDirty();
        return true;
    }

    // --- Nouveau: lister les claims dans un rectangle de chunks ---
    public List<Map.Entry<ClaimKey, String>> listInArea(String dimension, int minCx, int minCz, int maxCx, int maxCz) {
        List<Map.Entry<ClaimKey, String>> out = new ArrayList<>();
        for (Map.Entry<ClaimKey, String> e : claims.entrySet()) {
            ClaimKey k = e.getKey();
            if (!Objects.equals(k.dimension(), dimension)) continue;
            if (k.chunkX() >= minCx && k.chunkX() <= maxCx && k.chunkZ() >= minCz && k.chunkZ() <= maxCz) {
                out.add(e);
            }
        }
        return out;
    }
}
