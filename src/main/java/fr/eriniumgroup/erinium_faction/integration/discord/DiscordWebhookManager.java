package fr.eriniumgroup.erinium_faction.integration.discord;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.server.level.ServerPlayer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Gestionnaire centralisé des webhooks Discord
 */
public class DiscordWebhookManager {

    // URLs des webhooks
    private static final String ANTICHEAT_WEBHOOK = "https://discord.com/api/webhooks/1436003983423705150/9skaVfrK8JLKuO8_A4_jMkaJfMa_tUwh62PPfXmo-uKKe0A0LFv6v4b5W8H16LNgxNxh";
    private static final String CHAT_REPORT_WEBHOOK = "https://discord.com/api/webhooks/1436004076507627570/Hsx2L99kjCNlmMQ4rVHDgeXeiF04zSGyUhjZLUxr7Taw-c7vfo4lVyTJS4Mz1uYv1x0-";
    private static final String CHAT_SYNC_WEBHOOK = "https://discord.com/api/webhooks/1436004164256796712/sR8-a4xTFozzSIOkDZvqvp5dAtWZorK5dlfzCKebATWvXY2mchTTfPPA-9dHDCbkB980";

    // Couleurs Discord
    private static final int COLOR_RED = 0xFF0000;
    private static final int COLOR_ORANGE = 0xFF6600;
    private static final int COLOR_GREEN = 0x00FF00;
    private static final int COLOR_BLUE = 0x0099FF;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Envoie un report d'anti-cheat vers Discord
     */
    public static void sendAntiCheatReport(ServerPlayer player, String violationType, int violations, String details) {
        try {
            DiscordWebhook webhook = new DiscordWebhook(ANTICHEAT_WEBHOOK);

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                    .setTitle("🚨 Détection Anti-Cheat")
                    .setColor(COLOR_RED)
                    .addField("Joueur", player.getName().getString(), true)
                    .addField("UUID", player.getUUID().toString(), true)
                    .addField("Violation", violationType, true)
                    .addField("Violations totales", String.valueOf(violations), true)
                    .addField("Détails", details, false)
                    .addField("Position", String.format("X: %.1f, Y: %.1f, Z: %.1f",
                            player.getX(), player.getY(), player.getZ()), false)
                    .addField("Dimension", player.level().dimension().location().toString(), true)
                    .setFooter("Erinium Faction Anti-Cheat", null)
                    .setThumbnail("https://crafatar.com/avatars/" + player.getUUID().toString() + "?overlay");

            webhook.addEmbed(embed);
            webhook.executeAsync().thenAccept(success -> {
                if (!success) {
                    EFC.log.warn("Discord", "Échec envoi anti-cheat report");
                }
            });

        } catch (Exception e) {
            EFC.log.error("Discord", "Erreur lors de l'envoi du report anti-cheat: {}", e.getMessage());
        }
    }

    /**
     * Envoie un report de chat vers Discord
     */
    public static void sendChatReport(ServerPlayer reporter, ServerPlayer reported, String message, String reason) {
        try {
            DiscordWebhook webhook = new DiscordWebhook(CHAT_REPORT_WEBHOOK);

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                    .setTitle("⚠️ Report de Chat")
                    .setColor(COLOR_ORANGE)
                    .addField("Joueur reporté", reported.getName().getString(), true)
                    .addField("UUID", reported.getUUID().toString(), true)
                    .addField("Reporté par", reporter.getName().getString(), true)
                    .addField("Message", "```" + message + "```", false)
                    .addField("Raison", reason, false)
                    .addField("Date", DATE_FORMAT.format(new Date()), true)
                    .setFooter("Erinium Faction Modération", null)
                    .setThumbnail("https://crafatar.com/avatars/" + reported.getUUID().toString() + "?overlay");

            webhook.addEmbed(embed);
            webhook.executeAsync().thenAccept(success -> {
                if (!success) {
                    EFC.log.warn("Discord", "Échec envoi chat report");
                }
            });

        } catch (Exception e) {
            EFC.log.error("Discord", "Erreur lors de l'envoi du chat report: {}", e.getMessage());
        }
    }

    /**
     * Envoie un message de chat Minecraft vers Discord
     */
    public static void sendMinecraftChatToDiscord(String playerName, String message, String rank, String faction) {
        try {
            // Supprimer les codes couleur Minecraft
            String cleanMessage = message.replaceAll("§[0-9a-fk-or]", "");

            DiscordWebhook webhook = new DiscordWebhook(CHAT_SYNC_WEBHOOK);

            // Format: [Rank] [Faction] Player: Message
            String rankClean = rank.replaceAll("§[0-9a-fk-or]", "");
            String factionClean = faction.replaceAll("§[0-9a-fk-or]", "");

            webhook.setUsername(playerName)
                    .setContent(String.format("**[%s]** [%s] %s", rankClean, factionClean, cleanMessage));

            webhook.executeAsync().thenAccept(success -> {
                if (!success) {
                    EFC.log.warn("Discord", "Échec envoi message vers Discord");
                }
            });

        } catch (Exception e) {
            EFC.log.error("Discord", "Erreur lors de l'envoi du message vers Discord: {}", e.getMessage());
        }
    }

    /**
     * Envoie un message de Discord vers Minecraft (sera appelé par le bot Discord JDA)
     */
    public static void sendDiscordChatToMinecraft(String username, String message, net.minecraft.server.MinecraftServer server) {
        try {
            if (server == null) return;

            // Format: [Discord] Username: Message
            net.minecraft.network.chat.Component component = net.minecraft.network.chat.Component.literal(
                    String.format("§9[Discord]§r §7%s§r: %s", username, message)
            );

            server.getPlayerList().broadcastSystemMessage(component, false);

        } catch (Exception e) {
            EFC.log.error("Discord", "Erreur lors de l'envoi du message vers Minecraft: {}", e.getMessage());
        }
    }

    /**
     * Envoie un événement serveur vers Discord (join, leave, etc.)
     */
    public static void sendServerEvent(String eventType, String message) {
        try {
            DiscordWebhook webhook = new DiscordWebhook(CHAT_SYNC_WEBHOOK);

            int color = switch (eventType) {
                case "JOIN" -> COLOR_GREEN;
                case "LEAVE" -> COLOR_RED;
                case "DEATH" -> COLOR_ORANGE;
                default -> COLOR_BLUE;
            };

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                    .setDescription(message)
                    .setColor(color);

            webhook.addEmbed(embed);
            webhook.executeAsync();

        } catch (Exception e) {
            EFC.log.error("Discord", "Erreur lors de l'envoi de l'événement serveur: {}", e.getMessage());
        }
    }

    /**
     * Test la connexion aux webhooks
     */
    public static void testWebhooks() {
        EFC.log.info("Discord", "Test des webhooks Discord...");

        // Test webhook anti-cheat
        new DiscordWebhook(ANTICHEAT_WEBHOOK)
                .setContent("✅ Test webhook Anti-Cheat")
                .executeAsync();

        // Test webhook chat report
        new DiscordWebhook(CHAT_REPORT_WEBHOOK)
                .setContent("✅ Test webhook Chat Report")
                .executeAsync();

        // Test webhook chat sync
        new DiscordWebhook(CHAT_SYNC_WEBHOOK)
                .setContent("✅ Test webhook Chat Sync")
                .executeAsync();
    }
}
