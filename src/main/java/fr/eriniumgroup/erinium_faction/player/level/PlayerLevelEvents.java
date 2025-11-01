package fr.eriniumgroup.erinium_faction.player.level;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Événements pour le système de niveau des joueurs
 */
@EventBusSubscriber(modid = EFC.MODID)
public class PlayerLevelEvents {

    /**
     * Initialise les attributs du joueur lors de sa connexion
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerLevelManager.initializePlayer(player);

            // Synchroniser les données avec le client
            PlayerLevelPacketHandler.syncPlayerData(player);

            PlayerLevelData data = PlayerLevelManager.getLevelData(player);
            EFC.log.info("Player " + player.getName().getString() + " logged in - Level: " + data.getLevel());
        }
    }

    /**
     * Réapplique les attributs lors du respawn
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerLevelManager.initializePlayer(player);

            // Synchroniser les données avec le client
            PlayerLevelPacketHandler.syncPlayerData(player);
        }
    }

    // Note: Les événements de gain d'XP ont été supprimés car le système utilise maintenant uniquement les niveaux

    /**
     * Met à jour périodiquement les attributs (au cas où)
     */
    private static int updateTicks = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            updateTicks++;

            // Toutes les 5 secondes (100 ticks), vérifier les attributs
            if (updateTicks >= 100) {
                updateTicks = 0;

                PlayerLevelData data = PlayerLevelManager.getLevelData(player);
                // Seulement si le joueur a des modificateurs à appliquer
                if (data.getLevel() > 1 || data.getTotalPointsSpent() > 0) {
                    PlayerLevelManager.updatePlayerAttributes(player, data);
                }
            }
        }
    }

    /**
     * Clone les données lors de la mort (pour les garder après respawn)
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            PlayerLevelData oldData = PlayerLevelManager.getLevelData(oldPlayer);
            PlayerLevelData newData = PlayerLevelManager.getLevelData(newPlayer);

            // Copier toutes les données
            newData.deserializeNBT(oldData.serializeNBT());
        }
    }
}
