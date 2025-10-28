package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.TransactionHistory;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionClientData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Serveur -> Client: Synchroniser l'historique des transactions bancaires
 */
public record SyncTransactionHistoryMessage(List<TransactionData> transactions) implements CustomPacketPayload {
    public static final Type<SyncTransactionHistoryMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "sync_transaction_history"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTransactionHistoryMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            buf.writeVarInt(msg.transactions.size());
            for (TransactionData data : msg.transactions) {
                buf.writeUtf(data.type);
                buf.writeUtf(data.playerName);
                buf.writeVarLong(data.amount);
                buf.writeVarLong(data.timestamp);
            }
        },
        (buf) -> {
            int size = buf.readVarInt();
            List<TransactionData> transactions = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                String type = buf.readUtf();
                String playerName = buf.readUtf();
                long amount = buf.readVarLong();
                long timestamp = buf.readVarLong();
                transactions.add(new TransactionData(type, playerName, amount, timestamp));
            }
            return new SyncTransactionHistoryMessage(transactions);
        }
    );

    @Override
    public Type<SyncTransactionHistoryMessage> type() {
        return TYPE;
    }

    public static class TransactionData {
        public final String type; // "DEPOSIT" ou "WITHDRAW"
        public final String playerName;
        public final long amount;
        public final long timestamp;

        public TransactionData(String type, String playerName, long amount, long timestamp) {
            this.type = type;
            this.playerName = playerName;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    public static void handleData(final SyncTransactionHistoryMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> {
                // Stocker l'historique dans FactionClientData
                FactionClientData.setTransactionHistory(message.transactions);
            });
        }
    }

    /**
     * Envoie l'historique des transactions à un joueur
     */
    public static void sendTo(ServerPlayer player, TransactionHistory history) {
        if (history == null) {
            player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(
                new SyncTransactionHistoryMessage(new ArrayList<>())
            ));
            return;
        }

        List<TransactionData> data = new ArrayList<>();
        for (TransactionHistory.Transaction transaction : history.getRecentTransactions(20)) {
            data.add(new TransactionData(
                transaction.getType().name(),
                transaction.getPlayerName(),
                transaction.getAmount(),
                transaction.getTimestamp()
            ));
        }

        player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(
            new SyncTransactionHistoryMessage(data)
        ));
    }
}
