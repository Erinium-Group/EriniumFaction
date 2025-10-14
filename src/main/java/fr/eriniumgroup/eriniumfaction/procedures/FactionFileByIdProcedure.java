package fr.eriniumgroup.eriniumfaction.procedures;

import net.neoforged.fml.loading.FMLPaths;

import java.io.File;

public class FactionFileByIdProcedure {
	public static File execute(String name) {
		if (name == null)
			return new File("");
		String filename = "";
		File file = new File("");
		filename = name + ".json";
		file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "faction/factions"), File.separator + filename);
		return file;
	}
}