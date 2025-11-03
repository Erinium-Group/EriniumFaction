package fr.eriniumgroup.erinium_faction.compat.jade;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumBatteryTier1Block;
import fr.eriniumgroup.erinium_faction.common.block.TitaniumCreativeBatteryBlock;
import fr.eriniumgroup.erinium_faction.common.block.TitaniumCompressorBlock;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class EFJadePlugin implements IWailaPlugin {
    public static final ResourceLocation BATTERY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "battery");
    public static final ResourceLocation COMPRESSOR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "compressor");


    @Override
    public void register(IWailaCommonRegistration registration) {
        var provider = new EnergyAndMachineProvider();
        registration.registerBlockDataProvider(provider, fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumBatteryTier1BlockEntity.class);
        registration.registerBlockDataProvider(provider, fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCreativeBatteryBlockEntity.class);
        registration.registerBlockDataProvider(provider, fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCompressorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        var provider = new EnergyAndMachineProvider();
        registration.registerBlockComponent(provider, TitaniumBatteryTier1Block.class);
        registration.registerBlockComponent(provider, TitaniumCreativeBatteryBlock.class);
        registration.registerBlockComponent(provider, TitaniumCompressorBlock.class);
    }
}
