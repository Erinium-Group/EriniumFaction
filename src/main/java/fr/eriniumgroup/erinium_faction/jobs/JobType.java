package fr.eriniumgroup.erinium_faction.jobs;

/**
 * Enumération des types de métiers disponibles
 */
public enum JobType {
    MINER("Miner", 0xfbbf24, "⛏", "Extract valuable resources from the earth"),
    LUMBERJACK("LumberJack", 0x8b4513, "🪓", "Master the art of woodcutting"),
    HUNTER("Hunter", 0xef4444, "🏹", "Hunt down monsters and beasts"),
    FISHER("Fisher", 0x3b82f6, "🎣", "Catch fish and aquatic treasures"),
    FARMER("Farmer", 0x10b981, "🌾", "Grow crops and raise animals"),
    WIZARD("Wizard", 0xa855f7, "🔮", "Master magical arts and enchantments");

    private final String displayName;
    private final int color;
    private final String emoji;
    private final String description;

    JobType(String displayName, int color, String emoji, String description) {
        this.displayName = displayName;
        this.color = color;
        this.emoji = emoji;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDescription() {
        return description;
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
}
