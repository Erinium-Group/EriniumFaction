# Cyber Astral GUI Design - Erinium Faction

Design futuriste cyber astral pour les interfaces du mod avec des effets de glow, starfield et gradients holographiques.

## 🎨 Palette de Couleurs

### Couleurs Principales
- **Background Sombre**: `#0a0e27` → `#1a1535` (Bleu nuit profond)
- **Panels**: `#1a1535` → `#0f1123` (Gradient semi-transparent)

### Couleurs Accent
- **Cyan (Input)**: `#00ffff` / `#00d4ff` → `#0080ff`
- **Magenta (Output)**: `#ff006e` → `#c724b1`
- **Purple (Both)**: `#7209b7` → `#9d4edd`
- **Gray (None)**: `#2a2a3a` → `#1a1a2a`

### Effets
- **Glow Filter**: Gaussian Blur pour effets lumineux
- **Starfield**: Pattern d'étoiles avec opacité variée
- **Borders**: Lignes néon avec gradients

---

## 📁 Structure des Fichiers

### Designs Complets

#### `TitaniumCompressorScreen.svg` (176x166 px)
Interface complète du compresseur avec:
- Background starfield astral
- Barre d'énergie animable (cyan → purple)
- Flèche de progression avec glow
- Slots input (cyan) / output (purple)
- Inventaire avec accents violets
- Hotbar avec glow cyan
- Bouton config avec icône engrenage

#### `FaceConfigScreen.svg` (500x400 px)
Interface de configuration 3D avec:
- Vue isométrique du cube (6 faces)
- Face NORTH: 60x60 (grande)
- Faces EAST/UP: 40x40 (moyennes)
- Faces DOWN/SOUTH/WEST: 30x30 (petites)
- Panneaux de contrôle avec LEDs
- Légende des modes avec accents colorés
- Corners holographiques et scan lines

---

## 🧩 Assets Individuels (21 fichiers)

### Backgrounds & Patterns
| Fichier | Dimensions | Description |
|---------|-----------|-------------|
| `bg-starfield.svg` | 200x200 | Pattern d'étoiles avec variations de couleur |
| `bg-gradient-dark.svg` | 200x200 | Gradient background bleu nuit |

### Slots
| Fichier | Dimensions | Glow Color | Usage |
|---------|-----------|-----------|-------|
| `slot-input.svg` | 18x18 | Cyan | Input machine slot |
| `slot-output.svg` | 18x18 | Purple | Output machine slot |
| `slot-inventory.svg` | 16x16 | Purple (subtle) | Inventory 3x9 grid |
| `slot-hotbar.svg` | 16x16 | Cyan | Hotbar slots |

### Machine Components
| Fichier | Dimensions | Description |
|---------|-----------|-------------|
| `energy-bar.svg` | 14x44 | Barre d'énergie verticale avec gradient cyan→purple |
| `progress-arrow.svg` | 48x8 | Flèche de progression avec animation cyan |
| `panel-main.svg` | 164x56 | Panel principal avec gradient et bordure |

### Boutons
| Fichier | Dimensions | Glow | Usage |
|---------|-----------|------|-------|
| `button-config.svg` | 18x14 | Cyan | Config gear button |
| `button-large.svg` | 140x24 | Cyan | Auto Input/Output |
| `button-small.svg` | 100x24 | Purple | Done button |

### Face Modes
| Fichier | Dimensions | Mode | Couleurs |
|---------|-----------|------|----------|
| `face-none.svg` | 60x60 | NONE | Gray (#2a2a3a) |
| `face-input.svg` | 60x60 | INPUT | Cyan (#00d4ff → #0080ff) |
| `face-output.svg` | 60x60 | OUTPUT | Magenta (#ff006e → #c724b1) |
| `face-both.svg` | 60x60 | BOTH | Purple (#7209b7 → #9d4edd) |

### Indicateurs
| Fichier | Dimensions | Description |
|---------|-----------|-------------|
| `led-on.svg` | 8x8 | LED verte avec strong glow |
| `led-off.svg` | 8x8 | LED grise désactivée |

### Décorations
| Fichier | Dimensions | Description |
|---------|-----------|-------------|
| `corner-accent.svg` | 20x20 | Accent holographique de coin |
| `glow-line-cyan.svg` | 200x2 | Ligne lumineuse cyan |
| `glow-line-purple.svg` | 200x2 | Ligne lumineuse violette |

---

## ✨ Effets et Filtres SVG

### Glow Filter (Standard)
```xml
<filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
  <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
  <feMerge>
    <feMergeNode in="coloredBlur"/>
    <feMergeNode in="SourceGraphic"/>
  </feMerge>
</filter>
```

### Strong Glow (Pour LEDs)
```xml
<filter id="strongGlow" x="-50%" y="-50%" width="200%" height="200%">
  <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
  ...
</filter>
```

### Starfield Pattern
Pattern répétable 100x100 ou 150x150 avec étoiles de tailles variées et couleurs (blanc, cyan, purple)

---

## 🎯 Utilisation

### Pour Minecraft
1. Convertir les SVG en PNG avec résolution exacte
2. Utiliser les assets comme textures GUI
3. Animer les éléments (energy bar, progress arrow) via code Java

### Animation Suggestions

**Energy Bar** (`energy-bar.svg`):
- Modifier la hauteur du rectangle `energy-fill`
- Hauteur min: 0px, max: 42px

**Progress Arrow** (`progress-arrow.svg`):
- Animer le path cyan de x=4 à x=36
- Déplacer le cercle lumineux de cx=4 à cx=36

**LEDs** (`led-on.svg` / `led-off.svg`):
- Toggle entre les deux états selon auto-input/output

---

## 🔧 Customisation

### Changer les Couleurs
Les gradients sont définis dans `<defs>`, facile à modifier:

```xml
<!-- Exemple: Changer input de cyan à vert -->
<linearGradient id="inputGradient">
  <stop offset="0%" style="stop-color:#00ff88"/> <!-- Ancien: #00d4ff -->
  <stop offset="100%" style="stop-color:#00aa44"/> <!-- Ancien: #0080ff -->
</linearGradient>
```

### Ajuster le Glow
Modifier `stdDeviation` dans les filtres:
- Standard: `2` à `2.5`
- Strong: `4` à `5`
- Subtle: `1` à `1.5`

### Modifier l'Opacité des Étoiles
Dans `bg-starfield.svg`, ajuster `opacity` des cercles (0.5 à 1.0)

---

## 📐 Dimensions de Référence

### TitaniumCompressorScreen
```
Screen: 176x166
Title Area: 168x16 (x:4, y:4)
Machine Panel: 164x56 (x:6, y:24)
Energy Bar: 14x44 (x:10, y:30)
Input Slot: 18x18 (x:32, y:36)
Progress: 48x8 (x:60, y:42)
Output Slot: 18x18 (x:118, y:36)
Inventory: 164x56 (x:6, y:96)
Hotbar: 9 slots 16x16 (y:156)
```

### FaceConfigScreen
```
Screen: 500x400
Cube Center: (250, 190)
Face NORTH: 60x60 (-30, -30 from center)
Face UP: 40x40 (-20, -80 from center)
Face EAST: 40x40 (40, -30 from center)
Small faces: 30x30 (various positions)
Buttons: (250, 300)
Legend: (400, 110)
```

---

## 🌟 Caractéristiques Cyber Astral

### Style Visuel
- **Sci-fi futuriste** avec néons et hologrammes
- **Starfield astral** en background
- **Glow effects** sur tous les éléments interactifs
- **Corners accent** style holographique
- **Scan lines** subtiles pour effet CRT

### Hiérarchie des Couleurs
1. **Cyan**: Éléments input, énergie, accents principaux
2. **Purple/Magenta**: Output, mode both, accents secondaires
3. **White**: Texte, labels, highlights
4. **Dark blue**: Backgrounds, panels

### Contraste
- Background très sombre (#0a0e27) pour faire ressortir les glows
- Texte blanc/cyan avec opacité 0.8-1.0
- Bordures avec opacité 0.4-0.7 pour subtilité

---

## 📝 Notes Techniques

### Compatibilité Minecraft
- Les filtres SVG (glow) ne seront pas rendus dans Minecraft
- Convertir en PNG avec les effets "baked in"
- Utiliser un éditeur comme Inkscape pour export de qualité

### Optimisation PNG
```bash
# Export avec Inkscape (recommandé)
inkscape --export-type=png --export-dpi=96 fichier.svg

# Ou avec dimensions exactes
inkscape --export-width=176 --export-height=166 TitaniumCompressorScreen.svg
```

### Code Java Integration
Les assets peuvent être référencés dans le code:
```java
// TitaniumCompressorScreen.java:13
private static final ResourceLocation GUI_TEX =
    ResourceLocation.fromNamespaceAndPath("erinium_faction",
        "textures/gui/titanium_compressor.png");
```

---

## 🎨 Inspiration
- **Mekanism**: Interface de machine tech
- **Cyberpunk 2077**: Néons et hologrammes
- **No Man's Sky**: Starfields et UI spatiale
- **Subnautica**: Panels futuristes avec glow

---

Créé le 2025-11-08
Style: Cyber Astral Futuristic
Version: 1.0
