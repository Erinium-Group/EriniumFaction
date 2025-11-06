package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.features.economy.EconomyIntegration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * Commandes d’économie (/money): solde, transferts, administration et classement.
 *
 * @author Blaackknight <dragclover@gmail.com>
 */
public class EconomyCommand {
    /**
     * Enregistre la commande /money et ses sous-commandes.
     */
    private static boolean hasServerPerm(CommandSourceStack src, String node) {
        try {
            if (src.hasPermission(2)) return true; // OP
            ServerPlayer sp = src.getPlayer();
            if (sp == null) return true; // console autorisée
            return fr.eriniumgroup.erinium_faction.core.permissions.EFPerms.has(sp, node);
        } catch (Exception e) {
            return false;
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("money").executes(ctx -> showBalance(ctx.getSource())).then(Commands.literal("pay").then(Commands.argument("player", StringArgumentType.word()).suggests(EconomyCommand::suggestOnlinePlayers).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01)).suggests(EconomyCommand::suggestAmounts).executes(ctx -> doPay(ctx.getSource(), StringArgumentType.getString(ctx, "player"), DoubleArgumentType.getDouble(ctx, "amount")))))).then(Commands.literal("set").requires(src -> hasServerPerm(src, "ef.economy.admin")).then(Commands.argument("player", StringArgumentType.word()).suggests(EconomyCommand::suggestOnlinePlayers).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0)).suggests(EconomyCommand::suggestAmounts).executes(ctx -> doSet(ctx.getSource(), StringArgumentType.getString(ctx, "player"), DoubleArgumentType.getDouble(ctx, "amount")))))).then(Commands.literal("give").requires(src -> hasServerPerm(src, "ef.economy.admin")).then(Commands.argument("player", StringArgumentType.word()).suggests(EconomyCommand::suggestOnlinePlayers).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01)).suggests(EconomyCommand::suggestAmounts).executes(ctx -> doGive(ctx.getSource(), StringArgumentType.getString(ctx, "player"), DoubleArgumentType.getDouble(ctx, "amount")))))).then(Commands.literal("take").requires(src -> hasServerPerm(src, "ef.economy.admin")).then(Commands.argument("player", StringArgumentType.word()).suggests(EconomyCommand::suggestOnlinePlayers).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01)).suggests(EconomyCommand::suggestAmounts).executes(ctx -> doTake(ctx.getSource(), StringArgumentType.getString(ctx, "player"), DoubleArgumentType.getDouble(ctx, "amount")))))).then(Commands.literal("top").executes(ctx -> showTop(ctx.getSource(), 1)).then(Commands.argument("page", IntegerArgumentType.integer(1)).suggests(EconomyCommand::suggestPages).executes(ctx -> showTop(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page"))))));
    }

    // Sous-commandes impl ----------------------------------------------------
    private static int doPay(CommandSourceStack src, String targetName, double amount) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sender = src.getPlayerOrException();
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            src.sendFailure(Component.translatable("erinium_faction.cmd.economy.player_not_found"));
            return 0;
        }
        if (amount <= 0) {
            src.sendFailure(Component.translatable("erinium_faction.cmd.economy.invalid_amount"));
            return 0;
        }
        if (!EconomyIntegration.withdraw(sender, amount)) {
            src.sendFailure(Component.translatable("erinium_faction.cmd.economy.not_enough"));
            return 0;
        }
        EconomyIntegration.deposit(target, amount);
        syncMoney(sender);
        syncMoney(target);
        src.sendSuccess(() -> Component.translatable("erinium_faction.cmd.economy.pay.success", target.getGameProfile().getName(), String.format("%.2f", amount)), true);
        return 1;
    }

    private static int doSet(CommandSourceStack src, String targetName, double amount) {
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            src.sendFailure(Component.translatable("erinium_faction.cmd.economy.player_not_found"));
            return 0;
        }
        EconomyIntegration.setBalance(target, amount);
        syncMoney(target);
        src.sendSuccess(() -> Component.translatable("erinium_faction.cmd.economy.set", target.getGameProfile().getName(), String.format("%.2f", amount)), true);
        return 1;
    }

    private static int doGive(CommandSourceStack src, String targetName, double amount) {
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            src.sendFailure(Component.translatable("erinium_faction.cmd.economy.player_not_found"));
            return 0;
        }
        EconomyIntegration.deposit(target, amount);
        syncMoney(target);
        src.sendSuccess(() -> Component.translatable("erinium_faction.cmd.economy.give", target.getGameProfile().getName(), String.format("%.2f", amount)), true);
        return 1;
    }

    private static int doTake(CommandSourceStack src, String targetName, double amount) {
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            src.sendFailure(Component.translatable("erinium_faction.cmd.economy.player_not_found"));
            return 0;
        }
        if (!EconomyIntegration.withdraw(target, amount)) {
            src.sendFailure(Component.translatable("erinium_faction.cmd.economy.not_enough"));
            return 0;
        }
        syncMoney(target);
        src.sendSuccess(() -> Component.translatable("erinium_faction.cmd.economy.take", target.getGameProfile().getName(), String.format("%.2f", amount)), true);
        return 1;
    }

    /**
     * Affiche le solde du joueur appelant.
     */
    private static int showBalance(CommandSourceStack src) {
        try {
            ServerPlayer sp = src.getPlayerOrException();
            double bal = EconomyIntegration.getBalance(sp);
            src.sendSuccess(() -> Component.translatable("erinium_faction.cmd.economy.balance", String.format("%.2f", bal)), false);
            // sync var client
            syncMoney(sp);
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.translatable("erinium_faction.cmd.economy.only_players"));
            return 0;
        }
    }

    /**
     * Affiche le classement par page (top global offline/online).
     */
    private static int showTop(CommandSourceStack src, int page) {
        var server = src.getServer();
        var entries = EconomyIntegration.getTop(server);
        int total = entries.size();
        if (total == 0) {
            src.sendSuccess(() -> Component.translatable("erinium_faction.cmd.economy.top.empty"), false);
            return 1;
        }
        int perPage = 10;
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) perPage));
        int p = Math.max(1, Math.min(page, totalPages));
        int start = (p - 1) * perPage;
        int end = Math.min(total, start + perPage);
        src.sendSuccess(() -> Component.translatable("erinium_faction.cmd.economy.top.header", p, totalPages, total), false);
        for (int i = start; i < end; i++) {
            var e = entries.get(i);
            final int rank = i + 1;
            final String name = e.name != null ? e.name : e.uuid.toString();
            final String balStr = String.format("%.2f", e.balance);
            src.sendSuccess(() -> Component.translatable("erinium_faction.cmd.economy.top.item", rank, name, balStr), false);
        }
        return 1;
    }

    /**
     * Synchronise la variable client EFVariables.money pour un joueur.
     */
    private static void syncMoney(ServerPlayer p) {
        var vars = p.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES);
        vars.money = EconomyIntegration.getBalance(p);
        vars.syncPlayerVariables(p);
    }

    /**
     * Suggestions: liste des joueurs en ligne.
     */
    private static CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var server = ctx.getSource().getServer();
        if (server != null) {
            for (var p : server.getPlayerList().getPlayers()) {
                builder.suggest(p.getGameProfile().getName());
            }
        }
        return builder.buildFuture();
    }

    /**
     * Suggestions: montants courants.
     */
    private static CompletableFuture<Suggestions> suggestAmounts(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        builder.suggest("10");
        builder.suggest("50");
        builder.suggest("100");
        builder.suggest("250");
        builder.suggest("1000");
        return builder.buildFuture();
    }

    /**
     * Suggestions: pages rapides 1..5.
     */
    private static CompletableFuture<Suggestions> suggestPages(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        builder.suggest("1");
        builder.suggest("2");
        builder.suggest("3");
        builder.suggest("4");
        builder.suggest("5");
        return builder.buildFuture();
    }
}
