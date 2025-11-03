package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.common.network.packets.ChunkClaimPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Context menu pour les chunks (claim/unclaim)
 */
public class ChunkContextMenu {
    private final ChunkPos chunkPos;
    private final int x;
    private final int y;
    private final int width = 120;
    private final int height = 60;
    private final MinimapFullscreenScreen parent;

    public ChunkContextMenu(MinimapFullscreenScreen parent, ChunkPos chunkPos, int x, int y) {
        this.parent = parent;
        this.chunkPos = chunkPos;
        this.x = x;
        this.y = y;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();

        // Fond du menu
        graphics.fill(x, y, x + width, y + height, 0xDD1a1a1a);

        // Bordure dorée
        graphics.fill(x - 1, y - 1, x + width + 1, y, 0xFFd4af37);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, 0xFFd4af37);
        graphics.fill(x - 1, y, x, y + height, 0xFFd4af37);
        graphics.fill(x + width, y, x + width + 1, y + height, 0xFFd4af37);

        // Titre
        graphics.drawString(mc.font, "Chunk " + chunkPos.x + ", " + chunkPos.z, x + 5, y + 5, 0xFFffd700, false);

        // Options
        int optionY = y + 20;
        boolean hoverClaim = mouseX >= x + 5 && mouseX <= x + width - 5 && mouseY >= optionY && mouseY < optionY + 10;
        boolean hoverUnclaim = mouseX >= x + 5 && mouseX <= x + width - 5 && mouseY >= optionY + 15 && mouseY < optionY + 25;

        // Option Claim
        int claimColor = hoverClaim ? 0xFF00FF00 : 0xFFFFFFFF;
        graphics.drawString(mc.font, "Claim", x + 10, optionY, claimColor, false);

        // Option Unclaim
        int unclaimColor = hoverUnclaim ? 0xFFFF0000 : 0xFFFFFFFF;
        graphics.drawString(mc.font, "Unclaim", x + 10, optionY + 15, unclaimColor, false);

        // Option Cancel
        int cancelY = optionY + 30;
        boolean hoverCancel = mouseX >= x + 5 && mouseX <= x + width - 5 && mouseY >= cancelY && mouseY < cancelY + 10;
        int cancelColor = hoverCancel ? 0xFFFF8800 : 0xFF888888;
        graphics.drawString(mc.font, "Cancel", x + 10, cancelY, cancelColor, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false; // Seulement left click

        int optionY = y + 20;

        // Check Claim
        if (mouseX >= x + 5 && mouseX <= x + width - 5 && mouseY >= optionY && mouseY < optionY + 10) {
            onClaim();
            return true;
        }

        // Check Unclaim
        if (mouseX >= x + 5 && mouseX <= x + width - 5 && mouseY >= optionY + 15 && mouseY < optionY + 25) {
            onUnclaim();
            return true;
        }

        // Check Cancel
        int cancelY = optionY + 30;
        if (mouseX >= x + 5 && mouseX <= x + width - 5 && mouseY >= cancelY && mouseY < cancelY + 10) {
            parent.closeContextMenu();
            return true;
        }

        // Click outside menu = fermer
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            parent.closeContextMenu();
            return true;
        }

        return false;
    }

    private void onClaim() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String dimension = mc.player.level().dimension().location().toString();
        List<ChunkClaimPacket.ChunkPosition> chunks = List.of(
            new ChunkClaimPacket.ChunkPosition(chunkPos.x, chunkPos.z)
        );

        PacketDistributor.sendToServer(new ChunkClaimPacket(
            ChunkClaimPacket.Action.CLAIM,
            dimension,
            chunks
        ));

        parent.closeContextMenu();
    }

    private void onUnclaim() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String dimension = mc.player.level().dimension().location().toString();
        List<ChunkClaimPacket.ChunkPosition> chunks = List.of(
            new ChunkClaimPacket.ChunkPosition(chunkPos.x, chunkPos.z)
        );

        PacketDistributor.sendToServer(new ChunkClaimPacket(
            ChunkClaimPacket.Action.UNCLAIM,
            dimension,
            chunks
        ));

        parent.closeContextMenu();
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }
}
