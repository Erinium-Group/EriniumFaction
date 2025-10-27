package fr.eriniumgroup.erinium_faction;

import fr.eriniumgroup.erinium_faction.commands.FactionCommand;
import fr.eriniumgroup.erinium_faction.commands.RankCommand;
import fr.eriniumgroup.erinium_faction.commands.EconomyCommand;
import fr.eriniumgroup.erinium_faction.common.config.EFClientConfig;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.common.network.PacketHandler;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.logger.EFCC;
import fr.eriniumgroup.erinium_faction.init.EFCreativeTabs;
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
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import fr.eriniumgroup.erinium_faction.init.EFItems;
import fr.eriniumgroup.erinium_faction.init.EFCreativeTabs;
import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.init.EFArgumentTypes;
import fr.eriniumgroup.erinium_faction.commands.arguments.FactionArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import fr.eriniumgroup.erinium_faction.core.power.PowerManager;
import fr.eriniumgroup.erinium_faction.core.permissions.EFPerms;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelAttachments;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelConfig;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelCommand;

@Mod(EriniumFaction.MODID)
public class EriniumFaction {
    public static final String MODID = "erinium_faction";

    public EriniumFaction(IEventBus modEventBus, ModContainer modContainer) {
        EFCC.install();

        EFC.log.info("Initializing Erinium Faction - PvP Faction Mod");

        // Configuration
        modContainer.registerConfig(ModConfig.Type.SERVER, EFConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, EFClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, PlayerLevelConfig.SPEC, "erinium_faction-player_level.toml");

        // Register DeferredRegisters (must be before client screen registrations)
        EFMenus.REGISTER.register(modEventBus);
        EFItems.REGISTER.register(modEventBus);
        EFCreativeTabs.REGISTER.register(modEventBus);
        EFVariables.ATTACHMENT_TYPES.register(modEventBus);
        EFArgumentTypes.REGISTER.register(modEventBus);
        PowerManager.ATTACHMENTS.register(modEventBus);
        // Enregistrer l'économie (players.dat)
        fr.eriniumgroup.erinium_faction.integration.economy.EconomyIntegration.ATTACHMENTS.register(modEventBus);
        // Système de niveau des joueurs
        PlayerLevelAttachments.ATTACHMENTS.register(modEventBus);

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
        // Link custom argument class to its info so server can sync command tree to clients
        event.enqueueWork(() -> ArgumentTypeInfos.registerByClass(FactionArgumentType.class, (ArgumentTypeInfo<FactionArgumentType, ?>) EFArgumentTypes.FACTION.get()));
    }

    private void onServerStarting(ServerStartingEvent event) {
        EFC.log.info("§aLoading §dfaction §7data...");
        FactionManager.load(event.getServer());
        // Ranks system
        EFRManager.get().load();
        // Initialiser les permissions par défaut (SavedData .dat) et appliquer si nécessaire
        fr.eriniumgroup.erinium_faction.core.faction.RankDefaultsSavedData.bootstrapAndApply(event.getServer());
    }

    private void onServerStopping(ServerStoppingEvent event) {
        EFC.log.info("§2Saving §dfaction §7data...");
        FactionManager.save(event.getServer());
        EFRManager.get().save();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        EFC.log.info("§aRegistering §dfaction §7commands...");
        FactionCommand.register(event.getDispatcher());
        RankCommand.register(event.getDispatcher());
        // Commandes économie
        EconomyCommand.register(event.getDispatcher());
        // Commande ef (perm per-player)
        fr.eriniumgroup.erinium_faction.commands.EFCommand.register(event.getDispatcher());
        // Commande système de niveau
        PlayerLevelCommand.register(event.getDispatcher());
        // Appliquer la garde globale des permissions sur toutes les commandes
        EFPerms.guardDispatcher(event.getDispatcher());
    }
}
