package fr.eriniumgroup.erinium_faction.features.audit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class AuditJsonLog {
    private static final Gson G = new GsonBuilder().disableHtmlEscaping().create();
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private static Path auditDir() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            // ./<world>/barix/audit
            return server.getWorldPath(LevelResource.ROOT).resolve(EFC.MOD_ID).resolve("audit");
        }
        // Fallback si serveur indisponible
        return FMLPaths.GAMEDIR.get().resolve(EFC.MOD_ID).resolve("audit");
    }

    public static Path currentFile() {
        return auditDir().resolve("audit-" + DAY.format(Instant.now()) + ".jsonl");
    }

    public static void write(String event, Map<String, Object> payload) {
        if (!EFConfig.LOG_JSONL.get()) return;
        try {
            Files.createDirectories(auditDir());
            var obj = new java.util.LinkedHashMap<String, Object>();
            obj.put("ts", Instant.now().toString());
            obj.put("event", event);
            obj.putAll(payload);
            Files.writeString(currentFile(), G.toJson(obj) + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            maybeRotate();
        } catch (Exception ignored) {
        }
    }

    private static void maybeRotate() {
        try {
            long max = EFConfig.LOG_MAX_BYTES.get();
            Path f = currentFile();
            if (Files.exists(f) && Files.size(f) >= max) {
                AuditRotator.compressNow(f);
            }
        } catch (Exception ignored) {
        }
    }
}