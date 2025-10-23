package fr.eriniumgroup.erinium_faction.core.faction;

import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * Represents a faction with its members, claims, and relations
 */
public class Faction {
    private final String name;
    private final UUID ownerId;
    private final Map<UUID, Rank> members;
    private final Set<ClaimKey> claims;
    private final Set<String> allies;
    private final Set<String> enemies;

    private String homeDimension;
    private BlockPos homePosition;
    private double power;
    private long creationTime;

    public Faction(String name, UUID ownerId) {
        this.name = name.toLowerCase(Locale.ROOT);
        this.ownerId = ownerId;
        this.members = new LinkedHashMap<>();
        this.members.put(ownerId, Rank.OWNER);
        this.claims = new LinkedHashSet<>();
        this.allies = new LinkedHashSet<>();
        this.enemies = new LinkedHashSet<>();
        this.power = 10.0;
        this.creationTime = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Map<UUID, Rank> getMembers() {
        return members;
    }

    public Set<ClaimKey> getClaims() {
        return claims;
    }

    public Set<String> getAllies() {
        return allies;
    }

    public Set<String> getEnemies() {
        return enemies;
    }

    public String getHomeDimension() {
        return homeDimension;
    }

    public BlockPos getHomePosition() {
        return homePosition;
    }

    public double getPower() {
        return power;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public double getXPRequiredForNextLevel(int currentLevel) {
        if (currentLevel >= 20) {
            return 0;
        }
        return 500 * Math.pow(1.5, currentLevel);
    }

    public boolean isMember(UUID playerId) {
        return members.containsKey(playerId);
    }

    public Rank getRank(UUID playerId) {
        return members.get(playerId);
    }

    public boolean addMember(UUID playerId, Rank rank) {
        return members.putIfAbsent(playerId, rank) == null;
    }

    public boolean removeMember(UUID playerId) {
        return members.remove(playerId) != null;
    }

    public boolean setRank(UUID playerId, Rank newRank) {
        if (!members.containsKey(playerId)) return false;
        members.put(playerId, newRank);
        return true;
    }

    public void setHome(String dimension, BlockPos position) {
        this.homeDimension = dimension;
        this.homePosition = position;
    }

    public boolean hasHome() {
        return homeDimension != null && homePosition != null;
    }

    public boolean addClaim(ClaimKey claim) {
        return claims.add(claim);
    }

    public boolean removeClaim(ClaimKey claim) {
        return claims.remove(claim);
    }

    public boolean hasClaim(ClaimKey claim) {
        return claims.contains(claim);
    }

    public int getClaimCount() {
        return claims.size();
    }

    public boolean addAlly(String factionName) {
        return allies.add(factionName.toLowerCase(Locale.ROOT));
    }

    public boolean removeAlly(String factionName) {
        return allies.remove(factionName.toLowerCase(Locale.ROOT));
    }

    public boolean isAlly(String factionName) {
        return allies.contains(factionName.toLowerCase(Locale.ROOT));
    }

    public boolean addEnemy(String factionName) {
        return enemies.add(factionName.toLowerCase(Locale.ROOT));
    }

    public boolean removeEnemy(String factionName) {
        return enemies.remove(factionName.toLowerCase(Locale.ROOT));
    }

    public boolean isEnemy(String factionName) {
        return enemies.contains(factionName.toLowerCase(Locale.ROOT));
    }

    public void setPower(double power) {
        this.power = Math.max(0, power);
    }

    public void addPower(double amount) {
        this.power += amount;
    }

    public int getMaxClaims() {
        return (int) Math.floor(power);
    }

    public boolean canClaimMore() {
        return claims.size() < getMaxClaims();
    }
}

