package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.common.network.packets.FactionTitlePacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = EFC.MODID)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void playerTick(EntityTickEvent.Pre e){
        if (!(e.getEntity() instanceof ServerPlayer _p)) return;
        if (_p.level().isClientSide()) return;
        var vars = _p.getData(EFVariables.PLAYER_VARIABLES);

        if (_p.tickCount % 6000 == 0){
            String title = "";
            String subtitle = "";
            int fadeIn = 500, stay = 1500, fadeOut = 500;
            PacketDistributor.sendToPlayer(_p, new FactionTitlePacket(title, subtitle, fadeIn, stay, fadeOut));
        }
    }
}
