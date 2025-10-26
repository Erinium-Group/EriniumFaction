package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.common.network.packets.FactionMenuSettingsButtonMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.OpenFactionChestMessage;
import fr.eriniumgroup.erinium_faction.common.util.EFUtils;
import fr.eriniumgroup.erinium_faction.core.EriFont;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
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

    // Base design size
    private static final int BASE_W = 420;
    private static final int BASE_H = 240;

    private double scaleX = 1.0;
    private double scaleY = 1.0;

    public FactionMenuScreen(FactionMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = BASE_W;
        this.imageHeight = BASE_H;
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

    private void recomputeLayout() {
        // Adapter la taille de la GUI à la fenêtre tout en conservant le ratio BASE_W:BASE_H
        int availW = this.width - 20;
        int availH = this.height - 20;
        int targetW = BASE_W;
        int targetH = BASE_H;
        if (availW > 0 && availH > 0) {
            double scaleByW = availW / (double) BASE_W;
            double scaleByH = availH / (double) BASE_H;
            double scale = Math.min(scaleByW, scaleByH);
            scale = Math.max(1.0, Math.min(scale, 2.5)); // éviter trop petit/trop grand
            targetW = (int) Math.round(BASE_W * scale);
            targetH = (int) Math.round(BASE_H * scale);
        }
        this.imageWidth = targetW;
        this.imageHeight = targetH;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.scaleX = this.imageWidth / (double) BASE_W;
        this.scaleY = this.imageHeight / (double) BASE_H;
    }

    private int sx(int base) {
        return this.leftPos + (int) Math.round(base * this.scaleX);
    }

    private int sy(int base) {
        return this.topPos + (int) Math.round(base * this.scaleY);
    }

    private int sw(int base) {
        return (int) Math.round(base * this.scaleX);
    }

    private int sh(int base) {
        return (int) Math.round(base * this.scaleY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        boolean customTooltipShown = false;
        if (fsettings != null && fsettings.isMouseOver(mouseX, mouseY)) {
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

        // Fond étiré au nouveau ratio
        guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_menu_bg.png"), this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0, 0, BASE_W, BASE_H, BASE_W, BASE_H);

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

        // Titres et labels (positions relatives)
        drawText(guiGraphics, displayName, EriFont::orbitronBold, 14f, -1, sy(10) - this.topPos, false, true, EFUtils.Color.ARGBToInt(255, 255, 215, 0));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.resume").getString(), EriFont::orbitron, 10f, -1, sy(45) - this.topPos, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.playerlist").getString(), EriFont::orbitron, 10f, sx(349) - this.leftPos, sy(45) - this.topPos, true, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));

        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.claims").getString() + claimCount + " / " + maxClaims, EriFont::exo2, 8f, sx(149) - this.leftPos, sy(89) - this.topPos, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.membercount").getString() + memberCount + " / " + maxPlayers, EriFont::exo2, 8f, sx(149) - this.leftPos, sy(102) - this.topPos, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.power").getString() + currentPower + " / " + Math.max(0, maxPower), EriFont::exo2, 8f, sx(149) - this.leftPos, sy(115) - this.topPos, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.level").getString() + level, EriFont::exo2, 8f, -1, sy(128) - this.topPos, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));

        if (xpRequired > 0) {
            // barre de XP ajustée
            int barX = sx(149);
            int barY = sy(141);
            int bw = sw(122);
            int bh = sh(10);
            guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_xp_bar.png"), barX, barY, bw, bh, 0, 0, 122, 10, 122, 10);
            int fillWidth = Math.min(bw, Math.max(0, (int) Math.round(bw * xp / (double) xpRequired)));
            if (fillWidth > 0) {
                guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/faction_xp_bar_fill.png"), barX + sw(1), barY + sh(1), fillWidth, Math.max(1, bh - sh(2)), 0, 0, 122, 8, 122, 8);
            }
            drawText(guiGraphics, xp + " / " + xpRequired, EriFont::exo2, 6.5f, -1, sy(154) - this.topPos, false, true, EFUtils.Color.ARGBToInt(255, 255, 215, 0));
        }

        if (!hasFaction && (snap == null || snap.name == null || snap.name.isEmpty())) {
            drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.no_faction_hint").getString(), EriFont::exo2, 8f, -1, sy(160) - this.topPos, false, true, EFUtils.Color.ARGBToInt(255, 200, 200, 200));
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
        recomputeLayout();

        // Construire la liste des joueurs
        FactionSnapshot snap = this.menu instanceof FactionMenu fm ? fm.snapshot : null;
        StringBuilder playerlist = new StringBuilder();
        if (snap != null && snap.name != null && !snap.name.isEmpty()) {
            for (var e : snap.membersRank.entrySet()) {
                UUID id = e.getKey();
                String rank = e.getValue();
                String name = snap.memberNames.getOrDefault(id, id.toString());
                if (playerlist.length() > 0) playerlist.append(",");
                playerlist.append(id).append(":").append(rank).append(":").append(name);
            }
        } else if (hasFaction && faction != null) {
            for (Map.Entry<UUID, Faction.Member> t : faction.getMembers().entrySet()) {
                if (!playerlist.isEmpty()) playerlist.append(",");
                String rankId = t.getValue().rankId;
                String name = t.getValue().nameCached != null ? t.getValue().nameCached : t.getKey().toString();
                playerlist.append(t.getKey()).append(":").append(rankId).append(":").append(name);
            }
        }
        // Liste scrollable à droite, dimension en fonction de la fenêtre
        FactionMenuPlayerList scrollableList = new FactionMenuPlayerList(this.minecraft, sx(BASE_W - 130), sy(54), 120, Math.max(60, this.imageHeight - sh(95)), playerlist.toString(), world != null ? world.getServer() : null);
        this.addRenderableWidget(scrollableList);

        // Bouton settings réintroduit si le joueur a une faction
        if (hasFaction) {
            int btnSize = Math.max(32, Math.min(72, sw(64)));
            int bx = sx(20);
            int by = this.topPos + this.imageHeight - btnSize - sh(20);

            // Bouton Settings
            fsettings = new ImageButton(bx, by, btnSize, btnSize, new WidgetSprites(ResourceLocation.parse("erinium_faction:textures/screens/faction_settings.png"), ResourceLocation.parse("erinium_faction:textures/screens/faction_settings_hover.png")), e -> {
                int px = FactionMenuScreen.this.x;
                int py = FactionMenuScreen.this.y;
                int pz = FactionMenuScreen.this.z;
                PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(1, px, py, pz));
            }) {
                @Override
                public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
                    guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
                }
            };
            this.addRenderableWidget(fsettings);

            // Bouton Coffre (à droite du bouton settings)
            int chestBtnX = bx + btnSize + sw(10);
            ImageButton chestButton = new ImageButton(chestBtnX, by, btnSize, btnSize,
                new WidgetSprites(
                    ResourceLocation.parse("erinium_faction:textures/screens/faction_chest.png"),
                    ResourceLocation.parse("erinium_faction:textures/screens/faction_chest_open.png")
                ),
                e -> {
                    // Envoyer le paquet pour ouvrir le coffre
                    PacketDistributor.sendToServer(new OpenFactionChestMessage());
                }) {
                @Override
                public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
                    guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
                }
            };
            chestButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                net.minecraft.network.chat.Component.literal("§6Coffre de Faction\n§7Cliquez pour ouvrir")));
            this.addRenderableWidget(chestButton);
        } else {
            fsettings = null;
        }
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        recomputeLayout();
        this.init();
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

