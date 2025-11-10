package fr.eriniumgroup.erinium_faction.features.bounty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.eriniumgroup.erinium_faction.common.network.packets.BountyDataPacket;
import fr.eriniumgroup.erinium_faction.common.network.packets.OpenBountyMenuPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Commande /bounty pour le système de prime
 */
public class BountyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bounty")
                // /bounty - Ouvre le GUI principal
                .executes(BountyCommand::openMenu)
        );
    }

    /**
     * Ouvre le menu principal du bounty
     */
    private static int openMenu(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Cette commande ne peut être utilisée que par un joueur"));
            return 0;
        }

        // Synchroniser les données avant d'ouvrir le GUI
        BountyManager manager = BountyManager.get(player.getServer());
        List<Bounty> bounties = manager.getAllBounties();
        PacketDistributor.sendToPlayer(player, BountyDataPacket.fromBounties(bounties));

        // Ouvrir le GUI
        PacketDistributor.sendToPlayer(player, new OpenBountyMenuPacket());
        return 1;
    }
}
