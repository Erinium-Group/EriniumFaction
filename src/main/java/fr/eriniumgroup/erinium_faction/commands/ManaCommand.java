package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.features.mana.ManaAttachments;
import fr.eriniumgroup.erinium_faction.features.mana.PlayerManaData;
import fr.eriniumgroup.erinium_faction.features.mana.spell.SpellRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class ManaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        var root = Commands.literal("mana").requires(src -> src.hasPermission(2)); // admin for set/add/learn/forget

        // /mana info [player]
        root.then(Commands.literal("info").then(Commands.argument("player", StringArgumentType.word()).suggests(ManaCommand::suggestOnlinePlayers).executes(ctx -> doInfo(ctx.getSource(), StringArgumentType.getString(ctx, "player")))).executes(ctx -> doInfo(ctx.getSource(), null)));
        // /mana set <player> <value>
        root.then(Commands.literal("set").then(Commands.argument("player", StringArgumentType.word()).suggests(ManaCommand::suggestOnlinePlayers).then(Commands.argument("value", DoubleArgumentType.doubleArg(0)).executes(ctx -> doSet(ctx.getSource(), StringArgumentType.getString(ctx, "player"), DoubleArgumentType.getDouble(ctx, "value"))))));
        // /mana add <player> <value>
        root.then(Commands.literal("add").then(Commands.argument("player", StringArgumentType.word()).suggests(ManaCommand::suggestOnlinePlayers).then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes(ctx -> doAdd(ctx.getSource(), StringArgumentType.getString(ctx, "player"), DoubleArgumentType.getDouble(ctx, "value"))))));
        // /mana spend <player> <value>
        root.then(Commands.literal("spend").then(Commands.argument("player", StringArgumentType.word()).suggests(ManaCommand::suggestOnlinePlayers).then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes(ctx -> doSpend(ctx.getSource(), StringArgumentType.getString(ctx, "player"), DoubleArgumentType.getDouble(ctx, "value"))))));
        // /mana learn <player> <spell_id>
        root.then(Commands.literal("learn").then(Commands.argument("player", StringArgumentType.word()).suggests(ManaCommand::suggestOnlinePlayers).then(Commands.argument("spell", StringArgumentType.string()).suggests(ManaCommand::suggestSpellIds).executes(ctx -> doLearn(ctx.getSource(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "spell"))))));
        // /mana forget <player> <spell_id>
        root.then(Commands.literal("forget").then(Commands.argument("player", StringArgumentType.word()).suggests(ManaCommand::suggestOnlinePlayers).then(Commands.argument("spell", StringArgumentType.string()).suggests(ManaCommand::suggestLearnedSpellIdsForPlayer).executes(ctx -> doForget(ctx.getSource(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "spell"))))));
        // /mana cast <spell_id>
        root.then(Commands.literal("cast").then(Commands.argument("spell", StringArgumentType.string()).suggests(ManaCommand::suggestSpellIds).executes(ctx -> doCast(ctx.getSource(), StringArgumentType.getString(ctx, "spell")))));

        d.register(root);
    }

    private static int doInfo(CommandSourceStack src, String name) {
        ServerPlayer target = name == null ? src.getPlayer() : src.getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            src.sendFailure(Component.translatable("erinium_faction.common.player_not_found"));
            return 0;
        }
        PlayerManaData md = target.getData(ManaAttachments.PLAYER_MANA);
        double max = md.computeMaxMana(target);
        src.sendSuccess(() -> Component.literal(String.format("Mana: %.1f / %.1f", md.getMana(), max)), false);
        if (!md.getKnownSpells().isEmpty()) {
            src.sendSuccess(() -> Component.literal("Spells: " + String.join(", ", md.getKnownSpells())), false);
        } else {
            src.sendSuccess(() -> Component.literal("Spells: <none>"), false);
        }
        return 1;
    }

    private static int doSet(CommandSourceStack src, String name, double value) {
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(name);
        if (target == null) { src.sendFailure(Component.translatable("erinium_faction.common.player_not_found")); return 0; }
        PlayerManaData md = target.getData(ManaAttachments.PLAYER_MANA);
        double max = md.computeMaxMana(target);
        md.setMana(value, max);
        src.sendSuccess(() -> Component.literal(String.format("Set mana of %s to %.1f / %.1f", target.getName().getString(), md.getMana(), max)), true);
        return 1;
    }

    private static int doAdd(CommandSourceStack src, String name, double value) {
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(name);
        if (target == null) { src.sendFailure(Component.translatable("erinium_faction.common.player_not_found")); return 0; }
        PlayerManaData md = target.getData(ManaAttachments.PLAYER_MANA);
        double max = md.computeMaxMana(target);
        md.addMana(value, max);
        src.sendSuccess(() -> Component.literal(String.format("Added %.1f mana to %s (%.1f / %.1f)", value, target.getName().getString(), md.getMana(), max)), true);
        return 1;
    }

    private static int doSpend(CommandSourceStack src, String name, double value) {
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(name);
        if (target == null) { src.sendFailure(Component.translatable("erinium_faction.common.player_not_found")); return 0; }
        PlayerManaData md = target.getData(ManaAttachments.PLAYER_MANA);
        double max = md.computeMaxMana(target);
        if (md.getMana() < value) {
            src.sendFailure(Component.translatable("erinium_faction.mana.not_enough"));
            return 0;
        }
        md.setMana(md.getMana() - value, max);
        src.sendSuccess(() -> Component.literal(String.format("Spent %.1f mana from %s (%.1f / %.1f)", value, target.getName().getString(), md.getMana(), max)), true);
        return 1;
    }

    private static ResourceLocation resolveSpell(String raw) {
        String id = raw.trim();
        if (id.startsWith("/")) id = id.substring(1); // autoriser un slash initial accidentel
        // Si l’utilisateur fournit erinium_faction:fire/fireball on accepte tel quel
        if (id.contains(":")) {
            var parsed = ResourceLocation.tryParse(id);
            if (parsed != null) return parsed;
        }
        // par défaut namespace erinium_faction
        return ResourceLocation.fromNamespaceAndPath("erinium_faction", id);
    }

    private static int doLearn(CommandSourceStack src, String name, String spellId) {
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(name);
        if (target == null) { src.sendFailure(Component.translatable("erinium_faction.common.player_not_found")); return 0; }
        var rl = resolveSpell(spellId);
        var spell = SpellRegistry.get(rl);
        if (spell == null) { src.sendFailure(Component.translatable("erinium_faction.spell.unknown", rl.toString())); return 0; }
        PlayerManaData md = target.getData(ManaAttachments.PLAYER_MANA);
        md.learnSpell(rl.toString());
        src.sendSuccess(() -> Component.translatable("erinium_faction.spell.learned", rl.toString()), true);
        return 1;
    }

    private static int doForget(CommandSourceStack src, String name, String spellId) {
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(name);
        if (target == null) { src.sendFailure(Component.translatable("erinium_faction.common.player_not_found")); return 0; }
        var rl = resolveSpell(spellId);
        PlayerManaData md = target.getData(ManaAttachments.PLAYER_MANA);
        md.forgetSpell(rl.toString());
        src.sendSuccess(() -> Component.translatable("erinium_faction.spell.forgot", rl.getPath()), true);
        return 1;
    }

    private static int doCast(CommandSourceStack src, String spellId) {
        ServerPlayer player = src.getPlayer();
        if (player == null) return 0;
        var rl = resolveSpell(spellId);
        boolean ok = fr.eriniumgroup.erinium_faction.features.mana.spell.SpellCasting.cast(player, rl);
        if (ok) {
            src.sendSuccess(() -> Component.literal("Cast spell: " + rl), true);
            return 1;
        } else {
            src.sendFailure(Component.translatable("erinium_faction.spell.unknown", rl.toString()));
            return 0;
        }
    }

    private static java.util.concurrent.CompletableFuture<Suggestions> suggestSpellIds(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        try {
            for (var spell : SpellRegistry.all()) {
                if ("erinium_faction".equals(spell.id.getNamespace())) {
                    b.suggest(spell.id.getPath());
                }
            }
        } catch (Exception ignored) {}
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        try {
            var server = ctx.getSource().getServer();
            if (server != null) {
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    b.suggest(p.getGameProfile().getName());
                }
            }
        } catch (Exception ignored) {}
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestLearnedSpellIdsForPlayer(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        try {
            String playerName = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "player");
            var server = ctx.getSource().getServer();
            if (server == null) return b.buildFuture();
            ServerPlayer target = server.getPlayerList().getPlayerByName(playerName);
            if (target == null) return b.buildFuture();
            PlayerManaData md = target.getData(ManaAttachments.PLAYER_MANA);
            for (String s : md.getKnownSpells()) {
                var rl = net.minecraft.resources.ResourceLocation.tryParse(s);
                if (rl != null) {
                    if ("erinium_faction".equals(rl.getNamespace())) b.suggest(rl.getPath()); else b.suggest(rl.toString());
                } else {
                    b.suggest(s);
                }
            }
        } catch (Exception ignored) {}
        return b.buildFuture();
    }
}
