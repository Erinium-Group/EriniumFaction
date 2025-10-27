package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionClientData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Classe abstraite pour les pages du GUI de faction
 * Chaque page SVG a sa propre classe qui hérite de celle-ci
 */
public abstract class FactionPage {
    protected final Font font;

    // Dimensions de base du GUI (400x270 - taille fixe réduite)
    protected static final int BASE_W = 400;
    protected static final int BASE_H = 270;

    // Zone de contenu du panel principal (après sidebar et header)
    // Sidebar: x=9 à 86 (77px)
    // Main panel: x=90 à 387 (297px)
    // Header: y=13 à 39 (26px)
    // Content start: x=99, y=47
    protected static final int CONTENT_X = 99;
    protected static final int CONTENT_Y = 47;
    protected static final int CONTENT_W = 275; // Espace disponible
    protected static final int CONTENT_H = 211; // Espace disponible

    public FactionPage(Font font) {
        this.font = font;
    }

    /**
     * Récupère les données de faction depuis le stockage client
     * @return FactionSnapshot ou null si aucune donnée disponible
     */
    protected FactionSnapshot getFactionData() {
        return FactionClientData.getFactionData();
    }

    /**
     * Traduit une clé de langue en string
     * @param key Clé de traduction
     * @return String traduite
     */
    protected String translate(String key) {
        return Component.translatable(key).getString();
    }

    /**
     * Traduit une clé de langue avec des paramètres
     * @param key Clé de traduction
     * @param args Arguments pour le format
     * @return String traduite
     */
    protected String translate(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    /**
     * Rendu du contenu de la page
     * @param g GuiGraphics
     * @param leftPos Position gauche du GUI complet
     * @param topPos Position haut du GUI complet
     * @param scaleX Scale horizontal
     * @param scaleY Scale vertical
     * @param mouseX Position X de la souris
     * @param mouseY Position Y de la souris
     */
    public abstract void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY);

    /**
     * Gestion des clics de souris sur la page
     * @return true si le clic a été géré
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        return false;
    }

    /**
     * Gestion du relâchement de souris
     * @return true si l'événement a été géré
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        return false;
    }

    /**
     * Gestion du drag de souris
     * @return true si le drag a été géré
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        return false;
    }

    /**
     * Gestion du scroll de souris
     * @return true si le scroll a été géré
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        return false;
    }

    /**
     * Appelé à chaque tick pour animer les composants
     */
    public void tick() {
        // Override si nécessaire
    }

    /**
     * Gestion des touches clavier
     * @return true si la touche a été gérée
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers, int leftPos, int topPos, double scaleX, double scaleY) {
        return false;
    }

    /**
     * Gestion de la saisie de caractères
     * @return true si le caractère a été géré
     */
    public boolean charTyped(char codePoint, int modifiers, int leftPos, int topPos, double scaleX, double scaleY) {
        return false;
    }

    // Helpers pour scaling
    protected int sx(int base, int leftPos, double scaleX) {
        return leftPos + (int) Math.round(base * scaleX);
    }

    protected int sy(int base, int topPos, double scaleY) {
        return topPos + (int) Math.round(base * scaleY);
    }

    protected int sw(int base, double scaleX) {
        return (int) Math.round(base * scaleX);
    }

    protected int sh(int base, double scaleY) {
        return (int) Math.round(base * scaleY);
    }
}
