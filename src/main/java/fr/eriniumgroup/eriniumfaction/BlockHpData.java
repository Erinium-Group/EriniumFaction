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

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;

public final class BlockHpData extends SavedData {
	private final Long2IntOpenHashMap hp = new Long2IntOpenHashMap();

	public static BlockHpData get(ServerLevel level){
		DimensionDataStorage store = level.getDataStorage();
		return store.computeIfAbsent(BlockHpData::load, BlockHpData::new, "er_block_hp");
	}

	public static BlockHpData load(CompoundTag tag){
		BlockHpData d = new BlockHpData();
		long[] pos = tag.getLongArray("p");
		int[] vs = tag.getIntArray("v");
		for (int i=0;i<Math.min(pos.length, vs.length);i++) d.hp.put(pos[i], vs[i]);
		return d;
	}

	@Override
	public CompoundTag save(CompoundTag tag){
		tag.putLongArray("p", hp.keySet().toLongArray());
		tag.putIntArray("v", hp.values().toIntArray());
		return tag;
	}

	public int get(BlockPos p){ return hp.getOrDefault(p.asLong(), Integer.MIN_VALUE); }
	public void set(BlockPos p, int v){ if (v<=0) hp.remove(p.asLong()); else hp.put(p.asLong(), v); setDirty(); }
	public void clear(BlockPos p){ hp.remove(p.asLong()); setDirty(); }

	// helper optionnel
	private static ResourceLocation keyOf(BlockState s){
		var k = ForgeRegistries.BLOCKS.getKey(s.getBlock());
		return k != null ? k : new ResourceLocation("minecraft","air");
	}

	public static int current(ServerLevel lvl, BlockPos pos, BlockState state){
		BlockHpData d = get(lvl);
		int cur = d.get(pos);
		if (cur == Integer.MIN_VALUE){

			// usages
			int base = BlockHpRegistry.baseHp(keyOf(state));
			cur = base;
			d.set(pos, cur);
		}
		return cur;
	}

	public static int base(BlockState state){
		return BlockHpRegistry.baseHp(keyOf(state));
	}

	public static void damage(ServerLevel lvl, BlockPos pos, int dmg){
		if (lvl.isEmptyBlock(pos)) return;
		BlockState state = lvl.getBlockState(pos);
		int base = base(state);
		BlockHpData d = get(lvl);

		int cur = d.get(pos);
		if (cur == Integer.MIN_VALUE) cur = base;

		int next = cur - Math.max(1, dmg);
		if (next > 0){
			d.set(pos, next);
			int prog = 9 - Mth.clamp((int)Math.floor((next*10.0)/Math.max(1, base)), 0, 9);
			int id = (int)(pos.asLong() ^ 0x45AF13);
			ClientboundBlockDestructionPacket pkt = new ClientboundBlockDestructionPacket(id, pos, prog);
			double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
			double r2 = 32 * 32; // même portée que l'ancien broadcast

			for (ServerPlayer sp : lvl.players()) {
				if (sp.distanceToSqr(cx, cy, cz) <= r2) {
					sp.connection.send(pkt);
				}
			}
			return;
		}

		// break + drops
		var be = lvl.getBlockEntity(pos);
		var drops = Block.getDrops(state, lvl, pos, be, null, ItemStack.EMPTY);
		drops.forEach(s -> Containers.dropItemStack(lvl, pos.getX(), pos.getY(), pos.getZ(), s.copy()));
		lvl.levelEvent(2001, pos, Block.getId(state));
		lvl.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		d.clear(pos);
	}

	// --- API UTILITAIRE ---

	/** Applique des dégâts (>=1). Retourne les PV restants après application. */
	public static int applyDamage(ServerLevel lvl, BlockPos pos, int dmg) {
		dmg = Math.max(1, dmg);
		if (lvl.isEmptyBlock(pos)) return 0;
		BlockState state = lvl.getBlockState(pos);
		int base = base(state);
		BlockHpData d = get(lvl);

		int cur = d.get(pos);
		if (cur == Integer.MIN_VALUE) cur = base;

		int next = cur - dmg;
		if (next > 0) {
			d.set(pos, next);
			int prog = 9 - Mth.clamp((int)Math.floor((next*10.0)/Math.max(1, base)), 0, 9);
			int id = (int)(pos.asLong() ^ 0x45AF13);
			ClientboundBlockDestructionPacket pkt = new ClientboundBlockDestructionPacket(id, pos, prog);
			double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
			double r2 = 32 * 32; // même portée que l'ancien broadcast

			for (ServerPlayer sp : lvl.players()) {
				if (sp.distanceToSqr(cx, cy, cz) <= r2) {
					sp.connection.send(pkt);
				}
			}
			return next;
		}
		// casse + drop
		var be = lvl.getBlockEntity(pos);
		var drops = Block.getDrops(state, lvl, pos, be, null, ItemStack.EMPTY);
		drops.forEach(s -> Containers.dropItemStack(lvl, pos.getX(), pos.getY(), pos.getZ(), s.copy()));
		lvl.levelEvent(2001, pos, Block.getId(state));
		lvl.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		d.clear(pos);
		return 0;
	}

	/** Soigne un bloc (>=1), sans dépasser ses PV de base. Retourne PV courants après soin. */
	public static int heal(ServerLevel lvl, BlockPos pos, int amount) {
		amount = Math.max(1, amount);
		if (lvl.isEmptyBlock(pos)) return 0;
		BlockState state = lvl.getBlockState(pos);
		int base = base(state);
		BlockHpData d = get(lvl);

		int cur = d.get(pos);
		if (cur == Integer.MIN_VALUE) cur = base; // s’il n’avait jamais pris de dégâts
		int next = Math.min(base, cur + amount);
		d.set(pos, next);

		int prog = 9 - Mth.clamp((int)Math.floor((next*10.0)/Math.max(1, base)), 0, 9);
		int id = (int)(pos.asLong() ^ 0x45AF13);
		ClientboundBlockDestructionPacket pkt = new ClientboundBlockDestructionPacket(id, pos, prog);
		double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
		double r2 = 32 * 32; // même portée que l'ancien broadcast

		for (ServerPlayer sp : lvl.players()) {
			if (sp.distanceToSqr(cx, cy, cz) <= r2) {
				sp.connection.send(pkt);
			}
		}
		return next;
	}

	/** Renvoie [pvCourants, pvBase] pratique pour HUD/inspect. */
	public static int[] hp(ServerLevel lvl, BlockPos pos) {
		if (lvl.isEmptyBlock(pos)) return new int[]{0,0};
		BlockState s = lvl.getBlockState(pos);
		int base = base(s);
		int cur = get(lvl).get(pos);
		if (cur == Integer.MIN_VALUE) cur = base;
		return new int[]{cur, base};
	}

	/** Variante tolérante côté appelant: route vers serveur si possible. */
	public static boolean tryDamage(net.minecraft.world.level.Level level, BlockPos pos, int dmg) {
		if (level instanceof ServerLevel sl) { applyDamage(sl, pos, dmg); return true; }
		return false;
	}
}