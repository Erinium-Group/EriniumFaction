/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package fr.eriniumgroup.eriniumfaction.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import fr.eriniumgroup.eriniumfaction.client.gui.GuiForConstructScreen;
import fr.eriniumgroup.eriniumfaction.client.gui.FactionMenuSettingsScreen;
import fr.eriniumgroup.eriniumfaction.client.gui.FactionMenuScreen;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EriniumFactionModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(EriniumFactionModMenus.GUI_FOR_CONSTRUCT.get(), GuiForConstructScreen::new);
		event.register(EriniumFactionModMenus.FACTION_MENU.get(), FactionMenuScreen::new);
		event.register(EriniumFactionModMenus.FACTION_MENU_SETTINGS.get(), FactionMenuSettingsScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}