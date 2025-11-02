package fr.eriniumgroup.erinium_faction.features.topluck;

import fr.eriniumgroup.erinium_faction.common.config.TopLuckConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sauvegarde et chargement de la configuration TopLuck côté serveur (JSON sur disque) + hook SavedData.
 */
public class TopLuckSavedData extends SavedData {
    public static final String DATA_NAME = "erinium_faction-topluck";

    private TopLuckConfig config = TopLuckConfig.defaults();

    public TopLuckConfig getConfig() { return config; }

    public void setConfig(TopLuckConfig cfg) { this.config = cfg; setDirty(); }

    public static TopLuckSavedData get(MinecraftServer server) {
        var storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(TopLuckSavedData::new, TopLuckSavedData::load, null), DATA_NAME);
    }

    public TopLuckSavedData() {}

    public static TopLuckSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TopLuckSavedData data = new TopLuckSavedData();
        try {
            String json = tag.getString("json");
            if (json != null && !json.isEmpty()) {
                data.config = TopLuckConfig.fromJson(json);
            }
        } catch (Exception ignored) {}
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putString("json", config.toJson());
        return tag;
    }

    // Utilitaires pour export/import fichiers (optionnel)
    public void exportTo(Path dir) {
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("topluck.json"), config.toJson());
        } catch (Exception ignored) {}
    }

    public void importFrom(Path path) {
        try {
            String json = Files.readString(path);
            this.config = TopLuckConfig.fromJson(json);
            setDirty();
        } catch (Exception ignored) {}
    }
}
