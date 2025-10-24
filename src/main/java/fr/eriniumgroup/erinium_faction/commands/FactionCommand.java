package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;

import java.util.concurrent.CompletableFuture;

public class FactionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(buildRoot("faction"));
        d.register(buildRoot("f"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRoot(String root) {
        return Commands.literal(root)
                // Ouvrir le GUI: /<root> f [factionId]
                .then(Commands.literal("f").executes(ctx -> openFactionMenu(ctx.getSource().getPlayerOrException(), null)).then(Commands.argument("factionId", StringArgumentType.word()).suggests(FactionCommand::suggestFactionIds).executes(ctx -> openFactionMenu(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "factionId")))))
                // create <name>
                .then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestNewFactionName).executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    String name = StringArgumentType.getString(ctx, "name");
                    Faction f = FactionManager.create(name, name.substring(0, Math.min(4, name.length())).toUpperCase(), sp.getUUID());
                    if (f == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.create.fail"));
                        return 0;
                    }
                    FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                    sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.create.success", f.getName()), true);
                    return 1;
                })))
                // delete <name>
                .then(Commands.literal("delete").then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestFactionNames).requires(src -> src.hasPermission(2)).executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    boolean ok = FactionManager.delete(name);
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.delete.success"), true);
                    return 1;
                })))
                // info <name>
                .then(Commands.literal("info").then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestFactionNames).executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    Faction f = FactionManager.getByName(name);
                    if (f == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.info", f.getName(), String.format("%.0f", f.getPower()), String.format("%.0f", f.getMaxPower()), f.getLevel(), f.getXp()), false);
                    return 1;
                })))
                // join <name>
                .then(Commands.literal("join").then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestFactionNames).executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    String name = StringArgumentType.getString(ctx, "name");
                    Faction f = FactionManager.getByName(name);
                    if (f == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                        return 0;
                    }
                    int cap = FactionManager.getMaxMembersFor(f);
                    if (f.getMembers().size() >= cap) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.join.full", cap));
                        return 0;
                    }
                    boolean ok = FactionManager.invite(f, sp.getUUID(), sp.getGameProfile().getName());
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.join.fail"));
                        return 0;
                    }
                    FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                    sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.join.success", f.getName()), true);
                    return 1;
                })))
                // leave
                .then(Commands.literal("leave").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    Faction f = FactionManager.getFactionOf(sp.getUUID());
                    if (f == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                        return 0;
                    }
                    boolean ok = FactionManager.kick(f, sp.getUUID());
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.leave.fail"));
                        return 0;
                    }
                    FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                    sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.leave.success"), true);
                    return 1;
                }))
                // addxp <name> <amount>
                .then(Commands.literal("addxp").requires(src -> src.hasPermission(2)).then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestFactionNames).then(Commands.argument("amount", IntegerArgumentType.integer(1)).suggests(FactionCommand::suggestXpAmounts).executes(ctx -> {
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
                }))))
                // setrank <player> <rankId> (placeholder)
                .then(Commands.literal("setrank").requires(src -> src.hasPermission(2)).then(Commands.argument("player", StringArgumentType.word()).suggests(FactionCommand::suggestOnlinePlayers).then(Commands.argument("rankId", StringArgumentType.word()).suggests(FactionCommand::suggestServerRanks).executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.setrank.placeholder"), false);
                    return 1;
                }))))
                // claim
                .then(Commands.literal("claim").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    Faction f = FactionManager.getFactionOf(sp.getUUID());
                    if (f == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                        return 0;
                    }
                    Level lvl = sp.level();
                    ClaimKey key = ClaimKey.of(lvl.dimension(), sp.chunkPosition().x, sp.chunkPosition().z);
                    if (FactionManager.isClaimed(key)) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claim.already"));
                        return 0;
                    }
                    boolean ok = FactionManager.tryClaim(key, f.getId());
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claim.limit"));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claim.success"), true);
                    return 1;
                }))
                // unclaim
                .then(Commands.literal("unclaim").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    Faction f = FactionManager.getFactionOf(sp.getUUID());
                    if (f == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                        return 0;
                    }
                    Level lvl = sp.level();
                    ClaimKey key = ClaimKey.of(lvl.dimension(), sp.chunkPosition().x, sp.chunkPosition().z);
                    String owner = FactionManager.getClaimOwner(key);
                    if (owner == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.not_claimed"));
                        return 0;
                    }
                    if (!owner.equalsIgnoreCase(f.getId())) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.not_owner"));
                        return 0;
                    }
                    boolean ok = FactionManager.tryUnclaim(key, f.getId());
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.fail"));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.unclaim.success"), true);
                    return 1;
                }));
    }

    // Suggestions -------------------------------------------------------------
    private static CompletableFuture<Suggestions> suggestFactionNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (Faction f : FactionManager.getAllFactions()) builder.suggest(f.getName());
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestFactionIds(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (Faction f : FactionManager.getAllFactions()) {
            builder.suggest(f.getId());
            builder.suggest(f.getName());
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestNewFactionName(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        // Proposer quelques exemples si aucune faction actuelle
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

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var server = ctx.getSource().getServer();
        if (server != null) {
            for (var p : server.getPlayerList().getPlayers()) builder.suggest(p.getGameProfile().getName());
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestServerRanks(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var mgr = fr.eriniumgroup.erinium_faction.core.rank.EFRManager.get();
        for (var r : mgr.listRanksSorted()) builder.suggest(r.id);
        return builder.buildFuture();
    }

    // Ouverture du GUI --------------------------------------------------------
    private static int openFactionMenu(ServerPlayer sp, String factionIdOrNull) {
        Faction f = (factionIdOrNull == null || factionIdOrNull.isEmpty()) ? FactionManager.getFactionOf(sp.getUUID()) : FactionManager.getFaction(factionIdOrNull);
        if (f == null) {
            sp.sendSystemMessage(Component.translatable(factionIdOrNull == null ? "erinium_faction.cmd.faction.not_in_faction" : "erinium_faction.cmd.faction.not_found"));
            return 0;
        }
        var snapshot = FactionSnapshot.of(f);
        MenuProvider provider = new SimpleMenuProvider((id, inv, player) -> new FactionMenu(id, inv, null), Component.translatable("erinium_faction.faction.menu.title"));
        sp.openMenu(provider, buf -> {
            buf.writeBlockPos(sp.blockPosition());
            buf.writeVarInt(1);
            FactionSnapshot.write(snapshot, buf);
        });
        return 1;
    }
}
