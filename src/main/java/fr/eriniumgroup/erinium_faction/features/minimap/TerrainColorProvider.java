package fr.eriniumgroup.erinium_faction.features.minimap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Fournit les couleurs de terrain pour le rendu de la minimap
 * Optimisé pour performance 0 FPS
 */
public class TerrainColorProvider {

    /**
     * Obtient la couleur d'un bloc pour la minimap
     * Utilise le système MapColor de Minecraft pour cohérence
     */
    public static int getBlockColor(BlockState state, Level level, BlockPos pos) {
        if (state.isAir()) {
            return 0x00000000; // Transparent
        }

        // Utiliser MapColor pour cohérence avec les maps vanilla
        MapColor mapColor = state.getMapColor(level, pos);
        if (mapColor != null && mapColor != MapColor.NONE) {
            int color = mapColor.col;
            // Vérifier que la couleur n'est pas noire (0x000000)
            if (color != 0 && color != 0x000000) {
                return 0xFF000000 | color; // Ajouter alpha
            }
        }

        // Fallback sur couleurs personnalisées basées sur le type de bloc
        Block block = state.getBlock();

        if (block == Blocks.WATER) {
            return MinimapConfig.COLOR_WATER;
        }
        if (block == Blocks.LAVA) {
            return MinimapConfig.COLOR_LAVA;
        }
        if (block == Blocks.GRASS_BLOCK || block == Blocks.SHORT_GRASS) {
            return MinimapConfig.COLOR_GRASS;
        }
        if (block == Blocks.STONE || block == Blocks.DEEPSLATE || block == Blocks.COBBLESTONE ||
            block == Blocks.ANDESITE || block == Blocks.DIORITE || block == Blocks.GRANITE) {
            return MinimapConfig.COLOR_STONE;
        }
        if (block == Blocks.SAND || block == Blocks.RED_SAND) {
            return MinimapConfig.COLOR_SAND;
        }
        if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) {
            return MinimapConfig.COLOR_SNOW;
        }
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
            return MinimapConfig.COLOR_DIRT;
        }

        // Si on arrive ici et que le bloc n'est pas de l'air, utiliser la couleur unknown
        // MAIS jamais du noir pur (0xFF000000) pour éviter les chunks noirs
        return MinimapConfig.COLOR_UNKNOWN;
    }

    /**
     * Applique un effet d'ombre basé sur la hauteur pour donner du relief
     */
    public static int applyHeightShading(int baseColor, int currentHeight, int neighborHeight) {
        int diff = currentHeight - neighborHeight;

        if (diff > 0) {
            // Plus haut = plus clair
            return lighten(baseColor, Math.min(diff * 10, 50));
        } else if (diff < 0) {
            // Plus bas = plus foncé
            return darken(baseColor, Math.min(-diff * 10, 50));
        }

        return baseColor;
    }

    private static int lighten(int color, int amount) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.min(255, r + amount);
        g = Math.min(255, g + amount);
        b = Math.min(255, b + amount);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int darken(int color, int amount) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.max(0, r - amount);
        g = Math.max(0, g - amount);
        b = Math.max(0, b - amount);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Applique un effet d'obscurcissement pour les grottes souterraines
     */
    public static int applyCaveShading(int baseColor, int depth) {
        // Plus on est profond, plus c'est sombre
        int darkAmount = Math.min(depth * 5, 150);
        return darken(baseColor, darkAmount);
    }
}
