package fr.eriniumgroup.erinium_faction.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.eriniumgroup.erinium_faction.client.renderer.BannerTextureManager;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin pour intercepter le rendu des boucliers avec bannière custom
 */
@Mixin(BlockEntityWithoutLevelRenderer.class)
public class ShieldRendererMixin {

    @Shadow
    private ShieldModel shieldModel;

    /**
     * Injecte dans renderByItem pour gérer le rendu des boucliers avec bannière custom
     */
    @Inject(
        method = "renderByItem",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onRenderByItem(
        ItemStack stack,
        ItemDisplayContext displayContext,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay,
        CallbackInfo ci
    ) {
        // Vérifier si c'est un bouclier avec une bannière
        if (stack.getItem() instanceof net.minecraft.world.item.ShieldItem) {
            // Vérifier si le bouclier a une bannière avec un tag custom
            if (stack.has(DataComponents.BANNER_PATTERNS)) {
                // Chercher dans les components custom data
                if (stack.has(DataComponents.CUSTOM_DATA)) {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null && customData.copyTag().getBoolean("erinium_custom_banner")) {
                        String factionId = customData.copyTag().getString("erinium_faction_id");

                        // Vérifier si on a la texture
                        ResourceLocation texture = BannerTextureManager.getBannerTexture(factionId);
                        if (texture != null) {
                            // Rendre le bouclier avec la texture custom
                            renderCustomShield(stack, poseStack, buffer, combinedLight, combinedOverlay, texture);
                            ci.cancel();
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Rend un bouclier avec une bannière custom
     */
    private void renderCustomShield(
        ItemStack stack,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay,
        ResourceLocation bannerTexture
    ) {
        // Rendu du bouclier de base
        Material material = Sheets.SHIELD_BASE;
        VertexConsumer baseConsumer = material.sprite().wrap(
            material.buffer(buffer, RenderType::entitySolid)
        );
        this.shieldModel.handle().render(poseStack, baseConsumer, combinedLight, combinedOverlay);

        // Rendu de la bannière custom sur le bouclier
        RenderType renderType = RenderType.entitySolid(bannerTexture);
        VertexConsumer bannerConsumer = buffer.getBuffer(renderType);
        this.shieldModel.plate().render(poseStack, bannerConsumer, combinedLight, combinedOverlay);
    }
}
