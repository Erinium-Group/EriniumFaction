package fr.eriniumgroup.erinium_faction.player.level.network;

import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelAttachments;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Gestionnaire des paquets réseau pour le système de niveau
 */
public class PlayerLevelPacketHandler {

    /**
     * Handler pour la distribution de point (Client -> Serveur)
     */
    public static void handleDistributePoint(DistributePointPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                boolean success = PlayerLevelManager.distributePoint(player, packet.attributeType());

                if (success) {
                    // Synchroniser les données avec le client
                    syncPlayerData(player);
                }
            }
        });
    }

    /**
     * Handler pour la réinitialisation des attributs (Client -> Serveur)
     */
    public static void handleResetAttributes(ResetAttributesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PlayerLevelManager.resetAttributes(player);

                // Synchroniser les données avec le client
                syncPlayerData(player);
            }
        });
    }

    /**
     * Handler pour la synchronisation des données (Serveur -> Client)
     */
    public static void handleSyncPlayerLevel(SyncPlayerLevelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                // Mettre à jour les données côté client
                context.player().setData(PlayerLevelAttachments.PLAYER_LEVEL_DATA, packet.data());
            }
        });
    }

    /**
     * Handler pour la synchronisation de l'état du token (Serveur -> Client)
     */
    public static void handleSyncResetToken(SyncResetTokenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Stocker l'état du token côté client
            if (context.player() != null) {
                PlayerLevelClientData.setHasResetToken(packet.hasToken());
            }
        });
    }

    /**
     * Handler pour l'ouverture du menu Stats (Client -> Serveur)
     */
    public static void handleOpenStatsMenu(OpenStatsMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Synchroniser les données avant d'ouvrir le menu
                syncPlayerData(player);

                player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (id, inventory, p) -> new fr.eriniumgroup.erinium_faction.player.level.gui.PlayerStatsMenu(id, inventory),
                    net.minecraft.network.chat.Component.translatable("player_level.title")
                ));
            }
        });
    }

    /**
     * Synchronise les données d'un joueur vers son client
     */
    public static void syncPlayerData(ServerPlayer player) {
        var data = PlayerLevelManager.getLevelData(player);
        PacketDistributor.sendToPlayer(player, new SyncPlayerLevelPacket(data.copy()));

        // Synchroniser aussi l'état du token
        boolean hasToken = PlayerLevelManager.hasResetToken(player);
        PacketDistributor.sendToPlayer(player, new SyncResetTokenPacket(hasToken));
    }
}
