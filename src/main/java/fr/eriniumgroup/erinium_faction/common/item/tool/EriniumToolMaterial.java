package fr.eriniumgroup.erinium_faction.common.item.tool;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

/**
 * Matériau d'outils Erinium - Niveau Netherite
 */
public class EriniumToolMaterial implements Tier {

    public static final EriniumToolMaterial INSTANCE = new EriniumToolMaterial();

    private EriniumToolMaterial() {
    }

    @Override
    public int getUses() {
        // Durabilité - même que netherite: 2031
        return 2031;
    }

    @Override
    public float getSpeed() {
        // Vitesse de minage - même que netherite: 9.0
        return 9.0f;
    }

    @Override
    public float getAttackDamageBonus() {
        // Dégâts bonus - même que netherite: 4.0
        return 4.0f;
    }

    @Override
    public int getEnchantmentValue() {
        // Enchantability - même que netherite: 15
        return 15;
    }

    @Override
    public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
        // Peut miner les mêmes blocs que netherite (level netherite)
        return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
    }

    @Override
    public Ingredient getRepairIngredient() {
        // TODO: utiliser l'erinium_ingot pour réparer
        return Ingredient.EMPTY;
    }
}
