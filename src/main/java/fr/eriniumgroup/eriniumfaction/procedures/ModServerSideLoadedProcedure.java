package fr.eriniumgroup.eriniumfaction.procedures;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.item.Items;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.FileWriter;
import java.io.File;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

@EventBusSubscriber
public class ModServerSideLoadedProcedure {
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
		com.google.gson.JsonObject json = new com.google.gson.JsonObject();
		file = FilePathServerProcedure.execute("faction", "faction-settings", "erinium_faction");
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			json.addProperty("faction.cost.item", (BuiltInRegistries.ITEM.getKey(Items.DIAMOND).toString()));
			json.addProperty("faction.cost.number", 100);
			json.addProperty("faction.faction.powerperplayer", 10);
			json.addProperty("faction.faction.defaultmaxplayer", 10);
			json.addProperty("faction.faction.defaultmaxwarp", 1);
			{
				com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
				try {
					FileWriter fileWriter = new FileWriter(file);
					fileWriter.write(mainGSONBuilderVariable.toJson(json));
					fileWriter.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}
		if ((entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction).isEmpty()) {
			{
				EriniumFactionModVariables.PlayerVariables _vars = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES);
				_vars.faction = "wilderness";
				_vars.syncPlayerVariables(entity);
			}
		}
	}
}