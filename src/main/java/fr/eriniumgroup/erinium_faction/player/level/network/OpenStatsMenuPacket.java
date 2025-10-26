package fr.eriniumgroup.erinium_faction.player.level.network;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Paquet pour demander l'ouverture du menu Stats (Client -> Serveur)
 */
public record OpenStatsMenuPacket() implements CustomPacketPayload {

    public static final Type<OpenStatsMenuPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "open_stats_menu")
    );

    public static final StreamCodec<ByteBuf, OpenStatsMenuPacket> STREAM_CODEC = StreamCodec.unit(new OpenStatsMenuPacket());

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}