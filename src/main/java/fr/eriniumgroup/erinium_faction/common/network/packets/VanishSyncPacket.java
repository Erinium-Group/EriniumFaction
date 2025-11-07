package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.features.vanish.VanishClientData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Packet pour synchroniser l'état vanish d'un joueur entre serveur et clients.
 */
public record VanishSyncPacket(UUID playerUUID, boolean isVanished) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VanishSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "vanish_sync")
    );

    public static final StreamCodec<ByteBuf, VanishSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            VanishSyncPacket::playerUUID,
            ByteBufCodecs.BOOL,
            VanishSyncPacket::isVanished,
            VanishSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handler côté client
     */
    public static void handleData(VanishSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Mettre à jour l'état vanish côté client
            VanishClientData.setVanished(packet.playerUUID, packet.isVanished);
        });
    }
}
