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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Recette pour le Titanium Compressor
 */
public class CompressorRecipe implements Recipe<RecipeInput> {
    private final Ingredient input;
    private final ItemStack output;
    private final int processingTime; // en ticks (20 ticks = 1 seconde)
    private final int energyCost; // Coût en FE

    public CompressorRecipe(Ingredient input, ItemStack output, int processingTime, int energyCost) {
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
        this.energyCost = energyCost;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
        if (input.isEmpty()) return false;
        return this.input.test(input.getItem(0));
    }

    @Override
    @NotNull
    public ItemStack assemble(@NotNull RecipeInput input, HolderLookup.@NotNull Provider registries) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    @NotNull
    public ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return this.output;
    }

    @Override
    @NotNull
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.input);
        return list;
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer() {
        return CompressorRecipeSerializer.INSTANCE;
    }

    @Override
    @NotNull
    public RecipeType<?> getType() {
        return CompressorRecipeType.INSTANCE;
    }

    /**
     * Serializer pour les recettes de compressor
     */
    public static class CompressorRecipeSerializer implements RecipeSerializer<CompressorRecipe> {
        public static final CompressorRecipeSerializer INSTANCE = new CompressorRecipeSerializer();

        private static final MapCodec<CompressorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.input),
                        ItemStack.CODEC.fieldOf("result").forGetter(r -> r.output),
                        Codec.INT.optionalFieldOf("processingTime", 100).forGetter(r -> r.processingTime),
                        Codec.INT.optionalFieldOf("energyCost", 200).forGetter(r -> r.energyCost)
                ).apply(instance, CompressorRecipe::new)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> STREAM_CODEC = StreamCodec.of(
                CompressorRecipeSerializer::toNetwork,
                CompressorRecipeSerializer::fromNetwork
        );

        @Override
        @NotNull
        public MapCodec<CompressorRecipe> codec() {
            return CODEC;
        }

        @Override
        @NotNull
        public StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static CompressorRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buf) {
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            int processingTime = buf.readVarInt();
            int energyCost = buf.readVarInt();
            return new CompressorRecipe(input, output, processingTime, energyCost);
        }

        private static void toNetwork(@NotNull RegistryFriendlyByteBuf buf, @NotNull CompressorRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
            buf.writeVarInt(recipe.processingTime);
            buf.writeVarInt(recipe.energyCost);
        }
    }
}