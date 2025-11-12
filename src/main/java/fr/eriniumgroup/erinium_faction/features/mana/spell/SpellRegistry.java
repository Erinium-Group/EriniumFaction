package fr.eriniumgroup.erinium_faction.features.mana.spell;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@EventBusSubscriber
public class SpellRegistry implements PreparableReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, Spell> SPELLS = new HashMap<>();

    public static Spell get(ResourceLocation id) { return SPELLS.get(id); }
    public static Collection<Spell> all() { return java.util.Collections.unmodifiableCollection(SPELLS.values()); }

    @Override
    public java.util.concurrent.CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, java.util.concurrent.Executor backgroundExecutor, java.util.concurrent.Executor gameExecutor) {
        return java.util.concurrent.CompletableFuture.runAsync(() -> {
            Map<ResourceLocation, Spell> tmp = new HashMap<>();
            String path = "spells"; // data/erinium_faction/spells/*.json
            for (var res : resourceManager.listResources(path, p -> p.getPath().endsWith(".json")).entrySet()) {
                ResourceLocation rl = res.getKey();
                if (!rl.getNamespace().equals("erinium_faction")) continue;
                try (var in = resourceManager.open(rl); var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    JsonObject obj = GSON.fromJson(reader, JsonObject.class);
                    String type = obj.get("type").getAsString();
                    String idStr = obj.has("id") ? obj.get("id").getAsString() : rl.getPath();
                    int tier = obj.get("tier").getAsString().equalsIgnoreCase("ultimate") ? 6 : obj.get("tier").getAsInt();
                    double manaCost = obj.get("manaCost").getAsDouble();
                    int cooldown = obj.get("cooldownTicks").getAsInt();
                    String display = obj.has("display") ? obj.get("display").getAsString() : ("spell." + rl.getNamespace() + "." + rl.getPath());
                    String desc = obj.has("desc") ? obj.get("desc").getAsString() : (display + ".desc");
                    if (tier < 1 || (tier > 5 && tier != 6)) {
                        EFC.log.warn("Invalid tier for spell {}: {}", rl, tier);
                        continue;
                    }
                    Spell s = new Spell(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), idStr), type, tier, manaCost, cooldown, display, desc);
                    tmp.put(s.id, s);
                } catch (Exception ex) {
                    EFC.log.error("Failed to load spell {}: {}", rl, ex.toString());
                }
            }
            SPELLS.clear();
            SPELLS.putAll(tmp);
            EFC.log.info("Loaded {} spells", SPELLS.size());
        }, backgroundExecutor).thenCompose(barrier::wait);
    }

    @SubscribeEvent
    public static void onAddReload(AddReloadListenerEvent e) {
        e.addListener(new SpellRegistry());
    }
}