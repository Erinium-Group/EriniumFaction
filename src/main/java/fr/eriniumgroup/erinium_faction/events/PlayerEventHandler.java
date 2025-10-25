package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class PlayerEventHandler {

    @SubscribeEvent
    public static void playerTick(EntityTickEvent.Pre e){
        if (!(e.getEntity() instanceof ServerPlayer _p)) return;

        if(_p.level().isClientSide()) return;

        if (_p.tickCount() % 6000 == 0){
            _p.getData(EFVariables.PLAYER_VARIABLES)
        }
    }
}
