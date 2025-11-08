package fr.eriniumgroup.erinium_faction.common.block.entity;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumCompressorBlock;
import fr.eriniumgroup.erinium_faction.common.recipe.CompressorRecipe;
import fr.eriniumgroup.erinium_faction.common.recipe.CompressorRecipeType;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TitaniumCompressorBlockEntity extends BlockEntity implements MenuProvider, Container, IConfigurableMachine {
    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY); // 0=input, 1=fuel (futur), 2=output
    private int animTicks = 0;
    private boolean isWorking = false;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> TitaniumCompressorBlockEntity.this.getProgress();
                case 1 -> energy.getEnergyStored() & 0xFFFF; // Lower 16 bits
                case 2 -> (energy.getEnergyStored() >> 16) & 0xFFFF; // Upper 16 bits
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Client reçoit les données mais ne les modifie pas
        }

        @Override
        public int getCount() {
            return 3; // progress + energy (2 slots)
        }
    };
    private final FaceConfiguration faceConfig = new FaceConfiguration();

    public TitaniumCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(EFBlockEntities.TITANIUM_COMPRESSOR.get(), pos, state);
        // Par défaut, toutes les faces en mode ENERGY pour un compresseur
        for (Direction dir : Direction.values()) {
            faceConfig.setFaceMode(dir, FaceMode.ENERGY);
        }
    }

    private void updateAnimState() {
        if (this.level == null) return;
        int frame = 0;
        if (animTicks > 0 && currentProcessingTime > 0) {
            int phase = (currentProcessingTime - animTicks) / Math.max(1, currentProcessingTime / 4); // 0..3
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
        protected int lastReceivedTick = 0;
        protected int lastExtractedTick = 0;

        public BasicEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            this.capacity = capacity;
            this.maxReceive = maxReceive;
            this.maxExtract = maxExtract;
            this.energy = 0;
        }

        protected void onEnergyChanged() {
        }

        public void resetTransfer() {
            lastReceivedTick = 0;
            lastExtractedTick = 0;
        }

        public int getLastReceived() {
            return lastReceivedTick;
        }

        public int getLastExtracted() {
            return lastExtractedTick;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int toReceive = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
            if (!simulate && toReceive > 0) {
                energy += toReceive;
                lastReceivedTick += toReceive;
                onEnergyChanged();
            }
            return toReceive;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int toExtract = Math.min(energy, Math.min(this.maxExtract, maxExtract));
            if (!simulate && toExtract > 0) {
                energy -= toExtract;
                lastExtractedTick += toExtract;
                onEnergyChanged();
            }
            return toExtract;
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
            return this.maxExtract > 0;
        }

        @Override
        public boolean canReceive() {
            return this.maxReceive > 0;
        }

        public void setEnergy(int value) {
            energy = Math.max(0, Math.min(capacity, value));
            onEnergyChanged();
        }
    }

    private final BasicEnergyStorage energy = new BasicEnergyStorage(50000, 500, 500) {
        @Override
        protected void onEnergyChanged() {
            setChanged();
        }
    };

    public IEnergyStorage getEnergyCapability(@Nullable Direction side) {
        // Vérifier si cette face permet l'énergie
        if (side != null && !faceConfig.getFaceMode(side).allowsEnergy()) {
            return null;
        }
        return energy;
    }

    private CompressorRecipe currentRecipe = null;
    private int currentProcessingTime = 0;

    public void onTick() {
        if (this.level == null || this.level.isClientSide) return;
        energy.resetTransfer();

        if (!isWorking) {
            // Essayer de trouver une recette
            ItemStack input = items.get(0);
            if (!input.isEmpty()) {
                Optional<RecipeHolder<CompressorRecipe>> recipeOpt = level.getRecipeManager().getRecipeFor(CompressorRecipeType.INSTANCE, new SingleRecipeInput(input), level);

                if (recipeOpt.isPresent()) {
                    CompressorRecipe recipe = recipeOpt.get().value();
                    ItemStack output = items.get(2);
                    ItemStack result = recipe.getOutput();

                    // Vérifier si on peut sortir le résultat
                    boolean canOutput = output.isEmpty() || (ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize());

                    // Vérifier si on a assez d'énergie
                    if (canOutput && energy.getEnergyStored() >= recipe.getEnergyCost()) {
                        // Démarrer le craft
                        energy.extractEnergy(recipe.getEnergyCost(), false);
                        input.shrink(1);
                        isWorking = true;
                        currentRecipe = recipe;
                        currentProcessingTime = recipe.getProcessingTime();
                        animTicks = currentProcessingTime;
                        setChanged();
                        updateAnimState();
                    }
                }
            }
        } else {
            // Traitement en cours
            if (animTicks > 0) {
                animTicks--;
                updateAnimState();

                if (animTicks == 0 && currentRecipe != null) {
                    // Craft terminé
                    ItemStack output = items.get(2);
                    ItemStack result = currentRecipe.getOutput().copy();

                    if (output.isEmpty()) {
                        items.set(2, result);
                    } else if (ItemStack.isSameItemSameComponents(output, result)) {
                        output.grow(result.getCount());
                    }

                    isWorking = false;
                    currentRecipe = null;
                    currentProcessingTime = 0;
                    setChanged();
                }
            }
        }
    }

    // Expose progression pour Jade/overlay
    public int getProgress() {
        return isWorking ? (currentProcessingTime - Math.max(0, animTicks)) : 0;
    }

    public int getMaxProgress() {
        return currentProcessingTime > 0 ? currentProcessingTime : 20;
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.erinium_faction.titanium_compressor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new fr.eriniumgroup.erinium_faction.gui.menus.TitaniumCompressorMenu(id, inv, this, this.data);
    }

    // Container implementation
    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        ItemStack res = ContainerHelper.removeItem(items, index, count);
        if (!res.isEmpty()) setChanged();
        return res;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        ItemStack s = items.get(index);
        if (s.isEmpty()) return ItemStack.EMPTY;
        items.set(index, ItemStack.EMPTY);
        return s;
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        items.set(index, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("animTicks", animTicks);
        tag.putBoolean("working", isWorking);
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("lastIn", energy.getLastReceived());
        tag.putInt("lastOut", energy.getLastExtracted());
        tag.put("faceConfig", faceConfig.save());
        ContainerHelper.saveAllItems(tag, this.items, provider);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        this.animTicks = tag.getInt("animTicks");
        this.isWorking = tag.getBoolean("working");
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        this.energy.setEnergy(tag.getInt("energy"));
        // Restaurer les valeurs de transfert pour Jade
        if (tag.contains("lastIn")) {
            this.energy.lastReceivedTick = tag.getInt("lastIn");
        }
        if (tag.contains("lastOut")) {
            this.energy.lastExtractedTick = tag.getInt("lastOut");
        }
        if (tag.contains("faceConfig")) {
            faceConfig.load(tag.getCompound("faceConfig"));
        }
        ContainerHelper.loadAllItems(tag, this.items, provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void onDataPacket(net.minecraft.network.@NotNull Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider provider) {
        var tag = pkt.getTag();
        loadAdditional(tag, provider);
    }

    public int getLastInPerTick() {
        return energy.getLastReceived();
    }

    public int getLastUsePerTick() {
        return energy.getLastExtracted();
    }

    // Implémentation de IConfigurableMachine
    @Override
    @NotNull
    public FaceConfiguration getFaceConfiguration() {
        return faceConfig;
    }

    @Override
    public void setFaceMode(Direction face, FaceMode mode) {
        faceConfig.setFaceMode(face, mode);
        setChanged();
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