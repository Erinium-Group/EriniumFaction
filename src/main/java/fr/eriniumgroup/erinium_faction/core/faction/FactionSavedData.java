package fr.eriniumgroup.erinium_faction.core.faction;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persistance monde: world/data/erinium_faction_factions.dat
 */
public class FactionSavedData extends SavedData {
    public static final String DATA_NAME = "erinium_faction_factions";

    private static final SavedData.Factory<FactionSavedData> FACTORY = new SavedData.Factory<>(FactionSavedData::new, // constructeur par défaut si pas de fichier
            FactionSavedData::load, // lecteur NBT -> instance
            null // DataFixTypes si besoin (null si non utilisé)
    );

    private final Map<String, Faction> factions = new LinkedHashMap<>();

    public Map<String, Faction> getFactions() {
        return factions;
    }

    public static FactionSavedData get(MinecraftServer server) {
        var storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(FACTORY, DATA_NAME);
    }

    public FactionSavedData() {
    }

    public static FactionSavedData load(CompoundTag nbt, HolderLookup.Provider provider) {
        FactionSavedData data = new FactionSavedData();
        var list = nbt.getList("factions", 10);
        for (int i = 0; i < list.size(); i++) {
            var t = list.getCompound(i);
            Faction f = Faction.load(t, provider);
            data.factions.put(f.getId(), f);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Faction f : factions.values()) {
            list.add(f.save(provider));
        }
        nbt.put("factions", list);
        return nbt;
    }
}
