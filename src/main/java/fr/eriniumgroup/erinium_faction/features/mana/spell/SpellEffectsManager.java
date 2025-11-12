package fr.eriniumgroup.erinium_faction.features.mana.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

/** Manages ongoing area / transformation spell effects. */
@EventBusSubscriber
public class SpellEffectsManager {
    private static final class FireWallEffect { // 10s
        final ServerLevel level; final BlockPos origin; final long endTick; final ServerPlayer caster;
        FireWallEffect(ServerLevel lvl, BlockPos pos, long end, ServerPlayer caster){this.level=lvl;this.origin=pos;this.endTick=end;this.caster=caster;}
    }
    private static final class FireStormEffect { // 15s
        final ServerLevel level; final BlockPos center; final long endTick; final ServerPlayer caster; final int radius;
        FireStormEffect(ServerLevel lvl, BlockPos c, long end, ServerPlayer caster, int radius){this.level=lvl;this.center=c;this.endTick=end;this.caster=caster;this.radius=radius;}
    }
    private static final class PhoenixEffect { // 60s
        final ServerPlayer caster; final long endTick; final double originalHealth; final boolean originalMayFly;
        PhoenixEffect(ServerPlayer c, long end){this.caster=c;this.endTick=end;this.originalHealth=c.getHealth();this.originalMayFly=c.getAbilities().mayfly;}
    }

    private static final List<FireWallEffect> FIRE_WALLS = new ArrayList<>();
    private static final List<FireStormEffect> FIRE_STORMS = new ArrayList<>();
    private static final List<PhoenixEffect> PHOENIXES = new ArrayList<>();

    // --- Public query helpers ---
    public static boolean isPhoenixActive(ServerPlayer sp) {
        long now = sp.serverLevel().getGameTime();
        for (PhoenixEffect p : PHOENIXES) if (p.caster == sp && p.endTick > now) return true;
        return false;
    }
    public static int getPhoenixRemainingSeconds(ServerPlayer sp) {
        long now = sp.serverLevel().getGameTime();
        for (PhoenixEffect p : PHOENIXES) if (p.caster == sp && p.endTick > now) return (int)((p.endTick - now)/20);
        return 0;
    }

    public static double damageMultiplier(ServerPlayer caster) {
        // If phoenix active -> x3 damage for fire spells
        long now = caster.serverLevel().getGameTime();
        for (PhoenixEffect p : PHOENIXES) if (p.caster == caster && p.endTick > now) return 3.0;
        return 1.0;
    }

    public static void areaDamage(ServerLevel level, Vec3 center, double radius, double damage, int burnSeconds) {
        AABB box = new AABB(center.x-radius, center.y-radius, center.z-radius, center.x+radius, center.y+radius, center.z+radius);
        var targets = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box, le -> true);
        for (var le : targets) {
            le.hurt(le.damageSources().onFire(), (float)damage);
            if (burnSeconds > 0) le.setRemainingFireTicks(burnSeconds * 20);
        }
    }

    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        // Fire walls
        FIRE_WALLS.removeIf(w -> {
            if (w.endTick <= now) return true;
            AABB box = new AABB(w.origin).inflate(2,1.5,0.5);
            if (now % 20 == 0) {
                var entities = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box, le -> le != w.caster);
                for (var le : entities) {
                    le.hurt(le.damageSources().onFire(), 15f);
                    le.setRemainingFireTicks(3 * 20);
                }
            }
            return false;
        });
        // Fire storms
        FIRE_STORMS.removeIf(s -> {
            if (s.endTick <= now) return true;
            if (now % 20 == 0) {
                AABB area = new AABB(s.center).inflate(s.radius);
                var targets = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, area, le -> le != s.caster);
                for (var le : targets) {
                    le.hurt(le.damageSources().onFire(), 20f * (float)damageMultiplier(s.caster));
                    le.setRemainingFireTicks(2 * 20);
                }
            }
            return false;
        });
        // Phoenix active maintenance
        PHOENIXES.removeIf(p -> {
            boolean expired = p.endTick <= now || p.caster.isRemoved();
            if (!expired) {
                // Grant temporary flight & fire immunity
                if (!p.caster.getAbilities().mayfly) {
                    p.caster.getAbilities().mayfly = true;
                    p.caster.onUpdateAbilities();
                }
                // Clear fire periodically
                if (p.caster.getRemainingFireTicks() > 0) p.caster.setRemainingFireTicks(0);
            } else {
                // Restore original mayfly if effect ends
                if (!p.originalMayFly) {
                    p.caster.getAbilities().mayfly = false;
                    if (p.caster.getAbilities().flying) p.caster.getAbilities().flying = false;
                    p.caster.onUpdateAbilities();
                }
            }
            return expired;
        });
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (ServerLevel lvl : server.getAllLevels()) tick(lvl);
    }

    public static void addFireWall(ServerLevel level, BlockPos origin, long endTick, ServerPlayer caster){ FIRE_WALLS.add(new FireWallEffect(level, origin, endTick, caster)); }
    public static void addFireStorm(ServerLevel level, BlockPos center, long endTick, ServerPlayer caster, int radius){ FIRE_STORMS.add(new FireStormEffect(level, center, endTick, caster, radius)); }
    public static void addPhoenix(ServerPlayer caster, long endTick){
        // Remove existing phoenix effect if already active
        PHOENIXES.removeIf(p -> p.caster == caster);
        PHOENIXES.add(new PhoenixEffect(caster, endTick));
        // Immediate grant of flight
        if (!caster.getAbilities().mayfly) {
            caster.getAbilities().mayfly = true;
            caster.onUpdateAbilities();
        }
    }
}
