package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.util.PlayerProtection;
import fr.eriniumgroup.erinium_faction.features.block_hp.BlockHpData;
import fr.eriniumgroup.erinium_faction.features.block_hp.BlockHpSyncMessage;
import fr.eriniumgroup.erinium_faction.procedures.PlayerProtectionOptimisedProcedure;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "erinium_faction", bus = EventBusSubscriber.Bus.GAME)
public final class BlockHpEventHandler {

    private static float strengthOf(Explosion ex) {
        float s = 4f; // fallback
        try {
            var f = Explosion.class.getDeclaredField("radius"); // mojang 1.18.2
            f.setAccessible(true);
            s = f.getFloat(ex);
        } catch (Throwable ignored) {
        }
        return s;
    }

    // Wither + EnderDragon : dégâts custom
    @SubscribeEvent
    public static void onMobBreak(LivingDestroyBlockEvent e) {
        if (!(e.getEntity().level() instanceof ServerLevel lvl)) return;
        if (!(e.getEntity() instanceof WitherBoss || e.getEntity() instanceof EnderDragon)) return;

        BlockPos pos = e.getPos();
        BlockState state = lvl.getBlockState(pos);

        e.setCanceled(true);
        int dmg = (e.getEntity() instanceof WitherBoss) ? 8 : 12;
        BlockHpData.damage(lvl, pos, dmg);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!sp.getMainHandItem().is(Items.STICK)) return;

        // Vérifier ce que le joueur regarde
        HitResult hit = sp.pick(5.0D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        ServerLevel lvl = sp.serverLevel();

        if (lvl.isEmptyBlock(pos)) return;

        BlockState state = lvl.getBlockState(pos);
        int base = BlockHpData.base(state);
        int cur = BlockHpData.current(lvl, pos, state);

        // Envoyer le packet au client pour synchroniser l'affichage
        PacketDistributor.sendToPlayer(sp, new BlockHpSyncMessage(pos, cur, base));

        if (e.getEntity().getServer().getTickCount() % 200 == 0) { // Toutes les 10 secondes
            PlayerProtection.cleanExpiredCache();
        }
    }

    // Inspection PV par clic droit avec un stick
    @SubscribeEvent
    public static void onInspect(PlayerInteractEvent.RightClickBlock e) {
        if (e.getLevel().isClientSide()) return;
        if (!e.getItemStack().is(Items.STICK)) return;
        //if (!e.getEntity().isCrouching()) return; // Seulement en shift

        ServerPlayer sp = (ServerPlayer) e.getEntity();
        ServerLevel lvl = (ServerLevel) e.getLevel();
        BlockPos pos = e.getPos();
        BlockState state = lvl.getBlockState(pos);

        int base = BlockHpData.base(state);
        int cur = BlockHpData.current(lvl, pos, state);
        float pct = base > 0 ? (cur * 100f / base) : 0f;

        // Après avoir calculé cur et base
        PacketDistributor.sendToPlayer(sp, new BlockHpSyncMessage(pos, cur, base));

        ChatFormatting col = pct >= 75f ? ChatFormatting.GREEN : pct >= 50f ? ChatFormatting.YELLOW : pct >= 25f ? ChatFormatting.GOLD : ChatFormatting.RED;

        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String idStr = id != null ? id.toString() : "minecraft:air";

        MutableComponent msg = Component.literal("Block HP: ").append(Component.literal(String.valueOf(cur))).append(Component.literal(" / ").withStyle(ChatFormatting.DARK_GRAY)).append(Component.literal(String.valueOf(base))).append(Component.literal(" (")).append(Component.literal(String.format("%.0f%%", pct)).withStyle(col)).append(Component.literal(") ")).append(Component.literal(idStr).withStyle(ChatFormatting.GRAY));

        sp.displayClientMessage(msg, true);
        e.setCanceled(true);
    }

    // Nettoyage quand le joueur place ou casse un bloc
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (e.getLevel() instanceof ServerLevel lvl) BlockHpData.get(lvl).clear(e.getPos());
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (e.getLevel() instanceof ServerLevel lvl) BlockHpData.get(lvl).clear(e.getPos());
    }

    @SubscribeEvent
    public static void onExplode(ExplosionEvent.Detonate e) {
        if (!(e.getLevel() instanceof ServerLevel lvl)) return;
        e.getAffectedBlocks().clear();

        Entity entity = null;
        if (e.getExplosion().getIndirectSourceEntity() != null) {
            entity = e.getExplosion().getIndirectSourceEntity();
        } else {
            e.getExplosion().getDirectSourceEntity();
        }

        // rayon & dégâts à partir du radius passé à level.explode(...)
        float s = strengthOf(e.getExplosion()); // ex: 4f si _level.explode(..., 4, ...)
        int R = Mth.clamp((int) Math.round(s * 1.5), 2, 16);
        int baseDmg = Mth.clamp((int) Math.round(s * 2.5), 1, 30);

        // minimums par source (optionnel)
        Entity src = e.getExplosion().getIndirectSourceEntity();
        if (src instanceof Creeper) {
            R = Math.max(R, 4);
            baseDmg = Math.max(baseDmg, 6);
        }
        if (src instanceof PrimedTnt || src instanceof MinecartTNT) {
            R = Math.max(R, 6);
            baseDmg = Math.max(baseDmg, 10);
        }

        // source dans l'eau => aucun dégât
        Vec3 c = e.getExplosion().center();
        BlockPos center = BlockPos.containing(c.x, c.y, c.z);
        if (lvl.getFluidState(center).is(FluidTags.WATER)) return;

        double r2 = R * R;
        for (int dx = -R; dx <= R; dx++)
            for (int dy = -R; dy <= R; dy++)
                for (int dz = -R; dz <= R; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r2) continue;
                    BlockPos p = center.offset(dx, dy, dz);
                    if (lvl.isEmptyBlock(p) || lvl.getBlockState(p).getBlock() == Blocks.BEDROCK) continue;

                    // bloc dans l’eau / waterlogged => protégé
                    if (lvl.getFluidState(p).is(FluidTags.WATER)) continue;
                    if (lvl.getBlockState(p).getFluidState().is(FluidTags.WATER)) continue;

                    boolean waterAdj = lvl.getFluidState(p.north()).is(FluidTags.WATER) || lvl.getFluidState(p.south()).is(FluidTags.WATER) || lvl.getFluidState(p.east()).is(FluidTags.WATER) || lvl.getFluidState(p.west()).is(FluidTags.WATER) || lvl.getFluidState(p.above()).is(FluidTags.WATER) || lvl.getFluidState(p.below()).is(FluidTags.WATER);

                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    int dmg = Math.max(1, baseDmg - (int) Math.floor(dist));
                    if (waterAdj) dmg = Math.max(1, dmg / 2);

                    BlockHpData.applyDamage(lvl, p, dmg, entity);
                }
    }
}