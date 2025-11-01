package fr.eriniumgroup.erinium_faction.features.antixray;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Moteur principal de l'Anti-Xray
 * Gère l'obfuscation des minerais et la révélation progressive
 */
public class AntiXrayEngine {

    private final AntiXrayConfig config;

    // Cache des blocs révélés par joueur
    private final Map<UUID, Set<BlockPos>> revealedBlocks = new ConcurrentHashMap<>();

    // Cache des chunks obfusqués
    private final Map<ChunkPos, ObfuscatedChunkData> obfuscatedChunks = new ConcurrentHashMap<>();

    // Positions spoofées (faux minerais envoyés au client) par joueur
    private final Map<UUID, Set<BlockPos>> spoofedPositions = new ConcurrentHashMap<>();

    // Dernière position utilisée pour déclencher le recalcul par joueur
    private final Map<UUID, BlockPos> lastSpoofCenter = new ConcurrentHashMap<>();

    public AntiXrayEngine(AntiXrayConfig config) {
        this.config = config;
    }

    /**
     * Obfusque un chunk lors de son envoi au client
     */
    public void obfuscateChunk(LevelChunk chunk, ServerPlayer player) {
        if (!config.isEnabled()) return;

        ServerLevel level = (ServerLevel) chunk.getLevel();
        ChunkPos chunkPos = chunk.getPos();

        // Vérifier si la dimension est activée
        var dimConfig = config.getDimensionConfig(level.dimension().location());
        if (!dimConfig.isEnabled()) return;

        // Obtenir ou créer les données d'obfuscation
        ObfuscatedChunkData data = obfuscatedChunks.computeIfAbsent(
            chunkPos,
            pos -> new ObfuscatedChunkData(chunk, config)
        );

        // Appliquer l'obfuscation selon le mode
        switch (config.getMode()) {
            case OBFUSCATE_ONLY -> obfuscateOnly(chunk, data, dimConfig);
            case ENGINE_MODE_1 -> engineMode1(chunk, data, dimConfig);
            case ENGINE_MODE_2, HELL -> engineMode2(chunk, data, dimConfig);
            case DISABLED -> { /* no-op */ }
        }
    }

    /**
     * Mode OBFUSCATE_ONLY: Remplace les minerais cachés par de la pierre
     */
    private void obfuscateOnly(LevelChunk chunk, ObfuscatedChunkData data, AntiXrayConfig.DimensionConfig dimConfig) {
        for (BlockPos pos : data.getHiddenOrePositions()) {
            if (!dimConfig.isActiveAtY(pos.getY())) continue;

            BlockState currentState = chunk.getBlockState(pos);
            if (config.isHiddenBlock(currentState.getBlock())) {
                // Remplacer par le bloc de remplacement approprié
                BlockState replacement = getReplacementBlock(chunk, pos);
                // Note: L'obfuscation réelle se fait via packet interception
                data.addObfuscatedBlock(pos, currentState, replacement);
            }
        }
    }

    /**
     * Mode ENGINE_MODE_1: Remplace les minerais par la pierre environnante
     */
    private void engineMode1(LevelChunk chunk, ObfuscatedChunkData data, AntiXrayConfig.DimensionConfig dimConfig) {
        for (BlockPos pos : data.getHiddenOrePositions()) {
            if (!dimConfig.isActiveAtY(pos.getY())) continue;

            BlockState currentState = chunk.getBlockState(pos);
            if (config.isHiddenBlock(currentState.getBlock())) {
                // Analyser les blocs environnants pour choisir le meilleur remplacement
                BlockState replacement = getMostCommonSurroundingBlock(chunk, pos);
                data.addObfuscatedBlock(pos, currentState, replacement);
            }
        }
    }

    /**
     * Mode ENGINE_MODE_2: Ajoute des faux minerais dans la pierre (côté client via packets)
     */
    private void engineMode2(LevelChunk chunk, ObfuscatedChunkData data, AntiXrayConfig.DimensionConfig dimConfig) {
        // Obfusquer les vrais minerais
        for (BlockPos pos : data.getHiddenOrePositions()) {
            if (!dimConfig.isActiveAtY(pos.getY())) continue;

            BlockState currentState = chunk.getBlockState(pos);
            if (config.isHiddenBlock(currentState.getBlock())) {
                BlockState replacement = getReplacementBlock(chunk, pos);
                data.addObfuscatedBlock(pos, currentState, replacement);
            }
        }
        // L'injection de faux minerais visibles au client est gérée par spoofFakeOresAround()
    }

    /**
     * Révèle les blocs autour d'une position (quand un joueur mine ou se déplace)
     */
    public void revealBlocksAround(ServerLevel level, BlockPos centerPos, ServerPlayer player) {
        if (!config.isEnabled()) return;
        var dimConfig = config.getDimensionConfig(level.dimension().location());
        if (!dimConfig.isEnabled()) return;

        UUID playerId = player.getUUID();
        Set<BlockPos> playerRevealed = revealedBlocks.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());
        Set<BlockPos> playerSpoofed = spoofedPositions.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());

        int radius = config.getUpdateRadius();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centerPos.offset(dx, dy, dz);
                    if (!dimConfig.isActiveAtY(pos.getY())) continue;

                    // Marquer comme révélé et retirer du spoof si présent
                    playerRevealed.add(pos);
                    playerSpoofed.remove(pos);

                    // Envoyer la vraie valeur du bloc au client
                    BlockState realState = level.getBlockState(pos);
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(pos, realState));
                }
            }
        }
    }

    /**
     * Envoie des faux minerais autour du joueur (visuel client uniquement)
     */
    public void spoofFakeOresAround(ServerLevel level, ServerPlayer player) {
        if (!config.isEnabled()) return;
        if (config.getMode() != AntiXrayConfig.AntiXrayMode.ENGINE_MODE_2) return;

        var dimConfig = config.getDimensionConfig(level.dimension().location());
        if (!dimConfig.isEnabled()) return;

        UUID playerId = player.getUUID();
        Set<BlockPos> playerSpoofed = spoofedPositions.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());

        int radius = Math.max(1, config.getSpoofRadius());
        int maxCount = Math.max(0, config.getSpoofMaxCount());
        int budget = Math.max(0, config.getSpoofBudgetPerTick());

        // 1) Retirer les positions en dehors du rayon ou invalides et renvoyer l'état réel pour celles-ci
        if (!playerSpoofed.isEmpty()) {
            Iterator<BlockPos> it = playerSpoofed.iterator();
            while (it.hasNext()) {
                BlockPos pos = it.next();
                if (pos.distManhattan(player.blockPosition()) > radius
                        || !dimConfig.isActiveAtY(pos.getY())) {
                    BlockState real = level.getBlockState(pos);
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(pos, real));
                    it.remove();
                }
            }
        }

        // 2) Si on a atteint le max, ne rien ajouter
        if (playerSpoofed.size() >= maxCount || budget == 0) return;

        Random rnd = new Random(player.tickCount * 31L ^ playerId.getMostSignificantBits());
        BlockState template = getRandomHiddenBlock(rnd);
        BlockPos base = player.blockPosition();
        int dyRange = Math.max(1, radius / 2);

        int toAdd = Math.min(budget, Math.max(0, maxCount - playerSpoofed.size()));
        int attempts = toAdd * 8; // éviter les boucles infinies si environnement pauvre
        for (int i = 0, added = 0; i < attempts && added < toAdd; i++) {
            int dx = rnd.nextInt(radius * 2 + 1) - radius;
            int dy = rnd.nextInt(dyRange * 2 + 1) - dyRange;
            int dz = rnd.nextInt(radius * 2 + 1) - radius;
            BlockPos pos = base.offset(dx, dy, dz);
            if (!dimConfig.isActiveAtY(pos.getY())) continue;
            if (playerSpoofed.contains(pos)) continue;

            BlockState real = level.getBlockState(pos);
            if (real.isAir() || !config.isReplacementBlock(real.getBlock())) continue;

            BlockState fake = (rnd.nextInt(5) == 0) ? getRandomHiddenBlock(rnd) : template;
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(pos, fake));
            playerSpoofed.add(pos);
            added++;
        }
    }

    /**
     * Nettoie les données d'un joueur déconnecté
     */
    public void cleanupPlayer(UUID playerId) {
        revealedBlocks.remove(playerId);
        spoofedPositions.remove(playerId);
    }

    /**
     * Nettoie les données d'un chunk déchargé
     */
    public void cleanupChunk(ChunkPos chunkPos) {
        obfuscatedChunks.remove(chunkPos);
    }

    /**
     * Vérifie si un bloc est révélé pour un joueur
     */
    public boolean isBlockRevealed(UUID playerId, BlockPos pos) {
        Set<BlockPos> revealed = revealedBlocks.get(playerId);
        return revealed != null && revealed.contains(pos);
    }

    /**
     * Obtient le bloc de remplacement approprié selon le contexte
     */
    private BlockState getReplacementBlock(LevelChunk chunk, BlockPos pos) {
        Level level = chunk.getLevel();

        // Déterminer le type de roche approprié selon la dimension et la hauteur
        if (level.dimension() == Level.NETHER) {
            return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:netherrack")).defaultBlockState();
        } else if (level.dimension() == Level.END) {
            return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:end_stone")).defaultBlockState();
        } else {
            // Overworld
            if (pos.getY() < 0) {
                return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:deepslate")).defaultBlockState();
            } else {
                return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:stone")).defaultBlockState();
            }
        }
    }

    /**
     * Trouve le bloc le plus commun autour d'une position
     */
    private BlockState getMostCommonSurroundingBlock(LevelChunk chunk, BlockPos pos) {
        Map<Block, Integer> blockCounts = new HashMap<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    BlockState state = chunk.getBlockState(checkPos);

                    if (config.isReplacementBlock(state.getBlock())) {
                        blockCounts.merge(state.getBlock(), 1, Integer::sum);
                    }
                }
            }
        }

        // Retourner le bloc le plus commun ou pierre par défaut
        return blockCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey().defaultBlockState())
            .orElseGet(() -> BuiltInRegistries.BLOCK.get(
                ResourceLocation.parse("minecraft:stone")
            ).defaultBlockState());
    }

    /**
     * Obtient un minerai caché aléatoire pour les faux minerais
     */
    private BlockState getRandomHiddenBlock(Random random) {
        List<ResourceLocation> hiddenList = new ArrayList<>(config.getHiddenBlocks());
        if (hiddenList.isEmpty()) {
            return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:diamond_ore")).defaultBlockState();
        }
        ResourceLocation randomOre = hiddenList.get(random.nextInt(hiddenList.size()));
        Block block = BuiltInRegistries.BLOCK.get(randomOre);
        return block.defaultBlockState();
    }

    /**
     * Nettoie toutes les données en mémoire
     */
    public void clearAll() {
        revealedBlocks.clear();
        obfuscatedChunks.clear();
        spoofedPositions.clear();
    }

    public AntiXrayConfig getConfig() {
        return config;
    }

    /**
     * Appelé lorsque le joueur se déplace, pour déclencher un recalcul du spoof si nécessaire
     */
    public void onPlayerMoved(ServerPlayer player) {
        if (!config.isEnabled()) return;
        if (config.getMode() == AntiXrayConfig.AntiXrayMode.DISABLED) return;
        BlockPos last = lastSpoofCenter.get(player.getUUID());
        int trigger = Math.max(2, config.getSpoofRadius() / 3); // mouvement significatif
        if (last == null || last.distManhattan(player.blockPosition()) >= trigger) {
            lastSpoofCenter.put(player.getUUID(), player.blockPosition());
            // compléter/mettre à jour la zone une fois
            spoofFillCoverage(player.serverLevel(), player);
        }
    }

    /**
     * Appelé lorsque un chunk est chargé, pour mettre à jour le spoof des joueurs à proximité
     */
    public void onChunkLoaded(ServerLevel level, LevelChunk chunk, Collection<ServerPlayer> players) {
        if (!config.isEnabled()) return;
        if (config.getMode() == AntiXrayConfig.AntiXrayMode.DISABLED) return;
        ChunkPos cp = chunk.getPos();
        for (ServerPlayer p : players) {
            if (p.serverLevel() != level) continue;
            ChunkPos pc = p.chunkPosition();
            int dx = Math.abs(pc.x - cp.x);
            int dz = Math.abs(pc.z - cp.z);
            if (dx <= 1 && dz <= 1) {
                spoofFillCoverage(level, p);
            }
        }
    }

    /**
     * Remplit la zone autour d'un joueur pour atteindre la couverture cible de faux minerais
     */
    private void spoofFillCoverage(ServerLevel level, ServerPlayer player) {
        // HELL: applique des paramètres extrêmes à la volée
        boolean hell = config.getMode() == AntiXrayConfig.AntiXrayMode.HELL;
        int radius = hell ? Math.max(config.getSpoofRadius(), 24) : config.getSpoofRadius();
        int targetCoverage = hell ? Math.max(config.getSpoofTargetCoverage(), 80) : config.getSpoofTargetCoverage();
        int maxCount = hell ? Math.max(config.getSpoofMaxCount(), 2000) : config.getSpoofMaxCount();
        int budget = config.getSpoofBudgetPerTick();
        int burstCap = hell ? Math.max(budget * 8, 256) : Math.max(budget * 4, 128);

        var dimConfig = config.getDimensionConfig(level.dimension().location());
        if (!dimConfig.isEnabled()) return;

        UUID pid = player.getUUID();
        Set<BlockPos> spoofed = spoofedPositions.computeIfAbsent(pid, k -> ConcurrentHashMap.newKeySet());

        BlockPos base = player.blockPosition();
        int dyRange = Math.max(1, radius / 2);

        // 1) Nettoyage hors rayon/Y
        if (!spoofed.isEmpty()) {
            Iterator<BlockPos> it = spoofed.iterator();
            while (it.hasNext()) {
                BlockPos pos = it.next();
                if (pos.distManhattan(base) > radius || !dimConfig.isActiveAtY(pos.getY())) {
                    BlockState real = level.getBlockState(pos);
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(pos, real));
                    it.remove();
                }
            }
        }

        // 2) Calcul de la couverture actuelle et ajout pour atteindre la cible
        if (targetCoverage <= 0) return;

        // Approximation: on fait des essais aléatoires jusqu'à remplir Max(min manquant, budget implicite)
        Random rnd = new Random(base.asLong() ^ player.getUUID().getMostSignificantBits());
        BlockState template = getRandomHiddenBlock(rnd);

        int sample = Math.min(1200, (2*radius+1)*(2*radius+1));
        int replaceableCount = 0;
        int spoofedCount = 0;

        // Échantillonner l'environnement pour estimer la couverture
        for (int i = 0; i < sample; i++) {
            int dx = rnd.nextInt(radius * 2 + 1) - radius;
            int dz = rnd.nextInt(radius * 2 + 1) - radius;
            int dy = rnd.nextInt(dyRange * 2 + 1) - dyRange;
            BlockPos pos = base.offset(dx, dy, dz);
            if (!dimConfig.isActiveAtY(pos.getY())) continue;
            BlockState real = level.getBlockState(pos);
            if (real.isAir()) continue;
            if (config.isReplacementBlock(real.getBlock())) {
                replaceableCount++;
                if (spoofed.contains(pos)) spoofedCount++;
            }
        }
        int currentCoverage = replaceableCount == 0 ? 0 : (int)(spoofedCount * 100.0 / replaceableCount);
        if (currentCoverage >= targetCoverage) return;

        int needed = (int)Math.ceil((targetCoverage/100.0 * replaceableCount) - spoofedCount);
        int toAdd = Math.min(needed, Math.max(0, maxCount - spoofed.size()));
        toAdd = Math.min(toAdd, burstCap); // limiter le pic d’ajouts pour éviter les lags
        if (toAdd <= 0) return;

        int attempts = toAdd * 16;
        int added = 0;
        while (attempts-- > 0 && added < toAdd) {
            int dx = rnd.nextInt(radius * 2 + 1) - radius;
            int dz = rnd.nextInt(radius * 2 + 1) - radius;
            int dy = rnd.nextInt(dyRange * 2 + 1) - dyRange;
            BlockPos pos = base.offset(dx, dy, dz);
            if (!dimConfig.isActiveAtY(pos.getY())) continue;
            if (spoofed.contains(pos)) continue;
            BlockState real = level.getBlockState(pos);
            if (real.isAir() || !config.isReplacementBlock(real.getBlock())) continue;
            BlockState fake = (rnd.nextInt(5) == 0) ? getRandomHiddenBlock(rnd) : template;
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(pos, fake));
            spoofed.add(pos);
            added++;
        }
    }
}
