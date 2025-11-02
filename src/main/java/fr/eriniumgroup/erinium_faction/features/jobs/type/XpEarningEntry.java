package fr.eriniumgroup.erinium_faction.features.jobs.type;

import com.google.gson.annotations.SerializedName;

/**
 * Entrée de configuration pour gagner de l'XP
 * Représente une action qui donne de l'XP pour un métier
 */
public class XpEarningEntry {

    @SerializedName("actionType")
    private ActionType actionType;

    @SerializedName("targetId")
    private String targetId; // Block ID, Item ID, Entity ID, ou Custom ID

    @SerializedName("minLevel")
    private int minLevel = -1; // -1 = pas de minimum

    @SerializedName("maxLevel")
    private int maxLevel = -1; // -1 = pas de maximum

    @SerializedName("xpEarned")
    private int xpEarned;

    @SerializedName("iconItem")
    private String iconItem; // Item ID pour l'icône dans le GUI

    // Constructeur par défaut pour GSON
    public XpEarningEntry() {
    }

    public XpEarningEntry(ActionType actionType, String targetId, int minLevel, int maxLevel, int xpEarned, String iconItem) {
        this.actionType = actionType;
        this.targetId = targetId;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.xpEarned = xpEarned;
        this.iconItem = iconItem;
    }

    /**
     * Vérifie si cette entrée est disponible pour un niveau donné
     */
    public boolean isAvailableAtLevel(int playerLevel) {
        if (minLevel != -1 && playerLevel < minLevel) {
            return false;
        }
        if (maxLevel != -1 && playerLevel > maxLevel) {
            return false;
        }
        return true;
    }

    // Getters
    public ActionType getActionType() {
        return actionType;
    }

    public String getTargetId() {
        return targetId;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public String getIconItem() {
        return iconItem;
    }

    // Setters (pour GSON)
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    public void setIconItem(String iconItem) {
        this.iconItem = iconItem;
    }

    @Override
    public String toString() {
        return "XpEarningEntry{" +
                "actionType=" + actionType +
                ", targetId='" + targetId + '\'' +
                ", minLevel=" + minLevel +
                ", maxLevel=" + maxLevel +
                ", xpEarned=" + xpEarned +
                ", iconItem='" + iconItem + '\'' +
                '}';
    }
}
