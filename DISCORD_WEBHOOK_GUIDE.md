# Guide du Système de Webhook Discord

## 📋 Table des matières
1. [Introduction](#introduction)
2. [Configuration](#configuration)
3. [Utilisation](#utilisation)
4. [Événements disponibles](#événements-disponibles)
5. [Placeholders](#placeholders)
6. [Exemples avancés](#exemples-avancés)
7. [Personnalisation](#personnalisation)

---

## 🎯 Introduction

Le système de webhook Discord d'Erinium Faction permet d'envoyer des notifications Discord entièrement personnalisables pour tous les événements importants de votre serveur de factions.

**Caractéristiques principales:**
- ✅ 100% configurable via fichier de config
- ✅ 14 types d'événements différents
- ✅ Placeholders dynamiques
- ✅ Embeds Discord riches (titre, description, couleur, champs, footer, etc.)
- ✅ Activation/désactivation par événement
- ✅ Asynchrone (n'impacte pas les performances du serveur)
- ✅ Gestion d'erreurs robuste

---

## ⚙️ Configuration

### 1. Obtenir un webhook Discord

1. Ouvrez Discord et allez dans les **Paramètres du canal**
2. Cliquez sur **Intégrations** → **Webhooks**
3. Cliquez sur **Nouveau Webhook**
4. Donnez-lui un nom (ex: "Erinium Faction")
5. **Copiez l'URL du webhook** (format: `https://discord.com/api/webhooks/ID/TOKEN`)

### 2. Configurer le mod

Le fichier de configuration se trouve dans: `config/erinium_faction-common.toml`

#### Configuration de base
```toml
[discord]
    # Activer le système de webhook (true/false)
    enabled = true

    # URL du webhook Discord (OBLIGATOIRE)
    webhookUrl = "https://discord.com/api/webhooks/VOTRE_ID/VOTRE_TOKEN"

    # Nom affiché pour le bot
    webhookUsername = "Erinium Faction"

    # URL de l'avatar du bot (optionnel)
    webhookAvatarUrl = "https://votre-serveur.com/avatar.png"
```

#### Activer/désactiver les événements
```toml
[discord.events]
    factionCreate = true   # Création de faction
    factionDelete = true   # Suppression de faction
    factionJoin = true     # Joueur rejoint une faction
    factionLeave = true    # Joueur quitte une faction
    factionKick = true     # Expulsion de membre
    factionPromote = true  # Promotion
    factionDemote = true   # Rétrogradation
    factionClaim = true    # Claim de territoire
    factionUnclaim = true  # Abandon de territoire
    factionWar = true      # Déclaration de guerre
    factionAlly = true     # Alliance
    factionKill = true     # Kill PvP entre factions
    factionDeath = true    # Mort de membre
    factionLevelUp = true  # Montée de niveau
```

---

## 🚀 Utilisation

### Utilisation de base dans votre code

```java
import fr.eriniumgroup.erinium_faction.common.network.DiscordWebhookManager;
import fr.eriniumgroup.erinium_faction.common.network.DiscordWebhookManager.DiscordWebhookEvent;

// Envoyer une notification de création de faction
DiscordWebhookManager.sendWebhook(DiscordWebhookEvent.FACTION_CREATE)
    .placeholder("player", playerName)
    .placeholder("faction", factionName)
    .send();
```

### Avec plusieurs placeholders

```java
DiscordWebhookManager.sendWebhook(DiscordWebhookEvent.FACTION_PROMOTE)
    .placeholder("player", adminName)
    .placeholder("target", targetPlayer)
    .placeholder("faction", factionName)
    .placeholder("old_rank", "Member")
    .placeholder("new_rank", "Officer")
    .send();
```

### Avec champs personnalisés

```java
DiscordWebhookManager.sendWebhook(DiscordWebhookEvent.FACTION_CLAIM)
    .placeholder("faction", factionName)
    .placeholder("x", chunkX)
    .placeholder("z", chunkZ)
    .field("⚡ Power", String.valueOf(factionPower))
    .field("👥 Membres", String.valueOf(memberCount))
    .send();
```

---

## 📢 Événements disponibles

| Événement | Description | Placeholders typiques |
|-----------|-------------|----------------------|
| `FACTION_CREATE` | Création de faction | `{player}`, `{faction}` |
| `FACTION_DELETE` | Suppression de faction | `{player}`, `{faction}` |
| `FACTION_JOIN` | Joueur rejoint | `{player}`, `{faction}` |
| `FACTION_LEAVE` | Joueur quitte | `{player}`, `{faction}` |
| `FACTION_KICK` | Expulsion | `{player}`, `{target}`, `{faction}` |
| `FACTION_PROMOTE` | Promotion | `{player}`, `{target}`, `{old_rank}`, `{new_rank}` |
| `FACTION_DEMOTE` | Rétrogradation | `{player}`, `{target}`, `{old_rank}`, `{new_rank}` |
| `FACTION_CLAIM` | Claim territoire | `{faction}`, `{x}`, `{y}`, `{z}`, `{dimension}` |
| `FACTION_UNCLAIM` | Abandon territoire | `{faction}`, `{x}`, `{y}`, `{z}`, `{dimension}` |
| `FACTION_WAR` | Déclaration de guerre | `{faction}`, `{target}` |
| `FACTION_ALLY` | Alliance | `{faction}`, `{target}` |
| `FACTION_KILL` | Kill PvP | `{killer}`, `{victim}`, `{faction}`, `{target}` |
| `FACTION_DEATH` | Mort de membre | `{victim}`, `{faction}` |
| `FACTION_LEVEL_UP` | Montée de niveau | `{faction}`, `{level}`, `{xp}` |

---

## 🏷️ Placeholders

### Placeholders généraux
- `{player}` - Nom du joueur principal
- `{faction}` - Nom de la faction principale
- `{target}` - Joueur ou faction cible
- `{time}` - Timestamp actuel (format: DD/MM/YYYY HH:mm:ss)

### Rangs & Promotions
- `{rank}` - Rang actuel
- `{old_rank}` - Ancien rang
- `{new_rank}` - Nouveau rang

### Combat
- `{killer}` - Nom du tueur
- `{victim}` - Nom de la victime

### Progression
- `{level}` - Niveau de la faction
- `{xp}` - Points d'expérience

### Position
- `{x}` - Coordonnée X
- `{y}` - Coordonnée Y
- `{z}` - Coordonnée Z
- `{dimension}` - Nom de la dimension

---

## 💡 Exemples avancés

### Exemple 1: Notification de kill PvP détaillée

```java
DiscordWebhookManager.sendWebhook(DiscordWebhookEvent.FACTION_KILL)
    .placeholder("killer", killerName)
    .placeholder("faction", killerFaction)
    .placeholder("victim", victimName)
    .placeholder("target", victimFaction)
    .field("🗡️ Arme", weaponName)
    .field("💰 Butin", lootValue + " $")
    .field("📍 Position", x + ", " + y + ", " + z)
    .send();
```

### Exemple 2: Level up avec statistiques

```java
DiscordWebhookManager.sendWebhook(DiscordWebhookEvent.FACTION_LEVEL_UP)
    .placeholder("faction", factionName)
    .placeholder("level", newLevel)
    .placeholder("xp", totalXP)
    .field("⚡ Power", faction.getPower() + "/" + faction.getMaxPower())
    .field("🗺️ Territoires", String.valueOf(faction.getClaimCount()))
    .field("👥 Membres", faction.getMemberCount() + "/" + faction.getMaxMembers())
    .send();
```

### Exemple 3: Message complètement personnalisé

```java
DiscordWebhookManager.sendWebhook(DiscordWebhookEvent.FACTION_WAR)
    .title("⚔️ GUERRE TOTALE!")
    .description("La faction **" + attacker + "** a déclaré la guerre à **" + defender + "**!\n\n" +
                 "Préparez-vous au combat!")
    .color(0xFF0000)  // Rouge vif
    .field("🛡️ Défenseur", defender + " (Level " + defenderLevel + ")")
    .field("⚔️ Attaquant", attacker + " (Level " + attackerLevel + ")")
    .field("📅 Date du combat", "Demain à 20h00")
    .send();
```

### Exemple 4: Avec callback asynchrone

```java
DiscordWebhookManager.sendWebhook(DiscordWebhookEvent.FACTION_CREATE)
    .placeholder("player", playerName)
    .placeholder("faction", factionName)
    .send()
    .thenAccept(success -> {
        if (success) {
            player.sendSystemMessage(Component.literal("Notification Discord envoyée!"));
        } else {
            EFC.log.warn("Échec de l'envoi du webhook Discord");
        }
    });
```

---

## 🎨 Personnalisation

### Modifier les titres des événements

Dans le fichier de config:

```toml
[discord.titles]
    factionCreate = "🎉 Nouvelle Faction!"
    factionKill = "💀 ÉLIMINATION!"
    factionLevelUp = "🆙 LEVEL UP!"
```

### Modifier les descriptions

```toml
[discord.descriptions]
    factionCreate = "**{player}** vient de créer la faction **{faction}**!"
    factionJoin = "Bienvenue à **{player}** dans **{faction}**!"
```

### Modifier les couleurs

Les couleurs sont au format décimal (convertir depuis hexadécimal).

**Exemples de conversions:**
- Rouge (#FF0000) = 16711680
- Vert (#00FF00) = 65280
- Bleu (#0000FF) = 255
- Or (#FFD700) = 16766720

```toml
[discord.colors]
    factionCreate = 5763719   # Vert Discord (#57F287)
    factionDelete = 15548997  # Rouge Discord (#ED4245)
    factionKill = 10038562    # Rouge sang (#992D22)
```

**Convertisseur en ligne:** https://www.spycolor.com/

### Footer personnalisé

```toml
[discord.footer]
    text = "Serveur Erinium • {time}"
    iconUrl = "https://votre-serveur.com/icon.png"
    thumbnailUrl = "https://votre-serveur.com/logo.png"
```

---

## 🔧 Dépannage

### Le webhook ne s'envoie pas

1. Vérifiez que `enabled = true` dans la config
2. Vérifiez que l'événement spécifique est activé (ex: `factionCreate = true`)
3. Vérifiez que l'URL du webhook est correcte
4. Vérifiez les logs du serveur pour les erreurs

### Les placeholders ne sont pas remplacés

Assurez-vous d'appeler `.placeholder("nom", valeur)` avant `.send()`:

```java
// ✅ CORRECT
.placeholder("player", playerName)
.send();

// ❌ INCORRECT
.send()
.placeholder("player", playerName);
```

### Les couleurs ne s'affichent pas correctement

Les couleurs doivent être au format décimal, pas hexadécimal:

```toml
# ❌ INCORRECT
color = "#FF0000"

# ✅ CORRECT
color = 16711680
```

---

## 📚 Fichiers importants

- **Configuration:** `src/main/java/.../config/EFConfig.java`
- **Gestionnaire:** `src/main/java/.../network/DiscordWebhookManager.java`
- **Exemples:** `src/main/java/.../network/DiscordWebhookUsageExamples.java`

---

## ✨ Fonctionnalités futures possibles

- [ ] Support des images/GIFs dans les embeds
- [ ] Système de rate limiting configurable
- [ ] Templates de messages prédéfinis
- [ ] Support de plusieurs webhooks (différents canaux)
- [ ] Filtres conditionnels (ex: n'envoyer que si level > 5)
- [ ] Support des mentions Discord (@role, @user)
- [ ] Statistiques d'envoi de webhooks

---

**Développé avec ❤️ pour Erinium Faction**
