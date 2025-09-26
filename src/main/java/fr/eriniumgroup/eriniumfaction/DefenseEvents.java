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

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.util.Mth;
import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "erinium_faction", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DefenseEvents {

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

	// Inspection PV par clic droit avec un stick
	@SubscribeEvent
	public static void onInspect(PlayerInteractEvent.RightClickBlock e){
		if (e.getWorld().isClientSide()) return;
		if (!e.getItemStack().is(Items.STICK)) return; // change si tu veux un autre item

		ServerLevel lvl = (ServerLevel)e.getWorld();
		BlockPos pos = e.getPos();
		BlockState state = lvl.getBlockState(pos);

		int base = BlockHpData.base(state);
		int cur  = BlockHpData.current(lvl, pos, state);

		e.getEntity().sendMessage(Component.nullToEmpty("Block HP: " + cur + " / " + base + " (" + state.getBlock().getRegistryName() + ")"), e.getEntity().getUUID());
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
	public static void onExplode(net.minecraftforge.event.world.ExplosionEvent.Detonate e) {
		if (!(e.getWorld() instanceof ServerLevel lvl)) return;

		// Empêche la casse vanilla (optionnel si tu veux tout passer par tes PV)
		e.getAffectedBlocks().clear();

		// Source -> rayon et dégâts de base
		Entity src = e.getExplosion().getExploder();
		int baseDmg, R;
		if (src instanceof Creeper)            { baseDmg = 6;  R = 4; }
		else if (src instanceof PrimedTnt
				|| src instanceof MinecartTNT)   { baseDmg = 10; R = 6; }
		else                                   { baseDmg = 8;  R = 5; }

		// Centre de l’explosion
		Vec3 c = e.getExplosion().getPosition();
		BlockPos center = new BlockPos(Mth.floor(c.x), Mth.floor(c.y), Mth.floor(c.z));

		// Parcours sphère et falloff linéaire
		double r2 = R*R;
		for (int dx=-R; dx<=R; dx++) for (int dy=-R; dy<=R; dy++) for (int dz=-R; dz<=R; dz++) {
			if (dx*dx + dy*dy + dz*dz > r2) continue;
			BlockPos p = center.offset(dx, dy, dz);
			if (lvl.isEmptyBlock(p) || lvl.getBlockState(p).getBlock() == Blocks.BEDROCK) continue;

			double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
			int dmg = Math.max(1, baseDmg - (int)Math.floor(dist)); // falloff
			BlockHpData.damage(lvl, p, dmg);
		}
	}
}