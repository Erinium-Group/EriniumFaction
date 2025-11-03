package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Paquet pour synchroniser les données de métiers du serveur vers le client
 * Transmet les données sous forme de CompoundTag brut pour éviter les problèmes de classloading
 */
public record SyncJobsDataPacket(CompoundTag dataTag) implements CustomPacketPayload {

    public static final Type<SyncJobsDataPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_jobs_data")
    );

    public static final StreamCodec<ByteBuf, SyncJobsDataPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.COMPOUND_TAG,
        SyncJobsDataPacket::dataTag,
        SyncJobsDataPacket::new
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
