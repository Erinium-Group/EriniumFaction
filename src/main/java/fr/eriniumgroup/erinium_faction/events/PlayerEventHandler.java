package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.common.network.packets.FactionTitlePacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;

@EventBusSubscriber(modid = EFC.MODID)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void playerTick(EntityTickEvent.Post e){
        if (!(e.getEntity() instanceof ServerPlayer _p)) return;
        if (_p.level().isClientSide()) return;
        var vars = _p.getData(EFVariables.PLAYER_VARIABLES);
        int fadeIn = 500, stay = 1000, fadeOut = 500;
        String title = "";
        String subtitle = "";
        ChunkPos chunkpos = new ChunkPos(new BlockPos((int) _p.getX(), (int) _p.getY(), (int) _p.getZ()));
        String id = FactionManager.getClaimOwner(ClaimKey.of(_p.level().dimension(), (int) _p.getX(), (int) _p.getZ()));
        if (!vars.lastChunk.equals("r." + chunkpos.getRegionLocalX() + "." + chunkpos.getRegionLocalZ()) && !vars.factionInChunk.equals(id)){
            vars.lastChunk = "r." + chunkpos.getRegionLocalX() + "." + chunkpos.getRegionLocalZ();
            if (id.equals("warzone")){
                title = Component.translatable("erinium_faction.warzone.title").getString();
                subtitle = Component.translatable("rinium_faction.warzone.desc").getString();
                vars.factionInChunk = "warzone";
            }else if (id.equals("safezone")){
                title = Component.translatable("erinium_faction.safezone.title").getString();
                subtitle = Component.translatable("rinium_faction.safezone.desc").getString();
                vars.factionInChunk = "safezone";
            } else if (id.equals("wilderness")) {
                title = Component.translatable("erinium_faction.wilderness.title").getString();
                subtitle = Component.translatable("erinium_faction.wilderness.desc").getString();
                vars.factionInChunk = "wilderness";
            }else {
                Faction f = FactionManager.getFaction(id);
                title = f.getName();
                subtitle = f.getDescription();
                vars.factionInChunk = id;
            }
            EFC.log.debug("temp", (title + " " + subtitle));
            PacketDistributor.sendToPlayer(_p, new FactionTitlePacket(title, subtitle, fadeIn, stay, fadeOut));
            vars.syncPlayerVariables(_p);
        }
        // TODO : POWER
        if (_p.tickCount % 6000 == 0){

        }
    }
}
