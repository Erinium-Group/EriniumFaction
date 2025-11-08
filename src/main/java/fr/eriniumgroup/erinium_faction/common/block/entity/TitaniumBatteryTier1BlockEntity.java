package fr.eriniumgroup.erinium_faction.common.block.entity;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumBatteryTier1Block;
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

public class TitaniumBatteryTier1BlockEntity extends BlockEntity implements MenuProvider, IConfigurableMachine {
    public static final int CAPACITY = 100000;
    private final EnergyImpl energy = new EnergyImpl(CAPACITY, 400, 400);
    private int lastOutPerTick = 0;
    private int lastInPerTick = 0;
    private final FaceConfiguration faceConfig = new FaceConfiguration();

    public TitaniumBatteryTier1BlockEntity(BlockPos pos, BlockState state) {
        super(EFBlockEntities.TITANIUM_BATTERY_TIER1.get(), pos, state);
        // Par défaut, toutes les faces en mode ENERGY pour une batterie
        for (Direction dir : Direction.values()) {
            faceConfig.setFaceMode(dir, FaceMode.ENERGY);
        }
    }

    public void onTick() {
        if (level == null || level.isClientSide) return;
        // Reset débit pour ce tick
        energy.resetTransfer();
        lastOutPerTick = 0;
        // Pousser de l'énergie uniquement sur les faces configurées en ENERGY
        for (Direction dir : Direction.values()) {
            if (faceConfig.getFaceMode(dir).allowsEnergy()) {
                var storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(dir), dir.getOpposite());
                if (storage != null && storage.canReceive()) {
                    int can = energy.extractEnergy(500, true);
                    if (can > 0) {
                        int accepted = storage.receiveEnergy(can, false);
                        if (accepted > 0) {
                            energy.extractEnergy(accepted, false);
                            lastOutPerTick += accepted;
                        }
                    }
                }
            }
        }
        lastInPerTick = energy.getLastReceived();
        // Mettre à jour le level (0..4) selon le pourcentage
        int pct = (int) Math.round((energy.getEnergyStored() * 4.0) / energy.getMaxEnergyStored());
        pct = Math.max(0, Math.min(4, pct));
        BlockState st = getBlockState();
        if (st.getValue(TitaniumBatteryTier1Block.LEVEL) != pct) {
            level.setBlock(worldPosition, st.setValue(TitaniumBatteryTier1Block.LEVEL, pct), 3);
        }
    }

    public int getLastOutPerTick() {
        return lastOutPerTick;
    }

    public int getLastInPerTick() {
        return lastInPerTick;
    }

    public IEnergyStorage getEnergy(Direction side) {
        // Vérifier si la face permet l'énergie
        if (side != null && !faceConfig.getFaceMode(side).allowsEnergy()) {
            return null;
        }
        return energy;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energy.getEnergyStored());
        tag.put("faceConfig", faceConfig.save());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        energy.setEnergy(tag.getInt("energy"));
        if (tag.contains("faceConfig")) {
            faceConfig.load(tag.getCompound("faceConfig"));
        }
    }

    // MenuProvider
    @Override
    @NotNull
    public Component getDisplayName() {
        return Component.translatable("block.erinium_faction.titanium_battery_tier1");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new fr.eriniumgroup.erinium_faction.gui.menus.BatteryMenu(id, inv, this);
    }

    // IConfigurableMachine
    @Override
    @NotNull
    public FaceConfiguration getFaceConfiguration() {
        return faceConfig;
    }

    @Override
    public void setFaceMode(Direction face, FaceMode mode) {
        // Pour une batterie, on force ENERGY ou NONE uniquement
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

    // Classe interne pour la gestion de l'énergie
    private static class EnergyImpl implements IEnergyStorage {
        private int energy;
        private final int capacity, maxReceive, maxExtract;
        private int lastReceivedTick = 0;
        private int lastExtractedTick = 0;

        EnergyImpl(int cap, int in, int out) {
            capacity = cap;
            maxReceive = in;
            maxExtract = out;
        }

        void setEnergy(int e) {
            energy = Math.max(0, Math.min(capacity, e));
        }

        void resetTransfer() {
            lastReceivedTick = 0;
            lastExtractedTick = 0;
        }

        int getLastReceived() {
            return lastReceivedTick;
        }

        int getLastExtracted() {
            return lastExtractedTick;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int to = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
            if (!simulate && to > 0) {
                energy += to;
                lastReceivedTick += to;
            }
            return to;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int to = Math.min(energy, Math.min(this.maxExtract, maxExtract));
            if (!simulate && to > 0) {
                energy -= to;
                lastExtractedTick += to;
            }
            return to;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return maxExtract > 0;
        }

        @Override
        public boolean canReceive() {
            return maxReceive > 0;
        }
    }
}

