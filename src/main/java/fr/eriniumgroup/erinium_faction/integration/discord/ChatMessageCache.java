package fr.eriniumgroup.erinium_faction.integration.discord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache pour stocker les derniers messages de chat de chaque joueur
 */
public class ChatMessageCache {

    // Map pour stocker les messages: UUID du joueur -> Queue des 5 derniers messages
    private static final Map<UUID, Deque<ChatMessage>> messageCache = new ConcurrentHashMap<>();

    // Nombre maximum de messages à garder par joueur
    private static final int MAX_MESSAGES_PER_PLAYER = 5;

    /**
     * Ajoute un message au cache
     */
    public static void addMessage(UUID playerUUID, String message) {
        messageCache.computeIfAbsent(playerUUID, k -> new ArrayDeque<>(MAX_MESSAGES_PER_PLAYER))
                .addFirst(new ChatMessage(message, System.currentTimeMillis()));

        // Limiter la taille du cache
        Deque<ChatMessage> queue = messageCache.get(playerUUID);
        while (queue.size() > MAX_MESSAGES_PER_PLAYER) {
            queue.removeLast();
        }
    }

    /**
     * Récupère le dernier message d'un joueur
     */
    public static String getLastMessage(UUID playerUUID) {
        Deque<ChatMessage> messages = messageCache.get(playerUUID);
        if (messages == null || messages.isEmpty()) {
            return "Message non disponible";
        }
        return messages.peekFirst().content;
    }

    /**
     * Récupère tous les messages récents d'un joueur
     */
    public static List<ChatMessage> getRecentMessages(UUID playerUUID) {
        Deque<ChatMessage> messages = messageCache.get(playerUUID);
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(messages);
    }

    /**
     * Nettoie les messages d'un joueur (quand il se déconnecte par exemple)
     */
    public static void clearMessages(UUID playerUUID) {
        messageCache.remove(playerUUID);
    }

    /**
     * Nettoie tous les messages (au démarrage/arrêt du serveur)
     */
    public static void clearAll() {
        messageCache.clear();
    }

    /**
     * Classe pour stocker un message avec son timestamp
     */
    public static class ChatMessage {
        public final String content;
        public final long timestamp;

        public ChatMessage(String content, long timestamp) {
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getFormattedTime() {
            long secondsAgo = (System.currentTimeMillis() - timestamp) / 1000;
            if (secondsAgo < 60) {
                return secondsAgo + "s";
            } else if (secondsAgo < 3600) {
                return (secondsAgo / 60) + "m";
            } else {
                return (secondsAgo / 3600) + "h";
            }
        }
    }
}
