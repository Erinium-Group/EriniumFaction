package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.common.util.EFUtils;
import fr.eriniumgroup.erinium_faction.core.EriFont;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.core.faction.Rank;
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

import java.util.Map;
import java.util.UUID;

public class FactionMenuScreen extends AbstractContainerScreen<FactionMenu> implements EFScreens.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private boolean menuStateUpdateActive = false;
    private Player entity;
    ImageButton fsettings;

    EFVariables.PlayerVariables _vars;

    private Faction faction;
    private boolean hasFaction;
    private String fallbackFactionName;
    private int fallbackLevel;
    private int fallbackXp;
    private int fallbackPower;
    private int fallbackMaxPower;

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

        // Préférer le snapshot envoyé par le serveur
        FactionSnapshot snap = container.snapshot;
        if (snap != null && snap.name != null && !snap.name.isEmpty()) {
            this.faction = container.faction; // peut être null côté client, mais pas requis
            this.hasFaction = true;
        } else {
            this.faction = container.faction != null ? container.faction : FactionManager.getPlayerFactionObject(entity.getUUID());
            if (this.faction != null) {
                this.hasFaction = true;
            } else {
                // Fallback: utiliser EFVariables/nom transmis
                this.fallbackFactionName = container.factionName != null && !container.factionName.isEmpty() ? container.factionName : (this._vars != null ? this._vars.factionName : "");
                this.hasFaction = (this.fallbackFactionName != null && !this.fallbackFactionName.isEmpty());
                if (this._vars != null) {
                    this.fallbackLevel = this._vars.factionLevel;
                    this.fallbackXp = this._vars.factionXp;
                    this.fallbackPower = (int) Math.round(this._vars.factionPower);
                    this.fallbackMaxPower = (int) Math.round(this._vars.factionMaxPower);
                }
            }
        }
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
        if (fsettings != null && mouseX > leftPos + 74 && mouseX < leftPos + 74 + 64 && mouseY > topPos + 100 && mouseY < topPos + 100 + 64) {
            String hoverText = Component.translatable("erinium_faction.faction.menu.settings").getString();
            guiGraphics.renderComponentTooltip(font, java.util.List.of(Component.literal(hoverText)), mouseX, mouseY);
            customTooltipShown = true;
        }

        if (!customTooltipShown) this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_menu_bg.png"), this.leftPos, this.topPos, 0, 0, 420, 240, 420, 240);

        // Données: utiliser snapshot si dispo, sinon objet faction
        FactionSnapshot snap = this.menu instanceof FactionMenu fm ? fm.snapshot : null;

        String displayName;
        int claimCount;
        int maxClaims;
        int memberCount;
        int maxPlayers;
        int level;
        int xp;
        int xpRequired;
        int currentPower;
        int maxPower;

        if (snap != null && snap.name != null && !snap.name.isEmpty()) {
            displayName = (snap.displayName == null || snap.displayName.isEmpty()) ? snap.name : snap.displayName;
            claimCount = snap.claims;
            maxClaims = snap.maxClaims;
            memberCount = snap.membersCount;
            maxPlayers = snap.maxPlayers;
            level = snap.level;
            xp = snap.xp;
            xpRequired = snap.xpRequired;
            currentPower = snap.currentPower;
            maxPower = snap.maxPower;
        } else if (hasFaction && this.faction != null) {
            displayName = this.faction.getName();
            int cc = FactionManager.countClaims(this.faction.getId());
            claimCount = cc;
            maxClaims = EFConfig.FACTION_MAX_CLAIMS.get();
            memberCount = this.faction.getMembers().size();
            maxPlayers = FactionManager.getMaxMembersFor(this.faction);
            level = this.faction.getLevel();
            xp = this.faction.getXp();
            xpRequired = this.faction.xpNeededForNextLevel();
            currentPower = (int) Math.round(this.faction.getPower());
            maxPower = (int) Math.round(this.faction.getMaxPower());
        } else if (hasFaction) {
            // Fallback via EFVariables (client-only)
            displayName = (this.fallbackFactionName != null && !this.fallbackFactionName.isEmpty()) ? this.fallbackFactionName : Component.translatable("erinium_faction.faction.menu.no_faction").getString();
            claimCount = 0; // inconnu côté client sans snapshot
            maxClaims = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_MAX_CLAIMS.get();
            memberCount = 0; // inconnu sans snapshot
            maxPlayers = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_MAX_MEMBERS.get();
            level = this.fallbackLevel;
            xp = this.fallbackXp;
            xpRequired = level > 0 ? Math.max(100, (level + 1) * (level + 1) * 50) : 0;
            currentPower = this.fallbackPower;
            maxPower = this.fallbackMaxPower;
        } else {
            displayName = Component.translatable("erinium_faction.faction.menu.no_faction").getString();
            claimCount = 0;
            maxClaims = 0;
            memberCount = 0;
            maxPlayers = 0;
            level = 0;
            xp = 0;
            xpRequired = 0;
            currentPower = 0;
            maxPower = 0;
        }

        drawText(guiGraphics, displayName, EriFont::orbitronBold, 14f, -1, 10, false, true, EFUtils.Color.ARGBToInt(255, 255, 215, 0));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.resume").getString(), EriFont::orbitron, 10f, -1, 45, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.playerlist").getString(), EriFont::orbitron, 10f, 349, 45, true, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));

        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.claims").getString() + claimCount + " / " + maxClaims, EriFont::exo2, 8f, 149, 89, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.membercount").getString() + memberCount + " / " + maxPlayers, EriFont::exo2, 8f, 149, 102, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.power").getString() + currentPower + " / " + Math.max(0, maxPower), EriFont::exo2, 8f, 149, 115, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.level").getString() + level, EriFont::exo2, 8f, -1, 128, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));

        if (xpRequired > 0) {
            guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_xp_bar.png"), this.leftPos + 149, this.topPos + 141, 0, 0, 122, 10, 122, 10);
            int fillWidth = Math.min(122, Math.max(0, (int) Math.round(122.0 * xp / (double) xpRequired)));
            if (fillWidth > 0) {
                guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_xp_bar_fill.png"), this.leftPos + 150, this.topPos + 142, 0, 0, fillWidth, 8, 122, 8);
            }
            drawText(guiGraphics, xp + " / " + xpRequired, EriFont::exo2, 6.5f, -1, 154, false, true, EFUtils.Color.ARGBToInt(255, 255, 215, 0));
        }

        if (!hasFaction && (snap == null || snap.name == null || snap.name.isEmpty())) {
            drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.no_faction_hint").getString(), EriFont::exo2, 8f, -1, 160, false, true, EFUtils.Color.ARGBToInt(255, 200, 200, 200));
        }

        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.closeContainer();
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

        // Construire la liste des joueurs
        FactionSnapshot snap = this.menu instanceof FactionMenu fm ? fm.snapshot : null;
        if (snap != null && snap.name != null && !snap.name.isEmpty()) {
            StringBuilder playerlist = new StringBuilder();
            for (var e : snap.membersRank.entrySet()) {
                UUID id = e.getKey();
                String rank = e.getValue();
                String name = snap.memberNames.getOrDefault(id, id.toString());
                if (playerlist.length() > 0) playerlist.append(",");
                playerlist.append(id).append(":").append(rank).append(":").append(name);
            }
            FactionMenuPlayerList scrollableList = new FactionMenuPlayerList(this.minecraft, this.leftPos + 290, this.topPos + 54, 120, 145, playerlist.toString(), world != null ? world.getServer() : null);
            this.addRenderableWidget(scrollableList);

            Rank rank = (this.faction != null) ? this.faction.getRank(entity.getUUID()) : null;
            if (rank != null && rank.canManageSettings()) {
                fsettings = new ImageButton(this.leftPos + 74, this.topPos + 100, 64, 64, new WidgetSprites(ResourceLocation.parse("erinium_faction:textures/screens/fsettings.png"), ResourceLocation.parse("erinium_faction:textures/screens/fsettings_hover.png")), e -> {
                    int x = FactionMenuScreen.this.x;
                    int y = FactionMenuScreen.this.y;
                }) {
                    @Override
                    public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
                        guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
                    }
                };
                this.addRenderableWidget(fsettings);
            }
        } else if (hasFaction && faction != null) {
            StringBuilder playerlist = new StringBuilder();
            for (Map.Entry<UUID, Faction.Member> t : faction.getMembers().entrySet()) {
                if (!playerlist.isEmpty()) playerlist.append(",");
                String rankId = t.getValue().rankId;
                String name = t.getValue().nameCached != null ? t.getValue().nameCached : t.getKey().toString();
                playerlist.append(t.getKey()).append(":").append(rankId).append(":").append(name);
            }
            FactionMenuPlayerList scrollableList = new FactionMenuPlayerList(this.minecraft, this.leftPos + 290, this.topPos + 54, 120, 145, playerlist.toString(), world != null ? world.getServer() : null);
            this.addRenderableWidget(scrollableList);

            Rank rank = faction.getRank(entity.getUUID());
            if (rank != null && rank.canManageSettings()) {
                fsettings = new ImageButton(this.leftPos + 74, this.topPos + 100, 64, 64, new WidgetSprites(ResourceLocation.parse("erinium_faction:textures/screens/fsettings.png"), ResourceLocation.parse("erinium_faction:textures/screens/fsettings_hover.png")), e -> {
                    int x = FactionMenuScreen.this.x;
                    int y = FactionMenuScreen.this.y;
                }) {
                    @Override
                    public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
                        guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
                    }
                };
                this.addRenderableWidget(fsettings);
            }
        } else {
            fsettings = null;
        }
    }

    private void drawText(GuiGraphics guiGraphics, String text, EriFont.EriFontAccess fontAccess, float fontSize, float x, float y, boolean isXCentered, boolean hasShadow, int color) {
        float textScale = fontSize / 8f;
        if (text == null) text = "";
        Component comp = fontAccess.get(text);

        int tw = this.minecraft.font.width(comp);
        float totalTextWidth = tw * textScale;

        float xPos;
        if (isXCentered) {
            xPos = x - (totalTextWidth / 2f);
        } else if (x == -1f) {
            xPos = (this.imageWidth - totalTextWidth) / 2f;
        } else {
            xPos = x;
        }

        float xFinal = this.leftPos + xPos;
        float yFinal = this.topPos + y;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, 1f);

        float xDraw = xFinal / textScale;
        float yDraw = yFinal / textScale;

        guiGraphics.drawString(this.font, comp, (int) xDraw, (int) yDraw, color, hasShadow);

        guiGraphics.pose().popPose();
    }
}

