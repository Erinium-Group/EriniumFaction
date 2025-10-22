package fr.eriniumgroup.eriniumfaction.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EFUtil {
    /**
     * Utilitaires dédiés à la gestion des têtes de joueur (items et blocs) sous NeoForge 1.21.
     * Stratégie de résolution:
     * - Tente d'abord le cache de profils du serveur (GameProfile).
     * - Si nécessaire, résout les textures via ResolvableProfile.
     * Remarque: toute écriture (monde/inventaire) est rapatriée sur le thread serveur.
     */
    public static class Head {
        /**
         * Résout un profil joueur à partir de son pseudo.
         * <p>
         * - Utilise en priorité le cache de profils du serveur.
         * - Si absent/incomplet, déclenche une résolution asynchrone des textures.
         *
         * @param server serveur Minecraft (source du cache de profils)
         * @param name   pseudo du joueur
         * @return un CompletableFuture complété avec un ResolvableProfile (peut se compléter hors thread serveur)
         */
        public static CompletableFuture<ResolvableProfile> resolveProfileByName(MinecraftServer server, String name) {
            GameProfile cached = server.getProfileCache().get(name).orElse(null);
            ResolvableProfile rp = (cached != null) ? new ResolvableProfile(Optional.ofNullable(cached.getName()), Optional.ofNullable(cached.getId()), cached.getProperties()) : new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap());
            return rp.resolve();
        }

        /**
         * Résout un profil joueur à partir de son UUID.
         *
         * @param id UUID du joueur
         * @return un CompletableFuture complété avec un ResolvableProfile (peut se compléter hors thread serveur)
         */
        public static CompletableFuture<ResolvableProfile> resolveProfileByUUID(UUID id) {
            ResolvableProfile rp = new ResolvableProfile(Optional.empty(), Optional.of(id), new PropertyMap());
            return rp.resolve();
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
