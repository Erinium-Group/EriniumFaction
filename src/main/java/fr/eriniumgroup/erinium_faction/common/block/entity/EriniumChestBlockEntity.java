package fr.eriniumgroup.erinium_faction.common.block.entity;

import fr.eriniumgroup.erinium_faction.gui.menus.EriniumChestMenu;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EriniumChestBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
    private int openCount = 0;
    private float lidAngle = 0.0F;
    private float prevLidAngle = 0.0F;

    public EriniumChestBlockEntity(BlockPos pos, BlockState state) {
        super(EFBlockEntities.ERINIUM_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.erinium_faction.erinium_chest");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new EriniumChestMenu(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 54;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openCount++;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openCount--;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, EriniumChestBlockEntity blockEntity) {
        blockEntity.prevLidAngle = blockEntity.lidAngle;

        // Animation: 0.25 secondes = 5 ticks (20 ticks/sec)
        // Donc on change de 1/5 = 0.2 par tick
        float targetAngle = blockEntity.openCount > 0 ? 1.0F : 0.0F;
        float speed = 0.2F; // 1.0F / 5 ticks

        if (blockEntity.lidAngle < targetAngle) {
            blockEntity.lidAngle = Math.min(blockEntity.lidAngle + speed, targetAngle);
        } else if (blockEntity.lidAngle > targetAngle) {
            blockEntity.lidAngle = Math.max(blockEntity.lidAngle - speed, targetAngle);
        }
    }

    public float getOpenNess(float partialTicks) {
        return this.prevLidAngle + (this.lidAngle - this.prevLidAngle) * partialTicks;
    }

    public void dropContents(Level level, BlockPos pos) {
        net.minecraft.world.Containers.dropContents(level, pos, this);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.openCount = type;
            return true;
        }
        return super.triggerEvent(id, type);
    }
}
