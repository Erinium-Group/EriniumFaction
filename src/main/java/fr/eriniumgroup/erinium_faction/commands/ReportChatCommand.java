package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.eriniumgroup.erinium_faction.integration.discord.ChatReportManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Commande pour reporter les messages de chat
 */
public class ReportChatCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reportchat")
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> initiateReport(ctx, StringArgumentType.getString(ctx, "player"))))
        );

        dispatcher.register(Commands.literal("confirmreport")
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                        .executes(ctx -> confirmReport(ctx, StringArgumentType.getString(ctx, "reason"))))
        );

        dispatcher.register(Commands.literal("cancelreport")
                .executes(ReportChatCommand::cancelReport)
        );
    }

    private static int initiateReport(CommandContext<CommandSourceStack> ctx, String targetName) {
        try {
            ServerPlayer reporter = ctx.getSource().getPlayerOrException();
            ServerPlayer reported = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);

            if (reported == null) {
                reporter.sendSystemMessage(Component.literal("§cJoueur introuvable."));
                return 0;
            }

            if (reporter.getUUID().equals(reported.getUUID())) {
                reporter.sendSystemMessage(Component.literal("§cVous ne pouvez pas vous reporter vous-même."));
                return 0;
            }

            // Récupérer le dernier message du joueur depuis le cache
            String lastMessage = fr.eriniumgroup.erinium_faction.integration.discord.ChatMessageCache
                    .getLastMessage(reported.getUUID());

            // Créer le rapport en attente
            boolean success = ChatReportManager.createPendingReport(reporter, reported, lastMessage);

            if (!success) {
                return 0; // Le message d'erreur est déjà envoyé dans createPendingReport
            }

            // Envoyer le message de confirmation avec boutons
            reporter.sendSystemMessage(Component.literal("§e════════════════════════════════════════"));
            reporter.sendSystemMessage(Component.literal("§c§l⚠ CONFIRMATION DE REPORT ⚠"));
            reporter.sendSystemMessage(Component.literal("§e════════════════════════════════════════"));
            reporter.sendSystemMessage(Component.literal(""));
            reporter.sendSystemMessage(Component.literal("§7Joueur: §f" + reported.getName().getString()));
            reporter.sendSystemMessage(Component.literal("§7Message: §f" + lastMessage));
            reporter.sendSystemMessage(Component.literal(""));
            reporter.sendSystemMessage(Component.literal("§eChoisissez une raison ou annulez:"));
            reporter.sendSystemMessage(Component.literal(""));

            // Boutons de raison
            sendReasonButton(reporter, "§c[Langage inapproprié]", "Langage inapproprié");
            sendReasonButton(reporter, "§c[Spam]", "Spam");
            sendReasonButton(reporter, "§c[Harcèlement]", "Harcèlement");
            sendReasonButton(reporter, "§c[Publicité]", "Publicité");
            sendReasonButton(reporter, "§c[Autre]", "Autre raison");

            reporter.sendSystemMessage(Component.literal(""));

            // Bouton d'annulation
            MutableComponent cancelButton = Component.literal("§7[❌ Annuler]")
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cancelreport"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§7Cliquez pour annuler le report")))
                    );
            reporter.sendSystemMessage(cancelButton);

            reporter.sendSystemMessage(Component.literal("§e════════════════════════════════════════"));
            reporter.sendSystemMessage(Component.literal("§7§oExpire dans 30 secondes..."));

            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErreur lors du report."));
            return 0;
        }
    }

    private static void sendReasonButton(ServerPlayer player, String display, String reason) {
        MutableComponent button = Component.literal(display)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/confirmreport " + reason))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal("§eCliquez pour confirmer avec cette raison")))
                );
        player.sendSystemMessage(button);
    }

    private static int confirmReport(CommandContext<CommandSourceStack> ctx, String reason) {
        try {
            ServerPlayer reporter = ctx.getSource().getPlayerOrException();

            boolean success = ChatReportManager.confirmReport(reporter, reason);

            if (!success) {
                return 0; // Le message d'erreur est déjà envoyé dans confirmReport
            }

            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErreur lors de la confirmation du report."));
            return 0;
        }
    }

    private static int cancelReport(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer reporter = ctx.getSource().getPlayerOrException();

            boolean success = ChatReportManager.cancelReport(reporter);

            if (!success) {
                return 0; // Le message d'erreur est déjà envoyé dans cancelReport
            }

            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErreur lors de l'annulation du report."));
            return 0;
        }
    }
}
