package fr.eriniumgroup.erinium_faction.compat.jei;

import fr.eriniumgroup.erinium_faction.common.recipe.RocketMakerRecipe;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.init.EFBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Catégorie JEI pour les recettes du Rocket Maker
 * Affiche la grille en forme de fusée (63 slots)
 */
public class RocketMakerRecipeCategory implements IRecipeCategory<RocketMakerRecipe> {

    public static final RecipeType<RocketMakerRecipe> RECIPE_TYPE = RecipeType.create(
            EFC.MOD_ID,
            "rocket_making",
            RocketMakerRecipe.class
    );

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EFC.MOD_ID,
            "textures/gui/rocket_maker.png"
    );

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    // Positions des slots dans le background JEI
    // Background JEI = zone (4,4) à (396,175) de la texture 400x270
    // Slot 0 (top-left): position (179,9) dans le background JEI
    // Output slot: position (321,87) dans le background JEI
    private static final int[][] SLOT_POSITIONS = {
        // Ligne 1: 2 slots, Y=9
        {179, 9}, {197, 9},
        // Ligne 2: 4 slots, Y=27
        {161, 27}, {179, 27}, {197, 27}, {215, 27},
        // Ligne 3: 7 slots, Y=45
        {125, 45}, {143, 45}, {161, 45}, {179, 45}, {197, 45}, {215, 45}, {233, 45},
        // Ligne 4: 8 slots, Y=63
        {125, 63}, {143, 63}, {161, 63}, {179, 63}, {197, 63}, {215, 63}, {233, 63}, {251, 63},
        // Ligne 5: 7 slots, Y=81
        {125, 81}, {143, 81}, {161, 81}, {179, 81}, {197, 81}, {215, 81}, {233, 81},
        // Ligne 6: 7 slots, Y=99
        {125, 99}, {143, 99}, {161, 99}, {179, 99}, {197, 99}, {215, 99}, {233, 99},
        // Ligne 7: 8 slots, Y=117
        {125, 117}, {143, 117}, {161, 117}, {179, 117}, {197, 117}, {215, 117}, {233, 117}, {251, 117},
        // Ligne 8: 9 slots, Y=135
        {107, 135}, {125, 135}, {143, 135}, {161, 135}, {179, 135}, {197, 135}, {215, 135}, {233, 135}, {251, 135},
        // Ligne 9: 12 slots, Y=153
        {89, 153}, {107, 153}, {125, 153}, {143, 153}, {161, 153}, {179, 153}, {197, 153}, {215, 153}, {233, 153}, {251, 153}, {269, 153}, {287, 153}
    };

    public RocketMakerRecipeCategory(IGuiHelper guiHelper) {
        // Background: utilise la vraie texture du Rocket Maker GUI
        // Zone: 392x171px à partir de la position (4,4) dans la texture de 400x270px
        this.background = guiHelper.drawableBuilder(TEXTURE, 4, 4, 392, 171)
                .setTextureSize(400, 270)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(EFBlocks.ROCKET_MAKER.get()));
        this.title = Component.translatable("gui.erinium_faction.jei.rocket_making");
    }

    @Override
    @NotNull
    public RecipeType<RocketMakerRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    @NotNull
    public Component getTitle() {
        return title;
    }

    @Override
    @NotNull
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull RocketMakerRecipe recipe, @NotNull IFocusGroup focuses) {
        Map<Integer, Ingredient> pattern = recipe.getPattern();

        // Ajouter les slots d'entrée selon le pattern
        for (int slotIndex = 0; slotIndex < 63 && slotIndex < SLOT_POSITIONS.length; slotIndex++) {
            if (pattern.containsKey(slotIndex)) {
                int[] pos = SLOT_POSITIONS[slotIndex];
                builder.addSlot(RecipeIngredientRole.INPUT, pos[0], pos[1])
                        .addIngredients(pattern.get(slotIndex));
            }
        }

        // Slot de sortie (position du slot jaune dans la texture)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 321, 87)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(@NotNull RocketMakerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        // Dessiner le background
        background.draw(graphics, 0, 0);
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }
}
