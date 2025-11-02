package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.jobs.JobType;
import fr.eriniumgroup.erinium_faction.jobs.gui.JobToastOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet pour afficher une notification toast côté client
 */
public record ShowJobToastPacket(
    JobType jobType,
    int level,
    int xpGained,
    String actionDescription,
    int currentXp,
    int xpToNextLevel,
    boolean isLevelUp
) implements CustomPacketPayload {

    public static final Type<ShowJobToastPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath("erinium_faction", "show_job_toast")
    );

    public static final StreamCodec<ByteBuf, ShowJobToastPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ShowJobToastPacket decode(ByteBuf buffer) {
            int jobTypeOrdinal = VarInt.read(buffer);
            JobType jobType = JobType.byId(jobTypeOrdinal);
            int level = VarInt.read(buffer);
            int xpGained = VarInt.read(buffer);
            String actionDescription = ByteBufCodecs.STRING_UTF8.decode(buffer);
            int currentXp = VarInt.read(buffer);
            int xpToNextLevel = VarInt.read(buffer);
            boolean isLevelUp = buffer.readBoolean();
            return new ShowJobToastPacket(jobType, level, xpGained, actionDescription, currentXp, xpToNextLevel, isLevelUp);
        }

        @Override
        public void encode(ByteBuf buffer, ShowJobToastPacket packet) {
            VarInt.write(buffer, packet.jobType.ordinal());
            VarInt.write(buffer, packet.level);
            VarInt.write(buffer, packet.xpGained);
            ByteBufCodecs.STRING_UTF8.encode(buffer, packet.actionDescription);
            VarInt.write(buffer, packet.currentXp);
            VarInt.write(buffer, packet.xpToNextLevel);
            buffer.writeBoolean(packet.isLevelUp);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handler pour ce packet côté client
     */
    public static void handle(ShowJobToastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.isLevelUp) {
                JobToastOverlay.showLevelUp(
                    packet.jobType,
                    packet.level,
                    packet.currentXp,
                    packet.xpToNextLevel
                );
            } else {
                JobToastOverlay.showXpGain(
                    packet.jobType,
                    packet.level,
                    packet.xpGained,
                    packet.actionDescription,
                    packet.currentXp,
                    packet.xpToNextLevel
                );
            }
        });
    }
}
