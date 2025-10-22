/*
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside fr.eriniumgroup.eriniumfaction as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
*/
package fr.eriniumgroup.eriniumfaction;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;

public class ItemStackSerializer {

    /**
     * Convertit un ItemStack en String (NBT format)
     * Contient TOUT : enchantements, lore, display name, NBT custom, etc.
     */
    public static String itemStackToString(ItemStack itemStack, Level level) {
        if (itemStack.isEmpty()) {
            return "minecraft:air";
        }

        // Sauvegarder l'ItemStack complet en NBT
        CompoundTag nbtTag = new CompoundTag();
        itemStack.save(level.registryAccess(), nbtTag);

        // Convertir le NBT en String
        return nbtTag.toString();
    }

    /**
     * Recrée un ItemStack depuis un String NBT
     * Restaure TOUT : enchantements, lore, display name, NBT custom, etc.
     */
    public static void stringToItemStack(ItemStack targetItem, String nbtString, Level level) {
        try {
            if (nbtString == null || nbtString.isEmpty() || nbtString.equals("minecraft:air")) {
                return;
            }

            // Parser le String NBT
            CompoundTag nbtTag = TagParser.parseTag(nbtString);

            // Recréer l'ItemStack depuis le NBT
            ItemStack parsed = ItemStack.parse(level.registryAccess(), nbtTag).orElse(ItemStack.EMPTY);

            if (!parsed.isEmpty()) {
                // Appliquer tous les composants sur l'item target
                targetItem.applyComponents(parsed.getComponents());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Version Base64 (plus compact pour stockage DB)
     */
    public static String itemStackToBase64(ItemStack itemStack, Level level) {
        String nbtString = itemStackToString(itemStack, level);
        return java.util.Base64.getEncoder().encodeToString(nbtString.getBytes());
    }

    public static void base64ToItemStack(ItemStack targetItem, String base64, Level level) {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(base64);
            String nbtString = new String(decoded);
            stringToItemStack(targetItem, nbtString, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Afficher toutes les infos d'un ItemStack (debug)
     */
    public static String getItemInfo(ItemStack itemStack, Level level) {
        if (itemStack.isEmpty()) {
            return "Empty ItemStack";
        }

        StringBuilder info = new StringBuilder();

        // Item de base
        info.append("=== ITEM INFO ===\n");
        info.append("Item: ").append(BuiltInRegistries.ITEM.getKey(itemStack.getItem())).append("\n");
        info.append("Count: ").append(itemStack.getCount()).append("\n");

        // Display Name
        if (itemStack.has(DataComponents.CUSTOM_NAME)) {
            Component customName = itemStack.get(DataComponents.CUSTOM_NAME);
            info.append("Custom Name: ").append(customName.getString()).append("\n");
        }

        // Lore
        if (itemStack.has(DataComponents.LORE)) {
            info.append("Lore:\n");
            var lore = itemStack.get(DataComponents.LORE);
            for (Component line : lore.lines()) {
                info.append("  - ").append(line.getString()).append("\n");
            }
        }

        // Enchantements
        if (itemStack.has(DataComponents.ENCHANTMENTS)) {
            ItemEnchantments enchantments = itemStack.get(DataComponents.ENCHANTMENTS);
            info.append("Enchantments:\n");
            enchantments.entrySet().forEach(entry -> {
                info.append("  - ")
                        .append(entry.getKey().getRegisteredName())
                        .append(" ")
                        .append(entry.getIntValue())
                        .append("\n");
            });
        }

        // Durabilité
        if (itemStack.has(DataComponents.DAMAGE)) {
            int damage = itemStack.get(DataComponents.DAMAGE);
            int maxDamage = itemStack.getMaxDamage();
            info.append("Durability: ").append(maxDamage - damage).append("/").append(maxDamage).append("\n");
        }

        // Custom Data (NBT)
        if (itemStack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag customData = itemStack.get(DataComponents.CUSTOM_DATA).copyTag();
            info.append("Custom NBT Data:\n");
            info.append(customData.toString()).append("\n");
        }

        // NBT complet (pour debug)
        info.append("\n=== FULL NBT ===\n");
        info.append(itemStackToString(itemStack, level));

        return info.toString();
    }

    /**
     * Copier toutes les données d'un ItemStack vers un autre
     */
    public static ItemStack copyDataToItem(ItemStack source, ItemStack target) {
        if (source.isEmpty()) {
            return target;
        }

        // Créer le nouvel ItemStack avec l'item target
        ItemStack copy = new ItemStack(target.getItem(), source.getCount());

        // Copier TOUS les composants de source vers copy
        copy.applyComponents(source.getComponents());

        return copy;
    }
}