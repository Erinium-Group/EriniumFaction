package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.client.EFClient;
import fr.eriniumgroup.erinium_faction.gui.MinimapOverlayConfig;
import fr.eriniumgroup.erinium_faction.gui.screens.MinimapOverlaySettingsScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Handler côté client uniquement pour ouvrir les screens de minimap
 * Cette classe ne sera chargée QUE côté client
 */
@OnlyIn(Dist.CLIENT)
public class ClientMinimapScreenHandler {

    /**
     * Ouvre le screen de configuration de la minimap
     */
    public static void openSettingsScreen() {
        Minecraft.getInstance().setScreen(new MinimapOverlaySettingsScreen());
    }

    /**
     * Active/désactive la minimap
     */
    public static void toggleMinimap(boolean enabled) {
        MinimapOverlayConfig config = EFClient.getMinimapConfig();
        if (config != null) {
            config.enabled = enabled;
            config.save();
        }
    }
}
