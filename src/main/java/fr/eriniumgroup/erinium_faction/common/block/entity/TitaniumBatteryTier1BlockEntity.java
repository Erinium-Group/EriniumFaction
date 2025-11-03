package fr.eriniumgroup.erinium_faction.common.block.entity;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumBatteryTier1Block;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class TitaniumBatteryTier1BlockEntity extends BlockEntity {
    private final EnergyImpl energy = new EnergyImpl(100000, 400, 400);
    private int lastOutPerTick = 0;
    private int lastInPerTick = 0;

    public TitaniumBatteryTier1BlockEntity(BlockPos pos, BlockState state) {
        super(EFBlockEntities.TITANIUM_BATTERY_TIER1.get(), pos, state);
    }

    public void onTick() {
        if (level == null || level.isClientSide) return;
        // Reset débit pour ce tick
        energy.resetTransfer();
        lastOutPerTick = 0;
        // Pousser de l'énergie sur toutes les faces
        for (Direction dir : Direction.values()) {
            var storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(dir), dir.getOpposite());
            if (storage != null) {
                int can = energy.extractEnergy(400, true);
                int accepted = storage.receiveEnergy(can, false);
                if (accepted > 0) {
                    energy.extractEnergy(accepted, false);
                    lastOutPerTick += accepted;
                }
            }
        }
        lastInPerTick = energy.getLastReceived();
        // Mettre à jour le level (0..4) selon le pourcentage
        int pct = (int)Math.round((energy.getEnergyStored() * 4.0) / energy.getMaxEnergyStored());
        pct = Math.max(0, Math.min(4, pct));
        BlockState st = getBlockState();
        if (st.getValue(TitaniumBatteryTier1Block.LEVEL) != pct) {
            level.setBlock(worldPosition, st.setValue(TitaniumBatteryTier1Block.LEVEL, pct), 3);
        }
    }

    public int getLastOutPerTick() { return lastOutPerTick; }
    public int getLastInPerTick() { return lastInPerTick; }

    public IEnergyStorage getEnergy(Direction side) { return energy; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energy.setEnergy(tag.getInt("energy"));
    }

    private static class EnergyImpl implements IEnergyStorage {
        private int energy;
        private final int capacity, maxReceive, maxExtract;
        private int lastReceivedTick = 0;
        private int lastExtractedTick = 0;
        EnergyImpl(int cap, int in, int out) { capacity = cap; maxReceive = in; maxExtract = out; }
        void setEnergy(int e) { energy = Math.max(0, Math.min(capacity, e)); }
        void resetTransfer() { lastReceivedTick = 0; lastExtractedTick = 0; }
        int getLastReceived() { return lastReceivedTick; }
        int getLastExtracted() { return lastExtractedTick; }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int to = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
            if (!simulate && to > 0) { energy += to; lastReceivedTick += to; }
            return to;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int to = Math.min(energy, Math.min(this.maxExtract, maxExtract));
            if (!simulate && to > 0) { energy -= to; lastExtractedTick += to; }
            return to;
        }
        @Override public int getEnergyStored() { return energy; }
        @Override public int getMaxEnergyStored() { return capacity; }
        @Override public boolean canExtract() { return maxExtract > 0; }
        @Override public boolean canReceive() { return maxReceive > 0; }
    }
}
