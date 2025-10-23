package fr.eriniumgroup.erinium_faction.common.config;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = EFC.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EFConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // PvP Settings
    public static final ModConfigSpec.BooleanValue FRIENDLY_FIRE = BUILDER
            .comment("Allow friendly fire between faction members")
            .define("pvp.friendlyFire", false);

    public static final ModConfigSpec.BooleanValue ALLY_DAMAGE = BUILDER
            .comment("Allow damage between allied factions")
            .define("pvp.allyDamage", false);

    // Claim Settings
    public static final ModConfigSpec.IntValue MAX_CLAIMS_PER_FACTION = BUILDER
            .comment("Maximum number of chunks a faction can claim")
            .defineInRange("claims.maxClaimsPerFaction", 100, 1, 10000);

    public static final ModConfigSpec.IntValue MAX_POWER_PER_PLAYER = BUILDER
            .comment("Maximum power per player (affects max claims)")
            .defineInRange("claims.maxPowerPerPlayer", 10, 1, 100);

    // Teleport Settings
    public static final ModConfigSpec.IntValue HOME_TELEPORT_DELAY = BUILDER
            .comment("Delay in seconds before teleporting to faction home")
            .defineInRange("teleport.homeDelay", 3, 0, 60);

    public static final ModConfigSpec.BooleanValue CANCEL_ON_DAMAGE = BUILDER
            .comment("Cancel teleportation if player takes damage")
            .define("teleport.cancelOnDamage", true);

    // Faction Settings
    public static final ModConfigSpec.IntValue MIN_FACTION_NAME_LENGTH = BUILDER
            .comment("Minimum length for faction names")
            .defineInRange("faction.minNameLength", 3, 1, 16);

    public static final ModConfigSpec.IntValue MAX_FACTION_NAME_LENGTH = BUILDER
            .comment("Maximum length for faction names")
            .defineInRange("faction.maxNameLength", 16, 1, 32);

    public static final ModConfigSpec SPEC = BUILDER.build();

    // Cached values
    public static boolean friendlyFire;
    public static boolean allyDamage;
    public static int maxClaimsPerFaction;
    public static int maxPowerPerPlayer;
    public static int homeTeleportDelay;
    public static boolean cancelOnDamage;
    public static int minFactionNameLength;
    public static int maxFactionNameLength;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        friendlyFire = FRIENDLY_FIRE.get();
        allyDamage = ALLY_DAMAGE.get();
        maxClaimsPerFaction = MAX_CLAIMS_PER_FACTION.get();
        maxPowerPerPlayer = MAX_POWER_PER_PLAYER.get();
        homeTeleportDelay = HOME_TELEPORT_DELAY.get();
        cancelOnDamage = CANCEL_ON_DAMAGE.get();
        minFactionNameLength = MIN_FACTION_NAME_LENGTH.get();
        maxFactionNameLength = MAX_FACTION_NAME_LENGTH.get();
    }
}