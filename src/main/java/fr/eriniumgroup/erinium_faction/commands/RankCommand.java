package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class RankCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("rank").requires(src -> src.hasPermission(2)).then(Commands.literal("create").then(Commands.argument("id", StringArgumentType.word()).then(Commands.argument("display", StringArgumentType.greedyString()).executes(ctx -> {
            String id = StringArgumentType.getString(ctx, "id").toLowerCase(Locale.ROOT);
            String display = StringArgumentType.getString(ctx, "display");
            boolean ok = EFRManager.get().createRank(id, display, 0);
            if (!ok) {
                ctx.getSource().sendFailure(Component.literal("Impossible de créer le rank."));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Rank créé: " + id), true);
            return 1;
        })))).then(Commands.literal("priority").then(Commands.argument("id", StringArgumentType.word()).then(Commands.argument("priority", IntegerArgumentType.integer()).executes(ctx -> {
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
        })))).then(Commands.literal("perm").then(Commands.literal("add").then(Commands.argument("id", StringArgumentType.word()).then(Commands.argument("perm", StringArgumentType.greedyString()).executes(ctx -> {
            String id = StringArgumentType.getString(ctx, "id");
            String perm = StringArgumentType.getString(ctx, "perm");
            boolean ok = EFRManager.get().addPermission(id, perm);
            if (!ok) {
                ctx.getSource().sendFailure(Component.literal("Echec ajout permission"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Permission ajoutée."), true);
            return 1;
        })))).then(Commands.literal("remove").then(Commands.argument("id", StringArgumentType.word()).then(Commands.argument("perm", StringArgumentType.greedyString()).executes(ctx -> {
            String id = StringArgumentType.getString(ctx, "id");
            String perm = StringArgumentType.getString(ctx, "perm");
            boolean ok = EFRManager.get().removePermission(id, perm);
            if (!ok) {
                ctx.getSource().sendFailure(Component.literal("Echec suppression permission"));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Permission retirée."), true);
            return 1;
        }))))));
    }
}

