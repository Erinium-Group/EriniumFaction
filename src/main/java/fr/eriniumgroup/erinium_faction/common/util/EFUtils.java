package fr.eriniumgroup.erinium_faction.common.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EFUtils {
    /**
     * Têtes de joueur (items/blocs), NeoForge 1.21.
     * Résolution: cache -> ResolvableProfile -> API Mojang (repli).
     * Écritures sur le thread serveur.
     */
    public static class Head {
        // Client HTTP (repli Mojang)
        private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        // Vrai si le profil possède une propriété "textures"
        private static boolean hasTextures(ResolvableProfile p) {
            if (p == null) return false;
            PropertyMap map = p.properties();
            return map != null && map.containsKey("textures") && !map.get("textures").isEmpty();
        }

        // UUID sans tirets -> UUID standard
        private static UUID undashedToUUID(String undashed) {
            String s = undashed.replaceAll("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})", "$1-$2-$3-$4-$5");
            return UUID.fromString(s);
        }

        // Repli Mojang: pseudo -> UUID -> textures
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

        // Repli Mojang: UUID -> textures
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
                    for (var el : props) {
                        JsonObject p = el.getAsJsonObject();
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
         * Résout un profil par pseudo (cache -> résolution -> repli Mojang).
         */
        public static CompletableFuture<ResolvableProfile> resolveProfileByName(MinecraftServer server, String name) {
            GameProfile cached = server.getProfileCache().get(name).orElse(null);
            ResolvableProfile rp = (cached != null) ? new ResolvableProfile(Optional.ofNullable(cached.getName()), Optional.ofNullable(cached.getId()), cached.getProperties()) : new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap());

            return rp.resolve().handle((res, ex) -> res != null ? res : rp).thenCompose(res -> hasTextures(res) ? CompletableFuture.completedFuture(res) : fetchFromMojangByName(name));
        }

        /**
         * Résout un profil par UUID (résolution -> repli Mojang).
         */
        public static CompletableFuture<ResolvableProfile> resolveProfileByUUID(UUID id) {
            ResolvableProfile rp = new ResolvableProfile(Optional.empty(), Optional.of(id), new PropertyMap());
            return rp.resolve().handle((res, ex) -> res != null ? res : rp).thenCompose(res -> hasTextures(res) ? CompletableFuture.completedFuture(res) : fetchFromMojangByUUID(id));
        }

        /**
         * Crée une tête depuis une texture base64 (signature optionnelle).
         * Sans présence du joueur.
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
         * Place une tête avec une texture (base64). Sans présence du joueur.
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
                // Repli si pas de serveur (ex: solo)
                applyOwner(level, pos, rp);
            }
        }

        /**
         * Crée une tête (ItemStack) depuis un pseudo.
         * Le PROFILE est appliqué plus tard (async, thread serveur).
         */
        public static ItemStack createHeadStackByName(MinecraftServer server, String name) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            resolveProfileByName(server, name).thenAccept(resolved -> server.execute(() -> head.set(DataComponents.PROFILE, resolved)));
            return head;
        }

        /**
         * Crée une tête (ItemStack) depuis un UUID.
         * Le PROFILE est appliqué plus tard (async, thread serveur).
         */
        public static ItemStack createHeadStackByUUID(MinecraftServer server, UUID id) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            resolveProfileByUUID(id).thenAccept(resolved -> server.execute(() -> head.set(DataComponents.PROFILE, resolved)));
            return head;
        }

        /**
         * Place une tête orientée, puis applique le profil résolu par pseudo (async).
         */
        public static void placeHeadByName(Level level, BlockPos pos, String name, int rotation0to15) {
            level.setBlockAndUpdate(pos, Blocks.PLAYER_HEAD.defaultBlockState().setValue(SkullBlock.ROTATION, rotation0to15));

            MinecraftServer server = level.getServer();
            SkullBlockEntity be = (SkullBlockEntity) level.getBlockEntity(pos);
            if (server == null || be == null) return;

            resolveProfileByName(server, name).thenAccept(resolved -> server.execute(() -> applyOwner(level, pos, resolved)));
        }

        /**
         * Place une tête orientée, puis applique le profil résolu par UUID (async).
         */
        public static void placeHeadByUUID(Level level, BlockPos pos, UUID id, int rotation0to15) {
            level.setBlockAndUpdate(pos, Blocks.PLAYER_HEAD.defaultBlockState().setValue(SkullBlock.ROTATION, rotation0to15));

            MinecraftServer server = level.getServer();
            SkullBlockEntity be = (SkullBlockEntity) level.getBlockEntity(pos);
            if (server == null || be == null) return;

            resolveProfileByUUID(id).thenAccept(resolved -> server.execute(() -> applyOwner(level, pos, resolved)));
        }

        /**
         * Applique le propriétaire à une tête placée et notifie le monde (thread serveur).
         */
        private static void applyOwner(Level level, BlockPos pos, ResolvableProfile profile) {
            SkullBlockEntity be2 = (SkullBlockEntity) level.getBlockEntity(pos);
            if (be2 == null) return;
            be2.setOwner(profile);
            be2.setChanged();
            level.sendBlockUpdated(pos, be2.getBlockState(), be2.getBlockState(), 3);
        }
    }

    public static class Item {
        /**
         * Sérialise un ItemStack en NBT (inclut tout).
         */
        public static String itemStackToString(ItemStack itemStack, Level level) {
            if (itemStack.isEmpty()) {
                return "minecraft:air";
            }

            // Sauvegarder l'ItemStack complet en NBT
            CompoundTag nbtTag = new CompoundTag();
            itemStack.save(level.registryAccess(), nbtTag);

            // Convertir le NBT en String
            return nbtTag.toString();
        }

        /**
         * Désérialise un ItemStack depuis une chaîne NBT (restaure tout).
         */
        public static void stringToItemStack(ItemStack targetItem, String nbtString, Level level) {
            try {
                if (nbtString == null || nbtString.isEmpty() || nbtString.equals("minecraft:air")) {
                    return;
                }

                // Parser le String NBT
                CompoundTag nbtTag = TagParser.parseTag(nbtString);

                // Recréer l'ItemStack depuis le NBT
                ItemStack parsed = ItemStack.parse(level.registryAccess(), nbtTag).orElse(ItemStack.EMPTY);

                if (!parsed.isEmpty()) {
                    // Appliquer tous les composants sur l'item target
                    targetItem.applyComponents(parsed.getComponents());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Représentation Base64 (stockage compact).
         */
        public static String itemStackToBase64(ItemStack itemStack, Level level) {
            String nbtString = itemStackToString(itemStack, level);
            return java.util.Base64.getEncoder().encodeToString(nbtString.getBytes());
        }

        /**
         * Reconstruit un ItemStack depuis une Base64.
         */
        public static void base64ToItemStack(ItemStack targetItem, String base64, Level level) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(base64);
                String nbtString = new String(decoded);
                stringToItemStack(targetItem, nbtString, level);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Infos détaillées d'un ItemStack (débogage).
         */
        public static String getItemInfo(ItemStack itemStack, Level level) {
            if (itemStack.isEmpty()) {
                return "ItemStack vide";
            }

            StringBuilder info = new StringBuilder();

            // Item de base
            info.append("=== INFOS DE L'OBJET ===\n");
            info.append("Objet: ").append(BuiltInRegistries.ITEM.getKey(itemStack.getItem())).append("\n");
            info.append("Quantité: ").append(itemStack.getCount()).append("\n");

            // Nom personnalisé
            if (itemStack.has(DataComponents.CUSTOM_NAME)) {
                Component customName = itemStack.get(DataComponents.CUSTOM_NAME);
                info.append("Nom personnalisé: ").append(customName.getString()).append("\n");
            }

            // Lore
            if (itemStack.has(DataComponents.LORE)) {
                info.append("Lore:\n");
                var lore = itemStack.get(DataComponents.LORE);
                for (Component line : lore.lines()) {
                    info.append("  - ").append(line.getString()).append("\n");
                }
            }

            // Enchantements
            if (itemStack.has(DataComponents.ENCHANTMENTS)) {
                ItemEnchantments enchantments = itemStack.get(DataComponents.ENCHANTMENTS);
                info.append("Enchantements:\n");
                enchantments.entrySet().forEach(entry -> {
                    info.append("  - ").append(entry.getKey().getRegisteredName()).append(" ").append(entry.getIntValue()).append("\n");
                });
            }

            // Durabilité
            if (itemStack.has(DataComponents.DAMAGE)) {
                int damage = itemStack.get(DataComponents.DAMAGE);
                int maxDamage = itemStack.getMaxDamage();
                info.append("Durabilité: ").append(maxDamage - damage).append("/").append(maxDamage).append("\n");
            }

            // Données personnalisées (NBT)
            if (itemStack.has(DataComponents.CUSTOM_DATA)) {
                CompoundTag customData = itemStack.get(DataComponents.CUSTOM_DATA).copyTag();
                info.append("Données NBT personnalisées:\n");
                info.append(customData.toString()).append("\n");
            }

            // NBT complet (pour débogage)
            info.append("\n=== NBT COMPLET ===\n");
            info.append(itemStackToString(itemStack, level));

            return info.toString();
        }

        /**
         * Copie tous les composants d'un ItemStack source vers un ItemStack cible.
         */
        public static ItemStack copyDataToItem(ItemStack source, ItemStack target) {
            if (source.isEmpty()) {
                return target;
            }

            // Créer le nouvel ItemStack avec l'item target
            ItemStack copy = new ItemStack(target.getItem(), source.getCount());

            // Copier TOUS les composants de source vers copy
            copy.applyComponents(source.getComponents());

            return copy;
        }
    }

    public static class Block {
        // TODO: Utilitaires de blocs (ajouter au besoin)
    }

    /**
     * Utilitaires de couleurs.
     */
    public static class Color {
        /**
         * Compose ARGB en entier 32 bits.
         */
        public static int ARGBToInt(int a, int r, int g, int b) {
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    public static class Faction {

        //Get
        public static Path getDataPath(MinecraftServer server) {
            return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("data")
                    .resolve("erinium_faction.json");
        }

        public static File FactionFileById(String name) {
            if (name == null) return new File("");
            String filename = "";
            File file = new File("");
            filename = name + ".json";
            file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "faction/factions"), File.separator + filename);
            return file;
        }

        public static File CurrentChunkFile(LevelAccessor world, double x, double z) {
            File file = new File("");
            String filename = "";
            String StringReturn = "";
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            filename = new Object() {
                private String getRegion(int chunkX, int chunkZ) {
                    ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
                    return new String("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ());
                }
            }.getRegion((int) x, (int) z) + "/" + new Object() {
                private String getChunk(int chunkX, int chunkZ) {
                    ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
                    return new String(chunkpos.getRegionLocalX() + "-" + chunkpos.getRegionLocalZ());
                }
            }.getChunk((int) x, (int) z) + ".json";
            file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "faction/chunks/"), File.separator + filename);
            return file;
        }

        public static String CurrentChunkFactionId(LevelAccessor world, double x, double z) {
            File file = new File("");
            String filename = "";
            String StringReturn = "";
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            filename = new Object() {
                private String getChunk(int chunkX, int chunkZ) {
                    ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
                    return new String(chunkpos.getRegionLocalX() + "-" + chunkpos.getRegionLocalZ());
                }
            }.getChunk((int) x, (int) z) + ".json";
            file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "faction/chunks/" + new Object() {
                private String getRegion(int chunkX, int chunkZ) {
                    ChunkPos chunkpos = new ChunkPos(new BlockPos(chunkX, 0, chunkZ));
                    return new String("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ());
                }
            }.getRegion((int) x, (int) z)), File.separator + filename);
            if (file.exists()) {
                {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        StringBuilder jsonstringbuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            jsonstringbuilder.append(line);
                        }
                        bufferedReader.close();
                        json = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                        StringReturn = json.get("id").getAsString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return StringReturn;
            }
            return "wilderness";
        }

        public static boolean AdminProtectionBlockPos(LevelAccessor world, double x, double z) {
            File file = new File("");
            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
            String tempId = "";
            String factionRank = "";
            boolean impossibleToInterract = false;
            boolean needcheckworldfile = false;
            tempId = CurrentChunkFactionId(world, x, z);
            if ((tempId).equals("safezone") || (tempId).equals("warzone")) {
                return false;
            } else if ((tempId).equals("wilderness")) {
                return true;
            } else {
                file = FactionFileById(tempId);
                {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        StringBuilder jsonstringbuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            jsonstringbuilder.append(line);
                        }
                        bufferedReader.close();
                        jsonObject = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                        if (jsonObject.get("isAdminFaction").getAsBoolean() || jsonObject.get("isWarzone").getAsBoolean() || jsonObject.get("isSafezone").getAsBoolean()) {
                            impossibleToInterract = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (impossibleToInterract) {
                    return false;
                }
            }
            return true;
        }

        public static String FactionIsOpened(Entity entity) {
            if (entity == null)
                return "";
            File file = new File("");
            com.google.gson.JsonObject JsonObject = new com.google.gson.JsonObject();
            String returner = "";
            file = FactionFileById(FactionManager.getPlayerFaction(entity.getUUID()));
            {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    StringBuilder jsonstringbuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        jsonstringbuilder.append(line);
                    }
                    bufferedReader.close();
                    JsonObject = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                    if (JsonObject.get("openFaction").getAsBoolean()) {
                        returner = "\u00A7aOpen";
                    } else {
                        returner = "\u00A74Close";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return returner;
        }

        public static void ToggleOpenFaction(Entity entity) {
            if (entity == null)
                return;
            File file = new File("");
            com.google.gson.JsonObject JsonObject = new com.google.gson.JsonObject();
            String returner = "";
            file = FactionFileById(FactionManager.getPlayerFaction(entity.getUUID()));
            {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    StringBuilder jsonstringbuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        jsonstringbuilder.append(line);
                    }
                    bufferedReader.close();
                    JsonObject = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                    JsonObject.addProperty("openFaction", (!JsonObject.get("openFaction").getAsBoolean()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // File
    public static class F {
        public static String GetFileStringValue(File fileobj, String object) {
            if (fileobj == null || object == null) return "";
            File file = new File("");
            com.google.gson.JsonObject Json = new com.google.gson.JsonObject();
            double number = 0;
            boolean booleanObj = false;
            String string = "";
            file = fileobj;
            {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    StringBuilder jsonstringbuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        jsonstringbuilder.append(line);
                    }
                    bufferedReader.close();
                    Json = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                    if (Json.has(object)) {
                        string = Json.get(object).getAsString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return string;
        }

        public static double GetFileNumberValue(File fileobj, String object) {
            if (fileobj == null || object == null) return 0;
            File file = new File("");
            com.google.gson.JsonObject Json = new com.google.gson.JsonObject();
            double number = 0;
            file = fileobj;
            {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    StringBuilder jsonstringbuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        jsonstringbuilder.append(line);
                    }
                    bufferedReader.close();
                    Json = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                    if (Json.has(object)) {
                        number = Json.get(object).getAsDouble();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return number;
        }

        public static File UUIDFile(String uuid) {
            if (uuid == null)
                return new File("");
            String filename = "";
            File file = new File("");
            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
            filename = uuid + ".json";
            file = new File((FMLPaths.GAMEDIR.get().toString() + "/" + "erinium_faction" + "/" + "players/"), File.separator + filename);
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                jsonObject = new Object() {
                    public com.google.gson.JsonObject parse(String rawJson) {
                        try {
                            return new com.google.gson.Gson().fromJson(rawJson, com.google.gson.JsonObject.class);
                        } catch (Exception e) {
                            EFC.log.error("§aUUID", "§cError: ", e);
                            return new com.google.gson.Gson().fromJson("{}", com.google.gson.JsonObject.class);
                        }
                    }
                }.parse("{}");
                {
                    com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                    try {
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(mainGSONBuilderVariable.toJson(jsonObject));
                        fileWriter.close();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
            }
            return file;
        }
    }

    /**
     * Non instanciable.
     */
    private EFUtils() {
    }
}
