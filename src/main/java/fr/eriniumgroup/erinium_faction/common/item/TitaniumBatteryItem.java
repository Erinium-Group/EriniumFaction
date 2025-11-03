package fr.eriniumgroup.erinium_faction.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class TitaniumBatteryItem extends BlockItem {
    private final int capacity;
    private final boolean creative;

    public TitaniumBatteryItem(Block block, Item.Properties props, int capacity, boolean creative) {
        super(block, props);
        this.capacity = capacity;
        this.creative = creative;
    }

    private static String fmtFE(int v) {
        if (v >= 1_000_000) return String.format("%.1f MFE", v / 1_000_000.0);
        if (v >= 1_000) return String.format("%.1f KFE", v / 1_000.0);
        return v + " FE";
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (creative) {
            tooltip.add(Component.literal("Energy: ∞ FE"));
            return;
        }
        int energy = 0;
        CustomData beData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (beData != null && !beData.isEmpty()) {
            var tag = beData.copyTag();
            if (tag.contains("energy")) {
                energy = tag.getInt("energy");
            }
        }
        tooltip.add(Component.literal("Energy: " + fmtFE(energy) + " / " + fmtFE(capacity)));
    }
}
