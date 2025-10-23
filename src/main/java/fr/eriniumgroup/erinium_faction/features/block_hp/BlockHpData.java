package fr.eriniumgroup.erinium_faction.features.block_hp;

import fr.eriniumgroup.erinium_faction.common.util.EFUtils;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public final class BlockHpData extends SavedData {
    private final Long2IntOpenHashMap hp = new Long2IntOpenHashMap();
    private final Long2ObjectOpenHashMap<ResourceLocation> ids = new Long2ObjectOpenHashMap<>();

    public static BlockHpData get(ServerLevel level) {
        DimensionDataStorage store = level.getDataStorage();
        return store.computeIfAbsent(new Factory<>(BlockHpData::new, BlockHpData::load), "er_block_hp");
    }

    public static BlockHpData load(CompoundTag tag, HolderLookup.Provider provider) {
        BlockHpData d = new BlockHpData();
        long[] pos = tag.getLongArray("p");
        int[] vs = tag.getIntArray("v");
        var idList = tag.getList("id", 8); // 8 = String
        for (int i = 0; i < Math.min(pos.length, vs.length); i++) {
            d.hp.put(pos[i], vs[i]);
            if (i < idList.size()) d.ids.put(pos[i], ResourceLocation.parse(idList.getString(i)));
        }
        return d;
    }

    // imports: Level, BlockState
    public static int[] hpClient(net.minecraft.world.level.Level level, BlockPos pos) {
        // serveur → vrai calcul
        if (level instanceof ServerLevel sl) return hp(sl, pos);
        // client → base connue, courant inconnu: on approx cur=base si jamais touché
        BlockState s = level.getBlockState(pos);
        int base = base(s);
        return new int[]{base, base};
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var positions = hp.keySet().toLongArray();
        var values = hp.values().toIntArray();
        tag.putLongArray("p", positions);
        tag.putIntArray("v", values);
        var idList = new net.minecraft.nbt.ListTag();
        for (long p : positions) {
            var rl = ids.get(p);
            idList.add(net.minecraft.nbt.StringTag.valueOf(rl == null ? "minecraft:air" : rl.toString()));
        }
        tag.put("id", idList);
        return tag;
    }

    public int get(BlockPos p) {
        return hp.getOrDefault(p.asLong(), Integer.MIN_VALUE);
    }

    public void set(BlockPos p, int v, ResourceLocation id) {
        if (v <= 0) {
            hp.remove(p.asLong());
            ids.remove(p.asLong());
        } else {
            hp.put(p.asLong(), v);
            ids.put(p.asLong(), id);
        }
        setDirty();
    }

    public void clear(BlockPos p) {
        hp.remove(p.asLong());
        ids.remove(p.asLong());
        setDirty();
    }

    // helper optionnel
    private static ResourceLocation keyOf(BlockState s) {
        var k = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock());
        return k != null ? k : ResourceLocation.fromNamespaceAndPath("minecraft", "air");
    }

    public static int current(ServerLevel lvl, BlockPos pos, BlockState state) {
        BlockHpData d = get(lvl);
        long key = pos.asLong();
        int cur = d.hp.getOrDefault(key, Integer.MIN_VALUE);
        ResourceLocation idNow = keyOf(state);
        ResourceLocation idSaved = d.ids.get(key);

        if (cur == Integer.MIN_VALUE || idSaved == null || !idSaved.equals(idNow)) {
            int base = BlockHpRegistry.baseHp(idNow);
            d.set(pos, base, idNow);
            return base;
        }
        return cur;
    }

    public static int base(BlockState state) {
        return BlockHpRegistry.baseHp(keyOf(state));
    }

    public static void damage(ServerLevel lvl, BlockPos pos, int dmg) {
        if (lvl.isEmptyBlock(pos)) return;
        BlockState state = lvl.getBlockState(pos);
        int cur = current(lvl, pos, state);     // lit/initialise selon l'ID courant
        int base = base(state);
        BlockHpData d = get(lvl);

        if (cur == Integer.MIN_VALUE) cur = base;

        int next = cur - Math.max(1, dmg);
        if (next > 0) {
            d.set(pos, next, keyOf(state));
            int prog = 9 - Mth.clamp((int) Math.floor((next * 10.0) / Math.max(1, base)), 0, 9);
            int id = (int) (pos.asLong() ^ 0x45AF13);
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

    /**
     * Applique des dégâts (>=1). Retourne les PV restants après application.
     */
    public static int applyDamage(ServerLevel lvl, BlockPos pos, int dmg, Entity entity) {
        if (EFUtils.Faction.AdminProtectionBlockPos(lvl, pos.getX(), pos.getZ())) {
            dmg = Math.max(1, dmg);
            if (lvl.isEmptyBlock(pos)) return 0;
            BlockState state = lvl.getBlockState(pos);
            int cur = current(lvl, pos, state);     // lit/initialise selon l'ID courant
            int base = base(state);
            BlockHpData d = get(lvl);

            if (cur == Integer.MIN_VALUE) cur = base;

            int next = cur - dmg;
            if (next > 0) {
                d.set(pos, next, keyOf(state));
                int prog = 9 - Mth.clamp((int) Math.floor((next * 10.0) / Math.max(1, base)), 0, 9);
                int id = (int) (pos.asLong() ^ 0x45AF13);
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
        return 0;
    }

    /**
     * Soigne un bloc (>=1), sans dépasser ses PV de base. Retourne PV courants après soin.
     */
    public static int heal(ServerLevel lvl, BlockPos pos, int amount) {
        amount = Math.max(1, amount);
        if (lvl.isEmptyBlock(pos)) return 0;
        BlockState state = lvl.getBlockState(pos);
        int cur = current(lvl, pos, state);     // lit/initialise selon l'ID courant
        int base = base(state);
        BlockHpData d = get(lvl);

        if (cur == Integer.MIN_VALUE) cur = base; // s’il n’avait jamais pris de dégâts
        int next = Math.min(base, cur + amount);
        d.set(pos, next, keyOf(state));

        int prog = 9 - Mth.clamp((int) Math.floor((next * 10.0) / Math.max(1, base)), 0, 9);
        int id = (int) (pos.asLong() ^ 0x45AF13);
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

    /**
     * Renvoie [pvCourants, pvBase] pratique pour HUD/inspect.
     */
    public static int[] hp(ServerLevel lvl, BlockPos pos) {
        if (lvl.isEmptyBlock(pos)) return new int[]{0, 0};
        BlockState s = lvl.getBlockState(pos);
        int base = base(s);
        int cur = get(lvl).get(pos);
        if (cur == Integer.MIN_VALUE) cur = base;
        return new int[]{cur, base};
    }
}