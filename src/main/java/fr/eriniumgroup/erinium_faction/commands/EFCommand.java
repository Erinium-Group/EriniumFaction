package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.Faction.Mode;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import fr.eriniumgroup.erinium_faction.features.homes.HomesConfig;
import fr.eriniumgroup.erinium_faction.features.homes.HomesManager;
import fr.eriniumgroup.erinium_faction.features.homes.PlayerHomesData;
import fr.eriniumgroup.erinium_faction.common.network.packets.OpenAuditViewerPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * /ef perm <add|remove|list> <player> [permission]
 * <p>
 * Gestion simple d'overrides de permissions au niveau joueur.
 */
public class EFCommand {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        var root = Commands.literal("ef").requires(src -> {
            if (src.hasPermission(2)) return true; // OP
            try {
                ServerPlayer sp = src.getPlayer();
                if (sp == null) return true; // console
                return fr.eriniumgroup.erinium_faction.core.permissions.EFPerms.has(sp, "ef.admin");
            } catch (Exception e) {
                return false;
            }
        });

        // /ef perm ...
        root.then(Commands.literal("perm").then(Commands.literal("list").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).executes(EFCommand::doList))).then(Commands.literal("add").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("permission", StringArgumentType.greedyString()).suggests(EFCommand::suggestKnownPermissions).executes(EFCommand::doAdd)))).then(Commands.literal("remove").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("permission", StringArgumentType.greedyString()).suggests(EFCommand::suggestPlayerPermissions).executes(EFCommand::doRemove)))));

        // /ef delete <name>
        root.then(Commands.literal("delete").then(Commands.argument("name", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).executes(EFCommand::doDelete)));

        // /ef addxp <name> <amount>
        root.then(Commands.literal("addxp").then(Commands.argument("name", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("amount", IntegerArgumentType.integer(1)).suggests(EFCommand::suggestXpAmounts).executes(EFCommand::doAddXp))));

        // /ef setrank <player> <rankId>
        root.then(Commands.literal("setrank").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("rankId", StringArgumentType.word()).suggests(EFCommand::suggestServerRanks).executes(EFCommand::doSetRank))));

        // /ef mode <factionName> <PUBLIC|INVITE_ONLY>
        root.then(Commands.literal("mode").then(Commands.argument("factionName", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("value", StringArgumentType.word()).suggests((ctx, b) -> {
            b.suggest("PUBLIC");
            b.suggest("INVITE_ONLY");
            return b.buildFuture();
        }).executes(EFCommand::doMode))));

        // /ef flag <factionName> <admin|warzone|safezone> <on|off>
        root.then(Commands.literal("flag").then(Commands.argument("factionName", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("flagName", StringArgumentType.word()).suggests((ctx, b) -> {
            b.suggest("admin");
            b.suggest("warzone");
            b.suggest("safezone");
            return b.buildFuture();
        }).then(Commands.argument("value", StringArgumentType.word()).suggests((ctx, b) -> {
            b.suggest("on");
            b.suggest("off");
            return b.buildFuture();
        }).executes(EFCommand::doFlag)))));

        // /ef applydefaults
        root.then(Commands.literal("applydefaults").executes(EFCommand::doApplyDefaults));

        // /ef claimperm ...
        root.then(Commands.literal("claimperm").then(Commands.literal("list").then(Commands.argument("dimension", StringArgumentType.word()).then(Commands.argument("cx", IntegerArgumentType.integer()).then(Commands.argument("cz", IntegerArgumentType.integer()).executes(ctx -> {
            String dim = StringArgumentType.getString(ctx, "dimension");
            int cx = IntegerArgumentType.getInteger(ctx, "cx");
            int cz = IntegerArgumentType.getInteger(ctx, "cz");
            ClaimKey key = new ClaimKey(dim, cx, cz);
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
        })))).then(Commands.literal("add").then(Commands.argument("dimension", StringArgumentType.word()).then(Commands.argument("cx", IntegerArgumentType.integer()).then(Commands.argument("cz", IntegerArgumentType.integer()).then(Commands.argument("rank", StringArgumentType.word()).then(Commands.argument("perm", StringArgumentType.greedyString()).executes(ctx -> {
            String dim = StringArgumentType.getString(ctx, "dimension");
            int cx = IntegerArgumentType.getInteger(ctx, "cx");
            int cz = IntegerArgumentType.getInteger(ctx, "cz");
            ClaimKey key = new ClaimKey(dim, cx, cz);
            String rank = StringArgumentType.getString(ctx, "rank").toLowerCase(java.util.Locale.ROOT);
            String perm = StringArgumentType.getString(ctx, "perm");
            boolean ok = FactionManager.addClaimPerm(key, rank, perm);
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claimperm.add.fail"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.add.success", rank, perm), true);
            return 1;
        })))))).then(Commands.literal("remove").then(Commands.argument("dimension", StringArgumentType.word()).then(Commands.argument("cx", IntegerArgumentType.integer()).then(Commands.argument("cz", IntegerArgumentType.integer()).then(Commands.argument("rank", StringArgumentType.word()).then(Commands.argument("perm", StringArgumentType.greedyString()).executes(ctx -> {
            String dim = StringArgumentType.getString(ctx, "dimension");
            int cx = IntegerArgumentType.getInteger(ctx, "cx");
            int cz = IntegerArgumentType.getInteger(ctx, "cz");
            ClaimKey key = new ClaimKey(dim, cx, cz);
            String rank = StringArgumentType.getString(ctx, "rank").toLowerCase(java.util.Locale.ROOT);
            String perm = StringArgumentType.getString(ctx, "perm");
            boolean ok = FactionManager.removeClaimPerm(key, rank, perm);
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claimperm.remove.fail"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.remove.success", rank, perm), true);
            return 1;
        })))))).then(Commands.literal("clear").then(Commands.argument("dimension", StringArgumentType.word()).then(Commands.argument("cx", IntegerArgumentType.integer()).then(Commands.argument("cz", IntegerArgumentType.integer()).executes(ctx -> {
            String dim = StringArgumentType.getString(ctx, "dimension");
            int cx = IntegerArgumentType.getInteger(ctx, "cx");
            int cz = IntegerArgumentType.getInteger(ctx, "cz");
            ClaimKey key = new ClaimKey(dim, cx, cz);
            boolean ok = FactionManager.clearClaimPerms(key);
            if (!ok) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claimperm.clear.empty"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claimperm.clear.success"), true);
            return 1;
        })))))))));

        // /ef homes ... (admin homes management)
        root.then(Commands.literal("homes").then(Commands.literal("config").then(Commands.literal("max").then(Commands.argument("value", IntegerArgumentType.integer(1, 100)).executes(EFCommand::doSetMaxHomes))).then(Commands.literal("crossdim").then(Commands.argument("enabled", StringArgumentType.word()).suggests((ctx, b) -> {
            b.suggest("true");
            b.suggest("false");
            return b.buildFuture();
        }).executes(EFCommand::doSetCrossDimTeleport))).then(Commands.literal("warmup").then(Commands.argument("seconds", IntegerArgumentType.integer(0, 60)).executes(ctx -> {
            int s = IntegerArgumentType.getInteger(ctx, "seconds");
            HomesConfig.get(ctx.getSource().getServer()).setWarmupSeconds(s);
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.config.warmup.set", s), true);
            return 1;
        }))).then(Commands.literal("cooldown").then(Commands.argument("seconds", IntegerArgumentType.integer(0, 3600)).executes(ctx -> {
            int s = IntegerArgumentType.getInteger(ctx, "seconds");
            HomesConfig.get(ctx.getSource().getServer()).setCooldownSeconds(s);
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.config.cooldown.set", s), true);
            return 1;
        }))).then(Commands.literal("list").executes(EFCommand::doListHomesConfig))).then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).executes(EFCommand::doListPlayerHomes)));

        // /ef home <player> <homeName>
        root.then(Commands.literal("home").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("homeName", StringArgumentType.word()).suggests(EFCommand::suggestPlayerHomes).executes(EFCommand::doTeleportPlayerHome))));

        // Ajout: /ef audit gui
        root.then(Commands.literal("audit").then(Commands.literal("gui").executes(ctx -> {
            ServerPlayer sender;
            try { sender = ctx.getSource().getPlayer(); } catch (Exception e) { sender = null; }
            if (sender == null) {
                ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
                return 0;
            }
            PacketDistributor.sendToPlayer(sender, new OpenAuditViewerPacket());
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.audit.gui.open"), false);
            return 1;
        })));

        d.register(root);
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
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.perm.overrides.none", target.getGameProfile().getName()), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.perm.overrides.header", target.getGameProfile().getName()), false);
            for (String p : list)
                ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.perm.overrides.item", p), false);
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
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.perm.already_present"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.perm.added", target.getGameProfile().getName()), true);
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
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.perm.not_found"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.perm.removed", target.getGameProfile().getName()), true);
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
        String playerName = StringArgumentType.getString(ctx, "player");
        String rankId = StringArgumentType.getString(ctx, "rankId");

        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
            return 0;
        }
        EFRManager.Rank rank = EFRManager.get().getRank(rankId);
        if (rank == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.rank.not_found", rankId));
            return 0;
        }
        boolean success = EFRManager.get().setPlayerRank(target.getUUID(), rankId);
        if (!success) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.rank.assign.fail", rank.displayName));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.rank.assign.success", rank.displayName, target.getGameProfile().getName()), true);
        target.sendSystemMessage(Component.translatable("erinium_faction.cmd.rank.changed.player", rank.displayName));
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

    private static int doApplyDefaults(CommandContext<CommandSourceStack> ctx) {
        var data = fr.eriniumgroup.erinium_faction.core.faction.RankDefaultsSavedData.get(ctx.getSource().getServer());
        data.ensureDefaultsInitialized();

        int totalChanges = 0;
        int factionsUpdated = 0;

        for (Faction f : FactionManager.getAllFactions()) {
            int changes = applyDefaultsToFaction(f, data);
            if (changes > 0) {
                totalChanges += changes;
                factionsUpdated++;
            }
        }

        if (totalChanges > 0) {
            FactionManager.markDirty();
            final int finalTotal = totalChanges;
            final int finalFactions = factionsUpdated;
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.ef.applydefaults.success", finalTotal, finalFactions), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.ef.applydefaults.none"), false);
        }

        return 1;
    }

    private static int applyDefaultsToFaction(Faction faction, fr.eriniumgroup.erinium_faction.core.faction.RankDefaultsSavedData defaults) {
        int changes = 0;
        for (Map.Entry<String, Faction.RankDef> e : faction.getRanks().entrySet()) {
            String rankId = e.getKey();
            Faction.RankDef rank = e.getValue();
            java.util.Set<String> defaultPerms = defaults.getDefaultsFor(rankId);

            if (!defaultPerms.isEmpty()) {
                for (String perm : defaultPerms) {
                    if (!rank.perms.contains(perm)) {
                        rank.perms.add(perm);
                        changes++;
                    }
                }
            }
        }
        return changes;
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

    // Homes management ---------------------------------------------------------------

    private static int doListPlayerHomes(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        var server = ctx.getSource().getServer();

        ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(playerName);
        if (targetPlayer == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
            return 0;
        }

        var playerHomes = HomesManager.getPlayerHomes(targetPlayer);
        if (playerHomes.isEmpty() || playerHomes.get().getAllHomes().isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.list.none", playerName), false);
            return 1;
        }

        PlayerHomesData homes = playerHomes.get();
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.list.header", playerName), false);
        homes.getAllHomes().forEach((name2, home) -> {
            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.list.entry", name2, home.getDimension(), (int) home.getX(), (int) home.getY(), (int) home.getZ()), false);
        });
        HomesConfig hc = HomesConfig.get(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.list.count", homes.getHomeCount(), homes.getMaxHomes(hc)), false);
        return 1;
    }

    private static int doTeleportPlayerHome(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        String homeName = StringArgumentType.getString(ctx, "homeName");
        var server = ctx.getSource().getServer();

        ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(playerName);
        if (targetPlayer == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
            return 0;
        }

        var home = HomesManager.getHome(targetPlayer, homeName);
        if (home.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.homes.not_found", homeName, playerName));
            return 0;
        }

        HomesManager.teleportHome(targetPlayer, homeName);
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.tp.success", playerName, homeName), true);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestPlayerHomes(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            String playerName = StringArgumentType.getString(ctx, "player");
            ServerPlayer targetPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
            if (targetPlayer != null) {
                var playerHomes = HomesManager.getPlayerHomes(targetPlayer);
                if (playerHomes.isPresent()) {
                    playerHomes.get().getAllHomes().keySet().forEach(builder::suggest);
                }
            }
        } catch (Exception ignored) {
        }
        return builder.buildFuture();
    }

    // Homes configuration ---------------------------------------------------------------

    private static int doSetMaxHomes(CommandContext<CommandSourceStack> ctx) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        HomesConfig config = HomesConfig.get(ctx.getSource().getServer());
        config.setMaxHomesPerPlayer(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.config.max.set", value), true);
        return 1;
    }

    private static int doSetCrossDimTeleport(CommandContext<CommandSourceStack> ctx) {
        String value = StringArgumentType.getString(ctx, "enabled");
        boolean enabled = Boolean.parseBoolean(value);
        HomesConfig config = HomesConfig.get(ctx.getSource().getServer());
        config.setAllowCrossDimensionTeleport(enabled);
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.config.crossdim.set", Component.translatable(enabled ? "erinium_faction.generic.enabled" : "erinium_faction.generic.disabled")), true);
        return 1;
    }

    private static int doListHomesConfig(CommandContext<CommandSourceStack> ctx) {
        HomesConfig config = HomesConfig.get(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.config.header"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.config.max", config.getMaxHomesPerPlayer()), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.homes.config.crossdim.status", Component.translatable(config.isAllowCrossDimensionTeleport() ? "erinium_faction.generic.enabled" : "erinium_faction.generic.disabled")), false);
        return 1;
    }
}

