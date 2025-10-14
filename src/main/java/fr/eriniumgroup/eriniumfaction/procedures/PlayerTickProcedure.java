package fr.eriniumgroup.eriniumfaction.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

@EventBusSubscriber
public class PlayerTickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double z, Entity entity) {
		execute(null, world, x, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double z, Entity entity) {
		if (entity == null)
			return;
		File file = new File("");
		com.google.gson.JsonObject json = new com.google.gson.JsonObject();
		boolean customfac = false;
		if (!(entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).lastChunk).equals(new Object() {
			private String getChunk(int chunkX, int chunkZ) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
				return new String(chunkpos.getRegionLocalX() + "-" + chunkpos.getRegionLocalZ());
			}
		}.getChunk((int) x, (int) z)) || !(entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).lastRegion).equals(new Object() {
			private String getRegion(int chunkX, int chunkZ) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
				return new String("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ());
			}
		}.getRegion((int) x, (int) z))) {
			{
				EriniumFactionModVariables.PlayerVariables _vars = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES);
				_vars.lastChunk = new Object() {
					private String getChunk(int chunkX, int chunkZ) {
						ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
						return new String(chunkpos.getRegionLocalX() + "-" + chunkpos.getRegionLocalZ());
					}
				}.getChunk((int) x, (int) z);
				_vars.syncPlayerVariables(entity);
			}
			{
				EriniumFactionModVariables.PlayerVariables _vars = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES);
				_vars.lastRegion = new Object() {
					private String getRegion(int chunkX, int chunkZ) {
						ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
						return new String("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ());
					}
				}.getRegion((int) x, (int) z);
				_vars.syncPlayerVariables(entity);
			}
			if (!(CurrentChunkFactionIdProcedure.execute(world, x, z)).equals(entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).factioninchunk)) {
				{
					EriniumFactionModVariables.PlayerVariables _vars = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES);
					_vars.factioninchunk = CurrentChunkFactionIdProcedure.execute(world, x, z);
					_vars.syncPlayerVariables(entity);
				}
				if ((entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).factioninchunk).equals("wilderness")) {
					if (entity instanceof Player || entity instanceof ServerPlayer) {
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " subtitle \"" + (Component.translatable("wilderness.desc").getString()) + "\""));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " times " + 1 + 1 + 1));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " title \"" + "\u00A7aWilderness" + "\""));
					}
				} else if ((entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).factioninchunk).equals("safezone")) {
					if (entity instanceof Player || entity instanceof ServerPlayer) {
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " subtitle \"" + (Component.translatable("safezone.desc").getString()) + "\""));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " times " + 1 + 1 + 1));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " title \"" + "\u00A72Safezone" + "\""));
					}
				} else if ((entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).factioninchunk).equals("warzone")) {
					if (entity instanceof Player || entity instanceof ServerPlayer) {
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " subtitle \"" + (Component.translatable("warzone.desc").getString()) + "\""));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " times " + 1 + 1 + 1));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performPrefixedCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " title \"" + "\u00A74Warzone" + "\""));
					}
				} else {
					file = FactionFileByIdProcedure.execute(entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).factioninchunk);
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
							if (entity instanceof Player || entity instanceof ServerPlayer) {
								if (world instanceof ServerLevel _level)
									_level.getServer().getCommands().performPrefixedCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
											("title " + entity.getDisplayName().getString() + " subtitle \"" + json.get("description").getAsString() + "\""));
								if (world instanceof ServerLevel _level)
									_level.getServer().getCommands().performPrefixedCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
											("title " + entity.getDisplayName().getString() + " times " + 1 + 1 + 1));
								if (world instanceof ServerLevel _level)
									_level.getServer().getCommands().performPrefixedCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
											("title " + entity.getDisplayName().getString() + " title \"" + ("\u00A7a" + json.get("displayname").getAsString()) + "\""));
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}