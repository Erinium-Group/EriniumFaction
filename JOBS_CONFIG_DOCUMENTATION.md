# Documentation - Système de Configuration des Métiers

## Vue d'ensemble

Le système de métiers utilise des fichiers JSON pour configurer:
- **XP Earning**: Quelles actions donnent de l'XP et combien
- **Unlocking**: Quels éléments sont débloqués à quel niveau

## Emplacement des fichiers

Les fichiers de configuration se trouvent dans:
```
config/erinium_faction/jobs/
├── miner.json
├── lumberjack.json
├── hunter.json
├── fisher.json
├── farmer.json
└── wizard.json
```

Ces fichiers sont **générés automatiquement** au premier lancement avec des valeurs par défaut.

## Structure JSON

### Racine du fichier

```json
{
  "jobType": "MINER",
  "xpEarning": [ ... ],
  "unlocking": [ ... ]
}
```

| Champ | Type | Description |
|-------|------|-------------|
| `jobType` | String | Nom du métier: MINER, LUMBERJACK, HUNTER, FISHER, FARMER, WIZARD |
| `xpEarning` | Array | Liste des actions qui donnent de l'XP |
| `unlocking` | Array | Liste des débloquages par niveau |

---

## XP Earning

### Structure d'une entrée

```json
{
  "actionType": "BREAK",
  "targetId": "minecraft:diamond_ore",
  "minLevel": 10,
  "maxLevel": -1,
  "xpEarned": 100,
  "iconItem": "minecraft:diamond_ore"
}
```

### Champs

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| `actionType` | String | Type d'action (voir ci-dessous) | ✅ |
| `targetId` | String | ID de l'élément ciblé | ✅ |
| `minLevel` | Int | Niveau minimum requis (-1 = aucun) | ✅ |
| `maxLevel` | Int | Niveau maximum (-1 = aucun) | ✅ |
| `xpEarned` | Int | XP donné par l'action | ✅ |
| `iconItem` | String | Item ID pour l'icône GUI | ✅ |

### Types d'actions (actionType)

| Type | Description | targetId attendu | Exemple |
|------|-------------|------------------|---------|
| `BREAK` | Casser un bloc | Block ID | `minecraft:stone` |
| `PLACE` | Placer un bloc | Block ID | `minecraft:oak_planks` |
| `CRAFT` | Crafter un item | Item ID | `minecraft:iron_pickaxe` |
| `SMELT` | Fondre un item | Item ID | `minecraft:iron_ingot` |
| `KILL` | Tuer une entité | Entity Type ID | `minecraft:zombie` |
| `FISHING` | Pêcher un item | Item ID | `minecraft:cod` |
| `DRINK` | Boire un item | Item ID | `minecraft:milk_bucket` |
| `EAT` | Manger un item | Item ID | `minecraft:bread` |
| `USE` | Utiliser un item | Item ID | `minecraft:ender_pearl` |
| `OTHER` | Autre action | Item ID | ... |
| `CUSTOM` | Action personnalisée | Custom String | `my_custom_action` |

### Niveaux min/max

- **minLevel = -1**: Pas de niveau minimum (disponible dès le niveau 1)
- **minLevel = 10**: Nécessite le niveau 10 ou plus
- **maxLevel = -1**: Pas de niveau maximum (disponible jusqu'au niveau 100)
- **maxLevel = 50**: Disponible jusqu'au niveau 50 (après, ne donne plus d'XP)

### Exemples

```json
{
  "actionType": "BREAK",
  "targetId": "minecraft:stone",
  "minLevel": -1,
  "maxLevel": 10,
  "xpEarned": 5,
  "iconItem": "minecraft:stone"
}
```
> Casser de la pierre donne 5 XP, mais seulement jusqu'au niveau 10

```json
{
  "actionType": "KILL",
  "targetId": "minecraft:ender_dragon",
  "minLevel": 75,
  "maxLevel": -1,
  "xpEarned": 10000,
  "iconItem": "minecraft:dragon_egg"
}
```
> Tuer l'Ender Dragon donne 10000 XP, mais nécessite le niveau 75

---

## Unlocking

### Structure d'une entrée

```json
{
  "type": "ITEM",
  "targetId": "minecraft:diamond_pickaxe",
  "level": 15,
  "displayName": "Diamond Pickaxe",
  "description": "Unlock diamond tier mining tools"
}
```

### Champs

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| `type` | String | Type de débloquage (voir ci-dessous) | ✅ |
| `targetId` | String | ID de l'élément débloqué | ✅ |
| `level` | Int | Niveau requis pour débloquer | ✅ |
| `displayName` | String | Nom affiché dans le GUI | ❌ |
| `description` | String | Description affichée dans le GUI | ❌ |

### Types de débloquages (type)

| Type | Description | targetId attendu | Exemple |
|------|-------------|------------------|---------|
| `ITEM` | Débloquer un item | Item ID | `minecraft:netherite_pickaxe` |
| `BLOCK` | Débloquer un bloc | Block ID | `minecraft:enchanting_table` |
| `DIMENSION` | Débloquer une dimension | Dimension ID | `minecraft:the_nether` |
| `CUSTOM` | Débloquage personnalisé | Custom String | `bonus_xp_multiplier` |

### Exemples

```json
{
  "type": "ITEM",
  "targetId": "minecraft:netherite_pickaxe",
  "level": 30,
  "displayName": "Netherite Pickaxe",
  "description": "Unlock the most powerful mining tool"
}
```
> Au niveau 30, le joueur peut utiliser/crafter des pioches en netherite

```json
{
  "type": "CUSTOM",
  "targetId": "miner_speed_boost",
  "level": 50,
  "displayName": "Speed Boost",
  "description": "+20% mining speed bonus"
}
```
> Au niveau 50, le joueur obtient un boost de vitesse de minage personnalisé

---

## Système de chargement

### Au démarrage du serveur

1. Le `JobsConfigManager` est initialisé
2. Pour chaque métier (MINER, LUMBERJACK, etc.):
   - Si le fichier JSON n'existe pas → crée une config par défaut
   - Si le fichier existe → charge la config depuis le fichier
3. Toutes les configs sont gardées en **mémoire** (RAM)
4. Les fichiers ne sont plus lus pendant le jeu

### Synchronisation client-serveur

1. Quand un joueur se connecte au serveur:
   - Le serveur envoie **toutes** les configs au client via paquet réseau
   - Le client stocke les configs en mémoire
2. Le client utilise ces configs pour:
   - Afficher les actions XP dans le GUI
   - Afficher les débloquages dans le GUI
   - Calculer les statistiques disponibles/verrouillées

### Rechargement

Pour appliquer des modifications de config:
1. **Modifier le fichier JSON** dans `config/erinium_faction/jobs/`
2. **Redémarrer le serveur** (ou utiliser une commande de reload si implémentée)
3. Les joueurs connectés recevront automatiquement les nouvelles configs

---

## Exemples de configurations complètes

### MINER (Mineur)

```json
{
  "jobType": "MINER",
  "xpEarning": [
    {"actionType": "BREAK", "targetId": "minecraft:stone", "minLevel": -1, "maxLevel": -1, "xpEarned": 5, "iconItem": "minecraft:stone"},
    {"actionType": "BREAK", "targetId": "minecraft:coal_ore", "minLevel": -1, "maxLevel": -1, "xpEarned": 15, "iconItem": "minecraft:coal_ore"},
    {"actionType": "BREAK", "targetId": "minecraft:iron_ore", "minLevel": 5, "maxLevel": -1, "xpEarned": 30, "iconItem": "minecraft:iron_ore"},
    {"actionType": "BREAK", "targetId": "minecraft:diamond_ore", "minLevel": 10, "maxLevel": -1, "xpEarned": 100, "iconItem": "minecraft:diamond_ore"}
  ],
  "unlocking": [
    {"type": "ITEM", "targetId": "minecraft:iron_pickaxe", "level": 5, "displayName": "Iron Pickaxe", "description": "Unlock iron tier tools"},
    {"type": "ITEM", "targetId": "minecraft:diamond_pickaxe", "level": 15, "displayName": "Diamond Pickaxe", "description": "Unlock diamond tier tools"}
  ]
}
```

### HUNTER (Chasseur)

```json
{
  "jobType": "HUNTER",
  "xpEarning": [
    {"actionType": "KILL", "targetId": "minecraft:zombie", "minLevel": -1, "maxLevel": -1, "xpEarned": 20, "iconItem": "minecraft:rotten_flesh"},
    {"actionType": "KILL", "targetId": "minecraft:skeleton", "minLevel": -1, "maxLevel": -1, "xpEarned": 25, "iconItem": "minecraft:bone"},
    {"actionType": "KILL", "targetId": "minecraft:creeper", "minLevel": 5, "maxLevel": -1, "xpEarned": 30, "iconItem": "minecraft:gunpowder"},
    {"actionType": "KILL", "targetId": "minecraft:ender_dragon", "minLevel": 75, "maxLevel": -1, "xpEarned": 10000, "iconItem": "minecraft:dragon_egg"}
  ],
  "unlocking": [
    {"type": "ITEM", "targetId": "minecraft:bow", "level": 5, "displayName": "Bow", "description": "Unlock ranged weapons"},
    {"type": "ITEM", "targetId": "minecraft:crossbow", "level": 15, "displayName": "Crossbow", "description": "Unlock advanced ranged weapons"}
  ]
}
```

---

## API pour les développeurs

### Obtenir une configuration

```java
// Côté serveur
JobConfig config = JobsConfigManager.getConfig(JobType.MINER);

// Côté client
JobConfig config = JobsClientConfig.getConfig(JobType.MINER);
```

### Trouver une entrée XP

```java
JobConfig config = JobsConfigManager.getConfig(JobType.MINER);
XpEarningEntry entry = config.findXpEntry(ActionType.BREAK, "minecraft:diamond_ore", playerLevel);

if (entry != null) {
    int xp = entry.getXpEarned();
    // Donner l'XP au joueur
}
```

### Vérifier un débloquage

```java
JobConfig config = JobsConfigManager.getConfig(JobType.MINER);
boolean unlocked = config.isUnlocked(UnlockType.ITEM, "minecraft:diamond_pickaxe", playerLevel);

if (!unlocked) {
    player.sendSystemMessage(Component.literal("Vous devez atteindre le niveau requis!"));
}
```

### Recharger les configurations

```java
JobsConfigManager.reload();

// Re-synchroniser avec tous les joueurs connectés
for (ServerPlayer player : server.getPlayerList().getPlayers()) {
    JobsPacketHandler.syncJobsConfig(player);
}
```

---

## Notes importantes

1. ✅ **Les configs sont côté serveur uniquement** - Le client reçoit une copie synchronisée
2. ✅ **Modification à chaud impossible** - Nécessite un redémarrage du serveur
3. ✅ **Validation automatique** - Les configs invalides utilisent des valeurs par défaut
4. ✅ **Format JSON strict** - Utiliser des validateurs JSON pour éviter les erreurs
5. ✅ **IDs Minecraft** - Utiliser le format `namespace:path` (ex: `minecraft:diamond_ore`)
6. ✅ **Actions personnalisées** - Utiliser `CUSTOM` pour des mécaniques custom

---

## Dépannage

### Le fichier JSON ne se charge pas
- Vérifier la syntaxe JSON (virgules, guillemets, accolades)
- Consulter les logs du serveur pour voir l'erreur
- Une config par défaut sera utilisée en cas d'erreur

### Les modifications ne s'appliquent pas
- Vérifier que le serveur a été redémarré
- Vérifier que le fichier JSON est dans le bon dossier
- S'assurer qu'il n'y a pas de fichier dupliqué

### Le client ne voit pas les bonnes configs
- Le client reçoit les configs au login
- Se reconnecter au serveur pour forcer une resynchronisation
