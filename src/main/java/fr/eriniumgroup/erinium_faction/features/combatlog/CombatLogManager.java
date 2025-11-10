package fr.eriniumgroup.erinium_faction.features.combatlog;

import fr.eriniumgroup.erinium_faction.common.config.CombatLogConfig;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire principal du système de combat logging
 */
public class CombatLogManager {
    private static CombatLogManager instance;

    private final Map<UUID, CombatTagData> taggedPlayers = new ConcurrentHashMap<>();

    private CombatLogManager() {}

    public static CombatLogManager getInstance() {
        if (instance == null) {
            instance = new CombatLogManager();
        }
        return instance;
    }

    /**
     * Tag un joueur en combat
     */
    public void tagPlayer(ServerPlayer player, ServerPlayer attacker) {
        // Vérifier si c'est du PvP (pas du PvE)
        if (player == null || attacker == null) return;
        if (player.getUUID().equals(attacker.getUUID())) return;

        // Vérifier si le joueur est dans son territoire de faction
        boolean inTerritory = isInOwnFactionTerritory(player);

        // Tag les deux joueurs
        tagSinglePlayer(player, attacker.getUUID(), inTerritory);
        tagSinglePlayer(attacker, player.getUUID(), isInOwnFactionTerritory(attacker));
    }

    private void tagSinglePlayer(ServerPlayer player, UUID attackerId, boolean inTerritory) {
        UUID playerId = player.getUUID();

        CombatTagData data = taggedPlayers.computeIfAbsent(playerId, CombatTagData::new);
        data.tag(attackerId, inTerritory);

        // Notifier le joueur
        int duration = inTerritory ? 10 : 15;
        Component message = Component.literal("⚔ EN COMBAT - " + duration + "s")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }

    /**
     * Vérifie si un joueur est dans son propre territoire de faction
     */
    private boolean isInOwnFactionTerritory(ServerPlayer player) {
        Faction playerFaction = FactionManager.getFactionOf(player.getUUID());
        if (playerFaction == null) return false;

        ClaimKey claimKey = ClaimKey.of(
                player.level().dimension(),
                player.chunkPosition().x,
                player.chunkPosition().z
        );
        String ownerFactionId = FactionManager.getClaimOwner(claimKey);

        if (ownerFactionId == null || ownerFactionId.equals("wilderness")) return false;

        return playerFaction.getId().equals(ownerFactionId);
    }

    /**
     * Vérifie si un joueur est tagué
     */
    public boolean isTagged(UUID playerId) {
        CombatTagData data = taggedPlayers.get(playerId);
        if (data == null) return false;

        boolean tagged = data.isTagged();
        if (!tagged) {
            taggedPlayers.remove(playerId);
        }
        return tagged;
    }

    /**
     * Obtient le temps restant de combat tag
     */
    public int getRemainingSeconds(UUID playerId) {
        CombatTagData data = taggedPlayers.get(playerId);
        return data != null ? data.getRemainingSeconds() : 0;
    }

    /**
     * Gère la déconnexion d'un joueur
     */
    public void handlePlayerDisconnect(ServerPlayer player) {
        UUID playerId = player.getUUID();

        // Si le joueur est tagué en combat, il perd tout
        if (isTagged(playerId)) {
            System.out.println("§c[COMBAT LOG] Player " + player.getName().getString() + " disconnected while in combat!");

            // Le joueur meurt (kill)
            player.kill();

            // Retirer le tag
            taggedPlayers.remove(playerId);
        }
    }


    /**
     * Met à jour les action bars des joueurs tagués
     */
    public void tick(ServerLevel level) {
        List<UUID> toRemove = new ArrayList<>();

        // Mettre à jour les combat tags
        for (Map.Entry<UUID, CombatTagData> entry : taggedPlayers.entrySet()) {
            UUID playerId = entry.getKey();
            CombatTagData data = entry.getValue();

            if (!data.isTagged()) {
                toRemove.add(playerId);
                continue;
            }

            // Mettre à jour l'action bar
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player != null && CombatLogConfig.SHOW_ACTION_BAR.get()) {
                int remaining = data.getRemainingSeconds();
                Component message = Component.literal("⚔ EN COMBAT - " + remaining + "s")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
                player.connection.send(new ClientboundSetActionBarTextPacket(message));
            }
        }

        // Nettoyer les tags expirés
        toRemove.forEach(taggedPlayers::remove);
    }

    /**
     * Vérifie si une commande est bloquée pendant le combat
     */
    public boolean isCommandBlocked(String command) {
        List<? extends String> blockedCommands = CombatLogConfig.BLOCKED_COMMANDS.get();

        // Normaliser la commande (enlever le / si présent)
        String normalizedCommand = command.startsWith("/") ? command.substring(1) : command;

        // Extraire juste le nom de la commande (avant les espaces/arguments)
        String commandName = normalizedCommand.split(" ")[0].toLowerCase();

        // Vérifier si la commande est dans la liste
        for (String blocked : blockedCommands) {
            if (commandName.equals(blocked.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Nettoie toutes les données
     */
    public void clear() {
        taggedPlayers.clear();
    }

    public CombatTagData getTagData(UUID playerId) {
        return taggedPlayers.get(playerId);
    }
}
