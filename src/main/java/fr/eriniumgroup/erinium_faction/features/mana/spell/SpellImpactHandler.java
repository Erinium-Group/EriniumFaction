package fr.eriniumgroup.erinium_faction.features.mana.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

/** Applies custom spell damage/effects when our tagged projectiles hit. */
@EventBusSubscriber
public class SpellImpactHandler {
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Entity proj = event.getProjectile();
        if (!(proj instanceof SmallFireball)) return;
        if (!(proj.level() instanceof ServerLevel server)) return;
        var tag = proj.getPersistentData();
        if (!tag.contains(FireSpells.TAG_SPELL_TYPE)) return;
        String type = tag.getString(FireSpells.TAG_SPELL_TYPE);
        double dmg = tag.getDouble(FireSpells.TAG_SPELL_DAMAGE);
        int burn = tag.getInt(FireSpells.TAG_SPELL_BURN_SECONDS);

        switch (type) {
            case "fireball" -> {
                if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult ehr) {
                    if (ehr.getEntity() instanceof net.minecraft.world.entity.LivingEntity le) {
                        le.hurt(le.damageSources().onFire(), (float)dmg);
                        if (burn > 0) le.setRemainingFireTicks(burn * 20);
                    }
                }
                proj.discard();
                event.setCanceled(true); // prevent vanilla handling
            }
            case "meteor" -> {
                // Explosion at impact point; also apply direct damage if entity hit
                if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult ehr) {
                    if (ehr.getEntity() instanceof net.minecraft.world.entity.LivingEntity le) {
                        le.hurt(le.damageSources().onFire(), (float)dmg);
                        if (burn > 0) le.setRemainingFireTicks(burn * 20);
                    }
                    var pos = ehr.getEntity().position();
                    server.explode(null, pos.x, pos.y, pos.z, 6.0f, server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING), Level.ExplosionInteraction.BLOCK);
                } else if (event.getRayTraceResult() instanceof net.minecraft.world.phys.BlockHitResult bhr) {
                    var pos = bhr.getLocation();
                    server.explode(null, pos.x, pos.y, pos.z, 6.0f, server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING), Level.ExplosionInteraction.BLOCK);
                }
                proj.discard();
                event.setCanceled(true);
            }
            default -> {
                // Unknown tag type; ignore -> vanilla behavior
            }
        }
    }
}
