package fr.eriniumgroup.erinium_faction.features.jobs.type;

import com.google.gson.annotations.SerializedName;

/**
 * Entrée de configuration pour les débloquages
 * Représente un élément débloqué à un certain niveau
 */
public class UnlockingEntry {

    @SerializedName("type")
    private UnlockType type;

    @SerializedName("targetId")
    private String targetId; // Item ID, Block ID, Dimension ID, ou Custom ID

    @SerializedName("level")
    private int level;

    @SerializedName("displayName")
    private String displayName; // Nom à afficher dans le GUI (optionnel)

    @SerializedName("description")
    private String description; // Description (optionnel)

    // Constructeur par défaut pour GSON
    public UnlockingEntry() {
    }

    public UnlockingEntry(UnlockType type, String targetId, int level, String displayName, String description) {
        this.type = type;
        this.targetId = targetId;
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Vérifie si cet élément est débloqué pour un niveau donné
     */
    public boolean isUnlockedAtLevel(int playerLevel) {
        return playerLevel >= level;
    }

    // Getters
    public UnlockType getType() {
        return type;
    }

    public String getTargetId() {
        return targetId;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : targetId;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    // Setters (pour GSON)
    public void setType(UnlockType type) {
        this.type = type;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "UnlockingEntry{" +
                "type=" + type +
                ", targetId='" + targetId + '\'' +
                ", level=" + level +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
