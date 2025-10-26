package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.Faction.Mode;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * /ef perm <add|remove|list> <player> [permission]
 * <p>
 * Gestion simple d'overrides de permissions au niveau joueur.
 */
public class EFCommand {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("ef").requires(src -> src.hasPermission(2)).then(Commands.literal("perm").then(Commands.literal("list").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).executes(ctx -> doList(ctx)))).then(Commands.literal("add").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("permission", StringArgumentType.greedyString()).suggests(EFCommand::suggestKnownPermissions).executes(EFCommand::doAdd)))).then(Commands.literal("remove").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("permission", StringArgumentType.greedyString()).suggests(EFCommand::suggestPlayerPermissions).executes(EFCommand::doRemove)))))
                // delete <name> - Supprime une faction
                .then(Commands.literal("delete").then(Commands.argument("name", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).executes(EFCommand::doDelete)))
                // addxp <name> <amount> - Ajoute de l'XP à une faction
                .then(Commands.literal("addxp").then(Commands.argument("name", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("amount", IntegerArgumentType.integer(1)).suggests(EFCommand::suggestXpAmounts).executes(EFCommand::doAddXp))))
                // setrank <player> <rankId> - Définit le rang d'un joueur
                .then(Commands.literal("setrank").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("rankId", StringArgumentType.word()).suggests(EFCommand::suggestServerRanks).executes(EFCommand::doSetRank))))
                // mode <factionName> <PUBLIC|INVITE_ONLY> - Change le mode de la faction
                .then(Commands.literal("mode").then(Commands.argument("factionName", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("value", StringArgumentType.word()).suggests((ctx, b) -> {
                    b.suggest("PUBLIC");
                    b.suggest("INVITE_ONLY");
                    return b.buildFuture();
                }).executes(EFCommand::doMode))))
                // flag <factionName> <admin|warzone|safezone> <on|off> - Modifie les flags
                .then(Commands.literal("flag").then(Commands.argument("factionName", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("flagName", StringArgumentType.word()).suggests((c, b) -> {
                    b.suggest("admin");
                    b.suggest("warzone");
                    b.suggest("safezone");
                    return b.buildFuture();
                }).then(Commands.argument("value", StringArgumentType.word()).suggests((c, b) -> {
                    b.suggest("on");
                    b.suggest("off");
                    return b.buildFuture();
                }).executes(EFCommand::doFlag))))));
    }

    // Impl ---------------------------------------------------------------

    private static int doList(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "player");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
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
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
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
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
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

    private static int doDelete(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        boolean ok = FactionManager.delete(name);
        if (!ok) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.delete.success"), true);
        return 1;
    }

    private static int doAddXp(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        Faction f = FactionManager.getByName(name);
        if (f == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
            return 0;
        }
        f.addXp(amount);
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.addxp.success"), true);
        return 1;
    }

    private static int doSetRank(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.setrank.placeholder"), false);
        return 1;
    }

    private static int doMode(CommandContext<CommandSourceStack> ctx) {
        String factionName = StringArgumentType.getString(ctx, "factionName");
        String val = StringArgumentType.getString(ctx, "value");
        Faction f = FactionManager.getByName(factionName);
        if (f == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
            return 0;
        }
        try {
            f.setMode(Mode.valueOf(val.toUpperCase()));
            FactionManager.markDirty();
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.mode.set", f.getMode().name()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.mode.invalid"));
            return 0;
        }
    }

    private static int doFlag(CommandContext<CommandSourceStack> ctx) {
        String factionName = StringArgumentType.getString(ctx, "factionName");
        String name = StringArgumentType.getString(ctx, "flagName");
        String v = StringArgumentType.getString(ctx, "value");
        Faction f = FactionManager.getByName(factionName);
        if (f == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
            return 0;
        }
        boolean on = v.equalsIgnoreCase("on");
        switch (name.toLowerCase()) {
            case "admin" -> f.setAdminFaction(on);
            case "warzone" -> f.setWarzone(on);
            case "safezone" -> f.setSafezone(on);
            default -> {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.flag.invalid"));
                return 0;
            }
        }
        FactionManager.markDirty();
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.flag.set", name, on ? "on" : "off"), true);
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
        } catch (Exception ignored) {
        }
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestFactionNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (Faction f : FactionManager.getAllFactions()) builder.suggest(f.getName());
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestXpAmounts(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        builder.suggest("10");
        builder.suggest("50");
        builder.suggest("100");
        builder.suggest("250");
        builder.suggest("1000");
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestServerRanks(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (var r : EFRManager.get().listRanksSorted()) builder.suggest(r.id);
        return builder.buildFuture();
    }
}

