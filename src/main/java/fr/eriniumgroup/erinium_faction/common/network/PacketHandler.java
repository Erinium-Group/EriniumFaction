package fr.eriniumgroup.erinium_faction.common.network;

import fr.eriniumgroup.erinium_faction.common.network.EFVariables.PlayerVariablesSyncMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.FactionMenuSettingsButtonMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.MenuStateUpdateMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.OpenFactionChestMessage;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.block_hp.BlockHpSyncMessage;
import fr.eriniumgroup.erinium_faction.gui.widgets.FactionGuiNetwork;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Système central d’enregistrement des paquets réseau (client <-> serveur).
 * - Utilise RegisterPayloadHandlersEvent (NeoForge 1.21)
 * - Versionnable (permet d’ajouter d’autres paquets facilement)
 */
@EventBusSubscriber(modid = EFC.MODID)
public class PacketHandler {

    // Optionnel: log manuel si vous appelez register() ailleurs
    public static void register() {
        EFC.log.info("Initialisation du système réseau...");
        // ...existing code...
    }

    /**
     * Enregistre tous les paquets du mod.
     * Ajoutez ici vos autres paquets (playToServer / playToClient).
     */
    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        // Version protocole de vos paquets (à incrémenter si vous cassez la compat)
        var registrar = event.registrar("1");

        // Paquet GUI -> Serveur (clics, actions)
        registrar.playToServer(FactionGuiNetwork.TYPE, FactionGuiNetwork.STREAM_CODEC, FactionGuiNetwork::handleData);
        registrar.playToServer(FactionMenuSettingsButtonMessage.TYPE, FactionMenuSettingsButtonMessage.STREAM_CODEC, FactionMenuSettingsButtonMessage::handleData);
        registrar.playToServer(OpenFactionChestMessage.TYPE, OpenFactionChestMessage.STREAM_CODEC, OpenFactionChestMessage::handleData);

        // Serveur -> Client (sync affichage HP de bloc)
        registrar.playToClient(BlockHpSyncMessage.TYPE, BlockHpSyncMessage.STREAM_CODEC, BlockHpSyncMessage::handleData);

        // Menus: synchro état (les deux sens)
        registrar.playBidirectional(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage.STREAM_CODEC, MenuStateUpdateMessage::handleMenuState);

        // Variables
        registrar.playBidirectional(PlayerVariablesSyncMessage.TYPE, PlayerVariablesSyncMessage.STREAM_CODEC, PlayerVariablesSyncMessage::handleData);

        // Nouveaux paquets: carte des claims
        registrar.playToServer(fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage.TYPE, fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage.STREAM_CODEC, fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage::handleData);
        registrar.playToClient(fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapDataMessage.TYPE, fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapDataMessage.STREAM_CODEC, fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapDataMessage::handleData);

        // Nouveau paquet clientbound pour synchroniser l’état des Settings
        registrar.playToClient(fr.eriniumgroup.erinium_faction.common.network.packets.FactionSettingsStateMessage.TYPE, fr.eriniumgroup.erinium_faction.common.network.packets.FactionSettingsStateMessage.STREAM_CODEC, fr.eriniumgroup.erinium_faction.common.network.packets.FactionSettingsStateMessage::handleData);

        // Nouveau paquet clientbound: afficher un titre overlay
        registrar.playToClient(fr.eriniumgroup.erinium_faction.common.network.packets.FactionTitlePacket.TYPE, fr.eriniumgroup.erinium_faction.common.network.packets.FactionTitlePacket.STREAM_CODEC, fr.eriniumgroup.erinium_faction.common.network.packets.FactionTitlePacket::handleData);

        // Paquet clientbound: synchroniser les données de faction
        registrar.playToClient(fr.eriniumgroup.erinium_faction.common.network.packets.FactionDataPacket.TYPE, fr.eriniumgroup.erinium_faction.common.network.packets.FactionDataPacket.STREAM_CODEC, fr.eriniumgroup.erinium_faction.common.network.packets.FactionDataPacketHandler::handleFactionData);

        // Paquets système de niveau joueur
        registrar.playToServer(fr.eriniumgroup.erinium_faction.player.level.network.OpenStatsMenuPacket.TYPE, fr.eriniumgroup.erinium_faction.player.level.network.OpenStatsMenuPacket.STREAM_CODEC, fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelPacketHandler::handleOpenStatsMenu);
        registrar.playToServer(fr.eriniumgroup.erinium_faction.player.level.network.DistributePointPacket.TYPE, fr.eriniumgroup.erinium_faction.player.level.network.DistributePointPacket.STREAM_CODEC, fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelPacketHandler::handleDistributePoint);
        registrar.playToServer(fr.eriniumgroup.erinium_faction.player.level.network.ResetAttributesPacket.TYPE, fr.eriniumgroup.erinium_faction.player.level.network.ResetAttributesPacket.STREAM_CODEC, fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelPacketHandler::handleResetAttributes);
        registrar.playToClient(fr.eriniumgroup.erinium_faction.player.level.network.SyncPlayerLevelPacket.TYPE, fr.eriniumgroup.erinium_faction.player.level.network.SyncPlayerLevelPacket.STREAM_CODEC, fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelPacketHandler::handleSyncPlayerLevel);
        registrar.playToClient(fr.eriniumgroup.erinium_faction.player.level.network.SyncResetTokenPacket.TYPE, fr.eriniumgroup.erinium_faction.player.level.network.SyncResetTokenPacket.STREAM_CODEC, fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelPacketHandler::handleSyncResetToken);

        EFC.log.info("Paquets réseau enregistrés: FactionGuiNetwork, FactionMenuSettingsButtonMessage (serverbound), BlockHpSyncMessage (clientbound), MenuStateUpdateMessage (bi), PlayerVariables (bi), ClaimsMap (request/data), FactionSettingsStateMessage (clientbound), FactionTitlePacket (clientbound), FactionDataPacket (clientbound), PlayerLevel (open/distribute/reset/sync/token)");
    }
}
