package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.network.packets.SyncPlayerRankPacket;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Event handler pour synchroniser les rangs des joueurs vers les clients
 */
@EventBusSubscriber(modid = "erinium_faction")
public class RankSyncEventHandler {

    /**
     * Quand un joueur se connecte, synchroniser les rangs de tous les joueurs
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer joiningPlayer)) {
            return;
        }

        // Synchroniser les rangs de tous les joueurs en ligne vers le nouveau joueur
        for (ServerPlayer player : joiningPlayer.serverLevel().players()) {
            syncPlayerRankToClient(player, joiningPlayer);
        }
    }

    /**
     * Synchronise le rang d'un joueur vers un client spécifique
     */
    public static void syncPlayerRankToClient(ServerPlayer targetPlayer, ServerPlayer receivingPlayer) {
        EFRManager.Rank rank = EFRManager.get().getPlayerRank(targetPlayer.getUUID());

        if (rank == null) {
            // Pas de rang = envoyer un paquet vide pour nettoyer le cache
            SyncPlayerRankPacket packet = new SyncPlayerRankPacket(
                targetPlayer.getUUID(),
                "",
                "",
                "",
                "",
                0
            );
            PacketDistributor.sendToPlayer(receivingPlayer, packet);
        } else {
            SyncPlayerRankPacket packet = new SyncPlayerRankPacket(
                targetPlayer.getUUID(),
                rank.id,
                rank.displayName != null ? rank.displayName : "",
                rank.prefix != null ? rank.prefix : "",
                rank.suffix != null ? rank.suffix : "",
                rank.priority
            );
            PacketDistributor.sendToPlayer(receivingPlayer, packet);
        }
    }

    /**
     * Synchronise le rang d'un joueur vers tous les clients
     */
    public static void syncPlayerRankToAll(ServerPlayer targetPlayer) {
        EFRManager.Rank rank = EFRManager.get().getPlayerRank(targetPlayer.getUUID());

        if (rank == null) {
            // Pas de rang = envoyer un paquet vide pour nettoyer le cache
            SyncPlayerRankPacket packet = new SyncPlayerRankPacket(
                targetPlayer.getUUID(),
                "",
                "",
                "",
                "",
                0
            );
            PacketDistributor.sendToAllPlayers(packet);
        } else {
            SyncPlayerRankPacket packet = new SyncPlayerRankPacket(
                targetPlayer.getUUID(),
                rank.id,
                rank.displayName != null ? rank.displayName : "",
                rank.prefix != null ? rank.prefix : "",
                rank.suffix != null ? rank.suffix : "",
                rank.priority
            );
            PacketDistributor.sendToAllPlayers(packet);
        }
    }
}
