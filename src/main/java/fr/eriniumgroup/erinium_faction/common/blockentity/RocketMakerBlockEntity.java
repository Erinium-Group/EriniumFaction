package fr.eriniumgroup.erinium_faction.common.blockentity;

import fr.eriniumgroup.erinium_faction.common.menu.RocketMakerMenu;
import fr.eriniumgroup.erinium_faction.common.recipe.RocketMakerRecipe;
import fr.eriniumgroup.erinium_faction.common.recipe.RocketMakerRecipeType;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RocketMakerBlockEntity extends BlockEntity implements MenuProvider {
    // 63 craft slots + 1 output slot = 64 total
    private final ItemStackHandler inventory = new ItemStackHandler(64) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // Check recipe and update output slot when craft grid changes
            if (slot < 63) { // Craft grid changed (slots 0-62)
                checkAndUpdateRecipe();
            }
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public RocketMakerBlockEntity(BlockPos pos, BlockState state) {
        super(EFBlockEntities.ROCKET_MAKER.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    private void checkAndUpdateRecipe() {
        if (level == null || level.isClientSide()) return;

        // Check if grid is completely empty
        boolean allEmpty = true;
        for (int i = 0; i < 63; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                allEmpty = false;
                break;
            }
        }

        // Clear output if grid is empty
        if (allEmpty) {
            inventory.setStackInSlot(63, ItemStack.EMPTY);
            return;
        }

        // Search for matching recipe
        Optional<RecipeHolder<RocketMakerRecipe>> recipeOptional = level.getRecipeManager()
                .getAllRecipesFor(RocketMakerRecipeType.INSTANCE)
                .stream()
                .filter(holder -> holder.value().matches(inventory, level))
                .findFirst();

        if (recipeOptional.isPresent()) {
            // Recipe found, set output
            RocketMakerRecipe recipe = recipeOptional.get().value();
            ItemStack result = recipe.assemble(inventory);
            inventory.setStackInSlot(63, result);
        } else {
            // No matching recipe, clear output
            inventory.setStackInSlot(63, ItemStack.EMPTY);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.erinium_faction.rocket_maker");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new RocketMakerMenu(containerId, playerInventory, this, this.data);
    }
}
