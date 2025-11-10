package fr.eriniumgroup.erinium_faction.features.kits;

import fr.eriniumgroup.erinium_faction.common.config.KitConfig;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.common.config.KitConfig;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire central des kits
 */
public class KitManager {
    private static KitManager instance;

    private final Map<String, Kit> kits = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>(); // playerId -> (kitId -> lastUseTime)

    private KitManager() {}

    public static KitManager getInstance() {
        if (instance == null) {
            instance = new KitManager();
        }
        return instance;
    }

    /**
     * Charge tous les kits depuis la config
     */
    public void loadKits() {
        kits.clear();
        List<Kit> loadedKits = KitConfig.parseKits();

        for (Kit kit : loadedKits) {
            kits.put(kit.getId().toLowerCase(), kit);
        }

        EFC.log.debug("§9§nKit","§d" + kits.size() + " §7kit(s) §achargé(s)");
    }

    /**
     * Enregistre un kit manuellement (pour les addons)
     */
    public void registerKit(Kit kit) {
        kits.put(kit.getId().toLowerCase(), kit);
        EFC.log.debug("§9§nKit","§7Kit §aenregistré: §b" + kit.getId());
    }

    /**
     * Récupère un kit par son ID
     */
    public Optional<Kit> getKit(String id) {
        return Optional.ofNullable(kits.get(id.toLowerCase()));
    }

    /**
     * Récupère tous les kits disponibles pour un joueur
     */
    public List<Kit> getAvailableKits(ServerPlayer player) {
        List<Kit> available = new ArrayList<>();
        UUID playerId = player.getUUID();

        for (Kit kit : kits.values()) {
            if (canUseKit(player, kit)) {
                available.add(kit);
            }
        }

        return available;
    }

    /**
     * Vérifie si un joueur peut utiliser un kit (permission de rank)
     */
    public boolean canUseKit(ServerPlayer player, Kit kit) {
        // Si pas de rank requis, accessible à tous
        if (!kit.hasRequiredRank()) {
            return true;
        }

        // Vérifier le rank du joueur via EFRManager
        UUID playerId = player.getUUID();
        EFRManager.Rank playerRank = EFRManager.get().getPlayerRank(playerId);

        if (playerRank == null) {
            return false; // Pas de rank = pas accès aux kits avec rank requis
        }

        // Vérifier si le joueur a le rank requis ou un rank supérieur
        String requiredRankName = kit.getRequiredRank().toLowerCase();
        EFRManager.Rank requiredRank = EFRManager.get().getRank(requiredRankName);

        if (requiredRank == null) {
            EFC.log.error("§9§nKit","§eRank §cinconnu pour kit '§n" + kit.getId() + "§r§c': §4" + kit.getRequiredRank());
            return false;
        }

        // Comparer la priorité des ranks
        return playerRank.priority >= requiredRank.priority;
    }

    /**
     * Vérifie le cooldown d'un kit pour un joueur
     * @return Le temps restant en secondes, ou 0 si pas de cooldown
     */
    public long getCooldownRemaining(UUID playerId, String kitId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return 0;

        Long lastUse = playerCooldowns.get(kitId.toLowerCase());
        if (lastUse == null) return 0;

        // Récupérer le cooldown du kit
        Optional<Kit> kitOpt = getKit(kitId);
        if (kitOpt.isEmpty()) return 0;

        long cooldownSeconds = kitOpt.get().getCooldownSeconds();
        if (cooldownSeconds == 0) return 0; // Pas de cooldown

        long elapsedSeconds = (System.currentTimeMillis() - lastUse) / 1000;
        long remaining = cooldownSeconds - elapsedSeconds;

        return Math.max(0, remaining);
    }

    /**
     * Donne un kit à un joueur
     * @return 0 = succès, 1 = pas de permission, 2 = cooldown, 3 = pas assez de place
     */
    public int giveKit(ServerPlayer player, String kitId, ServerLevel level) {
        Optional<Kit> kitOpt = getKit(kitId);

        if (kitOpt.isEmpty()) {
            return -1; // Kit introuvable
        }

        Kit kit = kitOpt.get();

        // Vérifier les permissions
        if (!canUseKit(player, kit)) {
            return 1; // Pas de permission
        }

        // Vérifier le cooldown
        UUID playerId = player.getUUID();
        long cooldownRemaining = getCooldownRemaining(playerId, kitId);
        if (cooldownRemaining > 0) {
            return 2; // En cooldown
        }

        // Vérifier la place disponible
        if (!kit.canReceive(player, level)) {
            return 3; // Pas assez de place
        }

        // Donner le kit
        boolean success = kit.give(player, level);
        if (!success) {
            return 3; // Erreur lors du don (pas assez de place)
        }

        // Mettre à jour le cooldown
        cooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(kitId.toLowerCase(), System.currentTimeMillis());

        return 0; // Succès
    }

    /**
     * Formate un temps en secondes en format lisible
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + "m " + secs + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }

    /**
     * Récupère tous les kits enregistrés
     */
    public Collection<Kit> getAllKits() {
        return kits.values();
    }

    /**
     * Reset le cooldown d'un kit spécifique pour un joueur
     */
    public boolean resetCooldown(UUID playerId, String kitId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return false;

        return playerCooldowns.remove(kitId.toLowerCase()) != null;
    }

    /**
     * Reset tous les cooldowns d'un joueur
     */
    public void resetAllCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }

    /**
     * Reset un kit spécifique pour tous les joueurs
     */
    public int resetKitForAll(String kitId) {
        int count = 0;
        String kitIdLower = kitId.toLowerCase();

        for (Map<String, Long> playerCooldowns : cooldowns.values()) {
            if (playerCooldowns.remove(kitIdLower) != null) {
                count++;
            }
        }

        return count;
    }

    /**
     * Reset tous les cooldowns de tous les joueurs
     */
    public void resetAllCooldownsGlobal() {
        cooldowns.clear();
    }

    /**
     * Nettoie toutes les données
     */
    public void clear() {
        kits.clear();
        cooldowns.clear();
    }
}
