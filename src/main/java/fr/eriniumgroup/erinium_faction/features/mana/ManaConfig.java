package fr.eriniumgroup.erinium_faction.features.mana;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ManaConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue BASE_MAX_MANA;
    public static final ModConfigSpec.DoubleValue BASE_REGEN_PER_SEC;
    public static final ModConfigSpec.DoubleValue MAX_PER_INT_POINT;
    public static final ModConfigSpec.DoubleValue REGEN_PER_INT_POINT;
    public static final ModConfigSpec.IntValue TICK_REGEN_INTERVAL;
    public static final ModConfigSpec.IntValue OUT_OF_COMBAT_TICKS;

    static {
        BUILDER.push("Mana");
        BASE_MAX_MANA = BUILDER.comment("Base max mana for all players").defineInRange("baseMaxMana", 100.0, 0.0, 100000.0);
        BASE_REGEN_PER_SEC = BUILDER.comment("Base mana regen per second").defineInRange("baseRegenPerSec", 2.0, 0.0, 10000.0);
        MAX_PER_INT_POINT = BUILDER.comment("Additional max mana per Intelligence point").defineInRange("maxPerIntPoint", 10.0, 0.0, 1000.0);
        REGEN_PER_INT_POINT = BUILDER.comment("Additional mana regen per second per Intelligence point").defineInRange("regenPerIntPoint", 0.2, 0.0, 1000.0);
        TICK_REGEN_INTERVAL = BUILDER.comment("Regen period in ticks (e.g., 20 = once per second)").defineInRange("tickRegenInterval", 20, 1, 1200);
        OUT_OF_COMBAT_TICKS = BUILDER.comment("Ticks since last damage before full regen applies").defineInRange("outOfCombatTicks", 100, 0, 12000);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}