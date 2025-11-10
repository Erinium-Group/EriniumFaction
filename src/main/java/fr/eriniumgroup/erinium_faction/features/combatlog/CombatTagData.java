package fr.eriniumgroup.erinium_faction.features.combatlog;

import java.util.UUID;

/**
 * Stocke les données de combat tag pour un joueur
 */
public class CombatTagData {
    private final UUID playerId;
    private long tagEndTime;
    private UUID lastAttacker;
    private boolean inFactionTerritory;

    public CombatTagData(UUID playerId) {
        this.playerId = playerId;
        this.tagEndTime = 0;
        this.lastAttacker = null;
        this.inFactionTerritory = false;
    }

    public void tag(UUID attacker, boolean inTerritory) {
        this.lastAttacker = attacker;
        this.inFactionTerritory = inTerritory;
        // Durée: 10s en territoire, 15s ailleurs
        int duration = inTerritory ? 10000 : 15000;
        this.tagEndTime = System.currentTimeMillis() + duration;
    }

    public boolean isTagged() {
        return System.currentTimeMillis() < tagEndTime;
    }

    public long getRemainingTime() {
        long remaining = tagEndTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public int getRemainingSeconds() {
        return (int) Math.ceil(getRemainingTime() / 1000.0);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getLastAttacker() {
        return lastAttacker;
    }

    public boolean isInFactionTerritory() {
        return inFactionTerritory;
    }

    public void clear() {
        this.tagEndTime = 0;
        this.lastAttacker = null;
    }
}
