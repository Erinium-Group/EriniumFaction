package fr.eriniumgroup.erinium_faction.gui.jobs;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Énumération des actions qui donnent de l'XP
 * Chaque action contient:
 * - ActionType: le type d'action (BREAK, KILL, etc.)
 * - target: la "CHOSE" (Block, EntityType, ou Item)
 * - xpAmount: quantité d'XP donnée
 * - minLevel: niveau minimum pour effectuer l'action (optionnel, -1 = pas de minimum)
 * - maxLevel: niveau maximum pour effectuer l'action (optionnel, -1 = pas de maximum)
 * - color: couleur d'affichage
 */
public enum JobXPAction {
    // ========================================
    // BREAK BLOCKS - Mining
    // ========================================
    MINE_STONE("Mine Stone", "Mine basic stone blocks for building and smelting", ActionType.BREAK, Blocks.STONE, 5, 1, 20, 0x10b981),
    MINE_DEEPSLATE("Mine Deepslate", "Mine harder deepslate found in lower depths", ActionType.BREAK, Blocks.DEEPSLATE, 6, 1, 20, 0x10b981),
    MINE_COAL("Mine Coal Ore", "Mine common coal ore for fuel and torches", ActionType.BREAK, Blocks.COAL_ORE, 10, 1, 30, 0x10b981),
    MINE_COPPER("Mine Copper Ore", "Mine copper ore for building and lightning rods", ActionType.BREAK, Blocks.COPPER_ORE, 12, 5, 35, 0x10b981),
    MINE_IRON("Mine Iron Ore", "Mine iron ore for tools, weapons, and armor", ActionType.BREAK, Blocks.IRON_ORE, 20, 10, 50, 0xfbbf24),
    MINE_GOLD("Mine Gold Ore", "Mine valuable gold ore for powered rails and crafting", ActionType.BREAK, Blocks.GOLD_ORE, 30, 15, 60, 0xfbbf24),
    MINE_LAPIS("Mine Lapis Ore", "Mine lapis lazuli ore used for enchanting", ActionType.BREAK, Blocks.LAPIS_ORE, 25, 10, 50, 0xfbbf24),
    MINE_REDSTONE("Mine Redstone Ore", "Mine redstone ore for complex mechanisms and circuits", ActionType.BREAK, Blocks.REDSTONE_ORE, 25, 10, 55, 0xfbbf24),
    MINE_DIAMOND("Mine Diamond Ore", "Mine rare diamond ore for the best equipment", ActionType.BREAK, Blocks.DIAMOND_ORE, 50, 20, 70, 0xa855f7),
    MINE_EMERALD("Mine Emerald Ore", "Mine extremely rare emerald ore for villager trading", ActionType.BREAK, Blocks.EMERALD_ORE, 60, 25, 75, 0xa855f7),
    MINE_ANCIENT_DEBRIS("Mine Ancient Debris", "Mine the rarest Nether ore for netherite equipment", ActionType.BREAK, Blocks.ANCIENT_DEBRIS, 100, 30, -1, 0xef4444),
    MINE_REINFORCED_DEEPSLATE("Mine Reinforced Deepslate", "Mine the hardest block in the game", ActionType.BREAK, Blocks.REINFORCED_DEEPSLATE, 200, 50, -1, 0xef4444),

    // ========================================
    // BREAK BLOCKS - Farming
    // ========================================
    HARVEST_WHEAT("Harvest Wheat", "Harvest wheat crops for bread and animal breeding", ActionType.BREAK, Blocks.WHEAT, 5, 1, 25, 0x10b981),
    HARVEST_CARROT("Harvest Carrot", "Harvest carrots for food and breeding pigs", ActionType.BREAK, Blocks.CARROTS, 5, 1, 25, 0x10b981),
    HARVEST_POTATO("Harvest Potato", "Harvest potatoes for food and baked potatoes", ActionType.BREAK, Blocks.POTATOES, 5, 1, 25, 0x10b981),
    HARVEST_BEETROOT("Harvest Beetroot", "Harvest beetroots for food and red dye", ActionType.BREAK, Blocks.BEETROOTS, 8, 5, 30, 0x10b981),
    HARVEST_MELON("Harvest Melon", "Harvest melons for food and glistering melons", ActionType.BREAK, Blocks.MELON, 12, 10, 40, 0xfbbf24),
    HARVEST_PUMPKIN("Harvest Pumpkin", "Harvest pumpkins for pies and golems", ActionType.BREAK, Blocks.PUMPKIN, 12, 10, 40, 0xfbbf24),
    HARVEST_SUGAR_CANE("Harvest Sugar Cane", "Harvest sugar cane for paper and sugar", ActionType.BREAK, Blocks.SUGAR_CANE, 3, 1, 20, 0x10b981),
    HARVEST_CACTUS("Harvest Cactus", "Harvest cactus for green dye and traps", ActionType.BREAK, Blocks.CACTUS, 3, 1, 20, 0x10b981),
    HARVEST_BAMBOO("Harvest Bamboo", "Harvest fast-growing bamboo for building and scaffolding", ActionType.BREAK, Blocks.BAMBOO, 2, 1, 15, 0x10b981),
    HARVEST_NETHER_WART("Harvest Nether Wart", "Harvest Nether crops essential for potion brewing", ActionType.BREAK, Blocks.NETHER_WART, 15, 15, 50, 0xfbbf24),

    // ========================================
    // BREAK BLOCKS - Lumberjack
    // ========================================
    CHOP_OAK("Chop Oak Log", "Chop common oak wood for basic building and crafting", ActionType.BREAK, Blocks.OAK_LOG, 5, 1, 20, 0x10b981),
    CHOP_BIRCH("Chop Birch Log", "Chop birch wood with bright white planks", ActionType.BREAK, Blocks.BIRCH_LOG, 5, 1, 20, 0x10b981),
    CHOP_SPRUCE("Chop Spruce Log", "Chop spruce wood for dark brown builds", ActionType.BREAK, Blocks.SPRUCE_LOG, 5, 1, 20, 0x10b981),
    CHOP_JUNGLE("Chop Jungle Log", "Chop large jungle trees for unique wood", ActionType.BREAK, Blocks.JUNGLE_LOG, 8, 5, 30, 0x10b981),
    CHOP_ACACIA("Chop Acacia Log", "Chop distinctive orange-tinted acacia wood", ActionType.BREAK, Blocks.ACACIA_LOG, 7, 5, 30, 0x10b981),
    CHOP_DARK_OAK("Chop Dark Oak Log", "Chop dark oak from large trees for rich wood", ActionType.BREAK, Blocks.DARK_OAK_LOG, 10, 10, 35, 0xfbbf24),
    CHOP_MANGROVE("Chop Mangrove Log", "Chop rare mangrove wood from swamp trees", ActionType.BREAK, Blocks.MANGROVE_LOG, 12, 15, 40, 0xfbbf24),
    CHOP_CHERRY("Chop Cherry Log", "Chop beautiful pink cherry wood from grove trees", ActionType.BREAK, Blocks.CHERRY_LOG, 15, 20, 50, 0xa855f7),
    CHOP_CRIMSON_STEM("Chop Crimson Stem", "Chop crimson fungi stems from the Nether", ActionType.BREAK, Blocks.CRIMSON_STEM, 20, 20, 60, 0xa855f7),
    CHOP_WARPED_STEM("Chop Warped Stem", "Chop warped fungi stems from the Nether", ActionType.BREAK, Blocks.WARPED_STEM, 20, 20, 60, 0xa855f7),

    // ========================================
    // KILL ENTITIES - Hostile mobs
    // ========================================
    KILL_ZOMBIE("Kill Zombie", "Defeat common undead zombies that spawn at night", ActionType.KILL, EntityType.ZOMBIE, 10, 1, 25, 0x10b981),
    KILL_SKELETON("Kill Skeleton", "Defeat arrow-shooting skeletons for bones and arrows", ActionType.KILL, EntityType.SKELETON, 10, 1, 25, 0x10b981),
    KILL_CREEPER("Kill Creeper", "Defeat explosive creepers without getting blown up", ActionType.KILL, EntityType.CREEPER, 15, 5, 30, 0xfbbf24),
    KILL_SPIDER("Kill Spider", "Defeat wall-climbing spiders for string and eyes", ActionType.KILL, EntityType.SPIDER, 10, 1, 25, 0x10b981),
    KILL_CAVE_SPIDER("Kill Cave Spider", "Defeat poisonous spiders found in mineshafts", ActionType.KILL, EntityType.CAVE_SPIDER, 15, 5, 30, 0xfbbf24),
    KILL_ENDERMAN("Kill Enderman", "Defeat teleporting endermen for ender pearls", ActionType.KILL, EntityType.ENDERMAN, 25, 10, 40, 0xfbbf24),
    KILL_WITCH("Kill Witch", "Defeat potion-throwing witches in swamp huts", ActionType.KILL, EntityType.WITCH, 20, 10, 35, 0xfbbf24),
    KILL_ZOMBIE_VILLAGER("Kill Zombie Villager", "Defeat infected villagers or cure them first", ActionType.KILL, EntityType.ZOMBIE_VILLAGER, 12, 5, 30, 0x10b981),
    KILL_DROWNED("Kill Drowned", "Defeat underwater zombies that drop tridents", ActionType.KILL, EntityType.DROWNED, 15, 10, 35, 0xfbbf24),
    KILL_HUSK("Kill Husk", "Defeat desert zombies that inflict hunger", ActionType.KILL, EntityType.HUSK, 12, 5, 30, 0x10b981),
    KILL_STRAY("Kill Stray", "Defeat frozen skeletons that shoot slowness arrows", ActionType.KILL, EntityType.STRAY, 12, 5, 30, 0x10b981),
    KILL_PHANTOM("Kill Phantom", "Defeat flying phantoms that attack sleep-deprived players", ActionType.KILL, EntityType.PHANTOM, 18, 10, 35, 0xfbbf24),
    KILL_SLIME("Kill Slime", "Defeat bouncing slimes found in swamps and caves", ActionType.KILL, EntityType.SLIME, 8, 1, 25, 0x10b981),

    // Nether mobs
    KILL_BLAZE("Kill Blaze", "Defeat fire-shooting blazes in Nether fortresses", ActionType.KILL, EntityType.BLAZE, 30, 15, 50, 0xa855f7),
    KILL_WITHER_SKELETON("Kill Wither Skeleton", "Defeat dangerous wither skeletons for wither skulls", ActionType.KILL, EntityType.WITHER_SKELETON, 40, 20, 60, 0xa855f7),
    KILL_GHAST("Kill Ghast", "Defeat flying ghasts that shoot explosive fireballs", ActionType.KILL, EntityType.GHAST, 35, 15, 50, 0xa855f7),
    KILL_MAGMA_CUBE("Kill Magma Cube", "Defeat bouncing magma cubes in the Nether", ActionType.KILL, EntityType.MAGMA_CUBE, 15, 10, 35, 0xfbbf24),
    KILL_PIGLIN("Kill Piglin", "Defeat piglins but beware of angering the horde", ActionType.KILL, EntityType.PIGLIN, 20, 15, 40, 0xfbbf24),
    KILL_PIGLIN_BRUTE("Kill Piglin Brute", "Defeat powerful piglin brutes guarding bastions", ActionType.KILL, EntityType.PIGLIN_BRUTE, 35, 20, 50, 0xa855f7),
    KILL_HOGLIN("Kill Hoglin", "Defeat aggressive hoglin beasts for porkchops", ActionType.KILL, EntityType.HOGLIN, 25, 15, 45, 0xfbbf24),

    // End mobs
    KILL_ENDERMITE("Kill Endermite", "Defeat rare endermites that spawn from ender pearls", ActionType.KILL, EntityType.ENDERMITE, 20, 15, 40, 0xfbbf24),
    KILL_SHULKER("Kill Shulker", "Defeat shulkers in End cities for levitation shells", ActionType.KILL, EntityType.SHULKER, 50, 30, 70, 0xa855f7),

    // Boss mobs
    KILL_ENDER_DRAGON("Kill Ender Dragon", "Defeat the ultimate boss of the End dimension", ActionType.KILL, EntityType.ENDER_DRAGON, 500, 50, -1, 0xef4444),
    KILL_WITHER("Kill Wither", "Defeat the summoned wither boss for a nether star", ActionType.KILL, EntityType.WITHER, 400, 45, -1, 0xef4444),
    KILL_ELDER_GUARDIAN("Kill Elder Guardian", "Defeat ocean monument guardians for sponges", ActionType.KILL, EntityType.ELDER_GUARDIAN, 100, 30, -1, 0xa855f7),
    KILL_WARDEN("Kill Warden", "Defeat the deadliest mob in the deep dark", ActionType.KILL, EntityType.WARDEN, 600, 60, -1, 0xef4444),

    // ========================================
    // BREED ENTITIES - Animal farming
    // ========================================
    BREED_COW("Breed Cow", "Breed cows for renewable leather and beef production", ActionType.BREED, EntityType.COW, 15, 5, 40, 0xfbbf24),
    BREED_PIG("Breed Pig", "Breed pigs for sustainable porkchop farming", ActionType.BREED, EntityType.PIG, 15, 5, 40, 0xfbbf24),
    BREED_SHEEP("Breed Sheep", "Breed sheep for wool and mutton production", ActionType.BREED, EntityType.SHEEP, 15, 5, 40, 0xfbbf24),
    BREED_CHICKEN("Breed Chicken", "Breed chickens for eggs and feathers", ActionType.BREED, EntityType.CHICKEN, 12, 1, 35, 0x10b981),
    BREED_RABBIT("Breed Rabbit", "Breed rabbits for rabbit hide and meat", ActionType.BREED, EntityType.RABBIT, 12, 5, 35, 0x10b981),
    BREED_HORSE("Breed Horse", "Breed horses for faster transportation", ActionType.BREED, EntityType.HORSE, 25, 10, 50, 0xfbbf24),
    BREED_DONKEY("Breed Donkey", "Breed donkeys for chest-equipped transport", ActionType.BREED, EntityType.DONKEY, 25, 10, 50, 0xfbbf24),
    BREED_LLAMA("Breed Llama", "Breed llamas for caravan transportation", ActionType.BREED, EntityType.LLAMA, 20, 10, 45, 0xfbbf24),
    BREED_BEE("Breed Bee", "Breed bees for honey and pollination", ActionType.BREED, EntityType.BEE, 18, 15, 45, 0xfbbf24),
    BREED_FOX("Breed Fox", "Breed foxes for companionship and item gathering", ActionType.BREED, EntityType.FOX, 20, 15, 50, 0xfbbf24),
    BREED_AXOLOTL("Breed Axolotl", "Breed rare axolotls for underwater companionship", ActionType.BREED, EntityType.AXOLOTL, 30, 20, 60, 0xa855f7),
    BREED_FROG("Breed Frog", "Breed frogs for froglight production", ActionType.BREED, EntityType.FROG, 18, 15, 45, 0xfbbf24),

    // ========================================
    // FISH ITEMS
    // ========================================
    CATCH_COD("Catch Cod", "Fish common cod for food and cooking", ActionType.FISH, Items.COD, 5, 1, 25, 0x10b981),
    CATCH_SALMON("Catch Salmon", "Fish salmon for nutritious meals", ActionType.FISH, Items.SALMON, 5, 1, 25, 0x10b981),
    CATCH_TROPICAL_FISH("Catch Tropical Fish", "Fish tropical fish for variety and axolotl food", ActionType.FISH, Items.TROPICAL_FISH, 10, 5, 30, 0x10b981),
    CATCH_PUFFERFISH("Catch Pufferfish", "Fish pufferfish for water breathing potions", ActionType.FISH, Items.PUFFERFISH, 10, 5, 30, 0x10b981),
    CATCH_TREASURE("Catch Treasure", "Fish rare treasure items like name tags", ActionType.FISH, Items.NAME_TAG, 25, 10, -1, 0xfbbf24),
    CATCH_ENCHANTED_BOOK("Catch Enchanted Book", "Fish extremely rare enchanted books", ActionType.FISH, Items.ENCHANTED_BOOK, 50, 15, -1, 0xa855f7),
    CATCH_SADDLE("Catch Saddle", "Fish valuable saddles for riding mounts", ActionType.FISH, Items.SADDLE, 30, 10, -1, 0xfbbf24),
    CATCH_BOW("Catch Bow", "Fish enchanted bows from the depths", ActionType.FISH, Items.BOW, 25, 10, -1, 0xfbbf24);

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

    private final String name;
    private final String description;
    private final ActionType actionType;
    private final Object target; // La "CHOSE" : Block, EntityType, ou Item selon actionType
    private final int xpAmount;
    private final int minLevel; // -1 = pas de minimum
    private final int maxLevel; // -1 = pas de maximum
    private final int color;

    JobXPAction(String name, String description, ActionType actionType, Object target,
                int xpAmount, int minLevel, int maxLevel, int color) {
        this.name = name;
        this.description = description;
        this.actionType = actionType;
        this.target = target;
        this.xpAmount = xpAmount;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.color = color;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ActionType getActionType() { return actionType; }
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

    /**
     * Vérifie si l'action est disponible pour un niveau donné
     */
    public boolean isAvailable(int playerLevel) {
        boolean aboveMin = minLevel == -1 || playerLevel >= minLevel;
        boolean belowMax = maxLevel == -1 || playerLevel <= maxLevel;
        return aboveMin && belowMax;
    }

    /**
     * Trouve une action XP spécifique basée sur l'action et la cible
     */
    public static JobXPAction findAction(ActionType actionType, Object target) {
        for (JobXPAction action : values()) {
            if (action.actionType == actionType && action.target.equals(target)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Trouve une action pour BREAK
     */
    public static JobXPAction findBreakAction(net.minecraft.world.level.block.Block block) {
        return findAction(ActionType.BREAK, block);
    }

    /**
     * Trouve une action pour KILL
     */
    public static JobXPAction findKillAction(EntityType<?> entityType) {
        return findAction(ActionType.KILL, entityType);
    }

    /**
     * Trouve une action pour BREED
     */
    public static JobXPAction findBreedAction(EntityType<?> entityType) {
        return findAction(ActionType.BREED, entityType);
    }

    /**
     * Trouve une action pour FISH
     */
    public static JobXPAction findFishAction(net.minecraft.world.item.Item item) {
        return findAction(ActionType.FISH, item);
    }

    /**
     * Vérifie si une action spécifique existe et est disponible pour un niveau donné
     */
    public static boolean canPerformAction(ActionType actionType, Object target, int playerLevel) {
        JobXPAction action = findAction(actionType, target);
        if (action == null) {
            return false;
        }
        return action.isAvailable(playerLevel);
    }

    /**
     * Obtient la quantité d'XP pour une action spécifique
     */
    public static int getXPForAction(ActionType actionType, Object target, int playerLevel) {
        JobXPAction action = findAction(actionType, target);
        if (action == null || !action.isAvailable(playerLevel)) {
            return 0;
        }
        return action.xpAmount;
    }
}
