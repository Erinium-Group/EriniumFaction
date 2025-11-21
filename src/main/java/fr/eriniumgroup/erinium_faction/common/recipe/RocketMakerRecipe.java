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
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Recette pour le Rocket Maker
 * Supporte des patterns de craft custom en forme de fusée (grille 63 slots)
 */
public class RocketMakerRecipe implements Recipe<RecipeInput> {
    private final Map<Integer, Ingredient> pattern; // Position du slot -> Ingredient requis
    private final ItemStack output;

    public RocketMakerRecipe(Map<Integer, Ingredient> pattern, ItemStack output) {
        this.pattern = pattern;
        this.output = output;
    }

    /**
     * Vérifie si l'inventaire du Rocket Maker correspond à cette recette
     */
    public boolean matches(ItemStackHandler inventory, Level level) {
        if (level.isClientSide()) return false;

        // Vérifier que tous les slots requis contiennent le bon item
        for (Map.Entry<Integer, Ingredient> entry : pattern.entrySet()) {
            int slotIndex = entry.getKey();
            Ingredient requiredIngredient = entry.getValue();

            if (slotIndex >= 63) continue; // Ignorer les slots invalides (output est slot 63)

            ItemStack stackInSlot = inventory.getStackInSlot(slotIndex);

            if (!requiredIngredient.test(stackInSlot)) {
                return false; // Item incorrect ou slot vide
            }
        }

        // Vérifier que les slots NON définis dans le pattern sont vides
        for (int i = 0; i < 63; i++) {
            if (!pattern.containsKey(i)) {
                if (!inventory.getStackInSlot(i).isEmpty()) {
                    return false; // Slot doit être vide mais contient un item
                }
            }
        }

        return true;
    }

    @Override
    public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
        // Cette méthode n'est pas utilisée car on utilise matches(ItemStackHandler, Level)
        // Mais elle est requise par l'interface Recipe
        return false;
    }

    @Override
    @NotNull
    public ItemStack assemble(@NotNull RecipeInput input, HolderLookup.@NotNull Provider registries) {
        return this.output.copy();
    }

    public ItemStack assemble(ItemStackHandler inventory) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true; // Pas de contrainte de dimensions pour notre grille custom
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
        list.addAll(pattern.values());
        return list;
    }

    public Map<Integer, Ingredient> getPattern() {
        return pattern;
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer() {
        return RocketMakerRecipeSerializer.INSTANCE;
    }

    @Override
    @NotNull
    public RecipeType<?> getType() {
        return RocketMakerRecipeType.INSTANCE;
    }

    /**
     * Serializer pour les recettes de Rocket Maker
     */
    public static class RocketMakerRecipeSerializer implements RecipeSerializer<RocketMakerRecipe> {
        public static final RocketMakerRecipeSerializer INSTANCE = new RocketMakerRecipeSerializer();

        // Codec pour lire/écrire les recettes depuis JSON
        private static final MapCodec<RocketMakerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.unboundedMap(Codec.STRING, Ingredient.CODEC_NONEMPTY)
                                .fieldOf("pattern")
                                .xmap(
                                        // Convert String keys to Integer
                                        stringMap -> {
                                            Map<Integer, Ingredient> intMap = new HashMap<>();
                                            stringMap.forEach((key, value) -> intMap.put(Integer.parseInt(key), value));
                                            return intMap;
                                        },
                                        // Convert Integer keys to String
                                        intMap -> {
                                            Map<String, Ingredient> stringMap = new HashMap<>();
                                            intMap.forEach((key, value) -> stringMap.put(String.valueOf(key), value));
                                            return stringMap;
                                        }
                                )
                                .forGetter(r -> r.pattern),
                        ItemStack.CODEC
                                .fieldOf("result")
                                .forGetter(r -> r.output)
                ).apply(instance, (pattern, result) -> new RocketMakerRecipe(pattern, result))
        );

        // StreamCodec pour la synchronisation réseau (serveur -> client)
        private static final StreamCodec<RegistryFriendlyByteBuf, RocketMakerRecipe> STREAM_CODEC = StreamCodec.of(
                RocketMakerRecipeSerializer::toNetwork,
                RocketMakerRecipeSerializer::fromNetwork
        );

        @Override
        @NotNull
        public MapCodec<RocketMakerRecipe> codec() {
            return CODEC;
        }

        @Override
        @NotNull
        public StreamCodec<RegistryFriendlyByteBuf, RocketMakerRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        // Lire depuis le réseau (buffer)
        private static RocketMakerRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buf) {
            int patternSize = buf.readVarInt();
            Map<Integer, Ingredient> pattern = new HashMap<>();

            for (int i = 0; i < patternSize; i++) {
                int slotIndex = buf.readVarInt();
                Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                pattern.put(slotIndex, ingredient);
            }

            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            return new RocketMakerRecipe(pattern, output);
        }

        // Écrire sur le réseau (buffer)
        private static void toNetwork(@NotNull RegistryFriendlyByteBuf buf, @NotNull RocketMakerRecipe recipe) {
            buf.writeVarInt(recipe.pattern.size());

            for (Map.Entry<Integer, Ingredient> entry : recipe.pattern.entrySet()) {
                buf.writeVarInt(entry.getKey());
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, entry.getValue());
            }

            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
        }
    }
}
