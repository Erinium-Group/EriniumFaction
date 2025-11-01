package fr.eriniumgroup.erinium_faction.features.topluck;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class TopLuckPlacedData extends SavedData {
    private static final String DATA_NAME = "erinium_faction-topluck_placed";
    private final LongOpenHashSet placed = new LongOpenHashSet();

    public static TopLuckPlacedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(TopLuckPlacedData::new, TopLuckPlacedData::load, null), DATA_NAME);
    }

    public TopLuckPlacedData() {}

    public static TopLuckPlacedData load(CompoundTag nbt, HolderLookup.Provider provider) {
        TopLuckPlacedData d = new TopLuckPlacedData();
        long[] arr = nbt.getLongArray("placed");
        for (long v : arr) d.placed.add(v);
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        long[] arr = placed.toLongArray();
        tag.putLongArray("placed", arr);
        return tag;
    }

    public void markPlaced(BlockPos pos) {
        placed.add(pos.asLong());
        setDirty();
    }

    public boolean isPlaced(BlockPos pos) {
        return placed.contains(pos.asLong());
    }

    public void clear(BlockPos pos) {
        if (placed.remove(pos.asLong())) setDirty();
    }
}

