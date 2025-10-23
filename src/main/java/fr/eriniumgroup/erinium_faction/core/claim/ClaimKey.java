package fr.eriniumgroup.erinium_faction.core.claim;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Unique identifier for a claimed chunk
 */
public record ClaimKey(String dimension, int chunkX, int chunkZ) {

    public static ClaimKey of(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return new ClaimKey(dimension.location().toString(), chunkX, chunkZ);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClaimKey)) return false;
        ClaimKey claimKey = (ClaimKey) o;
        return chunkX == claimKey.chunkX && chunkZ == claimKey.chunkZ && Objects.equals(dimension, claimKey.dimension);
    }

    // TODO: Voir pour changé le format de sauvegarde
    @Override
    public String toString() {
        return dimension + " [" + chunkX + ", " + chunkZ + "]";
    }
}

