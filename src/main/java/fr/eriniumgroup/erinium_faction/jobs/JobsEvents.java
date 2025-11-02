package fr.eriniumgroup.erinium_faction.jobs;

import fr.eriniumgroup.erinium_faction.jobs.network.JobsPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Gestionnaire d'événements pour le système de métiers
 */
@EventBusSubscriber
public class JobsEvents {

    /**
     * Synchronise les données de métiers quand un joueur se connecte
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JobsPacketHandler.syncJobsData(player);
            JobsPacketHandler.syncJobsConfig(player); // Sync config aussi
        }
    }

    /**
     * Synchronise les données de métiers quand un joueur change de dimension
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JobsPacketHandler.syncJobsData(player);
        }
    }

    /**
     * Synchronise les données de métiers quand un joueur respawn
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JobsPacketHandler.syncJobsData(player);
        }
    }
}
