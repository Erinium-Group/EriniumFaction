package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.features.bounty.Bounty;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Packet pour synchroniser les données de bounty avec le client
 */
public record BountyDataPacket(List<BountyEntry> bounties) implements CustomPacketPayload {
    public static final Type<BountyDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "bounty_data"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, BountyDataPacket> STREAM_CODEC = StreamCodec.composite(
            BountyEntry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BountyDataPacket::bounties,
            BountyDataPacket::new
    );

    public static void handleData(final BountyDataPacket msg, final IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND) return;
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ctx.enqueueWork(() -> ClientHandler.handleBountyData(msg.bounties));
        }
    }

    private static class ClientHandler {
        static void handleBountyData(List<BountyEntry> bounties) {
            fr.eriniumgroup.erinium_faction.client.gui.bounty.BountyClientData.setBounties(bounties);
        }
    }

    /**
     * Entrée de bounty simplifiée pour le réseau
     */
    public record BountyEntry(
            UUID targetId,
            String targetName,
            double totalAmount,
            long timeRemaining
    ) {
        public static final StreamCodec<RegistryFriendlyByteBuf, BountyEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC),
                BountyEntry::targetId,
                ByteBufCodecs.STRING_UTF8,
                BountyEntry::targetName,
                ByteBufCodecs.DOUBLE,
                BountyEntry::totalAmount,
                ByteBufCodecs.VAR_LONG,
                BountyEntry::timeRemaining,
                BountyEntry::new
        );

        public static BountyEntry fromBounty(Bounty bounty) {
            return new BountyEntry(
                    bounty.getTargetId(),
                    bounty.getTargetName(),
                    bounty.getTotalAmount(),
                    bounty.getTimeRemaining()
            );
        }
    }

    public static BountyDataPacket fromBounties(List<Bounty> bounties) {
        List<BountyEntry> entries = new ArrayList<>();
        for (Bounty bounty : bounties) {
            entries.add(BountyEntry.fromBounty(bounty));
        }
        return new BountyDataPacket(entries);
    }
}
