# Système de Combat Logging

## Description
Système anti-abus qui empêche les joueurs de se déconnecter pendant un combat PvP pour éviter la mort. Cette pratique déloyale est très courante en PvP et ce système la combat efficacement.

## Fonctionnalités

### Combat Tag
- **Activation** : Dès qu'un joueur inflige ou reçoit des dégâts PvP
- **Durée** :
  - 15 secondes en territoire neutre
  - 10 secondes dans son propre territoire de faction
  - Le timer se réinitialise à chaque coup
- **Notification** : Barre d'action affichant "⚔ EN COMBAT - Xs"

### Pénalité de déconnexion
Lorsqu'un joueur se déconnecte pendant le combat tag :
- **Le joueur meurt automatiquement** (`/kill`)
- Tous ses items sont droppés à sa position
- **Aucune exception** pour timeout/crash - toute déconnexion en combat = mort

### Blocage de commandes
Pendant le combat tag, certaines commandes sont bloquées :
- **Par défaut** : kill, suicide, tp, teleport, home, spawn, back, warp, tpa, tpaccept
- **Configurable** : Liste modifiable dans la configuration
- **Message** : "✖ Cette commande est bloquée en combat ! (Xs restantes)"

### Exceptions

#### Territoire de faction
- Dans son propre territoire : durée réduite à 10s
- Hors territoire : durée normale de 15s

#### PvE
- Les combats contre les mobs (PvE) ne déclenchent PAS de combat tag
- Seuls les combats joueur contre joueur (PvP) activent le système

## Configuration

Le système est configurable via `erinium_faction-combatlog.toml` :

```toml
[Combat Logging System]
    # Activer le système de combat logging
    enableCombatLog = true

    # Durée du combat tag en secondes (territoire neutre)
    combatTagDuration = 15

    # Durée du combat tag en secondes (dans son territoire)
    combatTagDurationInTerritory = 10

    # Activer le combat tag sur les combats PvE (contre les mobs)
    tagOnPvE = false

    # Afficher le timer dans la barre d'action
    showActionBar = true

    # Liste des commandes bloquées pendant le combat (sans le /)
    # Exemple: kill, tp, home, spawn, back, warp, etc.
    blockedCommands = ["kill", "suicide", "tp", "teleport", "home", "spawn", "back", "warp", "tpa", "tpaccept"]
```

## Commandes

### `/combatlog status`
Affiche votre statut de combat actuel :
- Si vous êtes en combat ou non
- Temps restant avant la fin du tag
- Type de territoire (votre faction ou neutre)

### `/combatlog clear` (Admin)
Retire le combat tag d'un joueur (requiert permission niveau 2)

## Architecture

### Classes principales

#### `CombatTagData`
Stocke les données de combat tag pour chaque joueur :
- ID du joueur
- Temps de fin du tag
- Dernier attaquant
- Territoire de faction

#### `CombatLogManager`
Gestionnaire singleton central :
- Gestion des tags actifs
- Vérification du territoire
- Mise à jour des notifications
- Vérification des commandes bloquées
- Gestion de la mort en cas de déconnexion

#### `CombatLogEventHandler`
Gestionnaire d'événements :
- `LivingDamageEvent` : Détection PvP et tagging
- `PlayerLoggedOutEvent` : Gestion déconnexion (kill du joueur si tagué)
- `LevelTickEvent` : Mise à jour des timers
- `CommandEvent` : Blocage des commandes interdites

#### `CombatLogConfig`
Configuration du système via fichier TOML :
- Durées de combat tag
- Liste des commandes bloquées
- Options d'affichage

#### `CombatLogCommand`
Commandes joueur et admin

## Intégration

Le système est automatiquement intégré dans `EriniumFaction.java` :

```java
// Import
import fr.eriniumgroup.erinium_faction.features.combatlog.CombatLogEventHandler;
import fr.eriniumgroup.erinium_faction.features.combatlog.CombatLogCommand;

// Enregistrement des events
NeoForge.EVENT_BUS.register(CombatLogEventHandler.class);

// Enregistrement de la commande
CombatLogCommand.register(event.getDispatcher());
```

## Notes techniques

### Détection PvP vs PvE
Le système vérifie que `DamageSource.getEntity()` est une instance de `ServerPlayer` pour garantir que seuls les combats PvP sont tagués.

### Gestion du territoire
Utilise `FactionManager` pour déterminer si le joueur est dans son propre territoire, affectant la durée du tag.

### Blocage de commandes
- Utilise `CommandEvent` avec priorité `HIGHEST` pour intercepter les commandes
- Normalise les commandes (enlève `/` et extrait le nom)
- Compare avec la liste configurable des commandes bloquées

### Performance
- Utilisation de `ConcurrentHashMap` pour thread-safety
- Nettoyage automatique des tags expirés
- Mise à jour toutes les secondes (20 ticks) pour réduire la charge
