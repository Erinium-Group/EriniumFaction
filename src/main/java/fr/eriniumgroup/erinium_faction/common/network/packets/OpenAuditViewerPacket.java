package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.client.gui.audit.AuditViewerScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenAuditViewerPacket() implements CustomPacketPayload {
    public static final Type<OpenAuditViewerPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "open_audit_viewer"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAuditViewerPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
    }, buf -> new OpenAuditViewerPacket());

    public static void handleData(final OpenAuditViewerPacket msg, final IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND) return;
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ctx.enqueueWork(ClientHandler::openAuditScreen);
        }
    }

    // Classe interne pour isoler le code client
    private static class ClientHandler {
        static void openAuditScreen() {
            net.minecraft.client.Minecraft.getInstance().setScreen(new AuditViewerScreen());
        }
    }
}
