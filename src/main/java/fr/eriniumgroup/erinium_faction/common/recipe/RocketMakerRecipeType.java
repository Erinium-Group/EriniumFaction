package fr.eriniumgroup.erinium_faction.common.recipe;

import net.minecraft.world.item.crafting.RecipeType;

/**
 * Type de recette pour le Rocket Maker
 */
public class RocketMakerRecipeType implements RecipeType<RocketMakerRecipe> {
    public static final RocketMakerRecipeType INSTANCE = new RocketMakerRecipeType();
    public static final String ID = "rocket_making";

    private RocketMakerRecipeType() {
    }
}
