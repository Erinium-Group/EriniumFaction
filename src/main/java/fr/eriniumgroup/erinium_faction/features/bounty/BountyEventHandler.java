package fr.eriniumgroup.erinium_faction.features.bounty;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Gère les événements liés au système de bounty
 */
@EventBusSubscriber(modid = "erinium_faction")
public class BountyEventHandler {

    private static int tickCounter = 0;
    private static final int CLEANUP_INTERVAL = 20 * 60 * 5; // 5 minutes

    /**
     * Appelé quand un joueur meurt
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (event.getSource().getEntity() == null) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        // Vérifier qu'on ne se tue pas soi-même
        if (victim.getUUID().equals(killer.getUUID())) return;

        // Vérifier s'il y a une bounty sur la victime
        BountyManager manager = BountyManager.get(victim.getServer());
        if (!manager.hasBounty(victim.getUUID())) return;

        // Réclamer la bounty
        double reward = manager.claimBounty(killer, victim.getUUID());

        if (reward > 0) {
            // Notifier le tueur
            killer.sendSystemMessage(Component.literal(""));
            killer.sendSystemMessage(Component.translatable("erinium_faction.bounty.claimed.killer.header"));
            killer.sendSystemMessage(Component.translatable("erinium_faction.bounty.claimed.killer.title"));
            killer.sendSystemMessage(Component.literal(""));
            killer.sendSystemMessage(Component.translatable("erinium_faction.bounty.claimed.killer.target", victim.getName().getString()));
            killer.sendSystemMessage(Component.translatable("erinium_faction.bounty.claimed.killer.reward", String.format("%.2f", reward)));
            killer.sendSystemMessage(Component.literal(""));

            // Bonus XP Hunter (si système de jobs disponible)
            double hunterBonus = BountyConfig.get().getHunterXpBonus();
            if (hunterBonus > 0) {
                killer.sendSystemMessage(Component.translatable("erinium_faction.bounty.claimed.killer.hunter_bonus", String.format("%.0f", hunterBonus)));
                // TODO: Ajouter l'XP au job Hunter quand le système sera intégré
            }

            killer.sendSystemMessage(Component.translatable("erinium_faction.bounty.claimed.killer.header"));
            killer.sendSystemMessage(Component.literal(""));

            // Notifier la victime
            victim.sendSystemMessage(Component.translatable("erinium_faction.bounty.claimed.victim", String.format("%.2f", reward), killer.getName().getString()));

            // Broadcast au serveur (message public)
            Component broadcastMessage = Component.translatable("erinium_faction.bounty.claimed.broadcast",
                    killer.getName().getString(),
                    String.format("%.2f", reward),
                    victim.getName().getString());

            for (ServerPlayer player : victim.getServer().getPlayerList().getPlayers()) {
                if (!player.getUUID().equals(killer.getUUID()) && !player.getUUID().equals(victim.getUUID())) {
                    player.sendSystemMessage(broadcastMessage);
                }
            }
        }
    }

    /**
     * Nettoyage périodique des bounties expirées
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;

        if (tickCounter >= CLEANUP_INTERVAL) {
            tickCounter = 0;

            // Nettoyer les bounties expirées
            var server = event.getServer();
            if (server != null) {
                BountyManager manager = BountyManager.get(server);
                manager.cleanupExpiredBounties();
            }
        }
    }
}
