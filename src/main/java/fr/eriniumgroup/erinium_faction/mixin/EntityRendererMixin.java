package fr.eriniumgroup.erinium_faction.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.eriniumgroup.erinium_faction.client.renderer.nameplate.NameplateMixinRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(
        method = "renderNameTag",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderNameTag(Entity entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick, CallbackInfo ci) {
        if (entity instanceof Player player) {
            // Dessiner notre nameplate custom
            boolean cancelled = NameplateMixinRenderer.renderCustomNameplate(player, poseStack, bufferSource, packedLight);
            if (cancelled) {
                ci.cancel(); // Annuler le rendu vanilla
            }
        }
    }
}
