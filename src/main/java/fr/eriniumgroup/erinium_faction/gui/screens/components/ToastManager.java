package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire global des toasts notifications
 * Gère l'empilement, les animations et le positionnement automatique
 */
public class ToastManager {
    private static ToastManager instance;

    private final List<ToastNotification> toasts = new ArrayList<>();
    private final Font font;

    private static final int TOAST_SPACING = 10;
    private static final int SCREEN_MARGIN = 20;
    private static final int DEFAULT_DURATION = 5000; // 5 secondes

    private ToastManager() {
        this.font = Minecraft.getInstance().font;
    }

    /**
     * Récupère l'instance singleton du ToastManager
     */
    public static ToastManager getInstance() {
        if (instance == null) {
            instance = new ToastManager();
        }
        return instance;
    }

    /**
     * Affiche un toast de succès
     */
    public static void success(String title, String message) {
        success(Component.literal(title), Component.literal(message));
    }

    public static void success(Component title, Component message) {
        getInstance().showToast(ToastNotification.ToastType.SUCCESS, title, message, DEFAULT_DURATION);
    }

    public static void success(String title, String message, int duration) {
        getInstance().showToast(ToastNotification.ToastType.SUCCESS,
            Component.literal(title), Component.literal(message), duration);
    }

    /**
     * Affiche un toast d'erreur
     */
    public static void error(String title, String message) {
        error(Component.literal(title), Component.literal(message));
    }

    public static void error(Component title, Component message) {
        getInstance().showToast(ToastNotification.ToastType.ERROR, title, message, DEFAULT_DURATION);
    }

    public static void error(String title, String message, int duration) {
        getInstance().showToast(ToastNotification.ToastType.ERROR,
            Component.literal(title), Component.literal(message), duration);
    }

    /**
     * Affiche un toast d'avertissement
     */
    public static void warning(String title, String message) {
        warning(Component.literal(title), Component.literal(message));
    }

    public static void warning(Component title, Component message) {
        getInstance().showToast(ToastNotification.ToastType.WARNING, title, message, DEFAULT_DURATION);
    }

    public static void warning(String title, String message, int duration) {
        getInstance().showToast(ToastNotification.ToastType.WARNING,
            Component.literal(title), Component.literal(message), duration);
    }

    /**
     * Affiche un toast d'information
     */
    public static void info(String title, String message) {
        info(Component.literal(title), Component.literal(message));
    }

    public static void info(Component title, Component message) {
        getInstance().showToast(ToastNotification.ToastType.INFO, title, message, DEFAULT_DURATION);
    }

    public static void info(String title, String message, int duration) {
        getInstance().showToast(ToastNotification.ToastType.INFO,
            Component.literal(title), Component.literal(message), duration);
    }

    /**
     * Crée et affiche un nouveau toast
     */
    private void showToast(ToastNotification.ToastType type, Component title, Component message, int duration) {
        ToastNotification toast = new ToastNotification(font, type, title, message, duration);
        toasts.add(toast);
        updateToastPositions();
    }

    /**
     * Met à jour les positions cibles de tous les toasts (pour l'empilement)
     */
    private void updateToastPositions() {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int currentY = SCREEN_MARGIN;

        for (ToastNotification toast : toasts) {
            int x = screenWidth - toast.getWidth() - SCREEN_MARGIN;
            toast.setTargetPosition(x, currentY);
            currentY += toast.getHeight() + TOAST_SPACING;
        }
    }

    /**
     * Met à jour tous les toasts (animations)
     * À appeler dans la méthode tick() ou containerTick() de votre GUI
     */
    public void tick() {
        // Mettre à jour les animations
        for (ToastNotification toast : toasts) {
            toast.tick();
        }

        // Supprimer les toasts terminés
        toasts.removeIf(ToastNotification::shouldRemove);

        // Mettre à jour les positions si nécessaire
        if (!toasts.isEmpty()) {
            updateToastPositions();
        }
    }

    /**
     * Rendu de tous les toasts
     * À appeler dans la méthode render() de votre GUI (après le rendu principal)
     */
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (ToastNotification toast : toasts) {
            toast.render(graphics, mouseX, mouseY);
        }
    }

    /**
     * Gère les clics de souris sur les toasts
     * À appeler dans la méthode mouseClicked() de votre GUI
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Parcourir en ordre inverse pour gérer le clic sur le toast du dessus
        for (int i = toasts.size() - 1; i >= 0; i--) {
            if (toasts.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ferme tous les toasts actifs
     */
    public void closeAll() {
        for (ToastNotification toast : toasts) {
            toast.startClosing();
        }
    }

    /**
     * Supprime immédiatement tous les toasts
     */
    public void clearAll() {
        toasts.clear();
    }

    /**
     * Retourne le nombre de toasts actifs
     */
    public int getActiveCount() {
        return toasts.size();
    }
}
