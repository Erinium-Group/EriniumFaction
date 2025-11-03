package fr.eriniumgroup.erinium_faction.features.minimap;

import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimsSavedData;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Helper pour le rendu des claims sur la minimap
 */
public class ClaimRenderHelper {

    // Cache côté client des claims visibles
    private static final Map<ChunkPos, ClaimInfo> claimCache = new HashMap<>();
    private static long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 5000; // 5 secondes

    // Cache des couleurs par faction pour cohérence et performance
    private static final Map<String, Integer> factionColorCache = new HashMap<>();

    // Listener pour notifier les changements de claims
    private static Runnable claimChangeListener = null;

    /**
     * Obtient les informations de claim pour un chunk
     */
    public static ClaimInfo getClaimInfo(ChunkPos chunk, String dimension) {
        return claimCache.get(chunk);
    }

    /**
     * Enregistrer un listener pour être notifié des changements de claims
     */
    public static void setClaimChangeListener(Runnable listener) {
        claimChangeListener = listener;
    }

    /**
     * Met à jour le cache des claims (appelé depuis un paquet réseau)
     */
    public static void updateClaimCache(ChunkPos chunk, ClaimInfo info) {
        if (info == null) {
            claimCache.remove(chunk);
        } else {
            claimCache.put(chunk, info);
        }
        lastCacheUpdate = System.currentTimeMillis();

        // Notifier le listener du changement
        if (claimChangeListener != null) {
            claimChangeListener.run();
        }
    }

    /**
     * Met à jour le cache avec plusieurs claims depuis le packet réseau
     * IMPORTANT: Remplace COMPLÈTEMENT le cache (clear puis rebuild)
     */
    public static void updateClaimsFromPacket(String dimension, int centerCx, int centerCz, int[] relCx, int[] relCz, String[] owners) {
        // CLEAR COMPLET du cache pour éviter les données obsolètes
        claimCache.clear();

        // Reconstruire le cache avec les données du serveur
        for (int i = 0; i < relCx.length; i++) {
            int chunkX = centerCx + relCx[i];
            int chunkZ = centerCz + relCz[i];
            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
            String owner = owners[i];

            // Ajouter SEULEMENT si owner n'est pas vide ET pas "wilderness"
            if (owner != null && !owner.isEmpty() && !owner.isBlank() && !owner.equalsIgnoreCase("wilderness")) {
                ClaimInfo info = new ClaimInfo(owner, owner, chunkPos, dimension);
                claimCache.put(chunkPos, info);
            }
        }
        lastCacheUpdate = System.currentTimeMillis();

        // Notifier le listener du changement
        if (claimChangeListener != null) {
            claimChangeListener.run();
        }
    }

    /**
     * Nettoie le cache
     */
    public static void clearCache() {
        claimCache.clear();
    }

    /**
     * Retourne tous les claims du cache (pour parcourir sans limite de rayon)
     */
    public static Map<ChunkPos, ClaimInfo> getAllClaims() {
        return claimCache;
    }

    /**
     * Obtient le type de relation avec un claim
     */
    public static ClaimRelation getClaimRelation(ClaimInfo claim, LocalPlayer player) {
        if (claim == null) {
            return ClaimRelation.WILDERNESS;
        }

        // Vérifier que le claim a bien un owner valide
        if (claim.factionId() == null || claim.factionId().isEmpty() || claim.factionId().isBlank()) {
            return ClaimRelation.WILDERNESS;
        }

        // Obtenir les données de faction depuis le cache client
        var factionData = fr.eriniumgroup.erinium_faction.gui.screens.FactionClientData.getFactionData();

        if (factionData == null) {
            // Pas de données de faction = tous les claims sont ennemis
            return ClaimRelation.ENEMY;
        }

        // Vérifier si c'est notre faction
        if (claim.factionId().equals(factionData.id)) {
            return ClaimRelation.OWNED;
        }

        // Vérifier si c'est une faction alliée
        if (factionData.allies.contains(claim.factionId())) {
            return ClaimRelation.ALLY;
        }

        // Sinon c'est un ennemi
        return ClaimRelation.ENEMY;
    }

    /**
     * Rendu d'un overlay de claim sur la carte
     */
    public static void renderClaimOverlay(GuiGraphics graphics, int x, int y, int size, ClaimRelation relation) {
        int color = switch (relation) {
            case OWNED -> 0x4000FF00; // Vert semi-transparent
            case ALLY -> 0x400066FF;  // Bleu semi-transparent
            case ENEMY -> 0x40FF0000; // Rouge semi-transparent
            case WILDERNESS -> 0x00000000; // Transparent
        };

        if (color != 0) {
            graphics.fill(x, y, x + size, y + size, color);

            // Bordure
            int borderColor = color | 0xFF000000; // Opaque
            graphics.fill(x, y, x + size, y + 1, borderColor);
            graphics.fill(x, y + size - 1, x + size, y + size, borderColor);
            graphics.fill(x, y, x + 1, y + size, borderColor);
            graphics.fill(x + size - 1, y, x + size, y + size, borderColor);
        }
    }

    /**
     * Rendu dynamique d'un overlay de claim avec couleurs basées sur la faction
     * - Warzone = rouge
     * - Safezone = vert
     * - Our claims = bleu
     * - Other factions = couleur aléatoire liée à la faction
     */
    public static void renderClaimOverlayDynamic(GuiGraphics graphics, int x, int y, int size, ClaimInfo claimInfo, ClaimRelation relation) {
        if (claimInfo == null) return;

        int fillColor;
        int borderColor;
        String factionId = claimInfo.factionId().toLowerCase();

        if ("warzone".equals(factionId)) {
            // Warzone = Rouge pur (RGB: 220, 20, 20)
            fillColor = 0x60DC1414; // Semi-transparent
            borderColor = 0xFFDC1414; // Opaque
        } else if ("safezone".equals(factionId)) {
            // Safezone = Vert pur (RGB: 20, 220, 20)
            fillColor = 0x6014DC14; // Semi-transparent
            borderColor = 0xFF14DC14; // Opaque
        } else {
            // Toutes les autres factions (y compris nos claims) - couleur basée sur factionId
            int baseColor = getFactionColor(factionId);
            fillColor = 0x60000000 | (baseColor & 0x00FFFFFF);
            borderColor = 0xFF000000 | (baseColor & 0x00FFFFFF);
        }

        // Remplir le claim
        graphics.fill(x, y, x + size, y + size, fillColor);

        // Bordure plus visible
        graphics.fill(x, y, x + size, y + 1, borderColor); // Top
        graphics.fill(x, y + size - 1, x + size, y + size, borderColor); // Bottom
        graphics.fill(x, y, x + 1, y + size, borderColor); // Left
        graphics.fill(x + size - 1, y, x + size, y + size, borderColor); // Right
    }

    /**
     * Génère une couleur cohérente et variée basée sur le factionId
     * Utilise le même algorithme que FactionMapScreen.colorForOwner pour cohérence
     */
    private static int getFactionColor(String factionId) {
        if (factionId == null || factionId.isBlank()) {
            return 0x555555; // wilderness
        }

        // Utiliser le cache pour cohérence et performance
        return factionColorCache.computeIfAbsent(factionId.toLowerCase(Locale.ROOT), k -> {
            int h = k.hashCode();
            // Générer RGB avec des valeurs entre 64 et 255 pour éviter les couleurs trop sombres
            int r = 64 + (Math.abs(h) % 192);
            int g = 64 + (Math.abs(h >> 8) % 192);
            int b = 64 + (Math.abs(h >> 16) % 192);
            return (r << 16) | (g << 8) | b;
        });
    }

    /**
     * Rendu d'un pattern pour les claims (pour fullscreen map)
     */
    public static void renderClaimPattern(GuiGraphics graphics, int x, int y, int size, ClaimRelation relation) {
        switch (relation) {
            case OWNED -> {
                // Lignes diagonales vertes
                graphics.fill(x, y, x + size, y + size, 0x2000FF00);
                for (int i = 0; i < size; i += 4) {
                    graphics.fill(x + i, y, x + i + 1, y + size, 0x8000FF00);
                }
            }
            case ALLY -> {
                // Points bleus
                graphics.fill(x, y, x + size, y + size, 0x200066FF);
                for (int px = 0; px < size; px += 4) {
                    for (int pz = 0; pz < size; pz += 4) {
                        graphics.fill(x + px, y + pz, x + px + 2, y + pz + 2, 0x800066FF);
                    }
                }
            }
            case ENEMY -> {
                // Croix rouges
                graphics.fill(x, y, x + size, y + size, 0x20FF0000);
                for (int i = 0; i < size; i += 4) {
                    graphics.fill(x + i, y, x + i + 1, y + size, 0x80FF0000);
                    graphics.fill(x, y + i, x + size, y + i + 1, 0x80FF0000);
                }
            }
            case WILDERNESS -> {
                // Rien
            }
        }
    }

    /**
     * Informations sur un claim
     */
    public record ClaimInfo(
        String factionId,
        String factionName,
        ChunkPos position,
        String dimension
    ) {}

    /**
     * Type de relation avec un claim
     */
    public enum ClaimRelation {
        OWNED,      // Notre faction
        ALLY,       // Faction alliée
        ENEMY,      // Faction ennemie/neutre
        WILDERNESS  // Pas de claim
    }
}
