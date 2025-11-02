package fr.eriniumgroup.erinium_faction.core;

import java.util.Map;

public class EFC {
    public static final String MODID = "erinium_faction";

    private static String loadBuildInfo(String field, String def) {
        try {
            Class<?> c = Class.forName("fr.eriniumgroup.erinium_faction.BuildInfo");
            java.lang.reflect.Field f = c.getField(field);
            Object v = f.get(null);
            return v != null ? v.toString() : def;
        } catch (Throwable t) {
            return def;
        }
    }

    public static final String MOD_VERSION = loadBuildInfo("VERSION", "unknown");
    public static final String AUTHORS = loadBuildInfo("AUTHORS", "unknown");
    public static final String MOD_NAME = loadBuildInfo("MOD_NAME", "Erinium Faction");
    public static final String MOD_ID = loadBuildInfo("MOD_ID", "erinium_faction"); // doit matcher MODID

    public static final String MINECRAFT_VERSION = loadBuildInfo("MINECRAFT", "unknown");
    public static final String NEOFORGE_VERSION = loadBuildInfo("NEOFORGE", "unknown");
    public static final String MAPPINGS = loadBuildInfo("MAPPINGS", "unknown");
    public static final String BUILD_TIME = loadBuildInfo("BUILD_TIME", "unknown");
    public static final String GIT_BRANCH = loadBuildInfo("GIT_BRANCH", "unknown");
    public static final String GIT_COMMIT = loadBuildInfo("GIT_COMMIT", "unknown");
    public static final String GRADLE_VERSION = loadBuildInfo("GRADLE", "unknown");

    public static class log {
        private static final java.util.regex.Pattern MC = java.util.regex.Pattern.compile("§([0-9a-frlmnok])", java.util.regex.Pattern.CASE_INSENSITIVE);
        private static final String RESET = "\u001B[0m";
        private static final Map<Character, String> MAP = Map.ofEntries(Map.entry('0', "\u001B[30m"), // noir
                Map.entry('1', "\u001B[34m"), // bleu foncé
                Map.entry('2', "\u001B[32m"), // vert foncé
                Map.entry('3', "\u001B[36m"), // cyan foncé
                Map.entry('4', "\u001B[31m"), // rouge foncé
                Map.entry('5', "\u001B[35m"), // magenta foncé
                Map.entry('6', "\u001B[38;5;208m"), // orange
                Map.entry('7', "\u001B[37m"), // gris clair
                Map.entry('8', "\u001B[90m"), // gris foncé
                Map.entry('9', "\u001B[94m"), // bleu clair
                Map.entry('a', "\u001B[92m"), // vert clair
                Map.entry('b', "\u001B[96m"), // cyan clair
                Map.entry('c', "\u001B[91m"), // rouge clair
                Map.entry('d', "\u001B[95m"), // magenta clair
                Map.entry('e', "\u001B[93m"), // jaune clair
                Map.entry('f', "\u001B[97m"), // blanc
                Map.entry('k', ""),             // obfuscated (non supporté)
                Map.entry('l', "\u001B[1m"),   // gras
                Map.entry('m', "\u001B[9m"),   // barré
                Map.entry('n', "\u001B[4m"),   // souligné
                Map.entry('o', "\u001B[3m"),   // italique
                Map.entry('r', RESET)             // reset
        );

        private log() {
        }

        static String colorize(String s) {
            var m = MC.matcher(s);
            var sb = new StringBuilder();
            int i = 0;
            while (m.find()) {
                sb.append(s, i, m.start()).append(MAP.getOrDefault(Character.toLowerCase(m.group(1).charAt(0)), ""));
                i = m.end();
            }
            return sb.append(s, i, s.length()).append(RESET).toString();
        }

        private static org.slf4j.Logger L(String cat) {
            String base = "Erinium"; // aligne avec LoggerConfig
            if (cat == null || cat.isEmpty()) {
                return org.slf4j.LoggerFactory.getLogger(base);
            }
            // Accept both "Alerts" and "Alerts/Sub" by normalizing '/' -> '.' for logger hierarchy
            String normalized = cat.replace('/', '.');
            return org.slf4j.LoggerFactory.getLogger(base + "." + normalized);
        }

        // Injecte la catégorie (colorisée) dans le MDC pour l'affichage console
        private static void putCatMdc(String cat) {
            if (cat == null || cat.isBlank()) {
                org.slf4j.MDC.remove("eriniumCat");
            } else {
                org.slf4j.MDC.put("eriniumCat", colorize(cat)); // garde les '/' pour l'affichage
            }
        }

        private static void withCat(String cat, java.util.function.Consumer<org.slf4j.Logger> fn) {
            putCatMdc(cat);
            try {
                fn.accept(L(cat));
            } finally {
                org.slf4j.MDC.remove("eriniumCat");
            }
        }

        public static void info(String cat, String msg, Object... args) {
            withCat(cat, l -> l.info(colorize(msg), args));
        }

        public static void info(String msg, Object... args) {
            info(null, msg, args);
        }

        public static void debug(String cat, String msg, Object... args) {
            withCat(cat, l -> l.debug(colorize(msg), args));
        }

        public static void debug(String msg, Object... args) {
            debug(null, msg, args);
        }

        public static void warn(String cat, String msg, Object... args) {
            withCat(cat, l -> l.warn(colorize(msg), args));
        }

        public static void warn(String msg, Object... args) {
            warn(null, msg, args);
        }

        public static void error(String cat, String msg, Object... args) {
            withCat(cat, l -> l.error(colorize(msg), args));
        }

        public static void error(String msg, Object... args) {
            error(null, msg, args);
        }

        public static void trace(String cat, String msg, Object... args) {
            withCat(cat, l -> l.trace(colorize(msg), args));
        }

        public static void trace(String msg, Object... args) {
            trace(null, msg, args);
        }
    }
}
