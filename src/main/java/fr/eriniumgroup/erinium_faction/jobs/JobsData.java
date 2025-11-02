package fr.eriniumgroup.erinium_faction.jobs;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Données de progression pour tous les métiers d'un joueur
 * Stocke le niveau et l'XP pour chaque JobType
 */
public class JobsData implements INBTSerializable<CompoundTag> {

    public static final int MAX_LEVEL = 100;

    // Map JobType -> JobProgressionData
    private final Map<JobType, JobProgressionData> jobs = new HashMap<>();

    public JobsData() {
        // Initialiser tous les métiers à niveau 1, 0 XP
        for (JobType type : JobType.values()) {
            jobs.put(type, new JobProgressionData());
        }
    }

    /**
     * Obtient les données de progression pour un métier spécifique
     */
    public JobProgressionData getJobProgression(JobType type) {
        return jobs.computeIfAbsent(type, k -> new JobProgressionData());
    }

    /**
     * Définit le niveau d'un métier
     */
    public void setLevel(JobType type, int level) {
        getJobProgression(type).level = Math.max(1, Math.min(MAX_LEVEL, level));
    }

    /**
     * Obtient le niveau d'un métier
     */
    public int getLevel(JobType type) {
        return getJobProgression(type).level;
    }

    /**
     * Définit l'expérience d'un métier
     */
    public void setExperience(JobType type, int experience) {
        getJobProgression(type).experience = Math.max(0, experience);
    }

    /**
     * Obtient l'expérience d'un métier
     */
    public int getExperience(JobType type) {
        return getJobProgression(type).experience;
    }

    /**
     * Ajoute de l'expérience à un métier et gère le level up automatiquement
     * @return Le nombre de niveaux gagnés (0 si aucun)
     */
    public int addExperience(JobType type, int amount) {
        JobProgressionData progression = getJobProgression(type);

        // Si déjà au max level, ne pas ajouter d'XP
        if (progression.level >= MAX_LEVEL) {
            progression.experience = 0;
            return 0;
        }

        progression.experience += amount;

        int levelsGained = 0;

        // Vérifier les level ups
        while (progression.level < MAX_LEVEL && progression.experience >= getExperienceForNextLevel(progression.level)) {
            progression.experience -= getExperienceForNextLevel(progression.level);
            progression.level++;
            levelsGained++;
        }

        // Si on atteint le max level, reset l'XP
        if (progression.level >= MAX_LEVEL) {
            progression.level = MAX_LEVEL;
            progression.experience = 0;
        }

        return levelsGained;
    }

    /**
     * Calcule l'XP nécessaire pour passer au niveau suivant
     * Formule: (1000 + 178.853 * LEVEL^1.5) * (LEVEL^1.005)
     * Niveaux: 1-100
     */
    public int getExperienceForNextLevel(int currentLevel) {
        if (currentLevel >= 100) {
            return Integer.MAX_VALUE; // Max level atteint
        }

        double base = 1000 + (178.853 * Math.pow(currentLevel, 1.5));
        double multiplier = Math.pow(currentLevel, 1.005);
        return (int) Math.round(base * multiplier);
    }

    /**
     * Classe interne pour stocker la progression d'un seul métier
     */
    public static class JobProgressionData {
        private int level = 1;
        private int experience = 0;

        public int getLevel() {
            return level;
        }

        public int getExperience() {
            return experience;
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("level", level);
            tag.putInt("experience", experience);
            return tag;
        }

        public void deserialize(CompoundTag tag) {
            level = tag.getInt("level");
            experience = tag.getInt("experience");
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        for (Map.Entry<JobType, JobProgressionData> entry : jobs.entrySet()) {
            tag.put(entry.getKey().name(), entry.getValue().serialize());
        }

        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        for (JobType type : JobType.values()) {
            if (tag.contains(type.name())) {
                getJobProgression(type).deserialize(tag.getCompound(type.name()));
            }
        }
    }

    public JobsData copy() {
        JobsData copy = new JobsData();

        for (Map.Entry<JobType, JobProgressionData> entry : this.jobs.entrySet()) {
            JobProgressionData copiedProgression = copy.getJobProgression(entry.getKey());
            copiedProgression.level = entry.getValue().level;
            copiedProgression.experience = entry.getValue().experience;
        }

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

    /**
     * Codec pour la sérialisation des attachments
     */
    public static class JobsDataCodec implements net.neoforged.neoforge.attachment.IAttachmentSerializer<CompoundTag, JobsData> {
        @Override
        public JobsData read(net.neoforged.neoforge.attachment.IAttachmentHolder holder, CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
            JobsData data = new JobsData();
            data.deserializeNBT(provider, tag);
            return data;
        }

        @Override
        public CompoundTag write(JobsData attachment, net.minecraft.core.HolderLookup.Provider provider) {
            return attachment.serializeNBT(provider);
        }
    }
}
