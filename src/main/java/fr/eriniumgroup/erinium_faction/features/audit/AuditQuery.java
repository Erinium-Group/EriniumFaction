package fr.eriniumgroup.erinium_faction.features.audit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

public final class AuditQuery {
    private static final Gson G = new Gson();

    public static List<JsonObject> find(String uuidOrNull, String blockIdOrNull, String eventOrNull, Instant fromOrNull, Instant toOrNull, int limitNewest, int offset) {
        try {
            // Utiliser le même emplacement que l’écriture: ./<world>/{modid}/audit et ./<world>/{modid}/archive
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return List.of();
            Path base = server.getWorldPath(LevelResource.ROOT).resolve(EFC.MOD_ID);
            Path auditDir = base.resolve("audit");
            Path archiveDir = base.resolve("archive");
            List<Path> files = new ArrayList<>();
            if (Files.isDirectory(auditDir)) try (var s = Files.list(auditDir)) { s.forEach(files::add); }
            if (Files.isDirectory(archiveDir)) try (var s = Files.list(archiveDir)) { s.forEach(files::add); }
            files.sort(Comparator.comparing(Path::getFileName).reversed());

            Predicate<JsonObject> pred = o -> {
                if (uuidOrNull != null) {
                    var pl = o.getAsJsonObject("player");
                    if (pl == null || !uuidOrNull.equalsIgnoreCase(pl.get("uuid").getAsString())) return false;
                }
                if (blockIdOrNull != null) {
                    var b = o.get("block");
                    if (b == null || !blockIdOrNull.equalsIgnoreCase(b.getAsString())) return false;
                }
                if (eventOrNull != null) {
                    var ev = o.get("event");
                    if (ev == null || !eventOrNull.equalsIgnoreCase(ev.getAsString())) return false;
                }
                if (fromOrNull != null || toOrNull != null) {
                    Instant ts = Instant.parse(o.get("ts").getAsString());
                    if (fromOrNull != null && ts.isBefore(fromOrNull)) return false;
                    if (toOrNull != null && ts.isAfter(toOrNull)) return false;
                }
                return true;
            };

            List<JsonObject> out = new ArrayList<>(Math.max(16, limitNewest));
            final int[] matched = {0};
            for (Path p : files) {
                readFile(p, line -> {
                    JsonObject o = G.fromJson(line, JsonObject.class);
                    if (pred.test(o)) {
                        if (matched[0]++ < offset) return true; // skip jusqu’à offset
                        out.add(o);
                        return out.size() < limitNewest; // continuer tant qu’on n’a pas atteint le quota
                    }
                    return true; // continuer
                });
                if (out.size() >= limitNewest) break;
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    // Lecture ligne à ligne; stopReader.accept(line) retourne false pour stopper
    private static void readFile(Path p, java.util.function.Function<String, Boolean> stopReader) throws IOException {
        boolean gz = p.getFileName().toString().endsWith(".gz");
        InputStream in = gz ? new GZIPInputStream(Files.newInputStream(p)) : Files.newInputStream(p);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!stopReader.apply(line)) break;
            }
        }
    }
}