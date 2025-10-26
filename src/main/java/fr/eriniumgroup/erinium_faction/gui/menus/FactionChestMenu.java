package fr.eriniumgroup.erinium_faction.gui.menus;

import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

public class FactionChestMenu extends AbstractContainerMenu {
    private final String factionId;
    private final Faction faction;
    private final FactionChestContainer container;
    private final int chestSize;

    // Container personnalisé pour le coffre de faction
    private static class FactionChestContainer implements Container {
        private final NonNullList<ItemStack> items;
        private final Faction faction;
        private final Player player;

        public FactionChestContainer(Faction faction, Player player) {
            this.items = NonNullList.withSize(27, ItemStack.EMPTY);
            this.faction = faction;
            this.player = player;

            // Charger les items depuis la faction
            if (faction != null) {
                for (int i = 0; i < 27; i++) {
                    ItemStack stored = faction.getChestItem(i);
                    this.items.set(i, stored.isEmpty() ? ItemStack.EMPTY : stored.copy());
                }
            }
        }

        @Override
        public int getContainerSize() {
            return 27;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
            return items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = items.get(slot);
            ItemStack result = stack.split(amount);
            if (stack.isEmpty()) {
                items.set(slot, ItemStack.EMPTY);
            }
            setChanged();
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = items.get(slot);
            items.set(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, stack);
                setChanged();
            }
        }

        @Override
        public void setChanged() {
            // Sauvegarder dans la faction quand le container change
            if (faction != null && player != null && !player.level().isClientSide) {
                for (int i = 0; i < 27; i++) {
                    ItemStack stack = items.get(i);
                    faction.setChestItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                }
                FactionManager.markDirty();
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return faction != null && faction.hasPermission(player.getUUID(), "faction.chest.access");
        }

        @Override
        public void clearContent() {
            items.clear();
            setChanged();
        }
    }

    // Constructeur serveur
    public FactionChestMenu(int id, Inventory playerInv, String factionId) {
        super(EFMenus.FACTION_CHEST_MENU.get(), id);
        this.factionId = factionId;
        this.faction = FactionManager.getFaction(factionId);

        if (faction != null) {
            this.chestSize = faction.getChestSize();
            this.container = new FactionChestContainer(faction, playerInv.player);
        } else {
            this.chestSize = 9;
            this.container = new FactionChestContainer(null, playerInv.player);
        }

        layoutSlots(playerInv);
    }

    // Constructeur client
    public FactionChestMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, buf.readUtf());
    }

    private void layoutSlots(Inventory playerInv) {
        int rows = chestSize / 9;

        // Slots du coffre de faction
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                addSlot(new Slot(container, index, 8 + col * 18, 18 + row * 18));
            }
        }

        int yOffset = 18 + rows * 18 + 14;

        // Inventaire du joueur
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, yOffset + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, yOffset + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            int containerSlots = chestSize;

            if (index < containerSlots) {
                // Du coffre vers l'inventaire
                if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // De l'inventaire vers le coffre
                if (!this.moveItemStackTo(stack, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        if (faction == null) return false;

        // Vérifier que le joueur a la permission d'accéder au coffre
        return faction.hasPermission(player.getUUID(), "faction.chest.access");
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Sauvegarder tous les items quand le menu se ferme
        if (faction != null && !player.level().isClientSide) {
            for (int i = 0; i < 27; i++) {
                ItemStack stack = container.getItem(i);
                faction.setChestItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            FactionManager.markDirty();

            // Log pour débug
            //fr.eriniumgroup.erinium_faction.core.EFC.log.info("Coffre fermé - " + faction.getName() + " - Items sauvegardés");
        }
    }

    public int getChestSize() {
        return chestSize;
    }

    public String getFactionId() {
        return factionId;
    }
}

