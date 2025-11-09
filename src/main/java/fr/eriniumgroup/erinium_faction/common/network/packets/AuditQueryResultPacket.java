package fr.eriniumgroup.erinium_faction.common.network.packets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record AuditQueryResultPacket(List<String> jsonLines) implements CustomPacketPayload {
    public static final Type<AuditQueryResultPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "audit_query_result"));
    private static final Gson G = new Gson();

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static AuditQueryResultPacket fromJsonObjects(List<JsonObject> list) {
        List<String> lines = new ArrayList<>(list.size());
        for (var o : list) lines.add(G.toJson(o));
        return new AuditQueryResultPacket(lines);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, AuditQueryResultPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeVarInt(msg.jsonLines.size());
        for (String s : msg.jsonLines) buf.writeUtf(s, 32767);
    }, buf -> {
        int n = buf.readVarInt();
        List<String> lines = new ArrayList<>(n);
        for (int i = 0; i < n; i++) lines.add(buf.readUtf(32767));
        return new AuditQueryResultPacket(lines);
    });

    public static void handleData(final AuditQueryResultPacket msg, final IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND) return;
        ctx.enqueueWork(() -> fr.eriniumgroup.erinium_faction.client.gui.audit.AuditViewerScreen.pushResults(msg.jsonLines));
    }
}

