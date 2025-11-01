package fr.eriniumgroup.erinium_faction.features.antixray;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

/**
 * Données d'obfuscation pour un chunk spécifique
 * Stocke les informations sur les blocs cachés et les faux minerais
 */
public class ObfuscatedChunkData {

    private final Set<BlockPos> hiddenOrePositions;
    private final Map<BlockPos, BlockState> originalBlocks; // Position -> état original
    private final Map<BlockPos, BlockState> obfuscatedBlocks; // Position -> état obfusqué
    private final Map<BlockPos, BlockState> fakeOres; // Position -> faux minerai

    private final long creationTime;

    public ObfuscatedChunkData(LevelChunk chunk, AntiXrayConfig config) {
        this.hiddenOrePositions = new HashSet<>();
        this.originalBlocks = new HashMap<>();
        this.obfuscatedBlocks = new HashMap<>();
        this.fakeOres = new HashMap<>();
        this.creationTime = System.currentTimeMillis();

        // Scanner le chunk pour trouver les minerais
        scanChunkForOres(chunk, config);
    }

    /**
     * Scanne le chunk pour identifier tous les minerais à cacher
     */
    private void scanChunkForOres(LevelChunk chunk, AntiXrayConfig config) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                    BlockState state = chunk.getBlockState(pos);

                    if (config.isHiddenBlock(state.getBlock())) {
                        hiddenOrePositions.add(pos);
                        originalBlocks.put(pos, state);
                    }
                }
            }
        }
    }

    /**
     * Ajoute un bloc obfusqué
     */
    public void addObfuscatedBlock(BlockPos pos, BlockState original, BlockState obfuscated) {
        originalBlocks.put(pos, original);
        obfuscatedBlocks.put(pos, obfuscated);
    }

    /**
     * Ajoute un faux minerai
     */
    public void addFakeOre(BlockPos pos, BlockState fakeOre) {
        fakeOres.put(pos, fakeOre);
    }

    /**
     * Obtient l'état obfusqué d'un bloc
     */
    public BlockState getObfuscatedState(BlockPos pos) {
        return obfuscatedBlocks.get(pos);
    }

    /**
     * Obtient l'état original d'un bloc
     */
    public BlockState getOriginalState(BlockPos pos) {
        return originalBlocks.get(pos);
    }

    /**
     * Vérifie si une position contient un faux minerai
     */
    public boolean isFakeOre(BlockPos pos) {
        return fakeOres.containsKey(pos);
    }

    /**
     * Obtient le faux minerai à une position
     */
    public BlockState getFakeOre(BlockPos pos) {
        return fakeOres.get(pos);
    }

    /**
     * Vérifie si une position contient un minerai caché
     */
    public boolean isHiddenOre(BlockPos pos) {
        return hiddenOrePositions.contains(pos);
    }

    /**
     * Obtient toutes les positions des minerais cachés
     */
    public Set<BlockPos> getHiddenOrePositions() {
        return Collections.unmodifiableSet(hiddenOrePositions);
    }

    /**
     * Obtient toutes les positions obfusquées
     */
    public Set<BlockPos> getObfuscatedPositions() {
        return Collections.unmodifiableSet(obfuscatedBlocks.keySet());
    }

    /**
     * Obtient toutes les positions de faux minerais
     */
    public Set<BlockPos> getFakeOrePositions() {
        return Collections.unmodifiableSet(fakeOres.keySet());
    }

    /**
     * Obtient le temps de création de ces données
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Obtient le nombre de minerais cachés
     */
    public int getHiddenOreCount() {
        return hiddenOrePositions.size();
    }

    /**
     * Obtient le nombre de faux minerais
     */
    public int getFakeOreCount() {
        return fakeOres.size();
    }

    /**
     * Nettoie les données (libère la mémoire)
     */
    public void clear() {
        hiddenOrePositions.clear();
        originalBlocks.clear();
        obfuscatedBlocks.clear();
        fakeOres.clear();
    }
}

