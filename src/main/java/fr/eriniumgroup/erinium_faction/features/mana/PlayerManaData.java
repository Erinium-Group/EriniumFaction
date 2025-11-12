package fr.eriniumgroup.erinium_faction.features.mana;

import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelData;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

public class PlayerManaData implements INBTSerializable<CompoundTag> {
    private double mana = 0.0;
    private long lastDamageTick = Long.MIN_VALUE;
    private final Set<String> knownSpells = new java.util.HashSet<>();
    private final Map<String, Integer> typeTier = new java.util.HashMap<>(); // type -> 0..5, 6 = ultimate
    private final Map<String, Long> cooldowns = new java.util.HashMap<>(); // spellId -> gameTime when usable

    public double getMana() { return mana; }
    public void setMana(double v, double max) { mana = Math.max(0.0, Math.min(v, max)); }
    public void addMana(double v, double max) { setMana(mana + v, max); }

    public void markDamaged(long gameTime) { lastDamageTick = gameTime; }
    public long getLastDamageTick() { return lastDamageTick; }

    public boolean knowsSpell(String id) { return knownSpells.contains(id); }
    public void learnSpell(String id) { knownSpells.add(id); }
    public void forgetSpell(String id) { knownSpells.remove(id); cooldowns.remove(id); }
    public Set<String> getKnownSpells() { return Collections.unmodifiableSet(knownSpells); }

    public int getTypeTier(String type) { return typeTier.getOrDefault(type, 0); }
    public void setTypeTier(String type, int tier) { typeTier.put(type, Math.max(0, Math.min(6, tier))); }
    public Map<String, Integer> getAllTypeTiers() { return Collections.unmodifiableMap(typeTier); }

    public boolean isOnCooldown(String spellId, long gameTime) { return cooldowns.getOrDefault(spellId, 0L) > gameTime; }
    public void setCooldown(String spellId, long readyTick) { cooldowns.put(spellId, readyTick); }

    public double computeMaxMana(ServerPlayer player) {
        int intPoints = 0;
        try {
            PlayerLevelData d = PlayerLevelManager.getLevelData(player);
            if (d != null) intPoints = d.getIntelligencePoints();
        } catch (Throwable ignored) {}
        return ManaConfig.BASE_MAX_MANA.get() + intPoints * ManaConfig.MAX_PER_INT_POINT.get();
    }

    public double computeRegenPerSecond(ServerPlayer player, long gameTime) {
        int intPoints = 0;
        try {
            PlayerLevelData d = PlayerLevelManager.getLevelData(player);
            if (d != null) intPoints = d.getIntelligencePoints();
        } catch (Throwable ignored) {}
        double base = ManaConfig.BASE_REGEN_PER_SEC.get() + intPoints * ManaConfig.REGEN_PER_INT_POINT.get();
        // simple in-combat penalty: if damaged within OUT_OF_COMBAT_TICKS, half regen
        if (gameTime - lastDamageTick <= ManaConfig.OUT_OF_COMBAT_TICKS.get()) {
            base *= 0.5;
        }
        return Math.max(0.0, base);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("mana", mana);
        tag.putLong("lastDamage", lastDamageTick);
        // known spells
        var spells = new net.minecraft.nbt.ListTag();
        for (String s : knownSpells) spells.add(net.minecraft.nbt.StringTag.valueOf(s));
        tag.put("spells", spells);
        // type tiers
        CompoundTag tiers = new CompoundTag();
        for (var e : typeTier.entrySet()) tiers.putInt(e.getKey(), e.getValue());
        tag.put("tiers", tiers);
        // cooldowns
        CompoundTag cds = new CompoundTag();
        for (var e : cooldowns.entrySet()) cds.putLong(e.getKey(), e.getValue());
        tag.put("cooldowns", cds);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        mana = nbt.getDouble("mana");
        lastDamageTick = nbt.getLong("lastDamage");
        knownSpells.clear();
        var spells = nbt.getList("spells", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < spells.size(); i++) knownSpells.add(spells.getString(i));
        typeTier.clear();
        CompoundTag tiers = nbt.getCompound("tiers");
        for (String k : tiers.getAllKeys()) typeTier.put(k, tiers.getInt(k));
        cooldowns.clear();
        CompoundTag cds = nbt.getCompound("cooldowns");
        for (String k : cds.getAllKeys()) cooldowns.put(k, cds.getLong(k));
    }
}

