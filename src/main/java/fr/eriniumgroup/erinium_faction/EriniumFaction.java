package fr.eriniumgroup.erinium_faction;

import com.mojang.logging.LogUtils;
import fr.eriniumgroup.erinium_faction.commands.FactionCommand;
import fr.eriniumgroup.erinium_faction.commands.RankCommand;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.common.network.PacketHandler;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.logger.EFCC;
import fr.eriniumgroup.erinium_faction.protection.ClaimProtection;
import fr.eriniumgroup.erinium_faction.protection.PvpProtection;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(EriniumFaction.MODID)
public class EriniumFaction {
    public static final String MODID = "erinium_faction";

    public EriniumFaction(IEventBus modEventBus, ModContainer modContainer) {
        EFCC.install();

        EFC.log.info("Initializing Erinium Faction - PvP Faction Mod");

        // Configuration
        modContainer.registerConfig(ModConfig.Type.SERVER, EFConfig.SPEC);

        // Setup phase
        modEventBus.addListener(this::commonSetup);

        // Register event listeners
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // Protection systems
        ClaimProtection.register();
        PvpProtection.register();

        EFC.log.info("§b" + EFC.MOD_NAME + " §7Mod §ainitialized §7!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        EFC.log.info("§6Common §aSetup");
        PacketHandler.register();
    }

    private void onServerStarting(ServerStartingEvent event) {
        EFC.log.info("§aLoading §dfaction §7data...");
        FactionManager.load(event.getServer());
    }

    private void onServerStopping(ServerStoppingEvent event) {
        EFC.log.info("§2Saving §dfaction §7data...");
        FactionManager.save(event.getServer());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        EFC.log.info("§aRegistering §dfaction §7commands...");
        FactionCommand.register(event.getDispatcher());
        RankCommand.register(event.getDispatcher());
    }
}

