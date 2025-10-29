package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Serveur -> Client: envoie la liste validée des demandes d'alliance
 */
public record AllyRequestsDataMessage(List<String> requestIds, List<String> requestNames) implements CustomPacketPayload {
    public static final Type<AllyRequestsDataMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "ally_requests_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AllyRequestsDataMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            int count = msg.requestIds.size();
            buf.writeVarInt(count);
            for (int i = 0; i < count; i++) {
                buf.writeUtf(msg.requestIds.get(i));
                buf.writeUtf(msg.requestNames.get(i));
            }
        },
        (buf) -> {
            int count = buf.readVarInt();
            List<String> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ids.add(buf.readUtf());
                names.add(buf.readUtf());
            }
            return new AllyRequestsDataMessage(ids, names);
        }
    );

    @Override
    public Type<AllyRequestsDataMessage> type() { return TYPE; }

    public static void handleData(final AllyRequestsDataMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> handleClient(message));
        }
    }

    private static void handleClient(AllyRequestsDataMessage msg) {
        // Stocker les données dans le cache client
        fr.eriniumgroup.erinium_faction.gui.screens.components.AllyRequestsCache.set(msg.requestIds, msg.requestNames);

        // Notifier le popup (qui fermera s'il n'y a plus de demandes)
        fr.eriniumgroup.erinium_faction.gui.screens.components.AllyRequestsCache.notifyPopup();
    }

    public static void sendTo(ServerPlayer sp, List<String> requestIds, List<String> requestNames) {
        sp.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(
            new AllyRequestsDataMessage(requestIds, requestNames)
        ));
    }
}
