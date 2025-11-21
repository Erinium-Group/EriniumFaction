package fr.eriniumgroup.erinium_faction.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.client.model.RocketModel;
import fr.eriniumgroup.erinium_faction.common.entity.RocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Axis;

/**
 * Renderer pour l'entité fusée
 */
public class RocketRenderer extends EntityRenderer<RocketEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        EriniumFaction.MODID, "textures/entity/rocket.png");

    private final RocketModel model;

    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RocketModel(context.bakeLayer(RocketModel.LAYER_LOCATION));
        this.shadowRadius = 1.5F;
    }

    @Override
    public void render(RocketEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Centrer et ajuster la rotation
        poseStack.translate(0.0, 1.5, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        // Échelle si nécessaire
        poseStack.scale(1.0F, 1.0F, 1.0F);

        // Rendu du modèle
        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, getOverlayCoords(entity, 0.0F), 0xFFFFFFFF);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity entity) {
        return TEXTURE;
    }

    protected static int getOverlayCoords(RocketEntity entity, float u) {
        return net.minecraft.client.renderer.texture.OverlayTexture.pack(
            net.minecraft.client.renderer.texture.OverlayTexture.u(u),
            net.minecraft.client.renderer.texture.OverlayTexture.v(entity.getHurtTime() > 0)
        );
    }
}
