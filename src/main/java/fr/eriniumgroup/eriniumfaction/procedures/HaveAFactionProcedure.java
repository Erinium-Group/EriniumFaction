package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraft.world.entity.Entity;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

public class HaveAFactionProcedure {
	public static boolean execute(Entity target) {
		if (target == null)
			return false;
		if (!(target.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction).equals("wilderness")) {
			return true;
		}
		return false;
	}
}