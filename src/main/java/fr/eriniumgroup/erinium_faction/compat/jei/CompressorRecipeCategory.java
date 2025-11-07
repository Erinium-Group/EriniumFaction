package fr.eriniumgroup.erinium_faction.compat.jei;

import fr.eriniumgroup.erinium_faction.common.recipe.CompressorRecipe;
import fr.eriniumgroup.erinium_faction.core.EFC;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Catégorie de recette JEI pour le Titanium Compressor
 */
public class CompressorRecipeCategory implements IRecipeCategory<CompressorRecipe> {

    public static final RecipeType<CompressorRecipe> RECIPE_TYPE =
        RecipeType.create(EFC.MOD_ID, "compressing", CompressorRecipe.class);

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(EFC.MOD_ID, "textures/gui/jei/compressor.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable energyBar;
    private final Component title;

    public CompressorRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 140, 60);
        this.icon = guiHelper.createDrawableItemStack(
            new ItemStack(fr.eriniumgroup.erinium_faction.init.EFBlocks.TITANIUM_COMPRESSOR.get())
        );
        this.arrow = guiHelper.drawableBuilder(TEXTURE, 140, 0, 24, 17)
            .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
        this.energyBar = guiHelper.createDrawable(TEXTURE, 164, 0, 14, 42);
        this.title = Component.translatable("gui.erinium_faction.jei.compressing");
    }

    @Override
    @NotNull
    public RecipeType<CompressorRecipe> getRecipeType() {
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
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CompressorRecipe recipe, @NotNull IFocusGroup focuses) {
        // Slot d'entrée (input)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 22)
            .addIngredients(recipe.getInput());

        // Slot de sortie (output)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 22)
            .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(@NotNull CompressorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        // Dessiner le background
        background.draw(graphics, 0, 0);

        // Dessiner la flèche de progression
        arrow.draw(graphics, 58, 22);

        // Dessiner la barre d'énergie
        energyBar.draw(graphics, 120, 8);

        Font font = Minecraft.getInstance().font;

        // Afficher le temps de traitement
        String timeText = String.format("%.1fs", recipe.getProcessingTime() / 20.0);
        graphics.drawString(font, timeText, 58, 45, 0x808080, false);

        // Afficher le coût en énergie
        String energyText = recipe.getEnergyCost() + " FE";
        graphics.drawString(font, energyText, 8, 8, 0xFFD700, false);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull CompressorRecipe recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // Tooltip pour la barre d'énergie (x: 120-134, y: 8-50)
        if (mouseX >= 120 && mouseX <= 134 && mouseY >= 8 && mouseY <= 50) {
            tooltip.add(Component.translatable("gui.erinium_faction.jei.energy_required", recipe.getEnergyCost()));
        }

        // Tooltip pour la flèche (x: 58-82, y: 22-39)
        if (mouseX >= 58 && mouseX <= 82 && mouseY >= 22 && mouseY <= 39) {
            tooltip.add(Component.translatable("gui.erinium_faction.jei.processing_time",
                String.format("%.1f", recipe.getProcessingTime() / 20.0)));
        }
    }
}


