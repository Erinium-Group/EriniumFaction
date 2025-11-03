package fr.eriniumgroup.erinium_faction.common.block.entity;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumCompressorBlock;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;
import fr.eriniumgroup.erinium_faction.init.EFItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;

public class TitaniumCompressorBlockEntity extends BlockEntity implements MenuProvider, Container {
    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY); // 0=input, 1=fuel (futur), 2=output
    private int animTicks = 0;
    private boolean isWorking = false;
    private final ContainerData data = new SimpleContainerData(1);
    private static final int CRAFT_TICKS = 20;

    public TitaniumCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(EFBlockEntities.TITANIUM_COMPRESSOR.get(), pos, state);
    }

    private void updateAnimState() {
        if (this.level == null) return;
        int frame = 0;
        if (animTicks > 0) {
            int phase = (CRAFT_TICKS - animTicks) / 5; // 0..3
            frame = Math.min(4, 1 + phase); // 1..4
        }
        this.level.setBlock(this.worldPosition, getBlockState().setValue(TitaniumCompressorBlock.ANIM, frame), 3);
    }

    // Energy storage (Forge Energy / NeoForge)
    public static class BasicEnergyStorage implements IEnergyStorage {
        protected int energy;
        protected final int capacity;
        protected final int maxReceive;
        protected final int maxExtract;
        private int lastReceivedTick = 0;
        private int lastExtractedTick = 0;
        public BasicEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            this.capacity = capacity;
            this.maxReceive = maxReceive;
            this.maxExtract = maxExtract;
            this.energy = 0;
        }
        protected void onEnergyChanged() {}
        public void resetTransfer() { lastReceivedTick = 0; lastExtractedTick = 0; }
        public int getLastReceived() { return lastReceivedTick; }
        public int getLastExtracted() { return lastExtractedTick; }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int toReceive = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
            if (!simulate && toReceive > 0) { energy += toReceive; lastReceivedTick += toReceive; onEnergyChanged(); }
            return toReceive;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int toExtract = Math.min(energy, Math.min(this.maxExtract, maxExtract));
            if (!simulate && toExtract > 0) { energy -= toExtract; lastExtractedTick += toExtract; onEnergyChanged(); }
            return toExtract;
        }
        @Override public int getEnergyStored() { return energy; }
        @Override public int getMaxEnergyStored() { return capacity; }
        @Override public boolean canExtract() { return this.maxExtract > 0; }
        @Override public boolean canReceive() { return this.maxReceive > 0; }
        public void setEnergy(int value) { energy = Math.max(0, Math.min(capacity, value)); onEnergyChanged(); }
    }

    private final BasicEnergyStorage energy = new BasicEnergyStorage(10000, 200, 200) {
        @Override protected void onEnergyChanged() { setChanged(); }
    };

    public IEnergyStorage getEnergyCapability(@Nullable Direction side) { return energy; }

    public void onTick() {
        if (this.level == null || this.level.isClientSide) return;
        energy.resetTransfer();
        final int ENERGY_PER_PLATE = 200;
        if (!isWorking) {
            ItemStack in = items.get(0);
            if (in.is(EFItems.TITANIUM_INGOT.get())) {
                ItemStack out = items.get(2);
                ItemStack plate = new ItemStack(EFItems.TITANIUM_PLATE.get());
                boolean canOutput = out.isEmpty() || (ItemStack.isSameItem(out, plate) && out.getCount() < out.getMaxStackSize());
                if (canOutput && energy.getEnergyStored() >= ENERGY_PER_PLATE) {
                    // Pay energy cost and start
                    energy.extractEnergy(ENERGY_PER_PLATE, false);
                    in.shrink(1);
                    isWorking = true;
                    animTicks = CRAFT_TICKS;
                    setChanged();
                    updateAnimState();
                }
            }
        } else {
            if (animTicks > 0) {
                animTicks--;
                updateAnimState();
                if (animTicks == 0) {
                    ItemStack out = items.get(2);
                    ItemStack plate = new ItemStack(EFItems.TITANIUM_PLATE.get());
                    if (out.isEmpty()) {
                        items.set(2, plate);
                    } else if (ItemStack.isSameItem(out, plate)) {
                        out.grow(1);
                    }
                    isWorking = false;
                    setChanged();
                }
            }
        }
    }

    // Expose progression pour Jade/overlay
    public int getProgress() { return isWorking ? (CRAFT_TICKS - Math.max(0, animTicks)) : 0; }
    public int getMaxProgress() { return CRAFT_TICKS; }

    public void serverTick() {
        onTick();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.erinium_faction.titanium_compressor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new fr.eriniumgroup.erinium_faction.gui.menus.TitaniumCompressorMenu(id, inv, this, this.data);
    }

    // Container implementation
    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { for (ItemStack s : items) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int index) { return items.get(index); }
    @Override public ItemStack removeItem(int index, int count) { ItemStack res = ContainerHelper.removeItem(items, index, count); if (!res.isEmpty()) setChanged(); return res; }
    @Override public ItemStack removeItemNoUpdate(int index) { ItemStack s = items.get(index); if (s.isEmpty()) return ItemStack.EMPTY; items.set(index, ItemStack.EMPTY); return s; }
    @Override public void setItem(int index, ItemStack stack) { items.set(index, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); setChanged(); }
    @Override public void setChanged() { super.setChanged(); }
    @Override public boolean stillValid(Player player) { return player.distanceToSqr(worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5) <= 64.0; }
    @Override public void clearContent() { items.clear(); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("animTicks", animTicks);
        tag.putBoolean("working", isWorking);
        tag.putInt("energy", energy.getEnergyStored());
        ContainerHelper.saveAllItems(tag, this.items, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.animTicks = tag.getInt("animTicks");
        this.isWorking = tag.getBoolean("working");
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        this.energy.setEnergy(tag.getInt("energy"));
        ContainerHelper.loadAllItems(tag, this.items, provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getLastInPerTick() { return energy.getLastReceived(); }
    public int getLastUsePerTick() { return energy.getLastExtracted(); }
}
