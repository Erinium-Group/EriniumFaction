package fr.eriniumgroup.erinium_faction.features.mana.spell;

import net.minecraft.resources.ResourceLocation;

public class Spell {
    public final ResourceLocation id;
    public final String type; // e.g., fire, water, arcane
    public final int tier; // 1..5, 6 = ultimate
    public final double manaCost;
    public final int cooldownTicks;
    public final String displayKey; // translation key
    public final String descKey; // translation key

    public Spell(ResourceLocation id, String type, int tier, double manaCost, int cooldownTicks, String displayKey, String descKey) {
        this.id = id; this.type = type; this.tier = tier; this.manaCost = manaCost; this.cooldownTicks = cooldownTicks; this.displayKey = displayKey; this.descKey = descKey;
    }
}

