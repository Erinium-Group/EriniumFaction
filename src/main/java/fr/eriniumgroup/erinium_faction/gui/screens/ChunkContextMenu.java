package fr.eriniumgroup.erinium_faction.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Menu contextuel pour claim/unclaim un chunk
 */
public class ChunkContextMenu {
    private final int x, y;
    private final int chunkX, chunkZ;
    private final String dimension;
    private final FactionMapScreen parent;
    private boolean visible = true;

    private static final int MENU_WIDTH = 100;
    private static final int MENU_HEIGHT = 40;
    private static final int OPTION_HEIGHT = 20;

    public ChunkContextMenu(int x, int y, int chunkX, int chunkZ, String dimension, FactionMapScreen parent) {
        this.x = x;
        this.y = y;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.dimension = dimension;
        this.parent = parent;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        if (!visible) return;

        var font = Minecraft.getInstance().font;

        // Fond
        g.fill(x, y, x + MENU_WIDTH, y + MENU_HEIGHT, 0xFF2B2B2B);
        g.fill(x + 1, y + 1, x + MENU_WIDTH - 1, y + MENU_HEIGHT - 1, 0xFF1A1A1A);

        // Options
        boolean hoverClaim = mouseX >= x && mouseX < x + MENU_WIDTH && mouseY >= y && mouseY < y + OPTION_HEIGHT;
        boolean hoverUnclaim = mouseX >= x && mouseX < x + MENU_WIDTH && mouseY >= y + OPTION_HEIGHT && mouseY < y + MENU_HEIGHT;

        if (hoverClaim) {
            g.fill(x + 2, y + 2, x + MENU_WIDTH - 2, y + OPTION_HEIGHT - 1, 0x55FFFFFF);
        }
        g.drawString(font, "Claim", x + 10, y + 6, 0xFFFFFF, false);

        if (hoverUnclaim) {
            g.fill(x + 2, y + OPTION_HEIGHT + 1, x + MENU_WIDTH - 2, y + MENU_HEIGHT - 2, 0x55FFFFFF);
        }
        g.drawString(font, "Unclaim", x + 10, y + OPTION_HEIGHT + 6, 0xFFFFFF, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Vérifier si on clique dans le menu
        if (mouseX >= x && mouseX < x + MENU_WIDTH && mouseY >= y && mouseY < y + MENU_HEIGHT) {
            if (mouseY < y + OPTION_HEIGHT) {
                // Claim
                parent.claimChunk(chunkX, chunkZ, dimension);
            } else {
                // Unclaim
                parent.unclaimChunk(chunkX, chunkZ, dimension);
            }
            visible = false;
            return true;
        }

        // Clic en dehors = fermer
        visible = false;
        return false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void close() {
        visible = false;
    }
}
