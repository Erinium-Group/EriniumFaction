package fr.eriniumgroup.erinium_faction.client.data;

import java.awt.image.BufferedImage;

/**
 * Cache côté client pour stocker temporairement l'image de bannière
 * Utilisé lors de l'édition et de la génération de bannière
 */
public class BannerImageCache {

    private static int[] cachedPixels = null;

    /**
     * Stocke l'image sous forme de tableau de pixels
     * @param pixels Tableau ARGB de 2048 pixels (32x64)
     */
    public static void setImage(int[] pixels) {
        cachedPixels = pixels;
    }

    /**
     * Récupère l'image stockée
     * @return BufferedImage ou null
     */
    public static BufferedImage getImage() {
        if (cachedPixels == null || cachedPixels.length != 2048) {
            return null;
        }

        BufferedImage image = new BufferedImage(32, 64, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 32, 64, cachedPixels, 0, 32);
        return image;
    }

    /**
     * Récupère le tableau de pixels brut
     * @return Tableau de pixels ou null
     */
    public static int[] getPixels() {
        return cachedPixels;
    }

    /**
     * Vide le cache
     */
    public static void clear() {
        cachedPixels = null;
    }

    /**
     * Vérifie si une image est en cache
     * @return true si une image est stockée
     */
    public static boolean hasImage() {
        return cachedPixels != null && cachedPixels.length == 2048;
    }
}
