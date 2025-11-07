package fr.eriniumgroup.erinium_faction.gui.menus;

import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCreativeBatteryBlockEntity;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeBatteryMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final TitaniumCreativeBatteryBlockEntity blockEntity;

    public CreativeBatteryMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (TitaniumCreativeBatteryBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public CreativeBatteryMenu(int id, Inventory inv, TitaniumCreativeBatteryBlockEntity be) {
        super(EFMenus.CREATIVE_BATTERY_MENU.get(), id);
        this.blockEntity = be;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public TitaniumCreativeBatteryBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}