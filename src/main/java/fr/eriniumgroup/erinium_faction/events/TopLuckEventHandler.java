package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.topluck.TopLuckAttachments;
import fr.eriniumgroup.erinium_faction.features.topluck.TopLuckPlacedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = EFC.MODID)
public class TopLuckEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent e) {
        if (!(e.getPlayer() instanceof ServerPlayer sp)) return;
        if (sp.isCreative()) return; // ignorer créatif
        if (!(sp.level() instanceof ServerLevel lvl)) return;
        BlockPos pos = e.getPos();
        // Ignorer les blocs placés par des joueurs
        if (TopLuckPlacedData.get(lvl).isPlaced(pos)) {
            TopLuckPlacedData.get(lvl).clear(pos);
            return;
        }
        BlockState state = lvl.getBlockState(pos);
        var block = state.getBlock();
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        String id = key.toString();

        var data = sp.getData(TopLuckAttachments.PLAYER_TOPLUCK);
        data.increment(id);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer)) return;
        if (!(e.getLevel() instanceof ServerLevel lvl)) return;
        BlockPos pos = e.getPos();
        // Marquer tous les blocs placés
        TopLuckPlacedData.get(lvl).markPlaced(pos);
    }
}
