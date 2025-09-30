package fr.eriniumgroup.eriniumfaction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.gui.GuiComponent;

@Mod.EventBusSubscriber(modid = "erinium_faction", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BlockHpRenderer {

	private static BlockPos lastPos = null;
	private static int lastCur = 0;
	private static int lastBase = 0;

	// Cette méthode est appelée par le packet
	public static void updateBlockHp(BlockPos pos, int cur, int base) {
		lastPos = pos;
		lastCur = cur;
		lastBase = base;
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || !player.getMainHandItem().is(Items.STICK)) return;

		HitResult hit = mc.hitResult;
		if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

		BlockHitResult blockHit = (BlockHitResult) hit;
		BlockPos pos = blockHit.getBlockPos();

		if (!pos.equals(lastPos)) return;

		int cur = lastCur;
		int base = lastBase;
		if (base <= 0) return;

		float pct = (cur * 100f) / base;

		PoseStack stack = event.getMatrixStack();
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		// Dimensions de la barre
		int barWidth = 100;
		int barHeight = 6;
		int x = (screenWidth - barWidth) / 2;
		int y = screenHeight - 84;

		// Bordure subtile
		GuiComponent.fill(stack, x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);

		// Fond de la barre
		GuiComponent.fill(stack, x, y, x + barWidth, y + barHeight, 0xCC1A1A1A);

		// Barre de vie colorée
		int filledWidth = (int) (barWidth * (pct / 100f));
		int color = pct >= 75f ? 0xFF00DD00 :
				pct >= 50f ? 0xFFFFDD00 :
						pct >= 25f ? 0xFFFF8800 :
								0xFFDD0000;

		if (filledWidth > 0) {
			GuiComponent.fill(stack, x, y, x + filledWidth, y + barHeight, color);
		}

		// Texte centré dans la barre
		stack.pushPose();
		float scaleFactor = 0.7f; // Pour rendre le code plus clair
		stack.scale(scaleFactor, scaleFactor, 1.0f);

		String text = cur + " / " + base;
		int textWidth = mc.font.width(text);
		int lineHeight = mc.font.lineHeight; // Hauteur du texte NON-SCALÉ (environ 9 ou 10 pixels)

		// 1. Calcul de la position X (scaled) - fonctionne déjà
		float scaledX = (x + barWidth / 2.0f) / scaleFactor - textWidth / 2.0f;

		// 2. Calcul de la position Y (scaled) - L'erreur est là
		// Centre vertical de la barre (non-scalé): y + barHeight / 2.0f
		// Début Y du texte (non-scalé) pour centrage: (y + barHeight / 2.0f) - (lineHeight * scaleFactor / 2.0f)

		// Cependant, le draw() du FontRenderer prend des coordonnées qui doivent être divisées par le scale!

		// Position Y non-scalée où le texte DOIT COMMENCER
		float textYStart = y + (barHeight / 2.0f) - (lineHeight * scaleFactor / 2.0f);

		// Position Y à donner au draw() après la mise à l'échelle
		float scaledY = textYStart / scaleFactor;

		// Calcul de Y (issu de la correction précédente)
		float centerY_NonScaled = y + barHeight / 2.0f;
		float centerY_Scaled = centerY_NonScaled / scaleFactor;

		// Si le texte est trop haut, la valeur finale est trop petite.
		// On ajoute un décalage positif (par exemple, 1.0f ou 2.0f)
		float offset = 1.0f; // <--- COMMENCEZ AVEC 1.0f et augmentez si besoin

		float finalScaledY = centerY_Scaled - (lineHeight / 2.0f) + offset; // <--- AUGMENTEZ L'OFFSET

		// Texte blanc
		mc.font.drawShadow(stack, text, scaledX, finalScaledY, 0xFFFFFFFF);

		stack.popPose();
	}
}