package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Paquet pour synchroniser si le joueur a un token de reset (Serveur -> Client)
 */
public record SyncResetTokenPacket(boolean hasToken) implements CustomPacketPayload {

    public static final Type<SyncResetTokenPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_reset_token")
    );

    public static final StreamCodec<ByteBuf, SyncResetTokenPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        SyncResetTokenPacket::hasToken,
        SyncResetTokenPacket::new
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

