package fr.eriniumgroup.erinium_faction.features.kits;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un kit avec tous ses items et permissions
 */
public class Kit {
    private final String id;
    private final String displayName;
    private final String description;
    private final String requiredRank; // Nom du rank requis (null = accessible à tous)
    private final List<KitItem> items;
    private final int cooldownMinutes; // Cooldown en minutes (0 = pas de cooldown)

    public Kit(String id, String displayName, String description, String requiredRank, List<KitItem> items, int cooldownMinutes) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.requiredRank = requiredRank;
        this.items = items != null ? items : new ArrayList<>();
        this.cooldownMinutes = Math.max(0, cooldownMinutes); // Minimum 0
    }

    // Constructor pour rétrocompatibilité (cooldown par défaut = 60 minutes)
    public Kit(String id, String displayName, String description, String requiredRank, List<KitItem> items) {
        this(id, displayName, description, requiredRank, items, 60);
    }

    /**
     * Vérifie si le joueur a assez de place pour recevoir le kit
     * @return true si le joueur a assez de place, false sinon
     */
    public boolean canReceive(ServerPlayer player, ServerLevel level) {
        int requiredSlots = 0;
        boolean needsHead = false, needsChest = false, needsLegs = false, needsFeet = false, needsOffhand = false;

        for (KitItem kitItem : items) {
            String slot = kitItem.getArmorSlot();
            if (slot != null) {
                // Vérifier les slots d'équipement
                switch (slot) {
                    case "HEAD" -> {
                        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) needsHead = true;
                    }
                    case "CHEST" -> {
                        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) needsChest = true;
                    }
                    case "LEGS" -> {
                        if (!player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) needsLegs = true;
                    }
                    case "FEET" -> {
                        if (!player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) needsFeet = true;
                    }
                    case "OFFHAND" -> {
                        if (!player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) needsOffhand = true;
                    }
                    default -> requiredSlots++;
                }
            } else {
                requiredSlots++;
            }
        }

        // Compter les slots libres dans l'inventaire
        int freeSlots = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                freeSlots++;
            }
        }

        // Si un slot d'équipement est occupé, il faudra de la place dans l'inventaire
        if (needsHead) requiredSlots++;
        if (needsChest) requiredSlots++;
        if (needsLegs) requiredSlots++;
        if (needsFeet) requiredSlots++;
        if (needsOffhand) requiredSlots++;

        return freeSlots >= requiredSlots;
    }

    /**
     * Donne le kit au joueur
     * @return true si le kit a été donné, false si pas assez de place
     */
    public boolean give(ServerPlayer player, ServerLevel level) {
        // Vérifier d'abord qu'il y a assez de place
        if (!canReceive(player, level)) {
            return false;
        }

        for (KitItem kitItem : items) {
            ItemStack stack = kitItem.createItemStack(level);

            if (stack.isEmpty()) continue;

            // Si c'est un item d'armure/offhand
            String slot = kitItem.getArmorSlot();
            if (slot != null) {
                switch (slot) {
                    case "HEAD" -> {
                        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                            // Mettre l'item actuel dans l'inventaire
                            addToInventory(player, player.getItemBySlot(EquipmentSlot.HEAD).copy());
                        }
                        player.setItemSlot(EquipmentSlot.HEAD, stack);
                    }
                    case "CHEST" -> {
                        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                            addToInventory(player, player.getItemBySlot(EquipmentSlot.CHEST).copy());
                        }
                        player.setItemSlot(EquipmentSlot.CHEST, stack);
                    }
                    case "LEGS" -> {
                        if (!player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
                            addToInventory(player, player.getItemBySlot(EquipmentSlot.LEGS).copy());
                        }
                        player.setItemSlot(EquipmentSlot.LEGS, stack);
                    }
                    case "FEET" -> {
                        if (!player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
                            addToInventory(player, player.getItemBySlot(EquipmentSlot.FEET).copy());
                        }
                        player.setItemSlot(EquipmentSlot.FEET, stack);
                    }
                    case "OFFHAND" -> {
                        if (!player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
                            addToInventory(player, player.getItemBySlot(EquipmentSlot.OFFHAND).copy());
                        }
                        player.setItemSlot(EquipmentSlot.OFFHAND, stack);
                    }
                    default -> addToInventory(player, stack);
                }
            } else {
                // Sinon ajouter à l'inventaire
                addToInventory(player, stack);
            }
        }

        return true;
    }

    /**
     * Ajoute un item à l'inventaire du joueur
     */
    private void addToInventory(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getRequiredRank() {
        return requiredRank;
    }

    public List<KitItem> getItems() {
        return items;
    }

    public boolean hasRequiredRank() {
        return requiredRank != null && !requiredRank.isEmpty();
    }

    public int getCooldownMinutes() {
        return cooldownMinutes;
    }

    /**
     * Retourne le cooldown en secondes
     */
    public long getCooldownSeconds() {
        return cooldownMinutes * 60L;
    }
}
