package fr.eriniumgroup.eriniumfaction.procedures;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

@EventBusSubscriber
public class PlayerJoinProcedure {
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		File file = new File("");
		com.google.gson.JsonObject JsonObject = new com.google.gson.JsonObject();
		file = UuidFileProcedure.execute(entity.getStringUUID());
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
				if (JsonObject.size() == 0) {
					JsonObject.addProperty("displayname", (entity.getDisplayName().getString()));
					JsonObject.addProperty("faction", "wilderness");
					{
						EriniumFactionModVariables.PlayerVariables _vars = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES);
						_vars.faction = "wilderness";
						_vars.syncPlayerVariables(entity);
					}
				}
				if (!(JsonObject.get("displayname").getAsString()).equals(entity.getDisplayName().getString())) {
					JsonObject.addProperty("displayname", (entity.getDisplayName().getString()));
				}
				{
					com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
					try {
						FileWriter fileWriter = new FileWriter(file);
						fileWriter.write(mainGSONBuilderVariable.toJson(JsonObject));
						fileWriter.close();
					} catch (IOException exception) {
						exception.printStackTrace();
					}
				}
				{
					EriniumFactionModVariables.PlayerVariables _vars = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES);
					_vars.faction = JsonObject.get("faction").getAsString();
					_vars.syncPlayerVariables(entity);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}