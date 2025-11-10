package fr.eriniumgroup.erinium_faction.features.combatlog;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration pour le système de combat logging
 */
public class CombatLogConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue COMBAT_TAG_DURATION;
    public static final ModConfigSpec.IntValue COMBAT_TAG_DURATION_IN_TERRITORY;
    public static final ModConfigSpec.BooleanValue ENABLE_COMBAT_LOG;
    public static final ModConfigSpec.BooleanValue TAG_ON_PVE;
    public static final ModConfigSpec.BooleanValue SHOW_ACTION_BAR;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_COMMANDS;

    static {
        BUILDER.push("Combat Logging System");

        ENABLE_COMBAT_LOG = BUILDER
                .comment("Activer le système de combat logging")
                .define("enableCombatLog", true);

        COMBAT_TAG_DURATION = BUILDER
                .comment("Durée du combat tag en secondes (territoire neutre)")
                .defineInRange("combatTagDuration", 15, 5, 60);

        COMBAT_TAG_DURATION_IN_TERRITORY = BUILDER
                .comment("Durée du combat tag en secondes (dans son territoire)")
                .defineInRange("combatTagDurationInTerritory", 10, 5, 60);

        TAG_ON_PVE = BUILDER
                .comment("Activer le combat tag sur les combats PvE (contre les mobs)")
                .define("tagOnPvE", false);

        SHOW_ACTION_BAR = BUILDER
                .comment("Afficher le timer dans la barre d'action")
                .define("showActionBar", true);

        BLOCKED_COMMANDS = BUILDER
                .comment("Liste des commandes bloquées pendant le combat (sans le /)",
                        "Exemple: kill, tp, home, spawn, back, warp, etc.")
                .defineListAllowEmpty(
                        Arrays.asList("blockedCommands"),
                        () -> Arrays.asList("kill", "suicide", "tp", "teleport", "home", "spawn", "back", "warp", "tpa", "tpaccept"),
                        obj -> obj instanceof String
                );

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
