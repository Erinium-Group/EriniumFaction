package fr.eriniumgroup.erinium_faction.jobs;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

/**
 * Enumération des types de métiers disponibles
 */
public enum JobType implements StringRepresentable {
    MINER(0xfbbf24, "⛏"),
    LUMBERJACK(0x8b4513, "🪓"),
    HUNTER(0xef4444, "🏹"),
    FISHER(0x3b82f6, "🎣"),
    FARMER(0x10b981, "🌾"),
    WIZARD(0xa855f7, "🔮");

    private final int color;
    private final String emoji;

    JobType(int color, String emoji) {
        this.color = color;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return Component.translatable("erinium_faction.jobs.type." + name().toLowerCase()).getString();
    }

    public int getColor() {
        return color;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDescription() {
        return Component.translatable("erinium_faction.jobs.desc." + name().toLowerCase()).getString();
    }

    public String getColorName() {
        return switch (this) {
            case MINER -> "gold";
            case LUMBERJACK -> "brown";
            case HUNTER -> "red";
            case FISHER -> "blue";
            case FARMER -> "green";
            case WIZARD -> "purple";
        };
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    // Codec pour la serialization réseau
    public static final Codec<JobType> CODEC = StringRepresentable.fromEnum(JobType::values);

    public static JobType byId(int id) {
        if (id < 0 || id >= values().length) {
            return MINER; // Valeur par défaut
        }
        return values()[id];
    }
}
