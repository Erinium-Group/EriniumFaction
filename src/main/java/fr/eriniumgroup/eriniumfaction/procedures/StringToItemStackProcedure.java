package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;

import fr.eriniumgroup.eriniumfaction.ItemStackSerializer;

public class StringToItemStackProcedure {
	public static ItemStack execute(LevelAccessor world, ItemStack item, boolean base64, String itemstring) {
		if (itemstring == null)
			return ItemStack.EMPTY;
		String string = "";
		ItemStack itemstack = ItemStack.EMPTY;
		itemstack = item.copy();
		string = itemstring;
		if (base64) {
			ItemStackSerializer.base64ToItemStack(itemstack, string, (Level) world);
			return itemstack;
		}
		ItemStackSerializer.stringToItemStack(itemstack, string, (Level) world);
		return itemstack;
	}
}