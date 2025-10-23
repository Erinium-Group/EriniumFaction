package fr.eriniumgroup.eriniumfaction.command;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

import fr.eriniumgroup.eriniumfaction.procedures.FFProcedure;
import fr.eriniumgroup.eriniumfaction.procedures.FCreateProcedure;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

// Remplace l’annotation par celle de Mod
@EventBusSubscriber
public class FactionCommand {
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Commande principale: /faction
        LiteralCommandNode<CommandSourceStack> factionNode = dispatcher.register(Commands.literal("faction").executes(FactionCommand::runBase).then(Commands.literal("f").executes(FactionCommand::runBase)) // /faction f
                .then(Commands.literal("create").then(Commands.argument("id", StringArgumentType.word()).then(Commands.argument("DisplayName", StringArgumentType.string()).executes(FactionCommand::runCreate)))));

        // Alias: /f -> redirige vers /faction (donc /f, /f f, /f create)
        dispatcher.register(Commands.literal("f").redirect(factionNode));
    }

    private static int runBase(CommandContext<CommandSourceStack> ctx) {
        Level world = ctx.getSource().getLevel();
        double x = ctx.getSource().getPosition().x();
        double y = ctx.getSource().getPosition().y();
        double z = ctx.getSource().getPosition().z();
        Entity entity = ctx.getSource().getEntity();
        if (entity == null && world instanceof ServerLevel _servLevel)
            entity = FakePlayerFactory.getMinecraft(_servLevel);

        FFProcedure.execute(world, x, y, z, entity);
        return 0;
    }

    private static int runCreate(CommandContext<CommandSourceStack> ctx) {
        Level world = ctx.getSource().getLevel();
        Entity entity = ctx.getSource().getEntity();
        if (entity == null && world instanceof ServerLevel _servLevel)
            entity = FakePlayerFactory.getMinecraft(_servLevel);

        FCreateProcedure.execute(ctx, entity);
        return 0;
    }

}