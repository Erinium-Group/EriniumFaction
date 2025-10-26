# Récapitulatif des mises à jour FactionSnapshot

Ce document liste toutes les modifications apportées aux pages du GUI de faction pour utiliser les données `FactionSnapshot`, ainsi que ce qui reste à implémenter.

## ✅ Pages mises à jour avec FactionSnapshot

### 1. **OverviewPage** ✅ COMPLET
**Données utilisées:**
- `displayName` - Nom de la faction
- `level` - Niveau de la faction
- `mode` - Mode de recrutement
- `membersCount`, `maxPlayers` - Membres
- `claims`, `maxClaims` - Territoires
- `warpsCount`, `maxWarps` - Téléportations
- `bank` - Banque
- `description` - Description

**Modifications:**
- Stats cards affichent members, claims et level
- Info list affiche toutes les données disponibles
- Plus de placeholders

---

### 2. **LevelPage** ✅ COMPLET
**Données utilisées:**
- `level` - Niveau actuel
- `xp`, `xpRequired` - Expérience
- `maxClaims` - Claims max
- `maxPlayers` - Membres max

**Modifications:**
- Badge de level dynamique
- Barre XP avec pourcentage calculé
- Max claims et max members affichés

---

### 3. **MembersPage** ✅ PARTIEL
**Données utilisées:**
- `membersCount` - Nombre total de membres
- `memberNames` - Map<UUID, String> des noms
- `membersRank` - Map<UUID, String> des rangs

**Modifications:**
- Liste dynamique des membres avec leurs noms et rangs
- Header affiche le nombre total de membres

**⚠️ Limitations (données non disponibles dans FactionSnapshot):**
- `power` de chaque membre → Fixé à 100 par défaut
- `online` status de chaque membre → Fixé à false par défaut

**🔧 À implémenter:**
Pour avoir le power et le status online de chaque membre, il faudrait :
1. Étendre `FactionSnapshot` avec `Map<UUID, Integer> memberPower`
2. Étendre `FactionSnapshot` avec `Set<UUID> onlineMembers`
3. Mettre à jour `FactionSnapshot.of()` pour calculer ces données

---

### 4. **TerritoryPage** ✅ PARTIEL
**Données utilisées:**
- `claims` - Nombre de chunks claimés
- `maxClaims` - Maximum de chunks
- `currentPower` - Power actuel

**Modifications:**
- Stats cards affichent les vraies valeurs

**⚠️ Limitations (données non disponibles dans FactionSnapshot):**
- Liste des coordonnées des claims → Reste avec placeholders

**🔧 À implémenter:**
Pour avoir la liste des claims, il faudrait :
1. Créer une classe `ClaimInfo` avec coords et type
2. Étendre `FactionSnapshot` avec `List<ClaimInfo> claims`
3. Récupérer les claims depuis `FactionManager.countClaims()` et les stocker

---

### 5. **AdminShopPage** ✅ PARTIEL
**Données utilisées:**
- `bank` - Balance de la faction

**Modifications:**
- Header affiche la balance réelle

**⚠️ Limitations (données non disponibles dans FactionSnapshot):**
- Shop items → Restent avec placeholders

**🔧 À implémenter:**
Les items du shop ne sont pas liés à FactionSnapshot mais à un système de shop.
Cela nécessiterait :
1. Un système de shop séparé avec `ShopItemRegistry`
2. Possiblement filtrer les items par niveau de faction
3. Envoyer la liste via un packet dédié

---

### 6. **SettingsFactionPage** ✅ PARTIEL
**Données utilisées:**
- `displayName` - Nom de la faction
- `description` - Description
- `mode` - Mode de recrutement (OPEN/INVITE_ONLY)

**Modifications:**
- Name field initialisé avec le vrai nom
- Description field initialisée avec la vraie description
- Toggle "Open to Join" basé sur le mode

**⚠️ Limitations (données non disponibles dans FactionSnapshot):**
- `motd` (Message of the Day) → Reste vide

**🔧 À implémenter:**
Pour avoir le MOTD, il faudrait :
1. Ajouter `String motd` à la classe `Faction`
2. Ajouter `motd` à `FactionSnapshot`
3. Implémenter la sauvegarde/chargement du MOTD

---

## ❌ Pages NON mises à jour (pas de données correspondantes)

### 7. **AlliancesPage** ❌ AUCUNE DONNÉE
**Raison:** FactionSnapshot ne contient pas de données d'alliances

**🔧 À implémenter:**
1. Ajouter `List<String> alliedFactions` à FactionSnapshot
2. Ajouter `Map<String, Integer> allianceMemberCounts` pour afficher le nombre de membres
3. Ou créer un système d'alliances séparé avec son propre packet

---

### 8. **QuestsPage** ❌ AUCUNE DONNÉE
**Raison:** FactionSnapshot ne contient pas de données de quêtes

**🔧 À implémenter:**
Les quêtes sont un système complètement séparé qui nécessiterait :
1. Classe `FactionQuest` avec nom, description, progrès, type (daily/weekly), reset time
2. `QuestManager` pour gérer les quêtes
3. Packet dédié `QuestDataPacket` pour synchroniser
4. Stockage dans `FactionSnapshot` ou système séparé

---

### 9. **SettingsPermissionsPage** ❌ SYSTÈME INDÉPENDANT
**Raison:** Les permissions sont gérées par un système séparé

**État actuel:**
- Fonctionne avec des données locales temporaires
- Les permissions ne sont PAS synchronisées avec le serveur

**🔧 À implémenter:**
1. Créer `PermissionsSnapshot` ou inclure dans `FactionSnapshot`
2. Structure: `Map<String, Set<String>> rankPermissions` (rank → permissions)
3. Packet dédié pour synchroniser les permissions
4. Envoyer au serveur les modifications quand "Save" est cliqué

---

### 10. **ChestPage** ✅ SYSTÈME DÉJÀ EN PLACE
**État:** Utilise le système de slots Minecraft natif via `FactionMenu`

Pas besoin de FactionSnapshot car les items sont synchronisés par le container system de Minecraft.

---

## 📊 Résumé

| Page | Status | Données FactionSnapshot | Données manquantes |
|------|--------|------------------------|-------------------|
| OverviewPage | ✅ Complet | name, level, members, claims, bank, etc. | - |
| LevelPage | ✅ Complet | level, xp, maxClaims, maxPlayers | - |
| MembersPage | ⚠️ Partiel | memberNames, membersRank, membersCount | power, online status |
| TerritoryPage | ⚠️ Partiel | claims, maxClaims, currentPower | Liste des claims |
| AdminShopPage | ⚠️ Partiel | bank | Shop items |
| SettingsFactionPage | ⚠️ Partiel | displayName, description, mode | motd |
| AlliancesPage | ❌ Aucune | - | Tout le système d'alliances |
| QuestsPage | ❌ Aucune | - | Tout le système de quêtes |
| SettingsPermissionsPage | ❌ Indépendant | - | Synchronisation permissions |
| ChestPage | ✅ Natif | - | - |

---

## 🎯 Priorités d'implémentation

### Priorité HAUTE (données critiques manquantes)
1. **Member power et online status** - Important pour MembersPage
2. **Liste des claims** - Important pour TerritoryPage
3. **MOTD** - Fonctionnalité de base pour SettingsFactionPage

### Priorité MOYENNE (fonctionnalités secondaires)
4. **Shop items** - Système complexe mais important
5. **Alliances** - Fonctionnalité sociale importante
6. **Permissions sync** - Important pour la gestion

### Priorité BASSE (features avancées)
7. **Quests system** - Feature bonus complexe

---

## 📝 Structure FactionSnapshot actuelle

```java
public class FactionSnapshot {
    // Informations de base
    public String name;
    public String displayName;
    public String description;
    public String mode;

    // Flags
    public boolean admin;
    public boolean warzone;
    public boolean safezone;

    // Progression
    public int level;
    public int xp;
    public int xpRequired;

    // Limites
    public int claims;
    public int maxClaims;
    public int membersCount;
    public int maxPlayers;
    public int warpsCount;
    public int maxWarps;

    // Ressources
    public int currentPower;
    public int maxPower;
    public int bank;

    // Membres
    public Map<UUID, String> membersRank;
    public Map<UUID, String> memberNames;
}
```

---

## 🚀 Suggestions d'améliorations

### Extension de FactionSnapshot (ajouts suggérés)

```java
public class FactionSnapshot {
    // ... champs existants ...

    // Pour MembersPage
    public Map<UUID, Integer> memberPower; // Power de chaque membre
    public Set<UUID> onlineMembers; // Membres connectés

    // Pour TerritoryPage
    public List<ClaimInfo> claimsList; // Liste détaillée des claims

    // Pour SettingsFactionPage
    public String motd; // Message du jour

    // Pour AlliancesPage
    public List<String> alliedFactions; // Noms des factions alliées
    public List<String> enemyFactions; // Noms des factions ennemies

    // Pour SettingsPermissionsPage
    public Map<String, Set<String>> rankPermissions; // Permissions par rang
}
```

### Nouveaux packets suggérés

1. **ShopDataPacket** - Pour synchroniser les items du shop
2. **QuestsDataPacket** - Pour synchroniser les quêtes
3. **ClaimsDataPacket** - Pour la liste détaillée des claims (si trop lourd pour FactionSnapshot)

---

## ✨ Ce qui fonctionne MAINTENANT

- ✅ Nom de faction affiché partout
- ✅ Power bar dynamique dans la sidebar
- ✅ Stats cards avec vraies valeurs (level, members, claims, power)
- ✅ Barre XP avec pourcentage correct
- ✅ Liste des membres avec noms et rangs
- ✅ Balance du shop
- ✅ Settings pré-remplis avec vraies données
- ✅ Système de synchronisation fonctionnel
- ✅ Mises à jour dynamiques possibles

---

*Généré le 2025-01-26 - Système FactionSnapshot v1.0*
