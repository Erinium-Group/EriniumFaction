package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.common.config.JobsConfigManager;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.jobs.data.JobsDataAttachment;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Gestionnaire des paquets réseau pour le système de métiers
 */
public class JobsPacketHandler {

    /**
     * Handler pour la synchronisation des données (Serveur -> Client)
     */
    public static void handleSyncJobsData(SyncJobsDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                // Mettre à jour les données côté client
                JobsClientData.setClientData(packet.data());
            }
        });
    }

    /**
     * Handler pour la synchronisation des configurations (Serveur -> Client)
     */
    public static void handleSyncJobsConfig(SyncJobsConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Mettre à jour les configurations côté client
            EFC.log.debug("§3Jobs§6/§7Packet", "Received configs: " + packet.configs().size() + " job types");
            for (var entry : packet.configs().entrySet()) {
                EFC.log.debug("§3Jobs§6/§7Packet", "- " + entry.getKey() + ": " + entry.getValue());
            }
            JobsClientConfig.setClientConfigs(packet.configs());
        });
    }

    /**
     * Synchronise les données de métiers d'un joueur vers son client
     */
    public static void syncJobsData(ServerPlayer player) {
        var data = player.getData(JobsDataAttachment.JOBS_DATA);
        PacketDistributor.sendToPlayer(player, new SyncJobsDataPacket(data.copy()));
    }

    /**
     * Synchronise les configurations de métiers vers un client
     */
    public static void syncJobsConfig(ServerPlayer player) {
        var configs = JobsConfigManager.getAllConfigs();
        EFC.log.debug("§3Jobs§6/§7Packet", "- Syncing configs to " + player.getName().getString() + ": " + configs.size() + " job types");
        for (var entry : configs.entrySet()) {
            EFC.log.debug("§3Jobs§6/§7Packet", "- " + entry.getKey() + " has " + entry.getValue().getXpEarning().size() + " XP entries and " + entry.getValue().getUnlocking().size() + " unlocks");
        }
        PacketDistributor.sendToPlayer(player, new SyncJobsConfigPacket(configs));
    }
}
