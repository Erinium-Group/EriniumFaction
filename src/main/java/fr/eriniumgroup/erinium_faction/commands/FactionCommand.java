package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.eriniumgroup.erinium_faction.commands.arguments.FactionArgumentType;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.common.util.TeleportUtil;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.Rank;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Collection;

/**
 * Main faction command handler - /faction or /f
 */
public class FactionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("faction").then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word()).executes(FactionCommand::create)))

                .then(Commands.literal("disband").executes(FactionCommand::disband))

                .then(Commands.literal("invite").then(Commands.argument("players", GameProfileArgument.gameProfile()).executes(FactionCommand::invite)))

                .then(Commands.literal("kick").then(Commands.argument("players", GameProfileArgument.gameProfile()).executes(FactionCommand::kick)))

                .then(Commands.literal("leave").executes(FactionCommand::leave))

                .then(Commands.literal("claim").executes(FactionCommand::claim))

                .then(Commands.literal("unclaim").executes(FactionCommand::unclaim))

                .then(Commands.literal("sethome").executes(FactionCommand::setHome))

                .then(Commands.literal("home").executes(FactionCommand::home))

                .then(Commands.literal("ally").then(Commands.argument("faction", FactionArgumentType.faction()).executes(FactionCommand::ally)))

                .then(Commands.literal("enemy").then(Commands.argument("faction", FactionArgumentType.faction()).executes(FactionCommand::enemy)))

                .then(Commands.literal("neutral").then(Commands.argument("faction", FactionArgumentType.faction()).executes(FactionCommand::neutral)))

                .then(Commands.literal("info").executes(ctx -> info(ctx, null)).then(Commands.argument("faction", FactionArgumentType.faction()).executes(ctx -> info(ctx, FactionArgumentType.getFaction(ctx, "faction")))))

                .then(Commands.literal("f")
                        .executes(ctx -> openMenu(ctx, null))
                        .then(Commands.argument("faction", FactionArgumentType.faction())
                                .executes(ctx -> openMenu(ctx, FactionArgumentType.getFaction(ctx, "faction")))))

                .then(Commands.literal("list").executes(FactionCommand::list)));

        // Alias /f
        dispatcher.register(Commands.literal("f").redirect(dispatcher.register(Commands.literal("faction"))));
    }

    private static int create(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(ctx, "name");

        if (name.length() < EFConfig.minFactionNameLength) {
            ctx.getSource().sendFailure(Component.literal("§cLe nom doit contenir au moins " + EFConfig.minFactionNameLength + " caractères !"));
            return 0;
        }
        if (name.length() > EFConfig.maxFactionNameLength) {
            ctx.getSource().sendFailure(Component.literal("§cLe nom doit contenir au maximum " + EFConfig.maxFactionNameLength + " caractères !"));
            return 0;
        }

        if (FactionManager.isInFaction(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("§cVous êtes déjà dans une faction !"));
            return 0;
        }

        Faction faction = FactionManager.createFaction(name, player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cCe nom de faction est déjà pris !"));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§aFaction §6" + name + " §acréée avec succès !"), true);
        return 1;
    }

    private static int disband(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        if (faction.getRank(player.getUUID()) != Rank.OWNER) {
            ctx.getSource().sendFailure(Component.literal("§cSeul le chef peut dissoudre la faction !"));
            return 0;
        }

        String factionName = faction.getName();
        FactionManager.disbandFaction(factionName);
        ctx.getSource().sendSuccess(() -> Component.literal("§cFaction §6" + factionName + " §cdissoute !"), true);
        return 1;
    }

    private static int invite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(ctx, "players");

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canInvite()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission d'inviter !"));
            return 0;
        }

        int count = 0;
        for (GameProfile profile : targets) {
            if (FactionManager.addMemberToFaction(faction.getName(), profile.getId(), Rank.RECRUIT)) {
                count++;
            }
        }

        final int finalCount = count;
        ctx.getSource().sendSuccess(() -> Component.literal("§a" + finalCount + " joueur(s) invité(s) !"), true);
        return count;
    }

    private static int kick(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(ctx, "players");

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canKick()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission d'expulser !"));
            return 0;
        }

        int count = 0;
        for (GameProfile profile : targets) {
            if (profile.getId().equals(faction.getOwnerId())) {
                ctx.getSource().sendFailure(Component.literal("§cVous ne pouvez pas expulser le chef !"));
                continue;
            }
            if (FactionManager.removeMemberFromFaction(faction.getName(), profile.getId())) {
                count++;
            }
        }

        final int finalCount = count;
        ctx.getSource().sendSuccess(() -> Component.literal("§a" + finalCount + " joueur(s) expulsé(s) !"), true);
        return count;
    }

    private static int leave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        if (faction.getOwnerId().equals(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("§cLe chef doit dissoudre la faction avec /f disband !"));
            return 0;
        }

        FactionManager.removeMemberFromFaction(faction.getName(), player.getUUID());
        ctx.getSource().sendSuccess(() -> Component.literal("§aVous avez quitté la faction !"), false);
        return 1;
    }

    private static int claim(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canClaim()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission de claim !"));
            return 0;
        }

        int chunkX = player.getBlockX() >> 4;
        int chunkZ = player.getBlockZ() >> 4;
        ClaimKey claim = ClaimKey.of(player.level().dimension(), chunkX, chunkZ);

        if (FactionManager.isClaimed(claim)) {
            String owner = FactionManager.getClaimOwner(claim);
            ctx.getSource().sendFailure(Component.literal("§cCe chunk appartient à §6" + owner + " §c!"));
            return 0;
        }

        if (!faction.canClaimMore()) {
            ctx.getSource().sendFailure(Component.literal("§cVotre faction a atteint la limite de claims ! (Power: " + faction.getPower() + ")"));
            return 0;
        }

        FactionManager.addClaim(faction.getName(), claim);
        ctx.getSource().sendSuccess(() -> Component.literal("§aChunk [" + chunkX + ", " + chunkZ + "] claimé !"), true);
        return 1;
    }

    private static int unclaim(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canUnclaim()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission d'unclaim !"));
            return 0;
        }

        int chunkX = player.getBlockX() >> 4;
        int chunkZ = player.getBlockZ() >> 4;
        ClaimKey claim = ClaimKey.of(player.level().dimension(), chunkX, chunkZ);

        if (FactionManager.removeClaim(faction.getName(), claim)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§aChunk [" + chunkX + ", " + chunkZ + "] unclaimé !"), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("§cCe chunk n'appartient pas à votre faction !"));
            return 0;
        }
    }

    private static int setHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canSetHome()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission de définir le home !"));
            return 0;
        }

        faction.setHome(player.level().dimension().location().toString(), player.blockPosition());
        ctx.getSource().sendSuccess(() -> Component.literal("§aHome de la faction défini !"), true);
        return 1;
    }

    private static int home(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        if (!faction.hasHome()) {
            ctx.getSource().sendFailure(Component.literal("§cVotre faction n'a pas de home !"));
            return 0;
        }

        boolean success = TeleportUtil.teleport(player, faction.getHomeDimension(), faction.getHomePosition());
        if (success) {
            ctx.getSource().sendSuccess(() -> Component.literal("§aTéléportation au home de la faction !"), false);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("§cErreur lors de la téléportation !"));
            return 0;
        }
    }

    private static int ally(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String targetFaction = FactionArgumentType.getFaction(ctx, "faction");

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canManageRelations()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission de gérer les relations !"));
            return 0;
        }

        faction.addAlly(targetFaction);
        faction.removeEnemy(targetFaction);
        ctx.getSource().sendSuccess(() -> Component.literal("§a" + targetFaction + " ajouté comme allié !"), true);
        return 1;
    }

    private static int enemy(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String targetFaction = FactionArgumentType.getFaction(ctx, "faction");

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canManageRelations()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission de gérer les relations !"));
            return 0;
        }

        faction.addEnemy(targetFaction);
        faction.removeAlly(targetFaction);
        ctx.getSource().sendSuccess(() -> Component.literal("§c" + targetFaction + " déclaré comme ennemi !"), true);
        return 1;
    }

    private static int neutral(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String targetFaction = FactionArgumentType.getFaction(ctx, "faction");

        Faction faction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
            return 0;
        }

        Rank playerRank = faction.getRank(player.getUUID());
        if (!playerRank.canManageRelations()) {
            ctx.getSource().sendFailure(Component.literal("§cVous n'avez pas la permission de gérer les relations !"));
            return 0;
        }

        faction.removeAlly(targetFaction);
        faction.removeEnemy(targetFaction);
        ctx.getSource().sendSuccess(() -> Component.literal("§e" + targetFaction + " est maintenant neutre !"), true);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> ctx, String factionName) throws CommandSyntaxException {
        Faction faction;

        if (factionName == null) {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            faction = FactionManager.getPlayerFactionObject(player.getUUID());
            if (faction == null) {
                ctx.getSource().sendFailure(Component.literal("§cVous n'êtes pas dans une faction !"));
                return 0;
            }
        } else {
            faction = FactionManager.getFaction(factionName);
            if (faction == null) {
                ctx.getSource().sendFailure(Component.literal("§cFaction introuvable !"));
                return 0;
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§6====== Faction: " + faction.getName() + " ======\n" + "§eMembres: §f" + faction.getMembers().size() + "\n" + "§ePower: §f" + faction.getPower() + "\n" + "§eClaims: §f" + faction.getClaimCount() + "/" + faction.getMaxClaims() + "\n" + "§eAlliés: §f" + faction.getAllies().size() + "\n" + "§eEnnemis: §f" + faction.getEnemies().size()), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        Collection<Faction> factions = FactionManager.getAllFactions();

        ctx.getSource().sendSuccess(() -> Component.literal("§6====== Factions (" + factions.size() + ") ======\n" + factions.stream().map(f -> "§e- " + f.getName() + " §7(" + f.getMembers().size() + " membres)").reduce((a, b) -> a + "\n" + b).orElse("§7Aucune faction")), false);
        return 1;
    }

    private static int openMenu(CommandContext<CommandSourceStack> ctx, String factionName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        String targetFaction = factionName != null ? factionName : FactionManager.getPlayerFaction(player.getUUID());
        if (targetFaction == null) {
            ctx.getSource().sendFailure(Component.literal("§cAucune faction spécifiée et vous n'êtes pas dans une faction !"));
            return 0;
        }
        if (!FactionManager.factionExists(targetFaction)) {
            ctx.getSource().sendFailure(Component.literal("§cFaction introuvable !"));
            return 0;
        }

        BlockPos pos = player.blockPosition();
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("FactionMenu");
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeBlockPos(pos);
                buf.writeUtf(targetFaction);
                return new FactionMenu(id, inventory, buf);
            }
        }, pos);

        return 1;
    }
}