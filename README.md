# 🛡️ Erinium Faction

> Un mod PvP Faction complet pour Minecraft 1.21.1 (NeoForge)

## 📖 Description

Erinium Faction est un mod de faction PvP qui permet aux joueurs de créer des factions, de revendiquer des territoires, de former des alliances et de faire la guerre. Parfait pour les serveurs PvP compétitifs !

## ✨ Fonctionnalités

### Factions
- ✅ Création et gestion de factions
- ✅ Système de rangs hiérarchiques (Owner, Officer, Member, Recruit)
- ✅ Invitation et expulsion de membres
- ✅ Système de power pour limiter les claims

### Territoires
- ✅ Claim de chunks par faction
- ✅ Protection automatique des territoires
- ✅ Limite de claims basée sur le power de la faction
- ✅ Empêche les non-membres de casser/placer des blocs

### Relations
- ✅ Système d'alliances entre factions
- ✅ Déclaration de guerre (ennemis)
- ✅ Relations neutres
- ✅ Protection PvP basée sur les relations

### PvP
- ✅ Friendly fire désactivable
- ✅ Protection des alliés configurable
- ✅ Système de guerre entre factions ennemies

### Téléportation
- ✅ Home de faction (point de spawn)
- ✅ Délai de téléportation configurable
- ✅ Annulation si dégâts reçus

## 🎮 Commandes

### Commandes Principales (`/faction` ou `/f`)

```
/f create <nom>              - Créer une faction
/f disband                   - Dissoudre votre faction (owner seulement)
/f invite <joueur>           - Inviter un joueur
/f kick <joueur>             - Expulser un joueur
/f leave                     - Quitter votre faction

/f claim                     - Revendiquer le chunk actuel
/f unclaim                   - Abandonner le chunk actuel

/f sethome                   - Définir le home de la faction
/f home                      - Se téléporter au home

/f ally <faction>            - Allier avec une faction
/f enemy <faction>           - Déclarer une faction ennemie
/f neutral <faction>         - Mettre une faction en neutre

/f info [faction]            - Voir les infos d'une faction
/f list                      - Liste toutes les factions
```

### Commandes de Rang (`/rank`)

```
/rank promote <joueur>       - Promouvoir un membre
/rank demote <joueur>        - Rétrograder un membre
/rank list                   - Liste les membres et leurs rangs
```

## ⚙️ Configuration

Le mod se configure via `config/erinium_faction-common.toml` :

```toml
[pvp]
    friendlyFire = false        # Activer le feu ami
    allyDamage = false          # Activer les dégâts entre alliés

[claims]
    maxClaimsPerFaction = 100   # Limite de claims par faction
    maxPowerPerPlayer = 10      # Power maximum par joueur

[teleport]
    homeDelay = 3               # Délai avant téléportation (secondes)
    cancelOnDamage = true       # Annuler si dégâts reçus

[faction]
    minNameLength = 3           # Longueur min du nom
    maxNameLength = 16          # Longueur max du nom
```

## 📦 Installation

1. Téléchargez le fichier `.jar` depuis les releases
2. Placez-le dans le dossier `mods/` de votre instance Minecraft
3. Assurez-vous d'avoir NeoForge 21.1.213+ installé
4. Lancez le jeu !

## 🛠️ Développement

### Prérequis
- Java 21+
- Gradle 8.10+
- NeoForge 21.1.213

### Build
```bash
./gradlew build
```

Le fichier `.jar` sera généré dans `build/libs/`

### Structure du Code
Consultez [STRUCTURE.md](STRUCTURE.md) pour comprendre l'architecture du mod.

## 📋 Permissions des Rangs

| Action | OWNER | OFFICER | MEMBER | RECRUIT |
|--------|-------|---------|--------|---------|
| Claim | ✅ | ✅ | ✅ | ❌ |
| Unclaim | ✅ | ✅ | ✅ | ❌ |
| Inviter | ✅ | ✅ | ❌ | ❌ |
| Kick | ✅ | ✅ | ❌ | ❌ |
| Promouvoir | ✅ | ❌ | ❌ | ❌ |
| Gérer Relations | ✅ | ✅ | ❌ | ❌ |
| Set Home | ✅ | ✅ | ❌ | ❌ |
| Dissoudre | ✅ | ❌ | ❌ | ❌ |

## 🗺️ Roadmap

- [ ] Interface graphique (GUI) pour la gestion
- [ ] Carte des territoires (overlay F3)
- [ ] Système de power dynamique (mort = perte de power)
- [ ] Raids et destruction de claims
- [ ] Économie de faction (banque)
- [ ] Coffres de faction partagés
- [ ] Permissions personnalisables par rang
- [ ] Chat de faction privé
- [ ] Logs d'activité détaillés
- [ ] Support multi-langue

## 👥 Auteurs

**Erinium Group & BCK**

## 📜 Licence

All Rights Reserved

## 🐛 Bugs et Suggestions

Si vous trouvez un bug ou avez une suggestion, n'hésitez pas à ouvrir une issue !

---

**Version actuelle :** 1.0-SNAPSHOT  
**Minecraft :** 1.21.1  
**NeoForge :** 21.1.213

