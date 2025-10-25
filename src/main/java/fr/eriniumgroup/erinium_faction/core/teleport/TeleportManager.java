package fr.eriniumgroup.erinium_faction.core.teleport;

import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "erinium_faction")
public class TeleportManager {
    private static final Map<UUID, Long> lastTp = new ConcurrentHashMap<>();

    private static class PendingTp {
        UUID playerId;
        ResourceKey<Level> targetDim;
        double x, y, z;
        long endTimeMs;
        double startX, startY, startZ;
        ResourceKey<Level> startDim;
        boolean isHome;
        String warpName;
    }

    private static final Map<UUID, PendingTp> pending = new ConcurrentHashMap<>();

    public static boolean requestHomeTp(ServerPlayer sp, Faction f) {
        if (f == null || !f.hasHome()) return false;
        int[] home = f.getHome();
        ResourceLocation dimId = f.getHomeDim();
        return scheduleTp(sp, dimId, home[0] + 0.5, home[1] + 0.1, home[2] + 0.5, true, null);
    }

    public static boolean requestWarpTp(ServerPlayer sp, Faction f, String warpName) {
        if (f == null || warpName == null) return false;
        var w = f.getWarps().get(warpName.toLowerCase(java.util.Locale.ROOT));
        if (w == null) return false;
        return scheduleTp(sp, w.dim, w.x + 0.5, w.y + 0.1, w.z + 0.5, false, warpName);
    }

    private static boolean scheduleTp(ServerPlayer sp, ResourceLocation targetDimId, double x, double y, double z, boolean isHome, String warpNameOrNull) {
        long now = System.currentTimeMillis();
        int cooldown = EFConfig.FACTION_TP_COOLDOWN_SECONDS.get();
        Long last = lastTp.get(sp.getUUID());
        if (cooldown > 0 && last != null) {
            long remain = (last + cooldown * 1000L) - now;
            if (remain > 0) {
                sp.sendSystemMessage(Component.translatable("erinium_faction.tp.cooldown", Math.ceil(remain / 1000.0)));
                return false;
            }
        }
        int warmup = EFConfig.FACTION_TP_WARMUP_SECONDS.get();
        ResourceKey<Level> startDim = sp.level().dimension();
        ResourceKey<Level> targetDim = targetDimId != null ? ResourceKey.create(Registries.DIMENSION, targetDimId) : startDim;
        boolean allowCross = EFConfig.FACTION_TP_ALLOW_CROSS_DIM.get();
        if (!allowCross && startDim != targetDim) {
            sp.sendSystemMessage(Component.translatable("erinium_faction.tp.crossdim_denied"));
            return false;
        }
        if (warmup <= 0) {
            doTeleport(sp, targetDim, x, y, z);
            lastTp.put(sp.getUUID(), System.currentTimeMillis());
            if (isHome) {
                sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.home.tp.success"));
            } else {
                sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.warp.tp.success", warpNameOrNull == null ? "" : warpNameOrNull));
            }
            return true;
        }
        PendingTp p = new PendingTp();
        p.playerId = sp.getUUID();
        p.targetDim = targetDim;
        p.x = x; p.y = y; p.z = z;
        p.endTimeMs = now + warmup * 1000L;
        p.startX = sp.getX(); p.startY = sp.getY(); p.startZ = sp.getZ();
        p.startDim = startDim;
        p.isHome = isHome;
        p.warpName = warpNameOrNull;
        pending.put(sp.getUUID(), p);
        sp.sendSystemMessage(Component.translatable("erinium_faction.tp.warmup", warmup));
        return true;
    }

    private static void doTeleport(ServerPlayer sp, ResourceKey<Level> dim, double x, double y, double z) {
        ServerLevel lvl = sp.server.getLevel(dim);
        if (lvl == null) lvl = sp.serverLevel();
        sp.teleportTo(lvl, x, y, z, sp.getYRot(), sp.getXRot());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        if (pending.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (var it = pending.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            UUID id = entry.getKey();
            PendingTp p = entry.getValue();
            ServerPlayer sp = sp(id);
            if (sp == null) { it.remove(); continue; }
            // Cancel on move
            if (EFConfig.FACTION_TP_CANCEL_ON_MOVE.get()) {
                if (sp.level().dimension() != p.startDim) { cancel(sp, "erinium_faction.tp.canceled.move"); it.remove(); continue; }
                double dx = sp.getX() - p.startX;
                double dy = sp.getY() - p.startY;
                double dz = sp.getZ() - p.startZ;
                if ((dx*dx + dy*dy + dz*dz) > 0.01) { cancel(sp, "erinium_faction.tp.canceled.move"); it.remove(); continue; }
            }
            if (now >= p.endTimeMs) {
                doTeleport(sp, p.targetDim, p.x, p.y, p.z);
                lastTp.put(sp.getUUID(), System.currentTimeMillis());
                // Succès après warmup
                if (p.isHome) sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.home.tp.success"));
                else sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.warp.tp.success", p.warpName == null ? "" : p.warpName));
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        PendingTp p = pending.remove(sp.getUUID());
        if (p != null && EFConfig.FACTION_TP_CANCEL_ON_DAMAGE.get()) {
            cancel(sp, "erinium_faction.tp.canceled.damage");
        } else if (p != null) {
            pending.put(sp.getUUID(), p); // restore if not configured to cancel
        }
    }

    private static void cancel(ServerPlayer sp, String key) {
        sp.sendSystemMessage(Component.translatable(key));
    }

    private static ServerPlayer sp(UUID id) {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.getPlayerList().getPlayer(id);
    }
}
