package fr.eriniumgroup.erinium_faction.player.level;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

/**
 * Données de niveau et d'expérience pour un joueur
 */
public class PlayerLevelData implements INBTSerializable<CompoundTag> {
    private int level = 1;
    private int experience = 0;
    private int experienceToNextLevel = 100;

    // Points d'attributs disponibles
    private int availablePoints = 0;

    // Attributs investis
    private int healthPoints = 0;      // Vie
    private int armorPoints = 0;       // Armure
    private int speedPoints = 0;       // Vitesse
    private int intelligencePoints = 0; // Intelligence
    private int strengthPoints = 0;     // Force
    private int luckPoints = 0;        // Chance

    public PlayerLevelData() {
    }

    // Getters
    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    public int getAvailablePoints() {
        return availablePoints;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public int getArmorPoints() {
        return armorPoints;
    }

    public int getSpeedPoints() {
        return speedPoints;
    }

    public int getIntelligencePoints() {
        return intelligencePoints;
    }

    public int getStrengthPoints() {
        return strengthPoints;
    }

    public int getLuckPoints() {
        return luckPoints;
    }

    // Setters
    public void setLevel(int level) {
        this.level = level;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setExperienceToNextLevel(int experienceToNextLevel) {
        this.experienceToNextLevel = experienceToNextLevel;
    }

    public void setAvailablePoints(int availablePoints) {
        this.availablePoints = availablePoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public void setArmorPoints(int armorPoints) {
        this.armorPoints = armorPoints;
    }

    public void setSpeedPoints(int speedPoints) {
        this.speedPoints = speedPoints;
    }

    public void setIntelligencePoints(int intelligencePoints) {
        this.intelligencePoints = intelligencePoints;
    }

    public void setStrengthPoints(int strengthPoints) {
        this.strengthPoints = strengthPoints;
    }

    public void setLuckPoints(int luckPoints) {
        this.luckPoints = luckPoints;
    }

    public int getTotalPointsSpent() {
        return healthPoints + armorPoints + speedPoints + intelligencePoints + strengthPoints + luckPoints;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putInt("experience", experience);
        tag.putInt("experienceToNextLevel", experienceToNextLevel);
        tag.putInt("availablePoints", availablePoints);
        tag.putInt("healthPoints", healthPoints);
        tag.putInt("armorPoints", armorPoints);
        tag.putInt("speedPoints", speedPoints);
        tag.putInt("intelligencePoints", intelligencePoints);
        tag.putInt("strengthPoints", strengthPoints);
        tag.putInt("luckPoints", luckPoints);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        level = tag.getInt("level");
        experience = tag.getInt("experience");
        experienceToNextLevel = tag.getInt("experienceToNextLevel");
        availablePoints = tag.getInt("availablePoints");
        healthPoints = tag.getInt("healthPoints");
        armorPoints = tag.getInt("armorPoints");
        speedPoints = tag.getInt("speedPoints");
        intelligencePoints = tag.getInt("intelligencePoints");
        strengthPoints = tag.getInt("strengthPoints");
        luckPoints = tag.getInt("luckPoints");
    }

    public PlayerLevelData copy() {
        PlayerLevelData copy = new PlayerLevelData();
        copy.level = this.level;
        copy.experience = this.experience;
        copy.experienceToNextLevel = this.experienceToNextLevel;
        copy.availablePoints = this.availablePoints;
        copy.healthPoints = this.healthPoints;
        copy.armorPoints = this.armorPoints;
        copy.speedPoints = this.speedPoints;
        copy.intelligencePoints = this.intelligencePoints;
        copy.strengthPoints = this.strengthPoints;
        copy.luckPoints = this.luckPoints;
        return copy;
    }

    @Override
    public CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider) {
        return serializeNBT();
    }

    @Override
    public void deserializeNBT(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag compoundTag) {
        deserializeNBT(compoundTag);
    }
}

