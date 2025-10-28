package fr.eriniumgroup.erinium_faction.core.faction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/**
 * Modèle de faction: nom, tag, power, xp, membres, grades internes et permissions.
 */
public class Faction {
    private String id;             // identifiant unique (lowercase, pour l'index)
    private String name;           // nom affiché
    private String tag;            // court tag

    private double power;          // puissance actuelle
    private double maxPower;       // puissance max (calculée, mais persistée/préconfigurable)

    private int level;             // niveau de faction
    private int xp;                // xp courant pour le niveau

    private UUID owner;            // propriétaire / créateur

    // rankId -> RankDef
    private final Map<String, RankDef> ranks = new LinkedHashMap<>();

    // uuid -> Member
    private final Map<UUID, Member> members = new LinkedHashMap<>();

    public static class RankDef {
        public String id;
        public String display;
        public int priority; // plus haut = plus de pouvoir
        public Set<String> perms = new HashSet<>();

        public RankDef() {
        }

        public RankDef(String id, String display, int priority) {
            this.id = id;
            this.display = display;
            this.priority = priority;
        }
    }

    public static class Member {
        public UUID uuid;
        public String nameCached;
        public String rankId;
        public long joinedAt;

        public Member() {
        }

        public Member(UUID uuid, String nameCached, String rankId, long joinedAt) {
            this.uuid = uuid;
            this.nameCached = nameCached;
            this.rankId = rankId;
            this.joinedAt = joinedAt;
        }
    }

    public Faction() {
    }

    public Faction(String id, String name, String tag, UUID owner) {
        this.id = id.toLowerCase(Locale.ROOT);
        this.name = name;
        this.tag = tag;
        this.owner = owner;
        this.power = 0;
        this.maxPower = 100;
        this.level = 1;
        this.xp = 0;
        // ranks par défaut avec permissions complètes
        RankDef leader = new RankDef("leader", "Leader", 100);
        leader.perms.add("faction.manage.*");
        leader.perms.add("faction.invite");
        leader.perms.add("faction.kick");
        leader.perms.add("faction.claim");
        leader.perms.add("faction.unclaim");
        leader.perms.add("faction.build");
        leader.perms.add("faction.break");
        leader.perms.add("faction.use.doors");
        leader.perms.add("faction.use.buttons");
        leader.perms.add("faction.use.levers");
        leader.perms.add("faction.use.containers");
        leader.perms.add("faction.manage.permissions");
        leader.perms.add("faction.manage.alliances");

        RankDef officer = new RankDef("officer", "Officer", 80);
        officer.perms.add("faction.invite");
        officer.perms.add("faction.kick");
        officer.perms.add("faction.claim");
        officer.perms.add("faction.unclaim");
        officer.perms.add("faction.build");
        officer.perms.add("faction.break");
        officer.perms.add("faction.use.doors");
        officer.perms.add("faction.use.buttons");
        officer.perms.add("faction.use.levers");
        officer.perms.add("faction.use.containers");

        RankDef member = new RankDef("member", "Member", 10);
        member.perms.add("faction.invite");
        member.perms.add("faction.claim");
        member.perms.add("faction.build");
        member.perms.add("faction.break");
        member.perms.add("faction.use.doors");
        member.perms.add("faction.use.buttons");
        member.perms.add("faction.use.levers");
        member.perms.add("faction.use.containers");

        RankDef recruit = new RankDef("recruit", "Recruit", 1);
        recruit.perms.add("faction.build");
        recruit.perms.add("faction.break");
        recruit.perms.add("faction.use.doors");
        recruit.perms.add("faction.use.containers");

        ranks.put(leader.id, leader);
        ranks.put(officer.id, officer);
        ranks.put(member.id, member);
        ranks.put(recruit.id, recruit);
        // owner membre leader
        members.put(owner, new Member(owner, "", leader.id, System.currentTimeMillis()));
    }

    // Getters/setters --------------------------------------------------------
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public double getPower() {
        return power;
    }

    public double getMaxPower() {
        return maxPower;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public UUID getOwner() {
        return owner;
    }

    public Map<String, RankDef> getRanks() {
        return ranks;
    }

    public Map<UUID, Member> getMembers() {
        return members;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setPower(double power) {
        this.power = Math.max(0, Math.min(power, maxPower));
    }

    public void setMaxPower(double maxPower) {
        this.maxPower = Math.max(0, maxPower);
        if (this.power > this.maxPower) this.power = this.maxPower;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    // --- Utilitaires joueurs connectés ---
    /**
     * Renvoie la liste des membres de cette faction actuellement connectés au serveur.
     * - Si server est null, renvoie une liste vide.
     */
    public List<ServerPlayer> getOnlinePlayers(MinecraftServer server) {
        if (server == null) return Collections.emptyList();
        List<ServerPlayer> out = new ArrayList<>();
        for (UUID id : members.keySet()) {
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            if (sp != null) out.add(sp);
        }
        return out;
    }

    // Rank helpers -----------------------------------------------------------
    public boolean addRank(String id, String display, int priority) {
        String key = id.toLowerCase(Locale.ROOT);
        if (ranks.containsKey(key)) return false;
        ranks.put(key, new RankDef(key, display, priority));
        return true;
    }

    public boolean addRankPerm(String id, String perm) {
        RankDef r = ranks.get(id.toLowerCase(Locale.ROOT));
        if (r == null) return false;
        return r.perms.add(perm);
    }

    public boolean removeRankPerm(String id, String perm) {
        RankDef r = ranks.get(id.toLowerCase(Locale.ROOT));
        if (r == null) return false;
        return r.perms.remove(perm);
    }

    public String getMemberRank(UUID uuid) {
        Member m = members.get(uuid);
        return m == null ? null : m.rankId;
    }

    public boolean setMemberRank(UUID uuid, String rankId) {
        Member m = members.get(uuid);
        if (m == null) return false;
        if (!ranks.containsKey(rankId)) return false;
        m.rankId = rankId;
        return true;
    }

    public boolean addMember(UUID uuid, String name, String rankId) {
        if (members.containsKey(uuid)) return false;
        if (!ranks.containsKey(rankId)) rankId = "member";
        members.put(uuid, new Member(uuid, name, rankId, System.currentTimeMillis()));
        return true;
    }

    public boolean removeMember(UUID uuid) {
        if (!members.containsKey(uuid)) return false;
        if (uuid.equals(owner)) return false; // empêcher suppression automatique du propriétaire
        members.remove(uuid);
        return true;
    }

    public boolean hasPermission(UUID player, String node) {
        if (player == null || node == null || node.isBlank()) return false;
        if (Objects.equals(owner, player)) return true; // propriétaire a tous les droits
        Member m = members.get(player);
        if (m == null) return false;
        RankDef r = ranks.get(m.rankId);
        if (r == null) return false;
        if (r.perms.contains("*")) return true;
        if (r.perms.contains(node)) return true;
        // préfixes hiérarchiques ex: faction.manage.*
        String cur = node;
        while (true) {
            int i = cur.lastIndexOf('.');
            if (i < 0) break;
            cur = cur.substring(0, i) + ".*";
            if (r.perms.contains(cur)) return true;
            cur = cur.substring(0, cur.length() - 2);
        }
        return false;
    }

    // XP / Level -------------------------------------------------------------
    public int xpNeededForNextLevel() {
        int next = level + 1;
        return Math.max(100, next * next * 50); // courbe simple
    }

    public void addXp(int amount) {
        if (amount <= 0) return;
        this.xp += amount;
        while (this.xp >= xpNeededForNextLevel()) {
            this.xp -= xpNeededForNextLevel();
            this.level++;
        }
    }

    // Settings & meta --------------------------------------------------------
    private boolean adminFaction;
    private boolean warzone;
    private boolean safezone;
    private boolean openFaction; // nouveau: faction ouverte à tous (open/close)

    public enum Mode { PUBLIC, INVITE_ONLY }
    private Mode mode = Mode.INVITE_ONLY;

    private String description = "";

    // Bank
    private double bankBalance = 0.0;

    // Home location (dimensionless placeholder: x,y,z)
    private int homeX = 0, homeY = 0, homeZ = 0;
    private boolean hasHome = false;
    private ResourceLocation homeDim = null;

    // Warps (name -> pos x,y,z) simple; max warps calculé dynamiquement via config/level
    public static class Warp { public int x, y, z; public ResourceLocation dim; }
    private final Map<String, Warp> warps = new LinkedHashMap<>();

    // Coffre de faction - Stockage d'items
    private final net.minecraft.world.item.ItemStack[] chestItems = new net.minecraft.world.item.ItemStack[27]; // Max 3 lignes de 9 slots

    // Alliances & invitations (placeholders simples)
    private final Set<String> allies = new HashSet<>();
    private final Set<UUID> invitedPlayers = new HashSet<>();

    // Historique des transactions bancaires
    private TransactionHistory transactionHistory = new TransactionHistory();

    // Accessors --------------------------------------------------------------
    public boolean isAdminFaction() { return adminFaction; }
    public void setAdminFaction(boolean v) { this.adminFaction = v; }

    public boolean isWarzone() { return warzone; }
    public void setWarzone(boolean v) { this.warzone = v; }

    public boolean isSafezone() { return safezone; }
    public void setSafezone(boolean v) { this.safezone = v; }

    public boolean isOpenFaction() { return openFaction; }
    public void setOpenFaction(boolean v) { this.openFaction = v; }

    public Mode getMode() { return mode; }
    public void setMode(Mode m) { if (m != null) this.mode = m; }

    public String getDescription() { return description; }
    public void setDescription(String s) { this.description = s == null ? "" : s; }

    public double getBankBalance() { return bankBalance; }
    public void setBankBalance(double v) { this.bankBalance = Math.max(0.0, v); }
    public void deposit(double v) { setBankBalance(this.bankBalance + Math.max(0.0, v)); }
    public boolean withdraw(double v) { if (v <= 0 || v > bankBalance) return false; bankBalance -= v; return true; }

    public boolean hasHome() { return hasHome; }
    public void setHome(int x, int y, int z, ResourceLocation dim) { this.homeX=x; this.homeY=y; this.homeZ=z; this.homeDim = dim; this.hasHome=true; }
    public ResourceLocation getHomeDim() { return homeDim; }
    public int[] getHome() { return hasHome ? new int[]{homeX,homeY,homeZ} : null; }

    public Map<String, Warp> getWarps() { return warps; }
    public int getMaxWarps() {
        int base = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_BASE_WARPS.get();
        int per5 = fr.eriniumgroup.erinium_faction.common.config.EFConfig.FACTION_WARPS_PER_5_LEVELS.get();
        int bonus = per5 * Math.max(0, getLevel() / 5);
        return Math.max(0, base + bonus);
    }
    public boolean addWarp(String name, int x, int y, int z, ResourceLocation dim) {
        if (name == null || name.isBlank()) return false;
        if (warps.size() >= getMaxWarps()) return false;
        Warp w = new Warp(); w.x=x; w.y=y; w.z=z; w.dim = dim; warps.put(name.toLowerCase(Locale.ROOT), w); return true;
    }
    public boolean removeWarp(String name) { return warps.remove(name.toLowerCase(Locale.ROOT)) != null; }

    public Set<String> getAllies() { return allies; }
    public Set<UUID> getInvitedPlayers() { return invitedPlayers; }

    public TransactionHistory getTransactionHistory() { return transactionHistory; }

    // Coffre de faction
    public int getChestSize() {
        // Progression par lignes complètes de 9 slots
        // Niveau 1-8 : 1 ligne (9 slots)
        // Niveau 9-17 : 2 lignes (18 slots)
        // Niveau 18+ : 3 lignes (27 slots) - coffre normal
        if (level >= 18) {
            return 27; // 3 lignes - coffre normal
        } else if (level >= 9) {
            return 18; // 2 lignes
        } else {
            return 9; // 1 ligne
        }
    }

    public net.minecraft.world.item.ItemStack[] getChestItems() { return chestItems; }

    public net.minecraft.world.item.ItemStack getChestItem(int slot) {
        if (slot < 0 || slot >= chestItems.length) return net.minecraft.world.item.ItemStack.EMPTY;
        return chestItems[slot] == null ? net.minecraft.world.item.ItemStack.EMPTY : chestItems[slot];
    }

    public void setChestItem(int slot, net.minecraft.world.item.ItemStack stack) {
        if (slot >= 0 && slot < chestItems.length) {
            chestItems[slot] = stack;
        }
    }

    // NBT serialization ------------------------------------------------------
    public CompoundTag save() {
        return save(net.minecraft.core.RegistryAccess.EMPTY);
    }

    public CompoundTag save(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("name", name);
        tag.putString("tag", this.tag == null ? "" : this.tag);
        tag.putDouble("power", power);
        tag.putDouble("maxPower", maxPower);
        tag.putInt("level", level);
        tag.putInt("xp", xp);
        if (owner != null) tag.putUUID("owner", owner);

        // ranks
        ListTag ranksList = new ListTag();
        for (RankDef r : ranks.values()) {
            CompoundTag rt = new CompoundTag();
            rt.putString("id", r.id);
            rt.putString("display", r.display);
            rt.putInt("priority", r.priority);
            ListTag perms = new ListTag();
            for (String p : r.perms) perms.add(StringTag.valueOf(p));
            rt.put("perms", perms);
            ranksList.add(rt);
        }
        tag.put("ranks", ranksList);

        // members
        ListTag memList = new ListTag();
        for (Member m : members.values()) {
            CompoundTag mt = new CompoundTag();
            mt.putUUID("uuid", m.uuid);
            if (m.nameCached != null) mt.putString("name", m.nameCached);
            mt.putString("rank", m.rankId);
            mt.putLong("joined", m.joinedAt);
            memList.add(mt);
        }
        tag.put("members", memList);

        tag.putBoolean("admin", adminFaction);
        tag.putBoolean("warzone", warzone);
        tag.putBoolean("safezone", safezone);
        tag.putBoolean("open", openFaction); // nouveau
        tag.putString("mode", mode.name());
        tag.putString("desc", description);
        tag.putDouble("bank", bankBalance);
        if (hasHome) {
            tag.putInt("homeX", homeX); tag.putInt("homeY", homeY); tag.putInt("homeZ", homeZ); tag.putBoolean("hasHome", true);
            if (homeDim != null) tag.putString("homeDim", homeDim.toString());
        }
        // warps
        ListTag warpsList = new ListTag();
        for (var e : warps.entrySet()) {
            CompoundTag wt = new CompoundTag();
            wt.putString("name", e.getKey());
            wt.putInt("x", e.getValue().x); wt.putInt("y", e.getValue().y); wt.putInt("z", e.getValue().z);
            if (e.getValue().dim != null) wt.putString("dim", e.getValue().dim.toString());
            warpsList.add(wt);
        }
        tag.put("warps", warpsList);

        // Coffre de faction - Items avec TOUS les composants (enchantements, NBT, etc.)
        ListTag chestList = new ListTag();
        for (int i = 0; i < chestItems.length; i++) {
            if (chestItems[i] != null && !chestItems[i].isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                // Sauvegarder l'item COMPLET avec tous ses composants
                itemTag.put("Item", chestItems[i].save(registries));
                chestList.add(itemTag);
            }
        }
        tag.put("ChestItems", chestList);

        // allies
        ListTag alliesList = new ListTag();
        for (String a : allies) alliesList.add(StringTag.valueOf(a));
        tag.put("allies", alliesList);
        // invites
        ListTag invList = new ListTag();
        for (UUID u : invitedPlayers) invList.add(StringTag.valueOf(u.toString()));
        tag.put("invited", invList);

        // transaction history
        if (transactionHistory != null) {
            tag.put("transactionHistory", transactionHistory.save());
        }

        return tag;
    }

    public static Faction load(CompoundTag tag) {
        return load(tag, net.minecraft.core.RegistryAccess.EMPTY);
    }

    public static Faction load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        Faction f = new Faction();
        f.id = tag.getString("id");
        f.name = tag.getString("name");
        f.tag = tag.getString("tag");
        f.power = tag.getDouble("power");
        f.maxPower = tag.getDouble("maxPower");
        f.level = tag.getInt("level");
        f.xp = tag.getInt("xp");
        if (tag.hasUUID("owner")) f.owner = tag.getUUID("owner");

        f.ranks.clear();
        var ranksList = tag.getList("ranks", 10);
        for (int i = 0; i < ranksList.size(); i++) {
            CompoundTag rt = ranksList.getCompound(i);
            RankDef r = new RankDef();
            r.id = rt.getString("id");
            r.display = rt.getString("display");
            r.priority = rt.getInt("priority");
            r.perms.clear();
            var perms = rt.getList("perms", 8);
            for (int j = 0; j < perms.size(); j++) r.perms.add(perms.getString(j));
            f.ranks.put(r.id, r);
        }

        f.members.clear();
        var memList = tag.getList("members", 10);
        for (int i = 0; i < memList.size(); i++) {
            CompoundTag mt = memList.getCompound(i);
            Member m = new Member();
            m.uuid = mt.getUUID("uuid");
            m.nameCached = mt.contains("name") ? mt.getString("name") : null;
            m.rankId = mt.getString("rank");
            m.joinedAt = mt.getLong("joined");
            f.members.put(m.uuid, m);
        }

        // Flags
        f.adminFaction = tag.getBoolean("admin");
        f.warzone = tag.getBoolean("warzone");
        f.safezone = tag.getBoolean("safezone");
        f.openFaction = tag.contains("open") ? tag.getBoolean("open") : false; // compat par défaut
        // Mode & desc
        if (tag.contains("mode")) {
            try { f.mode = Mode.valueOf(tag.getString("mode")); } catch (IllegalArgumentException ignored) {}
        }
        f.description = tag.getString("desc");
        f.bankBalance = tag.getDouble("bank");
        f.hasHome = tag.getBoolean("hasHome");
        if (f.hasHome) {
            f.homeX = tag.getInt("homeX"); f.homeY = tag.getInt("homeY"); f.homeZ = tag.getInt("homeZ");
            if (tag.contains("homeDim")) f.homeDim = ResourceLocation.tryParse(tag.getString("homeDim"));
        }
        // warps
        f.warps.clear();
        ListTag warpsList = tag.getList("warps", 10);
        for (int i=0;i<warpsList.size();i++) {
            CompoundTag wt = warpsList.getCompound(i);
            Warp w = new Warp(); w.x = wt.getInt("x"); w.y = wt.getInt("y"); w.z = wt.getInt("z");
            if (wt.contains("dim")) w.dim = ResourceLocation.tryParse(wt.getString("dim"));
            String nm = wt.getString("name");
            if (nm != null && !nm.isBlank()) f.warps.put(nm.toLowerCase(Locale.ROOT), w);
        }
        // allies
        f.allies.clear();
        ListTag alliesList = tag.getList("allies", 8);
        for (int i=0;i<alliesList.size();i++) f.allies.add(alliesList.getString(i));
        // invited
        f.invitedPlayers.clear();
        ListTag invList = tag.getList("invited", 8);
        for (int i=0;i<invList.size();i++) { try { f.invitedPlayers.add(UUID.fromString(invList.getString(i))); } catch (Exception ignored) {} }

        // Coffre de faction - Items
        for (int i = 0; i < f.chestItems.length; i++) {
            f.chestItems[i] = net.minecraft.world.item.ItemStack.EMPTY;
        }
        if (tag.contains("ChestItems")) {
            ListTag chestList = tag.getList("ChestItems", 10);
            var registryAccess = net.minecraft.core.RegistryAccess.EMPTY;
            for (int i = 0; i < chestList.size(); i++) {
                CompoundTag itemTag = chestList.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < f.chestItems.length && itemTag.contains("Item")) {
                    CompoundTag stackTag = itemTag.getCompound("Item");
                    f.chestItems[slot] = net.minecraft.world.item.ItemStack.parseOptional(registryAccess, stackTag);
                }
            }
        }

        // transaction history
        if (tag.contains("transactionHistory")) {
            f.transactionHistory = TransactionHistory.load(tag.getCompound("transactionHistory"));
        }

        return f;
    }

    public void addPower(double value){
        power += value;
    }

    public Rank getRank(UUID player) {
        String r = getMemberRank(player);
        if (r == null) return null;
        return new Rank(r, this);
    }

    public int getXPRequiredForNextLevel(int currentLevel) {
        int next = Math.max(1, currentLevel + 1);
        return Math.max(100, next * next * 50);
    }

    // Fournit un gestionnaire des rangs pour cette faction.
    public RankManager ranks() {
        return new RankManager(this);
    }
}
