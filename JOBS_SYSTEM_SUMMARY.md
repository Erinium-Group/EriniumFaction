# Système de Métiers - Résumé Complet

## Vue d'ensemble

Le système de métiers est maintenant **entièrement fonctionnel** avec:
- ✅ Stockage persistant des données (NBT)
- ✅ Synchronisation client-serveur automatique
- ✅ 4 écrans GUI complets
- ✅ 6 métiers différents avec thèmes colorés
- ✅ 93+ assets PNG

## Architecture

### Données (`jobs/` package)

**JobsData.java** - Stockage des progressions
- Map de tous les métiers → niveau + XP
- Formule XP: `100 * niveau * 1.2`
- Serialization NBT automatique
- Méthode `addExperience()` avec level up automatique

**JobsDataAttachment.java** - Enregistrement NeoForge
- Enregistré dans `EriniumFaction.java` ligne 71
- Persiste dans `players.dat`

**JobType.java** - Enumération des 6 métiers
- MINER (gold), LUMBERJACK (brown), HUNTER (red)
- FISHER (blue), FARMER (green), WIZARD (purple)
- Chaque métier a: nom, couleur, emoji, description

### API (`JobsManager.java`)

```java
// SERVEUR - Ajouter XP avec level up automatique
int levels = JobsManager.addJobExperience(serverPlayer, JobType.MINER, 100);
if (levels > 0) {
    player.sendSystemMessage(Component.literal("Level up!"));
}

// SERVEUR - Modifier niveau/XP
JobsManager.setJobLevel(serverPlayer, JobType.MINER, 10);
JobsManager.setJobExperience(serverPlayer, JobType.MINER, 500);

// SERVEUR - Lire données
int level = JobsManager.getJobLevel(serverPlayer, JobType.MINER);
int xp = JobsManager.getJobExperience(serverPlayer, JobType.MINER);

// CLIENT - Lire données synchronisées
JobsData clientData = JobsManager.getClientJobsData();
int level = clientData.getLevel(JobType.MINER);

// UNIVERSEL - Auto-détecte serveur/client
JobsData data = JobsManager.getJobsDataUniversal(player);
```

### Réseau (`jobs/network/`)

**Synchronisation automatique:**
- Login du joueur → sync
- Changement de dimension → sync
- Respawn → sync
- Après toute modification via JobsManager → sync

**Paquets:**
- `SyncJobsDataPacket` - Serveur → Client
- Géré par `JobsPacketHandler`
- Enregistré dans `PacketHandler.java` ligne 74
- Stocké côté client dans `JobsClientData`

### GUI (`jobs/gui/`)

**4 écrans complets:**

1. **JobsMenuScreen** - Menu principal
   - Liste scrollable de tous les métiers
   - Progression (niveau + XP) en temps réel
   - Clic sur un métier → ouvre JobDetailScreen
   - **Utilise les vraies données** via `JobsManager.getClientJobsData()`

2. **JobDetailScreen** - Détails d'un métier
   - Niveau, XP, barre de progression
   - 2 boutons: "How to gain XP" et "Unlocked Features"
   - Recent unlocks/locked items
   - **Utilise les vraies données** passées en paramètre

3. **JobHowToXPScreen** - Sources d'XP
   - Liste des actions qui donnent de l'XP
   - Affiche le niveau actuel
   - **Utilise les vraies données** pour le niveau

4. **JobUnlockedFeaturesScreen** - Features débloquées
   - Liste des fonctionnalités par niveau
   - Compteur unlocked/locked
   - **Utilise les vraies données** pour le niveau

**Accès GUI:**
- Bouton "Jobs" dans l'inventaire (à côté de "Stats")
- `minecraft.setScreen(new JobsMenuScreen())`

### Événements (`JobsEvents.java`)

Synchronisation automatique lors de:
```java
@SubscribeEvent
public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)

@SubscribeEvent
public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)

@SubscribeEvent
public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
```

## Corrections appliquées

### Bug fixes (session précédente):
1. ✅ Scroll list utilise 100% de la hauteur disponible
2. ✅ Textures dynamiques pour tous les métiers (plus de rose)
3. ✅ Items visibles partiellement lors du scroll
4. ✅ Drag vs click détection (threshold 3px)
5. ✅ XP text ne déborde plus
6. ✅ Scissor correctement restauré
7. ✅ Visual glitches en haut/bas éliminés
8. ✅ Scrollbar ne déborde plus

### Nouvelles fonctionnalités (cette session):
1. ✅ Système de données persistant (NBT)
2. ✅ Attachment NeoForge enregistré
3. ✅ Synchronisation réseau complète
4. ✅ API serveur/client fonctionnelle
5. ✅ Événements de sync automatiques
6. ✅ GUI utilisent les vraies données

## État actuel

**Système 100% fonctionnel:**
- ✅ Données sauvegardées dans players.dat
- ✅ Synchronisation serveur ↔ client
- ✅ GUI affichent les vraies données
- ✅ API prête pour l'intégration gameplay
- ✅ Tous les assets en place (93 PNG)

**Prochaines étapes (gameplay):**
- Implémenter les événements qui donnent de l'XP (minage, coupe, chasse, etc.)
- Définir les fonctionnalités débloquées par niveau
- Ajouter des commandes admin (`/jobs set`, `/jobs add`, etc.)
- Créer un système de récompenses
- Implémenter les bonus de métier (vitesse, drop rate, etc.)

## Exemple d'intégration

```java
// Dans un event handler - Donner XP quand on mine
@SubscribeEvent
public static void onBlockBreak(BlockEvent.BreakEvent event) {
    if (event.getPlayer() instanceof ServerPlayer player) {
        Block block = event.getState().getBlock();

        // Exemple: diamant donne 50 XP Miner
        if (block == Blocks.DIAMOND_ORE) {
            int levels = JobsManager.addJobExperience(player, JobType.MINER, 50);

            if (levels > 0) {
                player.sendSystemMessage(
                    Component.literal("Miner Level Up! Now level " +
                        JobsManager.getJobLevel(player, JobType.MINER))
                        .withStyle(ChatFormatting.GOLD)
                );
            }
        }
    }
}

// Vérifier niveau requis pour une action
public boolean canCraftItem(ServerPlayer player, Item item) {
    int minerLevel = JobsManager.getJobLevel(player, JobType.MINER);

    if (item == Items.NETHERITE_PICKAXE) {
        return minerLevel >= 30; // Nécessite niveau 30
    }
    return true;
}
```

## Fichiers créés

### Package `jobs/`:
- `JobsData.java` - Classe de données
- `JobsDataAttachment.java` - Enregistrement attachment
- `JobsManager.java` - API principale
- `JobsEvents.java` - Event handlers
- `JobType.java` - Enum des métiers (déjà existant)
- `JobData.java` - DTO GUI (déjà existant)
- `README.md` - Documentation API

### Package `jobs/network/`:
- `SyncJobsDataPacket.java` - Paquet de synchronisation
- `JobsPacketHandler.java` - Handler de paquets
- `JobsClientData.java` - Cache client

### Package `jobs/gui/`:
- `JobsMenuScreen.java` - Menu principal (modifié)
- `JobDetailScreen.java` - Détails métier
- `JobHowToXPScreen.java` - Sources XP
- `JobUnlockedFeaturesScreen.java` - Features

### Modifications:
- `EriniumFaction.java` - Enregistrement attachment (ligne 71)
- `PacketHandler.java` - Enregistrement paquet (ligne 74)
- `JobsMenuScreen.java` - Utilise vraies données (lignes 50-65)

## Documentation complète

Voir `src/main/java/fr/eriniumgroup/erinium_faction/jobs/README.md` pour:
- Exemples d'utilisation détaillés
- Architecture complète
- Guide d'intégration
- Référence API
