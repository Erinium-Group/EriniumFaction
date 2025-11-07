package fr.eriniumgroup.erinium_faction.client.network;

import fr.eriniumgroup.erinium_faction.client.data.BannerImageCache;
import fr.eriniumgroup.erinium_faction.client.gui.BannerEditorScreen;
import fr.eriniumgroup.erinium_faction.client.renderer.BannerTextureManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Handler pour les paquets bannière côté client uniquement
 * Séparé pour éviter le chargement des classes client sur le serveur
 */
@OnlyIn(Dist.CLIENT)
public class ClientBannerPacketHandler {

    /**
     * Ouvre l'éditeur de bannière avec les pixels fournis
     */
    public static void openBannerEditor(int[] pixels) {
        // Si des pixels sont fournis, les mettre en cache
        if (pixels != null && pixels.length == 2048) {
            BannerImageCache.setImage(pixels);
        } else {
            // Sinon vider le cache pour un nouveau canvas
            BannerImageCache.clear();
        }

        // Ouvrir l'éditeur
        Minecraft.getInstance().setScreen(new BannerEditorScreen());
    }

    /**
     * Stocke les données d'image de bannière en cache
     */
    public static void storeBannerImage(int[] pixels) {
        BannerImageCache.setImage(pixels);
    }

    /**
     * Synchronise une texture de bannière côté client
     */
    public static void syncBannerTexture(String factionId, int[] pixels) {
        if (pixels != null && pixels.length == 2048) {
            BannerTextureManager.registerBannerFromPixels(factionId, pixels);
        }
    }

    /**
     * Synchronise toutes les bannières au login
     */
    public static void syncAllBanners(java.util.Map<String, int[]> banners) {
        // Clear le cache existant
        BannerTextureManager.clear();

        // Enregistrer toutes les bannières
        for (java.util.Map.Entry<String, int[]> entry : banners.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length == 2048) {
                BannerTextureManager.registerBannerFromPixels(entry.getKey(), entry.getValue());
            }
        }
    }
}
