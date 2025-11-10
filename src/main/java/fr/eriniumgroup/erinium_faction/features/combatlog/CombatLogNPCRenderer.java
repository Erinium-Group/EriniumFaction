package fr.eriniumgroup.erinium_faction.features.combatlog;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renderer pour afficher le CombatLogNPC avec le skin du joueur
 */
@OnlyIn(Dist.CLIENT)
public class CombatLogNPCRenderer extends MobRenderer<CombatLogNPC, PlayerModel<CombatLogNPC>> {

    public CombatLogNPCRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);

        // Ajouter les layers pour un rendu réaliste
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(CombatLogNPC entity) {
        GameProfile profile = entity.getGameProfile();
        if (profile != null && profile.getId() != null) {
            // Utiliser le système de cache de skin de Minecraft
            Minecraft minecraft = Minecraft.getInstance();
            PlayerSkin playerSkin = minecraft.getSkinManager().getInsecureSkin(profile);

            if (playerSkin != null && playerSkin.texture() != null) {
                return playerSkin.texture();
            }

            // Fallback sur le skin par défaut basé sur l'UUID
            return DefaultPlayerSkin.get(profile.getId()).texture();
        }

        // Fallback ultime sur Steve (utiliser un UUID aléatoire pour obtenir le skin par défaut)
        return DefaultPlayerSkin.get(java.util.UUID.randomUUID()).texture();
    }

    @Override
    public void render(CombatLogNPC entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Rendre le modèle de joueur normal
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
