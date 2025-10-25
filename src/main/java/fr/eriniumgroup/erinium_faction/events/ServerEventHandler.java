package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.Rank;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = EFC.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onChat(ServerChatEvent e){
        e.setCanceled(true);

        EFRManager.Rank r = EFRManager.get().getPlayerRank(e.getPlayer().getUUID());
        Faction f = FactionManager.getFactionOf(e.getPlayer().getUUID());

        String formattedText = (r != null ? r.displayName : "§7Membre")
                + " " + (f != null ? f.getName() : "§aWilderness")
                + " §r| " + e.getPlayer().getDisplayName().getString()
                + "§r: " + e.getMessage().getString();

        // Convertir & en § seulement pour les codes valides
        String coloredText = translateColorCodes(formattedText);

        e.getPlayer().getServer().getPlayerList().broadcastSystemMessage(Component.literal(coloredText), false);
    }

    private static String translateColorCodes(String text) {
        // Codes de couleur valides : 0-9, a-f, k-o, r
        // 0-9, a-f = couleurs
        // k = obfusqué, l = gras, m = barré, n = souligné, o = italique, r = reset
        return text.replaceAll("&([0-9a-fk-or])", "§$1");
    }
}
