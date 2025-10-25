package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionMenuSettingsScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FactionTitlePacket (ServerPlayer sp, String lastchunk) implements CustomPacketPayload {
    public static final Type<FactionTitlePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_title"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, FactionTitlePacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeUtf(msg.lastchunk);
    }, (buf) -> new FactionTitlePacket(buf.readUtf());

    public static void handleData(final FactionTitlePacket message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> FactionMenuSettingsScreen.onSettingsState(message));
        }
    }


}
