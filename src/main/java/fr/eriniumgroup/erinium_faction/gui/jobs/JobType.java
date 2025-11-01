package fr.eriniumgroup.erinium_faction.gui.jobs;

import net.minecraft.resources.ResourceLocation;

/**
 * Énumération des types de jobs
 */
public enum JobType {
    GLOBAL("Global", "Overall Progress",
           "erinium_faction:textures/gui/components/jobs/bg-full-main.png",
           "erinium_faction:textures/gui/components/jobs/border-main.png",
           "erinium_faction:textures/gui/components/jobs/progressbar-fill-main.png",
           0xa855f7),

    MINER("Miner", "Mining Profession",
          "erinium_faction:textures/gui/components/jobs/bg-full-miner.png",
          "erinium_faction:textures/gui/components/jobs/border-miner.png",
          "erinium_faction:textures/gui/components/jobs/progressbar-fill-miner.png",
          0x6b7280),

    FARMER("Farmer", "Farming Profession",
           "erinium_faction:textures/gui/components/jobs/bg-full-farmer.png",
           "erinium_faction:textures/gui/components/jobs/border-farmer.png",
           "erinium_faction:textures/gui/components/jobs/progressbar-fill-farmer.png",
           0x10b981),

    LUMBERJACK("Lumberjack", "Logging Profession",
               "erinium_faction:textures/gui/components/jobs/bg-full-lumberjack.png",
               "erinium_faction:textures/gui/components/jobs/border-lumberjack.png",
               "erinium_faction:textures/gui/components/jobs/progressbar-fill-lumberjack.png",
               0x92400e),

    HUNTER("Hunter", "Hunting Profession",
           "erinium_faction:textures/gui/components/jobs/bg-full-hunter.png",
           "erinium_faction:textures/gui/components/jobs/border-hunter.png",
           "erinium_faction:textures/gui/components/jobs/progressbar-fill-hunter.png",
           0xdc2626),

    FISHER("Fisher", "Fishing Profession",
           "erinium_faction:textures/gui/components/jobs/bg-full-fisher.png",
           "erinium_faction:textures/gui/components/jobs/border-fisher.png",
           "erinium_faction:textures/gui/components/jobs/progressbar-fill-fisher.png",
           0x0891b2);

    private final String name;
    private final String subtitle;
    private final ResourceLocation background;
    private final ResourceLocation border;
    private final ResourceLocation progressFill;
    private final int color;

    JobType(String name, String subtitle, String bg, String border, String fill, int color) {
        this.name = name;
        this.subtitle = subtitle;
        this.background = ResourceLocation.parse(bg);
        this.border = ResourceLocation.parse(border);
        this.progressFill = ResourceLocation.parse(fill);
        this.color = color;
    }

    public String getName() { return name; }
    public String getSubtitle() { return subtitle; }
    public ResourceLocation getBackground() { return background; }
    public ResourceLocation getBorder() { return border; }
    public ResourceLocation getProgressFill() { return progressFill; }
    public int getColor() { return color; }
}
