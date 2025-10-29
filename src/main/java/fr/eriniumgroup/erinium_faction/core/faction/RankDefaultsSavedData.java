package fr.eriniumgroup.erinium_faction.core.faction;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Sauvegarde monde (.dat) des permissions par défaut des rangs de faction.
 * Fichier: data/erinium_faction_rank_defaults.dat
 */
public class RankDefaultsSavedData extends SavedData {
    public static final String ID = "erinium_faction_rank_defaults";

    // rankId -> set de permissions
    private final Map<String, Set<String>> defaults = new LinkedHashMap<>();

    public static RankDefaultsSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(RankDefaultsSavedData::new, RankDefaultsSavedData::load, null), ID);
    }

    public static RankDefaultsSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        RankDefaultsSavedData data = new RankDefaultsSavedData();
        ListTag list = tag.getList("ranks", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag rt = list.getCompound(i);
            String id = rt.getString("id");
            Set<String> perms = new LinkedHashSet<>();
            ListTag lp = rt.getList("perms", 8);
            for (int j = 0; j < lp.size(); j++) perms.add(lp.getString(j));
            if (!id.isBlank()) data.defaults.put(id.toLowerCase(Locale.ROOT), perms);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<String, Set<String>> e : defaults.entrySet()) {
            CompoundTag rt = new CompoundTag();
            rt.putString("id", e.getKey());
            ListTag lp = new ListTag();
            for (String p : e.getValue()) lp.add(StringTag.valueOf(p));
            rt.put("perms", lp);
            list.add(rt);
        }
        tag.put("ranks", list);
        return tag;
    }

    /** Initialise des valeurs par défaut si la sauvegarde est vide. */
    public void ensureDefaultsInitialized() {
        if (!defaults.isEmpty()) return;
        // Defaults pensés pour démarrer: ajustables en éditant le .dat
        defaults.put("leader", new LinkedHashSet<>(List.of("*")));
        defaults.put("officer", new LinkedHashSet<>(List.of(
                "faction.invite",
                "faction.kick",
                "faction.claim",
                "faction.unclaim",
                "faction.manage.warps",
                "faction.bank.withdraw"
        )));
        defaults.put("member", new LinkedHashSet<>(List.of(
                "faction.chat",
                "faction.use.warp",
                "faction.bank.deposit",
                "block.break",
                "block.place",
                "block.interact"
        )));
        defaults.put("recruit", new LinkedHashSet<>(List.of(
                "faction.chat",
                "block.break",
                "block.place",
                "block.interact"
        )));
        setDirty();
    }

    public Set<String> getDefaultsFor(String rankId) {
        if (rankId == null) return Collections.emptySet();
        Set<String> s = defaults.get(rankId.toLowerCase(Locale.ROOT));
        return s == null ? Collections.emptySet() : Collections.unmodifiableSet(s);
    }

    /** Applique les permissions par défaut aux rangs vides de la faction. */
    public int applyDefaultsIfEmpty(Faction faction) {
        if (faction == null) return 0;
        int changes = 0;
        for (Map.Entry<String, Faction.RankDef> e : faction.getRanks().entrySet()) {
            Faction.RankDef r = e.getValue();
            if (r.perms == null || r.perms.isEmpty()) {
                Set<String> def = defaults.get(e.getKey());
                if (def != null && !def.isEmpty()) {
                    r.perms.clear();
                    r.perms.addAll(def);
                    changes++;
                }
            }
        }
        if (changes > 0) {
            // Marquer les factions comme modifiées
            fr.eriniumgroup.erinium_faction.core.faction.FactionManager.markDirty();
        }
        return changes;
    }

    /** Applique les defaults aux factions dont les rangs sont vides. */
    public int applyDefaultsToAllFactionsIfEmpty() {
        int total = 0;
        for (Faction f : fr.eriniumgroup.erinium_faction.core.faction.FactionManager.getAllFactions()) {
            total += applyDefaultsIfEmpty(f);
        }
        return total;
    }

    /** Bootstrap: initialise si vide et applique aux factions (si rangs vides). */
    public static void bootstrapAndApply(MinecraftServer server) {
        RankDefaultsSavedData d = get(server);
        d.ensureDefaultsInitialized();
        int applied = d.applyDefaultsToAllFactionsIfEmpty();
        if (applied > 0) d.setDirty();
    }
}
