package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.network.packets.SyncAllBannersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handler pour synchroniser les bannières au login
 */
@EventBusSubscriber
public class BannerSyncHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Envoyer toutes les bannières au joueur qui se connecte
            SyncAllBannersPacket packet = SyncAllBannersPacket.create();
            PacketDistributor.sendToPlayer(player, packet);
        }
    }
}
