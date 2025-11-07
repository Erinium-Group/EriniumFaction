package fr.eriniumgroup.erinium_faction.compat.jei;

import fr.eriniumgroup.erinium_faction.common.recipe.CompressorRecipe;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.init.EFBlocks;
import fr.eriniumgroup.erinium_faction.init.EFRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Plugin JEI pour Erinium Faction
 */
@JeiPlugin
public class EriniumJEIPlugin implements IModPlugin {

    @Override
    @NotNull
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(EFC.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CompressorRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        RecipeManager recipeManager = minecraft.level.getRecipeManager();

        // Récupérer toutes les recettes de compressor
        List<CompressorRecipe> compressorRecipes = recipeManager.getAllRecipesFor(EFRecipes.COMPRESSING.get())
            .stream()
            .map(RecipeHolder::value)
            .toList();

        registration.addRecipes(CompressorRecipeCategory.RECIPE_TYPE, compressorRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Enregistrer le Titanium Compressor comme catalyseur pour les recettes
        registration.addRecipeCatalyst(
                new ItemStack(EFBlocks.TITANIUM_COMPRESSOR.get()),
                CompressorRecipeCategory.RECIPE_TYPE
        );
    }
}