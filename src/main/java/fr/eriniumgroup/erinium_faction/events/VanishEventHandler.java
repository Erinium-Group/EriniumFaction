package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.features.vanish.VanishClientData;
import fr.eriniumgroup.erinium_faction.features.vanish.VanishManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Event handler pour gérer la synchronisation du vanish lors de la connexion/déconnexion
 */
public class VanishEventHandler {

    /**
     * Quand un joueur se connecte, synchroniser tous les joueurs vanish vers lui
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Synchroniser tous les joueurs en vanish vers le nouveau joueur
            VanishManager.syncAllToPlayer(serverPlayer);
        }
    }

    /**
     * Quand un joueur se déconnecte, retirer son état vanish du serveur
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Ne pas retirer du set si le joueur se déconnecte en vanish,
            // on veut qu'il reste en vanish à la reconnexion
            // VanishManager.removePlayer(serverPlayer.getUUID());
        }
    }

    /**
     * Nettoyer les données client quand on quitte un monde
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            VanishClientData.clear();
        }
    }
}
