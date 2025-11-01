package fr.eriniumgroup.erinium_faction.gui.jobs;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Énumération des actions qui donnent de l'XP pour les jobs
 * Chaque action contient la "CHOSE" spécifique selon son type:
 * - BREAK → Block
 * - PLACE → Block
 * - CRAFT → Item
 * - SMELT → Item
 * - KILL → EntityType
 * - BREED → EntityType
 * - FISH → Item
 * - TAME → EntityType
 * - ENCHANT → Item
 * - TRADE → Item (le trade output)
 */
public enum JobXPAction {
    // ========================================
    // MINER - BREAK BLOCKS
    // ========================================
    MINE_STONE(JobType.MINER, ActionType.BREAK, "Mine Stone", "Break stone blocks", Blocks.STONE, 5, 1, 99, 0x10b981),
    MINE_COAL(JobType.MINER, ActionType.BREAK, "Mine Coal Ore", "Common ore", Blocks.COAL_ORE, 10, 1, 99, 0x10b981),
    MINE_IRON(JobType.MINER, ActionType.BREAK, "Mine Iron Ore", "Standard ore", Blocks.IRON_ORE, 15, 5, 99, 0xfbbf24),
    MINE_GOLD(JobType.MINER, ActionType.BREAK, "Mine Gold Ore", "Rare ore", Blocks.GOLD_ORE, 25, 10, 99, 0xfbbf24),
    MINE_DIAMOND(JobType.MINER, ActionType.BREAK, "Mine Diamond Ore", "Very rare ore", Blocks.DIAMOND_ORE, 50, 10, 99, 0xfbbf24),
    MINE_ANCIENT_DEBRIS(JobType.MINER, ActionType.BREAK, "Mine Ancient Debris", "Ultimate rare find", Blocks.ANCIENT_DEBRIS, 100, 20, 99, 0xa855f7),
    MINE_REINFORCED_DEEPSLATE(JobType.MINER, ActionType.BREAK, "Mine Reinforced Deepslate", "Locked - Requires Level 50", Blocks.REINFORCED_DEEPSLATE, 200, 50, 99, 0xef4444),
    MINE_DIRT(JobType.MINER, ActionType.BREAK, "Mine Dirt", "Too easy - no longer gives XP", Blocks.DIRT, 1, 1, 15, 0x6b7280),

    // ========================================
    // FARMER - BREAK BLOCKS (crops)
    // ========================================
    HARVEST_WHEAT(JobType.FARMER, ActionType.BREAK, "Harvest Wheat", "Basic crop", Blocks.WHEAT, 5, 1, 99, 0x10b981),
    HARVEST_CARROT(JobType.FARMER, ActionType.BREAK, "Harvest Carrot", "Basic crop", Blocks.CARROTS, 5, 1, 99, 0x10b981),
    HARVEST_POTATO(JobType.FARMER, ActionType.BREAK, "Harvest Potato", "Basic crop", Blocks.POTATOES, 5, 1, 99, 0x10b981),
    HARVEST_BEETROOT(JobType.FARMER, ActionType.BREAK, "Harvest Beetroot", "Standard crop", Blocks.BEETROOTS, 8, 5, 99, 0x10b981),
    HARVEST_MELON(JobType.FARMER, ActionType.BREAK, "Harvest Melon", "Advanced crop", Blocks.MELON, 12, 15, 99, 0xfbbf24),
    HARVEST_PUMPKIN(JobType.FARMER, ActionType.BREAK, "Harvest Pumpkin", "Advanced crop", Blocks.PUMPKIN, 12, 15, 99, 0xfbbf24),

    // ========================================
    // FARMER - BREED ENTITIES
    // ========================================
    BREED_COW(JobType.FARMER, ActionType.BREED, "Breed Cow", "Animal farming", EntityType.COW, 15, 10, 99, 0xfbbf24),
    BREED_PIG(JobType.FARMER, ActionType.BREED, "Breed Pig", "Animal farming", EntityType.PIG, 15, 10, 99, 0xfbbf24),
    BREED_SHEEP(JobType.FARMER, ActionType.BREED, "Breed Sheep", "Animal farming", EntityType.SHEEP, 15, 10, 99, 0xfbbf24),
    BREED_CHICKEN(JobType.FARMER, ActionType.BREED, "Breed Chicken", "Animal farming", EntityType.CHICKEN, 12, 5, 99, 0x10b981),

    // ========================================
    // LUMBERJACK - BREAK BLOCKS (logs)
    // ========================================
    CHOP_OAK(JobType.LUMBERJACK, ActionType.BREAK, "Chop Oak Log", "Basic wood", Blocks.OAK_LOG, 5, 1, 99, 0x10b981),
    CHOP_BIRCH(JobType.LUMBERJACK, ActionType.BREAK, "Chop Birch Log", "Basic wood", Blocks.BIRCH_LOG, 5, 1, 99, 0x10b981),
    CHOP_SPRUCE(JobType.LUMBERJACK, ActionType.BREAK, "Chop Spruce Log", "Basic wood", Blocks.SPRUCE_LOG, 5, 1, 99, 0x10b981),
    CHOP_JUNGLE(JobType.LUMBERJACK, ActionType.BREAK, "Chop Jungle Log", "Large trees", Blocks.JUNGLE_LOG, 8, 5, 99, 0x10b981),
    CHOP_ACACIA(JobType.LUMBERJACK, ActionType.BREAK, "Chop Acacia Log", "Standard wood", Blocks.ACACIA_LOG, 7, 5, 99, 0x10b981),
    CHOP_DARK_OAK(JobType.LUMBERJACK, ActionType.BREAK, "Chop Dark Oak Log", "Large trees", Blocks.DARK_OAK_LOG, 10, 10, 99, 0xfbbf24),
    CHOP_MANGROVE(JobType.LUMBERJACK, ActionType.BREAK, "Chop Mangrove Log", "Rare wood", Blocks.MANGROVE_LOG, 12, 15, 99, 0xfbbf24),
    CHOP_CHERRY(JobType.LUMBERJACK, ActionType.BREAK, "Chop Cherry Log", "Rare wood", Blocks.CHERRY_LOG, 15, 20, 99, 0xa855f7),

    // ========================================
    // HUNTER - KILL ENTITIES
    // ========================================
    KILL_ZOMBIE(JobType.HUNTER, ActionType.KILL, "Kill Zombie", "Basic hostile mob", EntityType.ZOMBIE, 10, 1, 99, 0x10b981),
    KILL_SKELETON(JobType.HUNTER, ActionType.KILL, "Kill Skeleton", "Basic hostile mob", EntityType.SKELETON, 10, 1, 99, 0x10b981),
    KILL_CREEPER(JobType.HUNTER, ActionType.KILL, "Kill Creeper", "Dangerous mob", EntityType.CREEPER, 15, 5, 99, 0xfbbf24),
    KILL_SPIDER(JobType.HUNTER, ActionType.KILL, "Kill Spider", "Basic hostile mob", EntityType.SPIDER, 10, 1, 99, 0x10b981),
    KILL_ENDERMAN(JobType.HUNTER, ActionType.KILL, "Kill Enderman", "Rare mob", EntityType.ENDERMAN, 25, 10, 99, 0xfbbf24),
    KILL_BLAZE(JobType.HUNTER, ActionType.KILL, "Kill Blaze", "Nether mob", EntityType.BLAZE, 30, 15, 99, 0xa855f7),
    KILL_WITHER_SKELETON(JobType.HUNTER, ActionType.KILL, "Kill Wither Skeleton", "Rare nether mob", EntityType.WITHER_SKELETON, 40, 20, 99, 0xa855f7),
    KILL_ENDER_DRAGON(JobType.HUNTER, ActionType.KILL, "Kill Ender Dragon", "Boss - Requires Level 50", EntityType.ENDER_DRAGON, 500, 50, 99, 0xef4444),

    // ========================================
    // FISHER - FISH ITEMS
    // ========================================
    CATCH_COD(JobType.FISHER, ActionType.FISH, "Catch Cod", "Common fish", Items.COD, 5, 1, 99, 0x10b981),
    CATCH_SALMON(JobType.FISHER, ActionType.FISH, "Catch Salmon", "Common fish", Items.SALMON, 5, 1, 99, 0x10b981),
    CATCH_TROPICAL_FISH(JobType.FISHER, ActionType.FISH, "Catch Tropical Fish", "Uncommon fish", Items.TROPICAL_FISH, 10, 5, 99, 0x10b981),
    CATCH_PUFFERFISH(JobType.FISHER, ActionType.FISH, "Catch Pufferfish", "Uncommon fish", Items.PUFFERFISH, 10, 5, 99, 0x10b981),
    CATCH_TREASURE(JobType.FISHER, ActionType.FISH, "Catch Treasure", "Rare catch", Items.NAME_TAG, 25, 10, 99, 0xfbbf24),
    CATCH_ENCHANTED_BOOK(JobType.FISHER, ActionType.FISH, "Catch Enchanted Book", "Very rare", Items.ENCHANTED_BOOK, 50, 20, 99, 0xa855f7),
    CATCH_SADDLE(JobType.FISHER, ActionType.FISH, "Catch Saddle", "Rare treasure", Items.SADDLE, 30, 15, 99, 0xfbbf24);

    /**
     * Type d'action pour gagner de l'XP
     */
    public enum ActionType {
        BREAK,   // Casser un bloc → target = Block
        PLACE,   // Placer un bloc → target = Block
        CRAFT,   // Crafter un item → target = Item
        SMELT,   // Fondre un item → target = Item
        KILL,    // Tuer une entité → target = EntityType
        BREED,   // Reproduire une entité → target = EntityType
        FISH,    // Pêcher un item → target = Item
        TAME,    // Apprivoiser une entité → target = EntityType
        ENCHANT, // Enchanter un item → target = Item
        TRADE    // Commerce avec un villageois → target = Item
    }

    private final JobType jobType;
    private final ActionType actionType;
    private final String name;
    private final String description;
    private final Object target; // La "CHOSE" : Block, EntityType, ou Item selon actionType
    private final int xpAmount;
    private final int minLevel;
    private final int maxLevel;
    private final int color;

    JobXPAction(JobType jobType, ActionType actionType, String name, String description, Object target,
                int xpAmount, int minLevel, int maxLevel, int color) {
        this.jobType = jobType;
        this.actionType = actionType;
        this.name = name;
        this.description = description;
        this.target = target;
        this.xpAmount = xpAmount;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.color = color;
    }

    public JobType getJobType() { return jobType; }
    public ActionType getActionType() { return actionType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Object getTarget() { return target; }
    public int getXpAmount() { return xpAmount; }
    public int getMinLevel() { return minLevel; }
    public int getMaxLevel() { return maxLevel; }
    public int getColor() { return color; }

    /**
     * Récupère la cible typée selon le type d'action
     */
    public net.minecraft.world.level.block.Block asBlock() {
        return (actionType == ActionType.BREAK || actionType == ActionType.PLACE)
            ? (net.minecraft.world.level.block.Block) target : null;
    }

    public net.minecraft.world.item.Item asItem() {
        return (actionType == ActionType.CRAFT || actionType == ActionType.SMELT ||
                actionType == ActionType.FISH || actionType == ActionType.ENCHANT || actionType == ActionType.TRADE)
            ? (net.minecraft.world.item.Item) target : null;
    }

    public EntityType<?> asEntityType() {
        return (actionType == ActionType.KILL || actionType == ActionType.BREED || actionType == ActionType.TAME)
            ? (EntityType<?>) target : null;
    }

    public boolean isAvailable(int playerLevel) {
        return playerLevel >= minLevel && playerLevel <= maxLevel;
    }

    /**
     * Obtient toutes les actions XP pour un type de job donné
     */
    public static JobXPAction[] getActionsForJob(JobType jobType) {
        java.util.List<JobXPAction> actions = new java.util.ArrayList<>();
        for (JobXPAction action : values()) {
            if (action.jobType == jobType) {
                actions.add(action);
            }
        }
        return actions.toArray(new JobXPAction[0]);
    }

    /**
     * Trouve une action XP spécifique basée sur le job, l'action et la cible
     *
     * @param jobType Le type de job (MINER, FARMER, etc.)
     * @param actionType Le type d'action (BREAK, KILL, FISH, etc.)
     * @param target La cible (Block, EntityType, ou Item selon actionType)
     * @return L'action XP correspondante, ou null si non trouvée
     */
    public static JobXPAction findAction(JobType jobType, ActionType actionType, Object target) {
        for (JobXPAction action : values()) {
            if (action.jobType == jobType &&
                action.actionType == actionType &&
                action.target.equals(target)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Trouve une action pour BREAK
     */
    public static JobXPAction findBreakAction(JobType jobType, net.minecraft.world.level.block.Block block) {
        return findAction(jobType, ActionType.BREAK, block);
    }

    /**
     * Trouve une action pour PLACE
     */
    public static JobXPAction findPlaceAction(JobType jobType, net.minecraft.world.level.block.Block block) {
        return findAction(jobType, ActionType.PLACE, block);
    }

    /**
     * Trouve une action pour KILL
     */
    public static JobXPAction findKillAction(JobType jobType, EntityType<?> entityType) {
        return findAction(jobType, ActionType.KILL, entityType);
    }

    /**
     * Trouve une action pour BREED
     */
    public static JobXPAction findBreedAction(JobType jobType, EntityType<?> entityType) {
        return findAction(jobType, ActionType.BREED, entityType);
    }

    /**
     * Trouve une action pour FISH
     */
    public static JobXPAction findFishAction(JobType jobType, net.minecraft.world.item.Item item) {
        return findAction(jobType, ActionType.FISH, item);
    }

    /**
     * Trouve une action pour CRAFT
     */
    public static JobXPAction findCraftAction(JobType jobType, net.minecraft.world.item.Item item) {
        return findAction(jobType, ActionType.CRAFT, item);
    }

    /**
     * Trouve une action pour SMELT
     */
    public static JobXPAction findSmeltAction(JobType jobType, net.minecraft.world.item.Item item) {
        return findAction(jobType, ActionType.SMELT, item);
    }

    /**
     * Vérifie si une action spécifique existe et est disponible pour un niveau donné
     */
    public static boolean canPerformAction(JobType jobType, ActionType actionType, Object target, int playerLevel) {
        JobXPAction action = findAction(jobType, actionType, target);
        if (action == null) {
            return false;
        }
        return action.isAvailable(playerLevel);
    }

    /**
     * Obtient la quantité d'XP pour une action spécifique
     */
    public static int getXPForAction(JobType jobType, ActionType actionType, Object target) {
        JobXPAction action = findAction(jobType, actionType, target);
        return action != null ? action.xpAmount : 0;
    }
}
