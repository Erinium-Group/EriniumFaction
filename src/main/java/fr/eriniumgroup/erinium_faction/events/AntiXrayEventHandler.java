package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.features.antixray.AntiXrayManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class AntiXrayEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AntiXrayManager.getInstance().getEngine().onPlayerMoved(player);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        var engine = AntiXrayManager.getInstance().getEngine();

        BlockState state = level.getBlockState(event.getPos());
        // Anti packet-canceller: interdire la casse d'un minerai non révélé pour ce joueur
        if (engine.getConfig().isEnabled() && engine.getConfig().isHiddenBlock(state.getBlock())) {
            boolean revealed = engine.isBlockRevealed(player.getUUID(), event.getPos());
            if (!revealed) {
                // Révéler autour et bloquer la casse
                engine.revealBlocksAround(level, event.getPos(), player);
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§7Ce bloc n'est pas encore révélé."));
                return;
            }
        }

        // Sinon: révélation douce autour du bloc cassé
        engine.revealBlocksAround(level, event.getPos(), player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AntiXrayManager.getInstance().getEngine().cleanupPlayer(player.getUUID());
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk chunk)) return;
        var engine = AntiXrayManager.getInstance().getEngine();
        engine.onChunkLoaded(level, chunk, level.players());
    }
}
