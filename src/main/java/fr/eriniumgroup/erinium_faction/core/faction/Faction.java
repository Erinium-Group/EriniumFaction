package fr.eriniumgroup.erinium_faction.core.faction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

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
        // ranks par défaut
        RankDef leader = new RankDef("leader", "Leader", 100);
        leader.perms.add("faction.manage.*");
        RankDef officer = new RankDef("officer", "Officier", 80);
        officer.perms.add("faction.invite");
        officer.perms.add("faction.kick");
        RankDef member = new RankDef("member", "Membre", 10);
        RankDef recruit = new RankDef("recruit", "Recrue", 1);
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

    // NBT serialization ------------------------------------------------------
    public CompoundTag save() {
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

        return tag;
    }

    public static Faction load(CompoundTag tag) {
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

        return f;
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
}
