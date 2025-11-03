package fr.eriniumgroup.erinium_faction.features.minimap;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuration de la minimap (constantes et position sauvegardée)
 */
public class MinimapConfig {
    // Taille de la minimap overlay (configurable)
    public static final int MIN_SIZE = 80;
    public static final int MAX_SIZE = 300;
    public static final int DEFAULT_SIZE = 120;

    public static int MINIMAP_FRAME_SIZE = DEFAULT_SIZE;
    public static int MINIMAP_SIZE = DEFAULT_SIZE - 10;

    // Position de la minimap (sauvegardée)
    public static int minimapX = 10;
    public static int minimapY = 10;

    // Mode de dragging
    public static boolean isDraggingMinimap = false;
    public static int dragOffsetX = 0;
    public static int dragOffsetY = 0;

    private static final String CONFIG_FILE = "minimap_client_config.properties";

    // Zoom
    public static final float DEFAULT_ZOOM = 1.0f;
    public static final float MIN_ZOOM = 0.5f;
    public static final float MAX_ZOOM = 4.0f;
    public static float currentZoom = DEFAULT_ZOOM;

    // Nombre maximum de waypoints
    public static final int MAX_WAYPOINTS = 100;

    // Waypoint overlay distance
    public static final int DEFAULT_WAYPOINT_OVERLAY_DISTANCE = 1000; // Distance en blocs
    public static final int MIN_WAYPOINT_OVERLAY_DISTANCE = 100;
    public static final int MAX_WAYPOINT_OVERLAY_DISTANCE = 5000;
    public static int waypointOverlayDistance = DEFAULT_WAYPOINT_OVERLAY_DISTANCE;

    // Rendu
    public static final int CHUNK_CACHE_SIZE = 256; // Nombre de chunks à garder en cache
    public static final int RENDER_DISTANCE_CHUNKS = 16; // Distance de rendu en chunks
    public static final int UPDATE_INTERVAL_TICKS = 10; // Mise à jour tous les 10 ticks (0.5s)

    // Rayon d'update des chunks (configurable)
    public static int CHUNK_UPDATE_RADIUS = 8; // Rayon en chunks autour du joueur à regénérer

    // Couleurs de terrain (valeurs RGB)
    public static final int COLOR_GRASS = 0xFF7CFC00;
    public static final int COLOR_STONE = 0xFF808080;
    public static final int COLOR_WATER = 0xFF1E90FF;
    public static final int COLOR_SAND = 0xFFF4A460;
    public static final int COLOR_SNOW = 0xFFFFFAFA;
    public static final int COLOR_DIRT = 0xFF8B4513;
    public static final int COLOR_WOOD = 0xFF654321;
    public static final int COLOR_LEAVES = 0xFF228B22;
    public static final int COLOR_LAVA = 0xFFFF4500;
    public static final int COLOR_UNKNOWN = 0xFF404040;

    /**
     * Sauvegarde la position et taille de la minimap dans un fichier
     */
    public static void savePosition() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameDirectory == null) {
                return;
            }

            Path configPath = mc.gameDirectory.toPath().resolve("config").resolve("erinium_faction");
            Files.createDirectories(configPath);

            File configFile = configPath.resolve(CONFIG_FILE).toFile();
            Properties props = new Properties();
            props.setProperty("minimapX", String.valueOf(minimapX));
            props.setProperty("minimapY", String.valueOf(minimapY));
            props.setProperty("minimapSize", String.valueOf(MINIMAP_FRAME_SIZE));
            props.setProperty("chunkUpdateRadius", String.valueOf(CHUNK_UPDATE_RADIUS));
            props.setProperty("waypointOverlayDistance", String.valueOf(waypointOverlayDistance));

            try (FileOutputStream out = new FileOutputStream(configFile)) {
                props.store(out, "Minimap HUD Position and Size");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge la position et taille de la minimap depuis le fichier
     */
    public static void loadPosition() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameDirectory == null) {
                return;
            }

            Path configPath = mc.gameDirectory.toPath().resolve("config").resolve("erinium_faction");
            File configFile = configPath.resolve(CONFIG_FILE).toFile();

            if (!configFile.exists()) {
                savePosition(); // Créer le fichier avec les valeurs par défaut
                return;
            }

            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
                minimapX = Integer.parseInt(props.getProperty("minimapX", "10"));
                minimapY = Integer.parseInt(props.getProperty("minimapY", "10"));
                int size = Integer.parseInt(props.getProperty("minimapSize", String.valueOf(DEFAULT_SIZE)));
                setMinimapSize(size);
                CHUNK_UPDATE_RADIUS = Integer.parseInt(props.getProperty("chunkUpdateRadius", "8"));
                waypointOverlayDistance = Integer.parseInt(props.getProperty("waypointOverlayDistance", String.valueOf(DEFAULT_WAYPOINT_OVERLAY_DISTANCE)));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Change la taille de la minimap
     */
    public static void setMinimapSize(int size) {
        MINIMAP_FRAME_SIZE = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size));
        MINIMAP_SIZE = MINIMAP_FRAME_SIZE - 10;
    }

    /**
     * Augmente la taille de la minimap
     */
    public static void increaseSize() {
        setMinimapSize(MINIMAP_FRAME_SIZE + 10);
        savePosition();
    }

    /**
     * Diminue la taille de la minimap
     */
    public static void decreaseSize() {
        setMinimapSize(MINIMAP_FRAME_SIZE - 10);
        savePosition();
    }

    /**
     * Augmente le zoom de la minimap (voit plus proche, plus de détails)
     */
    public static void increaseZoom() {
        currentZoom = Math.min(MAX_ZOOM, currentZoom * 1.2f);
    }

    /**
     * Diminue le zoom de la minimap (voit plus loin, moins de détails)
     */
    public static void decreaseZoom() {
        currentZoom = Math.max(MIN_ZOOM, currentZoom / 1.2f);
    }
}
