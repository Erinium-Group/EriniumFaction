package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSavedData;
import fr.eriniumgroup.erinium_faction.core.power.PowerManager;
import fr.eriniumgroup.erinium_faction.core.power.PlayerPower;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class FactionEvents {
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter >= 1200) { // ~1 minute à 20 TPS
            tickCounter = 0;
            FactionManager.tickMinute();
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer dead)) return;
        // Perte de power pour la faction du joueur mort (valeur faction)
        Faction fDead = FactionManager.getFactionOf(dead.getUUID());
        if (fDead != null) {
            FactionManager.onMemberDeath(fDead, dead);
            if (dead.getServer() != null) FactionSavedData.get(dead.getServer()).setDirty();
        }
        // Gain d'XP pour la faction du killer si joueur
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            Faction fKiller = FactionManager.getFactionOf(killer.getUUID());
            if (fKiller != null) {
                int xp = EFConfig.XP_PER_KILL.get();
                if (xp > 0) {
                    fKiller.addXp(xp);
                    if (killer.getServer() != null) FactionSavedData.get(killer.getServer()).setDirty();
                }
                // Sync killer variables après modifs éventuelles
                FactionManager.populatePlayerVariables(killer, killer.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                killer.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(killer);
            }
        }
        // Sync variables du mort (son power a été diminué par PowerManager)
        FactionManager.populatePlayerVariables(dead, dead.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
        dead.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(dead);
    }
}
