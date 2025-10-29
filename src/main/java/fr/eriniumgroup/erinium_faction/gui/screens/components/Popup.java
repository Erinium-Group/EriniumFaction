package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Classe de base pour les popups modaux
 */
public abstract class Popup {
    protected final Font font;
    protected int x, y, width, height;
    protected boolean visible = false;
    protected Runnable onClose;

    // Textures pour le popup (utiliser les textures existantes)
    private static final ResourceLocation POPUP_BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/main-background.png");
    private static final ResourceLocation POPUP_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/page-header.png");

    public Popup(Font font, int width, int height) {
        this.font = font;
        this.width = width;
        this.height = height;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void open(int screenWidth, int screenHeight) {
        this.visible = true;
        // Centrer le popup
        this.x = (screenWidth - width) / 2;
        this.y = (screenHeight - height) / 2;
        onOpen();
    }

    public void close() {
        this.visible = false;
        onClose();
        if (onClose != null) {
            onClose.run();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * Appelé quand le popup s'ouvre
     */
    protected abstract void onOpen();

    /**
     * Appelé quand le popup se ferme
     */
    protected abstract void onClose();

    /**
     * Rendu du contenu du popup (sans le fond/header)
     */
    protected abstract void renderContent(GuiGraphics g, int mouseX, int mouseY);

    /**
     * Retourne le titre du popup
     */
    protected abstract String getTitle();

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        if (!visible) return;

        // Overlay semi-transparent
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), 0x80000000);

        // Fond du popup (fallback avec rectangles si textures manquantes)
        try {
            ImageRenderer.renderScaledImage(g, POPUP_BACKGROUND, x, y, width, height);
        } catch (Exception e) {
            // Fallback : fond sombre avec bordure
            g.fill(x, y, x + width, y + height, 0xE61e1e2e);
            g.fill(x, y, x + width, y + 1, 0xFF00d2ff); // Bordure haut
            g.fill(x, y, x + 1, y + height, 0xFF00d2ff); // Bordure gauche
            g.fill(x + width - 1, y, x + width, y + height, 0xFF00d2ff); // Bordure droite
            g.fill(x, y + height - 1, x + width, y + height, 0xFF00d2ff); // Bordure bas
        }

        // Header
        int headerHeight = 24;
        try {
            ImageRenderer.renderScaledImage(g, POPUP_HEADER, x, y, width, headerHeight);
        } catch (Exception e) {
            // Fallback : header avec fond différent
            g.fill(x, y, x + width, y + headerHeight, 0xFF2a2a3e);
            g.fill(x, y + headerHeight, x + width, y + headerHeight + 1, 0xFF00d2ff);
        }
        g.drawString(font, getTitle(), x + 8, y + 8, 0xFFffffff, true);

        // Bouton de fermeture (X)
        int closeX = x + width - 20;
        int closeY = y + 6;
        boolean closeHovered = mouseX >= closeX && mouseX < closeX + 12 &&
                              mouseY >= closeY && mouseY < closeY + 12;
        g.drawString(font, "X", closeX, closeY, closeHovered ? 0xFFff0000 : 0xFFffffff, false);

        // Contenu
        renderContent(g, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Vérifier le clic sur le bouton X
        int closeX = x + width - 20;
        int closeY = y + 6;
        if (mouseX >= closeX && mouseX < closeX + 12 &&
            mouseY >= closeY && mouseY < closeY + 12) {
            close();
            return true;
        }

        // Clic en dehors du popup = fermer
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
            close();
            return true;
        }

        return handleMouseClick(mouseX, mouseY, button);
    }

    /**
     * Gère les clics à l'intérieur du popup
     */
    protected abstract boolean handleMouseClick(double mouseX, double mouseY, int button);

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        return handleMouseRelease(mouseX, mouseY, button);
    }

    protected boolean handleMouseRelease(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!visible) return false;
        return handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
    }

    protected boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible) return false;
        return handleMouseScroll(mouseX, mouseY, scrollX, scrollY);
    }

    protected boolean handleMouseScroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;
        // ESC pour fermer
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            close();
            return true;
        }
        return handleKeyPress(keyCode, scanCode, modifiers);
    }

    protected boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!visible) return false;
        return handleCharTyped(codePoint, modifiers);
    }

    protected boolean handleCharTyped(char codePoint, int modifiers) {
        return false;
    }
}
