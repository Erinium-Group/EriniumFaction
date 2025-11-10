# Système de Kits

## Description
Système de kits ultra-simple et configurable permettant de créer autant de kits que souhaité via un fichier TOML. Chaque kit peut avoir des items customisés, des enchantements, et être restreint par rank.

## Fonctionnalités

### Configuration TOML Simple
- **Fichier** : `erinium_faction-kits.toml`
- **Format intuitif** et facile à lire
- **Nombre illimité** de kits
- **Hot-reload** : rechargez les kits avec un redémarrage du serveur

### Items Customisables
Chaque item d'un kit peut avoir :
- **itemId** : L'ID de l'item (ex: `minecraft:diamond_sword`)
- **count** : Nombre d'items (défaut: 1)
- **slot** : Emplacement d'armure (`HEAD`, `CHEST`, `LEGS`, `FEET`, `OFFHAND`)
- **enchants** : Enchantements avec niveaux (format: `{ench:level,ench:level}`)
- **name** : Nom custom avec formatage
- **lore** : Description multi-lignes (format: `[ligne1,ligne2,ligne3]`)

### Système de Permissions
- Intégration avec le système de **ranks** (`core.rank.EFRManager`)
- Kits sans rank = accessibles à tous
- Kits avec rank = nécessite le rank spécifié ou supérieur
- Comparaison automatique par priorité de rank

### Cooldowns
- Cooldown de 1 heure par défaut par kit
- Empêche l'abus du système
- Affichage du temps restant formaté (1h 30m, 45m 20s, etc.)

### Commande /kit
- **Liste tous les kits** disponibles pour le joueur
- **Interface cliquable** (clic sur un kit pour le recevoir)
- **Affichage du cooldown** pour chaque kit
- **Auto-complétion** des noms de kits
- **Messages formatés** avec couleurs et symboles

## Format de Configuration

### Structure d'un Kit

```toml
[[kits]]
id = "starter"
displayName = "Kit Débutant"
description = "Kit de base pour tous les joueurs"
requiredRank = ""  # Vide = accessible à tous
items = [
    "minecraft:iron_sword,1,enchants:{minecraft:sharpness:1}",
    "minecraft:iron_helmet,1,slot:HEAD",
    "minecraft:bread,16"
]
```

### Format d'un Item

```
"itemId,count,slot:SLOT,enchants:{ench:lvl},name:Name,lore:[Line1,Line2]"
```

**Paramètres** (tous optionnels sauf itemId) :
- `itemId` : ID de l'item (requis)
- `count` : Nombre (défaut: 1)
- `slot` : HEAD/CHEST/LEGS/FEET/OFFHAND
- `enchants` : `{enchant_id:level,enchant_id:level}`
- `name` : Nom custom (supporte les codes couleur §)
- `lore` : `[ligne1,ligne2,ligne3]`

### Exemples d'Items

```toml
# Épée simple avec enchantement
"minecraft:diamond_sword,1,enchants:{minecraft:sharpness:5}"

# Casque avec protection et slot
"minecraft:diamond_helmet,1,slot:HEAD,enchants:{minecraft:protection:4}"

# Pommes dorées en stack
"minecraft:golden_apple,16"

# Arc avec nom custom et enchantements multiples
"minecraft:bow,1,enchants:{minecraft:power:5,minecraft:infinity:1},name:§6Arc Légendaire"

# Item avec lore
"minecraft:nether_star,1,name:§d§lÉtoile Magique,lore:[§7Une étoile mystérieuse,§7Contient un pouvoir ancien]"
```

## Exemple de Configuration Complète

```toml
# ═══════════════════════════════════════════════════════════════════════
#                        SYSTÈME DE KITS
# ═══════════════════════════════════════════════════════════════════════

[[kits]]
id = "starter"
displayName = "Kit Débutant"
description = "Kit de base pour tous les joueurs"
requiredRank = ""
items = [
    "minecraft:iron_sword,1,enchants:{minecraft:sharpness:1}",
    "minecraft:iron_helmet,1,slot:HEAD",
    "minecraft:iron_chestplate,1,slot:CHEST",
    "minecraft:iron_leggings,1,slot:LEGS",
    "minecraft:iron_boots,1,slot:FEET",
    "minecraft:bread,16",
    "minecraft:torch,32"
]

[[kits]]
id = "vip"
displayName = "Kit VIP"
description = "Kit réservé aux VIP"
requiredRank = "vip"
items = [
    "minecraft:diamond_sword,1,enchants:{minecraft:sharpness:3,minecraft:unbreaking:2}",
    "minecraft:diamond_helmet,1,slot:HEAD,enchants:{minecraft:protection:3}",
    "minecraft:diamond_chestplate,1,slot:CHEST,enchants:{minecraft:protection:3}",
    "minecraft:diamond_leggings,1,slot:LEGS,enchants:{minecraft:protection:3}",
    "minecraft:diamond_boots,1,slot:FEET,enchants:{minecraft:protection:3}",
    "minecraft:golden_apple,8",
    "minecraft:ender_pearl,16"
]

[[kits]]
id = "pvp"
displayName = "Kit PvP"
description = "Kit de combat pour les guerriers"
requiredRank = "warrior"
items = [
    "minecraft:netherite_sword,1,enchants:{minecraft:sharpness:5,minecraft:fire_aspect:2,minecraft:unbreaking:3},name:§c§lÉpée du Guerrier",
    "minecraft:netherite_helmet,1,slot:HEAD,enchants:{minecraft:protection:4,minecraft:unbreaking:3}",
    "minecraft:netherite_chestplate,1,slot:CHEST,enchants:{minecraft:protection:4,minecraft:unbreaking:3}",
    "minecraft:netherite_leggings,1,slot:LEGS,enchants:{minecraft:protection:4,minecraft:unbreaking:3}",
    "minecraft:netherite_boots,1,slot:FEET,enchants:{minecraft:protection:4,minecraft:unbreaking:3}",
    "minecraft:bow,1,enchants:{minecraft:power:5,minecraft:infinity:1,minecraft:flame:1}",
    "minecraft:arrow,1",
    "minecraft:golden_apple,16",
    "minecraft:totem_of_undying,1,slot:OFFHAND"
]
```

## Utilisation

### Commande /kit
```
/kit                    # Liste tous les kits disponibles
/kit <nom>              # Reçoit le kit spécifié
```

### Interface Utilisateur

**Liste des kits** :
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
         KITS DISPONIBLES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

✔ Kit Débutant           (Cliquable - disponible)
✔ Kit VIP                (Cliquable - disponible)
✖ Kit PvP (Cooldown: 30m) (Grisé - en cooldown)

Utilisez /kit <nom> pour obtenir un kit
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**Réception d'un kit** :
```
✔ Kit reçu: Kit VIP
Kit réservé aux VIP
```

## Architecture

### Classes Principales

#### `KitItem`
Parse et crée les ItemStacks depuis la configuration :
- Parsing du format string complexe
- Gestion des enchantements via DataComponents
- Application des noms custom et lore
- Support des slots d'équipement

#### `Kit`
Représente un kit complet :
- ID, nom d'affichage, description
- Rank requis (optionnel)
- Liste d'items
- Méthode `give()` pour donner le kit à un joueur

#### `KitConfig`
Configuration TOML avec NeoForge ConfigSpec :
- `defineList` pour nombre illimité de kits
- Helper `createDefaultKit()` pour kits par défaut
- Méthode `parseKits()` pour charger depuis la config
- Documentation complète dans les commentaires

#### `KitManager`
Gestionnaire singleton central :
- Chargement et enregistrement des kits
- Vérification des permissions via `EFRManager`
- Système de cooldowns par joueur/kit
- Méthode `getAvailableKits()` filtrée par joueur
- Map thread-safe (`ConcurrentHashMap`)

#### `KitCommand`
Commande Brigadier avec :
- Auto-complétion contextuelle (kits disponibles pour le joueur)
- Interface cliquable avec hover events
- Formatage des temps de cooldown
- Gestion des erreurs (kit inconnu, pas de permission, cooldown)

## Intégration

Le système est automatiquement intégré dans `EriniumFaction.java` :

```java
// Configuration
modContainer.registerConfig(ModConfig.Type.SERVER, KitConfig.SPEC, "erinium_faction-kits.toml");

// Chargement au démarrage du serveur
KitManager.getInstance().loadKits();

// Enregistrement de la commande
KitCommand.register(event.getDispatcher());
```

## API pour Développeurs

### Enregistrer un Kit Manuellement

```java
List<KitItem> items = Arrays.asList(
    KitItem.parse("minecraft:diamond_sword,1,enchants:{minecraft:sharpness:5}"),
    KitItem.parse("minecraft:diamond_helmet,1,slot:HEAD")
);

Kit customKit = new Kit("custom", "Kit Custom", "Description", "admin", items);
KitManager.getInstance().registerKit(customKit);
```

### Vérifier les Permissions

```java
ServerPlayer player = ...;
Kit kit = KitManager.getInstance().getKit("vip").orElse(null);

if (kit != null && KitManager.getInstance().canUseKit(player, kit)) {
    // Le joueur peut utiliser le kit
}
```

### Donner un Kit Programmatiquement

```java
ServerPlayer player = ...;
ServerLevel level = player.serverLevel();

boolean success = KitManager.getInstance().giveKit(player, "starter", level);
```

## Notes Techniques

### Enchantements
- Utilise `DataComponents.ENCHANTMENTS` (Minecraft 1.21+)
- Support des registres d'enchantements via `RegistryAccess`
- Format: `minecraft:enchant_id` ou namespace custom

### Cooldowns
- Stockés en mémoire (`ConcurrentHashMap`)
- Perdus au redémarrage du serveur (volontaire pour éviter abus)
- Format du temps: `System.currentTimeMillis()`

### Thread-Safety
- Utilisation de `ConcurrentHashMap` pour tous les Maps
- Safe pour accès multi-threaded

### Performance
- Kits chargés une seule fois au démarrage
- Pas de parsing à chaque utilisation
- Items créés à la demande (pas de cache)

## TODO / Améliorations Futures

- [ ] Cooldowns persistants (sauvegarde dans un fichier)
- [ ] Cooldowns configurables par kit
- [ ] GUI visuel pour voir les items du kit
- [ ] Système de preview (voir le kit avant de le prendre)
- [ ] Limites d'utilisations (X fois par jour/semaine)
- [ ] Kits à usage unique vs illimité
- [ ] Support des potions et effets
- [ ] Support des items NBT custom complexes
- [ ] Statistiques d'utilisation des kits
