package fr.eriniumgroup.erinium_faction.events;

import com.google.common.collect.Maps;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.features.audit.AuditJsonLog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class EFAuditEvents {
    private record OpenCtx(BlockPos pos, ResourceLocation dim, String blockId, Instant t) {
    }

    private final Map<UUID, OpenCtx> lastCtx = Maps.newConcurrentMap();

    // Associe la position du container au clic droit
    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock e) {
        if (!EFConfig.CORE_ENABLED.get() || !EFConfig.AUDIT_ENABLE_ALL.get()) return;
        var p = e.getEntity();
        var lv = p.level();
        var pos = e.getPos();
        if (isContainerLike(lv, pos)) {
            var state = lv.getBlockState(pos);
            lastCtx.put(p.getUUID(), new OpenCtx(pos, lv.dimension().location(), idOf(state.getBlock()), Instant.now()));
        }
    }

    // Ouverture de n'importe quel menu de type container (vanilla + mods)
    @SubscribeEvent
    public void onOpen(PlayerContainerEvent.Open e) {
        if (!EFConfig.CORE_ENABLED.get() || !EFConfig.AUDIT_ENABLE_ALL.get() || !EFConfig.AUDIT_LOG_OPEN.get()) return;
        var menu = e.getContainer();
        var p = e.getEntity();
        var ctx = pull(p, EFConfig.AUDIT_CLICK_OPEN_WINDOW_MS.get());
        var payload = basePlayer(p);
        payload.put("menu", menu.getClass().getName());
        if (ctx != null) {
            payload.put("pos", pos(ctx.pos));
            payload.put("dim", ctx.dim.toString());
            payload.put("block", ctx.blockId);
        } else {
            payload.put("pos", pos(p.blockPosition()));
            payload.put("dim", p.level().dimension().location().toString());
        }
        AuditJsonLog.write("container_open", payload);
    }

    // Fermeture
    @SubscribeEvent
    public void onClose(PlayerContainerEvent.Close e) {
        if (!EFConfig.CORE_ENABLED.get() || !EFConfig.AUDIT_ENABLE_ALL.get() || !EFConfig.AUDIT_LOG_CLOSE.get()) return;

        var menu = e.getContainer();
        var p = e.getEntity();
        var ctx = pull(p, Math.max(EFConfig.AUDIT_CLICK_OPEN_WINDOW_MS.get(), 10000));
        var payload = basePlayer(p);
        payload.put("menu", menu.getClass().getName());
        if (ctx != null) {
            payload.put("pos", pos(ctx.pos));
            payload.put("dim", ctx.dim.toString());
            payload.put("block", ctx.blockId);
        } else {
            payload.put("pos", pos(p.blockPosition()));
            payload.put("dim", p.level().dimension().location().toString());
        }
        AuditJsonLog.write("container_close", payload);
    }

    // Pose d’un bloc container-like
    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (!EFConfig.CORE_ENABLED.get() || !EFConfig.AUDIT_ENABLE_ALL.get() || !EFConfig.AUDIT_LOG_PLACE.get()) return;
        if (!(e.getEntity() instanceof Player p)) return;
        var lv = p.level();
        if (!isContainerLikeState(lv, e.getPlacedBlock(), e.getPos())) return;

        var payload = basePlayer(p);
        payload.put("block", idOf(e.getPlacedBlock().getBlock()));
        payload.put("pos", pos(e.getPos()));
        payload.put("dim", lv.dimension().location().toString());
        AuditJsonLog.write("block_place", payload);
    }

    // Casse d’un bloc container-like
    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent e) {
        if (!EFConfig.CORE_ENABLED.get() || !EFConfig.AUDIT_ENABLE_ALL.get() || !EFConfig.AUDIT_LOG_BREAK.get()) return;
        var p = e.getPlayer();
        var lv = p.level();
        if (!isContainerLikeState(lv, e.getState(), e.getPos())) return;

        var payload = basePlayer(p);
        payload.put("block", idOf(e.getState().getBlock()));
        payload.put("pos", pos(e.getPos()));
        payload.put("dim", lv.dimension().location().toString());
        AuditJsonLog.write("block_break", payload);
    }

    // ----- helpers -----

    // “Container-like” mod-friendly: ITEM_HANDLER ou MenuProvider
    private static boolean isContainerLike(Level lvl, BlockPos pos) {
        BlockEntity be = lvl.getBlockEntity(pos);
        if (be == null) return false;
        if (lvl.getCapability(Capabilities.ItemHandler.BLOCK, pos, null) != null) return true;
        return be instanceof MenuProvider;
    }

    private static boolean isContainerLikeState(Level lvl, BlockState s, BlockPos pos) {
        return isContainerLike(lvl, pos);
    }

    private static String idOf(Block b) {
        var k = BuiltInRegistries.BLOCK.getKey(b);
        return k != null ? k.toString() : "unknown";
    }

    private static Map<String, Object> basePlayer(Player p) {
        var m = new LinkedHashMap<String, Object>();
        var pl = new LinkedHashMap<String, Object>();
        pl.put("name", p.getScoreboardName());
        pl.put("uuid", p.getUUID().toString());
        m.put("player", pl);
        return m;
    }

    private static Map<String, Integer> pos(BlockPos p) {
        var m = new LinkedHashMap<String, Integer>();
        m.put("x", p.getX());
        m.put("y", p.getY());
        m.put("z", p.getZ());
        return m;
    }

    private OpenCtx pull(Player p, int ms) {
        var ctx = lastCtx.get(p.getUUID());
        if (ctx == null) return null;
        long dt = Instant.now().toEpochMilli() - ctx.t.toEpochMilli();
        return dt <= ms ? ctx : null;
    }
}