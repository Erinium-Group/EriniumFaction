package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FactionChestScreen extends AbstractContainerScreen<FactionChestMenu> {
    private static final ResourceLocation CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private final int containerRows;

    public FactionChestScreen(FactionChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.containerRows = menu.getChestSize() / 9;
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Dessiner le fond du coffre
        graphics.blit(CHEST_TEXTURE, x, y, 0, 0, this.imageWidth, this.containerRows * 18 + 17);

        // Dessiner l'inventaire du joueur
        graphics.blit(CHEST_TEXTURE, x, y + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Titre du coffre avec couleurs
        Component title = Component.literal("§6Coffre de Faction §7(" + menu.getChestSize() + " slots)");
        graphics.drawString(this.font, title, 8, 6, 0x404040, false);

        // Label inventaire
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }
}

