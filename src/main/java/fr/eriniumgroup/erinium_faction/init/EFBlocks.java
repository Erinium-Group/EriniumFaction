package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.block.DeepslateSilverOreBlock;
import fr.eriniumgroup.erinium_faction.common.block.SilverOreBlock;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EFBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(Registries.BLOCK, EFC.MOD_ID);

    public static final DeferredHolder<Block, Block> SILVER_ORE = REGISTER.register("silver_ore", SilverOreBlock::new);
    public static final DeferredHolder<Block, Block> DEEPSLATE_SILVER_ORE = REGISTER.register("deepslate_silver_ore", DeepslateSilverOreBlock::new);

    public static void registerBlockItems(DeferredRegister<Item> itemRegister) {
        itemRegister.register("silver_ore", () -> new BlockItem(SILVER_ORE.get(), new Item.Properties()));
        itemRegister.register("deepslate_silver_ore", () -> new BlockItem(DEEPSLATE_SILVER_ORE.get(), new Item.Properties()));
    }

    private EFBlocks() {
    }
}
