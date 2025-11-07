package fr.eriniumgroup.erinium_faction.gui.menus;

import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EriniumChestMenu extends AbstractContainerMenu {
    private final Container container;
    private static final int CHEST_ROWS = 6;
    private static final int CHEST_SLOTS = CHEST_ROWS * 9;

    // Constructeur pour le client (appelé via le réseau)
    public EriniumChestMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(CHEST_SLOTS));
    }

    // Constructeur pour le serveur
    public EriniumChestMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CHEST_SLOTS));
    }

    public EriniumChestMenu(int containerId, Inventory playerInventory, Container container) {
        super(EFMenus.ERINIUM_CHEST.get(), containerId);
        checkContainerSize(container, CHEST_SLOTS);
        this.container = container;
        container.startOpen(playerInventory.player);

        // Slots du coffre (6 lignes de 9 slots)
        for (int row = 0; row < CHEST_ROWS; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Inventaire du joueur (3 lignes)
        int startY = 18 + CHEST_ROWS * 18 + 14; // Position après le coffre + marge
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }

        // Hotbar du joueur
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, startY + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();

            if (index < CHEST_SLOTS) {
                // Depuis le coffre vers l'inventaire du joueur
                if (!this.moveItemStackTo(slotItem, CHEST_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Depuis l'inventaire du joueur vers le coffre
                if (!this.moveItemStackTo(slotItem, 0, CHEST_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public Container getContainer() {
        return this.container;
    }
}
