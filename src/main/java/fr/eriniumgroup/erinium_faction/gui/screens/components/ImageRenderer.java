package fr.eriniumgroup.erinium_faction.gui.screens.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Classe utilitaire pour le rendu d'images avec scale dynamique
 * Permet de remplacer les g.fill par des textures tout en maintenant les dimensions du GUI
 */
public class ImageRenderer {

    /**
     * Rend une image avec scale dynamique
     * @param g GuiGraphics
     * @param texture ResourceLocation de la texture
     * @param x Position X (déjà scalée)
     * @param y Position Y (déjà scalée)
     * @param width Largeur (déjà scalée)
     * @param height Hauteur (déjà scalée)
     */
    public static void renderScaledImage(GuiGraphics g, ResourceLocation texture, int x, int y, int width, int height) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        g.blit(texture, x, y, 0, 0, width, height, width, height);

        RenderSystem.disableBlend();
    }

    /**
     * Rend une image avec scale dynamique et alpha
     * @param g GuiGraphics
     * @param texture ResourceLocation de la texture
     * @param x Position X (déjà scalée)
     * @param y Position Y (déjà scalée)
     * @param width Largeur (déjà scalée)
     * @param height Hauteur (déjà scalée)
     * @param alpha Transparence (0.0 - 1.0)
     */
    public static void renderScaledImageWithAlpha(GuiGraphics g, ResourceLocation texture, int x, int y, int width, int height, float alpha) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        g.blit(texture, x, y, 0, 0, width, height, width, height);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Rend une image 9-slice (pour les bordures extensibles)
     * @param g GuiGraphics
     * @param texture ResourceLocation de la texture
     * @param x Position X
     * @param y Position Y
     * @param width Largeur totale
     * @param height Hauteur totale
     * @param textureWidth Largeur de la texture source
     * @param textureHeight Hauteur de la texture source
     * @param border Taille de la bordure (pixels)
     */
    public static void renderNineSlice(GuiGraphics g, ResourceLocation texture, int x, int y, int width, int height,
                                       int textureWidth, int textureHeight, int border) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Coins
        g.blit(texture, x, y, 0, 0, border, border, textureWidth, textureHeight); // Top-left
        g.blit(texture, x + width - border, y, textureWidth - border, 0, border, border, textureWidth, textureHeight); // Top-right
        g.blit(texture, x, y + height - border, 0, textureHeight - border, border, border, textureWidth, textureHeight); // Bottom-left
        g.blit(texture, x + width - border, y + height - border, textureWidth - border, textureHeight - border, border, border, textureWidth, textureHeight); // Bottom-right

        // Bords
        // Top
        for (int i = border; i < width - border; i += (textureWidth - 2 * border)) {
            int w = Math.min(textureWidth - 2 * border, width - border - i);
            g.blit(texture, x + i, y, border, 0, w, border, textureWidth, textureHeight);
        }
        // Bottom
        for (int i = border; i < width - border; i += (textureWidth - 2 * border)) {
            int w = Math.min(textureWidth - 2 * border, width - border - i);
            g.blit(texture, x + i, y + height - border, border, textureHeight - border, w, border, textureWidth, textureHeight);
        }
        // Left
        for (int i = border; i < height - border; i += (textureHeight - 2 * border)) {
            int h = Math.min(textureHeight - 2 * border, height - border - i);
            g.blit(texture, x, y + i, 0, border, border, h, textureWidth, textureHeight);
        }
        // Right
        for (int i = border; i < height - border; i += (textureHeight - 2 * border)) {
            int h = Math.min(textureHeight - 2 * border, height - border - i);
            g.blit(texture, x + width - border, y + i, textureWidth - border, border, border, h, textureWidth, textureHeight);
        }

        // Centre
        for (int i = border; i < width - border; i += (textureWidth - 2 * border)) {
            for (int j = border; j < height - border; j += (textureHeight - 2 * border)) {
                int w = Math.min(textureWidth - 2 * border, width - border - i);
                int h = Math.min(textureHeight - 2 * border, height - border - j);
                g.blit(texture, x + i, y + j, border, border, w, h, textureWidth, textureHeight);
            }
        }

        RenderSystem.disableBlend();
    }
}
