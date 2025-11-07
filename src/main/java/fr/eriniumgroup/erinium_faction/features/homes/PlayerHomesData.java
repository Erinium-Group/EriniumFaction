package fr.eriniumgroup.erinium_faction.features.homes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Représente l'ensemble des homes d'un joueur
 */
public class PlayerHomesData {
    private final Map<String, HomeData> homes = new HashMap<>();

    public Optional<HomeData> getHome(String name) {
        return Optional.ofNullable(homes.get(name.toLowerCase()));
    }

    public void setHome(String name, HomeData home) {
        homes.put(name.toLowerCase(), home);
    }

    public boolean removeHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public Map<String, HomeData> getAllHomes() {
        return new HashMap<>(homes);
    }

    public int getHomeCount() {
        return homes.size();
    }

    /** Retourne vrai si le joueur a atteint la limite de homes selon la config. */
    public boolean hasMaxHomes(HomesConfig config) {
        return homes.size() >= config.getMaxHomesPerPlayer();
    }

    /** Retourne la limite de homes selon la config. */
    public int getMaxHomes(HomesConfig config) {
        return config.getMaxHomesPerPlayer();
    }

    public CompoundTag save(CompoundTag tag) {
        ListTag homesList = new ListTag();
        for (HomeData home : homes.values()) {
            CompoundTag homeTag = new CompoundTag();
            home.save(homeTag);
            homesList.add(homeTag);
        }
        tag.put("homes", homesList);
        return tag;
    }

    public static PlayerHomesData load(CompoundTag tag) {
        PlayerHomesData data = new PlayerHomesData();
        ListTag homesList = tag.getList("homes", Tag.TAG_COMPOUND);
        for (int i = 0; i < homesList.size(); i++) {
            CompoundTag homeTag = homesList.getCompound(i);
            HomeData home = HomeData.load(homeTag);
            data.homes.put(home.getName().toLowerCase(), home);
        }
        return data;
    }
}
