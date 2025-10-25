package fr.eriniumgroup.erinium_faction.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Config côté client uniquement.
 * Caca
 */
public final class EFClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> MAP_DEFAULT_KEY;

    // Contrôle d’ouverture de la carte: KEY, BUTTON, BOTH
    public static final ModConfigSpec.ConfigValue<String> MAP_OPEN_CONTROL;
    // Ancrage du bouton: TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    public static final ModConfigSpec.ConfigValue<String> MAP_BUTTON_ANCHOR;
    public static final ModConfigSpec.IntValue MAP_BUTTON_OFFSET_X;
    public static final ModConfigSpec.IntValue MAP_BUTTON_OFFSET_Y;
    public static final ModConfigSpec.IntValue MAP_BUTTON_SIZE;
    public static final ModConfigSpec.ConfigValue<String> MAP_BUTTON_THEME; // AUTO, LIGHT, DARK
    public static final ModConfigSpec.ConfigValue<String> MAP_BUTTON_TEXTURE_LIGHT; // RL
    public static final ModConfigSpec.ConfigValue<String> MAP_BUTTON_TEXTURE_DARK;  // RL
    public static final ModConfigSpec.BooleanValue MAP_BUTTON_TOOLTIP;
    public static final ModConfigSpec.BooleanValue MAP_BUTTON_HIDE_IN_DEBUG;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("keybinds");
        MAP_DEFAULT_KEY = b.comment("Touche par défaut pour ouvrir la carte des claims (ex: M, key.keyboard.m, F10)")
                .define("mapDefaultKey", "M");
        b.pop();

        b.push("mapOverlay");
        MAP_OPEN_CONTROL = b.comment("Méthode d’ouverture de la carte: KEY (touche), BUTTON (bouton HUD), BOTH (les deux)")
                .define("openControl", "KEY");
        MAP_BUTTON_ANCHOR = b.comment("Ancrage du bouton HUD: TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT")
                .define("buttonAnchor", "TOP_RIGHT");
        MAP_BUTTON_OFFSET_X = b.comment("Décalage X à partir de l’ancre, en pixels")
                .defineInRange("buttonOffsetX", 6, 0, 10000);
        MAP_BUTTON_OFFSET_Y = b.comment("Décalage Y à partir de l’ancre, en pixels")
                .defineInRange("buttonOffsetY", 6, 0, 10000);
        MAP_BUTTON_SIZE = b.comment("Taille (côté, px) du bouton HUD")
                .defineInRange("buttonSize", 18, 10, 64);
        MAP_BUTTON_THEME = b.comment("Thème du bouton HUD: AUTO, LIGHT, DARK").define("buttonTheme", "AUTO");
        MAP_BUTTON_TEXTURE_LIGHT = b.comment("Texture RL pour le bouton en thème clair").define("buttonTextureLight", "erinium_faction:textures/gui/map_button_light.png");
        MAP_BUTTON_TEXTURE_DARK = b.comment("Texture RL pour le bouton en thème sombre").define("buttonTextureDark", "erinium_faction:textures/gui/map_button_dark.png");
        MAP_BUTTON_TOOLTIP = b.comment("Afficher un tooltip au survol du bouton HUD").define("buttonTooltip", true);
        MAP_BUTTON_HIDE_IN_DEBUG = b.comment("Masquer le bouton HUD lorsque l’overlay F3 est actif")
                .define("hideInDebug", true);
        b.pop();

        SPEC = b.build();
    }

    private EFClientConfig() {}
}
