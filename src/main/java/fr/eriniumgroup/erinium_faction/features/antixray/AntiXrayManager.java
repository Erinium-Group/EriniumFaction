package fr.eriniumgroup.erinium_faction.features.antixray;

import fr.eriniumgroup.erinium_faction.commands.AntiXrayCommand;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * Gestionnaire principal de l'Anti-Xray
 * Point d'entrée central pour toutes les fonctionnalités anti-xray
 */
public class AntiXrayManager {

    private static AntiXrayManager instance;

    private final AntiXrayConfig config;
    private final AntiXrayEngine engine;
    private final AntiXrayConfigManager configManager;

    private boolean initialized = false;

    private AntiXrayManager() {
        this.config = new AntiXrayConfig();
        this.engine = new AntiXrayEngine(config);

        Path configPath = FMLPaths.CONFIGDIR.get().resolve("erinium_faction").resolve("antixray.json");
        this.configManager = new AntiXrayConfigManager(configPath);
    }

    /**
     * Obtient l'instance singleton
     */
    public static AntiXrayManager getInstance() {
        if (instance == null) {
            instance = new AntiXrayManager();
        }
        return instance;
    }

    /**
     * Initialise le système anti-xray
     */
    public void init() {
        if (initialized) return;

        try {
            // Charger la configuration
            configManager.loadConfig(config);

            // Initialiser les gestionnaires d'événements et commandes
            AntiXrayCommand.init(engine);

            initialized = true;

            EFC.log.debug("§8AntiXray", "Anti-Xray initialisé avec succès!");
            EFC.log.debug("§8AntiXray", "Mode: " + config.getMode().name());
            EFC.log.debug("§8AntiXray", "Minerais cachés: " + config.getHiddenBlocks().size());

        } catch (Exception e) {
            EFC.log.error("§8AntiXray", "Erreur lors de l'initialisation de l'Anti-Xray: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarde la configuration
     */
    public void saveConfig() {
        try {
            configManager.saveConfig(config);
        } catch (Exception e) {
            EFC.log.error("§8AntiXray", "Erreur lors de la sauvegarde de la config Anti-Xray: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recharge la configuration
     */
    public void reloadConfig() {
        try {
            configManager.loadConfig(config);
            EFC.log.debug("§8AntiXray", "Configuration Anti-Xray rechargée!");
        } catch (Exception e) {
            EFC.log.error("§8AntiXray", "Erreur lors du rechargement de la config Anti-Xray: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Nettoie toutes les données en mémoire
     */
    public void shutdown() {
        if (engine != null) {
            engine.clearAll();
        }
        saveConfig();
        initialized = false;
    }

    // Getters

    public AntiXrayConfig getConfig() {
        return config;
    }

    public AntiXrayEngine getEngine() {
        return engine;
    }

    public AntiXrayConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Vérifie si l'anti-xray est actif
     */
    public boolean isEnabled() {
        return initialized && config.isEnabled();
    }

    /**
     * Active ou désactive l'anti-xray
     */
    public void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
        saveConfig();
    }

    /**
     * Change le mode de l'anti-xray
     */
    public void setMode(AntiXrayConfig.AntiXrayMode mode) {
        config.setMode(mode);
        saveConfig();
    }

    /**
     * Ajoute un minerai à cacher
     */
    public void addHiddenOre(String blockId) {
        config.addHiddenBlock(blockId);
        saveConfig();
    }

    /**
     * Retire un minerai de la liste des blocs cachés
     */
    public boolean removeHiddenOre(String blockId) {
        boolean r = config.removeHiddenBlock(blockId);
        saveConfig();
        return r;
    }

    /**
     * Nettoie tous les minerais cachés
     */
    public void clearHiddenOres() {
        config.clearHiddenBlocks();
        saveConfig();
    }

    /**
     * Ajoute un remplacement de bloc
     */
    public void addReplacement(String blockId) {
        config.addReplacementBlock(blockId);
        saveConfig();
    }

    /**
     * Retire un remplacement de bloc
     */
    public boolean removeReplacement(String blockId) {
        boolean r = config.removeReplacementBlock(blockId);
        saveConfig();
        return r;
    }

    /**
     * Nettoie tous les remplacements de blocs
     */
    public void clearReplacements() {
        config.clearReplacementBlocks();
        saveConfig();
    }

    /**
     * Obtient des statistiques sur l'anti-xray
     */
    public AntiXrayStats getStats() {
        return new AntiXrayStats(config.isEnabled(), config.getMode(), config.getHiddenBlocks().size(), config.getReplacementBlocks().size(), engine != null // Engine actif
        );
    }

    /**
     * Structure pour les statistiques
     */
    public record AntiXrayStats(boolean enabled, AntiXrayConfig.AntiXrayMode mode, int hiddenBlockCount,
                                int replacementBlockCount, boolean engineActive) {
    }
}
