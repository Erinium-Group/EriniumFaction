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
- ✅ Annulation si dégâts reçus ou mouvement (warmup/cooldown)

### Interfaces (GUI)
- ✅ Écran de Faction responsive (s'adapte à la taille de la fenêtre)
- ✅ Bouton « Settings » disponible (si le joueur est en faction)
- ✅ Écran « Faction Settings » refondu en interrupteurs glissants (switchs)
  - Ouvert/fermé (open/close) [perm: ef.faction.settings.open]
  - Mode PUBLIC / INVITE_ONLY [perm: ef.faction.settings.mode]
  - SAFEZONE [perm: ef.faction.settings.safezone]
  - Synchro initiale à l’ouverture + re-synchro après chaque action (même en cas de refus permission)

### Carte des claims (mini-map de territoire)
- ✅ Ouverture par une touche (par défaut: M, personnalisable)
- ✅ OU via un bouton HUD en haut à droite (customisable: position/offset/taille/texture/thème)
- ✅ Affiche une grille centrée sur le chunk du joueur, colorée par propriétaire
- ✅ Contrôles: « − » et « + » pour le rayon (1..32), « R » pour rafraîchir
- ✅ Rafraîchissement automatique quand le joueur change de chunk (anti-spam)
- ✅ Infobulle au survol: nom de la faction + coordonnées du chunk (cx, cz)

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

## ⌨️ Raccourcis et HUD
- Carte des claims: touche « M » par défaut (modifiable en jeu)
- Ouverture alternative: bouton HUD configuré (par défaut en haut à droite)
  - Masqué si un écran est ouvert; option pour le cacher aussi en F3 (debug)
  - Tooltip facultatif au survol

## 🔐 Permissions (extraits)
- Paramètres de faction (écran Settings):
  - ef.faction.settings.open       → basculer « ouvert/fermé »
  - ef.faction.settings.mode       → basculer PUBLIC/INVITE_ONLY
  - ef.faction.settings.safezone   → basculer SAFEZONE
- En cas d'absence: message « Vous n'avez pas la permission. » et action refusée; l’UI se re-synchronise.

## ⚙️ Configuration

### Côté serveur (erinium_faction-server.toml)
Paramètres principaux (extraits):
- Noms de faction: `nameMin`, `nameMax`
- Membres et limites: `maxMembers`, `baseMaxPlayers`, `playersPerLevel`
- Power: `baseMaxPower`, `powerRegenPerMinute`, `powerLossOnDeath`, `factionMaxFromPlayers`
- Claims: `maxClaims`
- Warps: `baseWarps`, `warpsPer5Levels`
- Téléportation: `tpWarmupSeconds`, `tpCooldownSeconds`, `tpCancelOnMove`, `tpCancelOnDamage`, `tpAllowCrossDimension`

### Côté client (erinium_faction-client.toml)
- keybinds:
  - `mapDefaultKey` (string) → touche par défaut pour ouvrir la carte
    - Exemples valides: "M", "key.keyboard.m", "F10", "key.keyboard.g"
- mapOverlay:
  - `openControl` ("KEY" | "BUTTON" | "BOTH") → méthode(s) d’ouverture de la carte
  - `buttonAnchor` ("TOP_RIGHT" | "TOP_LEFT" | "BOTTOM_RIGHT" | "BOTTOM_LEFT") → ancre du bouton HUD
  - `buttonOffsetX`, `buttonOffsetY` (int) → décalages en px
  - `buttonSize` (int) → taille (px) du bouton HUD
  - `buttonTheme` ("AUTO" | "LIGHT" | "DARK") → thème du bouton
  - `buttonTextureLight`, `buttonTextureDark` (ResourceLocation) → textures du bouton
  - `buttonTooltip` (bool) → afficher un tooltip au survol
  - `hideInDebug` (bool) → masquer le bouton HUD lorsque F3 (debug) est actif

Emplacements:
- En dev: `run/config/erinium_faction-client.toml`
- En jeu: `config/erinium_faction-client.toml`

## 🗺️ Carte des claims – Guide rapide
- Ouvrir: touche M (par défaut) ou bouton HUD
- Contrôles: « − / R / + »
  - − / +: ajuste le rayon d'affichage (1 à 32)
  - R: rafraîchit la grille
- Survol d'une case: nom de la faction + `Chunk: (cx, cz)`
- La carte se rafraîchit automatiquement si vous changez de chunk (anti-spam intégré)

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
- GUI client: `gui/screens/*`, widgets `gui/widgets/*`
- Réseau: `common/network/*` (packets map/settings inclus)
- Claims: `core/claim/*` (SavedData + helpers)
- Factions: `core/faction/*`
- Config: `common/config/*` (serveur + client)

## 📋 Permissions des Rangs (exemple)

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

- [x] Interface graphique (GUI) pour la gestion (responsive + Settings)
- [x] Carte des territoires (mini-map in-game avec keybind et bouton HUD)
- [ ] Système de power dynamique (mort = perte de power)
- [ ] Raids et destruction de claims
- [ ] Économie de faction (banque)
- [ ] Coffres de faction partagés
- [ ] Permissions personnalisables par rang (étendre et exposer en GUI)
- [ ] Chat de faction privé
- [ ] Logs d'activité détaillés
- [ ] Support multi-langue (déjà fr/en partiel)

## 📝 Changelog (récapitulatif des ajouts récents)
- Écran « Faction Settings »: remplacement des boutons par des switches glissants (vert/rouge)
  - Ouvert/fermé, Mode PUBLIC/INVITE, SAFEZONE
  - Synchro initiale à l’ouverture + re-synchro automatique après chaque action (même refusée)
  - Passage aux données NBT (.dat) pour « open » (fin des JSON EFUtils côté Settings)
  - Nouveau paquet clientbound: FactionSettingsStateMessage (synchro d’état)
- Carte des claims:
  - Ouverture par touche ou bouton HUD
  - Bouton HUD configurable: ancre/offset/taille, thème AUTO/LIGHT/DARK, textures personnalisables, tooltip, option pour cacher en F3
  - Tooltip: affiche nom de faction + coordonnées de chunk
- Stabilité: correctif d’abonnement aux événements dégâts (LivingDamageEvent.Pre) pour la TP (évite un crash au chargement)
- Séparation stricte client/serveur renforcée (handlers client via enqueueWork, helper client-only, aucune API client côté serveur)

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
