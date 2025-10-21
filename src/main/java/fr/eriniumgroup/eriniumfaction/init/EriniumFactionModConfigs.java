package fr.eriniumgroup.eriniumfaction.init;

import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.SubscribeEvent;

import fr.eriniumgroup.eriniumfaction.configuration.FactionConfigServerConfiguration;
import fr.eriniumgroup.eriniumfaction.EriniumFactionMod;

@EventBusSubscriber(modid = EriniumFactionMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EriniumFactionModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModList.get().getModContainerById("erinium_faction").get().registerConfig(ModConfig.Type.SERVER, FactionConfigServerConfiguration.SPEC, "faction-config.toml");
		});
	}
}