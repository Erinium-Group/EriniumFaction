package fr.eriniumgroup.erinium_faction.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Utility class for teleportation operations
 */
public class TeleportUtil {

    public static boolean teleport(ServerPlayer player, String dimensionStr, BlockPos position) {
        ResourceLocation dimLoc = ResourceLocation.tryParse(dimensionStr);
        if (dimLoc == null) return false;

        ResourceKey<Level> dimensionKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            dimLoc
        );

        ServerLevel targetLevel = player.server.getLevel(dimensionKey);
        if (targetLevel == null) return false;

        player.teleportTo(
            targetLevel,
            position.getX() + 0.5,
            position.getY(),
            position.getZ() + 0.5,
            player.getYRot(),
            player.getXRot()
        );

        return true;
    }
}

