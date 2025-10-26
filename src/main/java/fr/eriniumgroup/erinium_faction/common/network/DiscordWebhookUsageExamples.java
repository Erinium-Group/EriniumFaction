package fr.eriniumgroup.erinium_faction.common.network;

/**
 * EXEMPLES D'UTILISATION DU SYSTÈME DE WEBHOOK DISCORD
 *
 * Ce fichier contient des exemples de code pour utiliser le système de webhook Discord
 * dans vos classes. Vous pouvez copier-coller ces exemples et les adapter à vos besoins.
 *
 * NOTE: Ce fichier est purement documentaire et ne contient pas de code exécutable.
 */
public class DiscordWebhookUsageExamples {

    /**
     * EXEMPLE 1: Envoyer une notification de création de faction
     *
     * Utilisation typique dans FactionCommands ou FactionManager:
     */
    public void exampleFactionCreate(String playerName, String factionName) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_CREATE)
            .placeholder("player", playerName)
            .placeholder("faction", factionName)
            .send();
    }

    /**
     * EXEMPLE 2: Envoyer une notification quand un joueur rejoint une faction
     */
    public void exampleFactionJoin(String playerName, String factionName) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_JOIN)
            .placeholder("player", playerName)
            .placeholder("faction", factionName)
            .send();
    }

    /**
     * EXEMPLE 3: Envoyer une notification de kick avec message personnalisé
     */
    public void exampleFactionKick(String adminName, String targetName, String factionName, String reason) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_KICK)
            .placeholder("player", adminName)
            .placeholder("target", targetName)
            .placeholder("faction", factionName)
            .field("Raison", reason)  // Champ additionnel
            .send();
    }

    /**
     * EXEMPLE 4: Envoyer une notification de promotion avec rangs
     */
    public void exampleFactionPromote(String adminName, String targetName, String factionName, String oldRank, String newRank) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_PROMOTE)
            .placeholder("player", adminName)
            .placeholder("target", targetName)
            .placeholder("faction", factionName)
            .placeholder("old_rank", oldRank)
            .placeholder("new_rank", newRank)
            .send();
    }

    /**
     * EXEMPLE 5: Envoyer une notification de claim avec coordonnées
     */
    public void exampleFactionClaim(String factionName, int x, int y, int z, String dimension) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_CLAIM)
            .placeholder("faction", factionName)
            .placeholder("x", x)
            .placeholder("y", y)
            .placeholder("z", z)
            .placeholder("dimension", dimension)
            .send();
    }

    /**
     * EXEMPLE 6: Envoyer une notification de guerre entre factions
     */
    public void exampleFactionWar(String attackerFaction, String defenderFaction) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_WAR)
            .placeholder("faction", attackerFaction)
            .placeholder("target", defenderFaction)
            .send();
    }

    /**
     * EXEMPLE 7: Envoyer une notification de kill PvP
     */
    public void exampleFactionKill(String killerName, String killerFaction, String victimName, String victimFaction, String weapon) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_KILL)
            .placeholder("killer", killerName)
            .placeholder("faction", killerFaction)
            .placeholder("victim", victimName)
            .placeholder("target", victimFaction)
            .field("Arme", weapon)  // Champ additionnel
            .send();
    }

    /**
     * EXEMPLE 8: Envoyer une notification de level up avec détails
     */
    public void exampleFactionLevelUp(String factionName, int newLevel, int totalXP, int membersCount) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_LEVEL_UP)
            .placeholder("faction", factionName)
            .placeholder("level", newLevel)
            .placeholder("xp", totalXP)
            .field("Membres", String.valueOf(membersCount))
            .send();
    }

    /**
     * EXEMPLE 9: Envoyer une notification avec titre et description personnalisés
     */
    public void exampleCustomMessage(String playerName, String factionName) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_CREATE)
            .title("✨ Événement Spécial")
            .description("**" + playerName + "** vient de créer une faction légendaire: **" + factionName + "**!")
            .color(0xFF00FF)  // Couleur magenta personnalisée
            .placeholder("player", playerName)
            .placeholder("faction", factionName)
            .send();
    }

    /**
     * EXEMPLE 10: Envoyer une notification avec plusieurs champs personnalisés
     */
    public void exampleMultipleFields(String factionName, int level, int power, int claims, int members) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_LEVEL_UP)
            .placeholder("faction", factionName)
            .placeholder("level", level)
            .field("⚡ Power", String.valueOf(power))
            .field("🗺️ Territoires", String.valueOf(claims))
            .field("👥 Membres", String.valueOf(members))
            .send();
    }

    /**
     * EXEMPLE 11: Envoyer un webhook simple sans placeholders
     */
    public void exampleSimpleWebhook() {
        // Pour les événements qui n'ont pas besoin de données dynamiques
        DiscordWebhookManager.sendSimpleWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_CREATE);
    }

    /**
     * EXEMPLE 12: Gestion asynchrone avec callback
     */
    public void exampleAsyncWithCallback(String playerName, String factionName) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_CREATE)
            .placeholder("player", playerName)
            .placeholder("faction", factionName)
            .send()
            .thenAccept(success -> {
                if (success) {
                    // Webhook envoyé avec succès
                    System.out.println("Webhook Discord envoyé!");
                } else {
                    // Échec de l'envoi
                    System.out.println("Erreur lors de l'envoi du webhook Discord");
                }
            });
    }

    /**
     * EXEMPLE 13: Notification de mort avec coordonnées
     */
    public void exampleFactionDeath(String victimName, String factionName, int x, int y, int z, String deathMessage) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_DEATH)
            .placeholder("victim", victimName)
            .placeholder("faction", factionName)
            .field("📍 Position", x + ", " + y + ", " + z)
            .field("💀 Cause", deathMessage)
            .send();
    }

    /**
     * EXEMPLE 14: Alliance entre deux factions
     */
    public void exampleFactionAlly(String faction1, String faction2, String initiator) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_ALLY)
            .placeholder("faction", faction1)
            .placeholder("target", faction2)
            .field("Initié par", initiator)
            .send();
    }

    /**
     * EXEMPLE 15: Unclaim avec raison
     */
    public void exampleFactionUnclaim(String factionName, int x, int z, String dimension, String reason) {
        DiscordWebhookManager.sendWebhook(DiscordWebhookManager.DiscordWebhookEvent.FACTION_UNCLAIM)
            .placeholder("faction", factionName)
            .placeholder("x", x)
            .placeholder("z", z)
            .placeholder("dimension", dimension)
            .field("Raison", reason)
            .send();
    }

    /* ================================================================
     * PLACEHOLDERS DISPONIBLES
     * ================================================================
     *
     * Vous pouvez utiliser les placeholders suivants dans vos messages:
     *
     * - {player}     : Nom du joueur principal de l'action
     * - {faction}    : Nom de la faction principale
     * - {target}     : Nom du joueur/faction cible (kick, promotion, guerre, etc.)
     * - {rank}       : Rang actuel
     * - {old_rank}   : Ancien rang (pour promotion/rétrogradation)
     * - {new_rank}   : Nouveau rang (pour promotion/rétrogradation)
     * - {killer}     : Nom du tueur (pour kills PvP)
     * - {victim}     : Nom de la victime (pour kills/deaths)
     * - {level}      : Niveau de la faction
     * - {xp}         : XP de la faction
     * - {x}, {y}, {z}: Coordonnées
     * - {dimension}  : Nom de la dimension
     * - {time}       : Timestamp actuel (automatique)
     *
     * Tous les placeholders sont configurables dans la config!
     * ================================================================
     */

    /* ================================================================
     * CONFIGURATION REQUISE
     * ================================================================
     *
     * Pour utiliser le système de webhook Discord, configurez ces valeurs
     * dans le fichier de config (config/erinium_faction-common.toml):
     *
     * [discord]
     *     enabled = true
     *     webhookUrl = "https://discord.com/api/webhooks/VOTRE_WEBHOOK_ID/VOTRE_TOKEN"
     *     webhookUsername = "Erinium Faction"
     *     webhookAvatarUrl = "URL_DE_VOTRE_AVATAR" (optionnel)
     *
     * Vous pouvez ensuite personnaliser chaque événement individuellement!
     * ================================================================
     */

    /* ================================================================
     * OBTENIR UN WEBHOOK DISCORD
     * ================================================================
     *
     * 1. Allez dans les paramètres de votre canal Discord
     * 2. Cliquez sur "Intégrations"
     * 3. Cliquez sur "Webhooks" puis "Nouveau Webhook"
     * 4. Donnez-lui un nom et copiez l'URL du webhook
     * 5. Collez l'URL dans la config (webhookUrl)
     *
     * ================================================================
     */
}
