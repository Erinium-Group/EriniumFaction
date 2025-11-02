package fr.eriniumgroup.erinium_faction.features.level;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration du système de niveau des joueurs
 */
public class PlayerLevelConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Paramètres généraux
    public static final ModConfigSpec.IntValue MAX_LEVEL;
    public static final ModConfigSpec.IntValue BASE_HEALTH;
    public static final ModConfigSpec.IntValue HEARTS_PER_LEVEL_GAIN;
    public static final ModConfigSpec.IntValue LEVELS_BETWEEN_HEARTS;
    public static final ModConfigSpec.IntValue POINTS_PER_LEVEL;

    // Formule d'expérience
    public static final ModConfigSpec.IntValue BASE_EXP_FOR_LEVEL;
    public static final ModConfigSpec.DoubleValue EXP_MULTIPLIER_PER_LEVEL;

    // Bonus par point d'attribut
    public static final ModConfigSpec.DoubleValue HEALTH_BONUS_PER_POINT;
    public static final ModConfigSpec.DoubleValue ARMOR_BONUS_PER_POINT;
    public static final ModConfigSpec.DoubleValue SPEED_BONUS_PER_POINT;
    public static final ModConfigSpec.DoubleValue INTELLIGENCE_BONUS_PER_POINT;
    public static final ModConfigSpec.DoubleValue STRENGTH_BONUS_PER_POINT;
    public static final ModConfigSpec.DoubleValue LUCK_BONUS_PER_POINT;

    static {
        BUILDER.push("Player Level System");

        BUILDER.comment("=== General Settings ===");
        MAX_LEVEL = BUILDER
            .comment("Maximum level a player can reach")
            .defineInRange("maxLevel", 100, 1, 1000);

        BASE_HEALTH = BUILDER
            .comment("Base health for all players in hearts (20 = 10 hearts, 200 = 100 hearts)")
            .defineInRange("baseHealth", 200, 20, 1000);

        HEARTS_PER_LEVEL_GAIN = BUILDER
            .comment("Number of hearts gained every X levels (in hearts, 10 = 5 displayed hearts)")
            .defineInRange("heartsPerLevelGain", 10, 1, 100);

        LEVELS_BETWEEN_HEARTS = BUILDER
            .comment("Number of levels between each heart gain")
            .defineInRange("levelsBetweenHearts", 5, 1, 50);

        POINTS_PER_LEVEL = BUILDER
            .comment("Number of attribute points gained per level")
            .defineInRange("pointsPerLevel", 1, 1, 10);

        BUILDER.comment("=== Experience Formula ===");
        BASE_EXP_FOR_LEVEL = BUILDER
            .comment("Base experience required for level 2")
            .defineInRange("baseExpForLevel", 100, 10, 10000);

        EXP_MULTIPLIER_PER_LEVEL = BUILDER
            .comment("Multiplier applied per level (exp_needed = base * (multiplier ^ (level - 1)))")
            .defineInRange("expMultiplierPerLevel", 1.15, 1.0, 2.0);

        BUILDER.comment("=== Attribute Bonuses ===");
        HEALTH_BONUS_PER_POINT = BUILDER
            .comment("Health bonus per point (in health points, 2 = 1 heart)")
            .defineInRange("healthBonusPerPoint", 2.0, 0.0, 100.0);

        ARMOR_BONUS_PER_POINT = BUILDER
            .comment("Armor bonus per point")
            .defineInRange("armorBonusPerPoint", 0.5, 0.0, 10.0);

        SPEED_BONUS_PER_POINT = BUILDER
            .comment("Speed bonus per point (as percentage, 0.01 = 1%)")
            .defineInRange("speedBonusPerPoint", 0.01, 0.0, 1.0);

        INTELLIGENCE_BONUS_PER_POINT = BUILDER
            .comment("Intelligence bonus per point (reduces ability cooldowns and increases magic damage)")
            .defineInRange("intelligenceBonusPerPoint", 1.0, 0.0, 10.0);

        STRENGTH_BONUS_PER_POINT = BUILDER
            .comment("Strength bonus per point (increases physical damage)")
            .defineInRange("strengthBonusPerPoint", 0.5, 0.0, 10.0);

        LUCK_BONUS_PER_POINT = BUILDER
            .comment("Luck bonus per point")
            .defineInRange("luckBonusPerPoint", 0.5, 0.0, 10.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

