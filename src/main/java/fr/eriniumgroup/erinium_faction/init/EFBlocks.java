package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.block.*;
import fr.eriniumgroup.erinium_faction.common.item.TitaniumBatteryItem;
import fr.eriniumgroup.erinium_faction.common.block.DeepslateSilverOreBlock;
import fr.eriniumgroup.erinium_faction.common.block.SilverBlockBlock;
import fr.eriniumgroup.erinium_faction.common.block.DeepslateTitaniumOreBlock;
import fr.eriniumgroup.erinium_faction.common.block.SilverOreBlock;
import fr.eriniumgroup.erinium_faction.common.block.TitaniumOreBlock;
import fr.eriniumgroup.erinium_faction.common.block.TitaniumBlockBlock;
import fr.eriniumgroup.erinium_faction.common.block.EriniumChestBlock;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EFBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(Registries.BLOCK, EFC.MOD_ID);

    public static final DeferredHolder<Block, Block> SILVER_ORE = REGISTER.register("silver_ore", SilverOreBlock::new);
    public static final DeferredHolder<Block, Block> DEEPSLATE_SILVER_ORE = REGISTER.register("deepslate_silver_ore", DeepslateSilverOreBlock::new);
    public static final DeferredHolder<Block, Block> SILVER_BLOCK = REGISTER.register("silver_block", SilverBlockBlock::new);

    public static final DeferredHolder<Block, Block> TITANIUM_ORE = REGISTER.register("titanium_ore", TitaniumOreBlock::new);
    public static final DeferredHolder<Block, Block> DEEPSLATE_TITANIUM_ORE = REGISTER.register("deepslate_titanium_ore", DeepslateTitaniumOreBlock::new);
    public static final DeferredHolder<Block, Block> TITANIUM_BLOCK = REGISTER.register("titanium_block", TitaniumBlockBlock::new);

    public static final DeferredHolder<Block, Block> ERINIUM_CHEST = REGISTER.register("erinium_chest", EriniumChestBlock::new);

    // Machine: TitaniumCompressor
    public static final DeferredHolder<Block, Block> TITANIUM_COMPRESSOR = REGISTER.register("titanium_compressor", TitaniumCompressorBlock::new);

    // Batteries
    public static final DeferredHolder<Block, Block> TITANIUM_BATTERY_TIER1 = REGISTER.register("titanium_battery_tier1", TitaniumBatteryTier1Block::new);
    public static final DeferredHolder<Block, Block> TITANIUM_CREATIVE_BATTERY = REGISTER.register("titanium_creative_battery", TitaniumCreativeBatteryBlock::new);

    public static void registerBlockItems(DeferredRegister<Item> itemRegister) {
        itemRegister.register("silver_ore", () -> new BlockItem(SILVER_ORE.get(), new Item.Properties()));
        itemRegister.register("deepslate_silver_ore", () -> new BlockItem(DEEPSLATE_SILVER_ORE.get(), new Item.Properties()));

        itemRegister.register("titanium_ore", () -> new BlockItem(TITANIUM_ORE.get(), new Item.Properties()));
        itemRegister.register("deepslate_titanium_ore", () -> new BlockItem(DEEPSLATE_TITANIUM_ORE.get(), new Item.Properties()));
        // BlockItem pour le bloc d'argent
        itemRegister.register("silver_block", () -> new BlockItem(SILVER_BLOCK.get(), new Item.Properties()));
        // BlockItem pour le bloc de titane
        itemRegister.register("titanium_block", () -> new BlockItem(TITANIUM_BLOCK.get(), new Item.Properties()));
        // BlockItem pour la machine
        itemRegister.register("titanium_compressor", () -> new BlockItem(TITANIUM_COMPRESSOR.get(), new Item.Properties()));

        // Battery items
        itemRegister.register("titanium_battery_tier1", () -> new TitaniumBatteryItem(TITANIUM_BATTERY_TIER1.get(), new Item.Properties(), 100000, false));
        itemRegister.register("titanium_creative_battery", () -> new TitaniumBatteryItem(TITANIUM_CREATIVE_BATTERY.get(), new Item.Properties().rarity(Rarity.EPIC), Integer.MAX_VALUE, true));
        // BlockItem pour le coffre Erinium
        itemRegister.register("erinium_chest", () -> new BlockItem(ERINIUM_CHEST.get(), new Item.Properties()));
    }

    private EFBlocks() {
    }
}
