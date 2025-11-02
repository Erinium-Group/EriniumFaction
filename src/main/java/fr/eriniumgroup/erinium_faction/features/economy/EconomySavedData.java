package fr.eriniumgroup.erinium_faction.features.economy;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registre persistant des soldes joueurs (classements hors-ligne).
 * <p>
 * Stocké dans: world/data/erinium_faction_economy.dat
 *
 * @author Blaackknight <dragclover@gmail.com>
 */
public class EconomySavedData extends SavedData {
    /**
     * Nom du fichier de données dans le data storage.
     */
    public static final String DATA_NAME = "erinium_faction_economy";

    /**
     * Entrée de registre pour un joueur.
     */
    public static class PlayerEntry {
        /**
         * UUID du joueur.
         */
        public UUID uuid;
        /**
         * Dernier nom connu (fallback: UUID en string).
         */
        public String name;
        /**
         * Solde actuel connu (>= 0).
         */
        public double balance;
    }

    private final Map<UUID, PlayerEntry> players = new HashMap<>();

    /**
     * Retourne les entrées connues (vue sur la map interne).
     *
     * @return collection des entrées
     */
    public Collection<PlayerEntry> entries() {
        return players.values();
    }

    /**
     * Met à jour (ou crée) l’entrée d’un joueur.
     *
     * @param uuid    UUID joueur
     * @param name    nom (peut être null -> conserve l’existant ou UUID)
     * @param balance solde (clampé à >= 0)
     */
    public void update(UUID uuid, String name, double balance) {
        PlayerEntry e = players.get(uuid);
        if (e == null) {
            e = new PlayerEntry();
            e.uuid = uuid;
            players.put(uuid, e);
        }
        e.name = name == null ? (e.name == null ? uuid.toString() : e.name) : name;
        e.balance = Math.max(0.0, balance);
        setDirty();
    }

    /**
     * Récupère (ou crée) l’instance du registre pour ce serveur.
     */
    public static EconomySavedData get(MinecraftServer server) {
        var storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(EconomySavedData::new, EconomySavedData::load, null), DATA_NAME);
    }

    /**
     * Constructeur par défaut.
     */
    public EconomySavedData() {
    }

    /**
     * Chargement depuis NBT.
     */
    public static EconomySavedData load(CompoundTag nbt, HolderLookup.Provider provider) {
        EconomySavedData data = new EconomySavedData();
        ListTag list = nbt.getList("players", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            PlayerEntry e = new PlayerEntry();
            try {
                e.uuid = t.getUUID("uuid");
            } catch (Exception ignored) {
                continue;
            }
            e.name = t.contains("name") ? t.getString("name") : e.uuid.toString();
            e.balance = t.getDouble("bal");
            data.players.put(e.uuid, e);
        }
        return data;
    }

    /**
     * Sauvegarde vers NBT.
     */
    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (PlayerEntry e : players.values()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("uuid", e.uuid);
            if (e.name != null) t.putString("name", e.name);
            t.putDouble("bal", e.balance);
            list.add(t);
        }
        nbt.put("players", list);
        return nbt;
    }
}
