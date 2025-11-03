package fr.eriniumgroup.erinium_faction.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Configuration persistante pour l'overlay de minimap
 */
public class MinimapOverlayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "config/erinium_faction/minimap_overlay.json";

    // Paramètres d'affichage
    public boolean enabled = false;
    public boolean roundShape = false; // false = carré, true = rond
    public int size = 128; // Taille de la minimap en pixels
    public int cellSize = 8; // Taille d'un chunk en pixels
    public Position position = Position.TOP_RIGHT;

    // Couleurs (reprises du MapConfig)
    public int gridColor = 0x66FFFFFF;
    public int crossColor = 0xFFFFFFFF;

    // Position de l'overlay
    public enum Position {
        TOP_LEFT(10, 10),
        TOP_RIGHT(-10, 10),
        BOTTOM_LEFT(10, -10),
        BOTTOM_RIGHT(-10, -10);

        public final int xOffset; // négatif = depuis la droite
        public final int yOffset; // négatif = depuis le bas

        Position(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }

    public static MinimapOverlayConfig load() {
        File configFile = new File(Minecraft.getInstance().gameDirectory, CONFIG_FILE);

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                MinimapOverlayConfig loaded = GSON.fromJson(reader, MinimapOverlayConfig.class);
                if (loaded != null) return loaded;
            } catch (IOException e) {
                System.err.println("Failed to load minimap overlay settings: " + e.getMessage());
            }
        }
        return new MinimapOverlayConfig();
    }

    public void save() {
        File configFile = new File(Minecraft.getInstance().gameDirectory, CONFIG_FILE);
        configFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("Failed to save minimap overlay settings: " + e.getMessage());
        }
    }

    public int[] getScreenPosition(int screenWidth, int screenHeight) {
        int x = position.xOffset >= 0 ? position.xOffset : screenWidth + position.xOffset - size;
        int y = position.yOffset >= 0 ? position.yOffset : screenHeight + position.yOffset - size;
        return new int[]{x, y};
    }
}
