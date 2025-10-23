package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.Rank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Rank management command handler
 */
public class RankCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rank")
            .then(Commands.literal("promote")
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                    .executes(RankCommand::promote)))

            .then(Commands.literal("demote")
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                    .executes(RankCommand::demote)))

            .then(Commands.literal("list")
                .executes(RankCommand::list))
        );
    }

    private static int promote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(ctx, "player");

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canPromote()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission de promouvoir !"));
            return 0;
        }

        for (GameProfile profile : targets) {
            Rank currentRank = faction.getRank(profile.getId());
            if (currentRank == null) {
                ctx.getSource().sendFailure(Component.literal("§c" + profile.getName() + " n'est pas dans votre faction !"));
                continue;
            }

            Rank newRank = getNextRank(currentRank);
            if (newRank == null || newRank == Rank.OWNER) {
                ctx.getSource().sendFailure(Component.literal("§c" + profile.getName() + " ne peut pas être promu plus haut !"));
                continue;
            }

            faction.setRank(profile.getId(), newRank);
            ctx.getSource().sendSuccess(() -> Component.literal("§a" + profile.getName() + " promu à " + newRank), true);
        }

        return 1;
    }

    private static int demote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(ctx, "player");

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canPromote()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission de rétrograder !"));
            return 0;
        }

        for (GameProfile profile : targets) {
            if (profile.getId().equals(faction.getOwnerId())) {
                ctx.getSource().sendFailure(Component.literal("§cVous ne pouvez pas rétrograder le chef !"));
                continue;
            }

            Rank currentRank = faction.getRank(profile.getId());
            if (currentRank == null) {
                ctx.getSource().sendFailure(Component.literal("§c" + profile.getName() + " n'est pas dans votre faction !"));
                continue;
            }

            Rank newRank = getPreviousRank(currentRank);
            if (newRank == null) {
                ctx.getSource().sendFailure(Component.literal("§c" + profile.getName() + " est déjà au rang le plus bas !"));
                continue;
            }

            faction.setRank(profile.getId(), newRank);
            ctx.getSource().sendSuccess(() -> Component.literal("§c" + profile.getName() + " rétrogradé à " + newRank), true);
        }

        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        StringBuilder members = new StringBuilder("§6====== Membres de " + faction.getName() + " ======\n");

        faction.getMembers().forEach((uuid, rank) -> {
            String name = player.getServer().getProfileCache()
                .get(uuid)
                .map(profile -> profile.getName())
                .orElse("Unknown");

            String color = switch(rank) {
                case OWNER -> "§c";
                case OFFICER -> "§6";
                case MEMBER -> "§e";
                case RECRUIT -> "§7";
            };

            members.append(color).append(rank).append(" §f- ").append(name).append("\n");
        });

        ctx.getSource().sendSuccess(() -> Component.literal(members.toString()), false);
        return 1;
    }

    private static Rank getNextRank(Rank current) {
        return switch(current) {
            case RECRUIT -> Rank.MEMBER;
            case MEMBER -> Rank.OFFICER;
            case OFFICER -> Rank.OWNER;
            case OWNER -> null;
        };
    }

    private static Rank getPreviousRank(Rank current) {
        return switch(current) {
            case OWNER -> Rank.OFFICER;
            case OFFICER -> Rank.MEMBER;
            case MEMBER -> Rank.RECRUIT;
            case RECRUIT -> null;
        };
    }
}

