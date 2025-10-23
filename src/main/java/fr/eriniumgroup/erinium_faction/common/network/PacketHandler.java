package fr.eriniumgroup.erinium_faction.common.network;

import fr.eriniumgroup.erinium_faction.common.network.packets.FactionMenuSettingsButtonMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.GuiForConstructButtonMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.MenuStateUpdateMessage;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.block_hp.BlockHpSyncMessage;
import fr.eriniumgroup.erinium_faction.gui.widgets.FactionGuiNetwork;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import fr.eriniumgroup.erinium_faction.common.network.EFVariables.*;

/**
 * Système central d’enregistrement des paquets réseau (client <-> serveur).
 * - Utilise RegisterPayloadHandlersEvent (NeoForge 1.21)
 * - Versionnable (permet d’ajouter d’autres paquets facilement)
 */
@EventBusSubscriber(modid = EFC.MODID, bus = EventBusSubscriber.Bus.MOD)
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
        registrar.playToServer(GuiForConstructButtonMessage.TYPE, GuiForConstructButtonMessage.STREAM_CODEC, GuiForConstructButtonMessage::handleData);
        registrar.playToServer(FactionMenuSettingsButtonMessage.TYPE, FactionMenuSettingsButtonMessage.STREAM_CODEC, FactionMenuSettingsButtonMessage::handleData);

        // Serveur -> Client (sync affichage HP de bloc)
        registrar.playToClient(BlockHpSyncMessage.TYPE, BlockHpSyncMessage.STREAM_CODEC, BlockHpSyncMessage::handleData);

        // Menus: synchro état (les deux sens)
        registrar.playBidirectional(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage.STREAM_CODEC, MenuStateUpdateMessage::handleMenuState);

        // Variables
        registrar.playBidirectional(PlayerVariablesSyncMessage.TYPE, PlayerVariablesSyncMessage.STREAM_CODEC, PlayerVariablesSyncMessage::handleData);

        EFC.log.info("Paquets réseau enregistrés: FactionGuiNetwork, GuiForConstructButtonMessage, FactionMenuSettingsButtonMessage (serverbound), BlockHpSyncMessage (clientbound), MenuStateUpdateMessage (bi-directionnel), PlayerVariables (bi-directionnel)");
    }
}
