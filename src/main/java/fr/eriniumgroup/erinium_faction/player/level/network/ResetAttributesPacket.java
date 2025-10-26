package fr.eriniumgroup.erinium_faction.player.level.network;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Paquet pour réinitialiser les attributs (Client -> Serveur)
 */
public record ResetAttributesPacket() implements CustomPacketPayload {

    public static final Type<ResetAttributesPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "reset_attributes")
    );

    public static final StreamCodec<ByteBuf, ResetAttributesPacket> STREAM_CODEC = StreamCodec.unit(new ResetAttributesPacket());

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

