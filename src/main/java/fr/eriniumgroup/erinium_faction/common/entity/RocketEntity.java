package fr.eriniumgroup.erinium_faction.common.entity;

import fr.eriniumgroup.erinium_faction.init.EFEntities;
import fr.eriniumgroup.erinium_faction.init.EFItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Entité fusée montable inspirée de SpaceX
 * Modèle créé avec Blockbench
 */
public class RocketEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.FLOAT);

    public RocketEntity(EntityType<? extends RocketEntity> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
        this.setNoGravity(false);
    }

    public RocketEntity(Level level, double x, double y, double z) {
        this(EFEntities.ROCKET.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_HURT, 0);
        builder.define(DATA_ID_HURTDIR, 1);
        builder.define(DATA_ID_DAMAGE, 0.0F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void tick() {
        super.tick();

        // Gérer la physique basique
        if (!this.level().isClientSide) {
            // Appliquer la gravité si pas de passager
            if (this.getPassengers().isEmpty()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
            }
        }

        // Appliquer le mouvement
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

        // Friction
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.98, 0.98));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            // Shift + clic = ramasser la fusée
            // Sécurité : ne pas ramasser si quelqu'un est assis dedans
            if (!this.getPassengers().isEmpty()) {
                return InteractionResult.PASS;
            }

            if (!this.level().isClientSide) {
                ItemStack itemstack = new ItemStack(this.getDropItem());
                if (this.hasCustomName()) {
                    itemstack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, this.getCustomName());
                }

                if (!player.getAbilities().instabuild) {
                    if (!player.getInventory().add(itemstack)) {
                        player.drop(itemstack, false);
                    }
                } else {
                    player.getInventory().add(itemstack);
                }

                this.discard();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        } else {
            // Clic normal = monter dans la fusée
            if (!this.level().isClientSide) {
                if (!player.isPassenger()) {
                    return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
                }
            }
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, net.minecraft.world.entity.EntityDimensions dimensions, float partialTick) {
        // Position du siège d'après seat_position.txt: [0, 2.9, 0]
        return new Vec3(0.0, 2.9, 0.0);
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            Vec3 seatPos = this.getPassengerAttachmentPoint(passenger, passenger.getDimensions(passenger.getPose()), 1.0F);
            Vec3 pos = seatPos.yRot(-this.getYRot() * ((float)Math.PI / 180F));
            moveFunction.accept(passenger, this.getX() + pos.x, this.getY() + pos.y, this.getZ() + pos.z);
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.level().isClientSide && !this.isRemoved()) {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.setDamage(this.getDamage() + amount * 10.0F);
            this.markHurt();
            boolean isCreative = source.getEntity() instanceof Player && ((Player)source.getEntity()).getAbilities().instabuild;
            if (isCreative || this.getDamage() > 40.0F) {
                this.ejectPassengers();
                if (isCreative && !this.hasCustomName()) {
                    this.discard();
                } else {
                    this.destroy(source);
                }
            }
            return true;
        }
        return true;
    }

    public void destroy(net.minecraft.world.damagesource.DamageSource damageSource) {
        this.kill();
        if (this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemstack = new ItemStack(this.getDropItem());
            if (this.hasCustomName()) {
                itemstack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, this.getCustomName());
            }
            this.spawnAtLocation(itemstack);
        }
    }

    public Item getDropItem() {
        return EFItems.ROCKET_SPAWN_EGG.get();
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_ID_DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public void setHurtTime(int time) {
        this.entityData.set(DATA_ID_HURT, time);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public void setHurtDir(int dir) {
        this.entityData.set(DATA_ID_HURTDIR, dir);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Damage", this.getDamage());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 1;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
        // Pas de dégâts de chute pour l'entité
    }
}
