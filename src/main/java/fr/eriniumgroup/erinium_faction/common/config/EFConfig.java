package fr.eriniumgroup.erinium_faction.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class EFConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue FACTION_NAME_MIN;
    public static final ModConfigSpec.IntValue FACTION_NAME_MAX;
    public static final ModConfigSpec.IntValue FACTION_MAX_MEMBERS; // plafond global dur

    public static final ModConfigSpec.IntValue FACTION_BASE_MAX_PLAYERS; // base par faction
    public static final ModConfigSpec.IntValue FACTION_PLAYERS_PER_LEVEL; // +N par niveau

    public static final ModConfigSpec.DoubleValue FACTION_BASE_MAX_POWER;
    public static final ModConfigSpec.DoubleValue FACTION_POWER_PER_MEMBER;
    public static final ModConfigSpec.DoubleValue POWER_REGEN_PER_MINUTE;
    public static final ModConfigSpec.DoubleValue POWER_LOSS_ON_DEATH;

    public static final ModConfigSpec.IntValue XP_PER_KILL;
    public static final ModConfigSpec.BooleanValue FRIENDLY_FIRE;
    public static final ModConfigSpec.BooleanValue ALLOW_ALLIES;
    public static final ModConfigSpec.BooleanValue ALLY_DAMAGE;

    // Joueur power
    public static final ModConfigSpec.DoubleValue PLAYER_BASE_MAX_POWER;
    public static final ModConfigSpec.DoubleValue PLAYER_POWER_REGEN_PER_MINUTE;
    public static final ModConfigSpec.DoubleValue PLAYER_POWER_LOSS_ON_DEATH;
    public static final ModConfigSpec.BooleanValue FACTION_MAX_POWER_FROM_PLAYERS;

    public static final ModConfigSpec.IntValue FACTION_MAX_CLAIMS;

    public static final ModConfigSpec.IntValue FACTION_BASE_WARPS;
    public static final ModConfigSpec.IntValue FACTION_WARPS_PER_5_LEVELS;

    public static final ModConfigSpec.IntValue FACTION_TP_WARMUP_SECONDS;
    public static final ModConfigSpec.IntValue FACTION_TP_COOLDOWN_SECONDS;
    public static final ModConfigSpec.BooleanValue FACTION_TP_CANCEL_ON_MOVE;
    public static final ModConfigSpec.BooleanValue FACTION_TP_CANCEL_ON_DAMAGE;
    public static final ModConfigSpec.BooleanValue FACTION_TP_ALLOW_CROSS_DIM;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("factions");
        FACTION_NAME_MIN = b.comment("Longueur minimale du nom de faction").defineInRange("nameMin", 3, 2, 32);
        FACTION_NAME_MAX = b.comment("Longueur maximale du nom de faction").defineInRange("nameMax", 16, 3, 32);
        FACTION_MAX_MEMBERS = b.comment("Plafond global dur du nombre de membres par faction").defineInRange("maxMembers", 30, 1, 2000);
        FACTION_BASE_MAX_PLAYERS = b.comment("Nombre de membres autorisés par faction au niveau 0").defineInRange("baseMaxPlayers", 10, 1, 2000);
        FACTION_PLAYERS_PER_LEVEL = b.comment("Augmentation du nombre de membres autorisés par niveau").defineInRange("playersPerLevel", 1, 0, 2000);

        FACTION_BASE_MAX_POWER = b.comment("Puissance maximale de base par faction").defineInRange("baseMaxPower", 100.0, 0.0, 100000.0);
        FACTION_POWER_PER_MEMBER = b.comment("Bonus de puissance max par membre (non utilisé si FACTION_MAX_POWER_FROM_PLAYERS=true)").defineInRange("powerPerMember", 10.0, 0.0, 10000.0);
        POWER_REGEN_PER_MINUTE = b.comment("Régénération de puissance par minute (faction)").defineInRange("powerRegenPerMinute", 1.0, 0.0, 1000.0);
        POWER_LOSS_ON_DEATH = b.comment("Perte de puissance de faction à la mort d'un membre").defineInRange("powerLossOnDeath", 2.5, 0.0, 1000.0);

        XP_PER_KILL = b.comment("XP de faction gagné par kill").defineInRange("xpPerKill", 10, 0, 10000);
        FRIENDLY_FIRE = b.comment("Le friendly fire au sein d'une faction est-il autorisé ?").define("friendlyFire", false);
        ALLOW_ALLIES = b.comment("Autoriser des alliances (placeholder)").define("allowAllies", true);
        ALLY_DAMAGE = b.comment("Autoriser les dégâts entre alliés").define("allyDamage", false);
        FACTION_MAX_POWER_FROM_PLAYERS = b.comment("Additionner les max power des joueurs au max de faction").define("factionMaxFromPlayers", true);
        FACTION_MAX_CLAIMS = b.comment("Nombre maximum de chunks claim par faction").defineInRange("maxClaims", 100, 0, 100000);
        FACTION_BASE_WARPS = b.comment("Nombre de warps autorisés au niveau 0").defineInRange("baseWarps", 1, 0, 1000);
        FACTION_WARPS_PER_5_LEVELS = b.comment("Warps supplémentaires tous les 5 niveaux").defineInRange("warpsPer5Levels", 1, 0, 1000);
        FACTION_TP_WARMUP_SECONDS = b.comment("Temps de préparation (warmup) avant une TP home/warp, en secondes").defineInRange("tpWarmupSeconds", 3, 0, 300);
        FACTION_TP_COOLDOWN_SECONDS = b.comment("Cooldown entre deux TP home/warp, en secondes").defineInRange("tpCooldownSeconds", 15, 0, 3600);
        FACTION_TP_CANCEL_ON_MOVE = b.comment("Annuler le warmup si le joueur bouge").define("tpCancelOnMove", true);
        FACTION_TP_CANCEL_ON_DAMAGE = b.comment("Annuler le warmup si le joueur prend des dégâts").define("tpCancelOnDamage", true);
        FACTION_TP_ALLOW_CROSS_DIM = b.comment("Autoriser la téléportation inter-dimensionnelle").define("tpAllowCrossDimension", true);
        b.pop();

        b.push("players");
        PLAYER_BASE_MAX_POWER = b.comment("Puissance max de base par joueur").defineInRange("baseMaxPower", 50.0, 0.0, 100000.0);
        PLAYER_POWER_REGEN_PER_MINUTE = b.comment("Régénération de puissance par minute (joueur)").defineInRange("powerRegenPerMinute", 0.5, 0.0, 1000.0);
        PLAYER_POWER_LOSS_ON_DEATH = b.comment("Perte de puissance à la mort (joueur)").defineInRange("powerLossOnDeath", 5.0, 0.0, 1000.0);
        b.pop();

        SPEC = b.build();
    }

    private EFConfig() {}
}
