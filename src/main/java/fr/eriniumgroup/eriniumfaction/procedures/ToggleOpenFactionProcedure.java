package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraft.world.entity.Entity;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

public class ToggleOpenFactionProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		File file = new File("");
		com.google.gson.JsonObject JsonObject = new com.google.gson.JsonObject();
		String returner = "";
		file = FactionFileByIdProcedure.execute(entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction);
		{
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
				StringBuilder jsonstringbuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					jsonstringbuilder.append(line);
				}
				bufferedReader.close();
				JsonObject = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
				JsonObject.addProperty("openFaction", (!JsonObject.get("openFaction").getAsBoolean()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}