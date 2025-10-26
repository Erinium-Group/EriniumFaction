package fr.eriniumgroup.erinium_faction.player.level.network;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Paquet pour distribuer un point d'attribut (Client -> Serveur)
 */
public record DistributePointPacket(PlayerLevelManager.AttributeType attributeType) implements CustomPacketPayload {

    public static final Type<DistributePointPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "distribute_point")
    );

    public static final StreamCodec<ByteBuf, DistributePointPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        packet -> packet.attributeType.ordinal(),
        ordinal -> new DistributePointPacket(PlayerLevelManager.AttributeType.values()[ordinal])
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

