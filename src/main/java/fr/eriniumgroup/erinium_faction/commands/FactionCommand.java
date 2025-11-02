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
import fr.eriniumgroup.erinium_faction.features.economy.EconomyIntegration;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;

import java.util.Objects;
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
        LiteralArgumentBuilder<CommandSourceStack> b = Commands.literal(root);

        b.then(Commands.literal("f").executes(ctx -> openFactionMenu(ctx.getSource().getPlayerOrException(), null)).then(Commands.argument("factionId", StringArgumentType.word()).suggests(FactionCommand::suggestFactionIds).executes(ctx -> openFactionMenu(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "factionId")))));

        b.then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestNewFactionName).executes(ctx -> {
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
        })));

        b.then(Commands.literal("disband").executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            if (!java.util.Objects.equals(f.getOwner(), sp.getUUID())) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.disband.not_leader"));
                return 0;
            }
            boolean ok = FactionManager.disbandByLeader(sp);
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.disband.fail"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.disband.success", f.getName()), true);
            return 1;
        }));

        b.then(Commands.literal("info").then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestFactionNames).executes(ctx -> {
            String name = StringArgumentType.getString(ctx, "name");
            Faction f = FactionManager.getByName(name);
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.info", f.getName(), String.format("%.0f", f.getPower()), String.format("%.0f", f.getMaxPower()), f.getLevel(), f.getXp()), false);
            return 1;
        })));

        b.then(Commands.literal("join").then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestFactionNames).executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(ctx, "name");
            Faction f = FactionManager.getByName(name);
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                return 0;
            }

            // Si la faction est ouverte (PUBLIC), on rejoint directement (vérifie capacité)
            if (f.getMode() == Faction.Mode.PUBLIC) {
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
            }

            // Si la faction est en mode invite seule
            if (f.getMode() == Faction.Mode.INVITE_ONLY) {
                // Si le joueur est invité -> accepter l'invitation
                if (FactionManager.isInvited(f, sp.getUUID())) {
                    boolean ok = FactionManager.acceptInvite(f, sp.getUUID(), sp.getGameProfile().getName());
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.join.fail"));
                        return 0;
                    }
                    FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                    sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.join.accepted", f.getName()), true);
                    return 1;
                }

                // Sinon, informer le joueur qu'il doit être invité et comment accepter l'invitation
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.join.invite_only"));
                // Optionnel: suggérer d'envoyer une demande en tchat ou contacter un membre :/ nudge
                return 0;
            }

            // Par défaut, échec
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.join.fail"));
            return 0;
        })));

        b.then(Commands.literal("leave").executes(ctx -> {
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
        }));

        b.then(Commands.literal("claim").executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            var lvl = sp.level();
            ClaimKey key = ClaimKey.of(lvl.dimension(), sp.chunkPosition().x, sp.chunkPosition().z);
            boolean ok = FactionManager.tryClaim(key, f.getId());
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claim.limit"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claim.success"), true);
            return 1;
        }));

        b.then(Commands.literal("unclaim").executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            var lvl = sp.level();
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

        b.then(Commands.literal("desc").requires(src -> hasServerPerm(src, "ef.faction.desc")).then(Commands.argument("text", StringArgumentType.greedyString()).executes(ctx -> {
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
        })));

        b.then(Commands.literal("bank").then(Commands.literal("deposit").requires(src -> hasServerPerm(src, "ef.faction.bank.deposit")).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            int a = IntegerArgumentType.getInteger(ctx, "amount");
            if (!EconomyIntegration.withdraw(sp, a)) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.bank.player_not_enough"));
                return 0;
            }
            f.deposit(a);
            FactionManager.markDirty();
            fr.eriniumgroup.erinium_faction.core.faction.FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
            sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).money = EconomyIntegration.getBalance(sp);
            sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.bank.deposit", a), true);
            return 1;
        }))).then(Commands.literal("withdraw").requires(src -> hasServerPerm(src, "ef.faction.bank.withdraw")).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
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
            EconomyIntegration.deposit(sp, a);
            FactionManager.markDirty();
            fr.eriniumgroup.erinium_faction.core.faction.FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
            sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).money = EconomyIntegration.getBalance(sp);
            sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.bank.withdraw", a), true);
            return 1;
        }))));

        b.then(Commands.literal("home").executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            if (!hasServerPerm(ctx.getSource(), "ef.faction.home.tp")) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.no_permission"));
                return 0;
            }
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            int[] home = f.getHome();
            if (home == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.home.missing"));
                return 0;
            }
            ServerLevel lvl = sp.serverLevel();
            sp.teleportTo(lvl, home[0] + 0.5, home[1] + 0.1, home[2] + 0.5, sp.getYRot(), sp.getXRot());
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.home.tp.success"), false);
            return 1;
        }).then(Commands.literal("set").requires(src -> hasServerPerm(src, "ef.faction.home.set")).executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            var pos = sp.blockPosition();
            var lvl = sp.level();
            var dim = lvl.dimension().location();
            f.setHome(pos.getX(), pos.getY(), pos.getZ(), dim);
            FactionManager.markDirty();
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.home.set", pos.getX(), pos.getY(), pos.getZ()), true);
            return 1;
        })).then(Commands.literal("tp").requires(src -> hasServerPerm(src, "ef.faction.home.tp")).executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            int[] home = f.getHome();
            if (home == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.home.missing"));
                return 0;
            }
            ServerLevel lvl = sp.serverLevel();
            sp.teleportTo(lvl, home[0] + 0.5, home[1] + 0.1, home[2] + 0.5, sp.getYRot(), sp.getXRot());
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.home.tp.success"), false);
            return 1;
        })));

        b.then(Commands.literal("warp").then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestWarpNames).requires(src -> hasServerPerm(src, "ef.faction.warp.tp")).executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            String nm = StringArgumentType.getString(ctx, "name");
            var w = f.getWarps().get(nm.toLowerCase(java.util.Locale.ROOT));
            if (w == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.warp.tp.not_found"));
                return 0;
            }
            ServerLevel lvl = sp.serverLevel();
            sp.teleportTo(lvl, w.x + 0.5, w.y + 0.1, w.z + 0.5, sp.getYRot(), sp.getXRot());
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.tp.success", nm), false);
            return 1;
        })).then(Commands.literal("add").requires(src -> hasServerPerm(src, "ef.faction.warp.add")).then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            String nm = StringArgumentType.getString(ctx, "name");
            var pos = sp.blockPosition();
            var lvl = sp.level();
            var dim = lvl.dimension().location();
            boolean ok = f.addWarp(nm, pos.getX(), pos.getY(), pos.getZ(), dim);
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.warp.add.fail"));
                return 0;
            }
            FactionManager.markDirty();
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.add.success", nm), true);
            return 1;
        }))).then(Commands.literal("del").requires(src -> hasServerPerm(src, "ef.faction.warp.del")).then(Commands.argument("name", StringArgumentType.word()).suggests(FactionCommand::suggestWarpNames).executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            String nm = StringArgumentType.getString(ctx, "name");
            boolean ok = f.removeWarp(nm);
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.warp.del.fail"));
                return 0;
            }
            FactionManager.markDirty();
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.del.success", nm), true);
            return 1;
        }))).then(Commands.literal("list").executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            int size = f.getWarps().size();
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.list.header", size, f.getMaxWarps()), false);
            for (var e : f.getWarps().entrySet()) {
                var w = e.getValue();
                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.warp.list.item", e.getKey(), w.x, w.y, w.z), false);
            }
            return 1;
        })));

        // claimperm (réécrit en clair pour éviter les erreurs de parenthésage)
        var claimpermCmd = Commands.literal("claimperm");

        claimpermCmd.then(Commands.literal("list").executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            var lvl = sp.level();
            ClaimKey key = ClaimKey.of(lvl.dimension(), sp.chunkPosition().x, sp.chunkPosition().z);
            String owner = FactionManager.getClaimOwner(key);
            if (!f.getId().equalsIgnoreCase(owner)) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.not_owner"));
                return 0;
            }
            var perms = FactionManager.getClaimPerms(key);
            if (perms.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.empty"), false);
                return 1;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.header"), false);
            for (var e : perms.entrySet()) {
                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.item", e.getKey(), String.join(", ", e.getValue())), false);
            }
            return 1;
        }));

        // builder pour set
        var setBuilder = Commands.literal("set");
        var rankArg = Commands.argument("rank", StringArgumentType.word()).suggests(FactionCommand::suggestRankIds);
        var permArg = Commands.argument("perm", StringArgumentType.word()).suggests(FactionCommand::suggestPerms);
        var valueArg = Commands.argument("value", StringArgumentType.word()).suggests(FactionCommand::suggestBooleans);

        valueArg.executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            var lvl = sp.level();
            ClaimKey key = ClaimKey.of(lvl.dimension(), sp.chunkPosition().x, sp.chunkPosition().z);
            String owner = FactionManager.getClaimOwner(key);
            if (!f.getId().equalsIgnoreCase(owner)) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.not_owner"));
                return 0;
            }

            String rank = StringArgumentType.getString(ctx, "rank").toLowerCase(java.util.Locale.ROOT);
            String perm = StringArgumentType.getString(ctx, "perm");
            String val = StringArgumentType.getString(ctx, "value");
            boolean set = val.equalsIgnoreCase("true") || val.equalsIgnoreCase("1") || val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("on");

            boolean ok;
            if (set) {
                ok = FactionManager.addClaimPerm(key, rank, perm);
                if (!ok) {
                    ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claimperm.add.fail"));
                    return 0;
                }
                FactionManager.markDirty();
                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.add.success", rank, perm), true);
            } else {
                ok = FactionManager.removeClaimPerm(key, rank, perm);
                if (!ok) {
                    ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claimperm.remove.fail"));
                    return 0;
                }
                FactionManager.markDirty();
                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.remove.success", rank, perm), true);
            }
            return 1;
        });

        // assembler les arguments
        setBuilder.then(rankArg.then(permArg.then(valueArg)));
        claimpermCmd.then(setBuilder);

        // clear
        claimpermCmd.then(Commands.literal("clear").executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            var lvl = sp.level();
            ClaimKey key = ClaimKey.of(lvl.dimension(), sp.chunkPosition().x, sp.chunkPosition().z);
            String owner = FactionManager.getClaimOwner(key);
            if (!f.getId().equalsIgnoreCase(owner)) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.not_owner"));
                return 0;
            }
            boolean ok = FactionManager.clearClaimPerms(key);
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claimperm.clear.empty"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.clear.success"), true);
            return 1;
        }));

        // finally attach to root
        b.then(claimpermCmd);

        // Invite command: /f invite <player>
        b.then(Commands.literal("invite").then(Commands.argument("player", StringArgumentType.word()).suggests(FactionCommand::suggestOnlinePlayers).executes(ctx -> {
            ServerPlayer caller = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(caller.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            // permission: either owner or rank with faction.invite
            if (!Objects.equals(f.getOwner(), caller.getUUID()) && !f.hasPermission(caller.getUUID(), "faction.invite")) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.no_permission"));
                return 0;
            }
            String targetName = StringArgumentType.getString(ctx, "player");
            ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
                return 0;
            }
            boolean ok = FactionManager.sendInvite(f, target.getUUID());
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.invite.fail", target.getGameProfile().getName()));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.invite.sent", target.getGameProfile().getName()), true);
            return 1;
        })));

        // Revoke invite: /f revokeinvite <player>
        b.then(Commands.literal("revokeinvite").then(Commands.argument("player", StringArgumentType.word()).suggests(FactionCommand::suggestOnlinePlayers).executes(ctx -> {
            ServerPlayer caller = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(caller.getUUID());
            if (f == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                return 0;
            }
            if (!Objects.equals(f.getOwner(), caller.getUUID()) && !f.hasPermission(caller.getUUID(), "faction.invite")) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.no_permission"));
                return 0;
            }
            String targetName = StringArgumentType.getString(ctx, "player");
            ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
                return 0;
            }
            boolean ok = FactionManager.revokeInvite(f, target.getUUID());
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.revoke.fail", target.getGameProfile().getName()));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.revoke.success", target.getGameProfile().getName()), true);
            return 1;
        })));


        return b;
    }

    // Suggestions -------------------------------------------------------------
    private static CompletableFuture<Suggestions> suggestFactionNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (Faction f : FactionManager.getAllFactions()) builder.suggest(f.getId());
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestFactionIds(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (Faction f : FactionManager.getAllFactions()) {
            builder.suggest(f.getId());
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestNewFactionName(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestWarpNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            Faction f = FactionManager.getFactionOf(sp.getUUID());
            if (f != null) {
                for (String nm : f.getWarps().keySet()) builder.suggest(nm);
            }
        } catch (Exception ignored) {
        }
        return builder.buildFuture();
    }

    // New suggestions for ranks / permissions / booleans
    private static CompletableFuture<Suggestions> suggestRankIds(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            for (fr.eriniumgroup.erinium_faction.core.rank.EFRManager.Rank r : fr.eriniumgroup.erinium_faction.core.rank.EFRManager.get().listRanksSorted()) {
                if (r != null && r.id != null) builder.suggest(r.id);
            }
        } catch (Exception ignored) {
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPerms(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            for (String p : fr.eriniumgroup.erinium_faction.core.rank.EFRManager.get().getKnownPermissions())
                builder.suggest(p);
        } catch (Exception ignored) {
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestBooleans(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        builder.suggest("true");
        builder.suggest("false");
        return builder.buildFuture();
    }

    // Ouverture du GUI --------------------------------------------------------
    private static int openFactionMenu(ServerPlayer sp, String factionIdOrNull) {
        Faction f = (factionIdOrNull == null || factionIdOrNull.isEmpty()) ? FactionManager.getFactionOf(sp.getUUID()) : FactionManager.getFaction(factionIdOrNull);
        if (f == null) {
            sp.sendSystemMessage(Component.translatable(factionIdOrNull == null ? "erinium_faction.cmd.faction.not_in_faction" : "erinium_faction.cmd.faction.not_found"));
            return 0;
        }
        var snapshot = FactionSnapshot.of(f, sp);
        MenuProvider provider = new SimpleMenuProvider((id, inv, player) -> new FactionMenu(id, inv, null), Component.translatable("erinium_faction.faction.menu.title"));
        sp.openMenu(provider, buf -> {
            buf.writeBlockPos(sp.blockPosition());
            buf.writeVarInt(1);
            FactionSnapshot.write(snapshot, buf);
        });

        fr.eriniumgroup.erinium_faction.common.network.packets.FactionDataPacketHandler.sendFactionDataToPlayer(sp);

        return 1;
    }

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            var players = ctx.getSource().getServer().getPlayerList().getPlayers();
            for (ServerPlayer p : players) builder.suggest(p.getGameProfile().getName());
        } catch (Exception ignored) {}
        return builder.buildFuture();
    }
}
