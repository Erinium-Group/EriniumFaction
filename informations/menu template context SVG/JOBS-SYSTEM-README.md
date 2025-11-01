# Jobs System Design - Documentation

## Vue d'ensemble

Ce système de jobs comprend 6 métiers, chacun avec sa propre progression de niveau 1 à 100 et un système d'XP unique.

## Métiers disponibles

1. **Miner** (⛏️) - Couleur: `#fbbf24` (Jaune/Or)
   - Extraction de ressources minérales

2. **LumberJack** (🪓) - Couleur: `#8b4513` (Marron)
   - Coupe de bois et récolte forestière

3. **Hunter** (🏹) - Couleur: `#ef4444` (Rouge)
   - Chasse et combat contre les mobs

4. **Fisher** (🎣) - Couleur: `#3b82f6` (Bleu)
   - Pêche et ressources aquatiques

5. **Farmer** (🌾) - Couleur: `#10b981` (Vert)
   - Agriculture et élevage

6. **Wizard** (🔮) - Couleur: `#a855f7` (Violet)
   - Magie et enchantements

## Structure des écrans

### 1. Menu Principal (`jobs-main-menu.svg`)
- **Taille**: 400x270px
- **Contenu**:
  - Grille 3x2 de cartes de jobs
  - Chaque carte affiche:
    - Icône du métier (64x64px)
    - Nom du métier
    - Niveau actuel
    - Barre de progression XP
    - XP actuel / XP requis
    - Indicateur "Click to view"
  - Scrollbar verticale (pour extensibilité)
  - Stats en bas: Total jobs et niveau moyen

### 2. Vue Détail d'un Job (`jobs-detail-view.svg`)
- **Taille**: 400x270px
- **Contenu**:
  - Header avec icône du métier et description
  - Carte de niveau avec:
    - Badge de niveau
    - Information XP (750/1000 XP - 75%)
    - Barre de progression animée
  - 2 boutons d'action:
    - **"How to gain XP"** (Vert `#10b981`)
    - **"Unlocked Features"** (Violet `#a855f7`)
  - Section "Recent Unlocks":
    - Liste des derniers débloqués
    - Prochains unlocks verrouillés
    - Badge de niveau requis

### 3. Page "How to gain XP" (`jobs-howtoxp-view.svg`)
- **Taille**: 400x270px
- **Couleur thème**: Vert (`#10b981`)
- **Contenu**:
  - Header avec titre et description
  - Indicateur de niveau actuel
  - Scroll list des actions XP avec:
    - Icône de l'action
    - Nom et description
    - Badge de niveau (min-max ou "All Levels")
    - Valeur XP (ex: "+50 XP")
  - États visuels:
    - ✅ **Disponible**: Pleine opacité, bordure verte
    - 🔒 **Verrouillé (niveau trop bas)**: Opacité 40%, texte rouge "Requires Level X"
    - ⛔ **Non disponible (niveau trop haut)**: Opacité 40%, texte gris "No longer available"

### 4. Page "Unlocked Features" (`jobs-unlocked-features-view.svg`)
- **Taille**: 400x270px
- **Couleur thème**: Violet (`#a855f7`)
- **Contenu**:
  - Header avec titre et description
  - Indicateur de niveau et stats (X unlocked • Y locked)
  - Scroll list des features avec:
    - Icône de la feature
    - Nom et description
    - Badge de niveau requis
  - États visuels:
    - ✅ **Débloqué**: Bordure verte `#10b981`, icône ✓
    - 🔒 **Verrouillé**: Bordure rouge `#ef4444`, icône 🔒, texte "Requires Level X • Y levels to go"

## Système de couleurs

### Couleurs principales par job
```
Miner:      #fbbf24 (Jaune/Or)
LumberJack: #8b4513 (Marron)
Hunter:     #ef4444 (Rouge)
Fisher:     #3b82f6 (Bleu)
Farmer:     #10b981 (Vert)
Wizard:     #a855f7 (Violet)
```

### Couleurs système
```
Background:       #1a1a2e → #16213e (Gradient)
Card Background:  #2a2a3e → #1e1e2e (Gradient)
Border:           #fbbf24 (Or) ou couleur du job
Text Primary:     #ffffff (Blanc)
Text Secondary:   #9ca3af (Gris)
Text Disabled:    #6b7280 (Gris foncé)
Success:          #10b981 (Vert)
Error/Locked:     #ef4444 (Rouge)
```

## Éléments interactifs

### Boutons
- **Close**: Carré rouge `#ef4444` avec croix blanche (14x14px)
- **Back**: Cercle avec chevron gauche "‹"
- **Action buttons**: Rectangles avec bordure colorée et hover effect

### Badges de niveau
- **Format**: "LVL XX" ou "Lvl XX-YY" pour les ranges
- **Couleurs**:
  - Vert `#10b981`: Disponible
  - Rouge `#ef4444`: Verrouillé
  - Gris `#6b7280`: Non disponible
  - Violet `#a855f7`: Tous niveaux

### Barres de progression XP
- Track: `#3a3a4a` (Gris foncé)
- Fill: Gradient de la couleur du job
- Hauteur: 4px (petite), 16px (moyenne)
- Coins arrondis: `rx="8"`

### Scrollbar
- Track: `#2a2a3e` (Gris)
- Thumb: Couleur de la page courante
- Largeur: 4px
- Position: x="390"

## Dimensions et espacements

### Layout
```
Canvas:            400x270px
Border:            2px
Padding externe:   8px
Header height:     40px
Card spacing:      6px
Border radius:     4px (grande), 2-3px (petite)
```

### Cartes de job (Menu principal)
```
Largeur:   120px
Hauteur:   86px
Icon:      64x64px dans carré de 64x64px
Spacing:   6px entre les cartes
Grid:      3 colonnes x 2 rangées
```

### Items de liste (scroll)
```
Largeur:   372px (14px + 372px + 14px = 400px)
Hauteur:   28px par item
Spacing:   4px entre items
Icon:      16x16px
```

## Notes d'implémentation

1. **Extensibilité**: Le système est conçu pour supporter plus de 6 jobs via la scrollbar
2. **Responsive**: Les dimensions sont fixes à 400x270px pour correspondre à la GUI Minecraft
3. **Icons**: Les emojis sont des placeholders - remplacer par des vraies icônes 64x64px
4. **Animations**: Les barres XP et hover effects peuvent être animés en CSS/Java
5. **Localisation**: Tous les textes doivent être dans des fichiers de langue

## Progression XP

### Formule suggérée
```
XP_required = base_xp * (level ^ 1.5)
Où base_xp = 100 pour le niveau 1

Exemples:
Niveau 1:  100 XP
Niveau 10: 316 XP
Niveau 25: 1250 XP
Niveau 50: 3536 XP
Niveau 100: 10000 XP
```

### Paliers de déblocage suggérés
```
Niveau 1-10:   Outils de base
Niveau 11-25:  Outils avancés
Niveau 26-50:  Capacités spéciales
Niveau 51-75:  Maîtrise avancée
Niveau 76-100: Capacités ultimes
```

## Fichiers SVG créés

1. `jobs-main-menu.svg` - Menu principal avec grille de jobs
2. `jobs-detail-view.svg` - Vue détaillée d'un job spécifique
3. `jobs-howtoxp-view.svg` - Liste des actions qui donnent de l'XP
4. `jobs-unlocked-features-view.svg` - Liste des features débloquées/verrouillées

Ces designs peuvent être convertis en composants Java Swing/JavaFX pour Minecraft.
