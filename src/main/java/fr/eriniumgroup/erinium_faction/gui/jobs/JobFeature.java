package fr.eriniumgroup.erinium_faction.gui.jobs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Énumération des features débloquées par les jobs
 * Chaque feature contient directement la donnée concrète selon son type:
 * - ITEM → Item (l'item lui-même)
 * - BLOCK → Block (le bloc lui-même)
 * - DIMENSION → String (ResourceLocation en string)
 * - EFFECT → MobEffect (l'effet de potion)
 * - ENCHANT → ResourceLocation (l'enchantement)
 * - ABILITY → String (nom de l'abilité custom)
 */
public enum JobFeature {
    // ========================================
    // GLOBAL FEATURES
    // ========================================
    // ITEMS
    GLOBAL_TOTEM(JobType.GLOBAL, FeatureType.ITEM, "Totem of Undying", "Craft recipe unlocked", 50, Items.TOTEM_OF_UNDYING, Items.TOTEM_OF_UNDYING),
    GLOBAL_ELYTRA(JobType.GLOBAL, FeatureType.ITEM, "Elytra", "Craft recipe unlocked", 100, Items.ELYTRA, Items.ELYTRA),
    GLOBAL_NETHERITE(JobType.GLOBAL, FeatureType.ITEM, "Netherite Upgrade", "Smithing recipe unlocked", 150, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),

    // DIMENSIONS
    GLOBAL_NETHER(JobType.GLOBAL, FeatureType.DIMENSION, "The Nether", "Access to Nether dimension", 200, Items.NETHERRACK, "minecraft:the_nether"),
    GLOBAL_END(JobType.GLOBAL, FeatureType.DIMENSION, "The End", "Access to End dimension", 250, Items.END_STONE, "minecraft:the_end"),

    // ========================================
    // MINER FEATURES
    // ========================================
    // ITEMS
    MINER_IRON_PICKAXE(JobType.MINER, FeatureType.ITEM, "Iron Pickaxe", "Unlock iron tools", 10, Items.IRON_PICKAXE, Items.IRON_PICKAXE),
    MINER_DIAMOND_PICKAXE(JobType.MINER, FeatureType.ITEM, "Diamond Pickaxe", "Unlock diamond tools", 20, Items.DIAMOND_PICKAXE, Items.DIAMOND_PICKAXE),
    MINER_NETHERITE_PICKAXE(JobType.MINER, FeatureType.ITEM, "Netherite Pickaxe", "Unlock netherite tools", 30, Items.NETHERITE_PICKAXE, Items.NETHERITE_PICKAXE),

    // BLOCKS
    MINER_TNT(JobType.MINER, FeatureType.BLOCK, "TNT", "Craft recipe unlocked", 40, Items.TNT, Blocks.TNT),
    MINER_DEEPSLATE(JobType.MINER, FeatureType.BLOCK, "Reinforced Deepslate", "Can mine this block", 50, Items.REINFORCED_DEEPSLATE, Blocks.REINFORCED_DEEPSLATE),

    // ========================================
    // FARMER FEATURES
    // ========================================
    // ITEMS
    FARMER_IRON_HOE(JobType.FARMER, FeatureType.ITEM, "Iron Hoe", "Unlock iron hoes", 10, Items.IRON_HOE, Items.IRON_HOE),
    FARMER_GOLDEN_CARROT(JobType.FARMER, FeatureType.ITEM, "Golden Carrot", "Craft recipe unlocked", 40, Items.GOLDEN_CARROT, Items.GOLDEN_CARROT),
    FARMER_ENCHANTED_GOLDEN_APPLE(JobType.FARMER, FeatureType.ITEM, "Enchanted Golden Apple", "Craft recipe unlocked", 50, Items.ENCHANTED_GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE),

    // BLOCKS
    FARMER_COMPOSTER(JobType.FARMER, FeatureType.BLOCK, "Composter", "Craft recipe unlocked", 20, Items.COMPOSTER, Blocks.COMPOSTER),
    FARMER_BEEHIVE(JobType.FARMER, FeatureType.BLOCK, "Beehive", "Craft recipe unlocked", 30, Items.BEEHIVE, Blocks.BEEHIVE),

    // ========================================
    // LUMBERJACK FEATURES
    // ========================================
    // ITEMS
    LUMBER_IRON_AXE(JobType.LUMBERJACK, FeatureType.ITEM, "Iron Axe", "Unlock iron axes", 10, Items.IRON_AXE, Items.IRON_AXE),
    LUMBER_DIAMOND_AXE(JobType.LUMBERJACK, FeatureType.ITEM, "Diamond Axe", "Unlock diamond axes", 20, Items.DIAMOND_AXE, Items.DIAMOND_AXE),
    LUMBER_NETHERITE_AXE(JobType.LUMBERJACK, FeatureType.ITEM, "Netherite Axe", "Unlock netherite axes", 30, Items.NETHERITE_AXE, Items.NETHERITE_AXE),

    // BLOCKS
    LUMBER_SCAFFOLDING(JobType.LUMBERJACK, FeatureType.BLOCK, "Scaffolding", "Craft recipe unlocked", 40, Items.SCAFFOLDING, Blocks.SCAFFOLDING),
    LUMBER_CARTOGRAPHY_TABLE(JobType.LUMBERJACK, FeatureType.BLOCK, "Cartography Table", "Craft recipe unlocked", 50, Items.CARTOGRAPHY_TABLE, Blocks.CARTOGRAPHY_TABLE),

    // ========================================
    // HUNTER FEATURES
    // ========================================
    // ITEMS
    HUNTER_IRON_SWORD(JobType.HUNTER, FeatureType.ITEM, "Iron Sword", "Unlock iron swords", 10, Items.IRON_SWORD, Items.IRON_SWORD),
    HUNTER_BOW(JobType.HUNTER, FeatureType.ITEM, "Bow", "Can use bows", 20, Items.BOW, Items.BOW),
    HUNTER_CROSSBOW(JobType.HUNTER, FeatureType.ITEM, "Crossbow", "Craft recipe unlocked", 30, Items.CROSSBOW, Items.CROSSBOW),
    HUNTER_TRIDENT(JobType.HUNTER, FeatureType.ITEM, "Trident", "Can use tridents", 40, Items.TRIDENT, Items.TRIDENT),

    // EFFECTS
    HUNTER_NIGHT_VISION(JobType.HUNTER, FeatureType.EFFECT, "Night Vision", "Permanent night vision effect", 50, Items.SPIDER_EYE, MobEffects.NIGHT_VISION),

    // ========================================
    // FISHER FEATURES
    // ========================================
    // ITEMS
    FISHER_FISHING_ROD(JobType.FISHER, FeatureType.ITEM, "Fishing Rod", "Craft recipe unlocked", 10, Items.FISHING_ROD, Items.FISHING_ROD),
    FISHER_BUCKET(JobType.FISHER, FeatureType.ITEM, "Water Bucket", "Can use buckets", 20, Items.WATER_BUCKET, Items.WATER_BUCKET),
    FISHER_BOAT(JobType.FISHER, FeatureType.ITEM, "Boat", "Craft recipe unlocked", 30, Items.OAK_BOAT, Items.OAK_BOAT),
    FISHER_HEART_OF_SEA(JobType.FISHER, FeatureType.ITEM, "Heart of the Sea", "Can find in treasures", 40, Items.HEART_OF_THE_SEA, Items.HEART_OF_THE_SEA),

    // BLOCKS
    FISHER_CONDUIT(JobType.FISHER, FeatureType.BLOCK, "Conduit", "Craft recipe unlocked", 50, Items.CONDUIT, Blocks.CONDUIT);

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

    private final JobType jobType;
    private final FeatureType featureType;
    private final String name;
    private final String description;
    private final int requiredLevel;
    private final net.minecraft.world.item.Item displayItem; // Pour l'affichage GUI
    private final Object data; // La donnée concrète: Item, Block, String, MobEffect, etc.

    JobFeature(JobType jobType, FeatureType featureType, String name, String description, int requiredLevel,
               net.minecraft.world.item.Item displayItem, Object data) {
        this.jobType = jobType;
        this.featureType = featureType;
        this.name = name;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.displayItem = displayItem;
        this.data = data;
    }

    public JobType getJobType() { return jobType; }
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

    public boolean isUnlocked(int playerLevel) {
        return playerLevel >= requiredLevel;
    }

    /**
     * Obtient toutes les features pour un type de job donné
     */
    public static JobFeature[] getFeaturesForJob(JobType jobType) {
        java.util.List<JobFeature> features = new java.util.ArrayList<>();
        for (JobFeature feature : values()) {
            if (feature.jobType == jobType) {
                features.add(feature);
            }
        }
        return features.toArray(new JobFeature[0]);
    }

    /**
     * Trouve une feature spécifique basée sur le job, le type de feature et les données
     *
     * @param jobType Le type de job (MINER, FARMER, etc.)
     * @param featureType Le type de feature (ITEM, BLOCK, DIMENSION, etc.)
     * @param data Les données de la feature (Item, Block, String pour dimension, etc.)
     * @return La feature correspondante, ou null si non trouvée
     */
    public static JobFeature findFeature(JobType jobType, FeatureType featureType, Object data) {
        for (JobFeature feature : values()) {
            if (feature.jobType == jobType && feature.featureType == featureType) {
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
    public static JobFeature findFeatureByItem(JobType jobType, net.minecraft.world.item.Item item) {
        return findFeature(jobType, FeatureType.ITEM, item);
    }

    /**
     * Trouve une feature par son bloc
     */
    public static JobFeature findFeatureByBlock(JobType jobType, net.minecraft.world.level.block.Block block) {
        return findFeature(jobType, FeatureType.BLOCK, block);
    }

    /**
     * Trouve une feature par dimension
     */
    public static JobFeature findFeatureByDimension(JobType jobType, String dimension) {
        return findFeature(jobType, FeatureType.DIMENSION, dimension);
    }

    /**
     * Trouve une feature par effet
     */
    public static JobFeature findFeatureByEffect(JobType jobType, net.minecraft.world.effect.MobEffect effect) {
        return findFeature(jobType, FeatureType.EFFECT, effect);
    }

    /**
     * Trouve une feature par enchantement
     */
    public static JobFeature findFeatureByEnchantment(JobType jobType, ResourceLocation enchantment) {
        return findFeature(jobType, FeatureType.ENCHANT, enchantment);
    }

    /**
     * Trouve une feature par abilité
     */
    public static JobFeature findFeatureByAbility(JobType jobType, String ability) {
        return findFeature(jobType, FeatureType.ABILITY, ability);
    }

    /**
     * Vérifie si une feature spécifique est débloquée pour un niveau donné
     */
    public static boolean isFeatureUnlocked(JobType jobType, FeatureType featureType, Object data, int playerLevel) {
        JobFeature feature = findFeature(jobType, featureType, data);
        if (feature == null) {
            return false;
        }
        return feature.isUnlocked(playerLevel);
    }

    /**
     * Obtient le niveau requis pour une feature spécifique
     */
    public static int getRequiredLevelForFeature(JobType jobType, FeatureType featureType, Object data) {
        JobFeature feature = findFeature(jobType, featureType, data);
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
