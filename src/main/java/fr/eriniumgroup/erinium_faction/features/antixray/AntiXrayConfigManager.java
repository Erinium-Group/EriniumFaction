package fr.eriniumgroup.erinium_faction.features.antixray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire de configuration JSON pour l'Anti-Xray
 */
public class AntiXrayConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;

    public AntiXrayConfigManager(Path configPath) {
        this.configPath = configPath;
    }

    /**
     * Sauvegarde la configuration dans un fichier JSON
     */
    public void saveConfig(AntiXrayConfig config) throws IOException {
        ConfigData data = new ConfigData();

        // Configuration générale
        data.enabled = config.isEnabled();
        data.mode = config.getMode().name();
        data.updateRadius = config.getUpdateRadius();
        data.engineMode2ChunkRate = config.getEngineMode2ChunkRate();
        data.usePermission = config.isUsePermission();
        data.spoofRadius = config.getSpoofRadius();
        data.spoofMaxCount = config.getSpoofMaxCount();
        data.spoofBudgetPerTick = config.getSpoofBudgetPerTick();
        data.spoofTargetCoverage = config.getSpoofTargetCoverage();

        // Blocs cachés
        data.hiddenBlocks = new ArrayList<>();
        for (ResourceLocation loc : config.getHiddenBlocks()) {
            data.hiddenBlocks.add(loc.toString());
        }

        // Blocs de remplacement
        data.replacementBlocks = new ArrayList<>();
        for (ResourceLocation loc : config.getReplacementBlocks()) {
            data.replacementBlocks.add(loc.toString());
        }

        // Sauvegarder dans le fichier
        String json = GSON.toJson(data);
        Files.createDirectories(configPath.getParent());
        Files.writeString(configPath, json);
    }

    /**
     * Charge la configuration depuis un fichier JSON
     */
    public void loadConfig(AntiXrayConfig config) throws IOException {
        if (!Files.exists(configPath)) {
            // Créer une configuration par défaut
            saveConfig(config);
            return;
        }

        String json = Files.readString(configPath);
        ConfigData data = GSON.fromJson(json, ConfigData.class);

        if (data == null) return;

        // Charger la configuration générale
        config.setEnabled(data.enabled);
        try {
            config.setMode(AntiXrayConfig.AntiXrayMode.valueOf(data.mode));
        } catch (IllegalArgumentException e) {
            // Mode invalide, garder le mode par défaut
        }
        config.setUpdateRadius(data.updateRadius);
        config.setEngineMode2ChunkRate(data.engineMode2ChunkRate);
        config.setUsePermission(data.usePermission);
        config.setSpoofRadius(data.spoofRadius);
        config.setSpoofMaxCount(data.spoofMaxCount);
        config.setSpoofBudgetPerTick(data.spoofBudgetPerTick);
        config.setSpoofTargetCoverage(data.spoofTargetCoverage);

        // Charger les blocs cachés
        if (data.hiddenBlocks != null) {
            for (String blockId : data.hiddenBlocks) {
                try {
                    config.addHiddenBlock(blockId);
                } catch (Exception e) {
                    // Ignorer les blocs invalides
                }
            }
        }

        // Charger les blocs de remplacement
        if (data.replacementBlocks != null) {
            for (String blockId : data.replacementBlocks) {
                try {
                    config.addReplacementBlock(blockId);
                } catch (Exception e) {
                    // Ignorer les blocs invalides
                }
            }
        }
    }

    /**
     * Structure de données pour la sérialisation JSON
     */
    private static class ConfigData {
        boolean enabled = true;
        String mode = "ENGINE_MODE_2";
        int updateRadius = 2;
        int engineMode2ChunkRate = 1;
        boolean usePermission = false;
        int spoofRadius = 8;
        int spoofMaxCount = 400;
        int spoofBudgetPerTick = 64;
        int spoofTargetCoverage = 35;
        List<String> hiddenBlocks = new ArrayList<>();
        List<String> replacementBlocks = new ArrayList<>();
        Map<String, DimensionConfigData> dimensions = new HashMap<>();
    }

    /**
     * Structure pour les configurations par dimension
     */
    private static class DimensionConfigData {
        boolean enabled = true;
        int maxY = 64;
        int minY = -64;
    }

    /**
     * Exporte la configuration actuelle en JSON formaté
     */
    public String exportConfig(AntiXrayConfig config) {
        ConfigData data = new ConfigData();

        data.enabled = config.isEnabled();
        data.mode = config.getMode().name();
        data.updateRadius = config.getUpdateRadius();
        data.engineMode2ChunkRate = config.getEngineMode2ChunkRate();
        data.usePermission = config.isUsePermission();
        data.spoofRadius = config.getSpoofRadius();
        data.spoofMaxCount = config.getSpoofMaxCount();
        data.spoofBudgetPerTick = config.getSpoofBudgetPerTick();
        data.spoofTargetCoverage = config.getSpoofTargetCoverage();

        data.hiddenBlocks = new ArrayList<>();
        for (ResourceLocation loc : config.getHiddenBlocks()) {
            data.hiddenBlocks.add(loc.toString());
        }

        data.replacementBlocks = new ArrayList<>();
        for (ResourceLocation loc : config.getReplacementBlocks()) {
            data.replacementBlocks.add(loc.toString());
        }

        return GSON.toJson(data);
    }

    /**
     * Importe une configuration depuis une chaîne JSON
     */
    public void importConfig(AntiXrayConfig config, String json) {
        try {
            ConfigData data = GSON.fromJson(json, ConfigData.class);

            if (data == null) return;

            config.setEnabled(data.enabled);
            config.setMode(AntiXrayConfig.AntiXrayMode.valueOf(data.mode));
            config.setUpdateRadius(data.updateRadius);
            config.setEngineMode2ChunkRate(data.engineMode2ChunkRate);
            config.setUsePermission(data.usePermission);
            config.setSpoofRadius(data.spoofRadius);
            config.setSpoofMaxCount(data.spoofMaxCount);
            config.setSpoofBudgetPerTick(data.spoofBudgetPerTick);
            config.setSpoofTargetCoverage(data.spoofTargetCoverage);

            // Effacer les listes existantes
            config.getHiddenBlocks().clear();
            config.getReplacementBlocks().clear();

            if (data.hiddenBlocks != null) {
                for (String blockId : data.hiddenBlocks) {
                    config.addHiddenBlock(blockId);
                }
            }

            if (data.replacementBlocks != null) {
                for (String blockId : data.replacementBlocks) {
                    config.addReplacementBlock(blockId);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'import de la configuration: " + e.getMessage(), e);
        }
    }
}
