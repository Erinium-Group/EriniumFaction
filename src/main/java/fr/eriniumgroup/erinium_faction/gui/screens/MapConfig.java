package fr.eriniumgroup.erinium_faction.gui.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Configuration pour la carte des factions (sauvegarde des couleurs)
 */
public class MapConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "config/erinium_faction/map_settings.json";

    public int gridColor = 0x66FFFFFF; // Blanc semi-transparent par défaut
    public int crossColor = 0xFFFFFFFF; // Blanc par défaut

    /**
     * Charger la configuration depuis le fichier
     */
    public static MapConfig load() {
        File configFile = new File(Minecraft.getInstance().gameDirectory, CONFIG_FILE);

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                return GSON.fromJson(reader, MapConfig.class);
            } catch (IOException e) {
                System.err.println("Failed to load map settings: " + e.getMessage());
            }
        }

        // Retourner config par défaut si le fichier n'existe pas
        return new MapConfig();
    }

    /**
     * Sauvegarder la configuration dans le fichier
     */
    public void save() {
        File configFile = new File(Minecraft.getInstance().gameDirectory, CONFIG_FILE);

        // Créer le dossier parent si nécessaire
        configFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("Failed to save map settings: " + e.getMessage());
        }
    }
}
