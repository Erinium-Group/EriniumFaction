package fr.eriniumgroup.eriniumfaction.procedures;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.atomic.AtomicReference;

import java.io.IOException;
import java.io.FileWriter;
import java.io.File;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;
import fr.eriniumgroup.eriniumfaction.configuration.FactionConfigServerConfiguration;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;

public class FCreateProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;
		File file = new File("");
		com.google.gson.JsonObject JsonObject = new com.google.gson.JsonObject();
		file = FactionFileByIdProcedure.execute(StringArgumentType.getString(arguments, "id"));
		if ((entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction).equals("wilderness")) {
			if (!file.exists()) {
				if (new Object() {
					private int returnItemNumber(ItemStack item, Entity entity) {
						ItemStack tempItem = item;
						double count = 0;
						{
							AtomicReference<IItemHandler> _iitemhandlerref = new AtomicReference<>();
							IItemHandler capability = entity.getCapability(Capabilities.ItemHandler.ENTITY);
							if (capability != null) {
								_iitemhandlerref.set(capability);
							}
							if (_iitemhandlerref.get() != null) {
								for (int _idx = 0; _idx < _iitemhandlerref.get().getSlots(); _idx++) {
									ItemStack itemstackiterator = _iitemhandlerref.get().getStackInSlot(_idx).copy();
									if (itemstackiterator.getItem() == tempItem.getItem()) {
										count = count + (itemstackiterator).getCount();
									}
								}
							}
						}
						return (int) count;
					}
				}.returnItemNumber(BuiltInRegistries.ITEM.get(ResourceLocation.parse(((FactionConfigServerConfiguration.FACTIONCOSTITEM.get())).toLowerCase(java.util.Locale.ENGLISH))).getDefaultInstance(),
						entity) >= (double) FactionConfigServerConfiguration.FACTIONCOSTNUMBER.get()
						|| BuiltInRegistries.ITEM.get(ResourceLocation.parse(((FactionConfigServerConfiguration.FACTIONCOSTITEM.get())).toLowerCase(java.util.Locale.ENGLISH))) == Blocks.AIR.asItem()) {
					if (entity instanceof Player _player) {
						ItemStack _stktoremove = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(((FactionConfigServerConfiguration.FACTIONCOSTITEM.get())).toLowerCase(java.util.Locale.ENGLISH))));
						_player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), (int) (double) FactionConfigServerConfiguration.FACTIONCOSTNUMBER.get(), _player.inventoryMenu.getCraftSlots());
					}
					try {
						file.getParentFile().mkdirs();
						file.createNewFile();
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					JsonObject.addProperty("isAdminFaction", false);
					JsonObject.addProperty("openFaction", false);
					JsonObject.addProperty("isWarzone", false);
					JsonObject.addProperty("isSafezone", false);
					JsonObject.addProperty("displayname", (StringArgumentType.getString(arguments, "DisplayName")));
					JsonObject.addProperty("memberList", "");
					JsonObject.addProperty("owner", (entity.getStringUUID()));
					JsonObject.addProperty("invitedPlayers", "");
					JsonObject.addProperty("allies", "");
					JsonObject.addProperty("homeLocation", "");
					JsonObject.addProperty("warps", "");
					JsonObject.addProperty("description", "");
					JsonObject.addProperty("upgrades", "");
					JsonObject.addProperty("claimlist", "");
					JsonObject.addProperty("officer", "");
					JsonObject.addProperty("member", "");
					JsonObject.addProperty("recruit", "");
					JsonObject.addProperty("power", entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).power);
					JsonObject.addProperty("claim", 0);
					JsonObject.addProperty("maxClaims", ((double) FactionConfigServerConfiguration.POWERPERPLAYER.get()));
					JsonObject.addProperty("maxPlayer", ((double) FactionConfigServerConfiguration.DEFAULTMAXPLAYER.get()));
					JsonObject.addProperty("dateOfCreation", (System.currentTimeMillis() / 1000));
					JsonObject.addProperty("factionLevel", 0);
					JsonObject.addProperty("factionXp", 0);
					JsonObject.addProperty("bankBalance", 0);
					JsonObject.addProperty("maxWarps", ((double) FactionConfigServerConfiguration.DEFAULTMAXWARP.get()));
					{
						com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
						try {
							FileWriter fileWriter = new FileWriter(file);
							fileWriter.write(mainGSONBuilderVariable.toJson(JsonObject));
							fileWriter.close();
						} catch (IOException exception) {
							exception.printStackTrace();
						}
					}
					{
						EriniumFactionModVariables.PlayerVariables _vars = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES);
						_vars.faction = StringArgumentType.getString(arguments, "id");
						_vars.syncPlayerVariables(entity);
					}
					ChangePlayerFactionProcedure.execute(StringArgumentType.getString(arguments, "id"), entity.getStringUUID());
					if (entity instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(Component.literal((entity.getDisplayName().getString() + "" + Component.translatable("f.create.created").getString() + StringArgumentType.getString(arguments, "DisplayName"))), false);
				} else {
					if (entity instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(
								Component.literal((Component.translatable("f.create.needitem").getString() + "" + FactionConfigServerConfiguration.FACTIONCOSTITEM.get() + " x" + (double) FactionConfigServerConfiguration.FACTIONCOSTNUMBER.get())),
								false);
				}
			} else {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal((Component.translatable("f.create.alreadyexist").getString())), false);
			}
		} else {
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal((Component.translatable("f.create.alreayinfaction").getString())), false);
		}
	}
}