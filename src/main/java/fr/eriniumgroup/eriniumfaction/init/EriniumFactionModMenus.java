/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package fr.eriniumgroup.eriniumfaction.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.Minecraft;

import java.util.Map;

import fr.eriniumgroup.eriniumfaction.world.inventory.FactionMenuMenu;
import fr.eriniumgroup.eriniumfaction.network.MenuStateUpdateMessage;
import fr.eriniumgroup.eriniumfaction.EriniumFactionMod;

public class EriniumFactionModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.CONTAINERS, EriniumFactionMod.MODID);
	public static final RegistryObject<MenuType<FactionMenuMenu>> FACTION_MENU = REGISTRY.register("faction_menu", () -> IForgeMenuType.create(FactionMenuMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				EriniumFactionMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level.isClientSide) {
				if (Minecraft.getInstance().screen instanceof EriniumFactionModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				EriniumFactionMod.PACKET_HANDLER.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
			}
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}