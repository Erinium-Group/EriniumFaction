package fr.eriniumgroup.eriniumfaction.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.eriniumfaction.rank.EFRManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class RankCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("rank")
                // rank create <id> <displayName> <priority>
                .then(Commands.literal("create").requires(RankCommand::canManage)
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("displayName", StringArgumentType.string())
                                        .then(Commands.argument("priority", IntegerArgumentType.integer())
                                                .executes(ctx -> doCreate(ctx))))))
                // rank delete <id>
                .then(Commands.literal("delete").requires(RankCommand::canManage)
                        .then(Commands.argument("id", StringArgumentType.word()).suggests(RankCommand::suggestRanks)
                                .executes(ctx -> doDelete(ctx))))
                // rank list
                .then(Commands.literal("list").requires(RankCommand::canView)
                        .executes(RankCommand::doList))
                // rank info <id>
                .then(Commands.literal("info").requires(RankCommand::canView)
                        .then(Commands.argument("id", StringArgumentType.word()).suggests(RankCommand::suggestRanks)
                                .executes(RankCommand::doInfo)))
                // rank perm add/remove <rankId> <perm>
                .then(Commands.literal("perm")
                        .then(Commands.literal("add").requires(RankCommand::canManage)
                                .then(Commands.argument("rankId", StringArgumentType.word()).suggests(RankCommand::suggestRanks)
                                        .then(Commands.argument("permission", StringArgumentType.string())
                                                .executes(RankCommand::doPermAdd))))
                        .then(Commands.literal("remove").requires(RankCommand::canManage)
                                .then(Commands.argument("rankId", StringArgumentType.word()).suggests(RankCommand::suggestRanks)
                                        .then(Commands.argument("permission", StringArgumentType.string())
                                                .executes(RankCommand::doPermRemove)))))
                // rank set <player> <rankId>
                .then(Commands.literal("set").requires(RankCommand::canManage)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rankId", StringArgumentType.word()).suggests(RankCommand::suggestRanks)
                                        .executes(RankCommand::doSet))))
                // rank get <player>
                .then(Commands.literal("get").requires(RankCommand::canView)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(RankCommand::doGet)))
                // rank reload
                .then(Commands.literal("reload").requires(RankCommand::canManage)
                        .executes(RankCommand::doReload))
        );
    }

    // Permissions helpers -----------------------------------------------------

    private static boolean canManage(CommandSourceStack src) {
        if (src.hasPermission(2)) return true; // OP
        if (src.getEntity() instanceof ServerPlayer sp) {
            return EFRManager.get().hasPermission(sp, "efr.rank.manage");
        }
        return false;
    }

    private static boolean canView(CommandSourceStack src) {
        if (src.hasPermission(2)) return true; // OP
        if (src.getEntity() instanceof ServerPlayer sp) {
            return EFRManager.get().hasPermission(sp, "efr.rank.view") || EFRManager.get().hasPermission(sp, "efr.rank.manage");
        }
        return false;
    }

    private static CompletableFuture<Suggestions> suggestRanks(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        for (var r : EFRManager.get().listRanksSorted()) {
            b.suggest(r.id);
        }
        return b.buildFuture();
    }

    // Executors ---------------------------------------------------------------

    private static int doCreate(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "id");
        String dn = StringArgumentType.getString(ctx, "displayName");
        int pr = IntegerArgumentType.getInteger(ctx, "priority");
        boolean ok = EFRManager.get().createRank(id, dn, pr);
        if (ok) ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.create.success", id), false);
        else ctx.getSource().sendFailure(Component.translatable("erinium_faction.rank.create.fail"));
        return ok ? 1 : 0;
    }

    private static int doDelete(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "id");
        boolean ok = EFRManager.get().deleteRank(id);
        if (ok) ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.delete.success", id), false);
        else ctx.getSource().sendFailure(Component.translatable("erinium_faction.rank.not_found", id));
        return ok ? 1 : 0;
    }

    private static int doList(CommandContext<CommandSourceStack> ctx) {
        var list = EFRManager.get().listRanksSorted();
        if (list.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.list.empty"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.list.header"), false);
            for (var r : list) {
                String dn = r.displayName == null ? "" : r.displayName;
                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.list.item", r.id, r.priority, dn), false);
            }
        }
        return 1;
    }

    private static int doInfo(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "id");
        var r = EFRManager.get().getRank(id);
        if (r == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.rank.not_found", id));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.info.header", r.id, r.displayName, r.priority), false);
        if (r.permissions.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.info.perms.empty"), false);
        } else {
            String joined = String.join(", ", r.permissions);
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.info.perms.list", r.permissions.size(), joined), false);
        }
        return 1;
    }

    private static int doPermAdd(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "rankId");
        String perm = StringArgumentType.getString(ctx, "permission");
        boolean ok = EFRManager.get().addPermission(id, perm);
        if (ok) ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.perm.add.success", perm, id), false);
        else ctx.getSource().sendFailure(Component.translatable("erinium_faction.rank.perm.add.fail"));
        return ok ? 1 : 0;
    }

    private static int doPermRemove(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "rankId");
        String perm = StringArgumentType.getString(ctx, "permission");
        boolean ok = EFRManager.get().removePermission(id, perm);
        if (ok) ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.perm.remove.success", perm, id), false);
        else ctx.getSource().sendFailure(Component.translatable("erinium_faction.rank.perm.remove.fail"));
        return ok ? 1 : 0;
    }

    private static int doSet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer target;
        try {
            target = EntityArgument.getPlayer(ctx, "player");
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
            return 0;
        }
        String rankId = StringArgumentType.getString(ctx, "rankId");
        boolean ok = EFRManager.get().setPlayerRank(target.getUUID(), rankId);
        if (ok) ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.set.success", target.getGameProfile().getName(), rankId), false);
        else ctx.getSource().sendFailure(Component.translatable("erinium_faction.rank.set.fail"));
        return ok ? 1 : 0;
    }

    private static int doGet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer target;
        try {
            target = EntityArgument.getPlayer(ctx, "player");
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
            return 0;
        }
        var r = EFRManager.get().getPlayerRank(target.getUUID());
        if (r == null) ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.get.none", target.getGameProfile().getName()), false);
        else ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.get.success", target.getGameProfile().getName(), r.id, r.displayName), false);
        return 1;
    }

    private static int doReload(CommandContext<CommandSourceStack> ctx) {
        EFRManager.get().load();
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.rank.reload.success"), false);
        return 1;
    }
}
