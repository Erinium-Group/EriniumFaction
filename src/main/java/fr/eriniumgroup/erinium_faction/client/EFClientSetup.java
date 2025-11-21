package fr.eriniumgroup.erinium_faction.client;

import fr.eriniumgroup.erinium_faction.client.model.RocketModel;
import fr.eriniumgroup.erinium_faction.client.renderer.EriniumChestRenderer;
import fr.eriniumgroup.erinium_faction.client.renderer.RocketRenderer;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;
import fr.eriniumgroup.erinium_faction.init.EFEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = EFC.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
@SuppressWarnings("removal")
public class EFClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Block entity renderers
        event.registerBlockEntityRenderer(EFBlockEntities.ERINIUM_CHEST.get(), EriniumChestRenderer::new);

        // Entity renderers
        event.registerEntityRenderer(EFEntities.ROCKET.get(), RocketRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Block entity layers
        event.registerLayerDefinition(EriniumChestRenderer.LAYER_LOCATION, EriniumChestRenderer::createBodyLayer);

        // Entity layers
        event.registerLayerDefinition(RocketModel.LAYER_LOCATION, RocketModel::createBodyLayer);
    }
}
