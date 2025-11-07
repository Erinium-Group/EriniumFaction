package fr.eriniumgroup.erinium_faction.features.vanish;

import fr.eriniumgroup.erinium_faction.common.network.packets.VanishSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Gère l'état de vanish des joueurs.
 * Un joueur en vanish est complètement invisible: nameplate, tab list, model, armor, effects.
 */
public class VanishManager {

    // Set des UUIDs des joueurs en mode vanish
    private static final Set<UUID> vanishedPlayers = new HashSet<>();

    /**
     * Toggle le mode vanish pour un joueur
     * @param player Le joueur
     * @return true si le joueur est maintenant vanish, false sinon
     */
    public static boolean toggleVanish(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (vanishedPlayers.contains(uuid)) {
            // Désactiver vanish
            vanishedPlayers.remove(uuid);
            removeVanishEffects(player);
            syncToAll(player, false);
            return false;
        } else {
            // Activer vanish
            vanishedPlayers.add(uuid);
            applyVanishEffects(player);
            syncToAll(player, true);
            return true;
        }
    }

    /**
     * Vérifie si un joueur est en mode vanish
     */
    public static boolean isVanished(ServerPlayer player) {
        return vanishedPlayers.contains(player.getUUID());
    }

    /**
     * Vérifie si un UUID est en mode vanish
     */
    public static boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    /**
     * Définit l'état vanish d'un joueur (utilisé lors de la reconnexion)
     */
    public static void setVanished(UUID uuid, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(uuid);
        } else {
            vanishedPlayers.remove(uuid);
        }
    }

    /**
     * Retire un joueur du mode vanish (appelé à la déconnexion)
     */
    public static void removePlayer(UUID uuid) {
        vanishedPlayers.remove(uuid);
    }

    /**
     * Applique les effets visuels de vanish
     */
    private static void applyVanishEffects(ServerPlayer player) {
        // Invisibilité permanente (niveau 255 pour être sûr, durée infinie)
        MobEffectInstance invisibility = new MobEffectInstance(
                MobEffects.INVISIBILITY,
                Integer.MAX_VALUE,
                0,
                false,
                false,
                false
        );
        player.addEffect(invisibility);
    }

    /**
     * Retire les effets de vanish
     */
    private static void removeVanishEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.INVISIBILITY);
    }

    /**
     * Synchronise l'état vanish avec tous les clients
     */
    private static void syncToAll(ServerPlayer vanishedPlayer, boolean isVanished) {
        // Envoyer un packet à tous les joueurs en ligne pour mettre à jour l'état
        VanishSyncPacket packet = new VanishSyncPacket(vanishedPlayer.getUUID(), isVanished);
        for (ServerPlayer online : vanishedPlayer.getServer().getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(online, packet);
        }
    }

    /**
     * Synchronise l'état de tous les joueurs vanish vers un joueur qui vient de se connecter
     */
    public static void syncAllToPlayer(ServerPlayer newPlayer) {
        for (UUID vanishedUUID : vanishedPlayers) {
            VanishSyncPacket packet = new VanishSyncPacket(vanishedUUID, true);
            PacketDistributor.sendToPlayer(newPlayer, packet);
        }
    }
}
