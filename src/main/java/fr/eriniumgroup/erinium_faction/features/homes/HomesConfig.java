package fr.eriniumgroup.erinium_faction.features.homes;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration customisable du système de homes
 * Stockée en SavedData pour persistence
 */
public class HomesConfig extends SavedData {
    public static final String DATA_NAME = "erinium_faction-homes-config";

    private int maxHomesPerPlayer = 10;
    private int minHomeName = 1;
    private int maxHomeName = 32;
    private boolean allowCrossDimensionTeleport = true;
    private boolean requireOpToDeleteOthersHomes = true;
    private int warmupSeconds = 5;
    private int cooldownSeconds = 10;

    public HomesConfig() {}

    public static HomesConfig get(MinecraftServer server) {
        var storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(HomesConfig::new, HomesConfig::load, null),
                DATA_NAME
        );
    }

    public static HomesConfig load(CompoundTag tag, HolderLookup.Provider provider) {
        HomesConfig config = new HomesConfig();
        if (tag.contains("maxHomesPerPlayer")) config.maxHomesPerPlayer = tag.getInt("maxHomesPerPlayer");
        if (tag.contains("minHomeName")) config.minHomeName = tag.getInt("minHomeName");
        if (tag.contains("maxHomeName")) config.maxHomeName = tag.getInt("maxHomeName");
        if (tag.contains("allowCrossDimensionTeleport")) config.allowCrossDimensionTeleport = tag.getBoolean("allowCrossDimensionTeleport");
        if (tag.contains("requireOpToDeleteOthersHomes")) config.requireOpToDeleteOthersHomes = tag.getBoolean("requireOpToDeleteOthersHomes");
        if (tag.contains("warmupSeconds")) config.warmupSeconds = tag.getInt("warmupSeconds");
        if (tag.contains("cooldownSeconds")) config.cooldownSeconds = tag.getInt("cooldownSeconds");
        return config;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        tag.putInt("maxHomesPerPlayer", maxHomesPerPlayer);
        tag.putInt("minHomeName", minHomeName);
        tag.putInt("maxHomeName", maxHomeName);
        tag.putBoolean("allowCrossDimensionTeleport", allowCrossDimensionTeleport);
        tag.putBoolean("requireOpToDeleteOthersHomes", requireOpToDeleteOthersHomes);
        tag.putInt("warmupSeconds", warmupSeconds);
        tag.putInt("cooldownSeconds", cooldownSeconds);
        return tag;
    }

    // Getters et Setters

    public int getMaxHomesPerPlayer() {
        return maxHomesPerPlayer;
    }

    public void setMaxHomesPerPlayer(int max) {
        this.maxHomesPerPlayer = Math.max(1, max);
        setDirty();
    }

    public int getMinHomeName() {
        return minHomeName;
    }

    public void setMinHomeName(int min) {
        this.minHomeName = Math.max(1, min);
        setDirty();
    }

    public int getMaxHomeName() {
        return maxHomeName;
    }

    public void setMaxHomeName(int max) {
        this.maxHomeName = Math.max(1, max);
        setDirty();
    }

    public boolean isAllowCrossDimensionTeleport() {
        return allowCrossDimensionTeleport;
    }

    public void setAllowCrossDimensionTeleport(boolean allow) {
        this.allowCrossDimensionTeleport = allow;
        setDirty();
    }

    public boolean isRequireOpToDeleteOthersHomes() {
        return requireOpToDeleteOthersHomes;
    }

    public void setRequireOpToDeleteOthersHomes(boolean require) {
        this.requireOpToDeleteOthersHomes = require;
        setDirty();
    }

    public int getWarmupSeconds() {
        return warmupSeconds;
    }

    public void setWarmupSeconds(int warmupSeconds) {
        this.warmupSeconds = Math.max(0, warmupSeconds);
        setDirty();
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
        setDirty();
    }
}
