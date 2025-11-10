package fr.eriniumgroup.erinium_faction.features.combatlog;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * NPC qui apparaît quand un joueur se déconnecte en combat
 * Utilise le skin du joueur original via GameProfile
 */
public class CombatLogNPC extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> DATA_PLAYER_UUID =
            SynchedEntityData.defineId(CombatLogNPC.class, EntityDataSerializers.OPTIONAL_UUID);

    private GameProfile gameProfile;
    private final CombatNPCData npcData;

    public CombatLogNPC(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, null, null);
    }

    public CombatLogNPC(EntityType<? extends PathfinderMob> entityType, Level level, CombatNPCData data, GameProfile profile) {
        super(entityType, level);
        this.npcData = data;
        this.gameProfile = profile;

        if (data != null) {
            setupFromData(data);
        }

        // NPC ne peut pas bouger
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAYER_UUID, Optional.empty());
    }

    private void setupFromData(CombatNPCData data) {
        // Définir le nom
        this.setCustomName(Component.literal("§c[COMBAT] " + data.getPlayerName()));
        this.setCustomNameVisible(true);

        // Stocker l'UUID pour le skin
        this.entityData.set(DATA_PLAYER_UUID, Optional.of(data.getPlayerId()));

        // Définir la santé MAX en PREMIER (très important!)
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getMaxHealth());
        }

        // Ensuite définir la santé actuelle (doit être après maxHealth!)
        this.setHealth(data.getHealth());

        // Équiper l'armure
        List<ItemStack> armor = data.getArmor();
        if (armor.size() > 0) this.setItemSlot(EquipmentSlot.FEET, armor.get(0));
        if (armor.size() > 1) this.setItemSlot(EquipmentSlot.LEGS, armor.get(1));
        if (armor.size() > 2) this.setItemSlot(EquipmentSlot.CHEST, armor.get(2));
        if (armor.size() > 3) this.setItemSlot(EquipmentSlot.HEAD, armor.get(3));

        // Mains
        List<ItemStack> inventory = data.getInventory();
        if (!inventory.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, inventory.get(0));
        }
        this.setItemSlot(EquipmentSlot.OFFHAND, data.getOffhand());

        // 100% de chance de drop
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 1.0f);
        }

        this.setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override
    public void tick() {
        super.tick();

        // Vérifier si le NPC a expiré
        if (npcData != null && npcData.isExpired() && !this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void dropAllDeathLoot(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource damageSource) {
        // Dropper tout l'inventaire du joueur
        if (npcData != null) {
            // Inventaire principal
            for (ItemStack stack : npcData.getInventory()) {
                if (!stack.isEmpty()) {
                    this.spawnAtLocation(stack);
                }
            }

            // Armure
            for (ItemStack stack : npcData.getArmor()) {
                if (!stack.isEmpty()) {
                    this.spawnAtLocation(stack);
                }
            }

            // Main secondaire
            if (!npcData.getOffhand().isEmpty()) {
                this.spawnAtLocation(npcData.getOffhand());
            }
        }
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public Optional<UUID> getPlayerUUID() {
        return this.entityData.get(DATA_PLAYER_UUID);
    }

    public CombatNPCData getNpcData() {
        return npcData;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (gameProfile != null) {
            compound.putUUID("OwnerUUID", gameProfile.getId());
            compound.putString("OwnerName", gameProfile.getName());

            // Sauvegarder les propriétés du skin
            if (!gameProfile.getProperties().isEmpty()) {
                CompoundTag properties = new CompoundTag();
                for (Property property : gameProfile.getProperties().get("textures")) {
                    properties.putString("Value", property.value());
                    if (property.signature() != null) {
                        properties.putString("Signature", property.signature());
                    }
                }
                compound.put("SkinData", properties);
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("OwnerUUID")) {
            UUID uuid = compound.getUUID("OwnerUUID");
            String name = compound.getString("OwnerName");
            this.gameProfile = new GameProfile(uuid, name);

            // Restaurer les propriétés du skin
            if (compound.contains("SkinData")) {
                CompoundTag skinData = compound.getCompound("SkinData");
                String value = skinData.getString("Value");
                String signature = skinData.contains("Signature") ? skinData.getString("Signature") : null;

                Property property = new Property("textures", value, signature);
                this.gameProfile.getProperties().put("textures", property);
            }

            this.entityData.set(DATA_PLAYER_UUID, Optional.of(uuid));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
