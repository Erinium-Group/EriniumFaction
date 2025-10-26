package fr.eriniumgroup.erinium_faction.common.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Gestionnaire central du système de webhook Discord.
 * Permet d'envoyer des notifications Discord 100% customisables via la config.
 *
 * Utilisation dans d'autres classes:
 * <pre>
 * DiscordWebhookManager.sendWebhook(DiscordWebhookEvent.FACTION_CREATE)
 *     .placeholder("player", playerName)
 *     .placeholder("faction", factionName)
 *     .send();
 * </pre>
 */
public class DiscordWebhookManager {

    /**
     * Enum des événements Discord disponibles
     */
    public enum DiscordWebhookEvent {
        FACTION_CREATE(EFConfig.DISCORD_EVENT_FACTION_CREATE, EFConfig.DISCORD_TITLE_FACTION_CREATE,
                       EFConfig.DISCORD_DESC_FACTION_CREATE, EFConfig.DISCORD_COLOR_FACTION_CREATE),
        FACTION_DELETE(EFConfig.DISCORD_EVENT_FACTION_DELETE, EFConfig.DISCORD_TITLE_FACTION_DELETE,
                       EFConfig.DISCORD_DESC_FACTION_DELETE, EFConfig.DISCORD_COLOR_FACTION_DELETE),
        FACTION_JOIN(EFConfig.DISCORD_EVENT_FACTION_JOIN, EFConfig.DISCORD_TITLE_FACTION_JOIN,
                     EFConfig.DISCORD_DESC_FACTION_JOIN, EFConfig.DISCORD_COLOR_FACTION_JOIN),
        FACTION_LEAVE(EFConfig.DISCORD_EVENT_FACTION_LEAVE, EFConfig.DISCORD_TITLE_FACTION_LEAVE,
                      EFConfig.DISCORD_DESC_FACTION_LEAVE, EFConfig.DISCORD_COLOR_FACTION_LEAVE),
        FACTION_KICK(EFConfig.DISCORD_EVENT_FACTION_KICK, EFConfig.DISCORD_TITLE_FACTION_KICK,
                     EFConfig.DISCORD_DESC_FACTION_KICK, EFConfig.DISCORD_COLOR_FACTION_KICK),
        FACTION_PROMOTE(EFConfig.DISCORD_EVENT_FACTION_PROMOTE, EFConfig.DISCORD_TITLE_FACTION_PROMOTE,
                        EFConfig.DISCORD_DESC_FACTION_PROMOTE, EFConfig.DISCORD_COLOR_FACTION_PROMOTE),
        FACTION_DEMOTE(EFConfig.DISCORD_EVENT_FACTION_DEMOTE, EFConfig.DISCORD_TITLE_FACTION_DEMOTE,
                       EFConfig.DISCORD_DESC_FACTION_DEMOTE, EFConfig.DISCORD_COLOR_FACTION_DEMOTE),
        FACTION_CLAIM(EFConfig.DISCORD_EVENT_FACTION_CLAIM, EFConfig.DISCORD_TITLE_FACTION_CLAIM,
                      EFConfig.DISCORD_DESC_FACTION_CLAIM, EFConfig.DISCORD_COLOR_FACTION_CLAIM),
        FACTION_UNCLAIM(EFConfig.DISCORD_EVENT_FACTION_UNCLAIM, EFConfig.DISCORD_TITLE_FACTION_UNCLAIM,
                        EFConfig.DISCORD_DESC_FACTION_UNCLAIM, EFConfig.DISCORD_COLOR_FACTION_UNCLAIM),
        FACTION_WAR(EFConfig.DISCORD_EVENT_FACTION_WAR, EFConfig.DISCORD_TITLE_FACTION_WAR,
                    EFConfig.DISCORD_DESC_FACTION_WAR, EFConfig.DISCORD_COLOR_FACTION_WAR),
        FACTION_ALLY(EFConfig.DISCORD_EVENT_FACTION_ALLY, EFConfig.DISCORD_TITLE_FACTION_ALLY,
                     EFConfig.DISCORD_DESC_FACTION_ALLY, EFConfig.DISCORD_COLOR_FACTION_ALLY),
        FACTION_KILL(EFConfig.DISCORD_EVENT_FACTION_KILL, EFConfig.DISCORD_TITLE_FACTION_KILL,
                     EFConfig.DISCORD_DESC_FACTION_KILL, EFConfig.DISCORD_COLOR_FACTION_KILL),
        FACTION_DEATH(EFConfig.DISCORD_EVENT_FACTION_DEATH, EFConfig.DISCORD_TITLE_FACTION_DEATH,
                      EFConfig.DISCORD_DESC_FACTION_DEATH, EFConfig.DISCORD_COLOR_FACTION_DEATH),
        FACTION_LEVEL_UP(EFConfig.DISCORD_EVENT_FACTION_LEVEL_UP, EFConfig.DISCORD_TITLE_FACTION_LEVEL_UP,
                         EFConfig.DISCORD_DESC_FACTION_LEVEL_UP, EFConfig.DISCORD_COLOR_FACTION_LEVEL_UP);

        private final ModConfigSpec.BooleanValue enabled;
        private final ModConfigSpec.ConfigValue<String> title;
        private final ModConfigSpec.ConfigValue<String> description;
        private final ModConfigSpec.IntValue color;

        DiscordWebhookEvent(ModConfigSpec.BooleanValue enabled,
                           ModConfigSpec.ConfigValue<String> title,
                           ModConfigSpec.ConfigValue<String> description,
                           ModConfigSpec.IntValue color) {
            this.enabled = enabled;
            this.title = title;
            this.description = description;
            this.color = color;
        }

        public boolean isEnabled() {
            return enabled.get();
        }

        public String getTitle() {
            return title.get();
        }

        public String getDescription() {
            return description.get();
        }

        public int getColor() {
            return color.get();
        }
    }

    /**
     * Builder pour construire et envoyer un webhook Discord
     */
    public static class WebhookBuilder {
        private final DiscordWebhookEvent event;
        private final Map<String, String> placeholders = new HashMap<>();
        private String customTitle = null;
        private String customDescription = null;
        private Integer customColor = null;
        private final Map<String, String> fields = new HashMap<>();

        private WebhookBuilder(DiscordWebhookEvent event) {
            this.event = event;
        }

        /**
         * Ajoute un placeholder à remplacer dans le message
         * @param key Nom du placeholder (sans les accolades)
         * @param value Valeur à remplacer
         */
        public WebhookBuilder placeholder(String key, Object value) {
            placeholders.put(key, String.valueOf(value));
            return this;
        }

        /**
         * Remplace le titre par défaut
         */
        public WebhookBuilder title(String title) {
            this.customTitle = title;
            return this;
        }

        /**
         * Remplace la description par défaut
         */
        public WebhookBuilder description(String description) {
            this.customDescription = description;
            return this;
        }

        /**
         * Remplace la couleur par défaut
         */
        public WebhookBuilder color(int color) {
            this.customColor = color;
            return this;
        }

        /**
         * Ajoute un champ personnalisé à l'embed
         * @param name Nom du champ
         * @param value Valeur du champ
         */
        public WebhookBuilder field(String name, String value) {
            fields.put(name, value);
            return this;
        }

        /**
         * Envoie le webhook de manière asynchrone
         */
        public CompletableFuture<Boolean> send() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Vérifier si le système est activé
                    if (!EFConfig.DISCORD_WEBHOOK_ENABLED.get()) {
                        return false;
                    }

                    // Vérifier si l'événement est activé
                    if (!event.isEnabled()) {
                        return false;
                    }

                    // Vérifier si l'URL est configurée
                    String webhookUrl = EFConfig.DISCORD_WEBHOOK_URL.get();
                    if (webhookUrl == null || webhookUrl.isEmpty()) {
                        EFC.log.warn("Discord webhook URL not configured");
                        return false;
                    }

                    // Construire le JSON du webhook
                    JsonObject json = buildWebhookJson();

                    // Envoyer le webhook
                    return sendWebhookRequest(webhookUrl, json);

                } catch (Exception e) {
                    EFC.log.error("Error sending Discord webhook: " + e.getMessage(), e);
                    return false;
                }
            });
        }

        /**
         * Construit le JSON du webhook
         */
        private JsonObject buildWebhookJson() {
            JsonObject json = new JsonObject();

            // Username et avatar du bot
            String username = EFConfig.DISCORD_WEBHOOK_USERNAME.get();
            if (username != null && !username.isEmpty()) {
                json.addProperty("username", username);
            }

            String avatarUrl = EFConfig.DISCORD_WEBHOOK_AVATAR_URL.get();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                json.addProperty("avatar_url", avatarUrl);
            }

            // Construire l'embed
            JsonObject embed = new JsonObject();

            // Titre
            String title = customTitle != null ? customTitle : event.getTitle();
            title = replacePlaceholders(title);
            embed.addProperty("title", title);

            // Description
            String description = customDescription != null ? customDescription : event.getDescription();
            description = replacePlaceholders(description);
            embed.addProperty("description", description);

            // Couleur
            int color = customColor != null ? customColor : event.getColor();
            embed.addProperty("color", color);

            // Timestamp
            embed.addProperty("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

            // Footer
            String footerText = EFConfig.DISCORD_FOOTER_TEXT.get();
            if (footerText != null && !footerText.isEmpty()) {
                JsonObject footer = new JsonObject();
                footerText = replacePlaceholders(footerText);
                footer.addProperty("text", footerText);

                String footerIconUrl = EFConfig.DISCORD_FOOTER_ICON_URL.get();
                if (footerIconUrl != null && !footerIconUrl.isEmpty()) {
                    footer.addProperty("icon_url", footerIconUrl);
                }
                embed.add("footer", footer);
            }

            // Thumbnail
            String thumbnailUrl = EFConfig.DISCORD_THUMBNAIL_URL.get();
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                JsonObject thumbnail = new JsonObject();
                thumbnail.addProperty("url", thumbnailUrl);
                embed.add("thumbnail", thumbnail);
            }

            // Champs personnalisés
            if (!fields.isEmpty()) {
                JsonArray fieldsArray = new JsonArray();
                for (Map.Entry<String, String> field : fields.entrySet()) {
                    JsonObject fieldObj = new JsonObject();
                    fieldObj.addProperty("name", replacePlaceholders(field.getKey()));
                    fieldObj.addProperty("value", replacePlaceholders(field.getValue()));
                    fieldObj.addProperty("inline", true);
                    fieldsArray.add(fieldObj);
                }
                embed.add("fields", fieldsArray);
            }

            // Ajouter l'embed au JSON principal
            JsonArray embeds = new JsonArray();
            embeds.add(embed);
            json.add("embeds", embeds);

            return json;
        }

        /**
         * Remplace les placeholders dans une chaîne
         */
        private String replacePlaceholders(String text) {
            if (text == null) return "";

            // Timestamp actuel
            String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            text = text.replace("{time}", currentTime);

            // Placeholders personnalisés
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                text = text.replace("{" + entry.getKey() + "}", entry.getValue());
            }

            return text;
        }

        /**
         * Envoie la requête HTTP au webhook Discord
         */
        private boolean sendWebhookRequest(String webhookUrl, JsonObject json) {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                // Envoyer le JSON
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Vérifier la réponse
                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    EFC.log.debug("Discord webhook sent successfully");
                    return true;
                } else {
                    EFC.log.warn("Discord webhook failed with response code: " + responseCode);
                    return false;
                }

            } catch (Exception e) {
                EFC.log.error("Error sending Discord webhook request: " + e.getMessage(), e);
                return false;
            }
        }
    }

    /**
     * Point d'entrée principal pour envoyer un webhook
     * @param event Type d'événement à envoyer
     * @return Builder pour configurer le webhook
     */
    public static WebhookBuilder sendWebhook(DiscordWebhookEvent event) {
        return new WebhookBuilder(event);
    }

    /**
     * Méthode utilitaire pour envoyer un webhook simple sans placeholders
     */
    public static CompletableFuture<Boolean> sendSimpleWebhook(DiscordWebhookEvent event) {
        return sendWebhook(event).send();
    }
}
