package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class FilePathServerProcedure {
	public static File execute(String extrapath, String name, String root) {
		if (extrapath == null || name == null || root == null)
			return new File("");
		String filename = "";
		File file = new File("");
		filename = name + "-server" + ".json";
		file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + root + "/" + extrapath), File.separator + filename);
		return file;
	}
}