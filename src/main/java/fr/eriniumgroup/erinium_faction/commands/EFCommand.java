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
        d.register(Commands.literal("ef").requires(src -> {
            if (src.hasPermission(2)) return true; // OP
            try {
                ServerPlayer sp = src.getPlayer();
                if (sp == null) return true; // console
                return fr.eriniumgroup.erinium_faction.core.permissions.EFPerms.has(sp, "ef.admin");
            } catch (Exception e) {
                return false;
            }
        })
                // Gestion des permissions joueurs
                .then(Commands.literal("perm").then(Commands.literal("list").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).executes(ctx -> doList(ctx)))).then(Commands.literal("add").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("permission", StringArgumentType.greedyString()).suggests(EFCommand::suggestKnownPermissions).executes(EFCommand::doAdd)))).then(Commands.literal("remove").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("permission", StringArgumentType.greedyString()).suggests(EFCommand::suggestPlayerPermissions).executes(EFCommand::doRemove)))))
                // Suppression de faction
                .then(Commands.literal("delete").then(Commands.argument("name", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).executes(EFCommand::doDelete)))
                // Ajout d'XP à une faction
                .then(Commands.literal("addxp").then(Commands.argument("name", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("amount", IntegerArgumentType.integer(1)).suggests(EFCommand::suggestXpAmounts).executes(EFCommand::doAddXp))))
                // Changement de rang
                .then(Commands.literal("setrank").then(Commands.argument("player", StringArgumentType.word()).suggests(EFCommand::suggestOnlinePlayers).then(Commands.argument("rankId", StringArgumentType.word()).suggests(EFCommand::suggestServerRanks).executes(EFCommand::doSetRank))))
                // Changement de mode de faction
                .then(Commands.literal("mode").then(Commands.argument("factionName", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("value", StringArgumentType.word()).suggests((ctx, b) -> {
                    b.suggest("PUBLIC");
                    b.suggest("INVITE_ONLY");
                    return b.buildFuture();
                }).executes(EFCommand::doMode))))
                // Modification des flags de faction
                .then(Commands.literal("flag").then(Commands.argument("factionName", StringArgumentType.word()).suggests(EFCommand::suggestFactionNames).then(Commands.argument("flagName", StringArgumentType.word()).suggests((c, b) -> {
                    b.suggest("admin");
                    b.suggest("warzone");
                    b.suggest("safezone");
                    return b.buildFuture();
                }).then(Commands.argument("value", StringArgumentType.word()).suggests((c, b) -> {
                    b.suggest("on");
                    b.suggest("off");
                    return b.buildFuture();
                }).executes(EFCommand::doFlag)))))
                // Gestion des permissions de claim
                // Appliquer les permissions par défaut aux rangs de faction
                .then(Commands.literal("applydefaults").executes(EFCommand::doApplyDefaults))
                // Gestion des permissions de claim
                .then(Commands.literal("claimperm").then(Commands.literal("list").then(Commands.argument("dimension", StringArgumentType.word()).then(Commands.argument("cx", IntegerArgumentType.integer()).then(Commands.argument("cz", IntegerArgumentType.integer()).executes(ctx -> {
                    String dim = StringArgumentType.getString(ctx, "dimension");
                    int cx = IntegerArgumentType.getInteger(ctx, "cx");
                    int cz = IntegerArgumentType.getInteger(ctx, "cz");
                    ClaimKey key = new ClaimKey(dim, cx, cz);
                    var perms = FactionManager.getClaimPerms(key);
                    if (perms.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> Component.literal("<vide>"), false);
                        return 1;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("Permissions du claim:"), false);
                    for (var e : perms.entrySet())
                        ctx.getSource().sendSuccess(() -> Component.literal(" - " + e.getKey() + ": " + String.join(", ", e.getValue())), false);
                    return 1;
                }))))).then(Commands.literal("add").then(Commands.argument("dimension", StringArgumentType.word()).then(Commands.argument("cx", IntegerArgumentType.integer()).then(Commands.argument("cz", IntegerArgumentType.integer()).then(Commands.argument("rank", StringArgumentType.word()).then(Commands.argument("perm", StringArgumentType.greedyString()).executes(ctx -> {
                    String dim = StringArgumentType.getString(ctx, "dimension");
                    int cx = IntegerArgumentType.getInteger(ctx, "cx");
                    int cz = IntegerArgumentType.getInteger(ctx, "cz");
                    ClaimKey key = new ClaimKey(dim, cx, cz);
                    String rank = StringArgumentType.getString(ctx, "rank").toLowerCase(java.util.Locale.ROOT);
                    String perm = StringArgumentType.getString(ctx, "perm");
                    boolean ok = FactionManager.addClaimPerm(key, rank, perm);
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.literal("Permission déjà présente ou invalide."));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("Ajoutée pour " + rank + ": " + perm), true);
                    return 1;
                }))))))).then(Commands.literal("remove").then(Commands.argument("dimension", StringArgumentType.word()).then(Commands.argument("cx", IntegerArgumentType.integer()).then(Commands.argument("cz", IntegerArgumentType.integer()).then(Commands.argument("rank", StringArgumentType.word()).then(Commands.argument("perm", StringArgumentType.greedyString()).executes(ctx -> {
                    String dim = StringArgumentType.getString(ctx, "dimension");
                    int cx = IntegerArgumentType.getInteger(ctx, "cx");
                    int cz = IntegerArgumentType.getInteger(ctx, "cz");
                    ClaimKey key = new ClaimKey(dim, cx, cz);
                    String rank = StringArgumentType.getString(ctx, "rank").toLowerCase(java.util.Locale.ROOT);
                    String perm = StringArgumentType.getString(ctx, "perm");
                    boolean ok = FactionManager.removeClaimPerm(key, rank, perm);
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.literal("Permission absente ou invalide."));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("Retirée pour " + rank + ": " + perm), true);
                    return 1;
                }))))))).then(Commands.literal("clear").then(Commands.argument("dimension", StringArgumentType.word()).then(Commands.argument("cx", IntegerArgumentType.integer()).then(Commands.argument("cz", IntegerArgumentType.integer()).executes(ctx -> {
                    String dim = StringArgumentType.getString(ctx, "dimension");
                    int cx = IntegerArgumentType.getInteger(ctx, "cx");
                    int cz = IntegerArgumentType.getInteger(ctx, "cz");
                    ClaimKey key = new ClaimKey(dim, cx, cz);
                    boolean ok = FactionManager.clearClaimPerms(key);
                    if (!ok) {
                        ctx.getSource().sendFailure(Component.literal("Rien à effacer."));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("Permissions du claim effacées."), true);
                    return 1;
                })))))));
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
        String playerName = StringArgumentType.getString(ctx, "player");
        String rankId = StringArgumentType.getString(ctx, "rankId");

        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
            return 0;
        }

        // Vérifier que le rank existe
        EFRManager.Rank rank = EFRManager.get().getRank(rankId);
        if (rank == null) {
            ctx.getSource().sendFailure(Component.literal("§cLe rang '" + rankId + "' n'existe pas."));
            return 0;
        }

        // Attribuer le rank au joueur
        boolean success = EFRManager.get().setPlayerRank(target.getUUID(), rankId);
        if (!success) {
            ctx.getSource().sendFailure(Component.literal("§cImpossible d'attribuer le rang."));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§aRang '" + rank.displayName + "§a' attribué à " + target.getGameProfile().getName()), true);
        target.sendSystemMessage(Component.literal("§aVotre rang a été changé en " + rank.displayName));

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
            ctx.getSource().sendSuccess(() -> Component.literal("§aAppliqué " + finalTotal + " permissions par défaut à " + finalFactions + " faction(s)"), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§eAucune permission à ajouter"), false);
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
}
