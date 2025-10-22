package fr.eriniumgroup.eriniumfaction.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class EFUtil {
    /**
     * Utilitaires dédiés à la gestion des têtes de joueur (items et blocs) sous NeoForge 1.21.
     * Stratégie de résolution:
     * - Tente d'abord le cache de profils du serveur (GameProfile).
     * - Si nécessaire, résout les textures via ResolvableProfile.
     * Remarque: toute écriture (monde/inventaire) est rapatriée sur le thread serveur.
     */
    public static class Head {
        // Client HTTP pour les fallbacks Mojang
        private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        // Détermine si un ResolvableProfile possède déjà une propriété "textures"
        private static boolean hasTextures(ResolvableProfile p) {
            if (p == null) return false;
            PropertyMap map = p.properties();
            return map != null && map.containsKey("textures") && !map.get("textures").isEmpty();
        }

        // Utility: UUID "undashed" -> UUID standard
        private static UUID undashedToUUID(String undashed) {
            String s = undashed.replaceAll("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})", "$1-$2-$3-$4-$5");
            return UUID.fromString(s);
        }

        // Fallback Mojang: username -> UUID -> textures
        private static CompletableFuture<ResolvableProfile> fetchFromMojangByName(String name) {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name)).timeout(Duration.ofSeconds(5)).GET().build();

            return HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenCompose(resp -> {
                if (resp.statusCode() != 200 || resp.body() == null || resp.body().isBlank()) {
                    return CompletableFuture.completedFuture(new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap()));
                }
                JsonObject o = JsonParser.parseString(resp.body()).getAsJsonObject();
                String rawId = o.has("id") ? o.get("id").getAsString() : null;
                String resolvedName = o.has("name") ? o.get("name").getAsString() : name;
                if (rawId == null || rawId.isBlank()) {
                    return CompletableFuture.completedFuture(new ResolvableProfile(Optional.of(resolvedName), Optional.empty(), new PropertyMap()));
                }
                UUID uuid = undashedToUUID(rawId);
                return fetchFromMojangByUUID(uuid).thenApply(rp -> new ResolvableProfile(Optional.of(resolvedName), Optional.of(uuid), rp.properties()));
            }).exceptionally(ex -> new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap()));
        }

        // Fallback Mojang: UUID -> textures
        private static CompletableFuture<ResolvableProfile> fetchFromMojangByUUID(UUID id) {
            String undashed = id.toString().replace("-", "");
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + undashed + "?unsigned=false")).timeout(Duration.ofSeconds(5)).GET().build();

            return HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(resp -> {
                if (resp.statusCode() != 200 || resp.body() == null || resp.body().isBlank()) {
                    return new ResolvableProfile(Optional.empty(), Optional.of(id), new PropertyMap());
                }
                JsonObject o = JsonParser.parseString(resp.body()).getAsJsonObject();
                String name = o.has("name") ? o.get("name").getAsString() : null;

                PropertyMap pm = new PropertyMap();
                if (o.has("properties")) {
                    JsonArray props = o.getAsJsonArray("properties");
                    for (int i = 0; i < props.size(); i++) {
                        JsonObject p = props.get(i).getAsJsonObject();
                        if ("textures".equals(p.get("name").getAsString())) {
                            String value = p.get("value").getAsString();
                            String sig = p.has("signature") ? p.get("signature").getAsString() : null;
                            if (sig != null && !sig.isBlank()) {
                                pm.put("textures", new Property("textures", value, sig));
                            } else {
                                pm.put("textures", new Property("textures", value));
                            }
                            break;
                        }
                    }
                }
                return new ResolvableProfile(Optional.ofNullable(name), Optional.of(id), pm);
            }).exceptionally(ex -> new ResolvableProfile(Optional.empty(), Optional.of(id), new PropertyMap()));
        }

        /**
         * Résout un profil joueur à partir de son pseudo.
         * <p>
         * - Essaie le cache + résolution standard.
         * - Si aucune texture n’est trouvée, bascule sur l’API Mojang (offline friendly).
         */
        public static CompletableFuture<ResolvableProfile> resolveProfileByName(MinecraftServer server, String name) {
            GameProfile cached = server.getProfileCache().get(name).orElse(null);
            ResolvableProfile rp = (cached != null) ? new ResolvableProfile(Optional.ofNullable(cached.getName()), Optional.ofNullable(cached.getId()), cached.getProperties()) : new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap());

            return rp.resolve().handle((res, ex) -> res != null ? res : rp).thenCompose(res -> hasTextures(res) ? CompletableFuture.completedFuture(res) : fetchFromMojangByName(name));
        }

        /**
         * Résout un profil joueur à partir de son UUID.
         * <p>
         * - Essaie la résolution standard.
         * - Si aucune texture n’est trouvée, bascule sur l’API Mojang (offline friendly).
         */
        public static CompletableFuture<ResolvableProfile> resolveProfileByUUID(UUID id) {
            ResolvableProfile rp = new ResolvableProfile(Optional.empty(), Optional.of(id), new PropertyMap());
            return rp.resolve().handle((res, ex) -> res != null ? res : rp).thenCompose(res -> hasTextures(res) ? CompletableFuture.completedFuture(res) : fetchFromMojangByUUID(id));
        }

        /**
         * Crée une tête joueur depuis une valeur de texture (base64) et optionnellement sa signature.
         * Ne nécessite pas que le joueur soit en ligne.
         */
        public static ItemStack createHeadStackFromTexture(String textureBase64, String signatureOrNull) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            PropertyMap pm = new PropertyMap();
            if (signatureOrNull != null && !signatureOrNull.isBlank()) {
                pm.put("textures", new Property("textures", textureBase64, signatureOrNull));
            } else {
                pm.put("textures", new Property("textures", textureBase64));
            }
            ResolvableProfile rp = new ResolvableProfile(Optional.empty(), Optional.empty(), pm);
            head.set(DataComponents.PROFILE, rp);
            return head;
        }

        /**
         * Place un bloc tête en utilisant directement une texture (base64). Ne dépend pas de la présence du joueur.
         */
        public static void placeHeadWithTexture(Level level, BlockPos pos, String textureBase64, String signatureOrNull, int rotation0to15) {
            level.setBlockAndUpdate(pos, Blocks.PLAYER_HEAD.defaultBlockState().setValue(SkullBlock.ROTATION, rotation0to15));
            ResolvableProfile rp = new ResolvableProfile(Optional.empty(), Optional.empty(), new PropertyMap());
            if (signatureOrNull != null && !signatureOrNull.isBlank()) {
                rp.properties().put("textures", new Property("textures", textureBase64, signatureOrNull));
            } else {
                rp.properties().put("textures", new Property("textures", textureBase64));
            }
            MinecraftServer server = level.getServer();
            if (server != null) {
                server.execute(() -> applyOwner(level, pos, rp));
            } else {
                // Fallback si pas de serveur accessible (ex: client singleplayer)
                applyOwner(level, pos, rp);
            }
        }

        /**
         * Crée un ItemStack de tête de joueur à partir d'un pseudo.
         * <p>
         * Détails:
         * - L'ItemStack (PLAYER_HEAD) est retourné immédiatement.
         * - Le composant DataComponents.PROFILE est appliqué plus tard sur le thread serveur,
         * une fois le profil résolu (opération asynchrone).
         *
         * @param server serveur Minecraft utilisé pour la résolution et l'application sur le thread serveur
         * @param name   pseudo du joueur
         * @return l'ItemStack de tête (textures appliquées ultérieurement)
         */
        public static ItemStack createHeadStackByName(MinecraftServer server, String name) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            resolveProfileByName(server, name).thenAccept(resolved -> server.execute(() -> head.set(DataComponents.PROFILE, resolved)));
            return head;
        }

        /**
         * Crée un ItemStack de tête de joueur à partir d'un UUID.
         * <p>
         * Détails:
         * - L'ItemStack (PLAYER_HEAD) est retourné immédiatement.
         * - Le composant DataComponents.PROFILE est appliqué plus tard sur le thread serveur,
         * une fois le profil résolu (opération asynchrone).
         *
         * @param server serveur Minecraft utilisé pour la résolution et l'application sur le thread serveur
         * @param id     UUID du joueur
         * @return l'ItemStack de tête (textures appliquées ultérieurement)
         */
        public static ItemStack createHeadStackByUUID(MinecraftServer server, UUID id) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            resolveProfileByUUID(id).thenAccept(resolved -> server.execute(() -> head.set(DataComponents.PROFILE, resolved)));
            return head;
        }

        /**
         * Place un bloc tête de joueur orienté puis applique le profil résolu à partir d'un pseudo.
         * <p>
         * Contrainte:
         * - Le bloc est placé immédiatement.
         * - L'application du propriétaire (textures) se fait ensuite sur le thread serveur
         * après résolution asynchrone du profil.
         *
         * @param level         monde cible
         * @param pos           position du bloc
         * @param name          pseudo du joueur
         * @param rotation0to15 rotation dans la plage 0..15 (multiples de 22,5°)
         */
        public static void placeHeadByName(Level level, BlockPos pos, String name, int rotation0to15) {
            level.setBlockAndUpdate(pos, Blocks.PLAYER_HEAD.defaultBlockState().setValue(SkullBlock.ROTATION, rotation0to15));

            MinecraftServer server = level.getServer();
            SkullBlockEntity be = (SkullBlockEntity) level.getBlockEntity(pos);
            if (server == null || be == null) return;

            resolveProfileByName(server, name).thenAccept(resolved -> server.execute(() -> applyOwner(level, pos, resolved)));
        }

        /**
         * Place un bloc tête de joueur orienté puis applique le profil résolu à partir d'un UUID.
         * <p>
         * Contrainte:
         * - Le bloc est placé immédiatement.
         * - L'application du propriétaire (textures) se fait ensuite sur le thread serveur
         * après résolution asynchrone du profil.
         *
         * @param level         monde cible
         * @param pos           position du bloc
         * @param id            UUID du joueur
         * @param rotation0to15 rotation dans la plage 0..15 (multiples de 22,5°)
         */
        public static void placeHeadByUUID(Level level, BlockPos pos, UUID id, int rotation0to15) {
            level.setBlockAndUpdate(pos, Blocks.PLAYER_HEAD.defaultBlockState().setValue(SkullBlock.ROTATION, rotation0to15));

            MinecraftServer server = level.getServer();
            SkullBlockEntity be = (SkullBlockEntity) level.getBlockEntity(pos);
            if (server == null || be == null) return;

            resolveProfileByUUID(id).thenAccept(resolved -> server.execute(() -> applyOwner(level, pos, resolved)));
        }

        /**
         * Applique le propriétaire à une tête déjà placée et notifie le monde.
         * Doit être appelé depuis le thread serveur.
         *
         * @param level   monde cible
         * @param pos     position de la tête
         * @param profile profil résolu (avec propriétés/Textures)
         */
        private static void applyOwner(Level level, BlockPos pos, ResolvableProfile profile) {
            SkullBlockEntity be2 = (SkullBlockEntity) level.getBlockEntity(pos);
            if (be2 == null) return;
            be2.setOwner(profile);
            be2.setChanged();
            level.sendBlockUpdated(pos, be2.getBlockState(), be2.getBlockState(), 3);
        }
    }

    /**
     * Classe utilitaire non instanciable.
     */
    private EFUtil() {
    }
}
