package fr.eriniumgroup.erinium_faction.features.minimap;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Représente un waypoint sur la carte
 */
public class Waypoint {
    private final UUID id;
    private String name;
    private BlockPos position;
    private ResourceKey<Level> dimension;
    private WaypointColor color;
    private int customColor; // RGB custom (0xRRGGBB)
    private boolean enabled;

    public Waypoint(UUID id, String name, BlockPos position, ResourceKey<Level> dimension, WaypointColor color) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.dimension = dimension;
        this.color = color;
        this.customColor = color.getColor(); // Par défaut, utiliser la couleur du preset
        this.enabled = true;
    }

    public Waypoint(UUID id, String name, BlockPos position, ResourceKey<Level> dimension, int customColor) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.dimension = dimension;
        this.color = WaypointColor.CUSTOM; // Utiliser le type CUSTOM
        this.customColor = 0xFF000000 | customColor; // Forcer alpha = FF
        this.enabled = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BlockPos getPosition() {
        return position;
    }

    public void setPosition(BlockPos position) {
        this.position = position;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public WaypointColor getColor() {
        return color;
    }

    public void setColor(WaypointColor color) {
        this.color = color;
        if (color != WaypointColor.CUSTOM) {
            this.customColor = color.getColor();
        }
    }

    public int getCustomColor() {
        return customColor;
    }

    public void setCustomColor(int rgb) {
        this.customColor = 0xFF000000 | rgb; // Forcer alpha = FF
        this.color = WaypointColor.CUSTOM;
    }

    /**
     * Retourne la couleur effective à utiliser pour le rendu
     */
    public int getEffectiveColor() {
        if (color == WaypointColor.CUSTOM) {
            return 0xFF000000 | customColor; // Add alpha channel to RGB
        }
        return color.getColor();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("name", name);
        tag.putInt("x", position.getX());
        tag.putInt("y", position.getY());
        tag.putInt("z", position.getZ());
        tag.putString("dimension", dimension.location().toString());
        tag.putString("color", color.name());
        tag.putInt("customColor", customColor);
        tag.putBoolean("enabled", enabled);
        return tag;
    }

    public static Waypoint fromNBT(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        String name = tag.getString("name");
        BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        ResourceKey<Level> dimension = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            net.minecraft.resources.ResourceLocation.parse(tag.getString("dimension"))
        );
        WaypointColor color = WaypointColor.valueOf(tag.getString("color"));
        Waypoint waypoint = new Waypoint(id, name, pos, dimension, color);
        if (tag.contains("customColor")) {
            waypoint.customColor = tag.getInt("customColor");
        }
        waypoint.setEnabled(tag.getBoolean("enabled"));
        return waypoint;
    }

    public enum WaypointColor implements net.minecraft.util.StringRepresentable {
        RED(0xFFFF0000, "Home", "red"),
        BLUE(0xFF0066FF, "Mine", "blue"),
        GREEN(0xFF00AA00, "Farm", "green"),
        PURPLE(0xFFFF00FF, "Death", "purple"),
        ORANGE(0xFFFFAA00, "Other", "orange"),
        YELLOW(0xFFFFFF00, "Village", "yellow"),
        CUSTOM(0xFFFFFFFF, "Custom", "custom");

        private final int color;
        private final String defaultLabel;
        private final String serializedName;

        WaypointColor(int color, String defaultLabel, String serializedName) {
            this.color = color;
            this.defaultLabel = defaultLabel;
            this.serializedName = serializedName;
        }

        public int getColor() {
            return color;
        }

        public String getDefaultLabel() {
            return defaultLabel;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
