package fr.eriniumgroup.erinium_faction.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.eriniumgroup.erinium_faction.client.renderer.BannerTextureManager;
import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin pour CapeLayer qui remplace la texture de la cape vanilla par la bannière de faction
 */
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    /**
     * Intercepte le rendu de la cape et utilise la texture de bannière de faction si activé
     */
    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderCape(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        AbstractClientPlayer player,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        CallbackInfo ci
    ) {
        // Vérifier si le joueur a activé la cape de faction
        EFVariables.PlayerVariables variables = player.getData(EFVariables.PLAYER_VARIABLES);
        if (!variables.factionCapeEnabled) {
            return; // Laisser le vanilla gérer
        }

        // Vérifier si le joueur a une faction
        if (variables.factionId == null || variables.factionId.isEmpty()) {
            return; // Pas de faction = pas de cape custom
        }

        // Vérifier si la texture de bannière existe
        ResourceLocation bannerTexture = BannerTextureManager.getBannerTexture(variables.factionId);
        if (bannerTexture == null) {
            return; // Pas de texture = pas de cape custom
        }

        // Annuler le rendu vanilla et rendre notre cape custom
        ci.cancel();

        // Rendre la cape avec la texture de bannière
        renderFactionCape(poseStack, buffer, packedLight, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, bannerTexture);
    }

    /**
     * Rend la cape avec la texture de bannière de faction
     * Utilise la même logique que CapeLayer vanilla
     */
    private void renderFactionCape(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        AbstractClientPlayer player,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        ResourceLocation capeTexture
    ) {
        // Conditions vanilla pour afficher la cape
        if (player.isInvisible()) return;
        if (!player.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.CAPE)) return;

        // Ne pas afficher si le joueur porte une elytra
        net.minecraft.world.item.ItemStack chestItem = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (chestItem.is(net.minecraft.world.item.Items.ELYTRA)) return;

        // Reproduire le code vanilla de CapeLayer mais avec notre texture
        poseStack.pushPose();

        // Appliquer les transformations du corps du joueur pour que la cape suive
        // Le corps se penche quand on sneak, la cape doit suivre
        this.getParentModel().body.translateAndRotate(poseStack);

        // Décaler de 0.125F (vanilla) + 0.0625F (1 pixel) = 0.1875F pour coller au dos
        poseStack.translate(0.0F, 0.0F, 0.1875F);

        double d0 = net.minecraft.util.Mth.lerp((double)partialTicks, player.xCloakO, player.xCloak)
                  - net.minecraft.util.Mth.lerp((double)partialTicks, player.xo, player.getX());
        double d1 = net.minecraft.util.Mth.lerp((double)partialTicks, player.yCloakO, player.yCloak)
                  - net.minecraft.util.Mth.lerp((double)partialTicks, player.yo, player.getY());
        double d2 = net.minecraft.util.Mth.lerp((double)partialTicks, player.zCloakO, player.zCloak)
                  - net.minecraft.util.Mth.lerp((double)partialTicks, player.zo, player.getZ());

        float f = net.minecraft.util.Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        double d3 = (double)net.minecraft.util.Mth.sin(f * (float)(Math.PI / 180.0));
        double d4 = (double)(-net.minecraft.util.Mth.cos(f * (float)(Math.PI / 180.0)));

        float f1 = (float)d1 * 10.0F;
        f1 = net.minecraft.util.Mth.clamp(f1, -6.0F, 32.0F);

        float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
        f2 = net.minecraft.util.Mth.clamp(f2, 0.0F, 150.0F);

        float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
        f3 = net.minecraft.util.Mth.clamp(f3, -20.0F, 20.0F);

        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        float f4 = net.minecraft.util.Mth.lerp(partialTicks, player.oBob, player.bob);
        f1 += net.minecraft.util.Mth.sin(net.minecraft.util.Mth.lerp(partialTicks, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * f4;

        if (player.isCrouching()) {
            f1 += 25.0F;
        }

        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(f3 / 2.0F));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));

        // Utiliser notre renderer custom avec la texture de bannière
        fr.eriniumgroup.erinium_faction.client.renderer.CustomCapeRenderer.renderCustomCape(
            poseStack,
            buffer,
            capeTexture,
            packedLight,
            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
