package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraft.world.entity.Entity;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

public class GetPlayerFactionProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction;
	}
}