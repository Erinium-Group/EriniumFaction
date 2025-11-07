package fr.eriniumgroup.erinium_faction.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import fr.eriniumgroup.erinium_faction.EriniumFaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Gère les textures dynamiques des bannières custom côté client
 */
@OnlyIn(Dist.CLIENT)
public class BannerTextureManager {

    private static final Map<String, ResourceLocation> bannerTextures = new HashMap<>();
    private static final Map<String, BufferedImage> cachedImages = new HashMap<>();

    /**
     * Enregistre une texture de bannière depuis une image
     * @param factionId ID de la faction
     * @param image Image 32x64 de l'utilisateur
     * @return ResourceLocation de la texture
     */
    public static ResourceLocation registerBannerTexture(String factionId, BufferedImage image) {
        // Vérifier si déjà enregistrée
        if (bannerTextures.containsKey(factionId)) {
            return bannerTextures.get(factionId);
        }

        try {
            // Convertir BufferedImage en NativeImage directement (32x64)
            NativeImage nativeImage = convertToNativeImage(image);

            // Créer une texture dynamique
            DynamicTexture texture = new DynamicTexture(nativeImage);

            // Enregistrer dans le TextureManager de Minecraft
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                EriniumFaction.MODID,
                "textures/banner/" + factionId
            );

            Minecraft.getInstance().getTextureManager().register(location, texture);

            // Stocker dans le cache
            bannerTextures.put(factionId, location);
            cachedImages.put(factionId, image);

            return location;
        } catch (Exception e) {
            fr.eriniumgroup.erinium_faction.core.EFC.log.error("Erreur lors de l'enregistrement de la texture de bannière pour " + factionId, e);
            return null;
        }
    }

    /**
     * Récupère la ResourceLocation d'une bannière de faction
     */
    public static ResourceLocation getBannerTexture(String factionId) {
        return bannerTextures.get(factionId);
    }

    /**
     * Vérifie si une bannière est en cache
     */
    public static boolean hasBannerTexture(String factionId) {
        return bannerTextures.containsKey(factionId);
    }

    /**
     * Enregistre une bannière depuis les pixels reçus du serveur
     */
    public static ResourceLocation registerBannerFromPixels(String factionId, int[] pixels) {
        if (pixels == null || pixels.length != 2048) {
            return null;
        }

        BufferedImage image = new BufferedImage(32, 64, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 32, 64, pixels, 0, 32);

        return registerBannerTexture(factionId, image);
    }

    /**
     * Convertit BufferedImage (32x64) en NativeImage
     * Simple conversion 1:1 avec ARGB -> ABGR
     */
    private static NativeImage convertToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        NativeImage nativeImage = new NativeImage(width, height, true);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = bufferedImage.getRGB(x, y);

                // Convertir ARGB vers ABGR pour NativeImage
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                nativeImage.setPixelRGBA(x, y, abgr);
            }
        }

        return nativeImage;
    }

    /**
     * Nettoie toutes les textures en cache
     */
    public static void clear() {
        for (ResourceLocation location : bannerTextures.values()) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
        bannerTextures.clear();
        cachedImages.clear();
    }

    /**
     * Supprime une texture spécifique
     */
    public static void removeBannerTexture(String factionId) {
        ResourceLocation location = bannerTextures.remove(factionId);
        if (location != null) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
        cachedImages.remove(factionId);
    }
}
