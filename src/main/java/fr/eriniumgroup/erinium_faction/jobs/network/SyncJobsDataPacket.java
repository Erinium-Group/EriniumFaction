package fr.eriniumgroup.erinium_faction.jobs.network;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.jobs.JobsData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Paquet pour synchroniser les données de métiers du serveur vers le client
 */
public record SyncJobsDataPacket(JobsData data) implements CustomPacketPayload {

    public static final Type<SyncJobsDataPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_jobs_data")
    );

    public static final StreamCodec<ByteBuf, SyncJobsDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Nonnull
        @Override
        public SyncJobsDataPacket decode(@Nonnull ByteBuf buffer) {
            CompoundTag tag = ByteBufCodecs.COMPOUND_TAG.decode(buffer);
            JobsData data = new JobsData();
            data.deserializeNBT(tag);
            return new SyncJobsDataPacket(data);
        }

        @Override
        public void encode(@Nonnull ByteBuf buffer, @Nonnull SyncJobsDataPacket packet) {
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
