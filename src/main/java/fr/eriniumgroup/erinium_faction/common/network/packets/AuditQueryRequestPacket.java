package fr.eriniumgroup.erinium_faction.common.network.packets;

import com.google.gson.JsonObject;
import fr.eriniumgroup.erinium_faction.features.audit.AuditQuery;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

public record AuditQueryRequestPacket(String playerUuid, String blockId, String event, long fromEpochMs, long toEpochMs,
                                      int limit, int offset) implements CustomPacketPayload {
    public static final Type<AuditQueryRequestPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "audit_query_request"));
    private static final int MAX = 2048;

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, AuditQueryRequestPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeUtf(msg.playerUuid == null ? "" : msg.playerUuid, MAX);
        buf.writeUtf(msg.blockId == null ? "" : msg.blockId, MAX);
        buf.writeUtf(msg.event == null ? "" : msg.event, 128);
        buf.writeLong(msg.fromEpochMs);
        buf.writeLong(msg.toEpochMs);
        buf.writeVarInt(msg.limit);
        buf.writeVarInt(msg.offset);
    }, buf -> new AuditQueryRequestPacket(emptyToNull(buf.readUtf(MAX)), emptyToNull(buf.readUtf(MAX)), emptyToNull(buf.readUtf(128)), buf.readLong(), buf.readLong(), buf.readVarInt(), buf.readVarInt()));

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    public static void handleData(final AuditQueryRequestPacket message, final IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.SERVERBOUND) return;
        ctx.enqueueWork(() -> {
            var from = message.fromEpochMs > 0 ? Instant.ofEpochMilli(message.fromEpochMs) : null;
            var to = message.toEpochMs > 0 ? Instant.ofEpochMilli(message.toEpochMs) : null;
            int safeLimit = Math.max(1, Math.min(200, message.limit));
            int safeOffset = Math.max(0, message.offset);
            List<JsonObject> results = AuditQuery.find(message.playerUuid, message.blockId, message.event, from, to, safeLimit, safeOffset);
            if (ctx.player() instanceof ServerPlayer sp) {
                PacketDistributor.sendToPlayer(sp, AuditQueryResultPacket.fromJsonObjects(results));
            }
        });
    }
}
