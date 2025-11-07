package fr.eriniumgroup.erinium_faction.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.eriniumgroup.erinium_faction.client.renderer.CustomTabListRenderer;
import fr.eriniumgroup.erinium_faction.common.config.EFClientConfig;
import fr.eriniumgroup.erinium_faction.features.vanish.VanishClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mixin pour remplacer la tab list vanilla par notre version custom
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private CustomTabListRenderer erinium_faction$customRenderer;

    /**
     * Filtre les joueurs en vanish de la liste affichée dans la tab list
     */
    @Inject(method = "getPlayerInfos", at = @At("RETURN"), cancellable = true)
    private void onGetPlayerInfos(CallbackInfoReturnable<List<PlayerInfo>> cir) {
        List<PlayerInfo> original = cir.getReturnValue();

        // Filtrer les joueurs en vanish
        List<PlayerInfo> filtered = original.stream()
            .filter(info -> {
                GameProfile profile = info.getProfile();
                return !VanishClientData.isVanished(profile.getId());
            })
            .collect(Collectors.toList());

        cir.setReturnValue(filtered);
    }

    /**
     * Remplace le rendu de la tab list par notre version custom
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int screenWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        if (EFClientConfig.TAB_LIST_ENABLED.get()) {
            // Initialiser le renderer custom si nécessaire
            if (erinium_faction$customRenderer == null) {
                erinium_faction$customRenderer = new CustomTabListRenderer(minecraft);
            }

            // Rendu custom
            erinium_faction$customRenderer.render(guiGraphics, screenWidth, guiGraphics.guiHeight(), scoreboard, objective);

            // Cancel le rendu vanilla
            ci.cancel();
        }
    }
}
