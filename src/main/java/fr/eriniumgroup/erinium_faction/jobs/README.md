# Système de Métiers (Jobs System)

Ce package contient l'implémentation complète du système de métiers pour Erinium Faction.

## Architecture

### Données (`JobsData.java`)
- Stocke la progression de tous les métiers d'un joueur
- Utilise le système d'attachments NeoForge pour persister les données
- Stocke niveau + XP pour chaque métier (JobType)
- **Niveaux:** 1 à 100 (MAX_LEVEL = 100)
- **Formule XP:** `(1000 + 178.853 * LEVEL^1.5) * (LEVEL^1.005)`
- Progression exponentielle modérée avec ~40M XP total pour atteindre le niveau 100

### Gestionnaire (`JobsManager.java`)
Classe principale pour interagir avec le système:

#### Méthodes serveur (ServerPlayer required):
```java
// Obtenir les données
JobsData data = JobsManager.getJobsData(serverPlayer);

// Obtenir niveau/XP d'un métier
int level = JobsManager.getJobLevel(serverPlayer, JobType.MINER);
int xp = JobsManager.getJobExperience(serverPlayer, JobType.MINER);

// Modifier niveau/XP
JobsManager.setJobLevel(serverPlayer, JobType.MINER, 5);
JobsManager.setJobExperience(serverPlayer, JobType.MINER, 250);

// Ajouter de l'XP (gère automatiquement les level ups)
int levelsGained = JobsManager.addJobExperience(serverPlayer, JobType.MINER, 100);
if (levelsGained > 0) {
    player.sendSystemMessage(Component.literal("Level up!"));
}

// Synchroniser manuellement avec le client
JobsManager.syncPlayer(serverPlayer);
```

#### Méthodes client:
```java
// Obtenir les données synchronisées depuis le serveur
JobsData clientData = JobsManager.getClientJobsData();
int level = clientData.getLevel(JobType.MINER);
```

#### Méthode universelle (auto-détecte serveur/client):
```java
JobsData data = JobsManager.getJobsDataUniversal(player);
```

## Réseau

### Synchronisation automatique
Les données sont automatiquement synchronisées dans ces cas:
- Connexion du joueur (login)
- Changement de dimension
- Respawn
- Après toute modification via JobsManager

### Paquet réseau
- `SyncJobsDataPacket`: Serveur → Client pour synchroniser les données
- Géré par `JobsPacketHandler`
- Stocké côté client dans `JobsClientData`

## GUI

### Écrans disponibles:
1. **JobsMenuScreen**: Liste de tous les métiers avec progression
2. **JobDetailScreen**: Détails d'un métier spécifique
3. **JobHowToXPScreen**: Comment gagner de l'XP
4. **JobUnlockedFeaturesScreen**: Fonctionnalités débloquées par niveau

### Utilisation:
```java
// Ouvrir le menu depuis le client
minecraft.setScreen(new JobsMenuScreen());
```

## Types de métiers (JobType.java)

6 métiers disponibles:
- **MINER** (gold) - Extraction de ressources
- **LUMBERJACK** (brown) - Coupe de bois
- **HUNTER** (red) - Chasse de monstres
- **FISHER** (blue) - Pêche
- **FARMER** (green) - Agriculture
- **WIZARD** (purple) - Magie et enchantements

Chaque métier a:
- Nom d'affichage
- Couleur thématique
- Emoji
- Description

## Exemple d'intégration

### Donner de l'XP quand un joueur mine un bloc:
```java
@SubscribeEvent
public static void onBlockBreak(BlockEvent.BreakEvent event) {
    if (event.getPlayer() instanceof ServerPlayer player) {
        // Donner 10 XP au métier Miner
        int levels = JobsManager.addJobExperience(player, JobType.MINER, 10);

        if (levels > 0) {
            player.sendSystemMessage(
                Component.literal("Miner level up! Now level ")
                    .append(String.valueOf(JobsManager.getJobLevel(player, JobType.MINER)))
                    .withStyle(ChatFormatting.GOLD)
            );
        }
    }
}
```

### Vérifier le niveau requis:
```java
public boolean canUseTool(ServerPlayer player) {
    int minerLevel = JobsManager.getJobLevel(player, JobType.MINER);
    return minerLevel >= 10; // Nécessite niveau 10 Miner
}
```

## Fichiers principaux

- `JobsData.java`: Classe de données avec serialization NBT
- `JobsDataAttachment.java`: Enregistrement de l'attachment
- `JobsManager.java`: API principale
- `JobType.java`: Enumération des métiers
- `network/`: Paquets réseau et synchronisation
- `gui/`: Interfaces graphiques
- `JobsEvents.java`: Synchronisation automatique
