package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.client.overlay.FactionTitleOverlay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client: demande d’afficher un titre overlay.
 */
public record FactionTitlePacket(String title, String subtitle, int fadeInMs, int stayMs, int fadeOutMs,
                                 String frameKey) implements CustomPacketPayload {
    private static final int MAX = 1024;
    public static final Type<FactionTitlePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "faction_title"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, FactionTitlePacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeUtf(msg.title == null ? "" : msg.title, MAX);
        buf.writeUtf(msg.subtitle == null ? "" : msg.subtitle, MAX);
        buf.writeVarInt(msg.fadeInMs);
        buf.writeVarInt(msg.stayMs);
        buf.writeVarInt(msg.fadeOutMs);
        buf.writeUtf(msg.frameKey == null ? "" : msg.frameKey, 128);
    }, buf -> new FactionTitlePacket(buf.readUtf(MAX), buf.readUtf(MAX), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(128)));

    public static void handleData(final FactionTitlePacket message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> {
                FactionTitleOverlay.setCurrentFrame(message.frameKey);
                FactionTitleOverlay.showTitle(message.title, message.subtitle, message.fadeInMs, message.stayMs, message.fadeOutMs);
            });
        }
    }
}
