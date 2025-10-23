package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.common.network.packets.GuiForConstructButtonMessage;
import fr.eriniumgroup.erinium_faction.common.util.EFUtils;
import fr.eriniumgroup.erinium_faction.core.EriFont;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import fr.eriniumgroup.erinium_faction.gui.widgets.FactionMenuPlayerList;
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

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FactionMenuScreen extends AbstractContainerScreen<FactionMenu> implements EFScreens.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private boolean menuStateUpdateActive = false;
    private Player entity;
    ImageButton fsettings;

    EFVariables.PlayerVariables _vars;

    private File factionfile;
    private Faction faction;

    public FactionMenuScreen(FactionMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 420;
        this.imageHeight = 240;
        this._vars = entity.getData(EFVariables.PLAYER_VARIABLES);

        this.faction = container.faction != null ? container.faction : FactionManager.getPlayerFactionObject(entity.getUUID());
        String factionId = this.faction != null ? this.faction.getName() : FactionManager.getPlayerFaction(entity.getUUID());
        this.factionfile = EFUtils.Faction.FactionFileById(factionId);
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

        boolean customTooltipShown = false;
        if (mouseX > leftPos + 74 && mouseX < leftPos + 74 + 64 && mouseY > topPos + 100 && mouseY < topPos + 100 + 64) {
            String hoverText = Component.translatable("erinium_faction.faction.menu.settings").getString();
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

        guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_menu_bg.png"), this.leftPos + 0, this.topPos + 0, 0, 0, 420, 240, 420, 240);

        drawText(guiGraphics, EFUtils.F.GetFileStringValue(factionfile, "displayname"), EriFont::orbitronBold, 14f, -1, 10, false, true, EFUtils.Color.ARGBToInt(255, 255, 215, 0));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.resume").getString(), EriFont::orbitron, 10f, -1, 45, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.playerlist").getString(), EriFont::orbitron, 10f, 349, 45, true, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));

        int claimCount = (EFUtils.F.GetFileStringValue(factionfile, "claimlist") == null || EFUtils.F.GetFileStringValue(factionfile, "claimlist").isEmpty()) ? 0 : EFUtils.F.GetFileStringValue(factionfile, "claimlist").split(",").length;
        int playerCount = (EFUtils.F.GetFileStringValue(factionfile, "memberList") == null || EFUtils.F.GetFileStringValue(factionfile, "memberList").isEmpty()) ? 1 : EFUtils.F.GetFileStringValue(factionfile, "claimlist").split(",").length + 1;

        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.claims").getString() + claimCount + " / " + (int) EFUtils.F.GetFileNumberValue(factionfile, "maxClaims"), EriFont::exo2, 8f, 149, 89, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.membercount").getString() + playerCount + " / " + (int) EFUtils.F.GetFileNumberValue(factionfile, "maxPlayer"), EriFont::exo2, 8f, 149, 102, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.power").getString() + (int) EFUtils.F.GetFileNumberValue(factionfile, "power") + " / " + (int) faction.getPower(), EriFont::exo2, 8f, 149, 115, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.level").getString() + (int) EFUtils.F.GetFileNumberValue(factionfile, "factionLevel"), EriFont::exo2, 8f, -1, 128, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));

        guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_xp_bar.png"), this.leftPos + 149, this.topPos + 141, 0, 0, 122, 10, 122, 10);
        guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_xp_bar_fill.png"), this.leftPos + 150, this.topPos + 142, 0, 0, (int) (122 / faction.getXPRequiredForNextLevel((int) EFUtils.F.GetFileNumberValue(factionfile, "factionLevel"))) * (int) EFUtils.F.GetFileNumberValue(factionfile, "factionXp"), 8, 122, 8);

        drawText(guiGraphics, (int) EFUtils.F.GetFileNumberValue(factionfile, "factionXp") + " / " + (int) faction.getXPRequiredForNextLevel((int) EFUtils.F.GetFileNumberValue(factionfile, "factionLevel")), EriFont::exo2, 6.5f, -1, 154, false, true, EFUtils.Color.ARGBToInt(255, 255, 215, 0));

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

        String playerlist;
        if (EFUtils.F.GetFileStringValue(factionfile, "memberList").isEmpty()) {
            playerlist = EFUtils.F.GetFileStringValue(factionfile, "owner") + ":owner";
        } else {
            playerlist = EFUtils.F.GetFileStringValue(factionfile, "owner") + ":owner" + "," + EFUtils.F.GetFileStringValue(factionfile, "memberList");
        }

        FactionMenuPlayerList scrollableList = new FactionMenuPlayerList(this.minecraft, this.leftPos + 290, this.topPos + 54, 120, 145, playerlist, world.getServer());
        this.addRenderableWidget(scrollableList);

        if (faction != null && faction.getRank(entity.getUUID()).canManageSettings()) {
            fsettings = new ImageButton(this.leftPos + 74, this.topPos + 100, 64, 64, new WidgetSprites(ResourceLocation.parse("erinium_faction:textures/screens/fsettings.png"), ResourceLocation.parse("erinium_faction:textures/screens/fsettings_hover.png")), e -> {
                int x = FactionMenuScreen.this.x;
                int y = FactionMenuScreen.this.y;
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
            this.addRenderableWidget(fsettings);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Si tu as plusieurs scrolls, répète la ligne pour chaque (ex : shopScrollList2, etc.)
        for (var widget : this.renderables) {
            if (widget instanceof FactionMenuPlayerList scrollList) {
                if (scrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void drawText(GuiGraphics guiGraphics, String text, EriFont.EriFontAccess fontAccess, float fontSize, float x, float y, boolean isXCentered, boolean hasShadow, int color) {
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

        guiGraphics.drawString(this.font, comp, (int) xDraw, (int) yDraw, color, hasShadow);

        guiGraphics.pose().popPose();
    }
}

