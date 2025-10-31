package fr.eriniumgroup.erinium_faction.gui.menus;

import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.core.faction.Permission;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Slot personnalisé pour le coffre de faction
 * Vérifie la permission MANAGE_CHEST avant de permettre toute interaction
 */
public class FactionChestSlot extends SlotItemHandler {
    private final Player player;
    private final Faction faction;
    private final FactionMenu menu;

    public FactionChestSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Player player, Faction faction, FactionMenu menu) {
        super(itemHandler, index, xPosition, yPosition);
        this.player = player;
        this.faction = faction;
        this.menu = menu;
    }

    /**
     * Vérifie si le joueur a la permission MANAGE_CHEST
     */
    private boolean hasManageChestPermission() {
        if (player == null) {
            return false;
        }

        // Côté serveur: utiliser la faction
        if (faction != null) {
            return faction.hasPermission(player.getUUID(), Permission.MANAGE_CHEST.getServerKey());
        }

        // Côté client: utiliser le snapshot
        if (menu != null && menu.snapshot != null) {
            return menu.snapshot.hasPermission(player.getUUID(), Permission.MANAGE_CHEST);
        }

        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Vérifie la permission avant de permettre le placement
        boolean hasPermission = hasManageChestPermission();
        System.out.println("[FactionChestSlot] mayPlace called - hasPermission=" + hasPermission + ", stack=" + stack);
        if (!hasPermission) {
            return false;
        }
        return super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        // Vérifie la permission avant de permettre de prendre un item
        boolean hasPermission = hasManageChestPermission();
        System.out.println("[FactionChestSlot] mayPickup called - hasPermission=" + hasPermission);
        if (!hasPermission) {
            return false;
        }
        return super.mayPickup(player);
    }

    @Override
    public ItemStack remove(int amount) {
        // Vérifie la permission avant de retirer des items
        if (!hasManageChestPermission()) {
            return ItemStack.EMPTY;
        }
        return super.remove(amount);
    }
}
