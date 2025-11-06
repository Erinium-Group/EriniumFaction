package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class RankCommand {
    private static boolean hasServerPerm(CommandSourceStack src, String node) {
        try {
            if (src.hasPermission(2)) return true; // OP
            net.minecraft.server.level.ServerPlayer sp = src.getPlayer();
            if (sp == null) return true; // console autorisée
            return fr.eriniumgroup.erinium_faction.core.permissions.EFPerms.has(sp, node);
        } catch (Exception e) {
            return false;
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("rank").requires(src -> hasServerPerm(src, "ef.rank.admin"))
                .then(Commands.literal("create").then(Commands.argument("id", StringArgumentType.word()).then(Commands.argument("display", StringArgumentType.greedyString()).executes(ctx -> {
                    String id = StringArgumentType.getString(ctx, "id").toLowerCase(Locale.ROOT);
                    String display = StringArgumentType.getString(ctx, "display");
                    boolean ok = EFRManager.get().createRank(id, display, 0);
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.literal("Impossible de créer le rank."));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("Rank créé: " + id), true);
                    return 1;
                }))))

                .then(Commands.literal("priority").then(Commands.argument("id", StringArgumentType.word()).suggests(RankCommand::suggestRankIds).then(Commands.argument("priority", IntegerArgumentType.integer()).executes(ctx -> {
                    String id = StringArgumentType.getString(ctx, "id");
                    int pr = IntegerArgumentType.getInteger(ctx, "priority");
                    var r = EFRManager.get().getRank(id);
                    if (r == null) {
                        ctx.getSource().sendFailure(Component.literal("Rank introuvable"));
                        return 0;
                    }
                    r.priority = pr;
                    EFRManager.get().save();
                    ctx.getSource().sendSuccess(() -> Component.literal("Priority mise à jour."), true);
                    return 1;
                }))))

                .then(Commands.literal("perm")
                        .then(Commands.literal("add").then(Commands.argument("id", StringArgumentType.word()).suggests(RankCommand::suggestRankIds)
                                .then(Commands.argument("perm", StringArgumentType.greedyString()).suggests(RankCommand::suggestKnownPermissions).executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    String perm = StringArgumentType.getString(ctx, "perm");
                                    boolean ok = EFRManager.get().addPermission(id, perm);
                                    if (!ok) {
                                        ctx.getSource().sendFailure(Component.literal("Echec ajout permission"));
                                        return 0;
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal("Permission ajoutée."), true);
                                    return 1;
                                }))))

                        .then(Commands.literal("remove").then(Commands.argument("id", StringArgumentType.word()).suggests(RankCommand::suggestRankIds)
                                .then(Commands.argument("perm", StringArgumentType.greedyString()).suggests(RankCommand::suggestRankPermissions).executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    String perm = StringArgumentType.getString(ctx, "perm");
                                    boolean ok = EFRManager.get().removePermission(id, perm);
                                    if (!ok) {
                                        ctx.getSource().sendFailure(Component.literal("Echec suppression permission"));
                                        return 0;
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal("Permission retirée."), true);
                                    return 1;
                                }))))

                        .then(Commands.literal("list").then(Commands.argument("id", StringArgumentType.word()).suggests(RankCommand::suggestRankIds).executes(ctx -> {
                            String id = StringArgumentType.getString(ctx, "id");
                            var r = EFRManager.get().getRank(id);
                            if (r == null) {
                                ctx.getSource().sendFailure(Component.literal("Rank introuvable"));
                                return 0;
                            }
                            if (r.permissions == null || r.permissions.isEmpty()) {
                                ctx.getSource().sendSuccess(() -> Component.literal("Aucune permission dans ce rank."), false);
                            } else {
                                ctx.getSource().sendSuccess(() -> Component.literal("Permissions du rank " + r.id + ":"), false);
                                for (String p : r.permissions) ctx.getSource().sendSuccess(() -> Component.literal(" - " + p), false);
                            }
                            return 1;
                        }))))

        );
    }

    // Suggestions -------------------------------------------------------------
    private static CompletableFuture<Suggestions> suggestRankIds(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        for (var r : EFRManager.get().listRanksSorted()) b.suggest(r.id);
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestKnownPermissions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        for (String p : EFRManager.get().getKnownPermissions()) b.suggest(p);
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestRankPermissions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        try {
            String id = StringArgumentType.getString(ctx, "id");
            var r = EFRManager.get().getRank(id);
            if (r != null && r.permissions != null) for (String p : r.permissions) b.suggest(p);
        } catch (Exception ignored) {}
        return b.buildFuture();
    }
}
