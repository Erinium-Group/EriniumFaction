package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.client.data.PlayerRankCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Packet pour synchroniser le rang d'un joueur vers les clients
 */
public record SyncPlayerRankPacket(
        UUID playerUUID,
        String rankId,
        String displayName,
        String prefix,
        String suffix,
        int priority
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncPlayerRankPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_player_rank")
    );

    public static final StreamCodec<ByteBuf, SyncPlayerRankPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            SyncPlayerRankPacket::playerUUID,
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerRankPacket::rankId,
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerRankPacket::displayName,
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerRankPacket::prefix,
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerRankPacket::suffix,
            ByteBufCodecs.INT,
            SyncPlayerRankPacket::priority,
            SyncPlayerRankPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handler côté client
     */
    public static void handleData(SyncPlayerRankPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerRankCache.updateRank(
                    packet.playerUUID,
                    packet.rankId,
                    packet.displayName,
                    packet.prefix,
                    packet.suffix,
                    packet.priority
            );
        });
    }
}
