package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.eriniumgroup.erinium_faction.features.antixray.AntiXrayConfig;
import fr.eriniumgroup.erinium_faction.features.antixray.AntiXrayEngine;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class AntiXrayCommand {

    private static AntiXrayEngine engine;

    public static void init(AntiXrayEngine instance) {
        engine = instance;
    }

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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("antixray").requires(s -> hasServerPerm(s, "ef.antixray.admin")).then(Commands.literal("enable").executes(ctx -> setEnabled(ctx.getSource(), true))).then(Commands.literal("disable").executes(ctx -> setEnabled(ctx.getSource(), false))).then(Commands.literal("mode").then(Commands.argument("mode", StringArgumentType.word()).suggests((c, b) -> {
            for (AntiXrayConfig.AntiXrayMode m : AntiXrayConfig.AntiXrayMode.values())
                b.suggest(m.name());
            return b.buildFuture();
        }).executes(ctx -> setMode(ctx.getSource(), StringArgumentType.getString(ctx, "mode"))))).then(Commands.literal("setradius").then(Commands.argument("radius", IntegerArgumentType.integer(1, 8)).executes(ctx -> {
            engine.getConfig().setUpdateRadius(IntegerArgumentType.getInteger(ctx, "radius"));
            ctx.getSource().sendSuccess(() -> Component.literal("Rayon= " + engine.getConfig().getUpdateRadius()), true);
            return 1;
        }))).then(Commands.literal("setrate").then(Commands.argument("rate", IntegerArgumentType.integer(0, 100)).executes(ctx -> {
            engine.getConfig().setEngineMode2ChunkRate(IntegerArgumentType.getInteger(ctx, "rate"));
            ctx.getSource().sendSuccess(() -> Component.literal("Taux faux minerais= " + engine.getConfig().getEngineMode2ChunkRate() + "%"), true);
            return 1;
        }))).then(Commands.literal("addore").then(Commands.argument("id", StringArgumentType.string()).executes(ctx -> {
            engine.getConfig().addHiddenBlock(StringArgumentType.getString(ctx, "id"));
            ctx.getSource().sendSuccess(() -> Component.literal("Ajouté: " + StringArgumentType.getString(ctx, "id")), true);
            return 1;
        }))).then(Commands.literal("removeore").then(Commands.argument("id", StringArgumentType.string()).executes(ctx -> {
            boolean r = engine.getConfig().removeHiddenBlock(StringArgumentType.getString(ctx, "id"));
            ctx.getSource().sendSuccess(() -> Component.literal((r ? "Retiré" : "Introuvable") + ": " + StringArgumentType.getString(ctx, "id")), true);
            return r ? 1 : 0;
        }))).then(Commands.literal("clearores").executes(ctx -> {
            engine.getConfig().clearHiddenBlocks();
            ctx.getSource().sendSuccess(() -> Component.literal("Liste minerais vidée"), true);
            return 1;
        })).then(Commands.literal("addrepl").then(Commands.argument("id", StringArgumentType.string()).executes(ctx -> {
            engine.getConfig().addReplacementBlock(StringArgumentType.getString(ctx, "id"));
            ctx.getSource().sendSuccess(() -> Component.literal("Ajouté repl: " + StringArgumentType.getString(ctx, "id")), true);
            return 1;
        }))).then(Commands.literal("remrepl").then(Commands.argument("id", StringArgumentType.string()).executes(ctx -> {
            boolean r = engine.getConfig().removeReplacementBlock(StringArgumentType.getString(ctx, "id"));
            ctx.getSource().sendSuccess(() -> Component.literal((r ? "Retiré" : "Introuvable") + ": " + StringArgumentType.getString(ctx, "id")), true);
            return r ? 1 : 0;
        }))).then(Commands.literal("clearrepl").executes(ctx -> {
            engine.getConfig().clearReplacementBlocks();
            ctx.getSource().sendSuccess(() -> Component.literal("Liste remplacements vidée"), true);
            return 1;
        })).then(Commands.literal("setspoofradius").then(Commands.argument("radius", IntegerArgumentType.integer(1, 64)).executes(ctx -> {
            engine.getConfig().setSpoofRadius(IntegerArgumentType.getInteger(ctx, "radius"));
            ctx.getSource().sendSuccess(() -> Component.literal("Spoof radius= " + engine.getConfig().getSpoofRadius()), true);
            return 1;
        }))).then(Commands.literal("setspoofmax").then(Commands.argument("max", IntegerArgumentType.integer(0, 5000)).executes(ctx -> {
            engine.getConfig().setSpoofMaxCount(IntegerArgumentType.getInteger(ctx, "max"));
            ctx.getSource().sendSuccess(() -> Component.literal("Spoof max= " + engine.getConfig().getSpoofMaxCount()), true);
            return 1;
        }))).then(Commands.literal("setspoofbudget").then(Commands.argument("budget", IntegerArgumentType.integer(0, 1000)).executes(ctx -> {
            engine.getConfig().setSpoofBudgetPerTick(IntegerArgumentType.getInteger(ctx, "budget"));
            ctx.getSource().sendSuccess(() -> Component.literal("Spoof budget/tick= " + engine.getConfig().getSpoofBudgetPerTick()), true);
            return 1;
        }))).then(Commands.literal("setcoverage").then(Commands.argument("percent", IntegerArgumentType.integer(0, 100)).executes(ctx -> {
            engine.getConfig().setSpoofTargetCoverage(IntegerArgumentType.getInteger(ctx, "percent"));
            ctx.getSource().sendSuccess(() -> Component.literal("Spoof coverage= " + engine.getConfig().getSpoofTargetCoverage() + "%"), true);
            return 1;
        }))).then(Commands.literal("hell").executes(ctx -> {
            engine.getConfig().setMode(AntiXrayConfig.AntiXrayMode.HELL);
            ctx.getSource().sendSuccess(() -> Component.literal("Mode= HELL"), true);
            return 1;
        })).then(Commands.literal("info").executes(ctx -> {
            var c = engine.getConfig();
            ctx.getSource().sendSuccess(() -> Component.literal("enabled=" + c.isEnabled() + ", mode=" + c.getMode() + ", radius=" + c.getUpdateRadius() + ", rate=" + c.getEngineMode2ChunkRate() + ", spoofRadius=" + c.getSpoofRadius() + ", spoofMax=" + c.getSpoofMaxCount() + ", spoofBudget=" + c.getSpoofBudgetPerTick()), false);
            return 1;
        })).then(Commands.literal("preset").then(Commands.literal("hell").executes(ctx -> {
            var c = engine.getConfig();
            c.setMode(AntiXrayConfig.AntiXrayMode.HELL);
            c.setSpoofRadius(Math.max(c.getSpoofRadius(), 24));
            c.setSpoofTargetCoverage(Math.max(c.getSpoofTargetCoverage(), 85));
            c.setSpoofMaxCount(Math.max(c.getSpoofMaxCount(), 2500));
            c.setSpoofBudgetPerTick(Math.max(c.getSpoofBudgetPerTick(), 160));
            c.setEngineMode2ChunkRate(Math.max(c.getEngineMode2ChunkRate(), 15));
            ctx.getSource().sendSuccess(() -> Component.literal("Preset HELL appliqué."), true);
            return 1;
        })).then(Commands.literal("perf").executes(ctx -> {
            var c = engine.getConfig();
            c.setMode(AntiXrayConfig.AntiXrayMode.ENGINE_MODE_2);
            c.setSpoofRadius(12);
            c.setSpoofTargetCoverage(35);
            c.setSpoofMaxCount(600);
            c.setSpoofBudgetPerTick(60);
            c.setEngineMode2ChunkRate(8);
            ctx.getSource().sendSuccess(() -> Component.literal("Preset PERF appliqué."), true);
            return 1;
        }))));
    }

    private static int setEnabled(CommandSourceStack src, boolean enabled) {
        engine.getConfig().setEnabled(enabled);
        src.sendSuccess(() -> Component.literal("AntiXray: " + (enabled ? "ON" : "OFF")), true);
        return 1;
    }

    private static int setMode(CommandSourceStack src, String mode) {
        try {
            engine.getConfig().setMode(AntiXrayConfig.AntiXrayMode.valueOf(mode.toUpperCase()));
            src.sendSuccess(() -> Component.literal("Mode= " + engine.getConfig().getMode()), true);
            return 1;
        } catch (IllegalArgumentException e) {
            src.sendFailure(Component.literal("Mode invalide"));
            return 0;
        }
    }
}
