package fr.eriniumgroup.erinium_faction.compat.jade;

import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumBatteryTier1BlockEntity;
import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCompressorBlockEntity;
import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCreativeBatteryBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public class EnergyAndMachineProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    @Override
    public ResourceLocation getUid() { return EFJadePlugin.BATTERY; }

    private static String fmtFE(int v) {
        if (v >= 1_000_000) return String.format("%.1f MFE", v / 1_000_000.0);
        if (v >= 1_000) return String.format("%.1f KFE", v / 1_000.0);
        return v + " FE";
    }

    private static String fmtRate(int v) {
        if (v >= 1_000_000) return String.format("%.1f MFE/t", v / 1_000_000.0);
        if (v >= 1_000) return String.format("%.1f KFE/t", v / 1_000.0);
        return v + " FE/t";
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var be = accessor.getBlockEntity();
        var helper = IElementHelper.get();
        if (be instanceof TitaniumBatteryTier1BlockEntity batt) {
            var storage = batt.getEnergy(null);
            int cur = storage.getEnergyStored();
            int max = storage.getMaxEnergyStored();
            float pct = max > 0 ? (float) cur / (float) max : 0f;
            tooltip.add(Component.translatable("tooltip.erinium_faction.energy").append(": ").append(Component.literal(fmtFE(cur) + "/" + fmtFE(max))));
            tooltip.add(helper.progress(pct));
            tooltip.add(Component.translatable("tooltip.erinium_faction.energy_out").append(": ").append(Component.literal(fmtRate(batt.getLastOutPerTick()))));
            tooltip.add(Component.translatable("tooltip.erinium_faction.energy_in").append(": ").append(Component.literal(fmtRate(batt.getLastInPerTick()))));
        } else if (be instanceof TitaniumCreativeBatteryBlockEntity battC) {
            tooltip.add(Component.translatable("tooltip.erinium_faction.energy").append(": ").append(Component.literal("∞ FE")));
            tooltip.add(helper.progress(1.0f));
            tooltip.add(Component.translatable("tooltip.erinium_faction.energy_out").append(": ").append(Component.literal(fmtRate(battC.getLastOutPerTick()))));
        } else if (be instanceof TitaniumCompressorBlockEntity comp) {
            int prog = comp.getProgress();
            int total = comp.getMaxProgress();
            float pct = total > 0 ? (float) prog / (float) total : 0f;
            tooltip.add(Component.translatable("block.erinium_faction.titanium_compressor"));
            tooltip.add(helper.progress(pct));
            tooltip.add(Component.translatable("tooltip.erinium_faction.progress").append(": ").append(Component.literal(Math.round(pct * 100) + "%")));
            tooltip.add(Component.translatable("tooltip.erinium_faction.energy_in").append(": ").append(Component.literal(fmtRate(comp.getLastInPerTick()))));
            tooltip.add(Component.translatable("tooltip.erinium_faction.usage").append(": ").append(Component.literal(fmtRate(comp.getLastUsePerTick()))));
        }
    }

    @Override
    public void appendServerData(net.minecraft.nbt.CompoundTag data, BlockAccessor accessor) {
        // Pas de data custom nécessaire ici car on lit directement sur le BE côté client
    }
}
