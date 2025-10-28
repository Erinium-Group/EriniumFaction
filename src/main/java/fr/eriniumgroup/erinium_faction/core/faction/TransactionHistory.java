package fr.eriniumgroup.erinium_faction.core.faction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Historique des transactions bancaires d'une faction
 */
public class TransactionHistory {
    private static final int MAX_HISTORY_SIZE = 50; // Garder les 50 dernières transactions
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Transaction> transactions = new ArrayList<>();

    public enum TransactionType {
        DEPOSIT,
        WITHDRAW
    }

    public static class Transaction {
        private final TransactionType type;
        private final String playerName;
        private final long amount;
        private final long timestamp;

        public Transaction(TransactionType type, String playerName, long amount, long timestamp) {
            this.type = type;
            this.playerName = playerName;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        public TransactionType getType() {
            return type;
        }

        public String getPlayerName() {
            return playerName;
        }

        public long getAmount() {
            return amount;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getFormattedTimestamp() {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            return dateTime.format(FORMATTER);
        }
    }

    /**
     * Ajoute une transaction à l'historique
     */
    public void addTransaction(TransactionType type, String playerName, long amount) {
        transactions.add(0, new Transaction(type, playerName, amount, System.currentTimeMillis()));

        // Garder seulement les MAX_HISTORY_SIZE dernières transactions
        if (transactions.size() > MAX_HISTORY_SIZE) {
            transactions.remove(transactions.size() - 1);
        }
    }

    /**
     * Récupère toutes les transactions
     */
    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Récupère les N dernières transactions
     */
    public List<Transaction> getRecentTransactions(int count) {
        int limit = Math.min(count, transactions.size());
        return new ArrayList<>(transactions.subList(0, limit));
    }

    /**
     * Efface tout l'historique
     */
    public void clear() {
        transactions.clear();
    }

    /**
     * Nombre de transactions dans l'historique
     */
    public int size() {
        return transactions.size();
    }

    /**
     * Sauvegarde l'historique en NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Transaction t : transactions) {
            CompoundTag tTag = new CompoundTag();
            tTag.putString("type", t.type.name());
            tTag.putString("player", t.playerName);
            tTag.putLong("amount", t.amount);
            tTag.putLong("timestamp", t.timestamp);
            list.add(tTag);
        }
        tag.put("transactions", list);
        return tag;
    }

    /**
     * Charge l'historique depuis NBT
     */
    public static TransactionHistory load(CompoundTag tag) {
        TransactionHistory history = new TransactionHistory();
        if (tag.contains("transactions")) {
            ListTag list = tag.getList("transactions", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tTag = list.getCompound(i);
                TransactionType type = TransactionType.valueOf(tTag.getString("type"));
                String player = tTag.getString("player");
                long amount = tTag.getLong("amount");
                long timestamp = tTag.getLong("timestamp");
                history.transactions.add(new Transaction(type, player, amount, timestamp));
            }
        }
        return history;
    }
}
