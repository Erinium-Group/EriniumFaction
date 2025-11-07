package fr.eriniumgroup.erinium_faction.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

/**
 * Type de recette pour le Titanium Compressor
 */
public class CompressorRecipeType implements RecipeType<CompressorRecipe> {
    public static final CompressorRecipeType INSTANCE = new CompressorRecipeType();
    public static final String ID = "compressing";

    private CompressorRecipeType() {
    }
}