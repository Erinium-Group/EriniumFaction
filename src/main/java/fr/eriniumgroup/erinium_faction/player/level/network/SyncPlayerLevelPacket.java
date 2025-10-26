package fr.eriniumgroup.erinium_faction.player.level.network;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.player.level.PlayerLevelData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Paquet pour synchroniser les données de niveau du serveur vers le client
 */
public record SyncPlayerLevelPacket(PlayerLevelData data) implements CustomPacketPayload {

    public static final Type<SyncPlayerLevelPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_player_level")
    );

    public static final StreamCodec<ByteBuf, SyncPlayerLevelPacket> STREAM_CODEC = new StreamCodec<>() {
        @Nonnull
        @Override
        public SyncPlayerLevelPacket decode(@Nonnull ByteBuf buffer) {
            CompoundTag tag = ByteBufCodecs.COMPOUND_TAG.decode(buffer);
            PlayerLevelData data = new PlayerLevelData();
            data.deserializeNBT(tag);
            return new SyncPlayerLevelPacket(data);
        }

        @Override
        public void encode(@Nonnull ByteBuf buffer, @Nonnull SyncPlayerLevelPacket packet) {
            CompoundTag tag = packet.data.serializeNBT();
            ByteBufCodecs.COMPOUND_TAG.encode(buffer, tag);
        }
    };

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

