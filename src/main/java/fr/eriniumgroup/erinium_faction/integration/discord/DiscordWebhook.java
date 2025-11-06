package fr.eriniumgroup.erinium_faction.integration.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.eriniumgroup.erinium_faction.core.EFC;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Classe pour envoyer des messages vers Discord via Webhook
 */
public class DiscordWebhook {
    private final String webhookUrl;
    private String content;
    private String username;
    private String avatarUrl;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public DiscordWebhook setContent(String content) {
        this.content = content;
        return this;
    }

    public DiscordWebhook setUsername(String username) {
        this.username = username;
        return this;
    }

    public DiscordWebhook setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public DiscordWebhook addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
        return this;
    }

    /**
     * Envoie le webhook de manière asynchrone
     */
    public CompletableFuture<Boolean> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute();
            } catch (Exception e) {
                EFC.log.error("Discord", "Erreur lors de l'envoi du webhook: {}", e.getMessage());
                return false;
            }
        });
    }

    /**
     * Envoie le webhook de manière synchrone
     */
    public boolean execute() {
        try {
            JsonObject json = new JsonObject();

            if (content != null && !content.isEmpty()) {
                json.addProperty("content", content);
            }

            if (username != null && !username.isEmpty()) {
                json.addProperty("username", username);
            }

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                json.addProperty("avatar_url", avatarUrl);
            }

            if (!embeds.isEmpty()) {
                JsonArray embedArray = new JsonArray();
                for (EmbedObject embed : embeds) {
                    embedArray.add(embed.toJson());
                }
                json.add("embeds", embedArray);
            }

            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "EriniumFaction-Webhook");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            if (responseCode == 204 || responseCode == 200) {
                return true;
            } else {
                EFC.log.warn("Discord", "Webhook retourné code {}", responseCode);
                return false;
            }

        } catch (Exception e) {
            EFC.log.error("Discord", "Erreur webhook: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Classe pour créer des embeds Discord
     */
    public static class EmbedObject {
        private String title;
        private String description;
        private String url;
        private Integer color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private final List<Field> fields = new ArrayList<>();

        public EmbedObject setTitle(String title) {
            this.title = title;
            return this;
        }

        public EmbedObject setDescription(String description) {
            this.description = description;
            return this;
        }

        public EmbedObject setUrl(String url) {
            this.url = url;
            return this;
        }

        public EmbedObject setColor(int color) {
            this.color = color;
            return this;
        }

        public EmbedObject setFooter(String text, String icon) {
            this.footer = new Footer(text, icon);
            return this;
        }

        public EmbedObject setThumbnail(String url) {
            this.thumbnail = new Thumbnail(url);
            return this;
        }

        public EmbedObject setImage(String url) {
            this.image = new Image(url);
            return this;
        }

        public EmbedObject setAuthor(String name, String url, String icon) {
            this.author = new Author(name, url, icon);
            return this;
        }

        public EmbedObject addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        private JsonObject toJson() {
            JsonObject json = new JsonObject();

            if (title != null) json.addProperty("title", title);
            if (description != null) json.addProperty("description", description);
            if (url != null) json.addProperty("url", url);
            if (color != null) json.addProperty("color", color);

            if (footer != null) {
                JsonObject footerJson = new JsonObject();
                footerJson.addProperty("text", footer.text);
                if (footer.iconUrl != null) footerJson.addProperty("icon_url", footer.iconUrl);
                json.add("footer", footerJson);
            }

            if (thumbnail != null) {
                JsonObject thumbnailJson = new JsonObject();
                thumbnailJson.addProperty("url", thumbnail.url);
                json.add("thumbnail", thumbnailJson);
            }

            if (image != null) {
                JsonObject imageJson = new JsonObject();
                imageJson.addProperty("url", image.url);
                json.add("image", imageJson);
            }

            if (author != null) {
                JsonObject authorJson = new JsonObject();
                authorJson.addProperty("name", author.name);
                if (author.url != null) authorJson.addProperty("url", author.url);
                if (author.iconUrl != null) authorJson.addProperty("icon_url", author.iconUrl);
                json.add("author", authorJson);
            }

            if (!fields.isEmpty()) {
                JsonArray fieldsArray = new JsonArray();
                for (Field field : fields) {
                    JsonObject fieldJson = new JsonObject();
                    fieldJson.addProperty("name", field.name);
                    fieldJson.addProperty("value", field.value);
                    fieldJson.addProperty("inline", field.inline);
                    fieldsArray.add(fieldJson);
                }
                json.add("fields", fieldsArray);
            }

            return json;
        }

        private record Footer(String text, String iconUrl) {}
        private record Thumbnail(String url) {}
        private record Image(String url) {}
        private record Author(String name, String url, String iconUrl) {}
        private record Field(String name, String value, boolean inline) {}
    }
}
