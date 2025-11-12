package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.block.*;
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

    public static final DeferredHolder<Block, Block> ERINIUM_ORE = REGISTER.register("erinium_ore", EriniumOreBlock::new);
    public static final DeferredHolder<Block, Block> DEEPSLATE_ERINIUM_ORE = REGISTER.register("deepslate_erinium_ore", DeepslateEriniumOreBlock::new);
    public static final DeferredHolder<Block, Block> ERINIUM_BLOCK = REGISTER.register("erinium_block", EriniumBlockBlock::new);

    public static final DeferredHolder<Block, Block> ERINIUM_CHEST = REGISTER.register("erinium_chest", EriniumChestBlock::new);

    // Machine: TitaniumCompressor
    public static final DeferredHolder<Block, Block> TITANIUM_COMPRESSOR = REGISTER.register("titanium_compressor", TitaniumCompressorBlock::new);

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

        // BlockItem pour les blocs Erinium
        itemRegister.register("erinium_ore", () -> new BlockItem(ERINIUM_ORE.get(), new Item.Properties()));
        itemRegister.register("deepslate_erinium_ore", () -> new BlockItem(DEEPSLATE_ERINIUM_ORE.get(), new Item.Properties()));
        itemRegister.register("erinium_block", () -> new BlockItem(ERINIUM_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE).fireResistant()));
        // BlockItem pour le coffre Erinium
        itemRegister.register("erinium_chest", () -> new BlockItem(ERINIUM_CHEST.get(), new Item.Properties()));
    }

    private EFBlocks() {
    }
}
