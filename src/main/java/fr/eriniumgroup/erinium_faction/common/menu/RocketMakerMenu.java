package fr.eriniumgroup.erinium_faction.common.menu;

import fr.eriniumgroup.erinium_faction.common.blockentity.RocketMakerBlockEntity;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class RocketMakerMenu extends AbstractContainerMenu {
    private final RocketMakerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    int slotIndex = 0;

    // Client constructor
    public RocketMakerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    // Server constructor
    public RocketMakerMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity, ContainerData data) {
        super(EFMenus.ROCKET_MAKER.get(), containerId);
        this.blockEntity = (RocketMakerBlockEntity) blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        addDataSlots(data);

        // Forme de fusée - 63 craft slots total (exact pixel coordinates from user)
        // Organized by color groups from the coordinates list

        addSlot(183, 13, 2);
        addSlot(165, 31, 4);
        addSlot(129, 49, 7);
        addSlot(129, 67, 8);
        addSlot(129, 85, 7);
        addSlot(129, 103, 7);
        addSlot(129, 121, 7);
        addSlot(111, 139, 9);
        addSlot(93, 157, 12);

        // Output slot - jaune (slot 63)
        this.addSlot(new SlotItemHandler(this.blockEntity.getInventory(), slotIndex++, 325, 91) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false; // Slot output only
            }

            @Override
            public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                // Consume 1 item from each slot used in the recipe
                RocketMakerBlockEntity rocketMaker = (RocketMakerBlockEntity) blockEntity;
                for (int i = 0; i < 63; i++) {
                    ItemStack slotStack = rocketMaker.getInventory().getStackInSlot(i);
                    if (!slotStack.isEmpty()) {
                        slotStack.shrink(1);
                        rocketMaker.getInventory().setStackInSlot(i, slotStack);
                    }
                }
                super.onTake(player, stack);
            }
        });

        // Inventaire du joueur (9x3) - après craft (63) + output (1) = commence à slot 64 du menu
        // Position : X=92, Y=188
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    120 + col * 18, 185 + row * 18));
            }
        }

        // Hotbar (9x1)
        // Position : X=92, Y=246
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                120 + col * 18, 243));
        }
    }

    private void addSlot(int x, int y, int numberofSlot){
        int index = 0;
        while (index < numberofSlot){
            this.addSlot(new SlotItemHandler(this.blockEntity.getInventory(), slotIndex++, x + (18 * (index)), y));
            index++;
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // Total: 63 craft slots + 1 output = 64
            // Player inventory starts at index 64
            // From crafting grid or output
            if (index < 64) {
                if (!this.moveItemStackTo(slotStack, 64, 100, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to crafting grid
            else {
                if (!this.moveItemStackTo(slotStack, 0, 63, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        // Désactiver la vérification de distance pour cette GUI très grande (400x270)
        // La GUI étant très large, les clics sur les slots éloignés sont rejetés par la vérification standard
        // On vérifie seulement que le bloc existe toujours et n'a pas été détruit
        return level.getBlockEntity(blockEntity.getBlockPos()) == blockEntity;
    }

    public RocketMakerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
