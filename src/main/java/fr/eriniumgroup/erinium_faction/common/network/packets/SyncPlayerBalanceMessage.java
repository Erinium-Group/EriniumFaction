package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionClientData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Serveur -> Client: Synchroniser le solde du joueur
 */
public record SyncPlayerBalanceMessage(double balance) implements CustomPacketPayload {
    public static final Type<SyncPlayerBalanceMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "sync_player_balance"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerBalanceMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> buf.writeDouble(msg.balance),
        (buf) -> new SyncPlayerBalanceMessage(buf.readDouble())
    );

    @Override
    public Type<SyncPlayerBalanceMessage> type() {
        return TYPE;
    }

    public static void handleData(final SyncPlayerBalanceMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> {
                // Mettre à jour le solde côté client
                FactionClientData.setPlayerBalance(message.balance);
            });
        }
    }

    /**
     * Envoie le solde au joueur
     */
    public static void sendTo(ServerPlayer player, double balance) {
        PacketDistributor.sendToPlayer(player, new SyncPlayerBalanceMessage(balance));
    }
}
