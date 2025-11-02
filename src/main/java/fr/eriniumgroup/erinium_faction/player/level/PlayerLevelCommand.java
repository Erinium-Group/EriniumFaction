package fr.eriniumgroup.erinium_faction.player.level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import fr.eriniumgroup.erinium_faction.jobs.JobsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Commandes pour le système de niveau des joueurs
 */
public class PlayerLevelCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerlevel")
            .then(Commands.literal("info")
                .executes(ctx -> showInfo(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> showInfo(ctx, EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("setlevel")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.players())
                    .then(Commands.argument("level", IntegerArgumentType.integer(1, 1000))
                        .executes(ctx -> setLevel(ctx, EntityArgument.getPlayers(ctx, "player"), IntegerArgumentType.getInteger(ctx, "level"))))))
            .then(Commands.literal("distribute")
                .then(Commands.argument("attribute", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (PlayerLevelManager.AttributeType type : PlayerLevelManager.AttributeType.values()) {
                            builder.suggest(type.name().toLowerCase());
                        }
                        return builder.buildFuture();
                    })
                    .executes(ctx -> distributePoint(ctx, ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "attribute")))))
            .then(Commands.literal("reset")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> fullReset(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> fullReset(ctx, EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("resetattributes")
                .executes(ctx -> resetAttributes(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> resetAttributes(ctx, EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("jobs")
                .then(Commands.literal("info")
                    .executes(ctx -> showJobsInfo(ctx, ctx.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> showJobsInfo(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("setlevel")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("job", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (JobType type : JobType.values()) {
                                    builder.suggest(type.name().toLowerCase());
                                }
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> setJobLevel(ctx,
                                    EntityArgument.getPlayer(ctx, "player"),
                                    StringArgumentType.getString(ctx, "job"),
                                    IntegerArgumentType.getInteger(ctx, "level")))))))
                .then(Commands.literal("addxp")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("job", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (JobType type : JobType.values()) {
                                    builder.suggest(type.name().toLowerCase());
                                }
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> addJobXp(ctx,
                                    EntityArgument.getPlayer(ctx, "player"),
                                    StringArgumentType.getString(ctx, "job"),
                                    IntegerArgumentType.getInteger(ctx, "amount")))))))));
    }

    private static int showInfo(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        PlayerLevelData data = PlayerLevelManager.getLevelData(player);

        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.title")
            .withStyle(style -> style.withColor(0xFFAA00).withBold(true)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.player", player.getName().getString())
            .withStyle(style -> style.withColor(0xFFFF55)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.level",
            data.getLevel(), PlayerLevelConfig.MAX_LEVEL.get())
            .withStyle(style -> style.withColor(0xFFFF55)), false);

        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.points", data.getAvailablePoints())
            .withStyle(style -> style.withColor(0xFFFF55)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.attributes")
            .withStyle(style -> style.withColor(0xFFAA00).withBold(true)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.health_value", data.getHealthPoints())
            .withStyle(style -> style.withColor(0xFF5555)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.armor_value", data.getArmorPoints())
            .withStyle(style -> style.withColor(0xAAAAAA)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.speed_value", data.getSpeedPoints())
            .withStyle(style -> style.withColor(0x55FFFF)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.intelligence_value", data.getIntelligencePoints())
            .withStyle(style -> style.withColor(0xFF55FF)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.strength_value", data.getStrengthPoints())
            .withStyle(style -> style.withColor(0xAA0000)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.info.luck_value", data.getLuckPoints())
            .withStyle(style -> style.withColor(0x55FF55)), false);

        return 1;
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, int level) {
        for (ServerPlayer player : players) {
            PlayerLevelManager.setLevel(player, level);

            ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.setlevel.success",
                player.getName().getString(), level)
                .withStyle(style -> style.withColor(0xFFFF55)), true);
        }
        return players.size();
    }
    
    private static int distributePoint(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String attributeStr) {
        try {
            PlayerLevelManager.AttributeType type = PlayerLevelManager.AttributeType.valueOf(attributeStr.toUpperCase());
            
            if (PlayerLevelManager.distributePoint(player, type)) {
                return 1;
            } else {
                return 0;
            }
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable("player_level.command.distribute.invalid")
                .withStyle(style -> style.withColor(0xFF5555)));
            return 0;
        }
    }
    
    private static int resetAttributes(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        PlayerLevelManager.resetAttributes(player);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.reset.success",
            player.getName().getString())
            .withStyle(style -> style.withColor(0xFFFF55)), true);
        return 1;
    }

    private static int fullReset(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        // Réinitialiser le PlayerLevel et les stats
        PlayerLevelManager.fullReset(player);

        // Réinitialiser tous les métiers
        JobsManager.resetAllJobs(player);

        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.fullreset.success",
            player.getName().getString())
            .withStyle(style -> style.withColor(0xFFFF55)), true);

        player.sendSystemMessage(Component.translatable("player_level.command.fullreset.notification")
            .withStyle(style -> style.withColor(0xFF5555).withBold(true)));

        return 1;
    }

    private static int showJobsInfo(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.jobs.info.title")
            .withStyle(style -> style.withColor(0xFFAA00).withBold(true)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.jobs.info.player", player.getName().getString())
            .withStyle(style -> style.withColor(0xFFFF55)), false);

        // Afficher les informations pour chaque métier
        for (JobType jobType : JobType.values()) {
            int level = JobsManager.getJobLevel(player, jobType);
            int xp = JobsManager.getJobExperience(player, jobType);
            int xpToNext = JobsManager.getExperienceForNextLevel(level);

            ctx.getSource().sendSuccess(() -> Component.literal(jobType.getDisplayName() + ": ")
                .withStyle(style -> style.withColor(jobType.getColor()))
                .append(Component.translatable("player_level.command.jobs.info.level", level)
                    .withStyle(style -> style.withColor(0xFFFF55)))
                .append(Component.literal(" ")
                    .append(Component.translatable("player_level.command.jobs.info.xp", xp, xpToNext)
                        .withStyle(style -> style.withColor(0xAAAAAA)))), false);
        }

        return 1;
    }

    private static int setJobLevel(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String jobStr, int level) {
        try {
            JobType jobType = JobType.valueOf(jobStr.toUpperCase());
            JobsManager.setJobLevel(player, jobType, level);

            ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.jobs.setlevel.success",
                player.getName().getString(), jobType.getDisplayName(), level)
                .withStyle(style -> style.withColor(0xFFFF55)), true);

            return 1;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable("player_level.command.jobs.invalid")
                .withStyle(style -> style.withColor(0xFF5555)));
            return 0;
        }
    }

    private static int addJobXp(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String jobStr, int amount) {
        try {
            JobType jobType = JobType.valueOf(jobStr.toUpperCase());
            int levelsGained = JobsManager.addJobExperience(player, jobType, amount, "Admin command");

            if (levelsGained > 0) {
                ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.jobs.addxp.success.levelup",
                    player.getName().getString(), amount, jobType.getDisplayName(), levelsGained)
                    .withStyle(style -> style.withColor(0x55FF55)), true);
            } else {
                ctx.getSource().sendSuccess(() -> Component.translatable("player_level.command.jobs.addxp.success",
                    player.getName().getString(), amount, jobType.getDisplayName())
                    .withStyle(style -> style.withColor(0xFFFF55)), true);
            }

            return 1;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable("player_level.command.jobs.invalid")
                .withStyle(style -> style.withColor(0xFF5555)));
            return 0;
        }
    }
}

