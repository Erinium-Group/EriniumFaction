package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * /ef perm <add|remove|list> <player> [permission]
 *
 * Gestion simple d'overrides de permissions au niveau joueur.
 */
public class EFCommand {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("ef")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("perm")
                        .then(Commands.literal("list")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests(EFCommand::suggestOnlinePlayers)
                                        .executes(ctx -> doList(ctx))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers)
                                        .then(Commands.argument("permission", StringArgumentType.greedyString()).suggests(EFCommand::suggestKnownPermissions)
                                                .executes(ctx -> doAdd(ctx)))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers)
                                        .then(Commands.argument("permission", StringArgumentType.greedyString()).suggests(EFCommand::suggestPlayerPermissions)
                                                .executes(ctx -> doRemove(ctx))))))
        );
    }

    // Impl ---------------------------------------------------------------

    private static int doList(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "player");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.player_not_found"));
            return 0;
        }
        Set<String> list = EFRManager.get().listPlayerPermissions(target.getUUID());
        if (list.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Aucun override pour " + target.getGameProfile().getName()), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("Overrides pour " + target.getGameProfile().getName() + ":"), false);
            for (String p : list) ctx.getSource().sendSuccess(() -> Component.literal(" - " + p), false);
        }
        return 1;
    }

    private static int doAdd(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "player");
        String perm = StringArgumentType.getString(ctx, "permission");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.player_not_found"));
            return 0;
        }
        boolean ok = EFRManager.get().addPlayerPermission(target.getUUID(), perm);
        if (!ok) {
            ctx.getSource().sendFailure(Component.literal("Permission déjà présente ou invalide."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Permission ajoutée à " + target.getGameProfile().getName()), true);
        return 1;
    }

    private static int doRemove(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "player");
        String perm = StringArgumentType.getString(ctx, "permission");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.player_not_found"));
            return 0;
        }
        boolean ok = EFRManager.get().removePlayerPermission(target.getUUID(), perm);
        if (!ok) {
            ctx.getSource().sendFailure(Component.literal("Permission absente ou invalide."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Permission retirée de " + target.getGameProfile().getName()), true);
        return 1;
    }

    // Suggestions -----------------------------------------------------------

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        var server = ctx.getSource().getServer();
        if (server != null) for (var p : server.getPlayerList().getPlayers()) b.suggest(p.getGameProfile().getName());
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestKnownPermissions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        for (String p : EFRManager.get().getKnownPermissions()) b.suggest(p);
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPlayerPermissions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        try {
            String name = StringArgumentType.getString(ctx, "player");
            ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
            if (target != null) for (String p : EFRManager.get().listPlayerPermissions(target.getUUID())) b.suggest(p);
        } catch (Exception ignored) {}
        return b.buildFuture();
    }
}

