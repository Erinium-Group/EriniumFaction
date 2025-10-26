package fr.eriniumgroup.erinium_faction.player.level.gui;

import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelManager;
import fr.eriniumgroup.erinium_faction.player.level.network.DistributePointPacket;
import fr.eriniumgroup.erinium_faction.player.level.network.ResetAttributesPacket;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handler pour les paquets réseau du système de stats
 */
public class PlayerStatsPacketHandler {

    /**
     * Envoie une demande pour distribuer un point
     */
    public static void sendDistributePoint(PlayerLevelManager.AttributeType type) {
        PacketDistributor.sendToServer(new DistributePointPacket(type));
    }

    /**
     * Envoie une demande pour réinitialiser les attributs
     */
    public static void sendResetAttributes() {
        PacketDistributor.sendToServer(new ResetAttributesPacket());
    }
}
