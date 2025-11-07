package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import fr.eriniumgroup.erinium_faction.integration.discord.ChatReportManager;
import fr.eriniumgroup.erinium_faction.integration.discord.DiscordWebhookManager;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Objects;

@EventBusSubscriber(modid = EFC.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onChat(ServerChatEvent e) {
        e.setCanceled(true);

        ServerPlayer player = e.getPlayer();
        EFRManager.Rank r = EFRManager.get().getPlayerRank(player.getUUID());
        Faction f = FactionManager.getFactionOf(player.getUUID());

        String rank = r != null ? r.displayName : "§7Membre";
        String factionName = f != null ? f.getName() : "§aWilderness";
        String message = e.getMessage().getString();

        // Ajouter le message au cache
        fr.eriniumgroup.erinium_faction.integration.discord.ChatMessageCache.addMessage(player.getUUID(), message);

        // Créer le message formaté avec le bouton de report
        MutableComponent reportButton = Component.literal("⚠")
                .withStyle(style -> style
                        .withColor(net.minecraft.ChatFormatting.RED)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/reportchat " + player.getName().getString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal("§cCliquez pour reporter ce message")))
                );

        Component formattedText = Component.translatable("erinium_faction.chat.global_format",
                rank, factionName, player.getDisplayName(), message);

        String coloredText = translateColorCodes(formattedText);

        // Créer le message complet avec le bouton de report au début
        MutableComponent fullMessage = Component.literal("")
                .append(reportButton)
                .append(Component.literal(" "))
                .append(Component.literal(coloredText));

        // Broadcast le message à tous les joueurs
        Objects.requireNonNull(player.getServer()).getPlayerList()
                .broadcastSystemMessage(fullMessage, false);

        // Envoyer le message vers Discord (sans le bouton de report)
        DiscordWebhookManager.sendMinecraftChatToDiscord(
                player.getName().getString(),
                message,
                rank,
                factionName
        );
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String message = "**" + player.getName().getString() + "** a rejoint le serveur";
            DiscordWebhookManager.sendServerEvent("JOIN", message);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String message = "**" + player.getName().getString() + "** a quitté le serveur";
            DiscordWebhookManager.sendServerEvent("LEAVE", message);

            // Nettoyer le cache de messages du joueur
            fr.eriniumgroup.erinium_faction.integration.discord.ChatMessageCache.clearMessages(player.getUUID());
        }
    }

    private static String translateColorCodes(Component text) {
        return text.getString().replaceAll("&([0-9a-fk-or])", "§$1");
    }
}
