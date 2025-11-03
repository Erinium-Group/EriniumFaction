package fr.eriniumgroup.erinium_faction.common.block.entity;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumCreativeBatteryBlock;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class TitaniumCreativeBatteryBlockEntity extends BlockEntity {
    private final IEnergyStorage energy = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return maxExtract; }
        @Override public int getEnergyStored() { return Integer.MAX_VALUE; }
        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    };
    private int lastOutPerTick = 0;

    public TitaniumCreativeBatteryBlockEntity(BlockPos pos, BlockState state) { super(EFBlockEntities.TITANIUM_CREATIVE_BATTERY.get(), pos, state); }

    public void onTick() {
        if (level == null || level.isClientSide) return;
        int sum = 0;
        for (Direction dir : Direction.values()) {
            var storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(dir), dir.getOpposite());
            if (storage != null) {
                int a = storage.receiveEnergy(1000000, false);
                sum += a;
            }
        }
        lastOutPerTick = sum;
        // creative = toujours plein
        BlockState st = getBlockState();
        if (st.getValue(TitaniumCreativeBatteryBlock.LEVEL) != 4) {
            level.setBlock(worldPosition, st.setValue(TitaniumCreativeBatteryBlock.LEVEL, 4), 3);
        }
    }

    public int getLastOutPerTick() { return lastOutPerTick; }

    public IEnergyStorage getEnergy(Direction side) { return energy; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.saveAdditional(tag, provider); }
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.loadAdditional(tag, provider); }
}
