package fr.eriniumgroup.erinium_faction.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

/**
 * Renderer custom qui reproduit EXACTEMENT le comportement vanilla
 * mais avec notre texture 32x64 au lieu des patterns vanilla
 */
@OnlyIn(Dist.CLIENT)
public class CustomBannerRenderer {

    /**
     * Rend le flag avec notre texture custom
     * Reproduit le comportement de ModelPart.render() mais avec mapping UV custom
     */
    public static void renderCustomFlag(
        PoseStack poseStack,
        MultiBufferSource buffer,
        ResourceLocation texture,
        int combinedLight,
        int combinedOverlay
    ) {
        // Le vanilla scale à 0.6666667F et inverse Y et Z
        // Les coordonnées du ModelPart vanilla sont en unités de modèle qui sont automatiquement divisées par 16
        // addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F) devient addBox(-0.625F, 0.0F, -0.125F, 1.25F, 2.5F, 0.0625F)
        // Mais comme on appelle render() après le scale, on doit utiliser les coordonnées en unités de modèle

        // Coordonnées du modèle vanilla divisées par 16 (conversion modèle -> bloc)
        float x1 = -10.0F / 16.0F;  // -0.625
        float x2 = 10.0F / 16.0F;   // 0.625
        float y1 = 0.0F / 16.0F;    // 0.0
        float y2 = 40.0F / 16.0F;   // 2.5
        float z1 = -2.0F / 16.0F;   // -0.125
        float z2 = -1.0F / 16.0F;   // -0.0625

        // Utiliser entityTranslucentCull pour que la bannière soit rendue dans la phase translucent
        // mais avec culling (pour voir à travers le verre/blocks transparents)
        // Cette phase est rendue AVANT les nameplates mais APRÈS les solides
        RenderType renderType = RenderType.entityTranslucentCull(texture);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // On va render uniquement la face AVANT (Z+) du flag
        // avec notre texture 32x64 mappée dessus

        // Face avant (regarde vers Z+, donc normale = 0,0,1)
        // Ordre des vertices: bas-gauche, bas-droit, haut-droit, haut-gauche
        // UV inversés en U et V pour que l'image soit dans le bon sens (miroir horizontal + vertical)
        addVertex(vertexConsumer, pose, normal, x1, y1, z2, 1.0F, 0.0F, 0, 0, 1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x2, y1, z2, 0.0F, 0.0F, 0, 0, 1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x2, y2, z2, 0.0F, 1.0F, 0, 0, 1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x1, y2, z2, 1.0F, 1.0F, 0, 0, 1, combinedLight, combinedOverlay);

        // Face arrière (regarde vers Z-, donc normale = 0,0,-1)
        // Ordre inversé pour le culling, UV aussi inversés
        addVertex(vertexConsumer, pose, normal, x1, y1, z1, 0.0F, 0.0F, 0, 0, -1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x1, y2, z1, 0.0F, 1.0F, 0, 0, -1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x2, y2, z1, 1.0F, 1.0F, 0, 0, -1, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, normal, x2, y1, z1, 1.0F, 0.0F, 0, 0, -1, combinedLight, combinedOverlay);
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
}
