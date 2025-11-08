package fr.eriniumgroup.erinium_faction.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renderer custom pour les capes avec texture de bannière 32x64
 * La cape vanilla utilise une texture 64x32, on adapte pour 32x64
 */
@OnlyIn(Dist.CLIENT)
public class CustomCapeRenderer {

    /**
     * Rend la cape avec la texture de bannière
     * La cape vanilla mesure 10x16 unités (10 de large, 16 de haut)
     * On utilise notre texture 32x64 mappée dessus
     */
    public static void renderCustomCape(
        PoseStack poseStack,
        MultiBufferSource buffer,
        ResourceLocation texture,
        int combinedLight,
        int combinedOverlay
    ) {
        // Dimensions de la cape avec du volume (1 pixel d'épaisseur)
        float width = 10.0F / 16.0F;   // Convertir en unités de bloc
        float height = 16.0F / 16.0F;  // Convertir en unités de bloc
        float thickness = 1.0F / 16.0F; // 1 pixel d'épaisseur pour avoir du volume

        RenderType renderType = RenderType.entitySolid(texture);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Position de la cape (commence au niveau des épaules)
        float x1 = -width / 2.0F;
        float x2 = width / 2.0F;
        float y1 = 0.0F;
        float y2 = height;
        float z1 = -thickness / 2.0F;
        float z2 = thickness / 2.0F;

        // Face avant de la cape (côté visible)
        // UV mapping pour texture 32x64 (largeur x hauteur)
        // On inverse U et V pour que l'image soit dans le bon sens
        addVertex(vertexConsumer, pose, normal, x1, y1, z2, 1.0F, 0.0F, 0, 0, 1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x2, y1, z2, 0.0F, 0.0F, 0, 0, 1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x2, y2, z2, 0.0F, 1.0F, 0, 0, 1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x1, y2, z2, 1.0F, 1.0F, 0, 0, 1, combinedLight, combinedOverlay);

        // Face arrière de la cape (mirrorée)
        addVertex(vertexConsumer, pose, normal, x1, y1, z1, 0.0F, 0.0F, 0, 0, -1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x1, y2, z1, 0.0F, 1.0F, 0, 0, -1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x2, y2, z1, 1.0F, 1.0F, 0, 0, -1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x2, y1, z1, 1.0F, 0.0F, 0, 0, -1, combinedLight, combinedOverlay);

        // Face gauche (côté X-) - Blanc uni
        addVertexWhite(vertexConsumer, pose, normal, x1, y1, z1, -1, 0, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x1, y1, z2, -1, 0, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x1, y2, z2, -1, 0, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x1, y2, z1, -1, 0, 0, combinedLight, combinedOverlay);

        // Face droite (côté X+) - Blanc uni
        addVertexWhite(vertexConsumer, pose, normal, x2, y1, z2, 1, 0, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x2, y1, z1, 1, 0, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x2, y2, z1, 1, 0, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x2, y2, z2, 1, 0, 0, combinedLight, combinedOverlay);

        // Face haut (côté Y+) - Blanc uni
        addVertexWhite(vertexConsumer, pose, normal, x1, y2, z2, 0, 1, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x2, y2, z2, 0, 1, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x2, y2, z1, 0, 1, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x1, y2, z1, 0, 1, 0, combinedLight, combinedOverlay);

        // Face bas (côté Y-) - Blanc uni
        addVertexWhite(vertexConsumer, pose, normal, x1, y1, z1, 0, -1, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x2, y1, z1, 0, -1, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x2, y1, z2, 0, -1, 0, combinedLight, combinedOverlay);
        addVertexWhite(vertexConsumer, pose, normal, x1, y1, z2, 0, -1, 0, combinedLight, combinedOverlay);
    }

    private static void addVertex(
        VertexConsumer consumer,
        Matrix4f pose,
        Matrix3f normal,
        float x, float y, float z,
        float u, float v,
        int nx, int ny, int nz,
        int light,
        int overlay
    ) {
        consumer.addVertex(pose, x, y, z)
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal((float)nx, (float)ny, (float)nz);
    }

    /**
     * Ajoute un vertex blanc sans texture (pour les épaisseurs de la cape)
     */
    private static void addVertexWhite(
        VertexConsumer consumer,
        Matrix4f pose,
        Matrix3f normal,
        float x, float y, float z,
        int nx, int ny, int nz,
        int light,
        int overlay
    ) {
        consumer.addVertex(pose, x, y, z)
            .setColor(255, 255, 255, 255)
            .setUv(0.0F, 0.0F)  // UV à 0,0 pour éviter d'étirer la texture
            .setOverlay(overlay)
            .setLight(light)
            .setNormal((float)nx, (float)ny, (float)nz);
    }
}
