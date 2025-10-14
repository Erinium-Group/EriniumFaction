package fr.eriniumgroup.eriniumfaction.client.gui;

import fr.eriniumgroup.eriniumfaction.ARGBToInt;
import fr.eriniumgroup.eriniumfaction.EriFont;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import fr.eriniumgroup.eriniumfaction.world.inventory.FactionMenuMenu;
import fr.eriniumgroup.eriniumfaction.init.EriniumFactionModScreens;

import com.mojang.blaze3d.systems.RenderSystem;

public class FactionMenuScreen extends AbstractContainerScreen<FactionMenuMenu> implements EriniumFactionModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	private final int guiWidthVirtual = 1920;
	private final int guiHeightVirtual = 1080;

	private float uiScale;
	private float uiOriginX, uiOriginY;

	// calcule à partir de width/height
	private void computeUiMetrics() {
		float sx = ((float) this.width)  / guiWidthVirtual;
		float sy = ((float) this.height) / guiHeightVirtual;
		this.uiScale   = Math.min(sx, sy);
		this.uiOriginX = (this.width  / this.uiScale - guiWidthVirtual)  / 2f;
		this.uiOriginY = (this.height / this.uiScale - guiHeightVirtual) / 2f;
	}


	public FactionMenuScreen(FactionMenuMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 1920;
		this.imageHeight = 1080;

		float sx = ((float) this.width)  / guiWidthVirtual;
		float sy = ((float) this.height) / guiHeightVirtual;
		this.uiScale   = Math.min(sx, sy);
		this.uiOriginX = (this.width  / this.uiScale - guiWidthVirtual)  / 2f;
		this.uiOriginY = (this.height / this.uiScale - guiHeightVirtual) / 2f;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/screens/faction_menu_bg.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		int screenW = this.width;
		int screenH = this.height;
		float scaleX = (float) screenW / guiWidthVirtual;
		float scaleY = (float) screenH / guiHeightVirtual;
		float scale = Math.min(scaleX, scaleY); // pour garder les proportions

		guiGraphics.pose().pushPose();
		guiGraphics.pose().scale(Math.min(1.0f, scaleX), Math.min(1.0f, scaleY), 1.0f);
		// Position centrée en virtuel
		int offsetX = (int) ((screenW / scale - guiWidthVirtual) / 2f);
		int offsetY = (int) ((screenH / scale - guiHeightVirtual) / 2f);

		guiGraphics.blit(texture, 0, 0, 0, 0, guiWidthVirtual, guiHeightVirtual, guiWidthVirtual, guiHeightVirtual);

		// --- TEXTE 48 px virtuels ---
		drawText(guiGraphics, scale, "Faction Menu", EriFont::orbitronBold,48f, -1f, 20f, false, true, 0xFFFFD700);

		// --- TEXTE 38 px virtuels ---
		drawText(guiGraphics, scale, "Faction NAME", EriFont::orbitronBold,38f, -1f, 120f, false, true, 0xFFFFD700);

		// --- TEXTE 28 px virtuels ---
		drawText(guiGraphics, scale, "Résumé", EriFont::exo2,28f, -1f, 186f, false, true, ARGBToInt.ARGBToInt(255, 255, 255, 255));
		drawText(guiGraphics, scale, "Liste des membres", EriFont::exo2,28f, (float) ((1320 + 1889) / 2), 186f, true, true, ARGBToInt.ARGBToInt(255, 255, 255, 255));
		drawText(guiGraphics, scale, "Petites quêtes", EriFont::exo2,28f, (float) ((30 + 599) / 2), 186f, true, true, ARGBToInt.ARGBToInt(255, 255, 255, 255));

		// --- TEXTE 20 px virtuels ---
		drawText(guiGraphics, scale, "Claim", EriFont::exo2,20f, 685f, 442f, false, true, ARGBToInt.ARGBToInt(255, 255, 255, 255));
		drawText(guiGraphics, scale, "Power", EriFont::exo2,20f, 685f, 492f, false, true, ARGBToInt.ARGBToInt(255, 255, 255, 255));
		drawText(guiGraphics, scale, "Membres", EriFont::exo2,20f, 685f, 542f, false, true, ARGBToInt.ARGBToInt(255, 255, 255, 255));
		drawText(guiGraphics, scale, "Niveau", EriFont::exo2,20f, 685f, 592f, false, true, ARGBToInt.ARGBToInt(255, 255, 255, 255));

		guiGraphics.pose().popPose();
		RenderSystem.disableBlend();
	}

	private void drawText(GuiGraphics guiGraphics, float scale, String text, EriFont.EriFontAccess fontAccess, float virtualSize, float virtualX, float virtualY, boolean isXCentered, boolean hasShadow, int color) {
		float textScale = virtualSize / 8f;
		Component comp;

		// Déterminer la police (comme dans votre code original)
		comp = fontAccess.get(text);

		int tw = this.minecraft.font.width(comp);
		float totalTextWidthVirt = tw * textScale;

		// Calcul de la position X virtuelle
		float xVirt;

		if (isXCentered) {
			// Le texte est centré sur la position virtuelle fournie (ex: 960 pour le centre de l'écran)
			xVirt = virtualX - (totalTextWidthVirt / 2f);
		} else if (virtualX == -1f) {
			// Cas spécial : si virtualX est -1 (par convention), on centre sur l'écran (1920px)
			xVirt = (guiWidthVirtual - totalTextWidthVirt) / 2f;
		} else {
			// Le texte commence à la position virtuelle fournie (alignement à gauche)
			xVirt = virtualX;
		}

		// Pour ne pas oublier l'origine Y de l'écran
		float originY = (height / scale - guiHeightVirtual) / 2f;
		float yVirt = originY + virtualY;

		// APPLICATION DE LA POSE MATRICIELLE
		guiGraphics.pose().pushPose();
		guiGraphics.pose().scale(textScale, textScale, 1f);

		float xDraw = (xVirt / textScale);
		float yDraw = (yVirt / textScale);

		guiGraphics.drawString(this.font, comp, (int)xDraw, (int)yDraw, color, hasShadow);

		guiGraphics.pose().popPose();
	}

	private void drawImage(GuiGraphics guiGraphics, float scale, ResourceLocation texture, float virtualWidth, float virtualHeight, float virtualX, float virtualY, boolean isXCentered, boolean isYCentered) {
		// Dimensions virtuelles de référence
		float guiWidthVirtual = 1920f;
		float guiHeightVirtual = 1080f;

		// Calcul de la position X virtuelle
		float xVirt;
		if (isXCentered) {
			xVirt = virtualX - (virtualWidth / 2f);
		} else if (virtualX == -1f) {
			xVirt = (guiWidthVirtual - virtualWidth) / 2f;
		} else {
			xVirt = virtualX;
		}

		// Calcul de la position Y virtuelle
		float yVirt;
		if (isYCentered) {
			yVirt = virtualY - (virtualHeight / 2f);
		} else {
			yVirt = virtualY;
		}

		// Origine Y de l'écran
		int screenHeight = minecraft.getWindow().getGuiScaledHeight();
		float originY = (screenHeight / scale - guiHeightVirtual) / 2f;
		yVirt = originY + yVirt;

		// APPLICATION DE LA POSE MATRICIELLE
		guiGraphics.pose().pushPose();
		guiGraphics.pose().scale(scale, scale, 1f);

		float xDraw = xVirt / scale;
		float yDraw = yVirt / scale;
		float wDraw = virtualWidth / scale;
		float hDraw = virtualHeight / scale;

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		// Dessiner l'image
		guiGraphics.blit(texture, (int)xDraw, (int)yDraw, 0, 0, (int)wDraw, (int)hDraw, (int)wDraw, (int)hDraw);

		guiGraphics.pose().popPose();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

	}

	private int toRealX(float virt) { return net.minecraft.util.Mth.floor((uiOriginX + virt) * uiScale); }
	private int toRealY(float virt) { return net.minecraft.util.Mth.floor((uiOriginY + virt) * uiScale); }
	private int toRealS(float virt) { return net.minecraft.util.Mth.floor(virt * uiScale); }


	@Override
	public void init() {
		super.init();

		/*
		computeUiMetrics();

		int bx = toRealX(200);   // 200 px virtuels → réel
		int by = toRealY(150);
		int bw = toRealS(320);
		int bh = toRealS(64);
		 */
	}

	@Override
	public void resize(net.minecraft.client.Minecraft mc, int w, int h) {
		super.resize(mc, w, h);
		computeUiMetrics();
	}

}