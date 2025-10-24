package fr.eriniumgroup.erinium_faction.protection;

import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Handles protection of claimed chunks from unauthorized access
 */
public class ClaimProtection {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ClaimProtection::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(ClaimProtection::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(ClaimProtection::onRightClickBlock);
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) return;

        ClaimKey claim = ClaimKey.of(
            player.level().dimension(),
            event.getPos().getX() >> 4,
            event.getPos().getZ() >> 4
        );

        if (!canModifyInClaim(player, claim)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable("erinium_faction.claim.blocked.other_faction"));
        }
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        ClaimKey claim = ClaimKey.of(
            player.level().dimension(),
            event.getPos().getX() >> 4,
            event.getPos().getZ() >> 4
        );

        if (!canModifyInClaim(player, claim)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable("erinium_faction.claim.blocked.other_faction"));
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ClaimKey claim = ClaimKey.of(
            player.level().dimension(),
            event.getPos().getX() >> 4,
            event.getPos().getZ() >> 4
        );

        if (!canModifyInClaim(player, claim)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable("erinium_faction.interact.blocked.other_faction"));
        }
    }

    private static boolean canModifyInClaim(Player player, ClaimKey claim) {
        if (!FactionManager.isClaimed(claim)) return true;

        String claimOwner = FactionManager.getClaimOwner(claim);
        String playerFaction = FactionManager.getPlayerFaction(player.getUUID());

        if (claimOwner != null && claimOwner.equals(playerFaction)) return true;

        return false;
    }
}
