package fr.eriniumgroup.erinium_faction.features.bounty;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration pour le système de bounty
 */
public class BountyConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue MINIMUM_BOUNTY;
    public static final ModConfigSpec.DoubleValue COMMISSION_RATE;
    public static final ModConfigSpec.IntValue EXPIRATION_DAYS;
    public static final ModConfigSpec.DoubleValue HUNTER_XP_BONUS;

    static {
        BUILDER.push("Bounty System");

        BUILDER.comment(
                "═══════════════════════════════════════════════════════════════════════",
                "                        SYSTÈME DE BOUNTY (PRIME)",
                "═══════════════════════════════════════════════════════════════════════",
                "",
                "Configuration du système de prime sur les joueurs.",
                "",
                "Paramètres:",
                "  - minimumBounty    : Montant minimum pour placer une prime ($)",
                "  - commissionRate   : Taux de commission (0.1 = 10%)",
                "  - expirationDays   : Nombre de jours avant expiration",
                "  - hunterXpBonus    : Bonus XP pour le job Hunter lors de la réclamation",
                "",
                "═══════════════════════════════════════════════════════════════════════"
        );

        MINIMUM_BOUNTY = BUILDER
                .comment("Montant minimum pour placer une prime")
                .defineInRange("minimumBounty", 1000.0, 1.0, Double.MAX_VALUE);

        COMMISSION_RATE = BUILDER
                .comment("Taux de commission prélevé sur chaque prime (0.1 = 10%)")
                .defineInRange("commissionRate", 0.1, 0.0, 1.0);

        EXPIRATION_DAYS = BUILDER
                .comment("Nombre de jours avant expiration d'une prime")
                .defineInRange("expirationDays", 7, 1, 365);

        HUNTER_XP_BONUS = BUILDER
                .comment("Bonus XP donné au job Hunter lors de la réclamation d'une prime")
                .defineInRange("hunterXpBonus", 50.0, 0.0, 1000.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    // Helper class pour accès facile aux valeurs
    private static BountyConfig instance;

    private final double minimumBounty;
    private final double commissionRate;
    private final int expirationDays;
    private final double hunterXpBonus;

    private BountyConfig() {
        this.minimumBounty = MINIMUM_BOUNTY.get();
        this.commissionRate = COMMISSION_RATE.get();
        this.expirationDays = EXPIRATION_DAYS.get();
        this.hunterXpBonus = HUNTER_XP_BONUS.get();
    }

    public static BountyConfig get() {
        if (instance == null) {
            instance = new BountyConfig();
        }
        return instance;
    }

    /**
     * Recharge la config (appelé quand le fichier change)
     */
    public static void reload() {
        instance = new BountyConfig();
    }

    public double getMinimumBounty() {
        return minimumBounty;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public int getExpirationDays() {
        return expirationDays;
    }

    public double getHunterXpBonus() {
        return hunterXpBonus;
    }

    /**
     * Calcule le montant total avec commission
     */
    public double getTotalCost(double bountyAmount) {
        return bountyAmount + (bountyAmount * commissionRate);
    }

    /**
     * Calcule la commission
     */
    public double getCommission(double bountyAmount) {
        return bountyAmount * commissionRate;
    }
}
