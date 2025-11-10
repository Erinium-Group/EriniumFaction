package fr.eriniumgroup.erinium_faction.common.item.armor;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Matériau d'armure Erinium - Niveau Netherite
 */
public class EriniumArmorMaterial {

    public static final ArmorMaterial ERINIUM = new ArmorMaterial(
        // Durabilité par slot (même que netherite)
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 481);
            map.put(ArmorItem.Type.LEGGINGS, 555);
            map.put(ArmorItem.Type.CHESTPLATE, 592);
            map.put(ArmorItem.Type.HELMET, 407);
            map.put(ArmorItem.Type.BODY, 592);
        }),
        // Enchantability (même que netherite: 15)
        15,
        // Son d'équipement
        SoundEvents.ARMOR_EQUIP_NETHERITE,
        // Ingrédient de réparation (TODO: créer l'item erinium_ingot)
        () -> Ingredient.EMPTY,
        // Liste des couches d'armure
        List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath("erinium_faction", "erinium")
            )
        ),
        // Toughness (résistance aux dégâts) - même que netherite: 3.0
        3.0F,
        // Knockback resistance - même que netherite: 0.1
        0.1F
    );
}
