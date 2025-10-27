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
        REMOVE_RANK_PERMISSION(4);

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
            }

            // Synchroniser les données avec tous les membres de la faction
            FactionDataPacketHandler.sendFactionDataToAllMembers(faction.getName());
        });
    }
}
