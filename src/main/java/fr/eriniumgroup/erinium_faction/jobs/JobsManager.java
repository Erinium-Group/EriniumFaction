package fr.eriniumgroup.erinium_faction.jobs;

import fr.eriniumgroup.erinium_faction.jobs.network.JobsClientData;
import fr.eriniumgroup.erinium_faction.jobs.network.JobsPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Gestionnaire principal du système de métiers
 * Fournit des méthodes pour accéder et modifier les données de métiers
 * Fonctionne côté serveur et client
 */
public class JobsManager {

    /**
     * Obtient les données de métiers d'un joueur (SERVEUR UNIQUEMENT)
     * @param player Le joueur (doit être ServerPlayer)
     * @return Les données de métiers
     */
    public static JobsData getJobsData(ServerPlayer player) {
        return player.getData(JobsDataAttachment.JOBS_DATA);
    }

    /**
     * Obtient les données de métiers du joueur local (CLIENT UNIQUEMENT)
     * @return Les données de métiers stockées côté client
     */
    public static JobsData getClientJobsData() {
        return JobsClientData.getClientData();
    }

    /**
     * Obtient les données de métiers selon le contexte (serveur ou client)
     * @param player Le joueur
     * @return Les données de métiers
     */
    public static JobsData getJobsDataUniversal(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return getJobsData(serverPlayer);
        } else {
            return getClientJobsData();
        }
    }

    /**
     * Obtient le niveau d'un métier spécifique (SERVEUR)
     */
    public static int getJobLevel(ServerPlayer player, JobType jobType) {
        return getJobsData(player).getLevel(jobType);
    }

    /**
     * Obtient l'XP d'un métier spécifique (SERVEUR)
     */
    public static int getJobExperience(ServerPlayer player, JobType jobType) {
        return getJobsData(player).getExperience(jobType);
    }

    /**
     * Définit le niveau d'un métier (SERVEUR UNIQUEMENT)
     * Synchronise automatiquement avec le client
     */
    public static void setJobLevel(ServerPlayer player, JobType jobType, int level) {
        getJobsData(player).setLevel(jobType, level);
        JobsPacketHandler.syncJobsData(player);
    }

    /**
     * Définit l'XP d'un métier (SERVEUR UNIQUEMENT)
     * Synchronise automatiquement avec le client
     */
    public static void setJobExperience(ServerPlayer player, JobType jobType, int experience) {
        getJobsData(player).setExperience(jobType, experience);
        JobsPacketHandler.syncJobsData(player);
    }

    /**
     * Ajoute de l'XP à un métier (SERVEUR UNIQUEMENT)
     * Gère automatiquement les level ups
     * @return Le nombre de niveaux gagnés
     */
    public static int addJobExperience(ServerPlayer player, JobType jobType, int amount) {
        JobsData data = getJobsData(player);
        int levelsGained = data.addExperience(jobType, amount);

        // Synchroniser avec le client
        JobsPacketHandler.syncJobsData(player);

        // Si le joueur a gagné des niveaux, retourner le nombre
        return levelsGained;
    }

    /**
     * Calcule l'XP nécessaire pour passer au niveau suivant
     * Formule: (1000 + 178.853 * LEVEL^1.5) * (LEVEL^1.005)
     * Niveaux: 1-100
     */
    public static int getExperienceForNextLevel(int currentLevel) {
        if (currentLevel >= JobsData.MAX_LEVEL) {
            return Integer.MAX_VALUE;
        }

        double base = 1000 + (178.853 * Math.pow(currentLevel, 1.5));
        double multiplier = Math.pow(currentLevel, 1.005);
        return (int) Math.round(base * multiplier);
    }

    /**
     * Synchronise les données d'un joueur avec son client
     */
    public static void syncPlayer(ServerPlayer player) {
        JobsPacketHandler.syncJobsData(player);
    }
}
