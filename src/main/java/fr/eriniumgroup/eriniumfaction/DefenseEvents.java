/**
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside fr.eriniumgroup.eriniumfaction as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
*/
package fr.eriniumgroup.eriniumfaction;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "erinium_faction", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DefenseEvents {

	private static float strengthOf(Explosion ex) {
		float s = 4f; // fallback
		try {
			var f = Explosion.class.getDeclaredField("radius"); // mojang 1.18.2
			f.setAccessible(true);
			s = f.getFloat(ex);
		} catch (Throwable ignored) {}
		return s;
	}

	// Wither + EnderDragon : dégâts custom
	@SubscribeEvent
	public static void onMobBreak(LivingDestroyBlockEvent e){
		if (!(e.getEntity().level instanceof ServerLevel lvl)) return;
		if (!(e.getEntity() instanceof WitherBoss || e.getEntity() instanceof EnderDragon)) return;

		BlockPos pos = e.getPos();
		BlockState state = lvl.getBlockState(pos);

		e.setCanceled(true);
		int dmg = (e.getEntity() instanceof WitherBoss) ? 8 : 12;
		BlockHpData.damage(lvl, pos, dmg);
	}

	@SubscribeEvent
	public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent e) {
		if (e.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
		if (!(e.player instanceof ServerPlayer sp)) return;
		if (!sp.getMainHandItem().is(Items.STICK)) return;

		// Vérifier ce que le joueur regarde
		net.minecraft.world.phys.HitResult hit = sp.pick(5.0D, 0.0F, false);
		if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return;

		net.minecraft.world.phys.BlockHitResult blockHit = (net.minecraft.world.phys.BlockHitResult) hit;
		BlockPos pos = blockHit.getBlockPos();
		ServerLevel lvl = sp.getLevel();

		if (lvl.isEmptyBlock(pos)) return;

		BlockState state = lvl.getBlockState(pos);
		int base = BlockHpData.base(state);
		int cur = BlockHpData.current(lvl, pos, state);

		// Envoyer le packet au client pour synchroniser l'affichage
		EriniumFactionMod.PACKET_HANDLER.send(
				net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
				new BlockHpSyncMessage(pos, cur, base)
		);
	}

	// Inspection PV par clic droit avec un stick
	@SubscribeEvent
	public static void onInspect(PlayerInteractEvent.RightClickBlock e){
		if (e.getWorld().isClientSide()) return;
		if (!e.getItemStack().is(Items.STICK)) return;
		if (!e.getEntity().isCrouching()) return; // Seulement en shift

		ServerPlayer sp = (ServerPlayer) e.getEntity();
		ServerLevel lvl = (ServerLevel) e.getWorld();
		BlockPos pos = e.getPos();
		BlockState state = lvl.getBlockState(pos);

		int base = BlockHpData.base(state);
		int cur  = BlockHpData.current(lvl, pos, state);
		float pct = base > 0 ? (cur * 100f / base) : 0f;

		// Après avoir calculé cur et base
		EriniumFactionMod.PACKET_HANDLER.send(
				net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
				new BlockHpSyncMessage(pos, cur, base)
		);

		ChatFormatting col =
				pct >= 75f ? ChatFormatting.GREEN :
						pct >= 50f ? ChatFormatting.YELLOW :
								pct >= 25f ? ChatFormatting.GOLD  :
										ChatFormatting.RED;

		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
		String idStr = id != null ? id.toString() : "minecraft:air";

		MutableComponent msg = new TextComponent("Block HP: ")
				.append(new TextComponent(String.valueOf(cur)))
				.append(new TextComponent(" / ").withStyle(ChatFormatting.DARK_GRAY))
				.append(new TextComponent(String.valueOf(base)))
				.append(new TextComponent(" ("))
				.append(new TextComponent(String.format("%.0f%%", pct)).withStyle(col))
				.append(new TextComponent(") "))
				.append(new TextComponent(idStr).withStyle(ChatFormatting.GRAY));

		sp.displayClientMessage(msg, true);
		e.setCanceled(true);
	}

	// Nettoyage quand le joueur place ou casse un bloc
	@SubscribeEvent
	public static void onPlace(net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent e){
		if (e.getWorld() instanceof ServerLevel lvl) BlockHpData.get(lvl).clear(e.getPos());
	}
	@SubscribeEvent
	public static void onBreak(net.minecraftforge.event.world.BlockEvent.BreakEvent e){
		if (e.getWorld() instanceof ServerLevel lvl) BlockHpData.get(lvl).clear(e.getPos());
	}

	@SubscribeEvent
	public static void onExplode(ExplosionEvent.Detonate e) {
		if (!(e.getWorld() instanceof ServerLevel lvl)) return;
		e.getAffectedBlocks().clear();

		// rayon & dégâts à partir du radius passé à level.explode(...)
		float s = strengthOf(e.getExplosion()); // ex: 4f si _level.explode(..., 4, ...)
		int R = Mth.clamp((int)Math.round(s * 1.5), 2, 16);
		int baseDmg = Mth.clamp((int)Math.round(s * 2.5), 1, 30);

		// minimums par source (optionnel)
		Entity src = e.getExplosion().getExploder();
		if (src instanceof Creeper)           { R = Math.max(R, 4);  baseDmg = Math.max(baseDmg, 6);  }
		if (src instanceof PrimedTnt
				|| src instanceof MinecartTNT)       { R = Math.max(R, 6);  baseDmg = Math.max(baseDmg, 10); }

		// source dans l’eau => aucun dégât
		Vec3 c = e.getExplosion().getPosition();
		BlockPos center = new BlockPos(Mth.floor(c.x), Mth.floor(c.y), Mth.floor(c.z));
		if (lvl.getFluidState(center).is(FluidTags.WATER)) return;

		double r2 = R * R;
		for (int dx=-R; dx<=R; dx++) for (int dy=-R; dy<=R; dy++) for (int dz=-R; dz<=R; dz++) {
			if (dx*dx + dy*dy + dz*dz > r2) continue;
			BlockPos p = center.offset(dx, dy, dz);
			if (lvl.isEmptyBlock(p) || lvl.getBlockState(p).getBlock() == Blocks.BEDROCK) continue;

			// bloc dans l’eau / waterlogged => protégé
			if (lvl.getFluidState(p).is(FluidTags.WATER)) continue;
			if (lvl.getBlockState(p).getFluidState().is(FluidTags.WATER)) continue;

			boolean waterAdj =
					lvl.getFluidState(p.north()).is(FluidTags.WATER) ||
							lvl.getFluidState(p.south()).is(FluidTags.WATER) ||
							lvl.getFluidState(p.east()).is(FluidTags.WATER)  ||
							lvl.getFluidState(p.west()).is(FluidTags.WATER)  ||
							lvl.getFluidState(p.above()).is(FluidTags.WATER) ||
							lvl.getFluidState(p.below()).is(FluidTags.WATER);

			double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
			int dmg = Math.max(1, baseDmg - (int)Math.floor(dist));
			if (waterAdj) dmg = Math.max(1, dmg / 2);

			BlockHpData.applyDamage(lvl, p, dmg);
		}
	}
}