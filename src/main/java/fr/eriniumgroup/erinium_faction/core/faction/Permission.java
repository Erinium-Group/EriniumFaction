package fr.eriniumgroup.erinium_faction.core.faction;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum centralisée pour toutes les permissions de faction.
 * Permet de mapper facilement entre les clés serveur, les clés de traduction GUI et les noms.
 */
public enum Permission {
    // Permissions de gestion de faction
    INVITE_MEMBERS("faction.invite", "invite_members", "erinium_faction.gui.permissions.invite_members"),
    KICK_MEMBERS("faction.kick", "kick_members", "erinium_faction.gui.permissions.kick_members"),
    PROMOTE_MEMBERS("faction.promote", "promote_members", "erinium_faction.gui.permissions.promote_members"),
    DEMOTE_MEMBERS("faction.demote", "demote_members", "erinium_faction.gui.permissions.demote_members"),
    CLAIM_TERRITORY("faction.claim", "claim_territory", "erinium_faction.gui.permissions.claim_territory"),
    UNCLAIM_TERRITORY("faction.unclaim", "unclaim_territory", "erinium_faction.gui.permissions.unclaim_territory"),

    // Permissions de construction
    BUILD("block.place", "build", "erinium_faction.gui.permissions.build"),
    BREAK("block.break", "break", "erinium_faction.gui.permissions.break"),
    INTERACT("block.interact", "interact", "erinium_faction.gui.permissions.interact"),
    USE_CONTAINERS("faction.use.containers", "use_containers", "erinium_faction.gui.permissions.use_containers"),

    // Permissions de gestion avancée
    MANAGE_PERMISSIONS("faction.manage.permissions", "manage_permissions", "erinium_faction.gui.permissions.manage_permissions"),
    MANAGE_ALLIANCES("faction.manage.alliances", "manage_alliances", "erinium_faction.gui.permissions.manage_alliances"),
    DEPOSTIT_BANK("faction.bank.dep", "bank_deposit", "erinium_faction.gui.permissions.bank.dep"),
    WITHDRAW_BANK("faction.bank.with", "bank_withdraw", "erinium_faction.gui.permissions.bank.with"),

    // Permissions de téléportation
    USE_TELEPORTS("faction.use.teleports", "use_teleports", "erinium_faction.gui.permissions.use_teleports"),
    SET_HOME("faction.set.home", "set_home", "erinium_faction.gui.permissions.set_home"),
    CREATE_WARPS("faction.create.warps", "create_warps", "erinium_faction.gui.permissions.create_warps"),
    DELETE_WARPS("faction.delete.warps", "delete_warps", "erinium_faction.gui.permissions.delete_warps"),

    // Permissions de coffre et shop
    MANAGE_SHOP("faction.manage.shop", "manage_shop", "erinium_faction.gui.permissions.manage_shop"),
    ACCESS_CHEST("faction.access.chest", "access_chest", "erinium_faction.gui.permissions.access_chest"),
    MANAGE_CHEST("faction.manage.chest", "manage_chest", "erinium_faction.gui.permissions.manage_chest"),

    // Permissions de bannière et cape
    BANNER_EDIT("faction.banner.edit", "banner_edit", "erinium_faction.gui.permissions.banner_edit"),
    BANNER_GET("faction.banner.get", "banner_get", "erinium_faction.gui.permissions.banner_get");

    private final String serverKey;      // Clé utilisée côté serveur (ex: "faction.invite")
    private final String guiKey;         // Clé courte pour la GUI (ex: "invite_members")
    private final String translationKey; // Clé de traduction complète

    // Maps statiques pour lookup rapide
    private static final Map<String, Permission> BY_SERVER_KEY = new HashMap<>();
    private static final Map<String, Permission> BY_GUI_KEY = new HashMap<>();
    private static final Map<String, Permission> BY_TRANSLATION_KEY = new HashMap<>();

    static {
        for (Permission perm : values()) {
            BY_SERVER_KEY.put(perm.serverKey, perm);
            BY_GUI_KEY.put(perm.guiKey, perm);
            BY_TRANSLATION_KEY.put(perm.translationKey, perm);
        }
    }

    Permission(String serverKey, String guiKey, String translationKey) {
        this.serverKey = serverKey;
        this.guiKey = guiKey;
        this.translationKey = translationKey;
    }

    /**
     * @return La clé utilisée côté serveur (ex: "faction.invite", "block.place")
     */
    public String getServerKey() {
        return serverKey;
    }

    /**
     * @return La clé courte utilisée dans la GUI (ex: "invite_members", "build")
     */
    public String getGuiKey() {
        return guiKey;
    }

    /**
     * @return La clé de traduction complète (ex: "erinium_faction.gui.permissions.invite_members")
     */
    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * @return La clé de traduction pour la description (suffixe ".desc")
     */
    public String getDescriptionKey() {
        return translationKey + ".desc";
    }

    /**
     * Trouve une permission à partir de sa clé serveur
     * @param serverKey La clé serveur (ex: "faction.invite")
     * @return La permission correspondante ou null si non trouvée
     */
    public static Permission fromServerKey(String serverKey) {
        return BY_SERVER_KEY.get(serverKey);
    }

    /**
     * Trouve une permission à partir de sa clé GUI
     * @param guiKey La clé GUI (ex: "invite_members")
     * @return La permission correspondante ou null si non trouvée
     */
    public static Permission fromGuiKey(String guiKey) {
        return BY_GUI_KEY.get(guiKey);
    }

    /**
     * Trouve une permission à partir de sa clé de traduction
     * @param translationKey La clé de traduction
     * @return La permission correspondante ou null si non trouvée
     */
    public static Permission fromTranslationKey(String translationKey) {
        return BY_TRANSLATION_KEY.get(translationKey);
    }

    /**
     * @return Toutes les permissions sous forme de tableau
     */
    public static Permission[] all() {
        return values();
    }

    /**
     * @return Toutes les clés GUI sous forme de tableau (pour la compatibilité)
     */
    public static String[] allGuiKeys() {
        Permission[] perms = values();
        String[] keys = new String[perms.length];
        for (int i = 0; i < perms.length; i++) {
            keys[i] = perms[i].guiKey;
        }
        return keys;
    }

    /**
     * @return Toutes les clés serveur sous forme de tableau
     */
    public static String[] allServerKeys() {
        Permission[] perms = values();
        String[] keys = new String[perms.length];
        for (int i = 0; i < perms.length; i++) {
            keys[i] = perms[i].serverKey;
        }
        return keys;
    }
}
