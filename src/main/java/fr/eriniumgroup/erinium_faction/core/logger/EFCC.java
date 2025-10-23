package fr.eriniumgroup.erinium_faction.core.logger;

public class EFCC {
    private static final String PATTERN = "%style{[}{bright_black}%style{%d{HH:mm:ss}}{white}%style{]}{bright_black} " + "%style{[}{bright_black}%style{%t}{cyan}%style{/}{red}" + "%highlight{%p}{FATAL=bg_red bright_white, ERROR=red, WARN=yellow, INFO=green, DEBUG=blue, TRACE=magenta}" + "%style{]}{bright_black} "
            // Affiche uniquement Erinium + catégorie issue du MDC (si présente), la catégorie est déjà colorisée
            + "%style{[}{bright_black}%style{Erinium}{cyan}%notEmpty{%style{/}{red}%mdc{eriniumCat}}%style{]}{bright_black}: " + "%msg%n%throwable";

    public static void install() {
        var ctx = (org.apache.logging.log4j.core.LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        var cfg = ctx.getConfiguration();
        var layout = org.apache.logging.log4j.core.layout.PatternLayout.newBuilder().withPattern(PATTERN).withDisableAnsi(false).withNoConsoleNoAnsi(false).withConfiguration(cfg).build();
        var app = org.apache.logging.log4j.core.appender.ConsoleAppender.newBuilder().setName("EriniumConsole").setTarget(org.apache.logging.log4j.core.appender.ConsoleAppender.Target.SYSTEM_OUT).setLayout(layout).build();
        app.start();
        cfg.addAppender(app);

        cfg.removeLogger("Erinium");
        var barixLoggerCfg = new org.apache.logging.log4j.core.config.LoggerConfig("Erinium", org.apache.logging.log4j.Level.TRACE, false // additive false -> pas de propagation au root, les enfants Barix.* remontent ici
        );
        barixLoggerCfg.addAppender(app, org.apache.logging.log4j.Level.ALL, null);
        cfg.addLogger("Erinium", barixLoggerCfg);


        ctx.updateLoggers();
    }
}
