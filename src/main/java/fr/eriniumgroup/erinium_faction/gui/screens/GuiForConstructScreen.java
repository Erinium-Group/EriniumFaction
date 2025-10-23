package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.gui.menus.GuiForConstructMenu;
import fr.eriniumgroup.erinium_faction.init.EFScreens;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GuiForConstructScreen extends AbstractContainerScreen<GuiForConstructMenu> implements EFScreens.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    private boolean menuStateUpdateActive = false;
    ImageButton imagebutton_permission;

    public GuiForConstructScreen(GuiForConstructMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void updateMenuState(int elementType, String name, Object elementState) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }

    private static final ResourceLocation texture = ResourceLocation.parse("erinium_faction:textures/screens/gui_for_construct.png");

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        boolean customTooltipShown = false;
        if (mouseX > leftPos + -17 && mouseX < leftPos + 53 && mouseY > topPos + -11 && mouseY < topPos + 57) {
            String hoverText = CurrentChunkFactionIdProcedure.execute(world, x, z);
            if (hoverText != null) {
                guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
            }
            customTooltipShown = true;
        }
        if (mouseX > leftPos + 85 && mouseX < leftPos + 134 && mouseY > topPos + 4 && mouseY < topPos + 38) {
            String hoverText = CurrentChunkFactionIdProcedure.execute(world, x, z);
            if (hoverText != null) {
                guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
            }
            customTooltipShown = true;
        }
        if (!customTooltipShown) this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
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
        imagebutton_permission = new ImageButton(this.leftPos + -32, this.topPos + -32, 256, 256, new WidgetSprites(ResourceLocation.parse("erinium_faction:textures/screens/permission.png"), ResourceLocation.parse("erinium_faction:textures/screens/permission_hover.png")), e -> {
            int x = GuiForConstructScreen.this.x;
            int y = GuiForConstructScreen.this.y;
            if (true) {
                PacketDistributor.sendToServer(new GuiForConstructButtonMessage(0, x, y, z));
                GuiForConstructButtonMessage.handleButtonAction(entity, 0, x, y, z);
            }
        }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
                guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
            }
        };
        this.addRenderableWidget(imagebutton_permission);
    }
}