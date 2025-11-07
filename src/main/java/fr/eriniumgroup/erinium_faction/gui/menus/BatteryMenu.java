package fr.eriniumgroup.erinium_faction.gui.menus;

import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumBatteryTier1BlockEntity;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BatteryMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final TitaniumBatteryTier1BlockEntity blockEntity;

    public BatteryMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (TitaniumBatteryTier1BlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public BatteryMenu(int id, Inventory inv, TitaniumBatteryTier1BlockEntity be) {
        super(EFMenus.BATTERY_MENU.get(), id);
        this.blockEntity = be;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public TitaniumBatteryTier1BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}