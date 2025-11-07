package fr.eriniumgroup.erinium_faction.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.eriniumgroup.erinium_faction.common.block.entity.EriniumChestBlockEntity;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class EriniumChestRenderer implements BlockEntityRenderer<EriniumChestBlockEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(EFC.MOD_ID, "erinium_chest"), "main");

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EFC.MOD_ID, "textures/block/erinium_chest.png");

    private final ModelPart base;
    private final ModelPart lid;

    public EriniumChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER_LOCATION);
        this.base = root.getChild("base");
        this.lid = root.getChild("lid");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Base du coffre - ajustée pour être centrée sur le bloc (0-16 en x/z)
        // Hauteur: 0-10 (10 pixels de haut)
        // On part d'un coffre centré: x: 1-15, y: 0-10, z: 1-15
        partdefinition.addOrReplaceChild("base",
            CubeListBuilder.create()
                .texOffs(0, 19)
                .addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, new CubeDeformation(0.0F)),
            PartPose.ZERO);

        // Couvercle - pivot à l'arrière pour l'ouverture
        // Position: x: 1-15, y: 10-15 (5 pixels de haut), z: 0-14 (reculé de 1px)
        // Pivot sur l'arrière du coffre (z=0) pour que ça s'ouvre vers l'avant
        PartDefinition lidPart = partdefinition.addOrReplaceChild("lid",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 10.0F, 1.0F));

        // Poignée (knob) - ENFANT du lid pour tourner avec lui
        // Position sur le devant du couvercle (face avant)
        // x: 7-9 (centré), y: -1 à 3 (4 pixels de haut), z: 14 (devant)
        lidPart.addOrReplaceChild("knob",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(7.0F, -1.0F, 14.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(EriniumChestBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        poseStack.pushPose();

        // Obtenir l'angle d'ouverture (0.0 = fermé, 1.0 = ouvert)
        float openness = blockEntity.getOpenNess(partialTick);

        // Convertir en radians: 75° = 1.309 radians
        // L'animation va de 0 à 75° en fonction de openness
        float lidAngleRadians = openness * 1.309F; // 75° en radians

        // Appliquer la rotation au couvercle
        // Le knob tournera automatiquement car il est enfant du lid
        this.lid.xRot = -lidAngleRadians;

        // Rendu avec coordonnées en pixels (Minecraft utilise 1/16ème de bloc)
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.base.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        this.lid.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
