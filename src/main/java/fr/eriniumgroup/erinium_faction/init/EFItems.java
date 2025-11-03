package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.item.RawSilverItem;
import fr.eriniumgroup.erinium_faction.common.item.SilverIngotItem;
import fr.eriniumgroup.erinium_faction.common.item.SilverScrapItem;
import fr.eriniumgroup.erinium_faction.common.item.StatsResetTokenItem;
import fr.eriniumgroup.erinium_faction.common.item.*;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EFItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(Registries.ITEM, EFC.MOD_ID);

    public static final DeferredHolder<Item, StatsResetTokenItem> STATS_RESET_TOKEN = REGISTER.register("stats_reset_token", () -> new StatsResetTokenItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, RawSilverItem> RAW_SILVER = REGISTER.register("raw_silver", () -> new RawSilverItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, SilverIngotItem> SILVER_INGOT = REGISTER.register("silver_ingot", () -> new SilverIngotItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, SilverScrapItem> SILVER_SCRAP = REGISTER.register("silver_scrap", () -> new SilverScrapItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, RawTitaniumItem> RAW_TITANIUM = REGISTER.register("raw_titanium", () -> new RawTitaniumItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, TitaniumIngotItem> TITANIUM_INGOT = REGISTER.register("titanium_ingot", () -> new TitaniumIngotItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));

    public static final DeferredHolder<Item, SilverPlateItem> SILVER_PLATE = REGISTER.register("silver_plate", () -> new SilverPlateItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, TitaniumPlateItem> TITANIUM_PLATE = REGISTER.register("titanium_plate", () -> new TitaniumPlateItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));

    private EFItems() {
    }
}
