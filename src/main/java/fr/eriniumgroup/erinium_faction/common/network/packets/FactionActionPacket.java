package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Packet unifié pour les actions de faction (settings, permissions, etc.)
 */
public record FactionActionPacket(ActionType action, String data1, String data2) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FactionActionPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_action"));

    public static final StreamCodec<ByteBuf, FactionActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(ActionType::fromId, ActionType::getId),
            FactionActionPacket::action,
            ByteBufCodecs.STRING_UTF8,
            FactionActionPacket::data1,
            ByteBufCodecs.STRING_UTF8,
            FactionActionPacket::data2,
            FactionActionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum ActionType {
        UPDATE_NAME(0),
        UPDATE_DESCRIPTION(1),
        UPDATE_MODE(2),
        ADD_RANK_PERMISSION(3),
        REMOVE_RANK_PERMISSION(4),
        REQUEST_ALLIANCE(5),
        ACCEPT_ALLIANCE(6),
        REFUSE_ALLIANCE(7),
        REMOVE_ALLIANCE(8),
        PROMOTE_MEMBER(9),
        DEMOTE_MEMBER(10),
        KICK_MEMBER(11);

        private final int id;

        ActionType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static ActionType fromId(int id) {
            for (ActionType type : values()) {
                if (type.id == id) return type;
            }
            return UPDATE_NAME;
        }
    }

    public static void handleData(FactionActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Faction faction = FactionManager.getFactionOf(player.getUUID());
            if (faction == null) {
                EFC.log.warn("Player " + player.getName().getString() + " tried to update faction but is not in a faction");
                return;
            }

            // Vérifier les permissions (seulement le leader peut modifier pour l'instant)
            if (!faction.getOwner().equals(player.getUUID())) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cOnly the faction leader can modify settings!"));
                return;
            }

            switch (packet.action) {
                case UPDATE_NAME:
                    if (packet.data1 != null && !packet.data1.isEmpty()) {
                        faction.setName(packet.data1);
                        FactionManager.markDirty();
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aFaction name updated to: " + packet.data1));
                    }
                    break;

                case UPDATE_DESCRIPTION:
                    faction.setDescription(packet.data1 != null ? packet.data1 : "");
                    FactionManager.markDirty();
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aFaction description updated!"));
                    break;

                case UPDATE_MODE:
                    if ("OPEN".equalsIgnoreCase(packet.data1)) {
                        faction.setMode(Faction.Mode.PUBLIC);
                    } else {
                        faction.setMode(Faction.Mode.INVITE_ONLY);
                    }
                    FactionManager.markDirty();
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aFaction mode updated to: " + faction.getMode()));
                    break;

                case ADD_RANK_PERMISSION:
                    if (packet.data1 != null && packet.data2 != null) {
                        boolean success = faction.addRankPerm(packet.data1, packet.data2);
                        if (success) {
                            FactionManager.markDirty();
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aPermission added: " + packet.data2 + " to " + packet.data1));
                        }
                    }
                    break;

                case REMOVE_RANK_PERMISSION:
                    if (packet.data1 != null && packet.data2 != null) {
                        boolean success = faction.removeRankPerm(packet.data1, packet.data2);
                        if (success) {
                            FactionManager.markDirty();
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cPermission removed: " + packet.data2 + " from " + packet.data1));
                        }
                    }
                    break;

                case REQUEST_ALLIANCE:
                    if (packet.data1 != null && !packet.data1.isEmpty()) {
                        Faction targetFaction = FactionManager.getFaction(packet.data1);
                        if (targetFaction == null) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cFaction not found!"));
                            return;
                        }
                        if (targetFaction.getId().equals(faction.getId())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou cannot ally with yourself!"));
                            return;
                        }
                        if (faction.getAllies().contains(targetFaction.getId())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou are already allied with this faction!"));
                            return;
                        }
                        if (targetFaction.getAllyRequests().contains(faction.getId())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou already sent a request to this faction!"));
                            return;
                        }
                        targetFaction.getAllyRequests().add(faction.getId());
                        FactionManager.markDirty();
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aAlliance request sent to " + targetFaction.getName()));
                    }
                    break;

                case ACCEPT_ALLIANCE:
                    if (packet.data1 != null && !packet.data1.isEmpty()) {
                        Faction requestingFaction = FactionManager.getFaction(packet.data1);
                        if (requestingFaction == null) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cFaction not found!"));
                            return;
                        }
                        if (!faction.getAllyRequests().contains(requestingFaction.getId())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cNo alliance request from this faction!"));
                            return;
                        }
                        // Ajouter l'alliance des deux côtés
                        faction.getAllies().add(requestingFaction.getId());
                        requestingFaction.getAllies().add(faction.getId());
                        // Retirer la demande
                        faction.getAllyRequests().remove(requestingFaction.getId());
                        FactionManager.markDirty();
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aAlliance accepted with " + requestingFaction.getName()));

                        // Notifier l'autre faction
                        for (var member : requestingFaction.getMembers().keySet()) {
                            ServerPlayer sp = player.getServer().getPlayerList().getPlayer(member);
                            if (sp != null) {
                                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aYour alliance request to " + faction.getName() + " was accepted!"));
                            }
                        }
                    }
                    break;

                case REFUSE_ALLIANCE:
                    if (packet.data1 != null && !packet.data1.isEmpty()) {
                        Faction requestingFaction = FactionManager.getFaction(packet.data1);
                        if (requestingFaction == null) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cFaction not found!"));
                            return;
                        }
                        if (!faction.getAllyRequests().contains(requestingFaction.getId())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cNo alliance request from this faction!"));
                            return;
                        }
                        // Retirer la demande
                        faction.getAllyRequests().remove(requestingFaction.getId());
                        FactionManager.markDirty();
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cAlliance request refused from " + requestingFaction.getName()));
                    }
                    break;

                case REMOVE_ALLIANCE:
                    if (packet.data1 != null && !packet.data1.isEmpty()) {
                        Faction allyFaction = FactionManager.getFaction(packet.data1);
                        if (allyFaction == null) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cFaction not found!"));
                            return;
                        }
                        if (!faction.getAllies().contains(allyFaction.getId())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou are not allied with this faction!"));
                            return;
                        }
                        // Retirer l'alliance des deux côtés
                        faction.getAllies().remove(allyFaction.getId());
                        allyFaction.getAllies().remove(faction.getId());
                        FactionManager.markDirty();
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cAlliance removed with " + allyFaction.getName()));

                        // Notifier l'autre faction
                        for (var member : allyFaction.getMembers().keySet()) {
                            ServerPlayer sp = player.getServer().getPlayerList().getPlayer(member);
                            if (sp != null) {
                                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c" + faction.getName() + " has broken the alliance with your faction!"));
                            }
                        }
                    }
                    break;

                case PROMOTE_MEMBER:
                    if (packet.data1 != null && !packet.data1.isEmpty()) {
                        // Vérifier la permission
                        if (!faction.hasPermission(player.getUUID(), fr.eriniumgroup.erinium_faction.core.faction.Permission.PROMOTE_MEMBERS.getServerKey())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou don't have permission to promote members!"));
                            return;
                        }

                        // Trouver le joueur cible par nom
                        java.util.UUID targetUUID = null;
                        String targetName = packet.data1;
                        for (var entry : faction.getMembers().entrySet()) {
                            if (entry.getValue().nameCached.equalsIgnoreCase(targetName)) {
                                targetUUID = entry.getKey();
                                break;
                            }
                        }

                        if (targetUUID == null) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cMember not found!"));
                            return;
                        }

                        // Vérifier la hiérarchie (ne peut promouvoir que les membres de rank inférieur)
                        String callerRankId = faction.getMemberRank(player.getUUID());
                        String targetRankId = faction.getMemberRank(targetUUID);
                        if (callerRankId == null || targetRankId == null) return;

                        Faction.RankDef callerRank = faction.getRanks().get(callerRankId);
                        Faction.RankDef targetRank = faction.getRanks().get(targetRankId);
                        if (callerRank == null || targetRank == null) return;

                        if (callerRank.priority <= targetRank.priority) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou can only promote members with lower ranks!"));
                            return;
                        }

                        // Promouvoir
                        boolean success = faction.promoteMember(targetUUID);
                        if (success) {
                            FactionManager.markDirty();
                            String newRankId = faction.getMemberRank(targetUUID);
                            Faction.RankDef newRank = faction.getRanks().get(newRankId);
                            String newRankDisplay = newRank != null ? newRank.display : newRankId;
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a" + targetName + " promoted to " + newRankDisplay));

                            // Notifier le joueur promu
                            ServerPlayer target = player.getServer().getPlayerList().getPlayer(targetUUID);
                            if (target != null) {
                                target.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aYou have been promoted to " + newRankDisplay + "!"));
                            }
                        } else {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cCannot promote " + targetName + " (already at max rank)"));
                        }
                    }
                    break;

                case DEMOTE_MEMBER:
                    if (packet.data1 != null && !packet.data1.isEmpty()) {
                        // Vérifier la permission
                        if (!faction.hasPermission(player.getUUID(), fr.eriniumgroup.erinium_faction.core.faction.Permission.DEMOTE_MEMBERS.getServerKey())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou don't have permission to demote members!"));
                            return;
                        }

                        // Trouver le joueur cible par nom
                        java.util.UUID targetUUID = null;
                        String targetName = packet.data1;
                        for (var entry : faction.getMembers().entrySet()) {
                            if (entry.getValue().nameCached.equalsIgnoreCase(targetName)) {
                                targetUUID = entry.getKey();
                                break;
                            }
                        }

                        if (targetUUID == null) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cMember not found!"));
                            return;
                        }

                        // Vérifier la hiérarchie (ne peut démote que les membres de rank inférieur)
                        String callerRankId = faction.getMemberRank(player.getUUID());
                        String targetRankId = faction.getMemberRank(targetUUID);
                        if (callerRankId == null || targetRankId == null) return;

                        Faction.RankDef callerRank = faction.getRanks().get(callerRankId);
                        Faction.RankDef targetRank = faction.getRanks().get(targetRankId);
                        if (callerRank == null || targetRank == null) return;

                        if (callerRank.priority <= targetRank.priority) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou can only demote members with lower ranks!"));
                            return;
                        }

                        // Dégrader
                        boolean success = faction.demoteMember(targetUUID);
                        if (success) {
                            FactionManager.markDirty();
                            String newRankId = faction.getMemberRank(targetUUID);
                            Faction.RankDef newRank = faction.getRanks().get(newRankId);
                            String newRankDisplay = newRank != null ? newRank.display : newRankId;
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c" + targetName + " demoted to " + newRankDisplay));

                            // Notifier le joueur dégradé
                            ServerPlayer target = player.getServer().getPlayerList().getPlayer(targetUUID);
                            if (target != null) {
                                target.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou have been demoted to " + newRankDisplay));
                            }
                        } else {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cCannot demote " + targetName + " (already at min rank)"));
                        }
                    }
                    break;

                case KICK_MEMBER:
                    if (packet.data1 != null && !packet.data1.isEmpty()) {
                        // Vérifier la permission
                        if (!faction.hasPermission(player.getUUID(), fr.eriniumgroup.erinium_faction.core.faction.Permission.KICK_MEMBERS.getServerKey())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou don't have permission to kick members!"));
                            return;
                        }

                        // Trouver le joueur cible par nom
                        java.util.UUID targetUUID = null;
                        String targetName = packet.data1;
                        for (var entry : faction.getMembers().entrySet()) {
                            if (entry.getValue().nameCached.equalsIgnoreCase(targetName)) {
                                targetUUID = entry.getKey();
                                break;
                            }
                        }

                        if (targetUUID == null) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cMember not found!"));
                            return;
                        }

                        // Ne peut pas kicker le leader
                        if (targetUUID.equals(faction.getOwner())) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou cannot kick the faction leader!"));
                            return;
                        }

                        // Vérifier la hiérarchie (ne peut kick que les membres de rank inférieur)
                        String callerRankId = faction.getMemberRank(player.getUUID());
                        String targetRankId = faction.getMemberRank(targetUUID);
                        if (callerRankId == null || targetRankId == null) return;

                        Faction.RankDef callerRank = faction.getRanks().get(callerRankId);
                        Faction.RankDef targetRank = faction.getRanks().get(targetRankId);
                        if (callerRank == null || targetRank == null) return;

                        if (callerRank.priority <= targetRank.priority) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou can only kick members with lower ranks!"));
                            return;
                        }

                        // Kicker
                        boolean success = faction.removeMember(targetUUID);
                        if (success) {
                            FactionManager.markDirty();
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c" + targetName + " has been kicked from the faction"));

                            // Notifier le joueur kické et mettre à jour ses variables
                            ServerPlayer target = player.getServer().getPlayerList().getPlayer(targetUUID);
                            if (target != null) {
                                target.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou have been kicked from " + faction.getName()));
                                FactionManager.populatePlayerVariables(target, target.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES));
                                target.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES).syncPlayerVariables(target);
                            }
                        } else {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cFailed to kick " + targetName));
                        }
                    }
                    break;
            }

            // Synchroniser les données avec tous les membres de la faction
            FactionDataPacketHandler.sendFactionDataToAllMembers(faction.getName());
        });
    }
}
