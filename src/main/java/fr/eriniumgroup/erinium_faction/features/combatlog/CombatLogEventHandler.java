package fr.eriniumgroup.erinium_faction.features.combatlog;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Gestionnaire d'événements pour le système de combat logging
 */
public class CombatLogEventHandler {

    /**
     * Détecte les dégâts PvP et tag les joueurs
     */
    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Pre event) {
        if (!CombatLogConfig.ENABLE_COMBAT_LOG.get()) return;

        // Vérifier si c'est un joueur qui prend des dégâts
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;

        DamageSource source = event.getSource();

        // Vérifier si c'est du PvP (attaquant est un joueur)
        if (source.getEntity() instanceof ServerPlayer attacker) {
            // Tag les deux joueurs en combat
            CombatLogManager.getInstance().tagPlayer(victim, attacker);
        }
        // Si c'est du PvE (mobs, etc.), ne pas tag
    }

    /**
     * Gère la déconnexion des joueurs
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!CombatLogConfig.ENABLE_COMBAT_LOG.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CombatLogManager.getInstance().handlePlayerDisconnect(player);
    }

    /**
     * Met à jour les timers et notifications
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!CombatLogConfig.ENABLE_COMBAT_LOG.get()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getServer() == null) return;

        // Mettre à jour toutes les 20 ticks (1 seconde)
        if (serverLevel.getGameTime() % 20 == 0) {
            CombatLogManager.getInstance().tick(serverLevel);
        }
    }

    /**
     * Bloque les commandes interdites pendant le combat
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        if (!CombatLogConfig.ENABLE_COMBAT_LOG.get()) return;

        // Vérifier si c'est un joueur
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player)) return;

        // Vérifier si le joueur est en combat
        if (!CombatLogManager.getInstance().isTagged(player.getUUID())) return;

        // Récupérer la commande
        String command = event.getParseResults().getReader().getString();

        // Vérifier si la commande est bloquée
        if (CombatLogManager.getInstance().isCommandBlocked(command)) {
            event.setCanceled(true);
            int remaining = CombatLogManager.getInstance().getRemainingSeconds(player.getUUID());
            player.sendSystemMessage(Component.literal("§c✖ Cette commande est bloquée en combat ! (" + remaining + "s restantes)")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
