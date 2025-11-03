package fr.eriniumgroup.erinium_faction.features.minimap;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Gestionnaire des waypoints (client-side)
 */
public class WaypointManager {
    private static final WaypointManager INSTANCE = new WaypointManager();

    private final Map<UUID, Waypoint> waypoints = new LinkedHashMap<>();

    private WaypointManager() {}

    public static WaypointManager getInstance() {
        return INSTANCE;
    }

    public Waypoint addWaypoint(String name, BlockPos position, ResourceKey<Level> dimension, Waypoint.WaypointColor color) {
        if (waypoints.size() >= MinimapConfig.MAX_WAYPOINTS) {
            return null;
        }

        UUID id = UUID.randomUUID();
        Waypoint waypoint = new Waypoint(id, name, position, dimension, color);
        waypoints.put(id, waypoint);
        return waypoint;
    }

    public void removeWaypoint(UUID id) {
        waypoints.remove(id);
    }

    public Waypoint getWaypoint(UUID id) {
        return waypoints.get(id);
    }

    public Collection<Waypoint> getAllWaypoints() {
        return waypoints.values();
    }

    public List<Waypoint> getWaypointsForDimension(ResourceKey<Level> dimension) {
        List<Waypoint> result = new ArrayList<>();
        for (Waypoint waypoint : waypoints.values()) {
            if (waypoint.getDimension().equals(dimension) && waypoint.isEnabled()) {
                result.add(waypoint);
            }
        }
        return result;
    }

    public void clear() {
        waypoints.clear();
    }

    /**
     * Nettoie TOUS les waypoints (pour le sync)
     */
    public void clearAllWaypoints() {
        waypoints.clear();
    }

    /**
     * Ajoute un waypoint directement (pour le sync depuis le serveur)
     */
    public void addWaypointDirect(Waypoint waypoint) {
        waypoints.put(waypoint.getId(), waypoint);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Waypoint waypoint : waypoints.values()) {
            list.add(waypoint.toNBT());
        }
        tag.put("waypoints", list);
        return tag;
    }

    public void fromNBT(CompoundTag tag) {
        waypoints.clear();
        if (tag.contains("waypoints", Tag.TAG_LIST)) {
            ListTag list = tag.getList("waypoints", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag waypointTag = list.getCompound(i);
                Waypoint waypoint = Waypoint.fromNBT(waypointTag);
                waypoints.put(waypoint.getId(), waypoint);
            }
        }
    }

    public int getWaypointCount() {
        return waypoints.size();
    }
}
