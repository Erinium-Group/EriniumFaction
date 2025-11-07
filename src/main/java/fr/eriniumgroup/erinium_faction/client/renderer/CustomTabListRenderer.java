package fr.eriniumgroup.erinium_faction.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.eriniumgroup.erinium_faction.common.config.EFClientConfig;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renderer custom pour la tab list avec un style cyber astral
 */
public class CustomTabListRenderer {

    private static final ResourceLocation PING_ICONS = ResourceLocation.withDefaultNamespace("textures/gui/icons.png");
    private static final int PLAYER_HEAD_SIZE = 8;
    private static final int ENTRY_HEIGHT = 12;
    private static final int COLUMN_WIDTH = 150;
    private static final int COLUMN_SPACING = 5;
    private static final int HEADER_HEIGHT = 40;
    private static final int PADDING = 4;

    private final Minecraft minecraft;
    private final Font font;

    public CustomTabListRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
    }

    /**
     * Rendu principal de la tab list
     */
    public void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight, Scoreboard scoreboard, Objective objective) {
        if (!EFClientConfig.TAB_LIST_ENABLED.get()) {
            return; // Tab list custom désactivée
        }

        List<PlayerInfo> players = getPlayers();
        if (players.isEmpty()) {
            return;
        }

        int maxColumns = EFClientConfig.TAB_LIST_MAX_COLUMNS.get();
        int rowsPerColumn = EFClientConfig.TAB_LIST_ROWS_PER_COLUMN.get();

        // Calculer le nombre de colonnes nécessaires
        int columns = Math.min(maxColumns, (int) Math.ceil((double) players.size() / rowsPerColumn));
        columns = Math.max(1, columns);

        int totalWidth = columns * COLUMN_WIDTH + (columns - 1) * COLUMN_SPACING + PADDING * 2;
        int totalHeight = HEADER_HEIGHT + rowsPerColumn * ENTRY_HEIGHT + PADDING * 2;

        // Centrer la tab list
        int x = (screenWidth - totalWidth) / 2;
        int y = 10;

        // Render background
        renderBackground(guiGraphics, x, y, totalWidth, totalHeight);

        // Render header
        renderHeader(guiGraphics, x, y, totalWidth, players.size());

        // Render players
        renderPlayers(guiGraphics, x, y + HEADER_HEIGHT, columns, rowsPerColumn, players);
    }

    /**
     * Récupère la liste des joueurs triés
     */
    private List<PlayerInfo> getPlayers() {
        List<PlayerInfo> players = new ArrayList<>(minecraft.player.connection.getOnlinePlayers());
        players.sort(Comparator.comparing((PlayerInfo p) -> p.getProfile().getName().toLowerCase()));
        return players;
    }

    /**
     * Rendu du fond avec bordures stylées
     */
    private void renderBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int backgroundColor = EFClientConfig.TAB_LIST_BACKGROUND_COLOR.get();
        int borderColor = EFClientConfig.TAB_LIST_BORDER_COLOR.get();
        int accentColor = EFClientConfig.TAB_LIST_ACCENT_COLOR.get();

        // Fond principal
        guiGraphics.fill(x, y, x + width, y + height, backgroundColor);

        // Bordure principale (2px)
        guiGraphics.fill(x - 2, y - 2, x + width + 2, y, borderColor); // Top
        guiGraphics.fill(x - 2, y + height, x + width + 2, y + height + 2, borderColor); // Bottom
        guiGraphics.fill(x - 2, y, x, y + height, borderColor); // Left
        guiGraphics.fill(x + width, y, x + width + 2, y + height, borderColor); // Right

        // Bordure d'accent (1px intérieure, gradient)
        int accentLight = lightenColor(accentColor, 0.3f);
        guiGraphics.fill(x, y, x + width, y + 1, accentLight); // Top accent
        guiGraphics.fill(x, y + height - 1, x + width, y + height, accentColor); // Bottom accent
    }

    /**
     * Rendu de l'en-tête avec les informations du serveur
     */
    private void renderHeader(GuiGraphics guiGraphics, int x, int y, int width, int playerCount) {
        int headerBg = EFClientConfig.TAB_LIST_HEADER_BACKGROUND_COLOR.get();
        int textColor = EFClientConfig.TAB_LIST_TEXT_COLOR.get();
        int accentColor = EFClientConfig.TAB_LIST_ACCENT_COLOR.get();

        // Fond de l'en-tête
        guiGraphics.fill(x, y, x + width, y + HEADER_HEIGHT, headerBg);

        // Ligne séparatrice
        guiGraphics.fill(x, y + HEADER_HEIGHT - 1, x + width, y + HEADER_HEIGHT, accentColor);

        int centerX = x + width / 2;
        int currentY = y + 4;

        // Nom du serveur
        if (EFClientConfig.TAB_LIST_SHOW_SERVER_NAME.get()) {
            String serverName = EFClientConfig.TAB_LIST_SERVER_NAME.get();
            Component serverComp = Component.literal(serverName);
            int serverWidth = font.width(serverComp);
            guiGraphics.drawString(font, serverComp, centerX - serverWidth / 2, currentY, textColor, true);
            currentY += 10;
        }

        // Ligne d'informations (joueurs, TPS, ping)
        StringBuilder info = new StringBuilder();

        if (EFClientConfig.TAB_LIST_SHOW_PLAYER_COUNT.get()) {
            info.append("§7Joueurs: §f").append(playerCount);
        }

        if (EFClientConfig.TAB_LIST_SHOW_TPS.get()) {
            if (info.length() > 0) info.append(" §8| ");
            double tps = getTPS();
            String tpsColor = tps >= 19.0 ? "§a" : tps >= 15.0 ? "§e" : "§c";
            info.append("§7TPS: ").append(tpsColor).append(String.format("%.1f", tps));
        }

        if (EFClientConfig.TAB_LIST_SHOW_PING.get() && minecraft.player != null) {
            if (info.length() > 0) info.append(" §8| ");
            int ping = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID()).getLatency();
            String pingColor = ping < 100 ? "§a" : ping < 200 ? "§e" : "§c";
            info.append("§7Ping: ").append(pingColor).append(ping).append("ms");
        }

        if (info.length() > 0) {
            Component infoComp = Component.literal(info.toString());
            int infoWidth = font.width(infoComp);
            guiGraphics.drawString(font, infoComp, centerX - infoWidth / 2, currentY, textColor, true);
        }
    }

    /**
     * Rendu des joueurs en colonnes
     */
    private void renderPlayers(GuiGraphics guiGraphics, int x, int y, int columns, int rowsPerColumn, List<PlayerInfo> players) {
        int playerBg = EFClientConfig.TAB_LIST_PLAYER_BACKGROUND_COLOR.get();
        int textColor = EFClientConfig.TAB_LIST_TEXT_COLOR.get();

        for (int col = 0; col < columns; col++) {
            int columnX = x + PADDING + col * (COLUMN_WIDTH + COLUMN_SPACING);

            for (int row = 0; row < rowsPerColumn; row++) {
                int index = col * rowsPerColumn + row;
                if (index >= players.size()) break;

                PlayerInfo playerInfo = players.get(index);
                int entryY = y + PADDING + row * ENTRY_HEIGHT;

                // Fond de l'entrée
                guiGraphics.fill(columnX, entryY, columnX + COLUMN_WIDTH, entryY + ENTRY_HEIGHT - 1, playerBg);

                // Rendu de l'icône de ping
                renderPingIcon(guiGraphics, columnX + 2, entryY + 2, playerInfo.getLatency());

                // Rendu de la tête du joueur
                renderPlayerHead(guiGraphics, columnX + 12, entryY + 2, playerInfo);

                // Rendu du rang
                int textX = columnX + 22;
                String rankPrefix = getRankPrefix(playerInfo);
                if (!rankPrefix.isEmpty()) {
                    guiGraphics.drawString(font, rankPrefix, textX, entryY + 2, textColor, false);
                    textX += font.width(rankPrefix) + 2;
                }

                // Rendu du nom du joueur (tronqué si nécessaire)
                String playerName = playerInfo.getProfile().getName();
                playerName = truncateName(playerName);
                guiGraphics.drawString(font, playerName, textX, entryY + 2, textColor, false);
            }
        }
    }

    /**
     * Rendu de l'icône de ping
     */
    private void renderPingIcon(GuiGraphics guiGraphics, int x, int y, int ping) {
        RenderSystem.setShaderTexture(0, PING_ICONS);

        int u = 0;
        int v = ping < 0 ? 5 : ping < 150 ? 0 : ping < 300 ? 1 : ping < 600 ? 2 : ping < 1000 ? 3 : 4;

        guiGraphics.blit(PING_ICONS, x, y, 0, 176 + v * 8, 10, 8);
    }

    /**
     * Rendu de la tête du joueur
     */
    private void renderPlayerHead(GuiGraphics guiGraphics, int x, int y, PlayerInfo playerInfo) {
        ResourceLocation skin = playerInfo.getSkin().texture();
        RenderSystem.setShaderTexture(0, skin);

        // Rendu de la face du joueur (8x8 de la skin 64x64)
        guiGraphics.blit(skin, x, y, PLAYER_HEAD_SIZE, PLAYER_HEAD_SIZE, 8.0F, 8, 8, 8, 64, 64);
        // Overlay (casque)
        guiGraphics.blit(skin, x, y, PLAYER_HEAD_SIZE, PLAYER_HEAD_SIZE, 40.0F, 8, 8, 8, 64, 64);
    }

    /**
     * Récupère le préfixe de rang du joueur
     */
    private String getRankPrefix(PlayerInfo playerInfo) {
        try {
            var rank = EFRManager.get().getPlayerRank(playerInfo.getProfile().getId());
            if (rank != null && rank.prefix != null && !rank.prefix.isEmpty()) {
                return rank.prefix + " ";
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    /**
     * Tronque un nom s'il est trop long
     */
    private String truncateName(String name) {
        int maxLength = EFClientConfig.TAB_LIST_PLAYER_NAME_MAX_LENGTH.get();
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 2) + "..";
    }

    /**
     * Récupère le TPS du serveur (estimation)
     */
    private double getTPS() {
        // TODO: Implémenter un vrai système de tracking TPS
        // Pour l'instant, valeur par défaut
        return 20.0;
    }

    /**
     * Éclaircit une couleur
     */
    private int lightenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.min(255, (int) (r + (255 - r) * factor));
        g = Math.min(255, (int) (g + (255 - g) * factor));
        b = Math.min(255, (int) (b + (255 - b) * factor));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
