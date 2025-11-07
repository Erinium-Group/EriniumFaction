package fr.eriniumgroup.erinium_faction.common.block.entity;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumCreativeBatteryBlock;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TitaniumCreativeBatteryBlockEntity extends BlockEntity implements MenuProvider, IConfigurableMachine {
    private final IEnergyStorage energy = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return maxExtract;
        }

        @Override
        public int getEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };
    private int lastOutPerTick = 0;
    private final FaceConfiguration faceConfig = new FaceConfiguration();

    public TitaniumCreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(EFBlockEntities.TITANIUM_CREATIVE_BATTERY.get(), pos, state);
        // Par défaut, toutes les faces en mode ENERGY
        for (Direction dir : Direction.values()) {
            faceConfig.setFaceMode(dir, FaceMode.ENERGY);
        }
    }

    public void onTick() {
        if (level == null || level.isClientSide) return;
        int sum = 0;
        for (Direction dir : Direction.values()) {
            if (faceConfig.getFaceMode(dir).allowsEnergy()) {
                var storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(dir), dir.getOpposite());
                if (storage != null && storage.canReceive()) {
                    int a = storage.receiveEnergy(1000000, false);
                    sum += a;
                }
            }
        }
        lastOutPerTick = sum;
        // creative = toujours plein
        BlockState st = getBlockState();
        if (st.getValue(TitaniumCreativeBatteryBlock.LEVEL) != 4) {
            level.setBlock(worldPosition, st.setValue(TitaniumCreativeBatteryBlock.LEVEL, 4), 3);
        }
    }

    public int getLastOutPerTick() {
        return lastOutPerTick;
    }

    public int getLastInPerTick() {
        return 0;
    }

    public IEnergyStorage getEnergy(Direction side) {
        if (side != null && !faceConfig.getFaceMode(side).allowsEnergy()) {
            return null;
        }
        return energy;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("faceConfig", faceConfig.save());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("faceConfig")) {
            faceConfig.load(tag.getCompound("faceConfig"));
        }
    }

    // MenuProvider
    @Override
    @NotNull
    public Component getDisplayName() {
        return Component.translatable("block.erinium_faction.titanium_creative_battery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new fr.eriniumgroup.erinium_faction.gui.menus.CreativeBatteryMenu(id, inv, this);
    }

    // IConfigurableMachine
    @Override
    @NotNull
    public FaceConfiguration getFaceConfiguration() {
        return faceConfig;
    }

    @Override
    public void setFaceMode(Direction face, FaceMode mode) {
        if (mode == FaceMode.ENERGY || mode == FaceMode.NONE) {
            faceConfig.setFaceMode(face, mode);
            setChanged();
        }
    }

    @Override
    public FaceMode getFaceMode(Direction face) {
        return faceConfig.getFaceMode(face);
    }

    @Override
    public void setAutoInput(boolean enabled) {
        faceConfig.setAutoInput(enabled);
        setChanged();
    }

    @Override
    public boolean isAutoInput() {
        return faceConfig.isAutoInput();
    }

    @Override
    public void setAutoOutput(boolean enabled) {
        faceConfig.setAutoOutput(enabled);
        setChanged();
    }

    @Override
    public boolean isAutoOutput() {
        return faceConfig.isAutoOutput();
    }

    @Override
    public void onConfigurationChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
