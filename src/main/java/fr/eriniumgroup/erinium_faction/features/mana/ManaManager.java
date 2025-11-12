package fr.eriniumgroup.erinium_faction.features.mana;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class ManaManager {
    private static int tickCounter = 0;

    public static PlayerManaData get(ServerPlayer sp) {
        return sp.getData(ManaAttachments.PLAYER_MANA);
    }

    private static void initDefaults(ServerPlayer sp) {
        PlayerManaData md = get(sp);
        double max = md.computeMaxMana(sp);
        if (md.getMana() <= 0) {
            md.setMana(max, max);
        } else {
            md.setMana(Math.min(md.getMana(), max), max);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || !(e.getOriginal() instanceof ServerPlayer orig)) return;
        PlayerManaData src = orig.getData(ManaAttachments.PLAYER_MANA);
        sp.setData(ManaAttachments.PLAYER_MANA, src); // copy over
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        initDefaults(sp);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        initDefaults(sp);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        PlayerManaData md = get(sp);
        md.markDamaged(sp.serverLevel().getGameTime());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        tickCounter++;
        int interval = Math.max(1, ManaConfig.TICK_REGEN_INTERVAL.get());
        if (tickCounter % interval != 0) return;
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            PlayerManaData md = get(sp);
            var level = sp.serverLevel();
            long now = level.getGameTime();
            double max = md.computeMaxMana(sp);
            double perSec = md.computeRegenPerSecond(sp, now);
            double toAdd = perSec * (interval / 20.0);
            double before = md.getMana();
            md.addMana(toAdd, max);
            if ((int) before != (int) md.getMana()) {
                // TODO: send client sync packet later
                // noop: changement de mana détecté
            }
        }
    }
}
