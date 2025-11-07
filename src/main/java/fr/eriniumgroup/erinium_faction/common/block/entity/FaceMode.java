package fr.eriniumgroup.erinium_faction.common.block.entity;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Modes disponibles pour chaque face d'une machine
 */
public enum FaceMode implements StringRepresentable {
    NONE("none", 0x808080, "Disabled"),          // Gris - Désactivé
    INPUT("input", 0x4169E1, "Input"),           // Bleu - Entrée
    OUTPUT("output", 0xFF4500, "Output"),        // Rouge/Orange - Sortie
    INPUT_OUTPUT("input_output", 0x9370DB, "I/O"), // Violet - Entrée/Sortie
    ENERGY("energy", 0xFFD700, "Energy"),        // Jaune/Or - Énergie
    FUEL("fuel", 0xFF8C00, "Fuel");              // Orange foncé - Carburant

    private final String name;
    private final int color;
    private final String displayName;

    FaceMode(String name, int color, String displayName) {
        this.name = name;
        this.color = color;
        this.displayName = displayName;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Retourne le prochain mode dans le cycle
     */
    public FaceMode next() {
        FaceMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    /**
     * Vérifie si ce mode permet l'entrée d'items
     */
    public boolean allowsItemInput() {
        return this == INPUT || this == INPUT_OUTPUT;
    }

    /**
     * Vérifie si ce mode permet la sortie d'items
     */
    public boolean allowsItemOutput() {
        return this == OUTPUT || this == INPUT_OUTPUT;
    }

    /**
     * Vérifie si ce mode permet l'énergie
     */
    public boolean allowsEnergy() {
        return this == ENERGY;
    }

    /**
     * Vérifie si ce mode permet le carburant
     */
    public boolean allowsFuel() {
        return this == FUEL;
    }

    public static FaceMode fromName(String name) {
        for (FaceMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return NONE;
    }

    public static FaceMode fromOrdinal(int ordinal) {
        FaceMode[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return NONE;
    }
}
