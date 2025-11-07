package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.gui.menus.CreativeBatteryMenu;
import fr.eriniumgroup.erinium_faction.gui.widgets.FaceConfigButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class CreativeBatteryScreen extends AbstractContainerScreen<CreativeBatteryMenu> {
    private FaceConfigButton configButton;

    public CreativeBatteryScreen(CreativeBatteryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 90;
    }

    @Override
    protected void init() {
        super.init();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        configButton = new FaceConfigButton(
            x + this.imageWidth - 25,
            y + 5,
            this.menu.getBlockEntity().getBlockPos(),
            this.menu.getBlockEntity().getFaceConfiguration(),
            this
        );
        this.addRenderableWidget(configButton);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Fond
        gg.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);

        // Barre d'énergie (toujours pleine - créative)
        int barHeight = 60;

        // Barre fond
        gg.fill(x + 80, y + 20, x + 96, y + 20 + barHeight, 0xFF404040);
        // Barre pleine (violet/magenta pour créative)
        gg.fill(x + 80, y + 20, x + 96, y + 20 + barHeight, 0xFFFF00FF);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, 8, 6, 0x404040, false);

        // "Creative" badge
        Component creativeText = Component.literal("CREATIVE");
        gg.drawString(this.font, creativeText, 8, 25, 0xFFFF00FF, false);

        // Afficher le débit de sortie
        int outRate = this.menu.getBlockEntity().getLastOutPerTick();
        Component outText = Component.translatable("tooltip.erinium_faction.energy_out").append(": " + outRate + " FE/t");
        gg.drawString(this.font, outText, 8, 40, 0xAA0000, false);

        // "Infinite Energy"
        Component infiniteText = Component.literal("∞ FE");
        gg.drawString(this.font, infiniteText, 8, 50, 0xFFD700, false);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (configButton != null && configButton.isHovered()) {
            graphics.renderTooltip(this.font, Component.translatable("gui.erinium_faction.face_config.tooltip"), mouseX, mouseY);
        }

        // Tooltip pour la barre d'énergie
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        if (mouseX >= x + 80 && mouseX < x + 96 && mouseY >= y + 20 && mouseY < y + 80) {
            graphics.renderTooltip(this.font, Component.literal("Infinite Energy"), mouseX, mouseY);
        }
    }
}