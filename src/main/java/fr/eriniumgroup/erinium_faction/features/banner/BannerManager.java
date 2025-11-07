package fr.eriniumgroup.erinium_faction.features.banner;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.neoforged.fml.loading.FMLPaths;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gestionnaire des bannières custom des factions
 * Sauvegarde/charge les images PNG dans le dossier du serveur
 */
public class BannerManager {

    private static final Path BANNER_DIR = FMLPaths.GAMEDIR.get().resolve(EFC.MOD_ID).resolve("banners");

    static {
        try {
            Files.createDirectories(BANNER_DIR);
        } catch (IOException e) {
            EFC.log.error("Failed to create banner directory", e);
        }
    }

    /**
     * Sauvegarde l'image de bannière pour une faction
     * @param factionId ID de la faction
     * @param image Image 32x64 (largeur x hauteur)
     * @return true si succès
     */
    public static boolean saveBanner(String factionId, BufferedImage image) {
        if (image == null || factionId == null || factionId.isEmpty()) {
            return false;
        }

        try {
            File bannerFile = BANNER_DIR.resolve(factionId + ".png").toFile();
            ImageIO.write(image, "PNG", bannerFile);
            EFC.log.info("Saved banner for faction: " + factionId);
            return true;
        } catch (IOException e) {
            EFC.log.error("Failed to save banner for faction " + factionId, e);
            return false;
        }
    }

    /**
     * Charge l'image de bannière d'une faction
     * @param factionId ID de la faction
     * @return Image ou null si non trouvée
     */
    public static BufferedImage loadBanner(String factionId) {
        if (factionId == null || factionId.isEmpty()) {
            return null;
        }

        try {
            File bannerFile = BANNER_DIR.resolve(factionId + ".png").toFile();
            if (!bannerFile.exists()) {
                return null;
            }
            return ImageIO.read(bannerFile);
        } catch (IOException e) {
            EFC.log.error("Failed to load banner for faction " + factionId, e);
            return null;
        }
    }

    /**
     * Vérifie si une faction a une bannière sauvegardée
     * @param factionId ID de la faction
     * @return true si la bannière existe
     */
    public static boolean hasBanner(String factionId) {
        if (factionId == null || factionId.isEmpty()) {
            return false;
        }
        return BANNER_DIR.resolve(factionId + ".png").toFile().exists();
    }

    /**
     * Supprime la bannière d'une faction
     * @param factionId ID de la faction
     * @return true si succès
     */
    public static boolean deleteBanner(String factionId) {
        if (factionId == null || factionId.isEmpty()) {
            return false;
        }

        try {
            File bannerFile = BANNER_DIR.resolve(factionId + ".png").toFile();
            if (bannerFile.exists()) {
                return bannerFile.delete();
            }
            return true;
        } catch (Exception e) {
            EFC.log.error("Failed to delete banner for faction " + factionId, e);
            return false;
        }
    }

    /**
     * Récupère le chemin du dossier des bannières
     * @return Path du dossier
     */
    public static Path getBannerDirectory() {
        return BANNER_DIR;
    }
}
