package fr.eriniumgroup.erinium_faction.features.topluck;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Données TopLuck par joueur: compte le nombre de blocs minés par ID de bloc.
 * Stockage compact (id -> count) et méthodes utilitaires.
 */
public class PlayerTopLuckData implements INBTSerializable<CompoundTag> {
    private final Map<String, Long> minedCounts = new HashMap<>();

    public void increment(String blockId) {
        if (blockId == null || blockId.isEmpty()) return;
        minedCounts.merge(blockId, 1L, Long::sum);
    }

    public long getCount(String blockId) {
        return minedCounts.getOrDefault(blockId, 0L);
    }

    public Map<String, Long> getAll() {
        return minedCounts;
    }

    public long getTotal() {
        long total = 0L;
        for (long v : minedCounts.values()) total += v;
        return total;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag counts = new CompoundTag();
        for (var e : minedCounts.entrySet()) {
            counts.putLong(e.getKey(), e.getValue());
        }
        tag.put("counts", counts);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        minedCounts.clear();
        if (nbt == null) return;
        CompoundTag counts = nbt.getCompound("counts");
        for (String key : counts.getAllKeys()) {
            minedCounts.put(key, counts.getLong(key));
        }
    }
}

