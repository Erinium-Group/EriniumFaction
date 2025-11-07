package fr.eriniumgroup.erinium_faction.common.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BannerImageConverter {

    /**
     * Convertit une image en bannière Minecraft
     * @param image Image 64x32 (ou sera resize)
     * @return ItemStack bannière avec patterns
     */
    public static ItemStack createBanner(BufferedImage image, String factionId) {
        // Resize à 32x64
        BufferedImage resized = resize(image, 32, 64);

        // Créer bannière blanche
        ItemStack banner = new ItemStack(Items.WHITE_BANNER);

        // Ajouter un tag NBT custom pour identifier cette bannière comme custom
        net.minecraft.nbt.CompoundTag customTag = new net.minecraft.nbt.CompoundTag();
        customTag.putBoolean("erinium_custom_banner", true);
        customTag.putString("erinium_faction_id", factionId);
        banner.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(customTag));

        // Ajouter un nom custom pour identifier la bannière
        banner.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Bannière de Faction"));

        return banner;
    }

    /**
     * Convertit une image en BannerPatternLayers pour Minecraft 1.21
     */
    private static BannerPatternLayers imageToBannerPatterns(BufferedImage img) {
        List<BannerPatternLayers.Layer> layers = new ArrayList<>();

        // Analyse de l'image pour créer layers de couleurs
        for (DyeColor color : DyeColor.values()) {
            List<Point> pixels = findPixelsOfColor(img, color);

            if (!pixels.isEmpty()) {
                String patternCode = bestPatternFor(pixels);

                // Créer un Holder pour le pattern
                ResourceLocation patternLocation = ResourceLocation.withDefaultNamespace(patternCode);
                ResourceKey<BannerPattern> patternKey = ResourceKey.create(Registries.BANNER_PATTERN, patternLocation);

                // Créer un holder direct avec la key
                Holder<BannerPattern> patternHolder = Holder.direct(new BannerPattern(patternLocation, "block.minecraft.banner." + patternCode));

                // Ajouter le layer
                layers.add(new BannerPatternLayers.Layer(patternHolder, color));

                // Max 6 patterns par bannière (limite Minecraft)
                if (layers.size() >= 6) break;
            }
        }

        return new BannerPatternLayers(layers);
    }

    private static DyeColor getClosestColor(Color pixel) {
        DyeColor closest = DyeColor.WHITE;
        double minDist = Double.MAX_VALUE;

        for (DyeColor dye : DyeColor.values()) {
            int rgb = dye.getTextureDiffuseColor();
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            double dist = Math.sqrt(
                    Math.pow(pixel.getRed() - r, 2) +
                            Math.pow(pixel.getGreen() - g, 2) +
                            Math.pow(pixel.getBlue() - b, 2)
            );

            if (dist < minDist) {
                minDist = dist;
                closest = dye;
            }
        }

        return closest;
    }

    private static BufferedImage resize(BufferedImage img, int w, int h) {
        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return resized;
    }

    /**
     * Trouve tous les pixels d'une couleur spécifique dans l'image
     */
    private static List<Point> findPixelsOfColor(BufferedImage img, DyeColor targetColor) {
        List<Point> pixels = new ArrayList<>();
        int targetRgb = targetColor.getTextureDiffuseColor();

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                Color pixelColor = new Color(rgb, true);

                // Ignorer pixels transparents
                if (pixelColor.getAlpha() < 128) continue;

                // Vérifier si la couleur correspond
                DyeColor closestColor = getClosestColor(pixelColor);
                if (closestColor == targetColor) {
                    pixels.add(new Point(x, y));
                }
            }
        }

        return pixels;
    }

    /**
     * Détermine le meilleur pattern de bannière pour un ensemble de pixels
     * Retourne un code de pattern Minecraft (ex: "cr", "bs", "ts", etc.)
     */
    private static String bestPatternFor(List<Point> pixels) {
        if (pixels.isEmpty()) return "b"; // base (fallback)

        // Calculer le centre de masse des pixels
        double avgX = pixels.stream().mapToInt(p -> p.x).average().orElse(32);
        double avgY = pixels.stream().mapToInt(p -> p.y).average().orElse(16);

        // Analyser la distribution pour choisir un pattern approprié
        // Patterns basiques de bannière Minecraft:
        // - "b" = base
        // - "bs" = bottom stripe
        // - "ts" = top stripe
        // - "ms" = middle stripe
        // - "ls" = left stripe
        // - "rs" = right stripe
        // - "cs" = center stripe
        // - "drs" = down right stripe
        // - "dls" = down left stripe
        // - "ss" = small stripes
        // - "cr" = cross
        // - "sc" = square center
        // - "bt" = bottom triangle
        // - "tt" = top triangle
        // - "bts" = bottom triangle sawtooth
        // - "tts" = top triangle sawtooth

        // Logique simplifiée: choisir selon la position
        if (avgY < 10) {
            return "ts"; // top stripe
        } else if (avgY > 22) {
            return "bs"; // bottom stripe
        } else if (avgX < 21) {
            return "ls"; // left stripe
        } else if (avgX > 43) {
            return "rs"; // right stripe
        } else {
            return "ms"; // middle stripe (centre)
        }
    }
}