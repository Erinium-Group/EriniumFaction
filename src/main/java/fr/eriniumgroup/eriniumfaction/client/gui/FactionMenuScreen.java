package fr.eriniumgroup.eriniumfaction.client.gui;

import fr.eriniumgroup.eriniumfaction.ARGBToInt;
import fr.eriniumgroup.eriniumfaction.EriFont;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import fr.eriniumgroup.eriniumfaction.world.inventory.FactionMenuMenu;
import fr.eriniumgroup.eriniumfaction.init.EriniumFactionModScreens;

import com.mojang.blaze3d.systems.RenderSystem;

public class FactionMenuScreen extends AbstractContainerScreen<FactionMenuMenu> implements EriniumFactionModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	public FactionMenuScreen(FactionMenuMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 420;
		this.imageHeight = 240;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	private static final ResourceLocation texture = ResourceLocation.parse("erinium_faction:textures/screens/faction_menu.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_menu_bg.png"), this.leftPos + 0, this.topPos + 0, 0, 0, 420, 240, 420, 240);

        drawText(guiGraphics, "Faction Name", EriFont::orbitronBold, 14f, -1, 10, false, true, ARGBToInt.ARGBToInt(255,255,255,255));
        drawText(guiGraphics, Component.translatable("faction.menu.resume").getString(), EriFont::orbitron, 10f, -1, 45, false, true, ARGBToInt.ARGBToInt(255,255,255,255));

        drawText(guiGraphics, Component.translatable("faction.menu.resume").getString(), EriFont::exo2, 8f, 149, 89, false, true, ARGBToInt.ARGBToInt(255,255,255,255));
        drawText(guiGraphics, Component.translatable("faction.menu.resume").getString(), EriFont::exo2, 8f, 149, 102, false, true, ARGBToInt.ARGBToInt(255,255,255,255));
        drawText(guiGraphics, Component.translatable("faction.menu.resume").getString(), EriFont::exo2, 8f, 149, 115, false, true, ARGBToInt.ARGBToInt(255,255,255,255));
        drawText(guiGraphics, Component.translatable("faction.menu.resume").getString(), EriFont::exo2, 8f, 149, 128, false, true, ARGBToInt.ARGBToInt(255,255,255,255));

		RenderSystem.disableBlend();
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

	@Override
	public void init() {
		super.init();
	}

    private void drawText(GuiGraphics guiGraphics, String text,
                          EriFont.EriFontAccess fontAccess, float fontSize,
                          float x, float y, boolean isXCentered,
                          boolean hasShadow, int color) {
        float textScale = fontSize / 8f;
        Component comp = fontAccess.get(text);

        int tw = this.minecraft.font.width(comp);
        float totalTextWidth = tw * textScale;

        // Calcul de la position X
        float xPos;
        if (isXCentered) {
            xPos = x - (totalTextWidth / 2f);
        } else if (x == -1f) {
            // Centrer sur la largeur du GUI (420px)
            xPos = (this.imageWidth - totalTextWidth) / 2f;
        } else {
            xPos = x;
        }

        // Position absolue à l'écran
        float xFinal = this.leftPos + xPos;
        float yFinal = this.topPos + y;

        // Appliquer le scale pour le texte
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, 1f);

        float xDraw = xFinal / textScale;
        float yDraw = yFinal / textScale;

        guiGraphics.drawString(this.font, comp, (int)xDraw, (int)yDraw, color, hasShadow);

        guiGraphics.pose().popPose();
    }
}