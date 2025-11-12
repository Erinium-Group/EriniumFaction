package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.block.entity.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class EFCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> be instanceof TitaniumCompressorBlockEntity comp ? comp.getEnergyCapability(side) : null,
            EFBlocks.TITANIUM_COMPRESSOR.get()
        );
    }
}
