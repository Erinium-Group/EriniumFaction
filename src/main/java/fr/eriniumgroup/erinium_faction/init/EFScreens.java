package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionMenuScreen;
import fr.eriniumgroup.erinium_faction.gui.screens.TitaniumCompressorScreen;
import fr.eriniumgroup.erinium_faction.gui.screens.EriniumChestScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = EFC.MODID, value = Dist.CLIENT)
public class EFScreens {
    @SubscribeEvent
    public static void clientLoad(RegisterMenuScreensEvent event) {
        event.register(EFMenus.FACTION_MENU.get(), FactionMenuScreen::new);
        event.register(EFMenus.TITANIUM_COMPRESSOR_MENU.get(), TitaniumCompressorScreen::new);
        event.register(EFMenus.ERINIUM_CHEST.get(), EriniumChestScreen::new);
    }

    public interface ScreenAccessor {
        void updateMenuState(int elementType, String name, Object elementState);
    }
}
