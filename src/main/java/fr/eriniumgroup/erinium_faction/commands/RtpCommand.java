package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.eriniumgroup.erinium_faction.features.rtp.RtpManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Commande RTP (Random Teleport)
 * Téléporte le joueur à une position aléatoire entre 5000 et 10000 blocs
 * Cooldown de 30 minutes par dimension
 * Délai de 5 secondes avant téléportation
 */
public class RtpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("rtp")
                .executes(RtpCommand::executeRtp)
        );
    }

    private static int executeRtp(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.only_players"));
            return 0;
        }

        return RtpManager.startRtp(player) ? 1 : 0;
    }
}
