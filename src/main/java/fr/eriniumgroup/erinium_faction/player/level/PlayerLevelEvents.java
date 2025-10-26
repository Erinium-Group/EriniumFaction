package fr.eriniumgroup.erinium_faction.player.level;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.player.level.network.PlayerLevelPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
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

    /**
     * Donne de l'expérience lors de la mort d'une entité
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            int expGained = calculateExpFromKill(event.getEntity());

            if (expGained > 0) {
                PlayerLevelManager.addExperience(player, expGained);

                // Synchroniser les données avec le client après le gain d'XP
                PlayerLevelPacketHandler.syncPlayerData(player);
            }
        }
    }

    /**
     * Calcule l'expérience gagnée en tuant une entité
     */
    private static int calculateExpFromKill(net.minecraft.world.entity.LivingEntity entity) {
        // Boss
        if (entity instanceof EnderDragon) {
            return 5000;
        }
        if (entity instanceof WitherBoss) {
            return 3000;
        }

        // Monstres
        if (entity instanceof Monster) {
            return 10 + (int) (entity.getMaxHealth() * 2);
        }

        // Joueurs (PvP)
        if (entity instanceof ServerPlayer) {
            PlayerLevelData targetData = PlayerLevelManager.getLevelData((ServerPlayer) entity);
            return 50 + (targetData.getLevel() * 10);
        }

        return 0;
    }

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
