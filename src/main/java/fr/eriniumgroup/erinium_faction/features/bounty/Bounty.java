package fr.eriniumgroup.erinium_faction.features.bounty;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * Représente une prime (bounty) sur la tête d'un joueur
 */
public class Bounty {
    private final UUID targetId;
    private final String targetName;
    private final Map<UUID, ContributorEntry> contributors; // playerId -> contribution
    private final long createdTime;
    private long expirationTime;

    public Bounty(UUID targetId, String targetName) {
        this.targetId = targetId;
        this.targetName = targetName;
        this.contributors = new LinkedHashMap<>();
        this.createdTime = System.currentTimeMillis();
        this.expirationTime = createdTime + (7L * 24 * 60 * 60 * 1000); // 7 jours
    }

    /**
     * Ajoute une contribution à la prime
     */
    public void addContribution(UUID contributorId, String contributorName, double amount) {
        ContributorEntry existing = contributors.get(contributorId);
        if (existing != null) {
            existing.amount += amount;
        } else {
            contributors.put(contributorId, new ContributorEntry(contributorId, contributorName, amount));
        }
    }

    /**
     * Retourne le montant total de la prime
     */
    public double getTotalAmount() {
        return contributors.values().stream()
                .mapToDouble(c -> c.amount)
                .sum();
    }

    /**
     * Vérifie si la prime a expiré
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Retourne le temps restant en secondes
     */
    public long getTimeRemaining() {
        long remaining = (expirationTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public Map<UUID, ContributorEntry> getContributors() {
        return contributors;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Entrée pour un contributeur
     */
    public static class ContributorEntry {
        private final UUID playerId;
        private final String playerName;
        private double amount;

        public ContributorEntry(UUID playerId, String playerName, double amount) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.amount = amount;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public String getPlayerName() {
            return playerName;
        }

        public double getAmount() {
            return amount;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("PlayerId", playerId);
            tag.putString("PlayerName", playerName);
            tag.putDouble("Amount", amount);
            return tag;
        }

        public static ContributorEntry fromNBT(CompoundTag tag) {
            return new ContributorEntry(
                    tag.getUUID("PlayerId"),
                    tag.getString("PlayerName"),
                    tag.getDouble("Amount")
            );
        }
    }

    // Serialization ---------------------------------------------------------------

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("TargetId", targetId);
        tag.putString("TargetName", targetName);
        tag.putLong("CreatedTime", createdTime);
        tag.putLong("ExpirationTime", expirationTime);

        ListTag contributorsList = new ListTag();
        for (ContributorEntry entry : contributors.values()) {
            contributorsList.add(entry.toNBT());
        }
        tag.put("Contributors", contributorsList);

        return tag;
    }

    public static Bounty fromNBT(CompoundTag tag) {
        UUID targetId = tag.getUUID("TargetId");
        String targetName = tag.getString("TargetName");

        Bounty bounty = new Bounty(targetId, targetName);
        bounty.expirationTime = tag.getLong("ExpirationTime");

        ListTag contributorsList = tag.getList("Contributors", Tag.TAG_COMPOUND);
        for (int i = 0; i < contributorsList.size(); i++) {
            CompoundTag entryTag = contributorsList.getCompound(i);
            ContributorEntry entry = ContributorEntry.fromNBT(entryTag);
            bounty.contributors.put(entry.getPlayerId(), entry);
        }

        return bounty;
    }
}
