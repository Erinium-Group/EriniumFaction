package fr.eriniumgroup.erinium_faction.player.level.gui;

import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Menu pour l'interface de distribution des points d'attributs
 */
public class PlayerStatsMenu extends AbstractContainerMenu {

    public PlayerStatsMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv);
    }

    public PlayerStatsMenu(int id, Inventory inv) {
        super(EFMenus.PLAYER_STATS_MENU.get(), id);

        // Pas de slots nécessaires pour ce menu
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

