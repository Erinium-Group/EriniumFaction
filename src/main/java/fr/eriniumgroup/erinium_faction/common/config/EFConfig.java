package fr.eriniumgroup.erinium_faction.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class EFConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue FACTION_NAME_MIN;
    public static final ModConfigSpec.IntValue FACTION_NAME_MAX;
    public static final ModConfigSpec.IntValue FACTION_MAX_MEMBERS;

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

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("factions");
        FACTION_NAME_MIN = b.comment("Longueur minimale du nom de faction").defineInRange("nameMin", 3, 2, 32);
        FACTION_NAME_MAX = b.comment("Longueur maximale du nom de faction").defineInRange("nameMax", 16, 3, 32);
        FACTION_MAX_MEMBERS = b.comment("Nombre maximum de membres par faction").defineInRange("maxMembers", 20, 1, 200);

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
