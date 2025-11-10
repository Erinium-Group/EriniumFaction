package fr.eriniumgroup.erinium_faction.features.combatlog;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stocke les données pour créer un NPC de combat logging
 */
public class CombatNPCData {
    private final UUID playerId;
    private final String playerName;
    private final float health;
    private final float maxHealth;
    private final List<ItemStack> inventory;
    private final List<ItemStack> armor;
    private final ItemStack offhand;
    private final long spawnTime;
    private final boolean wasTimeout;

    public CombatNPCData(UUID playerId, String playerName, float health, float maxHealth,
                         List<ItemStack> inventory, List<ItemStack> armor, ItemStack offhand,
                         boolean wasTimeout) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.health = health;
        this.maxHealth = maxHealth;
        this.inventory = new ArrayList<>(inventory);
        this.armor = new ArrayList<>(armor);
        this.offhand = offhand.copy();
        this.spawnTime = System.currentTimeMillis();
        this.wasTimeout = wasTimeout;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }

    public List<ItemStack> getArmor() {
        return armor;
    }

    public ItemStack getOffhand() {
        return offhand;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public boolean isExpired() {
        // NPC expire après 30 secondes
        return System.currentTimeMillis() - spawnTime > 30000;
    }

    public boolean wasTimeout() {
        return wasTimeout;
    }

    public boolean canReconnectWithoutPenalty() {
        // Peut se reconnecter sans pénalité dans les 2 minutes si c'était un timeout
        if (!wasTimeout) return false;
        return System.currentTimeMillis() - spawnTime < 120000; // 2 minutes
    }
}
