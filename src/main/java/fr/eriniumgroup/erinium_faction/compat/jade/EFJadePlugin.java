package fr.eriniumgroup.erinium_faction.compat.jade;

import fr.eriniumgroup.erinium_faction.common.block.TitaniumBatteryTier1Block;
import fr.eriniumgroup.erinium_faction.common.block.TitaniumCreativeBatteryBlock;
import fr.eriniumgroup.erinium_faction.common.block.TitaniumCompressorBlock;
import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumBatteryTier1BlockEntity;
import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCompressorBlockEntity;
import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCreativeBatteryBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class EFJadePlugin implements IWailaPlugin {
    public static final ResourceLocation BATTERY_TIER1 = ResourceLocation.fromNamespaceAndPath("erinium_faction", "battery_tier1");
    public static final ResourceLocation BATTERY_CREATIVE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "battery_creative");
    public static final ResourceLocation COMPRESSOR = ResourceLocation.fromNamespaceAndPath("erinium_faction", "compressor");


    @Override
    public void register(IWailaCommonRegistration registration) {
        var batteryProvider = new EnergyAndMachineProvider(BATTERY_TIER1);
        var batteryCreativeProvider = new EnergyAndMachineProvider(BATTERY_CREATIVE);
        var compressorProvider = new EnergyAndMachineProvider(COMPRESSOR);

        registration.registerBlockDataProvider(batteryProvider, TitaniumBatteryTier1BlockEntity.class);
        registration.registerBlockDataProvider(batteryCreativeProvider, TitaniumCreativeBatteryBlockEntity.class);
        registration.registerBlockDataProvider(compressorProvider, TitaniumCompressorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        var batteryProvider = new EnergyAndMachineProvider(BATTERY_TIER1);
        var batteryCreativeProvider = new EnergyAndMachineProvider(BATTERY_CREATIVE);
        var compressorProvider = new EnergyAndMachineProvider(COMPRESSOR);

        registration.registerBlockComponent(batteryProvider, TitaniumBatteryTier1Block.class);
        registration.registerBlockComponent(batteryCreativeProvider, TitaniumCreativeBatteryBlock.class);
        registration.registerBlockComponent(compressorProvider, TitaniumCompressorBlock.class);
    }
}
