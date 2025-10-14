package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraft.world.level.LevelAccessor;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

public class AdminProtectionBlockPosProcedure {
	public static boolean execute(LevelAccessor world, double x, double z) {
		String tempId = "";
		File file = new File("");
		com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
		boolean impossibleToInterract = false;
		tempId = CurrentChunkFactionIdProcedure.execute(world, x, z);
		if ((tempId).equals("safezone") || (tempId).equals("warzone")) {
			return false;
		} else if ((tempId).equals("wilderness")) {
			return true;
		} else {
			file = FactionFileByIdProcedure.execute(tempId);
			{
				try {
					BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
					StringBuilder jsonstringbuilder = new StringBuilder();
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						jsonstringbuilder.append(line);
					}
					bufferedReader.close();
					jsonObject = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
					if (jsonObject.get("isAdminFaction").getAsBoolean() || jsonObject.get("isWarzone").getAsBoolean() || jsonObject.get("isSafezone").getAsBoolean()) {
						impossibleToInterract = true;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (impossibleToInterract) {
				return false;
			}
		}
		return true;
	}
}