# Erinium Faction - Mémoire Projet

## Vue d'ensemble
Mod Minecraft NeoForge 1.21+ pour un système de factions PvP avec mécaniques avancées.

**Auteurs**: Erinium Group
**Version NeoForge**: 21.1.73
**Langage**: Java + Gradle
**ID Mod**: `erinium_faction`

---

## Architecture du Mod

### Structure des packages
```
fr.eriniumgroup.erinium_faction/
├── client/              # Code client-only (renderers, GUI, overlays)
│   ├── model/          # Modèles 3D d'entités
│   ├── renderer/       # Renderers custom
│   ├── overlay/        # HUD overlays
│   └── gui/            # Écrans GUI
├── common/             # Code partagé client/serveur
│   ├── block/          # Blocs custom
│   ├── entity/         # Entités custom
│   ├── item/           # Items custom
│   ├── config/         # Configurations
│   └── network/        # Packets réseau
├── core/               # Systèmes core (factions, power, ranks)
├── features/           # Features modulaires
├── init/               # Registres DeferredRegister
└── events/             # Event handlers
```

### Systèmes d'enregistrement (init/)
- **EFBlocks**: Blocs (minerais, machines, coffres)
- **EFBlockEntities**: Block entities (batteries, compresseurs)
- **EFItems**: Items (matériaux, outils, armures, composants)
- **EFEntities**: Entités custom (Rocket, Combat Log)
- **EFCreativeTabs**: Onglet créatif
- **EFMenus**: Menus GUI
- **EFRecipes**: Types de recettes custom

---

## Entités Implémentées

### 🚀 Rocket Entity (Fusée SpaceX-style)
**Status**: ✅ Implémenté et compilé (2025-11-21)
**Last Update**: Fix montage + pickup (2025-11-21)

**Fichiers**:
- Entity: `common/entity/RocketEntity.java`
- Model: `client/model/RocketModel.java` (Blockbench, 512x512)
- Renderer: `client/renderer/RocketRenderer.java`
- Item: `common/item/RocketSpawnItem.java`
- Texture: `assets/erinium_faction/textures/entity/rocket.png`

**Caractéristiques**:
- ✅ **Montable** : Clic droit pour monter (position siège: [0, 2.9, 0])
- ✅ **Pickup** : Shift + Clic droit pour ramasser (retourne le spawn egg)
  - 🔒 Sécurité : impossible de ramasser si quelqu'un est assis dedans
- Hitbox: 2.0F × 6.0F
- Système de dégâts avec destroy
- Physique basique (gravité, friction)
- Drop le spawn egg à la destruction
- Inventaire du joueur géré (creative/survival)
- Max 1 passager

**Modèle 3D**: Importé depuis Blockbench
- Composants: hexagones, bone structures, jambes d'atterrissage
- Texture mapping 512×512
- Parties: hexadecagon1-5, bone1-3, legs

**Notes techniques**:
- NeoForge 1.21+ utilise `renderToBuffer(PoseStack, VertexConsumer, int, int, int)` avec ARGB color
- Spawn egg custom car l'entité n'hérite pas de `Mob`
- Enregistré dans `EFClientSetup` pour le layer et renderer
- **isPickable()** = true pour permettre l'interaction (targeting du crosshair)
- **canBeCollidedWith()** = true pour la détection des clics
- **canAddPassenger()** limite à 1 passager maximum

---

## Matériaux et Items

### Minerais de base
- **Silver** (Argent): raw, ingot, scrap, wire, plate
- **Titanium**: raw, ingot, wire, plate
- **Erinium** (rare): ingot uniquement, fire resistant

### Composants SpaceX (Starship-inspired)
**Status**: ✅ Tous enregistrés dans EFItems

| Item | Description | Rareté |
|------|-------------|--------|
| crew_module | Module d'équipage | Uncommon |
| docking_port | Port d'amarrage | Uncommon |
| erinium_core | Cœur d'Erinium (énergie) | Rare |
| flight_computer | Ordinateur de vol | Uncommon |
| fuel_tank | Réservoir de carburant | Common |
| grid_fin | Ailette de grille (stabilisation) | Common |
| heat_shield_tiles | Tuiles bouclier thermique | Uncommon |
| landing_leg | Pied d'atterrissage | Common |
| raptor_engine | Moteur Raptor | Rare |
| rcs_thruster | Propulseur RCS (16x16) | Common |
| steel_hull_panel | Panneau de coque en acier | Common |

**Textures**: Toutes présentes dans `assets/erinium_faction/textures/item/`
**Models JSON**: Tous dans `assets/erinium_faction/models/item/`

### Armure et Outils Erinium
- Armor set complet (helmet, chestplate, leggings, boots)
- Toolset complet (sword, pickaxe, axe, shovel, hoe)
- **Death Scythe**: Arme épique (damage: 7.0, speed: -3.2)

---

## Blocs et Machines

### Minerais
- Erinium Ore / Deepslate Erinium Ore
- Silver Ore / Deepslate Silver Ore
- Titanium Ore / Deepslate Titanium Ore

### Machines
- **Titanium Compressor**: Compression de matériaux
- **Titanium Battery Tier 1**: Stockage énergie (capacity dans BlockEntity)
- **Titanium Creative Battery**: Énergie infinie

### Stockage
- **Erinium Chest**: Coffre custom avec renderer 3D (`EriniumChestRenderer`)

---

## Systèmes Core

### Factions
- Création/dissolution de factions
- Système de claims (chunks)
- Alliances et ennemis
- Banque de faction
- Ranks personnalisables avec permissions

### Combat & PvP
- **Combat Log**: Entity spawnable lors de la déconnexion en combat
- Protection friendly fire
- Protection ally damage
- Zones (Warzone, Safezone, Wilderness)

### Économie
- Intégration économie (players.dat attachments)
- Transactions bancaires faction
- Bounty system (primes sur joueurs)

### Progression
- **Player Level System**: Niveaux joueurs avec config
- **Jobs System**: Métiers avec progression
- **Top Luck**: Système de chance pour le minage

### Utilitaires
- **RTP**: Random teleport avec cooldown
- **Homes**: Système de /home personnel
- **Kits**: Kits configurables
- **Vanish**: Mode invisible admin

---

## Configuration

### Fichiers de config (config/)
- `erinium_faction-server.toml`: Config serveur principale
- `erinium_faction-client.toml`: Config client (HUD, minimap)
- `erinium_faction-player_level.toml`: Système de niveaux
- `erinium_faction-combatlog.toml`: Combat logging
- `erinium_faction-kits.toml`: Kits serveur
- `erinium_faction-bounty.toml`: Système de primes

### Permissions Claude Code
**Fichier**: `.claude/settings.json`

Permissions actuelles:
```json
{
  "permissions": {
    "allow": [
      "Bash(*)",
      "Read(*)",
      "Write(*)",
      "Edit(*)",
      "Glob(*)",
      "Grep(*)"
    ],
    "deny": [],
    "ask": []
  }
}
```

**Notes**:
- `settings.local.json` est auto-généré (outputStyle)
- À ajouter au `.gitignore` si pas déjà fait

---

## Client-Side Features

### HUD & Overlays
- **Minimap**: Overlay configurable avec zoom
- **Waypoints**: Système de marqueurs 3D
- **Health Bar**: Barre de vie custom
- **Faction Title**: Affichage titre faction
- **Job Toast**: Notifications métiers

### GUI Screens
- **Faction Menu**: Interface complète (overview, members, bank, etc.)
- **Map Screen**: Carte des claims avec bouton HUD
- **Bounty GUI**: Interface système de primes
- **TopLuck Screen**: Classement chance

### Renderers Custom
- **Custom Tablist**: Liste joueurs avec factions
- **Custom Nameplate**: Noms avec ranks/factions
- **Custom Cape**: Capes de faction
- **Custom Banner**: Bannières de faction

---

## Anti-Cheat & Protection

### Anti-Xray
- Manager actif: `AntiXrayManager`
- Obfuscation des minerais
- Config par type de minerai

### Protections
- **Claim Protection**: Permissions par chunk
- **PvP Protection**: Friendly fire, ally damage
- **Block HP**: Système de durabilité des blocs

### Audit
- Log des actions importantes
- Rotation automatique des logs
- Compression configurée

---

## Traductions

**Langues supportées**:
- 🇺🇸 English (`en_us.json`)
- 🇫🇷 Français (`fr_fr.json`)

**Dernière mise à jour**: Ajout des traductions Rocket (2025-11-21)

Clés récentes:
```json
"item.erinium_faction.rocket_spawn_egg": "Rocket Spawn Egg" / "Œuf d'Apparition de Fusée"
"entity.erinium_faction.rocket": "Rocket" / "Fusée"
```

---

## Build & Compilation

### Commandes Gradle
```bash
./gradlew build          # Build complet
./gradlew runClient      # Lancer client test
./gradlew runServer      # Lancer serveur test
./gradlew jar            # Créer le JAR uniquement
```

### Dernière compilation
**Date**: 2025-11-21
**Statut**: ✅ BUILD SUCCESSFUL
**Warnings**: Gradle 10.0 deprecation (property assignment syntax)

### Dépendances
- NeoForge 21.1.73
- Mekanism (generators, tools, additions)
- JEI (Just Enough Items)
- Configured
- Catalogue

---

## Fichiers Importants à Suivre

### À implémenter prochainement
**Dossier**: `informations/to import/`
- Fichiers rocket déjà traités ✅
- Vérifier s'il reste d'autres assets à importer

### Textures en attente
**Dossier**: `informations/items and texture/`
- Composants SpaceX: tous les fichiers PNG présents
- Certains fichiers source (16x16) à vérifier si nécessaire

### Modèles 3D
- `erinium_chest_item.bbmodel` (Blockbench)
- Modèle rocket déjà intégré ✅

---

## Conventions du Projet

### Naming
- Classes: PascalCase (`RocketEntity`, `EriniumChestRenderer`)
- Packages: snake_case (`erinium_faction`)
- Registry names: snake_case (`rocket_spawn_egg`)
- Fichiers assets: snake_case

### Colors & Formatting
Le mod utilise des color codes Minecraft:
- `§a` = Vert (success)
- `§c` = Rouge (error)
- `§e` = Jaune (warning/info)
- `§b` = Cyan (emphasis)
- `§6` = Or (titles)

### Structure des Messages
Format standard: `§6[§bErinium§6/§dFeature§6] §7Message`

---

## TODOs & Roadmap

### ✅ Complété Récemment
- [x] Implémentation entité Rocket complète
- [x] Système de spawn egg custom
- [x] Composants SpaceX (11 items)
- [x] Death Scythe (arme épique)

### 🔄 En Cours
- [ ] Craft recipes pour composants SpaceX
- [ ] Système d'assemblage de fusée (multiblock?)
- [ ] Physique avancée pour la fusée (propulsion)

### 📋 Backlog
- [ ] Dimension spatiale custom
- [ ] Station spatiale
- [ ] Système de carburant pour fusées
- [ ] Décollage et atterrissage animés

---

## Notes de Développement

### Changements API NeoForge 1.21+
1. **ResourceLocation**: Utiliser `ResourceLocation.fromNamespaceAndPath()` au lieu du constructeur
2. **renderToBuffer**: Signature changée en `(PoseStack, VertexConsumer, int, int, int)` avec ARGB
3. **DeferredRegister**: Utiliser `Registries.*` pour les types
4. **Entity rendering**: Séparation stricte client/serveur via `@EventBusSubscriber(Dist.CLIENT)`

### Pièges Courants
- ❌ Ne pas utiliser `DeferredSpawnEggItem` pour entités non-Mob
- ⚠️ Toujours vérifier `level.isClientSide` avant spawn d'entités
- ⚠️ Les renderers doivent être enregistrés côté MOD bus, pas FORGE bus
- ✅ Utiliser `EFC.MOD_ID` pour la cohérence (défini dans EFC.java)

---

## Liens Utiles

**Repository Git**: (à ajouter)
**Discord**: (à ajouter)
**Wiki**: (à ajouter)

---

**Dernière mise à jour**: 2025-11-21
**Maintenu par**: Claude (Anthropic)
