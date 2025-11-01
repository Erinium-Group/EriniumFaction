package fr.eriniumgroup.erinium_faction.features.antixray;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import java.util.*;

/**
 * Configuration pour le système Anti-Xray
 */
public class AntiXrayConfig {

    // Mode de fonctionnement
    public enum AntiXrayMode {
        DISABLED,           // Désactivé
        OBFUSCATE_ONLY,    // Obfusquer seulement les minerais
        ENGINE_MODE_1,     // Mode moteur 1 - remplacer les minerais cachés
        ENGINE_MODE_2,     // Mode moteur 2 - mélanger avec de faux minerais
        HELL                 // Mode extrême
    }

    // Configuration principale
    private boolean enabled = true;
    private AntiXrayMode mode = AntiXrayMode.ENGINE_MODE_2;

    // Liste des blocs à cacher (minerais)
    private final Set<ResourceLocation> hiddenBlocks = new HashSet<>();

    // Liste des blocs de remplacement (pour le mode obfuscation)
    private final Set<ResourceLocation> replacementBlocks = new HashSet<>();

    // Configurations avancées
    private int maxChunkSectionIndex = 63; // Y max divisé par 16
    private int updateRadius = 5; // Rayon de mise à jour autour du joueur
    private boolean usePermission = false; // Utiliser les permissions pour bypass
    private int engineMode2ChunkRate = 2; // Taux de faux minerais en mode 2

    // Config spoof client-side
    private int spoofRadius = 8;          // Rayon du champ de faux minerais autour du joueur
    private int spoofMaxCount = 400;      // Nombre maximum de positions spoofées conservées par joueur
    private int spoofBudgetPerTick = 64;  // Nombre max d’ajouts (nouvelles positions) par tick et par joueur
    // Couverture visuelle cible des faux minerais dans la zone (en % de blocs remplaçables)
    private int spoofTargetCoverage = 35;

    // Listes par dimension
    private final Map<ResourceLocation, DimensionConfig> dimensionConfigs = new HashMap<>();

    public AntiXrayConfig() {
        loadDefaultConfiguration();
    }

    /**
     * Configuration par défaut avec support vanilla et modded
     */
    private void loadDefaultConfiguration() {
        // Minerais vanilla à cacher
        addHiddenBlock("minecraft:coal_ore");
        addHiddenBlock("minecraft:deepslate_coal_ore");
        addHiddenBlock("minecraft:iron_ore");
        addHiddenBlock("minecraft:deepslate_iron_ore");
        addHiddenBlock("minecraft:gold_ore");
        addHiddenBlock("minecraft:deepslate_gold_ore");
        addHiddenBlock("minecraft:diamond_ore");
        addHiddenBlock("minecraft:deepslate_diamond_ore");
        addHiddenBlock("minecraft:emerald_ore");
        addHiddenBlock("minecraft:deepslate_emerald_ore");
        addHiddenBlock("minecraft:lapis_ore");
        addHiddenBlock("minecraft:deepslate_lapis_ore");
        addHiddenBlock("minecraft:redstone_ore");
        addHiddenBlock("minecraft:deepslate_redstone_ore");
        addHiddenBlock("minecraft:copper_ore");
        addHiddenBlock("minecraft:deepslate_copper_ore");
        addHiddenBlock("minecraft:nether_gold_ore");
        addHiddenBlock("minecraft:nether_quartz_ore");
        addHiddenBlock("minecraft:ancient_debris");

        // Blocs de remplacement (pour obfuscation)
        addReplacementBlock("minecraft:stone");
        addReplacementBlock("minecraft:deepslate");
        addReplacementBlock("minecraft:netherrack");
        addReplacementBlock("minecraft:end_stone");

        // Configuration pour l'Overworld
        DimensionConfig overworldConfig = new DimensionConfig();
        overworldConfig.setEnabled(true);
        overworldConfig.setMaxY(64); // Au-dessus de Y64, pas d'anti-xray
        dimensionConfigs.put(ResourceLocation.parse("minecraft:overworld"), overworldConfig);

        // Configuration pour le Nether
        DimensionConfig netherConfig = new DimensionConfig();
        netherConfig.setEnabled(true);
        netherConfig.setMaxY(128);
        dimensionConfigs.put(ResourceLocation.parse("minecraft:the_nether"), netherConfig);

        // Configuration pour l'End
        DimensionConfig endConfig = new DimensionConfig();
        endConfig.setEnabled(false); // Désactivé par défaut dans l'End
        dimensionConfigs.put(ResourceLocation.parse("minecraft:the_end"), endConfig);
    }

    /**
     * Ajoute un bloc à cacher (minerai)
     */
    public void addHiddenBlock(String blockId) {
        hiddenBlocks.add(ResourceLocation.parse(blockId));
    }

    /** Supprime un bloc de la liste des minerais cachés. */
    public boolean removeHiddenBlock(String blockId) {
        return hiddenBlocks.remove(ResourceLocation.parse(blockId));
    }

    /** Vide la liste des minerais cachés. */
    public void clearHiddenBlocks() {
        hiddenBlocks.clear();
    }

    /**
     * Ajoute un bloc de remplacement
     */
    public void addReplacementBlock(String blockId) {
        replacementBlocks.add(ResourceLocation.parse(blockId));
    }

    /** Supprime un bloc de la liste des remplacements. */
    public boolean removeReplacementBlock(String blockId) {
        return replacementBlocks.remove(ResourceLocation.parse(blockId));
    }

    /** Vide la liste des remplacements. */
    public void clearReplacementBlocks() {
        replacementBlocks.clear();
    }

    /**
     * Vérifie si un bloc doit être caché
     */
    public boolean isHiddenBlock(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        boolean listed = key != null && hiddenBlocks.contains(key);
        boolean tagged = block.defaultBlockState().is(Tags.Blocks.ORES); // forge:ores
        return listed || tagged;
    }

    /**
     * Vérifie si un bloc peut être utilisé comme remplacement
     */
    public boolean isReplacementBlock(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key != null && replacementBlocks.contains(key);
    }

    /**
     * Obtient la configuration pour une dimension spécifique
     */
    public DimensionConfig getDimensionConfig(ResourceLocation dimension) {
        return dimensionConfigs.getOrDefault(dimension, new DimensionConfig());
    }

    /**
     * Charge la configuration depuis un fichier JSON
     */
    public void loadFromJson(String jsonContent) {
        // TODO: Implémenter le parsing JSON
    }

    /**
     * Sauvegarde la configuration vers un fichier JSON
     */
    public String saveToJson() {
        // TODO: Implémenter la sérialisation JSON
        return "{}";
    }

    // Getters et setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AntiXrayMode getMode() {
        return mode;
    }

    public void setMode(AntiXrayMode mode) {
        this.mode = mode;
    }

    public Set<ResourceLocation> getHiddenBlocks() {
        return Collections.unmodifiableSet(hiddenBlocks);
    }

    public Set<ResourceLocation> getReplacementBlocks() {
        return Collections.unmodifiableSet(replacementBlocks);
    }

    public int getMaxChunkSectionIndex() {
        return maxChunkSectionIndex;
    }

    public void setMaxChunkSectionIndex(int maxChunkSectionIndex) {
        this.maxChunkSectionIndex = maxChunkSectionIndex;
    }

    public int getUpdateRadius() {
        return updateRadius;
    }

    public void setUpdateRadius(int updateRadius) {
        this.updateRadius = updateRadius;
    }

    public boolean isUsePermission() {
        return usePermission;
    }

    public void setUsePermission(boolean usePermission) {
        this.usePermission = usePermission;
    }

    public int getEngineMode2ChunkRate() {
        return engineMode2ChunkRate;
    }

    public void setEngineMode2ChunkRate(int engineMode2ChunkRate) {
        this.engineMode2ChunkRate = engineMode2ChunkRate;
    }

    public int getSpoofRadius() {
        return spoofRadius;
    }

    public void setSpoofRadius(int spoofRadius) {
        this.spoofRadius = Math.max(1, spoofRadius);
    }

    public int getSpoofMaxCount() {
        return spoofMaxCount;
    }

    public void setSpoofMaxCount(int spoofMaxCount) {
        this.spoofMaxCount = Math.max(0, spoofMaxCount);
    }

    public int getSpoofBudgetPerTick() {
        return spoofBudgetPerTick;
    }

    public void setSpoofBudgetPerTick(int spoofBudgetPerTick) {
        this.spoofBudgetPerTick = Math.max(0, spoofBudgetPerTick);
    }

    public int getSpoofTargetCoverage() {
        return spoofTargetCoverage;
    }

    public void setSpoofTargetCoverage(int spoofTargetCoverage) {
        this.spoofTargetCoverage = Math.max(0, Math.min(100, spoofTargetCoverage));
    }

    /**
     * Configuration spécifique à une dimension
     */
    public static class DimensionConfig {
        private boolean enabled = true;
        private int maxY = 200;
        private int minY = -64;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxY() {
            return maxY;
        }

        public void setMaxY(int maxY) {
            this.maxY = maxY;
        }

        public int getMinY() {
            return minY;
        }

        public void setMinY(int minY) {
            this.minY = minY;
        }

        /**
         * Vérifie si l'anti-xray est actif à une position Y donnée
         */
        public boolean isActiveAtY(int y) {
            return enabled && y >= minY && y <= maxY;
        }
    }
}

