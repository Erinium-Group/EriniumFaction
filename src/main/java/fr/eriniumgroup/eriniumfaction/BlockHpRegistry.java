/**
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside fr.eriniumgroup.eriniumfaction as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
*/
package fr.eriniumgroup.eriniumfaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = "erinium_faction")
public final class BlockHpRegistry extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = new GsonBuilder().create();
	private static final Map<ResourceLocation, Integer> MAP = new HashMap<>();
	private static final String PATH = "block_hp";

	public BlockHpRegistry() {
		super(GSON, PATH);
	}

	public static int baseHp(ResourceLocation key) {
		return MAP.getOrDefault(key, 4);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files, net.minecraft.server.packs.resources.ResourceManager resMgr, ProfilerFiller profiler) {
		MAP.clear();
		files.forEach((rl, json) -> {
			json.getAsJsonObject().entrySet().forEach(e -> {
				MAP.put(ResourceLocation.parse(e.getKey()), Math.max(1, e.getValue().getAsInt()));
			});
		});
	}

	@SubscribeEvent
	public static void onAddReload(AddReloadListenerEvent e) {
		e.addListener(new BlockHpRegistry());
	}
}