package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelAttachments;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Gestionnaire pour synchroniser les données de nameplate
 */
public class NameplatePacketHandler {

    /**
     * Envoie les données de nameplate d'un joueur à tous les clients dans le chunk
     */
    public static void syncPlayerNameplateData(ServerPlayer player) {
        PlayerLevelData levelData = player.getData(PlayerLevelAttachments.PLAYER_LEVEL_DATA);
        Faction faction = FactionManager.getFactionOf(player.getUUID());

        String factionName = faction != null ? faction.getName() : "";
        int level = levelData.getLevel();

        SyncPlayerNameplateDataPacket packet = new SyncPlayerNameplateDataPacket(
            player.getUUID(),
            factionName,
            level
        );

        // Envoyer à tous les joueurs suivant ce joueur
        PacketDistributor.sendToPlayersTrackingEntity(player, packet);

        // Envoyer aussi au joueur lui-même
        PacketDistributor.sendToPlayer(player, packet);
    }

    /**
     * Envoie les données de nameplate d'un joueur à un client spécifique
     */
    public static void syncPlayerNameplateDataToClient(ServerPlayer target, ServerPlayer clientPlayer) {
        PlayerLevelData levelData = target.getData(PlayerLevelAttachments.PLAYER_LEVEL_DATA);
        Faction faction = FactionManager.getFactionOf(target.getUUID());

        String factionName = faction != null ? faction.getName() : "";
        int level = levelData.getLevel();

        SyncPlayerNameplateDataPacket packet = new SyncPlayerNameplateDataPacket(
            target.getUUID(),
            factionName,
            level
        );

        PacketDistributor.sendToPlayer(clientPlayer, packet);
    }

    /**
     * Handler côté client pour recevoir les données de nameplate
     * IMPORTANT: Cette méthode est appelée uniquement côté client
     */
    public static void handleClientSide(SyncPlayerNameplateDataPacket packet, IPayloadContext context) {
        // Vérifier qu'on est côté client (ne devrait jamais arriver mais sécurité)
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        context.enqueueWork(() -> {
            // Appeler la méthode client via une classe séparée
            ClientNameplateHandler.updateCaches(packet.playerUUID(), packet.factionName(), packet.level());
        });
    }
}
