package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.Objects;

@EventBusSubscriber(modid = EFC.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onChat(ServerChatEvent e) {
        e.setCanceled(true);

        EFRManager.Rank r = EFRManager.get().getPlayerRank(e.getPlayer().getUUID());
        Faction f = FactionManager.getFactionOf(e.getPlayer().getUUID());

        Component formattedText = Component.translatable("erinium_faction.chat.global_format", r != null ? r.displayName : "§7Membre", f != null ? f.getName() : "§aWilderness", e.getPlayer().getDisplayName(), e.getMessage());

        String coloredText = translateColorCodes(formattedText);

        Objects.requireNonNull(e.getPlayer().getServer()).getPlayerList().broadcastSystemMessage(Component.literal(coloredText), false);
    }

    private static String translateColorCodes(Component text) {
        return text.getString().replaceAll("&([0-9a-fk-or])", "§$1");
    }
}
