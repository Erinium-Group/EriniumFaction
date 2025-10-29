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
 * Serveur -> Client: envoie la liste de toutes les factions
 */
public record FactionListDataMessage(List<String> factionIds, List<String> factionNames, List<Integer> memberCounts) implements CustomPacketPayload {
    public static final Type<FactionListDataMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_list_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FactionListDataMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            int count = msg.factionIds.size();
            buf.writeVarInt(count);
            for (int i = 0; i < count; i++) {
                buf.writeUtf(msg.factionIds.get(i));
                buf.writeUtf(msg.factionNames.get(i));
                buf.writeVarInt(msg.memberCounts.get(i));
            }
        },
        (buf) -> {
            int count = buf.readVarInt();
            List<String> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ids.add(buf.readUtf());
                names.add(buf.readUtf());
                counts.add(buf.readVarInt());
            }
            return new FactionListDataMessage(ids, names, counts);
        }
    );

    @Override
    public Type<FactionListDataMessage> type() { return TYPE; }

    public static void handleData(final FactionListDataMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> handleClient(message));
        }
    }

    private static void handleClient(FactionListDataMessage msg) {
        // Stocker les données dans un cache client accessible par le GUI
        fr.eriniumgroup.erinium_faction.gui.screens.components.FactionListCache.set(msg.factionIds, msg.factionNames, msg.memberCounts);

        // Notifier le popup ouvert (s'il existe) pour qu'il se mette à jour
        fr.eriniumgroup.erinium_faction.gui.screens.components.FactionListCache.notifyPopup();
    }

    public static void sendTo(ServerPlayer sp, List<String> factionIds, List<String> factionNames, List<Integer> memberCounts) {
        sp.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(
            new FactionListDataMessage(factionIds, factionNames, memberCounts)
        ));
    }
}
