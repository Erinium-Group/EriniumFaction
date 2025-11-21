package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.recipe.CompressorRecipe;
import fr.eriniumgroup.erinium_faction.common.recipe.CompressorRecipeType;
import fr.eriniumgroup.erinium_faction.common.recipe.RocketMakerRecipe;
import fr.eriniumgroup.erinium_faction.common.recipe.RocketMakerRecipeType;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Enregistrement des recettes personnalisées
 */
public class EFRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, EFC.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, EFC.MOD_ID);

    // Compressor Recipe Type
    public static final DeferredHolder<RecipeType<?>, RecipeType<CompressorRecipe>> COMPRESSING =
        RECIPE_TYPES.register("compressing", () -> CompressorRecipeType.INSTANCE);

    // Compressor Recipe Serializer
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressorRecipe>> COMPRESSING_SERIALIZER =
        RECIPE_SERIALIZERS.register("compressing", () -> CompressorRecipe.CompressorRecipeSerializer.INSTANCE);

    // Rocket Maker Recipe Type
    public static final DeferredHolder<RecipeType<?>, RecipeType<RocketMakerRecipe>> ROCKET_MAKING =
        RECIPE_TYPES.register("rocket_making", () -> RocketMakerRecipeType.INSTANCE);

    // Rocket Maker Recipe Serializer
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RocketMakerRecipe>> ROCKET_MAKING_SERIALIZER =
        RECIPE_SERIALIZERS.register("rocket_making", () -> RocketMakerRecipe.RocketMakerRecipeSerializer.INSTANCE);
}

