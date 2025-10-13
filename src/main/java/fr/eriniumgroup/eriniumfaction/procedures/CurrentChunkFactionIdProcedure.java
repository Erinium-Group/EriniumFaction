package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraftforge.fml.loading.FMLPaths;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

import com.google.gson.Gson;

public class CurrentChunkFactionIdProcedure {
	public static String execute(LevelAccessor world, double x, double z) {
		File file = new File("");
		String filename = "";
		String StringReturn = "";
		com.google.gson.JsonObject json = new com.google.gson.JsonObject();
		filename = new Object() {
			private String getChunk(int chunkX, int chunkZ) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
				return new String(chunkpos.getRegionLocalX() + "-" + chunkpos.getRegionLocalZ());
			}
		}.getChunk((int) x, (int) z) + ".json";
		file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "faction/chunks/" + new Object() {
			private String getRegion(int chunkX, int chunkZ) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
				return new String("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ());
			}
		}.getRegion((int) x, (int) z)), File.separator + filename);
		if (file.exists()) {
			{
				try {
					BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
					StringBuilder jsonstringbuilder = new StringBuilder();
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						jsonstringbuilder.append(line);
					}
					bufferedReader.close();
					json = new Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
					StringReturn = json.get("id").getAsString();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return StringReturn;
		}
		return "wilderness";
	}
}