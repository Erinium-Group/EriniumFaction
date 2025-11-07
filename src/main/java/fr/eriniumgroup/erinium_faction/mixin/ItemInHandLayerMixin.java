package fr.eriniumgroup.erinium_faction.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.eriniumgroup.erinium_faction.features.vanish.VanishClientData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin pour empêcher le rendu des items en main des joueurs en vanish
 */
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    /**
     * Cancel le rendu des items en main si le joueur est en vanish
     */
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void onRenderItemInHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayer player) {
            if (VanishClientData.isVanished(player.getUUID())) {
                ci.cancel();
            }
        }
    }
}
