package fr.eriniumgroup.eriniumfaction.procedures;

import net.neoforged.fml.loading.FMLPaths;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;

import java.io.File;

public class CurrentChunkFileProcedure {
	public static File execute(LevelAccessor world, double x, double z) {
		File file = new File("");
		String filename = "";
		String StringReturn = "";
		com.google.gson.JsonObject json = new com.google.gson.JsonObject();
		filename = new Object() {
			private String getRegion(int chunkX, int chunkZ) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
				return new String("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ());
			}
		}.getRegion((int) x, (int) z) + "/" + new Object() {
			private String getChunk(int chunkX, int chunkZ) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
				return new String(chunkpos.getRegionLocalX() + "-" + chunkpos.getRegionLocalZ());
			}
		}.getChunk((int) x, (int) z) + ".json";
		file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "faction/chunks/"), File.separator + filename);
		return file;
	}
}