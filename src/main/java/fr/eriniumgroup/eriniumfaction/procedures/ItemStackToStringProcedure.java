package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;

import fr.eriniumgroup.eriniumfaction.ItemStackSerializer;

public class ItemStackToStringProcedure {
	public static String execute(LevelAccessor world, ItemStack itemstack, boolean base64) {
		ItemStack item = ItemStack.EMPTY;
		item = itemstack.copy();
		if (base64) {
			return ItemStackSerializer.itemStackToBase64(item, (Level) world);
		}
		return ItemStackSerializer.itemStackToString(item, (Level) world);
	}
}