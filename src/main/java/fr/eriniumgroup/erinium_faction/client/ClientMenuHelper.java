package fr.eriniumgroup.erinium_faction.client;

import fr.eriniumgroup.erinium_faction.init.EFScreens;
import net.minecraft.client.Minecraft;

/**
 * Helper client-only pour mettre à jour l'écran actif localement
 * suite à un changement d'état de menu (sans envoyer de paquet).
 */
public class ClientMenuHelper {
    public static void localScreenUpdate(int elementType, String name, Object elementState, boolean needClientUpdate) {
        if (!needClientUpdate) return;
        var mc = Minecraft.getInstance();
        if (mc == null) return;
        if (mc.screen instanceof EFScreens.ScreenAccessor accessor) {
            accessor.updateMenuState(elementType, name, elementState);
        }
    }
}

