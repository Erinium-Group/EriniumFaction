package fr.eriniumgroup.erinium_faction.common.block.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stocke la configuration des faces d'une machine
 */
public class FaceConfiguration {
    private final Map<Direction, FaceMode> faceModes = new EnumMap<>(Direction.class);
    private boolean autoInput = false;
    private boolean autoOutput = false;

    public FaceConfiguration() {
        // Initialisation par défaut : toutes les faces en NONE
        for (Direction dir : Direction.values()) {
            faceModes.put(dir, FaceMode.NONE);
        }
    }

    public FaceMode getFaceMode(@NotNull Direction face) {
        return faceModes.getOrDefault(face, FaceMode.NONE);
    }

    public void setFaceMode(@NotNull Direction face, @NotNull FaceMode mode) {
        faceModes.put(face, mode);
    }

    public boolean isAutoInput() {
        return autoInput;
    }

    public void setAutoInput(boolean enabled) {
        this.autoInput = enabled;
    }

    public boolean isAutoOutput() {
        return autoOutput;
    }

    public void setAutoOutput(boolean enabled) {
        this.autoOutput = enabled;
    }

    /**
     * Sauvegarde la configuration dans un tag NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag facesTag = new CompoundTag();

        for (Map.Entry<Direction, FaceMode> entry : faceModes.entrySet()) {
            facesTag.putString(entry.getKey().getName(), entry.getValue().getSerializedName());
        }

        tag.put("faces", facesTag);
        tag.putBoolean("autoInput", autoInput);
        tag.putBoolean("autoOutput", autoOutput);

        return tag;
    }

    /**
     * Charge la configuration depuis un tag NBT
     */
    public void load(CompoundTag tag) {
        if (tag.contains("faces")) {
            CompoundTag facesTag = tag.getCompound("faces");
            for (Direction dir : Direction.values()) {
                if (facesTag.contains(dir.getName())) {
                    String modeName = facesTag.getString(dir.getName());
                    faceModes.put(dir, FaceMode.fromName(modeName));
                }
            }
        }

        autoInput = tag.getBoolean("autoInput");
        autoOutput = tag.getBoolean("autoOutput");
    }

    /**
     * Copie la configuration
     */
    public FaceConfiguration copy() {
        FaceConfiguration copy = new FaceConfiguration();
        for (Map.Entry<Direction, FaceMode> entry : this.faceModes.entrySet()) {
            copy.faceModes.put(entry.getKey(), entry.getValue());
        }
        copy.autoInput = this.autoInput;
        copy.autoOutput = this.autoOutput;
        return copy;
    }
}