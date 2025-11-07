package fr.eriniumgroup.erinium_faction.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fr.eriniumgroup.erinium_faction.client.renderer.BannerTextureManager;
import fr.eriniumgroup.erinium_faction.client.renderer.CustomBannerRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin qui remplace COMPLETEMENT le rendu vanilla pour les bannières custom
 * On reproduit exactement le comportement vanilla mais avec notre texture
 */
@Mixin(BannerRenderer.class)
public class BannerRendererMixin {

    @Shadow @Final private ModelPart flag;
    @Shadow @Final private ModelPart pole;
    @Shadow @Final private ModelPart bar;

    @Unique
    private boolean erinium_faction$isCustomBanner = false;

    @Unique
    private String erinium_faction$factionId = null;

    /**
     * Remplace COMPLETEMENT le rendu si c'est une bannière custom
     */
    @Inject(
        method = "render(Lnet/minecraft/world/level/block/entity/BannerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onRender(
        BannerBlockEntity bannerEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay,
        CallbackInfo ci
    ) {
        ItemStack item = bannerEntity.getItem();
        if (item.has(DataComponents.CUSTOM_DATA)) {
            CustomData customData = item.get(DataComponents.CUSTOM_DATA);
            if (customData != null && customData.copyTag().getBoolean("erinium_custom_banner")) {
                String factionId = customData.copyTag().getString("erinium_faction_id");
                ResourceLocation texture = BannerTextureManager.getBannerTexture(factionId);

                if (texture != null) {
                    // Reproduire EXACTEMENT le comportement vanilla
                    renderCustomBanner(bannerEntity, partialTick, poseStack, buffer, combinedLight, combinedOverlay, texture);
                    ci.cancel(); // Annuler le rendu vanilla
                }
            }
        }
    }

    /**
     * Reproduction EXACTE du code vanilla mais avec notre texture
     */
    @Unique
    private void renderCustomBanner(
        BannerBlockEntity bannerEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay,
        ResourceLocation customTexture
    ) {
        float scale = 0.6666667F;
        boolean isGui = bannerEntity.getLevel() == null;

        poseStack.pushPose();

        long time;
        if (isGui) {
            time = 0L;
            poseStack.translate(0.5F, 0.5F, 0.5F);
            this.pole.visible = true;
        } else {
            time = bannerEntity.getLevel().getGameTime();
            BlockState blockstate = bannerEntity.getBlockState();

            if (blockstate.getBlock() instanceof BannerBlock) {
                // Bannière au sol
                poseStack.translate(0.5F, 0.5F, 0.5F);
                float rotation = -RotationSegment.convertToDegrees(blockstate.getValue(BannerBlock.ROTATION));
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                this.pole.visible = true;
            } else {
                // Bannière murale
                poseStack.translate(0.5F, -0.16666667F, 0.5F);
                float rotation = -((Direction)blockstate.getValue(WallBannerBlock.FACING)).toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                poseStack.translate(0.0F, -0.3125F, -0.4375F);
                this.pole.visible = false;
            }
        }

        poseStack.pushPose();
        poseStack.scale(scale, -scale, -scale);

        // Rendre le poteau/barre en bois (vanilla)
        VertexConsumer woodConsumer = ModelBakery.BANNER_BASE.buffer(buffer, RenderType::entitySolid);
        this.pole.render(poseStack, woodConsumer, combinedLight, combinedOverlay);
        this.bar.render(poseStack, woodConsumer, combinedLight, combinedOverlay);

        // Animation du drapeau (vanilla)
        BlockPos blockpos = bannerEntity.getBlockPos();
        float waveTime = ((float)Math.floorMod((long)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + time, 100L) + partialTick) / 100.0F;
        this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(((float)Math.PI * 2F) * waveTime)) * (float)Math.PI;
        this.flag.y = -32.0F;

        // Appliquer les transformations du flag
        poseStack.pushPose();
        poseStack.translate(0.0F, this.flag.y / 16.0F, 0.0F);
        poseStack.mulPose(Axis.XP.rotation(this.flag.xRot));

        // Rendre NOTRE texture custom
        CustomBannerRenderer.renderCustomFlag(poseStack, buffer, customTexture, combinedLight, combinedOverlay);

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }
}
