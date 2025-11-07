package fr.eriniumgroup.erinium_faction.integration.discord;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère les reports de chat en attente de confirmation
 */
public class ChatReportManager {

    // Structure pour stocker les rapports en attente
    private static final Map<UUID, PendingReport> pendingReports = new ConcurrentHashMap<>();

    // Cooldown pour éviter le spam de reports (30 secondes)
    private static final long REPORT_COOLDOWN = 30 * 1000;

    // Map pour tracker les derniers reports par joueur
    private static final Map<UUID, Long> lastReportTime = new ConcurrentHashMap<>();

    /**
     * Crée un rapport en attente de confirmation
     */
    public static boolean createPendingReport(ServerPlayer reporter, ServerPlayer reported, String message) {
        UUID reporterUUID = reporter.getUUID();

        // Vérifier le cooldown
        Long lastReport = lastReportTime.get(reporterUUID);
        if (lastReport != null && System.currentTimeMillis() - lastReport < REPORT_COOLDOWN) {
            long remainingSeconds = (REPORT_COOLDOWN - (System.currentTimeMillis() - lastReport)) / 1000;
            reporter.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cVous devez attendre " + remainingSeconds + " secondes avant de pouvoir reporter à nouveau."
            ));
            return false;
        }

        // Créer le rapport en attente
        PendingReport report = new PendingReport(reporter.getUUID(), reported.getUUID(),
                reported.getName().getString(), message);
        pendingReports.put(reporterUUID, report);

        // Le rapport expire après 30 secondes
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor()
                .schedule(() -> {
                    if (pendingReports.remove(reporterUUID) != null) {
                        try {
                            ServerPlayer player = reporter.server.getPlayerList().getPlayer(reporterUUID);
                            if (player != null) {
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                        "§cVotre rapport a expiré (non confirmé)."
                                ));
                            }
                        } catch (Exception ignored) {}
                    }
                }, 30, java.util.concurrent.TimeUnit.SECONDS);

        return true;
    }

    /**
     * Confirme un rapport et l'envoie vers Discord
     */
    public static boolean confirmReport(ServerPlayer reporter, String reason) {
        UUID reporterUUID = reporter.getUUID();
        PendingReport report = pendingReports.remove(reporterUUID);

        if (report == null) {
            reporter.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cAucun rapport en attente de confirmation."
            ));
            return false;
        }

        // Récupérer le joueur reporté
        ServerPlayer reported = reporter.server.getPlayerList().getPlayer(report.reportedUUID);
        if (reported == null) {
            reporter.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cLe joueur reporté n'est plus en ligne."
            ));
            return false;
        }

        // Envoyer le rapport vers Discord
        DiscordWebhookManager.sendChatReport(reporter, reported, report.message, reason);

        // Mettre à jour le cooldown
        lastReportTime.put(reporterUUID, System.currentTimeMillis());

        reporter.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§aRapport envoyé avec succès ! Merci d'avoir signalé ce comportement."
        ));

        return true;
    }

    /**
     * Annule un rapport en attente
     */
    public static boolean cancelReport(ServerPlayer reporter) {
        UUID reporterUUID = reporter.getUUID();
        PendingReport report = pendingReports.remove(reporterUUID);

        if (report == null) {
            reporter.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cAucun rapport en attente d'annulation."
            ));
            return false;
        }

        reporter.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§eRapport annulé."
        ));
        return true;
    }

    /**
     * Vérifie si un joueur a un rapport en attente
     */
    public static boolean hasPendingReport(UUID playerUUID) {
        return pendingReports.containsKey(playerUUID);
    }

    /**
     * Récupère le rapport en attente d'un joueur
     */
    public static PendingReport getPendingReport(UUID playerUUID) {
        return pendingReports.get(playerUUID);
    }

    /**
     * Structure pour stocker les informations d'un rapport en attente
     */
    public static class PendingReport {
        public final UUID reporterUUID;
        public final UUID reportedUUID;
        public final String reportedName;
        public final String message;
        public final long timestamp;

        public PendingReport(UUID reporterUUID, UUID reportedUUID, String reportedName, String message) {
            this.reporterUUID = reporterUUID;
            this.reportedUUID = reportedUUID;
            this.reportedName = reportedName;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
