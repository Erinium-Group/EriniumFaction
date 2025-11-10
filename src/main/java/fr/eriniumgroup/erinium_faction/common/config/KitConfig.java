package fr.eriniumgroup.erinium_faction.common.config;

import com.electronwill.nightconfig.core.Config;
import fr.eriniumgroup.erinium_faction.features.kits.Kit;
import fr.eriniumgroup.erinium_faction.features.kits.KitItem;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

/**
 * Configuration TOML pour les kits
 * Format ultra simple et lisible
 */
public class KitConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<List<? extends Config>> KITS;

    static {
        BUILDER.push("Kits System");

        BUILDER.comment(
                "═══════════════════════════════════════════════════════════════════════",
                "                        SYSTÈME DE KITS",
                "═══════════════════════════════════════════════════════════════════════",
                "",
                "Format d'un item:",
                "  \"itemId,count,slot:SLOT,enchants:{ench:lvl},name:Name,lore:[Line1,Line2]\"",
                "",
                "Paramètres (tous optionnels sauf itemId):",
                "  - itemId       : ID de l'item (ex: minecraft:diamond_sword)",
                "  - count        : Nombre d'items (défaut: 1)",
                "  - slot         : Emplacement armor (HEAD/CHEST/LEGS/FEET/OFFHAND)",
                "  - enchants     : Enchantements {enchant_id:level,enchant_id:level}",
                "  - name         : Nom custom de l'item",
                "  - lore         : Lore de l'item [ligne1,ligne2,ligne3]",
                "",
                "Exemples:",
                "  \"minecraft:diamond_sword,1,enchants:{minecraft:sharpness:5}\"",
                "  \"minecraft:diamond_helmet,1,slot:HEAD,enchants:{minecraft:protection:4}\"",
                "  \"minecraft:golden_apple,16\"",
                "  \"minecraft:bow,1,enchants:{minecraft:power:5,minecraft:infinity:1},name:§6Arc Légendaire\"",
                "",
                "Paramètres du kit:",
                "  - requiredRank   : Rank requis (vide = tous, sinon nom du rank)",
                "  - cooldownMinutes: Cooldown en MINUTES (défaut: 60, 0 = pas de cooldown)",
                "",
                "═══════════════════════════════════════════════════════════════════════"
        );

        KITS = BUILDER.defineList("kits",
                // Kits par défaut (exemples)
                Arrays.asList(
                        createDefaultKit("starter", "Kit Débutant", "Kit de base pour tous les joueurs", "",
                                Arrays.asList(
                                        "minecraft:iron_sword,1,enchants:{minecraft:sharpness:1}",
                                        "minecraft:iron_helmet,1,slot:HEAD",
                                        "minecraft:iron_chestplate,1,slot:CHEST",
                                        "minecraft:iron_leggings,1,slot:LEGS",
                                        "minecraft:iron_boots,1,slot:FEET",
                                        "minecraft:bread,16",
                                        "minecraft:torch,32"
                                )),
                        createDefaultKit("vip", "Kit VIP", "Kit réservé aux VIP", "vip",
                                Arrays.asList(
                                        "minecraft:diamond_sword,1,enchants:{minecraft:sharpness:3,minecraft:unbreaking:2}",
                                        "minecraft:diamond_helmet,1,slot:HEAD,enchants:{minecraft:protection:3}",
                                        "minecraft:diamond_chestplate,1,slot:CHEST,enchants:{minecraft:protection:3}",
                                        "minecraft:diamond_leggings,1,slot:LEGS,enchants:{minecraft:protection:3}",
                                        "minecraft:diamond_boots,1,slot:FEET,enchants:{minecraft:protection:3}",
                                        "minecraft:golden_apple,8",
                                        "minecraft:ender_pearl,16"
                                )),
                        createDefaultKit("pvp", "Kit PvP", "Kit de combat pour les guerriers", "warrior",
                                Arrays.asList(
                                        "minecraft:netherite_sword,1,enchants:{minecraft:sharpness:5,minecraft:fire_aspect:2,minecraft:unbreaking:3},name:§c§lÉpée du Guerrier",
                                        "minecraft:netherite_helmet,1,slot:HEAD,enchants:{minecraft:protection:4,minecraft:unbreaking:3}",
                                        "minecraft:netherite_chestplate,1,slot:CHEST,enchants:{minecraft:protection:4,minecraft:unbreaking:3}",
                                        "minecraft:netherite_leggings,1,slot:LEGS,enchants:{minecraft:protection:4,minecraft:unbreaking:3}",
                                        "minecraft:netherite_boots,1,slot:FEET,enchants:{minecraft:protection:4,minecraft:unbreaking:3}",
                                        "minecraft:bow,1,enchants:{minecraft:power:5,minecraft:infinity:1,minecraft:flame:1}",
                                        "minecraft:arrow,1",
                                        "minecraft:golden_apple,16",
                                        "minecraft:totem_of_undying,1,slot:OFFHAND"
                                ))
                ),
                obj -> obj instanceof Config
        );

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    /**
     * Helper pour créer un kit par défaut dans la config
     */
    private static Config createDefaultKit(String id, String displayName, String description, String rank, List<String> items) {
        return createDefaultKit(id, displayName, description, rank, items, 60); // 60 minutes par défaut
    }

    private static Config createDefaultKit(String id, String displayName, String description, String rank, List<String> items, int cooldownMinutes) {
        Config config = Config.inMemory();
        config.set("id", id);
        config.set("displayName", displayName);
        config.set("description", description);
        config.set("requiredRank", rank);
        config.set("cooldownMinutes", cooldownMinutes);
        config.set("items", items);
        return config;
    }

    /**
     * Parse tous les kits depuis la config
     */
    public static List<Kit> parseKits() {
        List<Kit> kits = new ArrayList<>();

        List<? extends Config> configKits = KITS.get();

        for (Config kitConfig : configKits) {
            try {
                String id = kitConfig.get("id");
                String displayName = kitConfig.getOrElse("displayName", id);
                String description = kitConfig.getOrElse("description", "");
                String requiredRank = kitConfig.getOrElse("requiredRank", "");
                int cooldownMinutes = kitConfig.getOrElse("cooldownMinutes", 60); // 60 minutes par défaut

                List<String> itemStrings = kitConfig.getOrElse("items", Collections.emptyList());
                List<KitItem> items = new ArrayList<>();

                for (String itemStr : itemStrings) {
                    try {
                        items.add(KitItem.parse(itemStr));
                    } catch (Exception e) {
                        System.err.println("§c[KIT] Erreur parsing item du kit '" + id + "': " + itemStr);
                        e.printStackTrace();
                    }
                }

                kits.add(new Kit(id, displayName, description, requiredRank, items, cooldownMinutes));
                System.out.println("§a[KIT] Kit chargé: " + id + " (" + items.size() + " items, cooldown: " + cooldownMinutes + "min)");

            } catch (Exception e) {
                System.err.println("§c[KIT] Erreur chargement kit");
                e.printStackTrace();
            }
        }

        return kits;
    }
}
