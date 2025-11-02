package fr.eriniumgroup.erinium_faction.jobs.config;

import com.google.gson.annotations.SerializedName;
import fr.eriniumgroup.erinium_faction.jobs.JobType;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration complète d'un métier
 * Contient les entrées XP et les débloquages
 */
public class JobConfig {

    @SerializedName("jobType")
    private String jobTypeName; // Nom du JobType (MINER, LUMBERJACK, etc.)

    @SerializedName("xpEarning")
    private List<XpEarningEntry> xpEarning = new ArrayList<>();

    @SerializedName("unlocking")
    private List<UnlockingEntry> unlocking = new ArrayList<>();

    // Constructeur par défaut pour GSON
    public JobConfig() {
    }

    public JobConfig(JobType jobType) {
        this.jobTypeName = jobType.name();
    }

    /**
     * Obtient le JobType associé à cette config
     */
    public JobType getJobType() {
        try {
            return JobType.valueOf(jobTypeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Trouve l'entrée XP pour une action donnée
     */
    public XpEarningEntry findXpEntry(ActionType actionType, String targetId, int playerLevel) {
        for (XpEarningEntry entry : xpEarning) {
            if (entry.getActionType() == actionType &&
                entry.getTargetId().equals(targetId) &&
                entry.isAvailableAtLevel(playerLevel)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Obtient tous les débloquages pour un niveau donné
     */
    public List<UnlockingEntry> getUnlocksAtLevel(int level) {
        List<UnlockingEntry> unlocks = new ArrayList<>();
        for (UnlockingEntry entry : unlocking) {
            if (entry.getLevel() == level) {
                unlocks.add(entry);
            }
        }
        return unlocks;
    }

    /**
     * Vérifie si un élément est débloqué pour un niveau donné
     */
    public boolean isUnlocked(UnlockType type, String targetId, int playerLevel) {
        for (UnlockingEntry entry : unlocking) {
            if (entry.getType() == type &&
                entry.getTargetId().equals(targetId) &&
                entry.isUnlockedAtLevel(playerLevel)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtient toutes les entrées XP disponibles pour un niveau
     */
    public List<XpEarningEntry> getAvailableXpEntries(int playerLevel) {
        List<XpEarningEntry> available = new ArrayList<>();
        for (XpEarningEntry entry : xpEarning) {
            if (entry.isAvailableAtLevel(playerLevel)) {
                available.add(entry);
            }
        }
        return available;
    }

    // Getters et Setters
    public String getJobTypeName() {
        return jobTypeName;
    }

    public void setJobTypeName(String jobTypeName) {
        this.jobTypeName = jobTypeName;
    }

    public List<XpEarningEntry> getXpEarning() {
        return xpEarning;
    }

    public void setXpEarning(List<XpEarningEntry> xpEarning) {
        this.xpEarning = xpEarning;
    }

    public List<UnlockingEntry> getUnlocking() {
        return unlocking;
    }

    public void setUnlocking(List<UnlockingEntry> unlocking) {
        this.unlocking = unlocking;
    }
}
