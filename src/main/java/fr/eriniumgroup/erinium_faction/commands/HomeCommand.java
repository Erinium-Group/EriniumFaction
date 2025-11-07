package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.features.homes.HomesManager;
import fr.eriniumgroup.erinium_faction.features.homes.PlayerHomesData;
import fr.eriniumgroup.erinium_faction.features.homes.HomesConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * Commandes de homes: /home, /sethome, /homes
 */
public class HomeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /home <name>
        dispatcher.register(Commands.literal("home").then(Commands.argument("name", StringArgumentType.word()).suggests(HomeCommand::suggestHomes).executes(HomeCommand::doHome)));

        // /sethome [name]
        dispatcher.register(Commands.literal("sethome").executes(ctx -> doSetHome(ctx, "home")).then(Commands.argument("name", StringArgumentType.word()).executes(HomeCommand::doSetHome)));

        // /homes
        dispatcher.register(Commands.literal("homes").executes(HomeCommand::doListHomes));
    }

    private static int doHome(CommandContext<CommandSourceStack> ctx) {
        String homeName = StringArgumentType.getString(ctx, "name");
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.only_players"));
            return 0;
        }

        var home = HomesManager.getHome(player, homeName);
        if (home.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.home.not_found", homeName));
            return 0;
        }

        HomesManager.teleportHome(player, homeName);
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.home.tp", homeName), false);
        return 1;
    }

    private static int doSetHome(CommandContext<CommandSourceStack> ctx) {
        return doSetHome(ctx, StringArgumentType.getString(ctx, "name"));
    }

    private static int doSetHome(CommandContext<CommandSourceStack> ctx, String homeName) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.only_players"));
            return 0;
        }

        var server = player.getServer();
        if (server == null) return 0;

        HomesConfig config = HomesConfig.get(server);
        var playerHomes = HomesManager.getPlayerHomes(player);
        if (playerHomes.isPresent() && playerHomes.get().hasMaxHomes(config) && playerHomes.get().getHome(homeName).isEmpty()) {
            int max = playerHomes.get().getMaxHomes(config);
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.home.max", max));
            return 0;
        }

        HomesManager.setHome(player, homeName);
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.home.set", homeName), false);
        return 1;
    }

    private static int doListHomes(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.only_players"));
            return 0;
        }

        var server = player.getServer();
        if (server == null) return 0;

        HomesConfig config = HomesConfig.get(server);
        var playerHomes = HomesManager.getPlayerHomes(player);
        if (playerHomes.isEmpty() || playerHomes.get().getAllHomes().isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.home.list.none"), false);
            return 1;
        }

        PlayerHomesData homes = playerHomes.get();
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.home.list.header"), false);
        homes.getAllHomes().forEach((name, home) -> {
            String info = String.format("§a- %s §7(%s: %.0f, %.0f, %.0f)", name, home.getDimension(), home.getX(), home.getY(), home.getZ());
            ctx.getSource().sendSuccess(() -> Component.literal(info), false);
        });
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.home.list.count", homes.getHomeCount(), homes.getMaxHomes(config)), false);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestHomes(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            var playerHomes = HomesManager.getPlayerHomes(player);
            if (playerHomes.isPresent()) {
                playerHomes.get().getAllHomes().keySet().forEach(builder::suggest);
            }
        }
        return builder.buildFuture();
    }
}

