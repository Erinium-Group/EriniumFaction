package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.common.network.packets.FactionTitlePacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

@EventBusSubscriber(modid = EFC.MODID)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void playerTick(EntityTickEvent.Pre e) {
        if (!(e.getEntity() instanceof ServerPlayer _p)) return;
        if (_p.level().isClientSide()) return;

        var vars = _p.getData(EFVariables.PLAYER_VARIABLES);
        int fadeIn = 500, stay = 1000, fadeOut = 500;
        String title = "";
        String subtitle = "";

        ChunkPos chunkpos = new ChunkPos(_p.blockPosition());
        String regionKey = "c." + chunkpos.x + "." + chunkpos.z;

        String id = FactionManager.getClaimOwner(ClaimKey.of(_p.level().dimension(), chunkpos.x, chunkpos.z));
        if (id == null || id.isBlank()) id = "wilderness";

        if (!Objects.equals(id, vars.factionInChunk)) {
            vars.lastChunk = regionKey;
            switch (id) {
                case "warzone" -> {
                    title = Component.translatable("erinium_faction.warzone.title").getString();
                    subtitle = Component.translatable("erinium_faction.warzone.desc").getString();
                    vars.factionInChunk = "warzone";
                }
                case "safezone" -> {
                    title = Component.translatable("erinium_faction.safezone.title").getString();
                    subtitle = Component.translatable("erinium_faction.safezone.desc").getString();
                    vars.factionInChunk = "safezone";
                }
                case "wilderness" -> {
                    title = Component.translatable("erinium_faction.wilderness.title").getString();
                    subtitle = Component.translatable("erinium_faction.wilderness.desc").getString();
                    vars.factionInChunk = "wilderness";
                }
                default -> {
                    Faction f = FactionManager.getFaction(id);
                    if (f != null) {
                        title = f.getName();
                        subtitle = f.getDescription() == null ? "" : f.getDescription();
                    } else {
                        title = id; // fallback lisible
                        subtitle = "";
                    }
                    vars.factionInChunk = id;
                }
            }
            //EFC.log.debug("PlayerEventHandler", title + " | " + subtitle);
            PacketDistributor.sendToPlayer(_p, new FactionTitlePacket(title, subtitle, fadeIn, stay, fadeOut));
            vars.syncPlayerVariables(_p);
        }

        // TODO : POWER (rafraîchissement périodique)
        if (_p.tickCount % 6000 == 0) {
            // futur: sync power périodique
        }
    }
}
