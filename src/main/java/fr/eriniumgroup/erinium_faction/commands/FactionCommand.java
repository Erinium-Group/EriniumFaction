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
import fr.eriniumgroup.erinium_faction.core.faction.Faction.Mode;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
                .then(Commands.literal("delete")
                        .requires(src -> hasServerPerm(src, "ef.faction.delete"))
                        .then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestFactionNames).executes(ctx -> {
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
                .then(Commands.literal("addxp")
                        .requires(src -> hasServerPerm(src, "ef.faction.addxp"))
                        .then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestFactionNames)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1)).suggests(FactionCommand::suggestXpAmounts).executes(ctx -> {
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
                .then(Commands.literal("setrank")
                        .requires(src -> hasServerPerm(src, "ef.rank.set"))
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(FactionCommand::suggestOnlinePlayers)
                                .then(Commands.argument("rankId", StringArgumentType.word()).suggests(FactionCommand::suggestServerRanks).executes(ctx -> {
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
                }))
                // mode <PUBLIC|INVITE_ONLY>
                .then(Commands.literal("mode")
                        .requires(src -> hasServerPerm(src, "ef.faction.mode"))
                        .then(Commands.argument("value", StringArgumentType.word()).suggests((ctx, b) -> {
                            b.suggest("PUBLIC");
                            b.suggest("INVITE_ONLY");
                            return b.buildFuture();
                        }).executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) {
                                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                                return 0;
                            }
                            String val = StringArgumentType.getString(ctx, "value");
                            try {
                                f.setMode(Mode.valueOf(val.toUpperCase()));
                                FactionManager.markDirty();
                                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.mode.set", f.getMode().name()), true);
                                return 1;
                            } catch (Exception e) {
                                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.mode.invalid"));
                                return 0;
                            }
                        })))
                // flags: admin/warzone/safezone on|off
                .then(Commands.literal("flag").requires(src -> hasServerPerm(src, "ef.faction.flag")).then(Commands.argument("name", StringArgumentType.word()).suggests((c, b) -> {
                    b.suggest("admin");
                    b.suggest("warzone");
                    b.suggest("safezone");
                    return b.buildFuture();
                }).then(Commands.argument("value", StringArgumentType.word()).suggests((c, b) -> {
                    b.suggest("on");
                    b.suggest("off");
                    return b.buildFuture();
                }).executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    Faction f = FactionManager.getFactionOf(sp.getUUID());
                    if (f == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                        return 0;
                    }
                    String name = StringArgumentType.getString(ctx, "name");
                    String v = StringArgumentType.getString(ctx, "value");
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
                }))))
                // desc <text>
                .then(Commands.literal("desc").requires(src -> hasServerPerm(src, "ef.faction.desc")).then(Commands.argument("text", StringArgumentType.greedyString()).executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    Faction f = FactionManager.getFactionOf(sp.getUUID());
                    if (f == null) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                        return 0;
                    }
                    String t = StringArgumentType.getString(ctx, "text");
                    f.setDescription(t);
                    FactionManager.markDirty();
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.desc.set"), true);
                    return 1;
                })))
                // bank deposit/withdraw <amount>
                .then(Commands.literal("bank")
                        .then(Commands.literal("deposit").requires(src -> hasServerPerm(src, "ef.faction.bank.deposit")).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) {
                                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                                return 0;
                            }
                            int a = IntegerArgumentType.getInteger(ctx, "amount");
                            // Débiter l'argent du joueur vers la banque de faction
                            if (!fr.eriniumgroup.erinium_faction.integration.economy.EconomyIntegration.withdraw(sp, a)) {
                                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.bank.player_not_enough"));
                                return 0;
                            }
                            f.deposit(a);
                            FactionManager.markDirty();
                            // Sync affiche client
                            fr.eriniumgroup.erinium_faction.core.faction.FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                            // mettre à jour money côté client
                            sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).money = fr.eriniumgroup.erinium_faction.integration.economy.EconomyIntegration.getBalance(sp);
                            sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.bank.deposit", a), true);
                            return 1;
                        })))
                        .then(Commands.literal("withdraw").requires(src -> hasServerPerm(src, "ef.faction.bank.withdraw")).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) {
                                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                                return 0;
                            }
                            int a = IntegerArgumentType.getInteger(ctx, "amount");
                            boolean ok = f.withdraw(a);
                            if (!ok) {
                                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.bank.not_enough"));
                                return 0;
                            }
                            // Créditer l'argent au joueur
                            fr.eriniumgroup.erinium_faction.integration.economy.EconomyIntegration.deposit(sp, a);
                            FactionManager.markDirty();
                            // Sync client variables
                            fr.eriniumgroup.erinium_faction.core.faction.FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                            sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).money = fr.eriniumgroup.erinium_faction.integration.economy.EconomyIntegration.getBalance(sp);
                            sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.bank.withdraw", a), true);
                            return 1;
                        }))))
                // home set|tp and direct tp on /home
                .then(Commands.literal("home")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            if (!hasServerPerm(ctx.getSource(), "ef.faction.home.tp")) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.no_permission")); return 0; }
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                            int[] home = f.getHome();
                            if (home == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.home.missing")); return 0; }
                            ServerLevel lvl = sp.serverLevel();
                            sp.teleportTo(lvl, home[0] + 0.5, home[1] + 0.1, home[2] + 0.5, sp.getYRot(), sp.getXRot());
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.home.tp.success"), false);
                            return 1;
                        })
                        .then(Commands.literal("set").requires(src -> hasServerPerm(src, "ef.faction.home.set")).executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                            var pos = sp.blockPosition();
                            var dim = sp.level().dimension().location();
                            f.setHome(pos.getX(), pos.getY(), pos.getZ(), dim);
                            FactionManager.markDirty();
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.home.set", pos.getX(), pos.getY(), pos.getZ()), true);
                            return 1;
                        }))
                        .then(Commands.literal("tp").requires(src -> hasServerPerm(src, "ef.faction.home.tp")).executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                            int[] home = f.getHome();
                            if (home == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.home.missing")); return 0; }
                            ServerLevel lvl = sp.serverLevel();
                            sp.teleportTo(lvl, home[0] + 0.5, home[1] + 0.1, home[2] + 0.5, sp.getYRot(), sp.getXRot());
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.home.tp.success"), false);
                            return 1;
                        })))
                // warp add/del/list and direct tp: /f warp <name>
                .then(Commands.literal("warp")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(FactionCommand::suggestWarpNames)
                                .requires(src -> hasServerPerm(src, "ef.faction.warp.tp"))
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    Faction f = FactionManager.getFactionOf(sp.getUUID());
                                    if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                                    String nm = StringArgumentType.getString(ctx, "name");
                                    var w = f.getWarps().get(nm.toLowerCase(java.util.Locale.ROOT));
                                    if (w == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.warp.tp.not_found")); return 0; }
                                    ServerLevel lvl = sp.serverLevel();
                                    sp.teleportTo(lvl, w.x + 0.5, w.y + 0.1, w.z + 0.5, sp.getYRot(), sp.getXRot());
                                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.tp.success", nm), false);
                                    return 1;
                                }))
                        .then(Commands.literal("add").requires(src -> hasServerPerm(src, "ef.faction.warp.add")).then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                            String nm = StringArgumentType.getString(ctx, "name");
                            var pos = sp.blockPosition();
                            var dim = sp.level().dimension().location();
                            boolean ok = f.addWarp(nm, pos.getX(), pos.getY(), pos.getZ(), dim);
                            if (!ok) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.warp.add.fail")); return 0; }
                            FactionManager.markDirty();
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.add.success", nm), true);
                            return 1;
                        })))
                        .then(Commands.literal("del").requires(src -> hasServerPerm(src, "ef.faction.warp.del")).then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestWarpNames).executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                            String nm = StringArgumentType.getString(ctx, "name");
                            boolean ok = f.removeWarp(nm);
                            if (!ok) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.warp.del.fail")); return 0; }
                            FactionManager.markDirty();
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.del.success", nm), true);
                            return 1;
                        })))
                        .then(Commands.literal("list").executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            Faction f = FactionManager.getFactionOf(sp.getUUID());
                            if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                            int size = f.getWarps().size();
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.list.header", size, f.getMaxWarps()), false);
                            for (var e : f.getWarps().entrySet()) {
                                var w = e.getValue();
                                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.list.item", e.getKey(), w.x, w.y, w.z), false);
                            }
                            return 1;
                        })))
                // end buildRoot
                ;
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

    private static CompletableFuture<Suggestions> suggestWarpNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f != null) {
                for (String nm : f.getWarps().keySet()) builder.suggest(nm);
            }
        } catch (Exception ignored) {}
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
