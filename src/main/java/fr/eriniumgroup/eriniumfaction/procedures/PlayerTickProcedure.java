package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

import com.google.gson.Gson;

@Mod.EventBusSubscriber
public class PlayerTickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event, event.player.level, event.player.getX(), event.player.getZ(), event.player);
		}
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
		if (!((entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EriniumFactionModVariables.PlayerVariables())).lastChunk).equals(new Object() {
			private String getChunk(int chunkX, int chunkZ) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
				return new String(chunkpos.getRegionLocalX() + "-" + chunkpos.getRegionLocalZ());
			}
		}.getChunk((int) x, (int) z)) || !((entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EriniumFactionModVariables.PlayerVariables())).lastRegion).equals(new Object() {
			private String getRegion(int chunkX, int chunkZ) {
				ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
				return new String("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ());
			}
		}.getRegion((int) x, (int) z))) {
			{
				String _setval = new Object() {
					private String getChunk(int chunkX, int chunkZ) {
						ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
						return new String(chunkpos.getRegionLocalX() + "-" + chunkpos.getRegionLocalZ());
					}
				}.getChunk((int) x, (int) z);
				entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.lastChunk = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				String _setval = new Object() {
					private String getRegion(int chunkX, int chunkZ) {
						ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
						return new String("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ());
					}
				}.getRegion((int) x, (int) z);
				entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.lastRegion = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if (!(CurrentChunkFactionIdProcedure.execute(world, x, z)).equals((entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EriniumFactionModVariables.PlayerVariables())).factioninchunk)) {
				{
					String _setval = CurrentChunkFactionIdProcedure.execute(world, x, z);
					entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.factioninchunk = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (((entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EriniumFactionModVariables.PlayerVariables())).factioninchunk).equals("wilderness")) {
					if (entity instanceof Player || entity instanceof ServerPlayer) {
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " subtitle \"" + (new TranslatableComponent("wilderness.desc").getString()) + "\""));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " times " + 1 + 1 + 1));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " title \"" + "\u00A7aWilderness" + "\""));
					}
				} else if (((entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EriniumFactionModVariables.PlayerVariables())).factioninchunk).equals("safezone")) {
					if (entity instanceof Player || entity instanceof ServerPlayer) {
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " subtitle \"" + (new TranslatableComponent("safezone.desc").getString()) + "\""));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " times " + 1 + 1 + 1));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " title \"" + "\u00A72Safezone" + "\""));
					}
				} else if (((entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EriniumFactionModVariables.PlayerVariables())).factioninchunk).equals("warzone")) {
					if (entity instanceof Player || entity instanceof ServerPlayer) {
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " subtitle \"" + (new TranslatableComponent("warzone.desc").getString()) + "\""));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " times " + 1 + 1 + 1));
						if (world instanceof ServerLevel _level)
							_level.getServer().getCommands().performCommand(
									new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
											.withSuppressedOutput(),
									("title " + entity.getDisplayName().getString() + " title \"" + "\u00A74Warzone" + "\""));
					}
				} else {
					file = FactionFileByIdProcedure.execute((entity.getCapability(EriniumFactionModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EriniumFactionModVariables.PlayerVariables())).factioninchunk);
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
							if (entity instanceof Player || entity instanceof ServerPlayer) {
								if (world instanceof ServerLevel _level)
									_level.getServer().getCommands().performCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
													.withSuppressedOutput(),
											("title " + entity.getDisplayName().getString() + " subtitle \"" + json.get("description").getAsString() + "\""));
								if (world instanceof ServerLevel _level)
									_level.getServer().getCommands().performCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
													.withSuppressedOutput(),
											("title " + entity.getDisplayName().getString() + " times " + 1 + 1 + 1));
								if (world instanceof ServerLevel _level)
									_level.getServer().getCommands().performCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", net.minecraft.network.chat.TextComponent.EMPTY, _level.getServer(), null)
													.withSuppressedOutput(),
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