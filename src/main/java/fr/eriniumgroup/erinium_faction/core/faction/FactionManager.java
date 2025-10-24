package fr.eriniumgroup.erinium_faction.core.faction;

import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimsSavedData;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.power.PowerManager;
import fr.eriniumgroup.erinium_faction.core.power.PlayerPower;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/**
 * Gestion à chaud côté serveur pour créer, modifier et sauvegarder les factions.
 */
public final class FactionManager {
    private static MinecraftServer SERVER;

    private FactionManager() {}

    public static void load(MinecraftServer server) {
        SERVER = server;
        // force création/chargement pour log
        FactionSavedData.get(server);
        EFC.log.info("Factions", "§aFactions chargées pour le monde");
    }

    public static void save(MinecraftServer server) {
        var data = FactionSavedData.get(server);
        data.setDirty();
        EFC.log.info("Factions", "§2Factions sauvegardées");
    }

    // Query helpers ----------------------------------------------------------
    private static Map<String, Faction> map() { return FactionSavedData.get(SERVER).getFactions(); }

    public static boolean factionExists(String nameOrId) {
        if (SERVER == null) return false;
        String key = nameOrId.toLowerCase(Locale.ROOT);
        return map().containsKey(key) || map().values().stream().anyMatch(f -> f.getName().equalsIgnoreCase(nameOrId));
    }

    public static Collection<Faction> getAllFactions() {
        return map().values();
    }

    public static Faction getById(String id) { return map().get(id.toLowerCase(Locale.ROOT)); }

    public static Faction getByName(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        for (Faction f : map().values()) {
            if (f.getName().equalsIgnoreCase(name) || f.getId().equalsIgnoreCase(n)) return f;
        }
        return null;
    }

    public static Faction getFactionOf(UUID player) {
        for (Faction f : map().values()) {
            if (f.getMembers().containsKey(player)) return f;
        }
        return null;
    }

    // Create/Delete ----------------------------------------------------------
    public static synchronized Faction create(String name, String tag, UUID owner) {
        if (SERVER == null) return null;
        int min = EFConfig.FACTION_NAME_MIN.get();
        int max = EFConfig.FACTION_NAME_MAX.get();
        if (name == null || name.length() < min || name.length() > max) return null;
        if (factionExists(name)) return null;
        String id = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
        if (id.isBlank()) return null;
        Faction f = new Faction(id, name, tag, owner);
        // config power max initiale
        f.setMaxPower(EFConfig.FACTION_BASE_MAX_POWER.get());
        f.setPower(EFConfig.FACTION_BASE_MAX_POWER.get() / 2.0);
        map().put(f.getId(), f);
        FactionSavedData.get(SERVER).setDirty();
        return f;
    }

    public static synchronized boolean delete(String nameOrId) {
        if (SERVER == null) return false;
        Faction f = getByName(nameOrId);
        if (f == null) return false;
        boolean ok = map().remove(f.getId()) != null;
        if (ok) FactionSavedData.get(SERVER).setDirty();
        return ok;
    }

    // Membership -------------------------------------------------------------
    public static boolean invite(Faction f, UUID player, String name) {
        if (f == null) return false;
        int max = getMaxMembersFor(f);
        if (f.getMembers().size() >= max) return false;
        boolean ok = f.addMember(player, name, "recruit");
        if (ok) {
            // Additionner le power du joueur à la faction
            ServerPlayer sp = SERVER.getPlayerList().getPlayer(player);
            if (sp != null) {
                PlayerPower pp = PowerManager.get(sp);
                f.setPower(f.getPower() + pp.getPower());
                if (EFConfig.FACTION_MAX_POWER_FROM_PLAYERS.get()) {
                    f.setMaxPower(f.getMaxPower() + pp.getMaxPower());
                }
            }
            FactionSavedData.get(SERVER).setDirty();
        }
        return ok;
    }

    public static boolean kick(Faction f, UUID player) {
        if (f == null) return false;
        boolean ok = f.removeMember(player);
        if (ok) {
            ServerPlayer sp = SERVER.getPlayerList().getPlayer(player);
            if (sp != null) {
                PlayerPower pp = PowerManager.get(sp);
                f.setPower(Math.max(0, f.getPower() - pp.getPower()));
                if (EFConfig.FACTION_MAX_POWER_FROM_PLAYERS.get()) {
                    f.setMaxPower(Math.max(0, f.getMaxPower() - pp.getMaxPower()));
                }
            }
            FactionSavedData.get(SERVER).setDirty();
        }
        return ok;
    }

    public static void onMemberDeath(Faction f, ServerPlayer player) {
        if (f == null) return;
        double loss = EFConfig.POWER_LOSS_ON_DEATH.get();
        f.setPower(f.getPower() - loss);
        FactionSavedData.get(SERVER).setDirty();
    }

    public static void tickMinute() {
        if (SERVER == null) return;
        double regen = EFConfig.POWER_REGEN_PER_MINUTE.get();
        for (Faction f : map().values()) {
            f.setPower(f.getPower() + regen);
        }
        FactionSavedData.get(SERVER).setDirty();
    }

    // Client sync helpers ----------------------------------------------------
    public static void populatePlayerVariables(ServerPlayer player, fr.eriniumgroup.erinium_faction.common.network.EFVariables.PlayerVariables vars) {
        Faction f = getFactionOf(player.getUUID());
        if (f != null) {
            vars.factionId = f.getId();
            vars.factionName = f.getName();
            vars.factionPower = f.getPower();
            vars.factionMaxPower = f.getMaxPower();
            vars.factionLevel = f.getLevel();
            vars.factionXp = f.getXp();
        } else {
            vars.factionId = "";
            vars.factionName = "";
            vars.factionPower = 0;
            vars.factionMaxPower = 0;
            vars.factionLevel = 0;
            vars.factionXp = 0;
        }
        // rank serveur
        var r = fr.eriniumgroup.erinium_faction.core.rank.EFRManager.get().getPlayerRank(player.getUUID());
        vars.serverRankId = r != null ? r.id : "";
        // power joueur
        PlayerPower pp = PowerManager.get(player);
        vars.playerPower = pp.getPower();
        vars.playerMaxPower = pp.getMaxPower();
    }

    public static String getPlayerFaction(UUID player) {
        Faction f = getFactionOf(player);
        return f != null ? f.getName() : null;
    }

    public static Faction getPlayerFactionObject(UUID player) {
        return getFactionOf(player);
    }

    public static boolean areSameFaction(UUID a, UUID b) {
        Faction fa = getFactionOf(a);
        Faction fb = getFactionOf(b);
        return fa != null && fb != null && Objects.equals(fa.getId(), fb.getId());
    }

    public static boolean areAllies(String factionA, String factionB) {
        // Alliances non implémentées pour l’instant
        return false;
    }

    public static Faction getFaction(String nameOrId) {
        Faction f = getByName(nameOrId);
        if (f == null) f = getById(nameOrId);
        return f;
    }

    // Claims -----------------------------------------------------------------
    public static boolean isClaimed(ClaimKey key) {
        if (SERVER == null) return false;
        return ClaimsSavedData.get(SERVER).isClaimed(key);
    }

    public static String getClaimOwner(ClaimKey key) {
        if (SERVER == null) return null;
        return ClaimsSavedData.get(SERVER).getOwner(key);
    }

    public static int countClaims(String factionId) {
        if (SERVER == null) return 0;
        return ClaimsSavedData.get(SERVER).countClaims(factionId);
    }

    public static boolean tryClaim(ClaimKey key, String factionId) {
        if (SERVER == null) return false;
        var data = ClaimsSavedData.get(SERVER);
        if (data.isClaimed(key)) return false;
        // Limite config
        if (countClaims(factionId) >= EFConfig.FACTION_MAX_CLAIMS.get()) return false;
        boolean ok = data.claim(key, factionId);
        return ok;
    }

    public static boolean tryUnclaim(ClaimKey key, String factionId) {
        if (SERVER == null) return false;
        var data = ClaimsSavedData.get(SERVER);
        return data.unclaim(key, factionId);
    }

    public static int getMaxMembersFor(Faction f) {
        int base = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_BASE_MAX_PLAYERS.get();
        int perLvl = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_PLAYERS_PER_LEVEL.get();
        int hardCap = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_MAX_MEMBERS.get();
        int computed = base + Math.max(0, f.getLevel()) * Math.max(0, perLvl);
        return Math.min(hardCap, computed);
    }
}
