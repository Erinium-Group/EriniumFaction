package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.features.jobs.data.JobsData;

/**
 * Stockage côté client des données de métiers du joueur local
 */
public class JobsClientData {
    private static JobsData clientJobsData = new JobsData();

    /**
     * Met à jour les données côté client (appelé lors de la réception du paquet)
     */
    public static void setClientData(JobsData data) {
        clientJobsData = data;
    }

    /**
     * Obtient les données côté client
     */
    public static JobsData getClientData() {
        return clientJobsData;
    }

    /**
     * Réinitialise les données (appelé lors de la déconnexion)
     */
    public static void reset() {
        clientJobsData = new JobsData();
    }
}
