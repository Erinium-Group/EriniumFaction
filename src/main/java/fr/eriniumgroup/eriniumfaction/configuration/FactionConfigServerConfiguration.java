package fr.eriniumgroup.eriniumfaction.configuration;

import net.neoforged.neoforge.common.ModConfigSpec;

import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

public class FactionConfigServerConfiguration {
	public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.ConfigValue<String> FACTIONCOSTITEM;
	public static final ModConfigSpec.ConfigValue<Double> FACTIONCOSTNUMBER;
	public static final ModConfigSpec.ConfigValue<Double> POWERPERPLAYER;
	public static final ModConfigSpec.ConfigValue<Double> DEFAULTMAXPLAYER;
	public static final ModConfigSpec.ConfigValue<Double> DEFAULTMAXWARP;
	static {
		BUILDER.push("Faction System");
		FACTIONCOSTITEM = BUILDER.comment("item pour créer une faction").define("Faction Cost Item", BuiltInRegistries.ITEM.getKey(Items.DIAMOND).toString());
		FACTIONCOSTNUMBER = BUILDER.comment("Cout pour créer une faction").define("Faction Cost Number", (double) 100);
		POWERPERPLAYER = BUILDER.comment("Le power d'un seul joueur").define("Faction Power per player", (double) 10);
		DEFAULTMAXPLAYER = BUILDER.comment("Le nombre de joueur max par défaut sans upgrade").define("Default max player", (double) 10);
		DEFAULTMAXWARP = BUILDER.comment("Nombre de warp max par defaut").define("Default max warp", (double) 1);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}