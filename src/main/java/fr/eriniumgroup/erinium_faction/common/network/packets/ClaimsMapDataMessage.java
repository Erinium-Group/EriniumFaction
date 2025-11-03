package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serveur -> Client: envoie la grille des claims autour d'un chunk.
 */
public record ClaimsMapDataMessage(String dimension, int centerCx, int centerCz, int radius, int[] relCx, int[] relCz, String[] owners) implements CustomPacketPayload {
    public static final Type<ClaimsMapDataMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "claims_map_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimsMapDataMessage> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeUtf(msg.dimension);
        buf.writeVarInt(msg.centerCx);
        buf.writeVarInt(msg.centerCz);
        buf.writeVarInt(msg.radius);
        int n = msg.relCx.length;
        buf.writeVarInt(n);
        for (int i = 0; i < n; i++) {
            buf.writeVarInt(msg.relCx[i]);
            buf.writeVarInt(msg.relCz[i]);
            buf.writeUtf(msg.owners[i]);
        }
    }, (buf) -> {
        String dim = buf.readUtf();
        int cx = buf.readVarInt();
        int cz = buf.readVarInt();
        int r = buf.readVarInt();
        int n = buf.readVarInt();
        int[] rxs = new int[n];
        int[] rzs = new int[n];
        String[] owners = new String[n];
        for (int i = 0; i < n; i++) {
            rxs[i] = buf.readVarInt();
            rzs[i] = buf.readVarInt();
            owners[i] = buf.readUtf();
        }
        return new ClaimsMapDataMessage(dim, cx, cz, r, rxs, rzs, owners);
    });

    @Override
    public Type<ClaimsMapDataMessage> type() { return TYPE; }

    public static void handleData(final ClaimsMapDataMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> {
                // Mettre à jour FactionMapScreen
                fr.eriniumgroup.erinium_faction.gui.screens.FactionMapScreen.onMapData(message);
            });
        }
    }

    public static void sendTo(ServerPlayer sp, String dim, int centerCx, int centerCz, int radius, List<Map.Entry<ClaimKey, String>> entries) {
        // Filtrer pour n'envoyer QUE les chunks qui ont vraiment un owner
        List<Map.Entry<ClaimKey, String>> validClaims = new ArrayList<>();
        for (var e : entries) {
            String ownerId = e.getValue();
            if (ownerId != null && !ownerId.isBlank()) {
                validClaims.add(e);
            }
        }

        int n = validClaims.size();
        int[] rx = new int[n];
        int[] rz = new int[n];
        String[] owners = new String[n];

        for (int i = 0; i < n; i++) {
            var e = validClaims.get(i);
            rx[i] = e.getKey().chunkX() - centerCx;
            rz[i] = e.getKey().chunkZ() - centerCz;
            String ownerId = e.getValue();

            // Récupérer le nom de faction
            var f = fr.eriniumgroup.erinium_faction.core.faction.FactionManager.getFaction(ownerId);
            owners[i] = (f != null) ? f.getName() : ownerId;
        }

        sp.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(new ClaimsMapDataMessage(dim, centerCx, centerCz, radius, rx, rz, owners)));
    }
}
