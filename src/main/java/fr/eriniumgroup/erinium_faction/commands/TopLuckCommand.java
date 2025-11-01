package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import fr.eriniumgroup.erinium_faction.features.topluck.TopLuckAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TopLuckCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("topluck")
            .requires(src -> src.hasPermission(2)) // admin only
            .executes(ctx -> {
                var src = ctx.getSource();
                if (!(src.getEntity() instanceof ServerPlayer viewer)) {
                    src.sendFailure(net.minecraft.network.chat.Component.literal("Exécutez cette commande en jeu (joueur requis)."));
                    return 0;
                }
                return executeFor(src, viewer, viewer);
            })
            .then(Commands.argument("player", GameProfileArgument.gameProfile())
                .executes(ctx -> {
                    var src = ctx.getSource();
                    if (!(src.getEntity() instanceof ServerPlayer viewer)) {
                        src.sendFailure(net.minecraft.network.chat.Component.literal("Exécutez cette commande en jeu (joueur requis)."));
                        return 0;
                    }
                    var profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                    var server = src.getServer();
                    ServerPlayer target = null;
                    for (var gp : profiles) {
                        var sp = server.getPlayerList().getPlayer(gp.getId());
                        if (sp != null) { target = sp; break; }
                    }
                    if (target == null) {
                        src.sendFailure(net.minecraft.network.chat.Component.literal("Joueur hors-ligne ou introuvable (doit être en ligne)."));
                        return 0;
                    }
                    return executeFor(src, viewer, target);
                })
            )
        );
    }

    private static int executeFor(CommandSourceStack src, ServerPlayer viewer, ServerPlayer target) {
        var attachment = target.getData(TopLuckAttachments.PLAYER_TOPLUCK);
        var all = attachment.getAll();

        long stoneLikeBase = 0L;
        long netherBase = 0L;
        for (var e : all.entrySet()) {
            String id = e.getKey();
            long c = e.getValue();
            if (isStoneLike(id)) stoneLikeBase += c;
            if (isNetherRack(id)) netherBase += c;
        }
        if (stoneLikeBase <= 0) stoneLikeBase = 1;
        if (netherBase <= 0) netherBase = 1;

        List<fr.eriniumgroup.erinium_faction.common.network.packets.TopLuckSyncMessage.BlockEntry> blockEntries = new ArrayList<>();
        for (var e : all.entrySet()) {
            String id = e.getKey();
            if (!isOre(id) && !isAncientDebris(id)) continue;
            long count = e.getValue();
            if (count <= 0) continue;
            boolean nether = isNetherOre(id) || isAncientDebris(id);
            long baselineCount = nether ? netherBase : (id.contains("deepslate") ? countDeepslate(all) : countStone(all));
            double ratio = count / (double) Math.max(1L, baselineCount);
            blockEntries.add(new fr.eriniumgroup.erinium_faction.common.network.packets.TopLuckSyncMessage.BlockEntry(id, count, ratio, baselineCount));
        }
        blockEntries.sort(Comparator.comparingLong(fr.eriniumgroup.erinium_faction.common.network.packets.TopLuckSyncMessage.BlockEntry::count).reversed());

        List<fr.eriniumgroup.erinium_faction.common.network.packets.TopLuckSyncMessage.CategoryEntry> catEntries = new ArrayList<>();

        fr.eriniumgroup.erinium_faction.common.network.packets.TopLuckSyncMessage.sendTo(viewer, catEntries, blockEntries, stoneLikeBase + netherBase, target.getGameProfile().getName());
        return 1;
    }

    private static long countStone(java.util.Map<String, Long> all) {
        long base = 0L;
        for (var e : all.entrySet()) if (e.getKey().contains(":stone")) base += e.getValue();
        return base;
    }

    private static long countDeepslate(java.util.Map<String, Long> all) {
        long base = 0L;
        for (var e : all.entrySet()) if (e.getKey().contains(":deepslate")) base += e.getValue();
        return base;
    }

    private static boolean isStoneLike(String id) {
        if (id == null) return false;
        return id.contains(":stone") || id.contains(":deepslate");
    }

    private static boolean isNetherRack(String id) { return "minecraft:netherrack".equals(id); }

    private static boolean isOre(String id) {
        if (id == null) return false;
        return id.endsWith("_ore") || id.contains(":deepslate_") && id.endsWith("_ore") || id.contains(":nether_") && id.endsWith("_ore");
    }

    private static boolean isNetherOre(String id) { return id != null && (id.contains(":nether_") || id.contains("_nether_")); }

    private static boolean isAncientDebris(String id) { return "minecraft:ancient_debris".equals(id); }
}
