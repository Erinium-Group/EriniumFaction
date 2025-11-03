package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.minimap.Waypoint;
import fr.eriniumgroup.erinium_faction.features.minimap.WaypointManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Paquet pour synchroniser tous les waypoints du serveur vers le client
 */
public record WaypointSyncPacket(List<WaypointData> waypoints) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WaypointSyncPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "waypoint_sync"));

    public static final StreamCodec<ByteBuf, WaypointSyncPacket> STREAM_CODEC = StreamCodec.composite(
        WaypointData.STREAM_CODEC.apply(ByteBufCodecs.list()),
        WaypointSyncPacket::waypoints,
        WaypointSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WaypointSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            WaypointManager manager = WaypointManager.getInstance();

            // Remplacer complètement la liste des waypoints avec celle du serveur
            manager.clearAllWaypoints();

            for (WaypointData data : packet.waypoints()) {
                Waypoint waypoint = new Waypoint(
                    data.id(),
                    data.name(),
                    data.position(),
                    data.dimension(),
                    data.customRGB()
                );
                waypoint.setColor(data.color());
                if (data.color() == Waypoint.WaypointColor.CUSTOM) {
                    waypoint.setCustomColor(data.customRGB());
                }
                waypoint.setEnabled(data.enabled());
                manager.addWaypointDirect(waypoint);
            }
        });
    }

    public record WaypointData(
        UUID id,
        String name,
        BlockPos position,
        ResourceKey<Level> dimension,
        Waypoint.WaypointColor color,
        int customRGB,
        boolean enabled
    ) {
        public static final StreamCodec<ByteBuf, WaypointData> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public WaypointData decode(ByteBuf buf) {
                UUID id = ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC).decode(buf);
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                BlockPos position = BlockPos.STREAM_CODEC.decode(buf);
                ResourceKey<Level> dimension = ResourceKey.streamCodec(Registries.DIMENSION).decode(buf);
                Waypoint.WaypointColor color = ByteBufCodecs.fromCodec(net.minecraft.util.StringRepresentable.fromEnum(Waypoint.WaypointColor::values)).decode(buf);
                int customRGB = ByteBufCodecs.VAR_INT.decode(buf);
                boolean enabled = ByteBufCodecs.BOOL.decode(buf);
                return new WaypointData(id, name, position, dimension, color, customRGB, enabled);
            }

            @Override
            public void encode(ByteBuf buf, WaypointData data) {
                ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC).encode(buf, data.id());
                ByteBufCodecs.STRING_UTF8.encode(buf, data.name());
                BlockPos.STREAM_CODEC.encode(buf, data.position());
                ResourceKey.streamCodec(Registries.DIMENSION).encode(buf, data.dimension());
                ByteBufCodecs.fromCodec(net.minecraft.util.StringRepresentable.fromEnum(Waypoint.WaypointColor::values)).encode(buf, data.color());
                ByteBufCodecs.VAR_INT.encode(buf, data.customRGB());
                ByteBufCodecs.BOOL.encode(buf, data.enabled());
            }
        };
    }
}
