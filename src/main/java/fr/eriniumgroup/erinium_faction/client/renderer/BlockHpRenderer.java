package fr.eriniumgroup.erinium_faction.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
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
	public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
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

		GuiGraphics guiGraphics = event.getGuiGraphics();
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		// Dimensions de la barre
		int barWidth = 100;
		int barHeight = 6;
		int x = (screenWidth - barWidth) / 2;
		int y = screenHeight - 84;

		// Bordure subtile
		guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);

		// Fond de la barre
		guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xCC1A1A1A);

		// Barre de vie colorée
		int filledWidth = (int) (barWidth * (pct / 100f));
		int color = pct >= 75f ? 0xFF00DD00 :
				pct >= 50f ? 0xFFFFDD00 :
						pct >= 25f ? 0xFFFF8800 :
								0xFFDD0000;

		if (filledWidth > 0) {
			guiGraphics.fill(x, y, x + filledWidth, y + barHeight, color);
		}

		// Texte centré dans la barre
		guiGraphics.pose().pushPose();
		float scaleFactor = 0.7f;
		guiGraphics.pose().scale(scaleFactor, scaleFactor, 1.0f);

		String text = cur + " / " + base;
		int textWidth = mc.font.width(text);
		int lineHeight = mc.font.lineHeight;

		float scaledX = (x + barWidth / 2.0f) / scaleFactor - textWidth / 2.0f;
		float textYStart = y + (barHeight / 2.0f) - (lineHeight * scaleFactor / 2.0f);
		float scaledY = textYStart / scaleFactor;
		float centerY_NonScaled = y + barHeight / 2.0f;
		float centerY_Scaled = centerY_NonScaled / scaleFactor;
		float offset = 1.0f;
		float finalScaledY = centerY_Scaled - (lineHeight / 2.0f) + offset;

		guiGraphics.drawString(mc.font, text, (int)scaledX, (int)finalScaledY, 0xFFFFFFFF, true);

		guiGraphics.pose().popPose();
	}
}