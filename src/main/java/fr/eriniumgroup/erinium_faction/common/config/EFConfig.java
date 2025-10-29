package fr.eriniumgroup.erinium_faction.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class EFConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue FACTION_NAME_MIN;
    public static final ModConfigSpec.IntValue FACTION_NAME_MAX;
    public static final ModConfigSpec.IntValue FACTION_DESC_MAX;
    public static final ModConfigSpec.IntValue FACTION_MAX_MEMBERS; // plafond global dur

    public static final ModConfigSpec.IntValue FACTION_BASE_MAX_PLAYERS; // base par faction
    public static final ModConfigSpec.IntValue FACTION_PLAYERS_PER_LEVEL; // +N par niveau

    public static final ModConfigSpec.DoubleValue FACTION_BASE_MAX_POWER;
    public static final ModConfigSpec.DoubleValue FACTION_POWER_PER_MEMBER;
    public static final ModConfigSpec.DoubleValue POWER_REGEN_PER_MINUTE;
    public static final ModConfigSpec.DoubleValue POWER_LOSS_ON_DEATH;

    public static final ModConfigSpec.IntValue XP_PER_KILL;
    public static final ModConfigSpec.BooleanValue FRIENDLY_FIRE;
    public static final ModConfigSpec.BooleanValue ALLOW_ALLIES;
    public static final ModConfigSpec.BooleanValue ALLY_DAMAGE;

    // Joueur power
    public static final ModConfigSpec.DoubleValue PLAYER_BASE_MAX_POWER;
    public static final ModConfigSpec.DoubleValue PLAYER_POWER_REGEN_PER_MINUTE;
    public static final ModConfigSpec.DoubleValue PLAYER_POWER_LOSS_ON_DEATH;
    public static final ModConfigSpec.BooleanValue FACTION_MAX_POWER_FROM_PLAYERS;

    public static final ModConfigSpec.IntValue FACTION_MAX_CLAIMS;

    public static final ModConfigSpec.IntValue FACTION_BASE_WARPS;
    public static final ModConfigSpec.IntValue FACTION_WARPS_PER_5_LEVELS;

    public static final ModConfigSpec.IntValue FACTION_TP_WARMUP_SECONDS;
    public static final ModConfigSpec.IntValue FACTION_TP_COOLDOWN_SECONDS;
    public static final ModConfigSpec.BooleanValue FACTION_TP_CANCEL_ON_MOVE;
    public static final ModConfigSpec.BooleanValue FACTION_TP_CANCEL_ON_DAMAGE;
    public static final ModConfigSpec.BooleanValue FACTION_TP_ALLOW_CROSS_DIM;

    // Discord Webhook
    public static final ModConfigSpec.BooleanValue DISCORD_WEBHOOK_ENABLED;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_WEBHOOK_URL;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_WEBHOOK_USERNAME;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_WEBHOOK_AVATAR_URL;

    // Discord Events - Enable/Disable
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_CREATE;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_DELETE;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_JOIN;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_LEAVE;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_KICK;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_PROMOTE;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_DEMOTE;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_CLAIM;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_UNCLAIM;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_WAR;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_ALLY;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_KILL;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_DEATH;
    public static final ModConfigSpec.BooleanValue DISCORD_EVENT_FACTION_LEVEL_UP;

    // Discord Messages - Titles
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_CREATE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_DELETE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_JOIN;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_LEAVE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_KICK;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_PROMOTE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_DEMOTE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_CLAIM;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_UNCLAIM;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_WAR;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_ALLY;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_KILL;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_DEATH;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_TITLE_FACTION_LEVEL_UP;

    // Discord Messages - Descriptions
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_CREATE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_DELETE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_JOIN;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_LEAVE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_KICK;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_PROMOTE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_DEMOTE;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_CLAIM;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_UNCLAIM;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_WAR;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_ALLY;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_KILL;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_DEATH;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_DESC_FACTION_LEVEL_UP;

    // Discord Colors (decimal format)
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_CREATE;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_DELETE;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_JOIN;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_LEAVE;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_KICK;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_PROMOTE;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_DEMOTE;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_CLAIM;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_UNCLAIM;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_WAR;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_ALLY;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_KILL;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_DEATH;
    public static final ModConfigSpec.IntValue DISCORD_COLOR_FACTION_LEVEL_UP;

    // Discord Footer
    public static final ModConfigSpec.ConfigValue<String> DISCORD_FOOTER_TEXT;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_FOOTER_ICON_URL;
    public static final ModConfigSpec.ConfigValue<String> DISCORD_THUMBNAIL_URL;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("factions");
        FACTION_NAME_MIN = b.comment("Longueur minimale du nom de faction").defineInRange("nameMin", 3, 2, 32);
        FACTION_NAME_MAX = b.comment("Longueur maximale du nom de faction").defineInRange("nameMax", 16, 3, 32);
        FACTION_DESC_MAX = b.comment("Longueur maximale de la description de faction").defineInRange("descMax", 150, 0, 500);
        FACTION_MAX_MEMBERS = b.comment("Plafond global dur du nombre de membres par faction").defineInRange("maxMembers", 30, 1, 2000);
        FACTION_BASE_MAX_PLAYERS = b.comment("Nombre de membres autorisés par faction au niveau 0").defineInRange("baseMaxPlayers", 9, 1, 2000);
        FACTION_PLAYERS_PER_LEVEL = b.comment("Augmentation du nombre de membres autorisés par niveau").defineInRange("playersPerLevel", 1, 0, 2000);

        FACTION_BASE_MAX_POWER = b.comment("Puissance maximale de base par faction").defineInRange("baseMaxPower", 10.0, 0.0, 100000.0);
        FACTION_POWER_PER_MEMBER = b.comment("Bonus de puissance max par membre (non utilisé si FACTION_MAX_POWER_FROM_PLAYERS=true)").defineInRange("powerPerMember", 10.0, 0.0, 10000.0);
        POWER_REGEN_PER_MINUTE = b.comment("Régénération de puissance par minute (faction)").defineInRange("powerRegenPerMinute", 0.2, 0.0, 1000.0);
        POWER_LOSS_ON_DEATH = b.comment("Perte de puissance de faction à la mort d'un membre").defineInRange("powerLossOnDeath", 1.5, 0.0, 1000.0);

        XP_PER_KILL = b.comment("XP de faction gagné par kill").defineInRange("xpPerKill", 10, 0, 10000);
        FRIENDLY_FIRE = b.comment("Le friendly fire au sein d'une faction est-il autorisé ?").define("friendlyFire", false);
        ALLOW_ALLIES = b.comment("Autoriser des alliances (placeholder)").define("allowAllies", true);
        ALLY_DAMAGE = b.comment("Autoriser les dégâts entre alliés").define("allyDamage", false);
        FACTION_MAX_POWER_FROM_PLAYERS = b.comment("Additionner les max power des joueurs au max de faction").define("factionMaxFromPlayers", true);
        FACTION_MAX_CLAIMS = b.comment("Nombre maximum de chunks claim par faction").defineInRange("maxClaims", 100, 0, 100000);
        FACTION_BASE_WARPS = b.comment("Nombre de warps autorisés au niveau 0").defineInRange("baseWarps", 1, 0, 1000);
        FACTION_WARPS_PER_5_LEVELS = b.comment("Warps supplémentaires tous les 5 niveaux").defineInRange("warpsPer5Levels", 1, 0, 1000);
        FACTION_TP_WARMUP_SECONDS = b.comment("Temps de préparation (warmup) avant une TP home/warp, en secondes").defineInRange("tpWarmupSeconds", 3, 0, 300);
        FACTION_TP_COOLDOWN_SECONDS = b.comment("Cooldown entre deux TP home/warp, en secondes").defineInRange("tpCooldownSeconds", 15, 0, 3600);
        FACTION_TP_CANCEL_ON_MOVE = b.comment("Annuler le warmup si le joueur bouge").define("tpCancelOnMove", true);
        FACTION_TP_CANCEL_ON_DAMAGE = b.comment("Annuler le warmup si le joueur prend des dégâts").define("tpCancelOnDamage", true);
        FACTION_TP_ALLOW_CROSS_DIM = b.comment("Autoriser la téléportation inter-dimensionnelle").define("tpAllowCrossDimension", true);
        b.pop();

        b.push("players");
        PLAYER_BASE_MAX_POWER = b.comment("Puissance max de base par joueur").defineInRange("baseMaxPower", 10.0, 0.0, 100000.0);
        PLAYER_POWER_REGEN_PER_MINUTE = b.comment("Régénération de puissance par minute (joueur)").defineInRange("powerRegenPerMinute", 0.2, 0.0, 1000.0);
        PLAYER_POWER_LOSS_ON_DEATH = b.comment("Perte de puissance à la mort (joueur)").defineInRange("powerLossOnDeath", 2, 0.0, 1000.0);
        b.pop();

        b.push("discord");
        b.comment("=== DISCORD WEBHOOK SYSTEM ===",
                "Configuration complète pour envoyer des notifications Discord",
                "Placeholders disponibles: {player}, {faction}, {rank}, {target}, {killer}, {victim}, {level}, {xp}, {x}, {y}, {z}, {dimension}, {old_rank}, {new_rank}");

        DISCORD_WEBHOOK_ENABLED = b.comment("Activer le système de webhook Discord").define("enabled", false);
        DISCORD_WEBHOOK_URL = b.comment("URL du webhook Discord (obtenue depuis les paramètres du canal Discord)").define("webhookUrl", "");
        DISCORD_WEBHOOK_USERNAME = b.comment("Nom affiché pour le bot Discord").define("webhookUsername", "Erinium Faction");
        DISCORD_WEBHOOK_AVATAR_URL = b.comment("URL de l'avatar du bot Discord").define("webhookAvatarUrl", "");

        b.push("events");
        DISCORD_EVENT_FACTION_CREATE = b.comment("Activer les notifications de création de faction").define("factionCreate", true);
        DISCORD_EVENT_FACTION_DELETE = b.comment("Activer les notifications de suppression de faction").define("factionDelete", true);
        DISCORD_EVENT_FACTION_JOIN = b.comment("Activer les notifications quand un joueur rejoint une faction").define("factionJoin", true);
        DISCORD_EVENT_FACTION_LEAVE = b.comment("Activer les notifications quand un joueur quitte une faction").define("factionLeave", true);
        DISCORD_EVENT_FACTION_KICK = b.comment("Activer les notifications d'expulsion de membre").define("factionKick", true);
        DISCORD_EVENT_FACTION_PROMOTE = b.comment("Activer les notifications de promotion").define("factionPromote", true);
        DISCORD_EVENT_FACTION_DEMOTE = b.comment("Activer les notifications de rétrogradation").define("factionDemote", true);
        DISCORD_EVENT_FACTION_CLAIM = b.comment("Activer les notifications de claim de territoire").define("factionClaim", true);
        DISCORD_EVENT_FACTION_UNCLAIM = b.comment("Activer les notifications d'abandon de territoire").define("factionUnclaim", true);
        DISCORD_EVENT_FACTION_WAR = b.comment("Activer les notifications de déclaration de guerre").define("factionWar", true);
        DISCORD_EVENT_FACTION_ALLY = b.comment("Activer les notifications d'alliance").define("factionAlly", true);
        DISCORD_EVENT_FACTION_KILL = b.comment("Activer les notifications de kill entre factions").define("factionKill", true);
        DISCORD_EVENT_FACTION_DEATH = b.comment("Activer les notifications de mort de membre").define("factionDeath", true);
        DISCORD_EVENT_FACTION_LEVEL_UP = b.comment("Activer les notifications de montée de niveau").define("factionLevelUp", true);
        b.pop();

        b.push("titles");
        DISCORD_TITLE_FACTION_CREATE = b.define("factionCreate", "🏰 Nouvelle Faction Créée");
        DISCORD_TITLE_FACTION_DELETE = b.define("factionDelete", "💔 Faction Dissoute");
        DISCORD_TITLE_FACTION_JOIN = b.define("factionJoin", "👋 Nouveau Membre");
        DISCORD_TITLE_FACTION_LEAVE = b.define("factionLeave", "👋 Membre Parti");
        DISCORD_TITLE_FACTION_KICK = b.define("factionKick", "⚠️ Membre Expulsé");
        DISCORD_TITLE_FACTION_PROMOTE = b.define("factionPromote", "⬆️ Promotion");
        DISCORD_TITLE_FACTION_DEMOTE = b.define("factionDemote", "⬇️ Rétrogradation");
        DISCORD_TITLE_FACTION_CLAIM = b.define("factionClaim", "🚩 Territoire Conquis");
        DISCORD_TITLE_FACTION_UNCLAIM = b.define("factionUnclaim", "🏳️ Territoire Abandonné");
        DISCORD_TITLE_FACTION_WAR = b.define("factionWar", "⚔️ Déclaration de Guerre");
        DISCORD_TITLE_FACTION_ALLY = b.define("factionAlly", "🤝 Nouvelle Alliance");
        DISCORD_TITLE_FACTION_KILL = b.define("factionKill", "💀 Elimination");
        DISCORD_TITLE_FACTION_DEATH = b.define("factionDeath", "☠️ Mort");
        DISCORD_TITLE_FACTION_LEVEL_UP = b.define("factionLevelUp", "🎉 Montée de Niveau");
        b.pop();

        b.push("descriptions");
        DISCORD_DESC_FACTION_CREATE = b.define("factionCreate", "**{player}** a créé la faction **{faction}**!");
        DISCORD_DESC_FACTION_DELETE = b.define("factionDelete", "La faction **{faction}** a été dissoute par **{player}**.");
        DISCORD_DESC_FACTION_JOIN = b.define("factionJoin", "**{player}** a rejoint la faction **{faction}**!");
        DISCORD_DESC_FACTION_LEAVE = b.define("factionLeave", "**{player}** a quitté la faction **{faction}**.");
        DISCORD_DESC_FACTION_KICK = b.define("factionKick", "**{target}** a été expulsé de **{faction}** par **{player}**.");
        DISCORD_DESC_FACTION_PROMOTE = b.define("factionPromote", "**{target}** a été promu au rang **{new_rank}** dans **{faction}**!");
        DISCORD_DESC_FACTION_DEMOTE = b.define("factionDemote", "**{target}** a été rétrogradé au rang **{new_rank}** dans **{faction}**.");
        DISCORD_DESC_FACTION_CLAIM = b.define("factionClaim", "**{faction}** a claim un territoire en **{x}**, **{z}** ({dimension}).");
        DISCORD_DESC_FACTION_UNCLAIM = b.define("factionUnclaim", "**{faction}** a abandonné un territoire en **{x}**, **{z}** ({dimension}).");
        DISCORD_DESC_FACTION_WAR = b.define("factionWar", "**{faction}** a déclaré la guerre à **{target}**!");
        DISCORD_DESC_FACTION_ALLY = b.define("factionAlly", "**{faction}** et **{target}** sont maintenant alliés!");
        DISCORD_DESC_FACTION_KILL = b.define("factionKill", "**{killer}** ({faction}) a éliminé **{victim}** ({target})!");
        DISCORD_DESC_FACTION_DEATH = b.define("factionDeath", "**{victim}** de **{faction}** est mort.");
        DISCORD_DESC_FACTION_LEVEL_UP = b.define("factionLevelUp", "**{faction}** est passée au niveau **{level}**! ({xp} XP)");
        b.pop();

        b.push("colors");
        b.comment("Couleurs en format décimal (convertir depuis hex: https://www.spycolor.com/)",
                "Exemples: Vert=#00FF00=65280, Rouge=#FF0000=16711680, Bleu=#0000FF=255");
        DISCORD_COLOR_FACTION_CREATE = b.defineInRange("factionCreate", 5763719, 0, 16777215); // #57F287 (vert Discord)
        DISCORD_COLOR_FACTION_DELETE = b.defineInRange("factionDelete", 15548997, 0, 16777215); // #ED4245 (rouge Discord)
        DISCORD_COLOR_FACTION_JOIN = b.defineInRange("factionJoin", 3447003, 0, 16777215); // #3498DB (bleu)
        DISCORD_COLOR_FACTION_LEAVE = b.defineInRange("factionLeave", 10070709, 0, 16777215); // #99AAB5 (gris)
        DISCORD_COLOR_FACTION_KICK = b.defineInRange("factionKick", 15105570, 0, 16777215); // #E67E22 (orange)
        DISCORD_COLOR_FACTION_PROMOTE = b.defineInRange("factionPromote", 15844367, 0, 16777215); // #F1C40F (jaune or)
        DISCORD_COLOR_FACTION_DEMOTE = b.defineInRange("factionDemote", 11027200, 0, 16777215); // #A84300 (marron)
        DISCORD_COLOR_FACTION_CLAIM = b.defineInRange("factionClaim", 9807270, 0, 16777215); // #9580F6 (violet)
        DISCORD_COLOR_FACTION_UNCLAIM = b.defineInRange("factionUnclaim", 8421504, 0, 16777215); // #808080 (gris)
        DISCORD_COLOR_FACTION_WAR = b.defineInRange("factionWar", 12260864, 0, 16777215); // #BB0000 (rouge foncé)
        DISCORD_COLOR_FACTION_ALLY = b.defineInRange("factionAlly", 2067276, 0, 16777215); // #1F8B4C (vert foncé)
        DISCORD_COLOR_FACTION_KILL = b.defineInRange("factionKill", 10038562, 0, 16777215); // #992D22 (rouge sang)
        DISCORD_COLOR_FACTION_DEATH = b.defineInRange("factionDeath", 5533306, 0, 16777215); // #546E7A (gris bleuté)
        DISCORD_COLOR_FACTION_LEVEL_UP = b.defineInRange("factionLevelUp", 16766720, 0, 16777215); // #FFD700 (or)
        b.pop();

        b.push("footer");
        DISCORD_FOOTER_TEXT = b.define("text", "Erinium Faction • {time}");
        DISCORD_FOOTER_ICON_URL = b.define("iconUrl", "");
        DISCORD_THUMBNAIL_URL = b.comment("URL de l'image miniature affichée dans l'embed").define("thumbnailUrl", "");
        b.pop();

        b.pop(); // discord

        SPEC = b.build();
    }

    private EFConfig() {}
}
