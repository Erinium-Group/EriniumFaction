package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.eriniumgroup.erinium_faction.features.kits.Kit;
import fr.eriniumgroup.erinium_faction.features.kits.KitManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Commande /kit pour gérer les kits
 */
public class KitCommand {

    private static final SuggestionProvider<CommandSourceStack> KIT_SUGGESTIONS = (context, builder) -> {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            List<Kit> availableKits = KitManager.getInstance().getAvailableKits(player);
            return SharedSuggestionProvider.suggest(
                    availableKits.stream().map(Kit::getId),
                    builder
            );
        }
        return SharedSuggestionProvider.suggest(
                KitManager.getInstance().getAllKits().stream().map(Kit::getId),
                builder
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kit")
                .executes(KitCommand::listKits)
                .then(Commands.argument("kitName", StringArgumentType.word())
                        .suggests(KIT_SUGGESTIONS)
                        .executes(KitCommand::giveKit)
                )
        );
    }

    /**
     * Liste tous les kits disponibles pour le joueur
     */
    private static int listKits(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Cette commande ne peut être utilisée que par un joueur"));
            return 0;
        }

        List<Kit> availableKits = KitManager.getInstance().getAvailableKits(player);

        if (availableKits.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cAucun kit disponible pour vous."));
            return 0;
        }

        // En-tête
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§8§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendSystemMessage(Component.literal("§6§l         KITS DISPONIBLES"));
        player.sendSystemMessage(Component.literal("§8§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendSystemMessage(Component.literal(""));

        // Lister chaque kit
        for (Kit kit : availableKits) {
            // Vérifier le cooldown
            long cooldown = KitManager.getInstance().getCooldownRemaining(player.getUUID(), kit.getId());
            boolean canUse = cooldown == 0;

            Component kitComponent;

            if (canUse) {
                // Kit disponible (cliquable)
                kitComponent = Component.literal("§a✔ §f" + kit.getDisplayName())
                        .withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kit " + kit.getId()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("§7" + kit.getDescription() + "\n§e§nCliquez pour obtenir ce kit")))
                        );
            } else {
                // Kit en cooldown
                String timeRemaining = formatTime(cooldown);
                kitComponent = Component.literal("§c✖ §7" + kit.getDisplayName() + " §8(Cooldown: " + timeRemaining + ")")
                        .withStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("§7" + kit.getDescription() + "\n§cDisponible dans: §e" + timeRemaining)))
                        );
            }

            player.sendSystemMessage(kitComponent);
        }

        // Pied de page
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7Utilisez §e/kit <nom> §7pour obtenir un kit"));
        player.sendSystemMessage(Component.literal("§8§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendSystemMessage(Component.literal(""));

        return 1;
    }

    /**
     * Donne un kit au joueur
     */
    private static int giveKit(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Cette commande ne peut être utilisée que par un joueur"));
            return 0;
        }

        String kitId = StringArgumentType.getString(context, "kitName");
        ServerLevel level = player.serverLevel();

        // Vérifier si le kit existe
        var kitOpt = KitManager.getInstance().getKit(kitId);
        if (kitOpt.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cKit inconnu: " + kitId));
            return 0;
        }

        Kit kit = kitOpt.get();

        // Vérifier les permissions
        if (!KitManager.getInstance().canUseKit(player, kit)) {
            player.sendSystemMessage(Component.literal("§cVous n'avez pas accès à ce kit !"));
            if (kit.hasRequiredRank()) {
                player.sendSystemMessage(Component.literal("§7Rank requis: §e" + kit.getRequiredRank()));
            }
            return 0;
        }

        // Vérifier le cooldown
        long cooldown = KitManager.getInstance().getCooldownRemaining(player.getUUID(), kitId);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§cVous devez attendre encore §e" + formatTime(cooldown) +
                    " §cavant de réutiliser ce kit !"));
            return 0;
        }

        // Donner le kit
        int result = KitManager.getInstance().giveKit(player, kitId, level);

        switch (result) {
            case 0 -> {
                // Succès
                player.sendSystemMessage(Component.literal(""));
                player.sendSystemMessage(Component.literal("§a§l✔ Kit reçu: §f" + kit.getDisplayName())
                        .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("§7" + kit.getDescription()));
                player.sendSystemMessage(Component.literal(""));
                return 1;
            }
            case 1 -> {
                // Pas de permission (déjà vérifié avant, mais par sécurité)
                player.sendSystemMessage(Component.literal("§cVous n'avez pas accès à ce kit !"));
                if (kit.hasRequiredRank()) {
                    player.sendSystemMessage(Component.literal("§7Rank requis: §e" + kit.getRequiredRank()));
                }
                return 0;
            }
            case 2 -> {
                // En cooldown (déjà vérifié avant, mais par sécurité)
                long remainingTime = KitManager.getInstance().getCooldownRemaining(player.getUUID(), kitId);
                player.sendSystemMessage(Component.literal("§cVous devez attendre encore §e" + formatTime(remainingTime) +
                        " §cavant de réutiliser ce kit !"));
                return 0;
            }
            case 3 -> {
                // Pas assez de place
                player.sendSystemMessage(Component.literal("§c✖ Inventaire plein !"));
                player.sendSystemMessage(Component.literal("§7Veuillez libérer de la place dans votre inventaire et réessayer."));
                return 0;
            }
            default -> {
                player.sendSystemMessage(Component.literal("§cImpossible de donner le kit."));
                return 0;
            }
        }
    }

    /**
     * Formate un temps en secondes en format lisible
     */
    private static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + "m " + secs + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
}
