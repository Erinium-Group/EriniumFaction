package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.network.packets.NameplatePacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event handler pour synchroniser les données de nameplate
 */
@EventBusSubscriber(modid = "erinium_faction")
public class NameplateEventHandler {

    /**
     * Quand un joueur se connecte, envoyer ses données de nameplate
     * et envoyer les données de tous les joueurs en ligne au nouveau joueur
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Envoyer les données du joueur qui se connecte à LUI-MÊME
        NameplatePacketHandler.syncPlayerNameplateData(serverPlayer);

        // Pour chaque autre joueur en ligne
        for (ServerPlayer otherPlayer : serverPlayer.serverLevel().players()) {
            if (!otherPlayer.getUUID().equals(serverPlayer.getUUID())) {
                // Envoyer les données de l'autre joueur au nouveau joueur
                NameplatePacketHandler.syncPlayerNameplateDataToClient(otherPlayer, serverPlayer);

                // Envoyer les données du nouveau joueur à l'autre joueur
                NameplatePacketHandler.syncPlayerNameplateDataToClient(serverPlayer, otherPlayer);
            }
        }
    }

    /**
     * Périodiquement, synchroniser les données de nameplate
     * (au cas où faction/niveau change)
     */
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();
    private static final int SYNC_INTERVAL = 100; // Toutes les 5 secondes (20 ticks/sec * 5)

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // S'assurer qu'on est côté serveur
        if (serverPlayer.level().isClientSide) {
            return;
        }

        UUID playerUUID = serverPlayer.getUUID();
        int currentTicks = tickCounters.getOrDefault(playerUUID, 0);
        currentTicks++;

        if (currentTicks >= SYNC_INTERVAL) {
            tickCounters.put(playerUUID, 0);
            // Synchroniser les données périodiquement
            NameplatePacketHandler.syncPlayerNameplateData(serverPlayer);
        } else {
            tickCounters.put(playerUUID, currentTicks);
        }
    }

    /**
     * Nettoyer le compteur quand un joueur se déconnecte
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            tickCounters.remove(serverPlayer.getUUID());
        }
    }
}
