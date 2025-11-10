package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.item.*;
import fr.eriniumgroup.erinium_faction.common.item.armor.EriniumArmorItem;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
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

    public static final DeferredHolder<Item, CopperWireItem> COPPER_WIRE = REGISTER.register("copper_wire", () -> new CopperWireItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, SilverWireItem> SILVER_WIRE = REGISTER.register("silver_wire", () -> new SilverWireItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, TitaniumWireItem> TITANIUM_WIRE = REGISTER.register("titanium_wire", () -> new TitaniumWireItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));

    public static final DeferredHolder<Item, SilverPlateItem> SILVER_PLATE = REGISTER.register("silver_plate", () -> new SilverPlateItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, TitaniumPlateItem> TITANIUM_PLATE = REGISTER.register("titanium_plate", () -> new TitaniumPlateItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));

    // Erinium items
    public static final DeferredHolder<Item, EriniumIngotItem> ERINIUM_INGOT = REGISTER.register("erinium_ingot", () -> new EriniumIngotItem(new Item.Properties().stacksTo(64).rarity(Rarity.RARE).fireResistant()));

    // Erinium armor
    public static final DeferredHolder<Item, EriniumArmorItem> ERINIUM_HELMET = REGISTER.register("erinium_helmet", () -> new EriniumArmorItem(ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, EriniumArmorItem> ERINIUM_CHESTPLATE = REGISTER.register("erinium_chestplate", () -> new EriniumArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, EriniumArmorItem> ERINIUM_LEGGINGS = REGISTER.register("erinium_leggings", () -> new EriniumArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, EriniumArmorItem> ERINIUM_BOOTS = REGISTER.register("erinium_boots", () -> new EriniumArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));

    // Erinium tools
    public static final DeferredHolder<Item, net.minecraft.world.item.SwordItem> ERINIUM_SWORD = REGISTER.register("erinium_sword", () -> new net.minecraft.world.item.SwordItem(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, new Item.Properties().attributes(net.minecraft.world.item.SwordItem.createAttributes(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, 3.0F, -2.4F)).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, net.minecraft.world.item.PickaxeItem> ERINIUM_PICKAXE = REGISTER.register("erinium_pickaxe", () -> new net.minecraft.world.item.PickaxeItem(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, new Item.Properties().attributes(net.minecraft.world.item.PickaxeItem.createAttributes(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, 1.0F, -2.8F)).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, net.minecraft.world.item.AxeItem> ERINIUM_AXE = REGISTER.register("erinium_axe", () -> new net.minecraft.world.item.AxeItem(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, new Item.Properties().attributes(net.minecraft.world.item.AxeItem.createAttributes(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, 5.0F, -3.0F)).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, net.minecraft.world.item.ShovelItem> ERINIUM_SHOVEL = REGISTER.register("erinium_shovel", () -> new net.minecraft.world.item.ShovelItem(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, new Item.Properties().attributes(net.minecraft.world.item.ShovelItem.createAttributes(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, 1.5F, -3.0F)).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, net.minecraft.world.item.HoeItem> ERINIUM_HOE = REGISTER.register("erinium_hoe", () -> new net.minecraft.world.item.HoeItem(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, new Item.Properties().attributes(net.minecraft.world.item.HoeItem.createAttributes(fr.eriniumgroup.erinium_faction.common.item.tool.EriniumToolMaterial.INSTANCE, -4.0F, 0.0F)).rarity(Rarity.RARE).fireResistant()));

    private EFItems() {
    }
}