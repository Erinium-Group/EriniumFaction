package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Commande /f cape toggle pour activer/désactiver la cape de faction
 * Accessible à tous les membres de faction sans permission spéciale
 */
public class CapeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("f")
                .then(Commands.literal("cape")
                    .then(Commands.literal("toggle")
                        .executes(CapeCommand::toggleCape))
                )
        );
    }

    /**
     * Commande /f cape toggle
     * Active/désactive la cape de faction pour le joueur
     */
    private static int toggleCape(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            // Vérifier que le joueur est dans une faction
            String factionId = FactionManager.getPlayerFaction(player.getUUID());
            if (factionId == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.cape.no_faction"));
                return 0;
            }

            Faction faction = FactionManager.getFaction(factionId);
            if (faction == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.cape.faction_not_found"));
                return 0;
            }

            // Vérifier que la faction a acheté la fonctionnalité bannière (requis pour la cape)
            if (!faction.hasCustomBanner()) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.cape.not_purchased"));
                return 0;
            }

            // Récupérer les variables du joueur
            EFVariables.PlayerVariables variables = player.getData(EFVariables.PLAYER_VARIABLES);

            // Toggle l'état de la cape
            boolean newState = !variables.factionCapeEnabled;
            variables.factionCapeEnabled = newState;

            // Synchroniser avec le client
            variables.syncPlayerVariables(player);

            // Message de confirmation
            if (newState) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.cape.enabled"));
            } else {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.cape.disabled"));
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("erinium_faction.command.error", e.getMessage()));
            return 0;
        }
    }
}
