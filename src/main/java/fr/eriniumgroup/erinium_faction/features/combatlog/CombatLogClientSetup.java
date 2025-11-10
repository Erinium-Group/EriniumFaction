package fr.eriniumgroup.erinium_faction.features.combatlog;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Setup côté client pour le système de combat logging
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class CombatLogClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CombatLogEntities.COMBAT_LOG_NPC.get(), CombatLogNPCRenderer::new);
    }
}
