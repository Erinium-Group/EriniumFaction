package fr.eriniumgroup.erinium_faction.features.mana.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

public class FireSpells {
    public static final String TAG_SPELL_TYPE = "ef_spell_type"; // fireball, meteor
    public static final String TAG_SPELL_DAMAGE = "ef_spell_damage"; // base damage
    public static final String TAG_SPELL_BURN_SECONDS = "ef_spell_burn"; // burn duration

    public static boolean cast(ServerLevel level, ServerPlayer caster, Spell spell) {
        String path = spell.id.getPath();
        if ("fire/fireball".equals(path)) return castFireball(level, caster, spell);
        if ("fire/fire_wall".equals(path)) return castFireWall(level, caster, spell);
        if ("fire/meteor".equals(path)) return castMeteor(level, caster, spell);
        if ("fire/firestorm".equals(path)) return castFireStorm(level, caster, spell);
        if ("fire/solar_apocalypse".equals(path)) return castSolarApocalypse(level, caster, spell);
        if ("fire/phoenix_incarnation".equals(path)) return castPhoenix(level, caster, spell);
        return false;
    }

    private static boolean castFireball(ServerLevel level, ServerPlayer caster, Spell spell) {
        // Tier 1: 20 dmg + burn 3s (5 dmg/s) => we store 20 direct + burn seconds. Damage multiplier if phoenix.
        Vec3 look = caster.getLookAngle().normalize();
        Vec3 spawn = caster.position().add(0, caster.getEyeHeight(), 0).add(look.scale(0.5));
        SmallFireball fb = EntityType.SMALL_FIREBALL.create(level);
        if (fb == null) return false;
        fb.setPos(spawn.x, spawn.y, spawn.z);
        fb.setOwner(caster);
        fb.setDeltaMovement(look.scale(0.6 + 0.1 * spell.tier));
        double dmg = 20.0 * SpellEffectsManager.damageMultiplier(caster);
        fb.getPersistentData().putString(TAG_SPELL_TYPE, "fireball");
        fb.getPersistentData().putDouble(TAG_SPELL_DAMAGE, dmg);
        fb.getPersistentData().putInt(TAG_SPELL_BURN_SECONDS, 3);
        return level.addFreshEntity(fb);
    }

    private static boolean castFireWall(ServerLevel level, ServerPlayer caster, Spell spell) {
        // 10s zone registered; damage logic handled in effects manager (15 dmg/s contact)
        Vec3 look = caster.getLookAngle().normalize();
        Vec3 front = caster.position().add(look.scale(3));
        SpellEffectsManager.addFireWall(level, BlockPos.containing(front), level.getGameTime() + 200, caster); // 10s
        return true;
    }

    private static boolean castMeteor(ServerLevel level, ServerPlayer caster, Spell spell) {
        // Tier 3: Meteor 150 dmg + explosion 10x10 -> damage applied on impact event + larger explosion.
        Vec3 target = caster.getEyePosition().add(caster.getLookAngle().scale(20));
        Vec3 spawn = target.add(0, 30, 0);
        SmallFireball fb = EntityType.SMALL_FIREBALL.create(level);
        if (fb == null) return false;
        fb.setPos(spawn.x, spawn.y, spawn.z);
        Vec3 dir = target.subtract(spawn).normalize();
        fb.setDeltaMovement(dir.scale(1.2));
        fb.setOwner(caster);
        double dmg = 150.0 * SpellEffectsManager.damageMultiplier(caster);
        fb.getPersistentData().putString(TAG_SPELL_TYPE, "meteor");
        fb.getPersistentData().putDouble(TAG_SPELL_DAMAGE, dmg);
        fb.getPersistentData().putInt(TAG_SPELL_BURN_SECONDS, 5); // longer burn for meteor
        level.addFreshEntity(fb);
        return true;
    }

    private static boolean castFireStorm(ServerLevel level, ServerPlayer caster, Spell spell) {
        // Tier 4: Fire storm 15s area damage handled in effects manager (total 300 dmg distributed)
        Vec3 center = caster.getEyePosition().add(caster.getLookAngle().scale(5));
        SpellEffectsManager.addFireStorm(level, BlockPos.containing(center), level.getGameTime() + 300, caster, 10); // 15s radius10 (20x20 square approx)
        return true;
    }

    private static boolean castSolarApocalypse(ServerLevel level, ServerPlayer caster, Spell spell) {
        // Tier 5: mini-sun explosive 1000 dmg in 30 block radius + strong explosion.
        Vec3 center = caster.getEyePosition().add(caster.getLookAngle().scale(10));
        double dmg = 1000.0 * SpellEffectsManager.damageMultiplier(caster);
        // Explosion (stronger than meteor) radius ~10
        level.explode(null, center.x, center.y, center.z, 10.0f, level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING), net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
        SpellEffectsManager.areaDamage(level, center, 30, dmg, 5); // burn 5s
        return true;
    }

    private static boolean castPhoenix(ServerLevel level, ServerPlayer caster, Spell spell) {
        // Ultimate Phoenix: transformation 60s handled in effects manager
        SpellEffectsManager.addPhoenix(caster, level.getGameTime() + 1200);
        return true;
    }
}
