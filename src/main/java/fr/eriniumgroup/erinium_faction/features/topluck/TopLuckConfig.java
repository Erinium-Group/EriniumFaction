package fr.eriniumgroup.erinium_faction.features.topluck;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;

/**
 * Configuration des catégories TopLuck.
 * Chaque catégorie possède:
 * - id (string)
 * - label (string)
 * - weight (double) => sert à calculer un ratio pondéré
 * - include (liste de tags ou d'ids de blocs, ex: "#forge:ores", "minecraft:diamond_ore")
 * - icon (ResourceLocation item/block pour l'affichage client)
 */
public class TopLuckConfig {
    public static class Category {
        public String id;
        public String label;
        public double weight = 1.0;
        public List<String> include = new ArrayList<>();
        public String icon = "minecraft:stone";
    }

    private final List<Category> categories = new ArrayList<>();

    public List<Category> getCategories() { return categories; }

    public Optional<Category> find(String id) {
        return categories.stream().filter(c -> Objects.equals(c.id, id)).findFirst();
    }

    public static TopLuckConfig defaults() {
        TopLuckConfig cfg = new TopLuckConfig();
        Category stone = new Category();
        stone.id = "stone";
        stone.label = "Stone";
        stone.weight = 1.0;
        stone.include = List.of("minecraft:stone", "minecraft:deepslate");
        stone.icon = "minecraft:stone";
        cfg.categories.add(stone);

        Category ores = new Category();
        ores.id = "ores";
        ores.label = "Ores";
        ores.weight = 3.0;
        ores.include = List.of("#forge:ores");
        ores.icon = "minecraft:diamond_ore";
        cfg.categories.add(ores);

        Category modded = new Category();
        modded.id = "modded";
        modded.label = "Modded Ores";
        modded.weight = 5.0;
        modded.include = List.of("#c:ores" /* tag courant dans mods modernes */);
        modded.icon = "minecraft:netherite_scrap";
        cfg.categories.add(modded);
        return cfg;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String toJson() {
        return GSON.toJson(this);
    }

    public static TopLuckConfig fromJson(String json) {
        try {
            return GSON.fromJson(json, TopLuckConfig.class);
        } catch (Exception e) {
            return defaults();
        }
    }
}

