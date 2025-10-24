package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FactionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(
            Commands.literal("faction")
                .then(Commands.literal("create")
                    .then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        String name = StringArgumentType.getString(ctx, "name");
                        Faction f = FactionManager.create(name, name.substring(0, Math.min(4, name.length())).toUpperCase(), sp.getUUID());
                        if (f == null) {
                            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.create.fail"));
                            return 0;
                        }
                        FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                        sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.create.success", f.getName()), true);
                        return 1;
                    })))
                .then(Commands.literal("delete")
                    .then(Commands.argument("name", StringArgumentType.word()).requires(src -> src.hasPermission(2)).executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "name");
                        boolean ok = FactionManager.delete(name);
                        if (!ok) {
                            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                            return 0;
                        }
                        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.delete.success"), true);
                        return 1;
                    })))
                .then(Commands.literal("info")
                    .then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "name");
                        Faction f = FactionManager.getByName(name);
                        if (f == null) {
                            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                            return 0;
                        }
                        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.info", f.getName(), String.format("%.0f", f.getPower()), String.format("%.0f", f.getMaxPower()), f.getLevel(), f.getXp()), false);
                        return 1;
                    })))
                .then(Commands.literal("join")
                    .then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        String name = StringArgumentType.getString(ctx, "name");
                        Faction f = FactionManager.getByName(name);
                        if (f == null) {
                            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                            return 0;
                        }
                        boolean ok = FactionManager.invite(f, sp.getUUID(), sp.getGameProfile().getName());
                        if (!ok) {
                            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.join.fail"));
                            return 0;
                        }
                        FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                        sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.join.success", f.getName()), true);
                        return 1;
                    })))
                .then(Commands.literal("leave")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        Faction f = FactionManager.getFactionOf(sp.getUUID());
                        if (f == null) {
                            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                            return 0;
                        }
                        boolean ok = FactionManager.kick(f, sp.getUUID());
                        if (!ok) {
                            ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.leave.fail"));
                            return 0;
                        }
                        FactionManager.populatePlayerVariables(sp, sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                        sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(sp);
                        ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.leave.success"), true);
                        return 1;
                    }))
                .then(Commands.literal("addxp").requires(src -> src.hasPermission(2))
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            Faction f = FactionManager.getByName(name);
                            if (f == null) {
                                ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_found"));
                                return 0;
                            }
                            f.addXp(amount);
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.addxp.success"), true);
                            return 1;
                        }))))
                .then(Commands.literal("setrank").requires(src -> src.hasPermission(2))
                    .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("rankId", StringArgumentType.word()).executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.setrank.placeholder"), false);
                            return 1;
                        }))))
                .then(Commands.literal("claim").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    Faction f = FactionManager.getFactionOf(sp.getUUID());
                    if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                    Level lvl = sp.level();
                    ClaimKey key = ClaimKey.of(lvl.dimension(), sp.chunkPosition().x, sp.chunkPosition().z);
                    if (FactionManager.isClaimed(key)) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claim.already")); return 0; }
                    boolean ok = FactionManager.tryClaim(key, f.getId());
                    if (!ok) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.claim.limit")); return 0; }
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.claim.success"), true);
                    return 1;
                }))
                .then(Commands.literal("unclaim").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    Faction f = FactionManager.getFactionOf(sp.getUUID());
                    if (f == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.not_in_faction")); return 0; }
                    Level lvl = sp.level();
                    ClaimKey key = ClaimKey.of(lvl.dimension(), sp.chunkPosition().x, sp.chunkPosition().z);
                    String owner = FactionManager.getClaimOwner(key);
                    if (owner == null) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.not_claimed")); return 0; }
                    if (!owner.equalsIgnoreCase(f.getId())) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.not_owner")); return 0; }
                    boolean ok = FactionManager.tryUnclaim(key, f.getId());
                    if (!ok) { ctx.getSource().sendFailure(Component.translatable("erinium_faction.cmd.faction.unclaim.fail")); return 0; }
                    ctx.getSource().sendSuccess(() -> Component.translatable("erinium_faction.cmd.faction.unclaim.success"), true);
                    return 1;
                }))
        );
    }
}
