package fr.eriniumgroup.erinium_faction.core.faction;

import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimsSavedData;
import fr.eriniumgroup.erinium_faction.core.power.PlayerPower;
import fr.eriniumgroup.erinium_faction.core.power.PowerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Gestion à chaud côté serveur pour créer, modifier et sauvegarder les factions.
 */
public final class FactionManager {
    private static MinecraftServer SERVER;

    private FactionManager() {
    }

    public static void load(MinecraftServer server) {
        SERVER = server;
        // force création/chargement pour log
        FactionSavedData.get(server);
        EFC.log.info("§5Factions", "§aFactions chargées pour le monde");
    }

    public static void save(MinecraftServer server) {
        var data = FactionSavedData.get(server);
        data.setDirty();
        EFC.log.info("§5Factions", "§2Factions sauvegardées");
    }

    // Query helpers ----------------------------------------------------------
    private static Map<String, Faction> map() {
        return FactionSavedData.get(SERVER).getFactions();
    }

    public static boolean factionExists(String nameOrId) {
        if (SERVER == null) return false;
        String key = nameOrId.toLowerCase(Locale.ROOT);
        return map().containsKey(key) || map().values().stream().anyMatch(f -> f.getName().equalsIgnoreCase(nameOrId));
    }

    public static Collection<Faction> getAllFactions() {
        return map().values();
    }

    public static Faction getById(String id) {
        return map().get(id.toLowerCase(Locale.ROOT));
    }

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
        int memberCount = f.getMembers().size();
        // Supprimer la faction de la map en premier pour éviter ré-entrance
        boolean ok = map().remove(f.getId()) != null;
        if (ok) {
            // 1) Unclaim tous les chunks de cette faction (compter pour logs)
            int removedClaims = ClaimsSavedData.get(SERVER).unclaimAllForFaction(f.getId());

            // 2) Reset variables côté membres connectés, overlay wilderness
            for (UUID m : new ArrayList<>(f.getMembers().keySet())) {
                ServerPlayer sp = SERVER.getPlayerList().getPlayer(m);
                if (sp != null) {
                    var vars = sp.getData(fr.eriniumgroup.erinium_faction.common.network.EFVariables.PLAYER_VARIABLES);
                    vars.factionId = "";
                    vars.factionName = "";
                    vars.factionPower = 0;
                    vars.factionMaxPower = 0;
                    vars.factionLevel = 0;
                    vars.factionXp = 0;
                    vars.factionInChunk = ""; // force la prochaine détection à wilderness
                    vars.syncPlayerVariables(sp);
                    PacketDistributor.sendToPlayer(sp, new fr.eriniumgroup.erinium_faction.common.network.packets.FactionTitlePacket(Component.translatable("erinium_faction.wilderness.title").getString(), Component.translatable("erinium_faction.wilderness.desc").getString(), 300, 900, 300, "wilderness"));
                }
            }

            // 3) Broadcast global au chat (traduisible)
            MutableComponent bc = Component.translatable("erinium_faction.broadcast.disband", f.getName(), removedClaims, memberCount);
            for (ServerPlayer p : SERVER.getPlayerList().getPlayers()) {
                p.sendSystemMessage(bc);
            }

            // 4) Log admin (console) avec couleurs
            EFC.log.info("§5Faction", "§cDISBAND §7: §e" + f.getName() + " §7(§6" + removedClaims + " claims§7, §b" + memberCount + " membres§7)");

            // 5) Message admin aux OPs en jeu (stylé)
            MutableComponent adminMsg = Component.literal("[Admin] ").withStyle(ChatFormatting.DARK_RED).append(Component.literal("DISBAND ").withStyle(ChatFormatting.RED)).append(Component.literal(f.getName()).withStyle(ChatFormatting.GOLD)).append(Component.literal(" (")).append(Component.literal(String.valueOf(removedClaims)).withStyle(ChatFormatting.GOLD)).append(Component.literal(" claims, ")).append(Component.literal(String.valueOf(memberCount)).withStyle(ChatFormatting.AQUA)).append(Component.literal(" membres)"));
            for (ServerPlayer p : SERVER.getPlayerList().getPlayers()) {
                if (p.hasPermissions(2)) p.sendSystemMessage(adminMsg);
            }

            FactionSavedData.get(SERVER).setDirty();
        }
        return ok;
    }

    /**
     * Disband par le leader: même logique que delete, mais autorisé seulement si caller est owner.
     */
    public static synchronized boolean disbandByLeader(ServerPlayer caller) {
        if (SERVER == null || caller == null) return false;
        Faction f = getFactionOf(caller.getUUID());
        if (f == null) return false;
        if (!Objects.equals(f.getOwner(), caller.getUUID())) return false; // doit être leader
        // délègue à delete avec l’id
        return delete(f.getId());
    }

    // Membership -------------------------------------------------------------
    public static boolean invite(Faction f, UUID player, String name) {
        if (f == null) return false;
        int max = getMaxMembersFor(f);
        if (f.getMembers().size() >= max) return false;
        boolean ok = f.addMember(player, name, "recruit");
        if (ok) {
            // Ajouter FACTION_POWER_PER_MEMBER au power de la faction
            double powerPerMember = EFConfig.FACTION_POWER_PER_MEMBER.get();
            f.setPower(f.getPower() + powerPerMember);
            f.setMaxPower(f.getMaxPower() + powerPerMember);
            FactionSavedData.get(SERVER).setDirty();
        }
        return ok;
    }

    public static boolean kick(Faction f, UUID player) {
        if (f == null) return false;
        boolean ok = f.removeMember(player);
        if (ok) {
            // Soustraire FACTION_POWER_PER_MEMBER du power de la faction
            double powerPerMember = EFConfig.FACTION_POWER_PER_MEMBER.get();
            f.setMaxPower(Math.max(0, f.getMaxPower() - powerPerMember));
            f.setPower(Math.max(0, Math.min(f.getPower(), f.getMaxPower())));
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
        // argent joueur (attachment)
        vars.money = fr.eriniumgroup.erinium_faction.integration.economy.EconomyIntegration.getBalance(player);
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
        return ClaimsSavedData.get(SERVER).getOwner(key) != null ? ClaimsSavedData.get(SERVER).getOwner(key) : "wilderness";
    }

    public static int countClaims(String factionId) {
        if (SERVER == null) return 0;
        return ClaimsSavedData.get(SERVER).countClaims(factionId);
    }

    public static boolean tryClaim(ClaimKey key, String factionId) {
        if (SERVER == null) return false;

        Faction pfaction = FactionManager.getFaction(factionId);
        if (pfaction == null) return false; // ✅ Vérification null

        int currentClaims = countClaims(factionId);
        if (currentClaims + 1 > pfaction.getPower() || currentClaims + 1 > pfaction.getMaxPower()) {
            return false;
        }

        var data = ClaimsSavedData.get(SERVER);

        if (data.isClaimed(key)) {
            String ownerFactionId = FactionManager.getClaimOwner(key); // ✅ Utilise directement key
            if (ownerFactionId == null) return false;

            Faction enemyFaction = FactionManager.getFaction(ownerFactionId);
            if (enemyFaction == null) return false; // ✅ Vérification null

            // Vérifier si c'est une zone protégée
            if (enemyFaction.getId().equals("safezone") || enemyFaction.getId().equals("warzone") || enemyFaction.isAdminFaction() || enemyFaction.isSafezone() || enemyFaction.isWarzone()) {
                return false;
            }

            // Si la faction ennemie a moins de power que de claims, on peut overclaim
            if (enemyFaction.getPower() < countClaims(enemyFaction.getId())) {
                tryUnclaim(key, enemyFaction.getId());
                return data.claim(key, factionId); // ✅ Retour direct
            }

            return false;
        }

        return data.claim(key, factionId); // ✅ Retour direct
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

    public static void markDirty() {
        if (SERVER != null) {
            FactionSavedData.get(SERVER).setDirty();
        }
    }

    /**
     * --- Récupérer tous les claims d'une faction ---
     */
    public static List<ClaimKey> getClaimsOfFaction(String factionId) {
        if (SERVER == null || factionId == null || factionId.isBlank()) return Collections.emptyList();
        return ClaimsSavedData.get(SERVER).listClaimsForFaction(factionId.toLowerCase(Locale.ROOT));
    }

    /**
     * Variante utilitaire: par objet Faction
     */
    public static List<ClaimKey> getClaimsOfFaction(Faction faction) {
        if (faction == null) return Collections.emptyList();
        return getClaimsOfFaction(faction.getId());
    }

    // --- Helpers claim-perms exposés aux commandes ---
    public static Map<String, Set<String>> getClaimPerms(ClaimKey key) {
        if (SERVER == null) return Collections.emptyMap();
        return ClaimsSavedData.get(SERVER).getClaimPerms(key);
    }

    public static Set<String> getClaimPermsForRank(ClaimKey key, String rankId) {
        if (SERVER == null) return Collections.emptySet();
        return ClaimsSavedData.get(SERVER).getClaimPermsForRank(key, rankId);
    }

    public static boolean addClaimPerm(ClaimKey key, String rankId, String perm) {
        if (SERVER == null) return false;
        return ClaimsSavedData.get(SERVER).addClaimPerm(key, rankId, perm);
    }

    public static boolean removeClaimPerm(ClaimKey key, String rankId, String perm) {
        if (SERVER == null) return false;
        return ClaimsSavedData.get(SERVER).removeClaimPerm(key, rankId, perm);
    }

    public static boolean clearClaimPerms(ClaimKey key) {
        if (SERVER == null) return false;
        return ClaimsSavedData.get(SERVER).clearClaimPerms(key);
    }

    public static boolean sendInvite(Faction f, UUID playerUuid) {
        if (f == null || playerUuid == null) return false;
        if (f.getMembers().containsKey(playerUuid)) return false; // déjà membre
        // ajouter en tant qu'invité (set invited flag)
        boolean ok = f.invitePlayer(playerUuid);
        if (ok) FactionSavedData.get(SERVER).setDirty();

        // envoyer notification clickable si le joueur est en ligne
        try {
            ServerPlayer target = SERVER.getPlayerList().getPlayer(playerUuid);
            if (target != null) {
                // Construire message: "You have been invited to <Faction>. Click to accept"
                String cmd = "/f join " + f.getName();
                net.minecraft.network.chat.Component msg = net.minecraft.network.chat.Component.literal("")
                        .append(net.minecraft.network.chat.Component.translatable("erinium_faction.msg.invite.header", f.getName()))
                        .append(net.minecraft.network.chat.Component.literal(" "))
                        .append(net.minecraft.network.chat.Component.literal("[Accept]").withStyle(s -> s.withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, cmd)).withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.translatable("erinium_faction.msg.invite.hover")))));
                target.sendSystemMessage(msg);
            }
        } catch (Exception ignored) {}

        return ok;
    }

    public static boolean revokeInvite(Faction f, UUID playerUuid) {
        if (f == null || playerUuid == null) return false;
        boolean ok = f.revokeInvite(playerUuid);
        if (ok) FactionSavedData.get(SERVER).setDirty();
        return ok;
    }

    public static boolean isInvited(Faction f, UUID playerUuid) {
        if (f == null || playerUuid == null) return false;
        return f.isPlayerInvited(playerUuid);
    }

    public static boolean acceptInvite(Faction f, UUID playerUuid, String playerName) {
        if (f == null || playerUuid == null) return false;
        if (f.getMembers().containsKey(playerUuid)) return false; // déjà membre
        if (!f.isPlayerInvited(playerUuid)) return false; // doit être invité
        int max = getMaxMembersFor(f);
        if (f.getMembers().size() >= max) return false; // full
        boolean added = f.addMember(playerUuid, playerName, "recruit");
        if (!added) return false;
        // retirer l'invitation
        f.revokeInvite(playerUuid);
        // ajouter power
        ServerPlayer sp = SERVER.getPlayerList().getPlayer(playerUuid);
        if (sp != null) {
            PlayerPower pp = PowerManager.get(sp);
            f.setPower(f.getPower() + pp.getPower());
            if (EFConfig.FACTION_MAX_POWER_FROM_PLAYERS.get()) {
                f.setMaxPower(f.getMaxPower() + pp.getMaxPower());
            }
        }
        FactionSavedData.get(SERVER).setDirty();
        return true;
    }
}
