package fr.eriniumgroup.erinium_faction.features.homes;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * SavedData pour la persistence des homes en .dat
 */
public class HomesSavedData extends SavedData {
    public static final String DATA_NAME = "erinium_faction-homes";

    private final Map<UUID, PlayerHomesData> playerHomes = new HashMap<>();

    public Optional<PlayerHomesData> getPlayerHomes(UUID playerUUID) {
        return Optional.ofNullable(playerHomes.get(playerUUID));
    }

    public PlayerHomesData getOrCreatePlayerHomes(UUID playerUUID) {
        return playerHomes.computeIfAbsent(playerUUID, k -> new PlayerHomesData());
    }

    public static HomesSavedData get(MinecraftServer server) {
        var storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(HomesSavedData::new, HomesSavedData::load, null),
                DATA_NAME
        );
    }

    public HomesSavedData() {}

    public static HomesSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        HomesSavedData data = new HomesSavedData();
        for (String key : tag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                CompoundTag playerTag = tag.getCompound(key);
                PlayerHomesData playerData = PlayerHomesData.load(playerTag);
                data.playerHomes.put(uuid, playerData);
            } catch (IllegalArgumentException ignored) {
                // UUID invalide, ignorer
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        for (Map.Entry<UUID, PlayerHomesData> entry : playerHomes.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            entry.getValue().save(playerTag);
            tag.put(entry.getKey().toString(), playerTag);
        }
        return tag;
    }
}

