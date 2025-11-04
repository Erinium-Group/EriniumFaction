package fr.eriniumgroup.erinium_faction.client.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gestionnaire des waypoints côté client
 */
public class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_WAYPOINTS = 100; // Limite max de waypoints

    private final List<Waypoint> waypoints = new ArrayList<>();
    private final String playerUUID;

    public WaypointManager(String playerUUID) {
        this.playerUUID = playerUUID;
        load();
    }

    /**
     * Obtenir le fichier de sauvegarde pour ce joueur
     */
    private File getSaveFile() {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config/erinium_faction/waypoints");
        configDir.mkdirs();
        return new File(configDir, playerUUID + ".json");
    }

    /**
     * Charger les waypoints depuis le fichier
     */
    public void load() {
        File file = getSaveFile();
        if (!file.exists()) {
            waypoints.clear();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Waypoint>>(){}.getType();
            List<Waypoint> loaded = GSON.fromJson(reader, listType);
            waypoints.clear();
            if (loaded != null) {
                waypoints.addAll(loaded);
            }
        } catch (IOException e) {
            System.err.println("Failed to load waypoints: " + e.getMessage());
        }
    }

    /**
     * Sauvegarder les waypoints dans le fichier
     */
    public void save() {
        File file = getSaveFile();
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(waypoints, writer);
        } catch (IOException e) {
            System.err.println("Failed to save waypoints: " + e.getMessage());
        }
    }

    /**
     * Ajouter un waypoint
     */
    public boolean addWaypoint(Waypoint waypoint) {
        if (waypoints.size() >= MAX_WAYPOINTS) {
            return false; // Limite atteinte
        }
        waypoints.add(waypoint);
        save();
        return true;
    }

    /**
     * Supprimer un waypoint
     */
    public void removeWaypoint(Waypoint waypoint) {
        waypoints.remove(waypoint);
        save();
    }

    /**
     * Mettre à jour un waypoint existant
     */
    public void updateWaypoint(Waypoint waypoint) {
        save();
    }

    /**
     * Obtenir tous les waypoints
     */
    public List<Waypoint> getAllWaypoints() {
        return new ArrayList<>(waypoints);
    }

    /**
     * Obtenir les waypoints pour une dimension donnée
     */
    public List<Waypoint> getWaypointsForDimension(String dimension) {
        return waypoints.stream()
                .filter(w -> w.getDimension().equals(dimension))
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les waypoints visibles pour une dimension donnée
     */
    public List<Waypoint> getVisibleWaypointsForDimension(String dimension) {
        return waypoints.stream()
                .filter(w -> w.getDimension().equals(dimension) && w.isVisible())
                .collect(Collectors.toList());
    }

    /**
     * Obtenir le nombre maximum de waypoints
     */
    public static int getMaxWaypoints() {
        return MAX_WAYPOINTS;
    }

    /**
     * Vérifier si on peut ajouter un waypoint
     */
    public boolean canAddWaypoint() {
        return waypoints.size() < MAX_WAYPOINTS;
    }

    /**
     * Obtenir le nombre de waypoints
     */
    public int getWaypointCount() {
        return waypoints.size();
    }
}
