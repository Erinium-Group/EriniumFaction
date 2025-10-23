package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.common.network.packets.FactionMenuSettingsButtonMessage;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenuSettingsMenu;
import fr.eriniumgroup.erinium_faction.init.EFScreens;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class FactionMenuSettingsScreen extends AbstractContainerScreen<FactionMenuSettingsMenu> implements EFScreens.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    private boolean menuStateUpdateActive = false;
    Button button_t;

    public FactionMenuSettingsScreen(FactionMenuSettingsMenu container, Inventory inventory, Component text) {
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
        guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/empty.png"), this.leftPos + 0, this.topPos + 0, 0, 0, 420, 240, 420, 240);
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
        guiGraphics.drawString(this.font, FactionIsOpenedProcedure.execute(entity), 30, 12, -12829636, false);
    }

    @Override
    public void init() {
        super.init();
        button_t = Button.builder(Component.translatable("gui.erinium_faction.faction_menu_settings.button_t"), e -> {
            int x = FactionMenuSettingsScreen.this.x;
            int y = FactionMenuSettingsScreen.this.y;
            if (true) {
                PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(0, x, y, z));
                FactionMenuSettingsButtonMessage.handleButtonAction(entity, 0, x, y, z);
            }
        }).bounds(this.leftPos + 11, this.topPos + 8, 18, 20).build();
        this.addRenderableWidget(button_t);
    }
}