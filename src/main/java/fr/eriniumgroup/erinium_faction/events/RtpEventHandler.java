package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.features.rtp.RtpManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Event handler pour le système RTP
 * Détecte les mouvements du joueur pendant le délai de téléportation
 */
public class RtpEventHandler {

    /**
     * Vérifie si le joueur bouge pendant le délai de téléportation
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Vérifier tous les joueurs en ligne
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            RtpManager.checkPlayerMovement(player);
        }
    }
}
