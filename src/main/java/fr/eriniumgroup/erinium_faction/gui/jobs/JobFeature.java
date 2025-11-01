package fr.eriniumgroup.erinium_faction.gui.jobs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Énumération des features débloquées par le niveau
 * Chaque feature contient:
 * - FeatureType: le type de feature (ITEM, BLOCK, DIMENSION, etc.)
 * - name: nom d'affichage
 * - description: description
 * - requiredLevel: niveau requis pour débloquer
 * - displayItem: item pour l'affichage GUI
 * - data: la donnée concrète (Item, Block, String, MobEffect, etc.)
 */
public enum JobFeature {
    // ========================================
    // ITEMS - Tools & Equipment
    // ========================================
    ITEM_IRON_TOOLS("Iron Tools", "Craft and use iron tier tools and weapons", 10, Items.IRON_PICKAXE, Items.IRON_PICKAXE),
    ITEM_DIAMOND_TOOLS("Diamond Tools", "Craft and use diamond tier tools and weapons", 20, Items.DIAMOND_PICKAXE, Items.DIAMOND_PICKAXE),
    ITEM_NETHERITE_TOOLS("Netherite Tools", "Craft and use netherite tier tools and weapons", 30, Items.NETHERITE_PICKAXE, Items.NETHERITE_PICKAXE),
    ITEM_IRON_ARMOR("Iron Armor", "Craft and wear iron armor pieces", 15, Items.IRON_CHESTPLATE, Items.IRON_CHESTPLATE),
    ITEM_DIAMOND_ARMOR("Diamond Armor", "Craft and wear diamond armor pieces", 25, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_CHESTPLATE),
    ITEM_NETHERITE_ARMOR("Netherite Armor", "Craft and wear netherite armor pieces", 35, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_CHESTPLATE),
    ITEM_BOW("Bow", "Craft and use bows for ranged combat", 10, Items.BOW, Items.BOW),
    ITEM_CROSSBOW("Crossbow", "Craft and use crossbows with bolts", 20, Items.CROSSBOW, Items.CROSSBOW),
    ITEM_TRIDENT("Trident", "Use tridents for melee and ranged attacks", 30, Items.TRIDENT, Items.TRIDENT),
    ITEM_SHIELD("Shield", "Craft and use shields for defense", 12, Items.SHIELD, Items.SHIELD),

    // Special items
    ITEM_TOTEM("Totem of Undying", "Craft totems to prevent death once", 40, Items.TOTEM_OF_UNDYING, Items.TOTEM_OF_UNDYING),
    ITEM_ELYTRA("Elytra", "Craft elytras for flight and gliding", 50, Items.ELYTRA, Items.ELYTRA),
    ITEM_ENCHANTED_GOLDEN_APPLE("Enchanted Golden Apple", "Craft powerful healing apples", 45, Items.ENCHANTED_GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE),
    ITEM_NETHERITE_UPGRADE("Netherite Upgrade Template", "Use smithing table to upgrade to netherite", 30, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
    ITEM_BEACON("Beacon", "Craft beacons for powerful area buffs", 60, Items.BEACON, Items.BEACON),
    ITEM_CONDUIT("Conduit", "Craft conduits for underwater breathing", 55, Items.CONDUIT, Items.CONDUIT),

    // ========================================
    // BLOCKS - Building & Utility
    // ========================================
    BLOCK_TNT("TNT", "Craft TNT for explosions and mining", 25, Items.TNT, Blocks.TNT),
    BLOCK_REINFORCED_DEEPSLATE("Reinforced Deepslate", "Mine the strongest block in the game", 70, Items.REINFORCED_DEEPSLATE, Blocks.REINFORCED_DEEPSLATE),
    BLOCK_ENCHANTING_TABLE("Enchanting Table", "Craft enchanting tables for enchantments", 15, Items.ENCHANTING_TABLE, Blocks.ENCHANTING_TABLE),
    BLOCK_ANVIL("Anvil", "Craft anvils for repairing and renaming", 18, Items.ANVIL, Blocks.ANVIL),
    BLOCK_BREWING_STAND("Brewing Stand", "Craft brewing stands for potions", 20, Items.BREWING_STAND, Blocks.BREWING_STAND),
    BLOCK_BEACON_BLOCK("Beacon Block", "Place and activate beacons", 60, Items.BEACON, Blocks.BEACON),
    BLOCK_END_PORTAL_FRAME("End Portal Frame", "Obtain and place end portal frames", 80, Items.END_PORTAL_FRAME, Blocks.END_PORTAL_FRAME),

    // ========================================
    // DIMENSIONS - World Access
    // ========================================
    DIM_NETHER("The Nether", "Travel to the dangerous Nether dimension", 5, Items.NETHERRACK, "minecraft:the_nether"),
    DIM_END("The End", "Access the End dimension and fight the dragon", 40, Items.END_STONE, "minecraft:the_end"),

    // ========================================
    // EFFECTS - Permanent Buffs
    // ========================================
    EFFECT_NIGHT_VISION("Night Vision", "See perfectly in the dark at all times", 50, Items.SPIDER_EYE, MobEffects.NIGHT_VISION),
    EFFECT_WATER_BREATHING("Water Breathing", "Breathe underwater without drowning", 45, Items.PUFFERFISH, MobEffects.WATER_BREATHING),
    EFFECT_FIRE_RESISTANCE("Fire Resistance", "Immunity to fire and lava damage", 55, Items.MAGMA_CREAM, MobEffects.FIRE_RESISTANCE),
    EFFECT_REGENERATION("Regeneration", "Slowly regenerate health over time", 65, Items.GHAST_TEAR, MobEffects.REGENERATION),
    EFFECT_SATURATION("Saturation", "Never lose hunger or saturation", 75, Items.GOLDEN_CARROT, MobEffects.SATURATION);

    /**
     * Type de feature débloquée
     */
    public enum FeatureType {
        ITEM,      // Débloquer un item → data = Item
        BLOCK,     // Débloquer un bloc → data = Block
        DIMENSION, // Accès à une dimension → data = String (ResourceLocation)
        EFFECT,    // Effet de potion permanent → data = MobEffect
        ENCHANT,   // Enchantement spécial → data = ResourceLocation
        ABILITY    // Capacité spéciale → data = String (nom custom)
    }

    private final FeatureType featureType;
    private final String name;
    private final String description;
    private final int requiredLevel;
    private final net.minecraft.world.item.Item displayItem; // Pour l'affichage GUI
    private final Object data; // La donnée concrète: Item, Block, String, MobEffect, etc.

    JobFeature(String name, String description, int requiredLevel,
               net.minecraft.world.item.Item displayItem, Object data) {
        this.name = name;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.displayItem = displayItem;
        this.data = data;

        // Déterminer automatiquement le type selon la donnée
        if (data instanceof net.minecraft.world.item.Item) {
            this.featureType = FeatureType.ITEM;
        } else if (data instanceof net.minecraft.world.level.block.Block) {
            this.featureType = FeatureType.BLOCK;
        } else if (data instanceof String) {
            this.featureType = FeatureType.DIMENSION;
        } else if (data instanceof net.minecraft.world.effect.MobEffect) {
            this.featureType = FeatureType.EFFECT;
        } else if (data instanceof ResourceLocation) {
            this.featureType = FeatureType.ENCHANT;
        } else {
            this.featureType = FeatureType.ABILITY;
        }
    }

    public FeatureType getFeatureType() { return featureType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getRequiredLevel() { return requiredLevel; }
    public net.minecraft.world.item.Item getDisplayItem() { return displayItem; }
    public Object getData() { return data; }

    /**
     * Récupère les données typées selon le type de feature
     */
    public net.minecraft.world.item.Item asItem() {
        return featureType == FeatureType.ITEM ? (net.minecraft.world.item.Item) data : null;
    }

    public net.minecraft.world.level.block.Block asBlock() {
        return featureType == FeatureType.BLOCK ? (net.minecraft.world.level.block.Block) data : null;
    }

    public String asDimension() {
        return featureType == FeatureType.DIMENSION ? (String) data : null;
    }

    public net.minecraft.world.effect.MobEffect asEffect() {
        return featureType == FeatureType.EFFECT ? (net.minecraft.world.effect.MobEffect) data : null;
    }

    public ResourceLocation asEnchantment() {
        return featureType == FeatureType.ENCHANT ? (ResourceLocation) data : null;
    }

    public String asAbility() {
        return featureType == FeatureType.ABILITY ? (String) data : null;
    }

    /**
     * Vérifie si la feature est débloquée pour un niveau donné
     */
    public boolean isUnlocked(int playerLevel) {
        return playerLevel >= requiredLevel;
    }

    /**
     * Trouve une feature spécifique basée sur le type de feature et les données
     */
    public static JobFeature findFeature(FeatureType featureType, Object data) {
        for (JobFeature feature : values()) {
            if (feature.featureType == featureType) {
                if (feature.data != null && feature.data.equals(data)) {
                    return feature;
                }
            }
        }
        return null;
    }

    /**
     * Trouve une feature par son item
     */
    public static JobFeature findFeatureByItem(net.minecraft.world.item.Item item) {
        return findFeature(FeatureType.ITEM, item);
    }

    /**
     * Trouve une feature par son bloc
     */
    public static JobFeature findFeatureByBlock(net.minecraft.world.level.block.Block block) {
        return findFeature(FeatureType.BLOCK, block);
    }

    /**
     * Trouve une feature par dimension
     */
    public static JobFeature findFeatureByDimension(String dimension) {
        return findFeature(FeatureType.DIMENSION, dimension);
    }

    /**
     * Trouve une feature par effet
     */
    public static JobFeature findFeatureByEffect(net.minecraft.world.effect.MobEffect effect) {
        return findFeature(FeatureType.EFFECT, effect);
    }

    /**
     * Vérifie si une feature spécifique est débloquée pour un niveau donné
     */
    public static boolean isFeatureUnlocked(FeatureType featureType, Object data, int playerLevel) {
        JobFeature feature = findFeature(featureType, data);
        if (feature == null) {
            return false;
        }
        return feature.isUnlocked(playerLevel);
    }

    /**
     * Obtient le niveau requis pour une feature spécifique
     */
    public static int getRequiredLevelForFeature(FeatureType featureType, Object data) {
        JobFeature feature = findFeature(featureType, data);
        return feature != null ? feature.requiredLevel : -1;
    }

    /**
     * Génère la description complète avec état de déverrouillage
     */
    public String getFullDescription(int playerLevel) {
        if (isUnlocked(playerLevel)) {
            return description;
        } else {
            int levelsToGo = requiredLevel - playerLevel;
            return "Requires Level " + requiredLevel + " • " + levelsToGo + " level" + (levelsToGo > 1 ? "s" : "") + " to go";
        }
    }
}
