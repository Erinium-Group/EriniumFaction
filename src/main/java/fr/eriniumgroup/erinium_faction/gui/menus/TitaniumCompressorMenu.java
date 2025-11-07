package fr.eriniumgroup.erinium_faction.gui.menus;

import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCompressorBlockEntity;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TitaniumCompressorMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final TitaniumCompressorBlockEntity blockEntity;
    private final ContainerData data;

    public TitaniumCompressorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (TitaniumCompressorBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(1));
    }

    public TitaniumCompressorMenu(int id, Inventory inv, TitaniumCompressorBlockEntity be, ContainerData data) {
        super(EFMenus.TITANIUM_COMPRESSOR_MENU.get(), id);
        this.blockEntity = be;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());
        this.data = data;
        addDataSlots(this.data);

        // le BE implémente Container
        // Slots: 0=input, 2=output (1 réservé futur)
        this.addSlot(new Slot(be, 0, 56, 35));
        this.addSlot(new Slot(be, 2, 116, 35) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

        // Player inventory
        int startY = 84;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, startY + 58));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public TitaniumCompressorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index == 1) { // output -> player
                if (!this.moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, itemstack);
            } else if (index != 0) { // player inv -> input
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    if (index < 29) {
                        if (!this.moveItemStackTo(stack, 29, 38, false)) return ItemStack.EMPTY;
                    } else if (!this.moveItemStackTo(stack, 2, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(stack, 2, 38, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
            if (stack.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return itemstack;
    }
}
