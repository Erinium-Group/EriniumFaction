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

    public static final ModConfigSpec.BooleanValue AUTO_SCREEN_RESIZE;

    public static final ModConfigSpec.IntValue NAME_MAX_LENGTH;
    public static final ModConfigSpec.IntValue DESCRIPTION_MAX_LENGTH;

    // Waypoints
    public static final ModConfigSpec.IntValue WAYPOINT_MAX_OVERLAY_DISTANCE;

    // Nameplates
    public static final ModConfigSpec.BooleanValue NAMEPLATE_SHOW_FACTION;
    public static final ModConfigSpec.BooleanValue NAMEPLATE_SHOW_LEVEL;
    public static final ModConfigSpec.IntValue NAMEPLATE_BACKGROUND_COLOR;
    public static final ModConfigSpec.IntValue NAMEPLATE_NAME_COLOR;
    public static final ModConfigSpec.IntValue NAMEPLATE_HEALTH_BAR_COLOR;
    public static final ModConfigSpec.IntValue NAMEPLATE_HEALTH_BAR_BACKGROUND_COLOR;
    public static final ModConfigSpec.IntValue NAMEPLATE_HEALTH_TEXT_COLOR;
    public static final ModConfigSpec.IntValue NAMEPLATE_FACTION_COLOR;
    public static final ModConfigSpec.IntValue NAMEPLATE_LEVEL_COLOR;
    public static final ModConfigSpec.IntValue NAMEPLATE_MAX_WIDTH;

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

        b.push("Client Screen");
        AUTO_SCREEN_RESIZE = b.comment("Auto resize to 800x600 if you want to put under set false but you may have problem with GUI").define("enabled", true);
        b.pop();

        b.push("factionFields");
        NAME_MAX_LENGTH = b.comment("Nombre max de caractères pour le nom de faction").defineInRange("nameMaxLength", 16, 1, 64);
        DESCRIPTION_MAX_LENGTH = b.comment("Nombre max de caractères pour la description de faction").defineInRange("descriptionMaxLength", 150, 1, 500);
        b.pop();

        b.push("waypoints");
        WAYPOINT_MAX_OVERLAY_DISTANCE = b.comment("Distance maximale (en blocs) pour afficher les waypoints dans l'overlay HUD")
                .defineInRange("maxOverlayDistance", 500, 0, 10000);
        b.pop();

        b.push("nameplates");
        NAMEPLATE_SHOW_FACTION = b.comment("Afficher le nom de faction au-dessus des joueurs")
                .define("showFaction", true);
        NAMEPLATE_SHOW_LEVEL = b.comment("Afficher le niveau du joueur")
                .define("showLevel", true);
        NAMEPLATE_MAX_WIDTH = b.comment("Largeur maximale de la nameplate en pixels")
                .defineInRange("maxWidth", 80, 40, 200);
        NAMEPLATE_BACKGROUND_COLOR = b.comment("Couleur de fond (ARGB hex, ex: 0xAA000000)")
                .defineInRange("backgroundColor", 0xAA000000, Integer.MIN_VALUE, Integer.MAX_VALUE);
        NAMEPLATE_NAME_COLOR = b.comment("Couleur du nom du joueur (ARGB hex)")
                .defineInRange("nameColor", 0xFFFFFFFF, Integer.MIN_VALUE, Integer.MAX_VALUE);
        NAMEPLATE_HEALTH_BAR_COLOR = b.comment("Couleur de la barre de vie (ARGB hex)")
                .defineInRange("healthBarColor", 0xFF00FF00, Integer.MIN_VALUE, Integer.MAX_VALUE);
        NAMEPLATE_HEALTH_BAR_BACKGROUND_COLOR = b.comment("Couleur de fond de la barre de vie (ARGB hex)")
                .defineInRange("healthBarBackgroundColor", 0xFF333333, Integer.MIN_VALUE, Integer.MAX_VALUE);
        NAMEPLATE_HEALTH_TEXT_COLOR = b.comment("Couleur du texte de vie (ARGB hex)")
                .defineInRange("healthTextColor", 0xFFFFFFFF, Integer.MIN_VALUE, Integer.MAX_VALUE);
        NAMEPLATE_FACTION_COLOR = b.comment("Couleur du nom de faction (ARGB hex)")
                .defineInRange("factionColor", 0xFFFFAA00, Integer.MIN_VALUE, Integer.MAX_VALUE);
        NAMEPLATE_LEVEL_COLOR = b.comment("Couleur du niveau (ARGB hex)")
                .defineInRange("levelColor", 0xFF55FF55, Integer.MIN_VALUE, Integer.MAX_VALUE);
        b.pop();

        SPEC = b.build();
    }

    private EFClientConfig() {}
}
