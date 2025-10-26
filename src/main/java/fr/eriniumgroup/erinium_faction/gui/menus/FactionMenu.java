package fr.eriniumgroup.erinium_faction.gui.menus;

import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FactionMenu extends AbstractContainerMenu implements EFMenus.MenuAccessor {
    // Chest slots configuration
    public static final int FACTION_CHEST_ROWS = 3;
    public static final int FACTION_CHEST_SLOTS = FACTION_CHEST_ROWS * 9; // 27 slots
    public final Map<String, Object> menuState = new HashMap<>() {
        @Override
        public Object put(String key, Object value) {
            if (!this.containsKey(key) && this.size() >= 1) return null;
            return super.put(key, value);
        }
    };
    public final Level world;
    public final Player entity;
    public int x, y, z;
    public Faction faction;
    public String factionName; // nom de faction transmis
    public FactionSnapshot snapshot; // données sérialisées pour l'affichage client
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    private IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;
    private Supplier<Boolean> boundItemMatcher = null;
    private Entity boundEntity = null;
    private BlockEntity boundBlockEntity = null;

    public FactionMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(EFMenus.FACTION_MENU.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();
        this.internal = new ItemStackHandler(FACTION_CHEST_SLOTS);
        BlockPos pos;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            access = ContainerLevelAccess.create(world, pos);

            if (extraData.readableBytes() > 0) {
                int version = extraData.readVarInt();
                if (version >= 1) {
                    this.snapshot = FactionSnapshot.read(extraData);
                    this.factionName = (this.snapshot != null) ? this.snapshot.name : null;
                }
            }
        }
        if (this.factionName == null) {
            if (this.world.isClientSide()) {
                var vars = this.entity.getData(EFVariables.PLAYER_VARIABLES);
                this.factionName = (vars != null && vars.factionName != null && !vars.factionName.isEmpty()) ? vars.factionName : null;
            } else {
                this.factionName = FactionManager.getPlayerFaction(this.entity.getUUID());
            }
        }
        if (!this.world.isClientSide()) {
            this.faction = (this.factionName != null && !this.factionName.isEmpty()) ? FactionManager.getFaction(this.factionName) : null;
        } else {
            this.faction = null; // côté client, on se fie au snapshot/EFVariables
        }

        // Positions de base pour la ChestPage (scaled for 400x270)
        // CONTENT_X = 99, CONTENT_Y = 47, CONTENT_W = 275 (FactionPage constants)
        // On centre horizontalement: 99 + (275 / 2 - 9 * 18 / 2) = 99 + 57 = 156
        int baseX = 156;
        int baseChestY = 47 + 27; // 74
        int baseInvY = baseChestY + FACTION_CHEST_ROWS * 18 + 20; // 74 + 54 + 20 = 148
        int baseHotbarY = baseInvY + 3 * 18 + 8; // 148 + 54 + 8 = 210

        // Add faction chest slots
        int slotIndex = 0;
        for (int row = 0; row < FACTION_CHEST_ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                SlotItemHandler slot = new SlotItemHandler(this.internal, slotIndex,
                    baseX + col * 18, baseChestY + row * 18);
                this.addSlot(slot);
                this.customSlots.put(slotIndex, slot);
                slotIndex++;
            }
        }

        // Add player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                Slot slot = new Slot(inv, col + row * 9 + 9,
                    baseX + col * 18, baseInvY + row * 18);
                this.addSlot(slot);
                this.customSlots.put(slotIndex, slot);
                slotIndex++;
            }
        }

        // Add player hotbar
        for (int col = 0; col < 9; col++) {
            Slot slot = new Slot(inv, col, baseX + col * 18, baseHotbarY);
            this.addSlot(slot);
            this.customSlots.put(slotIndex, slot);
            slotIndex++;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.bound) {
            if (this.boundItemMatcher != null) return this.boundItemMatcher.get();
            else if (this.boundBlockEntity != null)
                return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
            else if (this.boundEntity != null) return this.boundEntity.isAlive();
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem();
            itemstack = currentStack.copy();

            // Faction chest slots: 0 to FACTION_CHEST_SLOTS-1
            // Player inventory: FACTION_CHEST_SLOTS to FACTION_CHEST_SLOTS+35

            if (index < FACTION_CHEST_SLOTS) {
                // Moving from faction chest to player inventory
                if (!this.moveItemStackTo(currentStack, FACTION_CHEST_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to faction chest
                if (!this.moveItemStackTo(currentStack, 0, FACTION_CHEST_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (currentStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public Map<Integer, Slot> getSlots() {
        return Collections.unmodifiableMap(customSlots);
    }

    @Override
    public Map<String, Object> getMenuState() {
        return menuState;
    }
}