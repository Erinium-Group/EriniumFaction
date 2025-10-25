package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Serveur: demande des claims dans un rayon autour d'un chunk.
 */
public record ClaimsMapRequestMessage(String dimension, int centerCx, int centerCz, int radius) implements CustomPacketPayload {
    public static final Type<ClaimsMapRequestMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "claims_map_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimsMapRequestMessage> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeUtf(msg.dimension);
        buf.writeVarInt(msg.centerCx);
        buf.writeVarInt(msg.centerCz);
        buf.writeVarInt(msg.radius);
    }, (buf) -> new ClaimsMapRequestMessage(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

    @Override
    public Type<ClaimsMapRequestMessage> type() { return TYPE; }

    public static void handleData(final ClaimsMapRequestMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            ctx.enqueueWork(() -> handleServer(message, (ServerPlayer) ctx.player())).exceptionally(e -> {
                ctx.connection().disconnect(net.minecraft.network.chat.Component.literal(e.getMessage()));
                return null;
            });
        }
    }

    private static void handleServer(ClaimsMapRequestMessage msg, ServerPlayer sp) {
        if (sp == null) return;
        Level lvl = sp.level();
        // sécurité: limiter le rayon
        int radius = Math.max(1, Math.min(32, msg.radius()));
        // Vérifier le chunk center est chargé pour éviter abus
        BlockPos anyPos = new BlockPos((msg.centerCx() << 4) + 8, sp.getBlockY(), (msg.centerCz() << 4) + 8);
        if (!lvl.isLoaded(anyPos)) return;
        // Récupérer les claims
        MinecraftServer server = sp.server;
        var data = fr.eriniumgroup.erinium_faction.core.claim.ClaimsSavedData.get(server);
        int minCx = msg.centerCx() - radius;
        int minCz = msg.centerCz() - radius;
        int maxCx = msg.centerCx() + radius;
        int maxCz = msg.centerCz() + radius;
        var list = data.listInArea(msg.dimension(), minCx, minCz, maxCx, maxCz);
        // Répondre
        ClaimsMapDataMessage.sendTo(sp, msg.dimension(), msg.centerCx(), msg.centerCz(), radius, list);
    }
}

