package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

/**
 * Page Chest - Basée sur chest.svg
 * Coffre de faction avec inventaire joueur
 * Utilise de vrais slots Minecraft fonctionnels
 */
public class ChestPage extends FactionPage {

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 18;

    // Textures pour les slots d'inventaire
    private static final ResourceLocation INVENTORY_SLOT_EMPTY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/chest/inventory-slot-empty.png");
    private static final ResourceLocation INVENTORY_SLOT_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/chest/inventory-slot-hover.png");

    private final FactionMenu menu;

    public ChestPage(Font font, FactionMenu menu) {
        super(font);
        this.menu = menu;
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Header
        g.fill(x, y, x + w, y + sh(40, scaleY), 0xE61e1e2e);
        g.fill(x, y, x + w, y + 1, 0xFF00d2ff);
        g.drawString(font, translate("erinium_faction.gui.chest.title"), x + sw(16, scaleX), y + sh(16, scaleY), 0xFFffffff, true);

        // Info text
        String info = translate("erinium_faction.gui.chest.info", FactionMenu.FACTION_CHEST_ROWS, FactionMenu.FACTION_CHEST_SLOTS);
        g.drawString(font, info, x + w - font.width(info) - sw(16, scaleX), y + sh(16, scaleY), 0xFF00d2ff, false);

        // Calculate positions for labels (scaled) - utiliser doubles pour éviter erreurs d'arrondi
        double scaledBaseX = leftPos + 156 * scaleX;
        double scaledChestY = topPos + 74 * scaleY;
        double scaledInvY = topPos + 148 * scaleY;
        double scaledHotbarY = topPos + 210 * scaleY;
        double scaledSpacing = 18 * scaleX;

        // Label for player inventory
        g.drawString(font, translate("erinium_faction.gui.chest.player_inventory"), (int) Math.round(scaledBaseX), (int) Math.round(scaledInvY) - sh(9, scaleY), 0xFFa0a0c0, false);

        // Dessiner les emplacements de slots avec les textures
        // Les slots Minecraft font toujours 16x16 en rendu, donc on dessine des images de 16x16 centrés
        int slotRenderSize = 16;
        int centerOffset = (int) Math.round((scaledSpacing - slotRenderSize) / 2);

        // Faction chest slots (utiliser FACTION_CHEST_ROWS au lieu de hardcoder 3)
        for (int row = 0; row < FactionMenu.FACTION_CHEST_ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = (int) Math.round(scaledBaseX + col * scaledSpacing) + centerOffset;
                int slotY = (int) Math.round(scaledChestY + row * scaledSpacing) + centerOffset;

                // Vérifier si la souris est sur ce slot
                boolean isHovered = mouseX >= slotX && mouseX < slotX + slotRenderSize &&
                                   mouseY >= slotY && mouseY < slotY + slotRenderSize;

                // Dessiner le slot avec la texture appropriée
                ResourceLocation slotTexture = isHovered ? INVENTORY_SLOT_HOVER : INVENTORY_SLOT_EMPTY;
                ImageRenderer.renderScaledImage(g, slotTexture, slotX, slotY, slotRenderSize, slotRenderSize);
            }
        }

        // Player inventory slots (3 rangées de 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = (int) Math.round(scaledBaseX + col * scaledSpacing) + centerOffset;
                int slotY = (int) Math.round(scaledInvY + row * scaledSpacing) + centerOffset;

                boolean isHovered = mouseX >= slotX && mouseX < slotX + slotRenderSize &&
                                   mouseY >= slotY && mouseY < slotY + slotRenderSize;

                ResourceLocation slotTexture = isHovered ? INVENTORY_SLOT_HOVER : INVENTORY_SLOT_EMPTY;
                ImageRenderer.renderScaledImage(g, slotTexture, slotX, slotY, slotRenderSize, slotRenderSize);
            }
        }

        // Hotbar slots (1 rangée de 9)
        for (int col = 0; col < 9; col++) {
            int slotX = (int) Math.round(scaledBaseX + col * scaledSpacing) + centerOffset;
            int slotY = (int) Math.round(scaledHotbarY) + centerOffset;

            boolean isHovered = mouseX >= slotX && mouseX < slotX + slotRenderSize &&
                               mouseY >= slotY && mouseY < slotY + slotRenderSize;

            ResourceLocation slotTexture = isHovered ? INVENTORY_SLOT_HOVER : INVENTORY_SLOT_EMPTY;
            ImageRenderer.renderScaledImage(g, slotTexture, slotX, slotY, slotRenderSize, slotRenderSize);
        }

        // Note: Les slots sont automatiquement rendus par AbstractContainerScreen
        // grâce à renderSlot() appelé dans renderBg()
    }
}
