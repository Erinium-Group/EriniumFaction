package fr.eriniumgroup.erinium_faction.features.topluck;

import fr.eriniumgroup.erinium_faction.common.config.TopLuckConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;

/**
 * Calcule les totaux par catégorie à partir des compteurs par blockId et d'une config.
 */
public class TopLuckCalculator {

    public static class CategoryStat {
        public final TopLuckConfig.Category category;
        public long count;
        public double ratio; // count / total
        public double weighted; // ratio * weight

        public CategoryStat(TopLuckConfig.Category category) {
            this.category = category;
        }
    }

    public static Map<String, CategoryStat> compute(PlayerTopLuckData data, TopLuckConfig config) {
        Map<String, CategoryStat> out = new LinkedHashMap<>();
        for (var c : config.getCategories()) out.put(c.id, new CategoryStat(c));
        long total = Math.max(1L, data.getTotal());

        // Préparer les prédicats include
        Map<String, Predicate<String>> matchers = new HashMap<>();
        for (var c : config.getCategories()) {
            Predicate<String> p = blockId -> false;
            for (String inc : c.include) {
                Predicate<String> unit;
                if (inc.startsWith("#")) {
                    String tagId = inc.substring(1);
                    unit = blockId -> isInBlockTag(blockId, tagId);
                } else {
                    unit = blockId -> blockId.equals(inc);
                }
                p = p.or(unit);
            }
            matchers.put(c.id, p);
        }

        // Répartir
        for (var entry : data.getAll().entrySet()) {
            String blockId = entry.getKey();
            long count = entry.getValue();
            for (var c : config.getCategories()) {
                if (matchers.getOrDefault(c.id, s -> false).test(blockId)) {
                    CategoryStat cs = out.get(c.id);
                    cs.count += count;
                }
            }
        }
        for (var cs : out.values()) {
            cs.ratio = cs.count / (double) total;
            cs.weighted = cs.ratio * Math.max(0.0, cs.category.weight);
        }
        return out;
    }

    private static boolean isInBlockTag(String blockId, String tag) {
        try {
            ResourceLocation key = ResourceLocation.parse(blockId);
            Block b = BuiltInRegistries.BLOCK.get(key);
            if (b == null) return false;
            ResourceLocation tagRl = ResourceLocation.parse(tag);
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagRl);
            BlockState st = b.defaultBlockState();
            return st.is(tagKey);
        } catch (Exception e) {
            return false;
        }
    }
}
