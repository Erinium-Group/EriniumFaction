package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class FactionsRootFolderProcedure {
	public static String execute() {
		String filename = "";
		File file = new File("");
		return FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "faction/factions";
	}
}