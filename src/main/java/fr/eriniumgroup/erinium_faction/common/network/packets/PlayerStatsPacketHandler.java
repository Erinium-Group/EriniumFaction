package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelManager;
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
