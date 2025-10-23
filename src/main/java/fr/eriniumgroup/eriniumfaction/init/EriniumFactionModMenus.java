/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package fr.eriniumgroup.eriniumfaction.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;

import java.util.Map;

import fr.eriniumgroup.eriniumfaction.world.inventory.GuiForConstructMenu;
import fr.eriniumgroup.eriniumfaction.world.inventory.FactionMenuSettingsMenu;
import fr.eriniumgroup.eriniumfaction.world.inventory.FactionMenuMenu;
import fr.eriniumgroup.eriniumfaction.network.MenuStateUpdateMessage;
import fr.eriniumgroup.eriniumfaction.EriniumFactionMod;

public class EriniumFactionModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, EriniumFactionMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<GuiForConstructMenu>> GUI_FOR_CONSTRUCT = REGISTRY.register("gui_for_construct", () -> IMenuTypeExtension.create(GuiForConstructMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<FactionMenuMenu>> FACTION_MENU = REGISTRY.register("faction_menu", () -> IMenuTypeExtension.create(FactionMenuMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<FactionMenuSettingsMenu>> FACTION_MENU_SETTINGS = REGISTRY.register("faction_menu_settings", () -> IMenuTypeExtension.create(FactionMenuSettingsMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level().isClientSide) {
				if (Minecraft.getInstance().screen instanceof EriniumFactionModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				PacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
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