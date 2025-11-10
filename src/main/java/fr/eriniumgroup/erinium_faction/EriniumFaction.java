package fr.eriniumgroup.erinium_faction;

import fr.eriniumgroup.erinium_faction.commands.*;
import fr.eriniumgroup.erinium_faction.commands.arguments.FactionArgumentType;
import fr.eriniumgroup.erinium_faction.features.combatlog.CombatLogCommand;
import fr.eriniumgroup.erinium_faction.common.config.EFClientConfig;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.common.config.JobsConfigManager;
import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.common.network.PacketHandler;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.logger.EFCC;
import fr.eriniumgroup.erinium_faction.core.permissions.EFPerms;
import fr.eriniumgroup.erinium_faction.core.power.PowerManager;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import fr.eriniumgroup.erinium_faction.events.*;
import fr.eriniumgroup.erinium_faction.features.antixray.AntiXrayManager;
import fr.eriniumgroup.erinium_faction.features.audit.AuditRotator;
import fr.eriniumgroup.erinium_faction.features.combatlog.CombatLogConfig;
import fr.eriniumgroup.erinium_faction.features.combatlog.CombatLogEntities;
import fr.eriniumgroup.erinium_faction.features.combatlog.CombatLogEventHandler;
import fr.eriniumgroup.erinium_faction.features.economy.EconomyIntegration;
import fr.eriniumgroup.erinium_faction.features.kits.KitCommand;
import fr.eriniumgroup.erinium_faction.features.kits.KitConfig;
import fr.eriniumgroup.erinium_faction.features.kits.KitManager;
import fr.eriniumgroup.erinium_faction.features.homes.HomeTeleportService;
import fr.eriniumgroup.erinium_faction.features.jobs.data.JobsDataAttachment;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelAttachments;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelConfig;
import fr.eriniumgroup.erinium_faction.features.topluck.TopLuckAttachments;
import fr.eriniumgroup.erinium_faction.init.*;
import fr.eriniumgroup.erinium_faction.protection.ClaimProtection;
import fr.eriniumgroup.erinium_faction.protection.PvpProtection;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(EriniumFaction.MODID)
public class EriniumFaction {
    public static final String MODID = "erinium_faction";

    public EriniumFaction(IEventBus modEventBus, ModContainer modContainer) {
        EFCC.install();

        EFC.log.info("§aInitializing §9" + EFC.MOD_NAME + " §7- §ePvP Faction Mod");
        EFC.log.info("§eVersion: §a" + EFC.MOD_VERSION);
        EFC.log.info("§eAuthors: §a" + EFC.AUTHORS);
        EFC.log.info("§eNeoForge Version: §a" + EFC.NEOFORGE_VERSION);
        EFC.log.info("§eMod ID: §a" + MODID);

        // Initialiser l'Anti-Xray
        AntiXrayManager.getInstance().init();

        // Configuration
        modContainer.registerConfig(ModConfig.Type.SERVER, EFConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, EFClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, PlayerLevelConfig.SPEC, "erinium_faction-player_level.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, CombatLogConfig.SPEC, "erinium_faction-combatlog.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, KitConfig.SPEC, "erinium_faction-kits.toml");

        // Register DeferredRegisters (must be before client screen registrations)
        EFMenus.REGISTER.register(modEventBus);
        // Register blocks
        EFBlocks.REGISTER.register(modEventBus);
        // Register block entities
        EFBlockEntities.REGISTER.register(modEventBus);
        // Register items
        EFItems.REGISTER.register(modEventBus);
        // Register block items from item register
        EFBlocks.registerBlockItems(EFItems.REGISTER);
        // Register creative tabs
        EFCreativeTabs.REGISTER.register(modEventBus);
        // Register recipes
        EFRecipes.RECIPE_TYPES.register(modEventBus);
        EFRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        // Register network variables
        EFVariables.ATTACHMENT_TYPES.register(modEventBus);
        // Register argument types
        EFArgumentTypes.REGISTER.register(modEventBus);
        // Register player power system
        PowerManager.ATTACHMENTS.register(modEventBus);
        // Register TopLuck system
        TopLuckAttachments.ATTACHMENTS.register(modEventBus);
        // Enregistrer l'économie (players.dat)
        EconomyIntegration.ATTACHMENTS.register(modEventBus);
        // Système de niveau des joueurs
        PlayerLevelAttachments.ATTACHMENTS.register(modEventBus);
        // Système de métiers des joueurs
        JobsDataAttachment.ATTACHMENTS.register(modEventBus);
        // Combat Log entities
        CombatLogEntities.ENTITIES.register(modEventBus);

        // Setup phase
        modEventBus.addListener(this::commonSetup);
        // Capabilities
        modEventBus.addListener(this::onRegisterCapabilities);
        // Entity attributes
        modEventBus.addListener(CombatLogEntities::registerAttributes);

        // Register event listeners
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        // Enregistrer nos handlers anti-xray
        NeoForge.EVENT_BUS.register(AntiXrayEventHandler.class);
        // Enregistrer TopLuck handler
        NeoForge.EVENT_BUS.register(TopLuckEventHandler.class);
        // Enregistrer Vanish handler
        NeoForge.EVENT_BUS.register(VanishEventHandler.class);
        // Audit
        NeoForge.EVENT_BUS.register(new EFAuditEvents());
        NeoForge.EVENT_BUS.register(new EFAuditEventsExtra());
        // Combat Logging
        NeoForge.EVENT_BUS.register(CombatLogEventHandler.class);

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
        HomeTeleportService.init();
    }

    private void onServerStarting(ServerStartingEvent event) {
        EFC.log.info("§aLoading §dfaction §7data...");
        FactionManager.load(event.getServer());
        // Ranks system
        EFRManager.get().load();
        // Initialiser les permissions par défaut (SavedData .dat) et appliquer si nécessaire
        fr.eriniumgroup.erinium_faction.core.faction.RankDefaultsSavedData.bootstrapAndApply(event.getServer());
        // Charger les configurations des métiers
        JobsConfigManager.init();
        // Charger les kits
        KitManager.getInstance().loadKits();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        EFC.log.info("§2Saving §dfaction §7data...");
        FactionManager.save(event.getServer());
        EFRManager.get().save();
        AuditRotator.compressTodayIfConfigured(); // Audit logs compression
        // Sauvegarder et arrêter l'anti-xray
        AntiXrayManager.getInstance().shutdown();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        EFC.log.info("§aRegistering §dfaction §7commands...");
        FactionCommand.register(event.getDispatcher());
        RankCommand.register(event.getDispatcher());
        // Commandes économie
        EconomyCommand.register(event.getDispatcher());
        // Commande ef (perm per-player + homes admin)
        fr.eriniumgroup.erinium_faction.commands.EFCommand.register(event.getDispatcher());
        // Commande système de niveau
        PlayerLevelCommand.register(event.getDispatcher());
        // Commande anti-xray
        AntiXrayCommand.register(event.getDispatcher());
        // Commande report de chat
        ReportChatCommand.register(event.getDispatcher());
        // Commande vanish
        VanishCommand.register(event.getDispatcher());
        // Commandes de homes joueur (/home, /sethome, /homes)
        HomeCommand.register(event.getDispatcher());
        // Commandes de bannière faction
        fr.eriniumgroup.erinium_faction.commands.BannerCommand.register(event.getDispatcher());
        // Commandes de cape faction
        fr.eriniumgroup.erinium_faction.commands.CapeCommand.register(event.getDispatcher());
        // Commande combat log
        CombatLogCommand.register(event.getDispatcher());
        // Commande kits
        KitCommand.register(event.getDispatcher());
        // Appliquer la garde globale des permissions sur toutes les commandes
        EFPerms.guardDispatcher(event.getDispatcher());

        TopLuckCommand.register(event.getDispatcher());
        TopLuckConfigCommand.register(event.getDispatcher());
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        EFCapabilities.register(event);
    }
}
