package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapDataMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ecran simple affichant une carte des claims autour du joueur.
 */
public class FactionMapScreen extends Screen {
    private static ClaimsMapDataMessage lastData;

    public static void onMapData(ClaimsMapDataMessage msg) {
        lastData = msg;
        if (Minecraft.getInstance().screen instanceof FactionMapScreen s) s.refreshFrom(msg);
    }

    private int radius = 12; // par défaut
    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 32;
    private ClaimsMapDataMessage data;

    private Button btnMinus;
    private Button btnRefresh;
    private Button btnPlus;

    private long lastRequestMs = 0L;

    public FactionMapScreen() {
        super(Component.translatable("erinium_faction.faction.map.title"));
    }

    @Override
    protected void init() {
        super.init();
        buildButtons();
        requestData();
    }

    private void buildButtons() {
        // placer les boutons sous la carte, on recalculera approximativement
        int grid = radius * 2 + 1;
        int cell = Math.min(Math.max(8, Math.min(this.width, this.height) / (grid + 6)), 24);
        int mapW = grid * cell;
        int mapH = grid * cell;
        int x0 = (this.width - mapW) / 2;
        int y0 = (this.height - mapH) / 2;
        int yBtns = y0 + mapH + 10;
        int xCenter = x0 + mapW / 2;

        int w = 20, h = 20, gap = 6;
        btnMinus = Button.builder(Component.literal("-"), b -> changeRadius(-1)).bounds(xCenter - (w * 3 + gap * 2) / 2, yBtns, w, h).build();
        btnRefresh = Button.builder(Component.literal("R"), b -> requestData()).bounds(xCenter - (w / 2), yBtns, w, h).build();
        btnPlus = Button.builder(Component.literal("+"), b -> changeRadius(+1)).bounds(xCenter + (w + gap), yBtns, w, h).build();

        this.clearWidgets();
        this.addRenderableWidget(btnMinus);
        this.addRenderableWidget(btnRefresh);
        this.addRenderableWidget(btnPlus);
    }

    private void changeRadius(int delta) {
        int newR = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius + delta));
        if (newR != radius) {
            radius = newR;
            buildButtons();
            requestData();
        }
    }

    private void requestData() {
        Player p = Minecraft.getInstance().player;
        if (p == null) return;
        // simple cooldown 200ms
        long now = System.currentTimeMillis();
        if (now - lastRequestMs < 200) return;
        lastRequestMs = now;

        String dim = p.level().dimension().location().toString();
        int cx = p.chunkPosition().x;
        int cz = p.chunkPosition().z;
        PacketDistributor.sendToServer(new ClaimsMapRequestMessage(dim, cx, cz, radius));
    }

    private void refreshFrom(ClaimsMapDataMessage msg) {
        this.data = msg;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(g, mouseX, mouseY, partialTicks);
        super.render(g, mouseX, mouseY, partialTicks);
        RenderSystem.enableBlend();

        // rafraîchissement auto si le joueur a bougé de chunk
        Player p = Minecraft.getInstance().player;
        if (p != null && (data == null || p.chunkPosition().x != data.centerCx() || p.chunkPosition().z != data.centerCz())) {
            requestData();
        }

        int grid = radius * 2 + 1;
        int cell = Math.min(Math.max(8, Math.min(this.width, this.height) / (grid + 6)), 24);
        int pad = 4;
        int mapW = grid * cell;
        int mapH = grid * cell;
        int x0 = (this.width - mapW) / 2;
        int y0 = (this.height - mapH) / 2;

        // fond
        g.fill(x0 - pad, y0 - pad, x0 + mapW + pad, y0 + mapH + pad, 0xAA000000);

        // dessiner la grille vide
        int cx0 = (data != null ? data.centerCx() : (p != null ? p.chunkPosition().x : 0));
        int cz0 = (data != null ? data.centerCz() : (p != null ? p.chunkPosition().z : 0));

        // claims
        if (data != null && data.relCx().length == data.relCz().length) {
            for (int i = 0; i < data.relCx().length; i++) {
                int rx = data.relCx()[i];
                int rz = data.relCz()[i];
                int gx = rx + radius; // 0..grid-1
                int gz = rz + radius;
                if (gx < 0 || gz < 0 || gx >= grid || gz >= grid) continue;
                int x = x0 + gx * cell;
                int y = y0 + gz * cell;
                int color = colorForOwner(data.owners()[i]);
                g.fill(x + 1, y + 1, x + cell - 1, y + cell - 1, 0xFF000000 | color);
            }
        }

        // grille + position joueur au centre
        for (int r = 0; r < grid; r++) {
            for (int c = 0; c < grid; c++) {
                int x = x0 + c * cell;
                int y = y0 + r * cell;
                int border = 0x66FFFFFF;
                g.fill(x, y, x + cell, y + 1, border);
                g.fill(x, y, x + 1, y + cell, border);
                g.fill(x + cell - 1, y, x + cell, y + cell, border);
                g.fill(x, y + cell - 1, x + cell, y + cell, border);
            }
        }

        // marqueur centre
        int cc = radius;
        int px = x0 + cc * cell;
        int py = y0 + cc * cell;
        g.fill(px + 2, py + 2, px + cell - 2, py + cell - 2, 0xAAFFFFFF);

        // légende
        g.drawString(this.font, this.title, x0, y0 - 18, 0xFFFFFF, false);
        g.drawString(this.font, Component.literal("Center: [" + cx0 + ", " + cz0 + "] r=" + radius + "  [-] R [+]"), x0, y0 + mapH + pad + 2, 0xCCCCCC, false);

        // Affichage nom faction sous le curseur si sur une case claimée
        if (data != null && mouseX >= x0 && mouseX < x0 + mapW && mouseY >= y0 && mouseY < y0 + mapH) {
            int col = (mouseX - x0) / cell - radius; // relatif
            int row = (mouseY - y0) / cell - radius;
            int absCx = data.centerCx() + col;
            int absCz = data.centerCz() + row;
            // rechercher dans le tableau
            for (int i = 0; i < data.relCx().length; i++) {
                if (data.relCx()[i] == col && data.relCz()[i] == row) {
                    String ownerName = data.owners()[i];
                    List<Component> lines = new ArrayList<>();
                    if (ownerName != null && !ownerName.isBlank()) {
                        lines.add(Component.literal(ownerName).withStyle(ChatFormatting.GOLD));
                    } else {
                        lines.add(Component.translatable("erinium_faction.wilderness.desc").withStyle(ChatFormatting.GRAY));
                    }
                    lines.add(Component.literal("Chunk: (" + absCx + ", " + absCz + ")").withStyle(ChatFormatting.DARK_GRAY));
                    g.renderComponentTooltip(this.font, lines, mouseX, mouseY);
                    break;
                }
            }
        }

        RenderSystem.disableBlend();
    }

    private static final Map<String, Integer> ownerColorCache = new HashMap<>();
    private static int colorForOwner(String owner) {
        if (owner == null || owner.isBlank()) return 0x555555; // wilderness
        return ownerColorCache.computeIfAbsent(owner.toLowerCase(Locale.ROOT), k -> {
            int h = k.hashCode();
            int r = 64 + (Math.abs(h) % 192);
            int g = 64 + (Math.abs(h >> 8) % 192);
            int b = 64 + (Math.abs(h >> 16) % 192);
            return (r << 16) | (g << 8) | b;
        });
    }
}
