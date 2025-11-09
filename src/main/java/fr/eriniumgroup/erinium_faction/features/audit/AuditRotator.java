package fr.eriniumgroup.erinium_faction.features.audit;

import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;

public final class AuditRotator {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss").withZone(ZoneOffset.UTC);

    // Nouveau: dossier barix dans le monde courant -> ./<world>/barix/
    private static Path worldBarixDir() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            // Racine du monde courant, puis "barix"
            return server.getWorldPath(LevelResource.ROOT).resolve(EFC.MOD_ID);
        }
        // Fallback si serveur indisponible (ex: très tôt au boot)
        return FMLPaths.GAMEDIR.get().resolve(EFC.MOD_ID);
    }

    private static Path archiveDir() {
        // ./<world>/barix/archive
        return worldBarixDir().resolve("archive");
    }

    public static void compressNow(Path src) {
        try {
            if (!Files.exists(src)) return;
            Files.createDirectories(archiveDir());
            String base = src.getFileName().toString().replace(".jsonl", "");
            Path out = archiveDir().resolve(base + "-" + TS.format(Instant.now()) + ".jsonl.gz");
            try (var in = Files.newInputStream(src); var gz = new GZIPOutputStream(Files.newOutputStream(out))) {
                in.transferTo(gz);
            }
            Files.deleteIfExists(src);
        } catch (Exception ignored) {
        }
    }

    public static void compressTodayIfConfigured() {
        if (!EFConfig.LOG_COMPRESS_ON_STOP.get()) return;
        compressNow(AuditJsonLog.currentFile());
        EFC.log.info("§5AuditRotator", "§aAudit §elog §7compressed on §cserver stop§7: " + AuditJsonLog.currentFile().getFileName());
    }
}