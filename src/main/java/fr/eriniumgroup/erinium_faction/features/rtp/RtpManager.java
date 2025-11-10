package fr.eriniumgroup.erinium_faction.features.rtp;

import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimsSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire de la téléportation aléatoire (RTP)
 * - Cooldown de 30 minutes par dimension
 * - Téléportation entre 5000 et 10000 blocs
 * - Délai de 5 secondes avant téléportation
 */
public class RtpManager {

    private static final int COOLDOWN_MINUTES = 30;
    private static final int COOLDOWN_TICKS = COOLDOWN_MINUTES * 60 * 20; // 30 min en ticks
    private static final int MIN_DISTANCE = 5000;
    private static final int MAX_DISTANCE = 10000;
    private static final int WAIT_SECONDS = 5;
    private static final int WAIT_TICKS = WAIT_SECONDS * 20; // 5 secondes en ticks
    private static final int MAX_ATTEMPTS = 50; // Nombre maximum de tentatives pour trouver un emplacement sûr

    // Map: UUID du joueur -> Map: Dimension -> Timestamp du dernier RTP
    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    // Map: UUID du joueur -> Données de téléportation en attente
    private static final Map<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();

    /**
     * Démarre le processus de RTP pour un joueur
     * @param player Le joueur qui utilise /rtp
     * @return true si le processus a démarré, false si cooldown actif
     */
    public static boolean startRtp(ServerPlayer player) {
        UUID playerId = player.getUUID();
        String dimensionKey = player.level().dimension().location().toString();

        // Vérifier le cooldown
        long remainingTime = getRemainingCooldown(playerId, dimensionKey);
        if (remainingTime > 0) {
            long minutes = remainingTime / (60 * 20);
            long seconds = (remainingTime % (60 * 20)) / 20;
            player.sendSystemMessage(Component.translatable("erinium_faction.cmd.rtp.cooldown", minutes, seconds));
            return false;
        }

        // Annuler toute téléportation en attente
        if (pendingTeleports.containsKey(playerId)) {
            PendingTeleport pending = pendingTeleports.remove(playerId);
            if (pending != null && pending.scheduledTaskId != null) {
                // La tâche sera automatiquement annulée car on l'a retiré de la map
            }
        }

        // Sauvegarder la position initiale
        Vec3 startPos = player.position();

        // Créer une nouvelle téléportation en attente
        PendingTeleport pending = new PendingTeleport(player, startPos);
        pendingTeleports.put(playerId, pending);

        player.sendSystemMessage(Component.translatable("erinium_faction.cmd.rtp.starting", WAIT_SECONDS));

        // Programmer la téléportation après 5 secondes
        scheduleRtp(player, WAIT_TICKS);

        return true;
    }

    /**
     * Annule la téléportation d'un joueur s'il bouge
     * @param player Le joueur
     */
    public static void checkPlayerMovement(ServerPlayer player) {
        UUID playerId = player.getUUID();
        PendingTeleport pending = pendingTeleports.get(playerId);

        if (pending != null) {
            Vec3 currentPos = player.position();

            // Calculer la distance en 2D (X et Z seulement, ignorer Y pour les petites chutes)
            double dx = currentPos.x - pending.startPosition.x;
            double dz = currentPos.z - pending.startPosition.z;
            double distance2D = Math.sqrt(dx * dx + dz * dz);

            // Si le joueur a bougé de plus de 0.3 bloc horizontalement
            if (distance2D > 0.3) {
                pendingTeleports.remove(playerId);
                player.sendSystemMessage(Component.translatable("erinium_faction.cmd.rtp.cancelled"));
            }
        }
    }

    /**
     * Programme la téléportation après un délai
     */
    private static void scheduleRtp(ServerPlayer player, int tickDelay) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        server.tell(new net.minecraft.server.TickTask(server.getTickCount() + tickDelay, () -> {
            // Vérifier que la téléportation est toujours valide
            UUID playerId = player.getUUID();
            PendingTeleport pending = pendingTeleports.get(playerId);

            if (pending == null) {
                return; // Téléportation annulée
            }

            // Vérifier que le joueur n'a pas bougé
            Vec3 currentPos = player.position();
            double dx = currentPos.x - pending.startPosition.x;
            double dz = currentPos.z - pending.startPosition.z;
            double distance2D = Math.sqrt(dx * dx + dz * dz);

            if (distance2D > 0.3) {
                pendingTeleports.remove(playerId);
                player.sendSystemMessage(Component.translatable("erinium_faction.cmd.rtp.cancelled"));
                return;
            }

            // Effectuer la téléportation
            executeRtp(player);

            // Retirer de la liste des téléportations en attente
            pendingTeleports.remove(playerId);
        }));
    }

    /**
     * Exécute la téléportation aléatoire
     */
    private static void executeRtp(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Random random = new Random();
        boolean isNether = level.dimension() == Level.NETHER;

        BlockPos targetPos = null;
        int attempts = 0;

        // Chercher une position sûre
        while (attempts < MAX_ATTEMPTS && targetPos == null) {
            // Générer une distance aléatoire entre MIN et MAX à partir de 0,0
            int distance = MIN_DISTANCE + random.nextInt(MAX_DISTANCE - MIN_DISTANCE);

            // Générer un angle aléatoire
            double angle = random.nextDouble() * 2 * Math.PI;

            // Calculer les coordonnées X et Z à partir de 0,0
            int x = (int) (distance * Math.cos(angle));
            int z = (int) (distance * Math.sin(angle));

            // Trouver la hauteur appropriée (surface)
            BlockPos testPos = findSafeY(level, x, z, isNether);

            if (testPos != null) {
                // Vérifier que le chunk n'est pas claim
                ChunkPos chunkPos = new ChunkPos(testPos);
                ClaimKey claimKey = new ClaimKey(level.dimension().location().toString(), chunkPos.x, chunkPos.z);
                ClaimsSavedData claimsData = ClaimsSavedData.get(player.getServer());

                if (!claimsData.isClaimed(claimKey)) {
                    targetPos = testPos;
                }
            }

            attempts++;
        }

        if (targetPos == null) {
            player.sendSystemMessage(Component.translatable("erinium_faction.cmd.rtp.failed"));
            return;
        }

        // Téléporter le joueur
        player.teleportTo(
            level,
            targetPos.getX() + 0.5,
            targetPos.getY(),
            targetPos.getZ() + 0.5,
            player.getYRot(),
            player.getXRot()
        );

        // Enregistrer le cooldown
        UUID playerId = player.getUUID();
        String dimensionKey = level.dimension().location().toString();
        setCooldown(playerId, dimensionKey);

        player.sendSystemMessage(Component.translatable("erinium_faction.cmd.rtp.success", targetPos.getX(), targetPos.getY(), targetPos.getZ()));
    }

    /**
     * Trouve une position Y sûre pour la téléportation
     * @param level Le niveau
     * @param x Coordonnée X
     * @param z Coordonnée Z
     * @param isNether True si c'est le Nether (pas besoin de chercher la surface)
     * @return La position sûre, ou null si aucune n'est trouvée
     */
    private static BlockPos findSafeY(ServerLevel level, int x, int z, boolean isNether) {
        int maxY = level.getMaxBuildHeight() - 1;
        int minY = level.getMinBuildHeight();

        if (isNether) {
            // Dans le Nether, chercher de haut en bas (peu importe si c'est en cave)
            return findSafeYFromTop(level, x, z, maxY, minY);
        } else {
            // Dans l'Overworld et End, chercher la surface (pas de caves)
            return findSurfacePosition(level, x, z, maxY, minY);
        }
    }

    /**
     * Trouve une position sûre en partant du haut (pour le Nether)
     */
    private static BlockPos findSafeYFromTop(ServerLevel level, int x, int z, int maxY, int minY) {
        // Dans le Nether, limiter à Y=120 pour éviter le toit (bedrock à Y=127)
        int netherMaxY = Math.min(maxY, 120);

        for (int y = netherMaxY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState blockState = level.getBlockState(pos);
            BlockState blockAbove = level.getBlockState(pos.above());
            BlockState blockAbove2 = level.getBlockState(pos.above(2));

            // Vérifier que le bloc est solide et que les 2 blocs au-dessus sont de l'air
            if (blockState.isSolidRender(level, pos) &&
                !blockState.liquid() &&
                blockAbove.isAir() &&
                blockAbove2.isAir()) {

                // Vérifier que ce n'est pas de la lave ou un bloc dangereux
                if (!blockState.is(Blocks.LAVA) &&
                    !blockState.is(Blocks.MAGMA_BLOCK) &&
                    !blockState.is(Blocks.FIRE) &&
                    !blockState.is(Blocks.CACTUS)) {
                    return pos.above(); // Position au-dessus du bloc solide
                }
            }
        }
        return null;
    }

    /**
     * Trouve la position de surface (pour Overworld et End)
     * Évite les caves en s'assurant qu'il y a du ciel au-dessus
     */
    private static BlockPos findSurfacePosition(ServerLevel level, int x, int z, int maxY, int minY) {
        // Partir du haut et descendre jusqu'à trouver le premier bloc solide
        BlockPos surfacePos = null;

        for (int y = maxY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState blockState = level.getBlockState(pos);
            BlockState blockAbove = level.getBlockState(pos.above());
            BlockState blockAbove2 = level.getBlockState(pos.above(2));

            // Trouver le premier bloc solide en descendant
            if (blockState.isSolidRender(level, pos) &&
                !blockState.liquid() &&
                blockAbove.isAir() &&
                blockAbove2.isAir()) {

                // Vérifier que ce n'est pas un bloc dangereux
                if (!blockState.is(Blocks.LAVA) &&
                    !blockState.is(Blocks.MAGMA_BLOCK) &&
                    !blockState.is(Blocks.FIRE) &&
                    !blockState.is(Blocks.CACTUS)) {

                    surfacePos = pos.above();
                    break;
                }
            }
        }

        if (surfacePos == null) {
            return null;
        }

        // Vérifier qu'il y a du ciel au-dessus (pas dans une cave)
        // On vérifie s'il y a au moins 10 blocs d'air consécutifs au-dessus
        int airBlocks = 0;
        for (int y = surfacePos.getY(); y < Math.min(surfacePos.getY() + 20, maxY); y++) {
            BlockPos checkPos = new BlockPos(x, y, z);
            if (level.getBlockState(checkPos).isAir()) {
                airBlocks++;
            } else {
                // Si on rencontre un bloc solide, ce n'est probablement pas la surface
                if (airBlocks < 10) {
                    return null; // C'est une cave
                }
                break;
            }
        }

        // Vérifier qu'on a au moins 10 blocs d'air (ciel ouvert)
        if (airBlocks >= 10) {
            return surfacePos;
        }

        return null; // C'est une cave
    }

    /**
     * Obtient le temps de cooldown restant en ticks
     */
    private static long getRemainingCooldown(UUID playerId, String dimension) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return 0;
        }

        Long lastUsed = playerCooldowns.get(dimension);
        if (lastUsed == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = (currentTime - lastUsed) / 50; // Convertir ms en ticks (1 tick = 50ms)
        long remaining = COOLDOWN_TICKS - elapsed;

        return Math.max(0, remaining);
    }

    /**
     * Définit le cooldown pour un joueur dans une dimension
     */
    private static void setCooldown(UUID playerId, String dimension) {
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        playerCooldowns.put(dimension, System.currentTimeMillis());
    }

    /**
     * Classe interne pour stocker les données de téléportation en attente
     */
    private static class PendingTeleport {
        final ServerPlayer player;
        final Vec3 startPosition;
        Object scheduledTaskId; // Pour référence future si nécessaire

        PendingTeleport(ServerPlayer player, Vec3 startPosition) {
            this.player = player;
            this.startPosition = startPosition;
        }
    }
}
