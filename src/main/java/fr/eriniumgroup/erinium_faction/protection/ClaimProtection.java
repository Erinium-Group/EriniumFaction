package fr.eriniumgroup.erinium_faction.protection;

import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimsSavedData;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
        if (player.level().isClientSide()) return;

        ClaimKey claim = ClaimKey.of(
            player.level().dimension(),
            event.getPos().getX() >> 4,
            event.getPos().getZ() >> 4
        );

        if (!canModifyInClaim(player, claim, "block.break")) {
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

        if (!canModifyInClaim(player, claim, "block.place")) {
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

        // Récupérer le block cliqué
        net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(event.getPos());
        net.minecraft.world.level.block.Block block = state.getBlock();

        // Déterminer le type d'interaction
        String permission;

        // 1. Container (coffre, furnace, barrel, etc.)
        if (block instanceof net.minecraft.world.level.block.BaseEntityBlock baseEntityBlock) {
            // Vérifier si c'est un container
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = player.level().getBlockEntity(event.getPos());
            if (blockEntity instanceof net.minecraft.world.Container) {
                permission = "faction.use.containers";
            } else {
                permission = "block.interact";
            }
        }
        // 2. Placement de block (joueur tient un block dans la main)
        else if (!player.getItemInHand(event.getHand()).isEmpty() &&
                 player.getItemInHand(event.getHand()).getItem() instanceof net.minecraft.world.item.BlockItem) {
            permission = "block.place";
        }
        // 3. Interaction normale (porte, bouton, levier, etc.)
        else {
            permission = "block.interact";
        }

        if (!canModifyInClaim(player, claim, permission)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable("erinium_faction.interact.blocked.other_faction"));
        }
    }

    private static boolean canModifyInClaim(Player player, ClaimKey claim, String actionNode) {
        // Wilderness: toujours autorisé
        if (!FactionManager.isClaimed(claim)) return true;

        String claimOwner = FactionManager.getClaimOwner(claim);

        // Wilderness retourne "wilderness" depuis getClaimOwner, mais isClaimed a déjà vérifié
        // Si pas de faction du tout, refuser l'accès aux claims d'autres factions
        Faction playerFaction = FactionManager.getPlayerFactionObject(player.getUUID());
        if (playerFaction == null) return false;

        // même faction: appliquer overrides du claim si présents
        if (claimOwner != null && !claimOwner.equals("wilderness") && claimOwner.equalsIgnoreCase(playerFaction.getId())) {
            // Owner de la faction a tous les droits
            if (playerFaction.getOwner() != null && playerFaction.getOwner().equals(player.getUUID())) return true;
            String rankId = playerFaction.getMemberRank(player.getUUID());
            if (rankId == null) return false;
            // Permissions par claim pour ce rang
            var data = ClaimsSavedData.get(((ServerPlayer)player).server);
            var set = data.getClaimPermsForRank(claim, rankId);
            if (set.isEmpty()) {
                // fallback aux perms de rang de la faction
                return playerFaction.hasPermission(player.getUUID(), actionNode);
            }
            return ClaimsSavedData.matches(set, actionNode);
        }

        return false;
    }
}
