package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu contextuel (comme le clic droit Windows)
 * Affiche une liste d'options cliquables
 */
public class ContextMenu {
    private final Font font;
    private final List<MenuItem> items = new ArrayList<>();
    private int x, y;
    private int width, height;
    private boolean visible = false;

    private static final int ITEM_HEIGHT = 18;
    private static final int PADDING = 4;
    private static final int MIN_WIDTH = 100;

    public ContextMenu(Font font) {
        this.font = font;
    }

    /**
     * Classe représentant un item du menu
     */
    public static class MenuItem {
        private final String label;
        private final Runnable action;
        private boolean enabled;

        public MenuItem(String label, Runnable action, boolean enabled) {
            this.label = label;
            this.action = action;
            this.enabled = enabled;
        }

        public String getLabel() { return label; }
        public Runnable getAction() { return action; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * Ajoute un item au menu
     */
    public void addItem(String label, Runnable action, boolean enabled) {
        items.add(new MenuItem(label, action, enabled));
    }

    /**
     * Efface tous les items
     */
    public void clearItems() {
        items.clear();
    }

    /**
     * Ouvre le menu à la position spécifiée
     */
    public void open(int x, int y) {
        this.x = x;
        this.y = y;
        this.visible = true;

        // Calculer la taille du menu
        width = MIN_WIDTH;
        for (MenuItem item : items) {
            int textWidth = font.width(item.getLabel()) + PADDING * 2;
            width = Math.max(width, textWidth);
        }
        height = items.size() * ITEM_HEIGHT + PADDING * 2;
    }

    /**
     * Ferme le menu
     */
    public void close() {
        this.visible = false;
    }

    /**
     * Vérifie si le menu est visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Rendu du menu contextuel
     */
    public void render(GuiGraphics g, int mouseX, int mouseY) {
        if (!visible) return;

        // Fond du menu
        g.fill(x, y, x + width, y + height, 0xEE1e1e2e);
        g.fill(x, y, x + width, y + 1, 0xFF00d2ff); // Bordure haute
        g.fill(x, y + height - 1, x + width, y + height, 0xFF00d2ff); // Bordure basse
        g.fill(x, y, x + 1, y + height, 0xFF00d2ff); // Bordure gauche
        g.fill(x + width - 1, y, x + width, y + height, 0xFF00d2ff); // Bordure droite

        // Rendu des items
        int itemY = y + PADDING;
        for (MenuItem item : items) {
            boolean hovered = mouseX >= x && mouseX < x + width &&
                            mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT;

            // Fond de l'item si hover et activé
            if (hovered && item.isEnabled()) {
                g.fill(x + 2, itemY, x + width - 2, itemY + ITEM_HEIGHT, 0x8800d2ff);
            }

            // Texte de l'item
            int textColor = item.isEnabled() ? 0xFFffffff : 0xFF6a6a7e;
            g.drawString(font, item.getLabel(), x + PADDING, itemY + ITEM_HEIGHT / 2 - font.lineHeight / 2, textColor, false);

            itemY += ITEM_HEIGHT;
        }
    }

    /**
     * Gestion des clics
     * @return true si le clic a été géré
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (button == 0) {
            // Vérifier si on clique sur un item
            int itemY = y + PADDING;
            for (MenuItem item : items) {
                if (mouseX >= x && mouseX < x + width &&
                    mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT) {
                    if (item.isEnabled() && item.getAction() != null) {
                        item.getAction().run();
                        close();
                        return true;
                    }
                }
                itemY += ITEM_HEIGHT;
            }

            // Si on clique en dehors du menu, on le ferme
            if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
                close();
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Vérifie si un point est dans les limites du menu
     */
    public boolean isPointInMenu(double mouseX, double mouseY) {
        if (!visible) return false;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
