package fr.eriniumgroup.eriniumfaction.procedures;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

import fr.eriniumgroup.eriniumfaction.configuration.FactionConfigServerConfiguration;

public class GetFactionMaxPowerProcedure {
	public static double execute(String name) {
		if (name == null)
			return 0;
		String filename = "";
		File file = new File("");
		double number = 0;
		com.google.gson.JsonObject json = new com.google.gson.JsonObject();
		filename = name + ".json";
		file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "faction/factions"), File.separator + filename);
		{
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
				StringBuilder jsonstringbuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					jsonstringbuilder.append(line);
				}
				bufferedReader.close();
				json = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
				if ((json.get("memberList").getAsString()).isEmpty()) {
					number = 1;
				} else {
					number = new Object() {
						private int returnSize(String text, String separator) {
							String[] resultTxt = (text).split(separator);
							return resultTxt.length;
						}
					}.returnSize(json.get("memberList").getAsString(), ",") + 1;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return number * (double) FactionConfigServerConfiguration.POWERPERPLAYER.get();
	}
}