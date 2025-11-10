package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.features.bounty.BountyManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Packet pour placer une bounty (client -> serveur)
 */
public record PlaceBountyPacket(UUID targetId, String targetName, double amount) implements CustomPacketPayload {
    public static final Type<PlaceBountyPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "place_bounty"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceBountyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC),
            PlaceBountyPacket::targetId,
            ByteBufCodecs.STRING_UTF8,
            PlaceBountyPacket::targetName,
            ByteBufCodecs.DOUBLE,
            PlaceBountyPacket::amount,
            PlaceBountyPacket::new
    );

    public static void handleData(final PlaceBountyPacket msg, final IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.SERVERBOUND) return;

        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sender) {
                BountyManager manager = BountyManager.get(sender.getServer());
                int result = manager.placeBounty(sender, msg.targetId, msg.targetName, msg.amount);

                switch (result) {
                    case 0 -> {
                        double commission = msg.amount * fr.eriniumgroup.erinium_faction.features.bounty.BountyConfig.get().getCommissionRate();
                        sender.sendSystemMessage(Component.translatable("erinium_faction.bounty.placed.success",
                                msg.targetName,
                                String.format("%.2f", msg.amount),
                                String.format("%.2f", commission)));

                        // Notifier la cible
                        ServerPlayer target = sender.getServer().getPlayerList().getPlayer(msg.targetId);
                        if (target != null) {
                            target.sendSystemMessage(Component.translatable("erinium_faction.bounty.placed.target_notif",
                                    String.format("%.2f", msg.amount)));
                        }
                    }
                    case 1 -> sender.sendSystemMessage(Component.translatable("erinium_faction.bounty.placed.min_amount",
                            String.format("%.2f", fr.eriniumgroup.erinium_faction.features.bounty.BountyConfig.get().getMinimumBounty())));
                    case 2 -> {
                        double totalCost = msg.amount + (msg.amount * fr.eriniumgroup.erinium_faction.features.bounty.BountyConfig.get().getCommissionRate());
                        sender.sendSystemMessage(Component.translatable("erinium_faction.bounty.placed.insufficient_funds",
                                String.format("%.2f", totalCost)));
                    }
                    case 3 -> sender.sendSystemMessage(Component.translatable("erinium_faction.bounty.placed.failed"));
                }
            }
        });
    }
}
