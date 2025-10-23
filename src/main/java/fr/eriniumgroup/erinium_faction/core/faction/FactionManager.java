package fr.eriniumgroup.erinium_faction.core.faction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.data.FactionData;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Central manager for all faction operations
 */
public class FactionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FactionData data = new FactionData();
    private static final Map<UUID, String> playerToFaction = new HashMap<>();
    private static final Map<ClaimKey, String> claimToFaction = new HashMap<>();

    public static void load(MinecraftServer server) {
        Path dataPath = getDataPath(server);
        if (Files.exists(dataPath)) {
            try {
                String json = Files.readString(dataPath);
                data = GSON.fromJson(json, FactionData.class);
                if (data == null) data = new FactionData();
                rebuildLookups();
                LOGGER.info("Loaded {} factions", data.getFactions().size());
            } catch (IOException e) {
                LOGGER.error("Failed to load faction data", e);
                data = new FactionData();
            }
        }
    }

    public static void save(MinecraftServer server) {
        Path dataPath = getDataPath(server);
        try {
            Files.createDirectories(dataPath.getParent());
            String json = GSON.toJson(data);
            Files.writeString(dataPath, json);
            LOGGER.info("Saved {} factions", data.getFactions().size());
        } catch (IOException e) {
            LOGGER.error("Failed to save faction data", e);
        }
    }

    private static Path getDataPath(MinecraftServer server) {
        return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("data")
                .resolve("erinium_faction.json");
    }

    private static void rebuildLookups() {
        playerToFaction.clear();
        claimToFaction.clear();

        for (Faction faction : data.getFactions().values()) {
            String factionName = faction.getName();
            for (UUID playerId : faction.getMembers().keySet()) {
                playerToFaction.put(playerId, factionName);
            }
            for (ClaimKey claim : faction.getClaims()) {
                claimToFaction.put(claim, factionName);
            }
        }
    }

    public static Faction createFaction(String name, UUID ownerId) {
        String normalizedName = name.toLowerCase(Locale.ROOT);
        if (data.getFactions().containsKey(normalizedName)) return null;

        Faction faction = new Faction(normalizedName, ownerId);
        data.getFactions().put(normalizedName, faction);
        playerToFaction.put(ownerId, normalizedName);
        return faction;
    }

    public static boolean disbandFaction(String name) {
        String normalizedName = name.toLowerCase(Locale.ROOT);
        Faction faction = data.getFactions().remove(normalizedName);
        if (faction == null) return false;

        for (UUID playerId : faction.getMembers().keySet()) {
            playerToFaction.remove(playerId);
        }
        for (ClaimKey claim : faction.getClaims()) {
            claimToFaction.remove(claim);
        }
        return true;
    }

    public static Faction getFaction(String name) {
        return data.getFactions().get(name.toLowerCase(Locale.ROOT));
    }

    public static Collection<Faction> getAllFactions() {
        return data.getFactions().values();
    }

    public static boolean factionExists(String name) {
        return data.getFactions().containsKey(name.toLowerCase(Locale.ROOT));
    }

    public static String getPlayerFaction(UUID playerId) {
        return playerToFaction.get(playerId);
    }

    public static Faction getPlayerFactionObject(UUID playerId) {
        String factionName = playerToFaction.get(playerId);
        return factionName != null ? getFaction(factionName) : null;
    }

    public static boolean isInFaction(UUID playerId) {
        return playerToFaction.containsKey(playerId);
    }

    public static boolean addMemberToFaction(String factionName, UUID playerId, Rank rank) {
        Faction faction = getFaction(factionName);
        if (faction == null) return false;

        if (faction.addMember(playerId, rank)) {
            playerToFaction.put(playerId, factionName);
            return true;
        }
        return false;
    }

    public static boolean removeMemberFromFaction(String factionName, UUID playerId) {
        Faction faction = getFaction(factionName);
        if (faction == null) return false;

        if (faction.removeMember(playerId)) {
            playerToFaction.remove(playerId);
            return true;
        }
        return false;
    }

    public static String getClaimOwner(ClaimKey claim) {
        return claimToFaction.get(claim);
    }

    public static boolean isClaimed(ClaimKey claim) {
        return claimToFaction.containsKey(claim);
    }

    public static boolean addClaim(String factionName, ClaimKey claim) {
        if (isClaimed(claim)) return false;

        Faction faction = getFaction(factionName);
        if (faction == null || !faction.canClaimMore()) return false;

        if (faction.addClaim(claim)) {
            claimToFaction.put(claim, factionName);
            return true;
        }
        return false;
    }

    public static boolean removeClaim(String factionName, ClaimKey claim) {
        Faction faction = getFaction(factionName);
        if (faction == null) return false;

        if (faction.removeClaim(claim)) {
            claimToFaction.remove(claim);
            return true;
        }
        return false;
    }

    public static boolean areAllies(String faction1, String faction2) {
        Faction f1 = getFaction(faction1);
        Faction f2 = getFaction(faction2);
        return f1 != null && f2 != null && f1.isAlly(faction2) && f2.isAlly(faction1);
    }

    public static boolean areEnemies(String faction1, String faction2) {
        Faction f1 = getFaction(faction1);
        return f1 != null && f1.isEnemy(faction2);
    }

    public static boolean areSameFaction(UUID player1, UUID player2) {
        String f1 = getPlayerFaction(player1);
        String f2 = getPlayerFaction(player2);
        return f1 != null && f1.equals(f2);
    }
}

