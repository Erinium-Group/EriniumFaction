package fr.eriniumgroup.erinium_faction.protection;

import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * Handles PvP protection rules based on faction relations
 */
public class PvpProtection {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PvpProtection::onPlayerAttack);
    }

    private static void onPlayerAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof Player attacker)) return;
        if (!(event.getTarget() instanceof Player victim)) return;
        if (attacker.level().isClientSide()) return;

        if (FactionManager.areSameFaction(attacker.getUUID(), victim.getUUID())) {
            if (!EFConfig.friendlyFire) {
                event.setCanceled(true);
                attacker.sendSystemMessage(Component.literal("§cVous ne pouvez pas attaquer un membre de votre faction !"));
                return;
            }
        }

        String attackerFaction = FactionManager.getPlayerFaction(attacker.getUUID());
        String victimFaction = FactionManager.getPlayerFaction(victim.getUUID());

        if (attackerFaction != null && victimFaction != null) {
            if (FactionManager.areAllies(attackerFaction, victimFaction)) {
                if (!EFConfig.allyDamage) {
                    event.setCanceled(true);
                    attacker.sendSystemMessage(Component.literal("§cVous ne pouvez pas attaquer un allié !"));
                }
            }
        }
    }
}

