package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.gui.menus.TitaniumCompressorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TitaniumCompressorScreen extends AbstractContainerScreen<TitaniumCompressorMenu> {
    private static final ResourceLocation GUI_TEX = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/titanium_compressor.png");

    public TitaniumCompressorScreen(TitaniumCompressorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        gg.blit(GUI_TEX, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, 8, 6, 0x404040, false);
        gg.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }
}
