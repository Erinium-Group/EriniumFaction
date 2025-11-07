package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.eriniumgroup.erinium_faction.core.permissions.EFPerms;
import fr.eriniumgroup.erinium_faction.features.vanish.VanishManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Commande /vanish (ou /v) pour rendre un joueur complètement invisible.
 * Nécessite la permission "vanish.command"
 */
public class VanishCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /vanish
        dispatcher.register(Commands.literal("vanish")
                .requires(src -> {
                    try {
                        ServerPlayer sp = src.getPlayer();
                        if (sp == null) return true; // console
                        return EFPerms.has(sp, "vanish.command");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .executes(VanishCommand::toggleVanish));

        // /v (alias)
        dispatcher.register(Commands.literal("v")
                .requires(src -> {
                    try {
                        ServerPlayer sp = src.getPlayer();
                        if (sp == null) return true; // console
                        return EFPerms.has(sp, "vanish.command");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .executes(VanishCommand::toggleVanish));
    }

    private static int toggleVanish(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();

            // Toggle le mode vanish
            boolean isNowVanished = VanishManager.toggleVanish(player);

            if (isNowVanished) {
                player.sendSystemMessage(Component.literal("§aMode vanish activé. Vous êtes invisible."));
            } else {
                player.sendSystemMessage(Component.literal("§cMode vanish désactivé. Vous êtes visible."));
            }

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErreur: Cette commande est réservée aux joueurs."));
            return 0;
        }
    }
}