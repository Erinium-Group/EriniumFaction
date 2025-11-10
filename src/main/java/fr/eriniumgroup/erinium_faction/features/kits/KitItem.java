package fr.eriniumgroup.erinium_faction.features.kits;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Représente un item dans un kit avec tous ses paramètres
 * Format de configuration TOML simple et lisible
 */
public class KitItem {
    private final String itemId;
    private final int count;
    private final String armorSlot; // "HEAD", "CHEST", "LEGS", "FEET", "OFFHAND", ou null
    private final Map<String, Integer> enchantments;
    private final String displayName;
    private final List<String> lore;

    public KitItem(String itemId, int count, String armorSlot, Map<String, Integer> enchantments,
                   String displayName, List<String> lore) {
        this.itemId = itemId;
        this.count = count;
        this.armorSlot = armorSlot;
        this.enchantments = enchantments != null ? enchantments : new HashMap<>();
        this.displayName = displayName;
        this.lore = lore;
    }

    /**
     * Parse une string de configuration en KitItem
     * Format: "minecraft:diamond_sword,1,enchants:{sharpness:5,unbreaking:3},name:Super Sword,lore:[Line 1,Line 2]"
     */
    public static KitItem parse(String configString) {
        String[] parts = configString.split(",(?![^\\[\\]]*\\]|[^{}]*})"); // Split sans casser les arrays/maps

        String itemId = parts[0].trim();
        int count = parts.length > 1 ? parseIntSafe(parts[1].trim(), 1) : 1;
        String armorSlot = null;
        Map<String, Integer> enchantments = new HashMap<>();
        String displayName = null;
        List<String> lore = null;

        // Parser les paramètres optionnels
        for (int i = 2; i < parts.length; i++) {
            String param = parts[i].trim();

            if (param.startsWith("slot:")) {
                armorSlot = param.substring(5).trim().toUpperCase();
            } else if (param.startsWith("enchants:{")) {
                enchantments = parseEnchants(param);
            } else if (param.startsWith("name:")) {
                displayName = param.substring(5).trim();
            } else if (param.startsWith("lore:[")) {
                lore = parseLore(param);
            }
        }

        return new KitItem(itemId, count, armorSlot, enchantments, displayName, lore);
    }

    /**
     * Parse les enchantements depuis le format {enchant:level,enchant:level}
     */
    private static Map<String, Integer> parseEnchants(String enchantsString) {
        Map<String, Integer> enchants = new HashMap<>();

        // Extraire le contenu entre {}
        String content = enchantsString.substring(enchantsString.indexOf('{') + 1, enchantsString.lastIndexOf('}'));

        if (content.trim().isEmpty()) return enchants;

        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                enchants.put(kv[0].trim(), parseIntSafe(kv[1].trim(), 1));
            }
        }

        return enchants;
    }

    /**
     * Parse la lore depuis le format [line1,line2,line3]
     */
    private static List<String> parseLore(String loreString) {
        // Extraire le contenu entre []
        String content = loreString.substring(loreString.indexOf('[') + 1, loreString.lastIndexOf(']'));

        if (content.trim().isEmpty()) return null;

        return List.of(content.split(","));
    }

    private static int parseIntSafe(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Crée l'ItemStack depuis cette configuration
     */
    public ItemStack createItemStack(ServerLevel level) {
        // Trouver l'item par son ID
        ResourceLocation itemLocation = ResourceLocation.parse(itemId);
        Item item = BuiltInRegistries.ITEM.get(itemLocation);

        if (item == null) {
            System.err.println("§c[KIT] Item inconnu: " + itemId);
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item, count);

        // Appliquer les enchantements
        if (!enchantments.isEmpty()) {
            ItemEnchantments.Mutable mutableEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                ResourceLocation enchantLocation = ResourceLocation.parse(entry.getKey());

                level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolder(enchantLocation)
                        .ifPresent(enchantHolder -> {
                            mutableEnchants.set(enchantHolder, entry.getValue());
                        });
            }

            stack.set(DataComponents.ENCHANTMENTS, mutableEnchants.toImmutable());
        }

        // Appliquer le nom custom
        if (displayName != null && !displayName.isEmpty()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));
        }

        // Appliquer la lore
        if (lore != null && !lore.isEmpty()) {
            List<Component> loreComponents = lore.stream()
                    .map(line -> (Component) Component.literal(line))
                    .toList();
            stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(loreComponents));
        }

        return stack;
    }

    public String getArmorSlot() {
        return armorSlot;
    }

    public String getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }
}
