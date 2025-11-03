package fr.eriniumgroup.erinium_faction.features.minimap;

import fr.eriniumgroup.erinium_faction.common.network.packets.WaypointSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Gestionnaire des waypoints côté serveur (SavedData)
 */
public class WaypointServerManager extends SavedData {
    private static final String DATA_NAME = "erinium_faction_waypoints";
    private static WaypointServerManager INSTANCE;

    // Map des waypoints par joueur UUID -> List<Waypoint>
    private final Map<UUID, List<Waypoint>> playerWaypoints = new HashMap<>();

    public WaypointServerManager() {}

    public static WaypointServerManager getInstance() {
        return INSTANCE;
    }

    public static WaypointServerManager load(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        INSTANCE = storage.computeIfAbsent(
            new Factory<>(WaypointServerManager::new, WaypointServerManager::load, null),
            DATA_NAME
        );
        return INSTANCE;
    }

    private static WaypointServerManager load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        WaypointServerManager manager = new WaypointServerManager();

        if (tag.contains("players", Tag.TAG_LIST)) {
            ListTag playersList = tag.getList("players", Tag.TAG_COMPOUND);
            for (int i = 0; i < playersList.size(); i++) {
                CompoundTag playerTag = playersList.getCompound(i);
                UUID playerUUID = playerTag.getUUID("uuid");
                List<Waypoint> waypoints = new ArrayList<>();

                if (playerTag.contains("waypoints", Tag.TAG_LIST)) {
                    ListTag waypointsList = playerTag.getList("waypoints", Tag.TAG_COMPOUND);
                    for (int j = 0; j < waypointsList.size(); j++) {
                        waypoints.add(Waypoint.fromNBT(waypointsList.getCompound(j)));
                    }
                }

                manager.playerWaypoints.put(playerUUID, waypoints);
            }
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        ListTag playersList = new ListTag();

        for (Map.Entry<UUID, List<Waypoint>> entry : playerWaypoints.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());

            ListTag waypointsList = new ListTag();
            for (Waypoint waypoint : entry.getValue()) {
                waypointsList.add(waypoint.toNBT());
            }

            playerTag.put("waypoints", waypointsList);
            playersList.add(playerTag);
        }

        tag.put("players", playersList);
        return tag;
    }

    public Waypoint addWaypoint(UUID playerUUID, String name, BlockPos position, ResourceKey<Level> dimension, Waypoint.WaypointColor color, int customRGB, boolean enabled) {
        List<Waypoint> waypoints = playerWaypoints.computeIfAbsent(playerUUID, k -> new ArrayList<>());

        if (waypoints.size() >= MinimapConfig.MAX_WAYPOINTS) {
            return null;
        }

        UUID waypointId = UUID.randomUUID();
        Waypoint waypoint = new Waypoint(waypointId, name, position, dimension, customRGB);
        waypoint.setColor(color);
        if (color == Waypoint.WaypointColor.CUSTOM) {
            waypoint.setCustomColor(customRGB);
        }
        waypoint.setEnabled(enabled);
        waypoints.add(waypoint);
        setDirty();
        return waypoint;
    }

    public void removeWaypoint(UUID playerUUID, UUID waypointId) {
        List<Waypoint> waypoints = playerWaypoints.get(playerUUID);
        if (waypoints != null) {
            waypoints.removeIf(w -> w.getId().equals(waypointId));
            setDirty();
        }
    }

    public void updateWaypoint(UUID playerUUID, UUID waypointId, String name, BlockPos position, Waypoint.WaypointColor color, int customRGB, boolean enabled) {
        List<Waypoint> waypoints = playerWaypoints.get(playerUUID);
        if (waypoints != null) {
            for (Waypoint waypoint : waypoints) {
                if (waypoint.getId().equals(waypointId)) {
                    waypoint.setName(name);
                    waypoint.setPosition(position);
                    waypoint.setColor(color);
                    if (color == Waypoint.WaypointColor.CUSTOM) {
                        waypoint.setCustomColor(customRGB);
                    }
                    waypoint.setEnabled(enabled);
                    setDirty();
                    break;
                }
            }
        }
    }

    public List<Waypoint> getWaypoints(UUID playerUUID) {
        return playerWaypoints.getOrDefault(playerUUID, Collections.emptyList());
    }

    public void syncToPlayer(ServerPlayer player) {
        List<Waypoint> waypoints = getWaypoints(player.getUUID());
        List<WaypointSyncPacket.WaypointData> data = new ArrayList<>();

        for (Waypoint waypoint : waypoints) {
            data.add(new WaypointSyncPacket.WaypointData(
                waypoint.getId(),
                waypoint.getName(),
                waypoint.getPosition(),
                waypoint.getDimension(),
                waypoint.getColor(),
                waypoint.getCustomColor(),
                waypoint.isEnabled()
            ));
        }

        PacketDistributor.sendToPlayer(player, new WaypointSyncPacket(data));
    }
}
