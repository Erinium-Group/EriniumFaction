package fr.eriniumgroup.erinium_faction.core.claim;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class ClaimsSavedData extends SavedData {
    public static final String ID = "erinium_faction_claims";

    // key -> factionId (lowercase id)
    private final Map<ClaimKey, String> claims = new HashMap<>();

    // Nouveau: permissions spécifiques au claim (par rang de la faction propriétaire)
    // key -> (rankId -> set(perms))
    private final Map<ClaimKey, Map<String, Set<String>>> claimPerms = new HashMap<>();

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
        // Charger les permissions par claim (optionnel)
        if (nbt.contains("claimPerms", 9)) { // 9 = List
            ListTag lp = nbt.getList("claimPerms", 10); // list de compound
            for (int i = 0; i < lp.size(); i++) {
                CompoundTag ct = lp.getCompound(i);
                String dim = ct.getString("dim");
                int cx = ct.getInt("cx");
                int cz = ct.getInt("cz");
                String rank = ct.getString("rank");
                ListTag perms = ct.getList("perms", 8);
                ClaimKey key = new ClaimKey(dim, cx, cz);
                Map<String, Set<String>> byRank = data.claimPerms.computeIfAbsent(key, k -> new HashMap<>());
                Set<String> set = byRank.computeIfAbsent(rank.toLowerCase(Locale.ROOT), r -> new LinkedHashSet<>());
                for (int j = 0; j < perms.size(); j++) set.add(perms.getString(j));
            }
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
        // Sauver les permissions par claim
        if (!claimPerms.isEmpty()) {
            ListTag lp = new ListTag();
            for (Map.Entry<ClaimKey, Map<String, Set<String>>> e : claimPerms.entrySet()) {
                ClaimKey k = e.getKey();
                Map<String, Set<String>> byRank = e.getValue();
                for (Map.Entry<String, Set<String>> r : byRank.entrySet()) {
                    CompoundTag ct = new CompoundTag();
                    ct.putString("dim", k.dimension());
                    ct.putInt("cx", k.chunkX());
                    ct.putInt("cz", k.chunkZ());
                    ct.putString("rank", r.getKey());
                    ListTag perms = new ListTag();
                    for (String p : r.getValue()) perms.add(StringTag.valueOf(p));
                    ct.put("perms", perms);
                    lp.add(ct);
                }
            }
            nbt.put("claimPerms", lp);
        }
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

    /** Supprime tous les claims d’une faction. Renvoie le nombre d’entrées supprimées. */
    public int unclaimAllForFaction(String factionId) {
        if (factionId == null || factionId.isBlank()) return 0;
        int removed = 0;
        Iterator<Map.Entry<ClaimKey, String>> it = claims.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ClaimKey, String> e = it.next();
            if (Objects.equals(e.getValue(), factionId)) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) setDirty();
        return removed;
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

    // --- Nouveau: lister tous les ClaimKey d'une faction ---
    public List<ClaimKey> listClaimsForFaction(String factionId) {
        if (factionId == null || factionId.isBlank()) return Collections.emptyList();
        List<ClaimKey> out = new ArrayList<>();
        for (Map.Entry<ClaimKey, String> e : claims.entrySet()) {
            if (Objects.equals(e.getValue(), factionId)) {
                out.add(e.getKey());
            }
        }
        return out;
    }

    // --- Nouveau: permissions de claim ---
    public Map<String, Set<String>> getClaimPerms(ClaimKey key) {
        Map<String, Set<String>> m = claimPerms.get(key);
        return m == null ? Collections.emptyMap() : Collections.unmodifiableMap(m);
    }

    public Set<String> getClaimPermsForRank(ClaimKey key, String rankId) {
        if (rankId == null) return Collections.emptySet();
        Map<String, Set<String>> m = claimPerms.get(key);
        if (m == null) return Collections.emptySet();
        Set<String> s = m.get(rankId.toLowerCase(Locale.ROOT));
        return s == null ? Collections.emptySet() : Collections.unmodifiableSet(s);
    }

    public boolean addClaimPerm(ClaimKey key, String rankId, String perm) {
        if (key == null || rankId == null || perm == null || perm.isBlank()) return false;
        Map<String, Set<String>> m = claimPerms.computeIfAbsent(key, k -> new HashMap<>());
        Set<String> s = m.computeIfAbsent(rankId.toLowerCase(Locale.ROOT), r -> new LinkedHashSet<>());
        boolean changed = s.add(perm.trim());
        if (changed) setDirty();
        return changed;
    }

    public boolean removeClaimPerm(ClaimKey key, String rankId, String perm) {
        if (key == null || rankId == null || perm == null || perm.isBlank()) return false;
        Map<String, Set<String>> m = claimPerms.get(key);
        if (m == null) return false;
        Set<String> s = m.get(rankId.toLowerCase(Locale.ROOT));
        if (s == null) return false;
        boolean changed = s.remove(perm.trim());
        if (changed) setDirty();
        return changed;
    }

    public boolean clearClaimPerms(ClaimKey key) {
        if (key == null) return false;
        if (claimPerms.remove(key) != null) { setDirty(); return true; }
        return false;
    }

    // util matchmaking wildcards pour ClaimProtection
    public static boolean matches(Set<String> perms, String node) {
        if (perms.contains("*")) return true;
        if (perms.contains(node)) return true;
        String cur = node;
        while (true) {
            int i = cur.lastIndexOf('.');
            if (i < 0) break;
            cur = cur.substring(0, i) + ".*";
            if (perms.contains(cur)) return true;
            cur = cur.substring(0, cur.length() - 2);
        }
        return false;
    }
}
