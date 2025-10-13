package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class FilePathClientAndCommonProcedure {
	public static File execute(boolean client, String extrapath, String name, String root) {
		if (extrapath == null || name == null || root == null)
			return new File("");
		String filename = "";
		File file = new File("");
		if (client) {
			filename = name + "-client" + ".json";
		} else {
			filename = name + "-common" + ".json";
		}
		file = new File((FMLPaths.GAMEDIR.get().toString() + "/config/" + root + "/" + extrapath), File.separator + filename);
		return file;
	}
}