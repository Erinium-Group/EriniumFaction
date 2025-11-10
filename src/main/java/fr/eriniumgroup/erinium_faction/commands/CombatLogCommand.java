package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.eriniumgroup.erinium_faction.features.combatlog.CombatLogManager;
import fr.eriniumgroup.erinium_faction.features.combatlog.CombatTagData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Commandes pour gérer le système de combat logging
 */
public class CombatLogCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("combatlog")
                .then(Commands.literal("status")
                        .executes(CombatLogCommand::showStatus))
                .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2))
                        .executes(CombatLogCommand::clearTag))
        );
    }

    /**
     * Affiche le statut du combat tag du joueur
     */
    private static int showStatus(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Cette commande doit être exécutée par un joueur"));
            return 0;
        }

        UUID playerId = player.getUUID();
        CombatLogManager manager = CombatLogManager.getInstance();

        if (manager.isTagged(playerId)) {
            int remaining = manager.getRemainingSeconds(playerId);
            CombatTagData data = manager.getTagData(playerId);

            Component message = Component.literal("⚔ Vous êtes en combat !\n")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal("Temps restant: " + remaining + "s\n")
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("Territoire: " + (data.isInFactionTerritory() ? "Votre faction" : "Neutre"))
                            .withStyle(ChatFormatting.GRAY));

            context.getSource().sendSuccess(() -> message, false);
        } else {
            context.getSource().sendSuccess(() ->
                Component.literal("✓ Vous n'êtes pas en combat")
                    .withStyle(ChatFormatting.GREEN), false);
        }

        return 1;
    }

    /**
     * Retire le combat tag (admin uniquement)
     */
    private static int clearTag(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Cette commande doit être exécutée par un joueur"));
            return 0;
        }

        UUID playerId = player.getUUID();
        CombatLogManager manager = CombatLogManager.getInstance();

        if (manager.isTagged(playerId)) {
            CombatTagData data = manager.getTagData(playerId);
            if (data != null) {
                data.clear();
            }
            context.getSource().sendSuccess(() ->
                Component.literal("✓ Combat tag retiré")
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            context.getSource().sendFailure(Component.literal("Vous n'êtes pas en combat"));
            return 0;
        }

        return 1;
    }
}
