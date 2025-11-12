package fr.eriniumgroup.erinium_faction.compat.jade;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumCompressorBlock;
import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCompressorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class EFJadePlugin implements IWailaPlugin {
    public static final ResourceLocation COMPRESSOR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "compressor");


    @Override
    public void register(IWailaCommonRegistration registration) {
        var compressorProvider = new EnergyAndMachineProvider(COMPRESSOR);

        registration.registerBlockDataProvider(compressorProvider, TitaniumCompressorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        var compressorProvider = new EnergyAndMachineProvider(COMPRESSOR);

        registration.registerBlockComponent(compressorProvider, TitaniumCompressorBlock.class);
    }
}
