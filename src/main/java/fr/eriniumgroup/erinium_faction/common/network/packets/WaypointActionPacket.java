package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.minimap.Waypoint;
import fr.eriniumgroup.erinium_faction.features.minimap.WaypointServerManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Paquet pour les actions sur les waypoints (ajout, suppression, modification)
 */
public record WaypointActionPacket(
    Action action,
    UUID waypointId,
    String name,
    BlockPos position,
    ResourceKey<Level> dimension,
    Waypoint.WaypointColor color,
    int customRGB,
    boolean enabled
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WaypointActionPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "waypoint_action"));

    public static final StreamCodec<ByteBuf, WaypointActionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public WaypointActionPacket decode(ByteBuf buf) {
            Action action = ByteBufCodecs.fromCodec(net.minecraft.util.StringRepresentable.fromEnum(Action::values)).decode(buf);
            UUID waypointId = ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC).decode(buf);
            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
            BlockPos position = BlockPos.STREAM_CODEC.decode(buf);
            ResourceKey<Level> dimension = ResourceKey.streamCodec(Registries.DIMENSION).decode(buf);
            Waypoint.WaypointColor color = ByteBufCodecs.fromCodec(net.minecraft.util.StringRepresentable.fromEnum(Waypoint.WaypointColor::values)).decode(buf);
            int customRGB = ByteBufCodecs.VAR_INT.decode(buf);
            boolean enabled = ByteBufCodecs.BOOL.decode(buf);
            return new WaypointActionPacket(action, waypointId, name, position, dimension, color, customRGB, enabled);
        }

        @Override
        public void encode(ByteBuf buf, WaypointActionPacket packet) {
            ByteBufCodecs.fromCodec(net.minecraft.util.StringRepresentable.fromEnum(Action::values)).encode(buf, packet.action());
            ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC).encode(buf, packet.waypointId());
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.name());
            BlockPos.STREAM_CODEC.encode(buf, packet.position());
            ResourceKey.streamCodec(Registries.DIMENSION).encode(buf, packet.dimension());
            ByteBufCodecs.fromCodec(net.minecraft.util.StringRepresentable.fromEnum(Waypoint.WaypointColor::values)).encode(buf, packet.color());
            ByteBufCodecs.VAR_INT.encode(buf, packet.customRGB());
            ByteBufCodecs.BOOL.encode(buf, packet.enabled());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WaypointActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            WaypointServerManager manager = WaypointServerManager.getInstance();

            switch (packet.action()) {
                case ADD -> manager.addWaypoint(
                    serverPlayer.getUUID(),
                    packet.name(),
                    packet.position(),
                    packet.dimension(),
                    packet.color(),
                    packet.customRGB(),
                    packet.enabled()
                );
                case REMOVE -> manager.removeWaypoint(
                    serverPlayer.getUUID(),
                    packet.waypointId()
                );
                case UPDATE -> manager.updateWaypoint(
                    serverPlayer.getUUID(),
                    packet.waypointId(),
                    packet.name(),
                    packet.position(),
                    packet.color(),
                    packet.customRGB(),
                    packet.enabled()
                );
            }

            // Synchroniser les waypoints du joueur
            manager.syncToPlayer(serverPlayer);
        });
    }

    public enum Action implements net.minecraft.util.StringRepresentable {
        ADD("add"),
        REMOVE("remove"),
        UPDATE("update");

        private final String name;

        Action(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
