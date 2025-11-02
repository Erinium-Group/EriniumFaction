package fr.eriniumgroup.erinium_faction.jobs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire de configuration pour les métiers
 * Charge les configs JSON au démarrage et les garde en mémoire
 * Côté SERVEUR uniquement
 */
public class JobsConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<JobType, JobConfig> CONFIGS = new HashMap<>();
    private static Path configDir;

    /**
     * Initialise le gestionnaire et charge toutes les configurations
     */
    public static void init() {
        configDir = FMLPaths.CONFIGDIR.get().resolve("erinium_faction").resolve("jobs");

        try {
            // Créer le dossier s'il n'existe pas
            Files.createDirectories(configDir);

            // Charger les configs pour chaque métier
            for (JobType jobType : JobType.values()) {
                loadConfig(jobType);
            }

            EFC.log.info("Loaded {} job configurations", CONFIGS.size());
        } catch (IOException e) {
            EFC.log.error("Failed to initialize jobs config manager", e);
        }
    }

    /**
     * Charge la configuration d'un métier depuis le fichier JSON
     */
    private static void loadConfig(JobType jobType) {
        File configFile = getConfigFile(jobType);

        if (!configFile.exists()) {
            // Créer une config par défaut
            JobConfig defaultConfig = createDefaultConfig(jobType);
            saveConfig(jobType, defaultConfig);
            CONFIGS.put(jobType, defaultConfig);
            EFC.log.info("Created default config for job: {}", jobType.name());
        } else {
            // Charger depuis le fichier
            try (FileReader reader = new FileReader(configFile)) {
                JobConfig config = GSON.fromJson(reader, JobConfig.class);
                CONFIGS.put(jobType, config);
                EFC.log.info("Loaded config for job: {}", jobType.name());
            } catch (IOException e) {
                EFC.log.error("Failed to load config for job: {}", jobType.name(), e);
                // Utiliser une config par défaut en cas d'erreur
                CONFIGS.put(jobType, createDefaultConfig(jobType));
            }
        }
    }

    /**
     * Sauvegarde la configuration d'un métier dans le fichier JSON
     */
    private static void saveConfig(JobType jobType, JobConfig config) {
        File configFile = getConfigFile(jobType);

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            EFC.log.error("Failed to save config for job: {}", jobType.name(), e);
        }
    }

    /**
     * Obtient le fichier de configuration pour un métier
     */
    private static File getConfigFile(JobType jobType) {
        return configDir.resolve(jobType.name().toLowerCase() + ".json").toFile();
    }

    /**
     * Crée une configuration par défaut pour un métier
     */
    private static JobConfig createDefaultConfig(JobType jobType) {
        JobConfig config = new JobConfig(jobType);

        // Exemples de configuration selon le métier
        switch (jobType) {
            case MINER:
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:stone", -1, -1, 5, "minecraft:stone"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:coal_ore", -1, -1, 15, "minecraft:coal_ore"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:iron_ore", 5, -1, 30, "minecraft:iron_ore"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:diamond_ore", 10, -1, 100, "minecraft:diamond_ore"));

                config.getUnlocking().add(new UnlockingEntry(UnlockType.ITEM, "minecraft:iron_pickaxe", 5, "Iron Pickaxe", "Unlock iron tier tools"));
                config.getUnlocking().add(new UnlockingEntry(UnlockType.ITEM, "minecraft:diamond_pickaxe", 15, "Diamond Pickaxe", "Unlock diamond tier tools"));
                break;

            case LUMBERJACK:
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:oak_log", -1, -1, 10, "minecraft:oak_log"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:birch_log", -1, -1, 10, "minecraft:birch_log"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:spruce_log", -1, -1, 10, "minecraft:spruce_log"));

                config.getUnlocking().add(new UnlockingEntry(UnlockType.ITEM, "minecraft:iron_axe", 5, "Iron Axe", "Unlock iron tier axes"));
                break;

            case HUNTER:
                config.getXpEarning().add(new XpEarningEntry(ActionType.KILL, "minecraft:zombie", -1, -1, 20, "minecraft:rotten_flesh"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.KILL, "minecraft:skeleton", -1, -1, 25, "minecraft:bone"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.KILL, "minecraft:creeper", -1, -1, 30, "minecraft:gunpowder"));

                config.getUnlocking().add(new UnlockingEntry(UnlockType.ITEM, "minecraft:bow", 5, "Bow", "Unlock ranged weapons"));
                break;

            case FISHER:
                config.getXpEarning().add(new XpEarningEntry(ActionType.FISHING, "minecraft:cod", -1, -1, 15, "minecraft:cod"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.FISHING, "minecraft:salmon", -1, -1, 20, "minecraft:salmon"));

                config.getUnlocking().add(new UnlockingEntry(UnlockType.ITEM, "minecraft:fishing_rod", 1, "Fishing Rod", "Basic fishing tool"));
                break;

            case FARMER:
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:wheat", -1, -1, 5, "minecraft:wheat"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.BREAK, "minecraft:carrots", -1, -1, 5, "minecraft:carrot"));

                config.getUnlocking().add(new UnlockingEntry(UnlockType.ITEM, "minecraft:iron_hoe", 5, "Iron Hoe", "Better farming tools"));
                break;

            case WIZARD:
                config.getXpEarning().add(new XpEarningEntry(ActionType.CRAFT, "minecraft:enchanting_table", -1, -1, 500, "minecraft:enchanting_table"));
                config.getXpEarning().add(new XpEarningEntry(ActionType.USE, "minecraft:experience_bottle", -1, -1, 50, "minecraft:experience_bottle"));

                config.getUnlocking().add(new UnlockingEntry(UnlockType.BLOCK, "minecraft:enchanting_table", 10, "Enchanting", "Unlock enchanting abilities"));
                break;
        }

        return config;
    }

    /**
     * Obtient la configuration d'un métier
     */
    public static JobConfig getConfig(JobType jobType) {
        return CONFIGS.getOrDefault(jobType, createDefaultConfig(jobType));
    }

    /**
     * Recharge toutes les configurations depuis les fichiers
     */
    public static void reload() {
        CONFIGS.clear();
        init();
    }

    /**
     * Obtient toutes les configurations chargées
     */
    public static Map<JobType, JobConfig> getAllConfigs() {
        return new HashMap<>(CONFIGS);
    }
}
