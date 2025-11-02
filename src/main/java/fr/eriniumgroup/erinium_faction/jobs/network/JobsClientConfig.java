package fr.eriniumgroup.erinium_faction.jobs.network;

import fr.eriniumgroup.erinium_faction.jobs.JobType;
import fr.eriniumgroup.erinium_faction.jobs.config.JobConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Stockage côté client des configurations de métiers synchronisées depuis le serveur
 */
public class JobsClientConfig {

    private static Map<JobType, JobConfig> clientConfigs = new HashMap<>();

    /**
     * Met à jour les configurations côté client (appelé lors de la réception du paquet)
     */
    public static void setClientConfigs(Map<JobType, JobConfig> configs) {
        clientConfigs = configs;
    }

    /**
     * Obtient la configuration d'un métier côté client
     */
    public static JobConfig getConfig(JobType jobType) {
        return clientConfigs.get(jobType);
    }

    /**
     * Obtient toutes les configurations côté client
     */
    public static Map<JobType, JobConfig> getAllConfigs() {
        return new HashMap<>(clientConfigs);
    }

    /**
     * Réinitialise les configurations (appelé lors de la déconnexion)
     */
    public static void reset() {
        clientConfigs.clear();
    }
}
