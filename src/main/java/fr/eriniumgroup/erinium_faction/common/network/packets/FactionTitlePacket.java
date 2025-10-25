package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.client.overlay.FactionTitleOverlay;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client: demande d’afficher un titre overlay.
 */
public record FactionTitlePacket(String title, String subtitle, int fadeInMs, int stayMs, int fadeOutMs) implements CustomPacketPayload {
    public static final Type<FactionTitlePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_title"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<RegistryFriendlyByteBuf, FactionTitlePacket> STREAM_CODEC = StreamCodec.of(
        (RegistryFriendlyByteBuf buf, FactionTitlePacket msg) -> {
            buf.writeUtf(msg.title == null ? "" : msg.title);
            buf.writeUtf(msg.subtitle == null ? "" : msg.subtitle);
            buf.writeVarInt(msg.fadeInMs);
            buf.writeVarInt(msg.stayMs);
            buf.writeVarInt(msg.fadeOutMs);
        },
        (RegistryFriendlyByteBuf buf) -> new FactionTitlePacket(
            buf.readUtf(),
            buf.readUtf(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt()
        )
    );

    public static void handleData(final FactionTitlePacket message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> {
                FactionTitleOverlay.showTitle(
                    message.title,
                    message.subtitle,
                    message.fadeInMs,
                    message.stayMs,
                    message.fadeOutMs
                );
            });
        }
    }
}
