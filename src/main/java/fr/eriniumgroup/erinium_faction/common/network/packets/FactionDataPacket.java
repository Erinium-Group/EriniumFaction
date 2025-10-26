package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Paquet pour synchroniser les données de faction du serveur vers le client
 */
public record FactionDataPacket(FactionSnapshot snapshot) implements CustomPacketPayload {

    public static final Type<FactionDataPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "faction_data")
    );

    public static final StreamCodec<ByteBuf, FactionDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Nonnull
        @Override
        public FactionDataPacket decode(@Nonnull ByteBuf buffer) {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
            FactionSnapshot snapshot = FactionSnapshot.read(buf);
            return new FactionDataPacket(snapshot);
        }

        @Override
        public void encode(@Nonnull ByteBuf buffer, @Nonnull FactionDataPacket packet) {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
            FactionSnapshot.write(packet.snapshot, buf);
        }
    };

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
