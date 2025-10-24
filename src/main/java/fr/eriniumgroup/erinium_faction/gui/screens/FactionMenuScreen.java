package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
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

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
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
    private boolean hasFaction; // nouveau: indique si une faction valide est disponible

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
            this.factionfile = null; // on n'utilise pas les fichiers côté client si snapshot dispo
            this.hasFaction = true;
        } else {
            this.faction = container.faction != null ? container.faction : FactionManager.getPlayerFactionObject(entity.getUUID());
            String factionId = null;
            if (this.faction != null) {
                factionId = this.faction.getName();
            } else if (container.factionName != null && !container.factionName.isEmpty()) {
                factionId = container.factionName;
            } else {
                factionId = FactionManager.getPlayerFaction(entity.getUUID());
            }
            this.factionfile = (factionId != null && !factionId.isEmpty()) ? EFUtils.Faction.FactionFileById(factionId) : null;
            this.hasFaction = (factionId != null && !factionId.isEmpty()) && this.factionfile != null;
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
        // Afficher le tooltip des paramètres uniquement si le bouton existe
        if (fsettings != null && mouseX > leftPos + 74 && mouseX < leftPos + 74 + 64 && mouseY > topPos + 100 && mouseY < topPos + 100 + 64) {
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

        // Données: utiliser snapshot si dispo, sinon fichiers
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
        } else {
            displayName = hasFaction ? EFUtils.F.GetFileStringValue(factionfile, "displayname") : Component.translatable("erinium_faction.faction.menu.no_faction").getString();
            if (displayName == null || displayName.isEmpty()) displayName = "-";
            String claimlist = hasFaction ? EFUtils.F.GetFileStringValue(factionfile, "claimlist") : null;
            claimCount = (claimlist == null || claimlist.isEmpty()) ? 0 : claimlist.split(",").length;
            maxClaims = hasFaction ? (int) EFUtils.F.GetFileNumberValue(factionfile, "maxClaims") : 0;
            String memberList = hasFaction ? EFUtils.F.GetFileStringValue(factionfile, "memberList") : null;
            memberCount = (memberList == null || memberList.isEmpty()) ? (hasFaction ? 1 : 0) : memberList.split(",").length + 1;
            maxPlayers = hasFaction ? (int) EFUtils.F.GetFileNumberValue(factionfile, "maxPlayer") : 0;
            level = hasFaction ? (int) EFUtils.F.GetFileNumberValue(factionfile, "factionLevel") : 0;
            xp = hasFaction ? (int) EFUtils.F.GetFileNumberValue(factionfile, "factionXp") : 0;
            xpRequired = (hasFaction && this.faction != null) ? (int) this.faction.getXPRequiredForNextLevel(level) : 0;
            currentPower = hasFaction ? (int) EFUtils.F.GetFileNumberValue(factionfile, "power") : 0;
            maxPower = (hasFaction && this.faction != null) ? (int) this.faction.getPower() : 0;
        }

        drawText(guiGraphics, displayName, EriFont::orbitronBold, 14f, -1, 10, false, true, EFUtils.Color.ARGBToInt(255, 255, 215, 0));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.resume").getString(), EriFont::orbitron, 10f, -1, 45, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.playerlist").getString(), EriFont::orbitron, 10f, 349, 45, true, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));

        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.claims").getString() + claimCount + " / " + maxClaims, EriFont::exo2, 8f, 149, 89, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.membercount").getString() + memberCount + " / " + maxPlayers, EriFont::exo2, 8f, 149, 102, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
        drawText(guiGraphics, Component.translatable("erinium_faction.faction.menu.power").getString() + currentPower + " / " + (maxPower > 0 ? maxPower : 0), EriFont::exo2, 8f, 149, 115, false, true, EFUtils.Color.ARGBToInt(255, 255, 255, 255));
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

        // Construire la liste des joueurs
        FactionSnapshot snap = this.menu instanceof FactionMenu fm ? fm.snapshot : null;
        if (snap != null && snap.name != null && !snap.name.isEmpty()) {
            StringBuilder playerlist = new StringBuilder();
            for (var e : snap.membersRank.entrySet()) {
                UUID id = e.getKey();
                String rank = e.getValue();
                String name = snap.memberNames.getOrDefault(id, id.toString());
                if (playerlist.length() > 0) playerlist.append(",");
                // Inclure le nom pour éviter lecture disque côté client
                playerlist.append(id).append(":").append(rank).append(":").append(name);
            }
            FactionMenuPlayerList scrollableList = new FactionMenuPlayerList(this.minecraft, this.leftPos + 290, this.topPos + 54, 120, 145, playerlist.toString(), world != null ? world.getServer() : null);
            this.addRenderableWidget(scrollableList);

            // Bouton settings selon rang du joueur (si faction objet dispo côté client)
            Rank rank = (this.faction != null) ? this.faction.getRank(entity.getUUID()) : null;
            if (rank != null && rank.canManageSettings()) {
                fsettings = new ImageButton(this.leftPos + 74, this.topPos + 100, 64, 64, new WidgetSprites(ResourceLocation.parse("erinium_faction:textures/screens/fsettings.png"), ResourceLocation.parse("erinium_faction:textures/screens/fsettings_hover.png")), e -> {
                    int x = FactionMenuScreen.this.x;
                    int y = FactionMenuScreen.this.y;
                    if (true) {
                        // action réseau si nécessaire
                    }
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
            for (Map.Entry<UUID, Rank> t : faction.getMembers().entrySet()) {
                if (playerlist.length() > 0) playerlist.append(",");
                playerlist.append(t.getKey()).append(":").append(t.getValue());
            }
            FactionMenuPlayerList scrollableList = new FactionMenuPlayerList(this.minecraft, this.leftPos + 290, this.topPos + 54, 120, 145, playerlist.toString(), world != null ? world.getServer() : null);
            this.addRenderableWidget(scrollableList);

            Rank rank = faction.getRank(entity.getUUID());
            if (rank != null && rank.canManageSettings()) {
                fsettings = new ImageButton(this.leftPos + 74, this.topPos + 100, 64, 64, new WidgetSprites(ResourceLocation.parse("erinium_faction:textures/screens/fsettings.png"), ResourceLocation.parse("erinium_faction:textures/screens/fsettings_hover.png")), e -> {
                    int x = FactionMenuScreen.this.x;
                    int y = FactionMenuScreen.this.y;
                    if (true) {
                        // action réseau si nécessaire
                    }
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
        if (text == null) text = "";
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

