# Composants SVG - Système de Faction

## 📁 Structure des dossiers

```
components/
├── common/          # Composants communs à toutes les pages
├── members/         # Composants de la page Members
├── alliances/       # Composants de la page Alliances
├── territory/       # Composants de la page Territory
├── chest/           # Composants de la page Chest
├── settings/        # Composants de la page Settings
├── permissions/     # Composants de la page Permissions
├── level/           # Composants de la page Level
├── quests/          # Composants de la page Quests
└── shop/            # Composants de la page Shop
```

---

## 🎨 Composants COMMON (Communs)

### Boutons Principaux
- **button-primary-normal.svg** (170x40) - Bouton principal normal
- **button-primary-hover.svg** (170x40) - Bouton principal au survol
- **button-secondary-normal.svg** (170x40) - Bouton secondaire normal
- **button-secondary-hover.svg** (170x40) - Bouton secondaire au survol
- **button-danger-normal.svg** (170x40) - Bouton danger normal
- **button-danger-hover.svg** (170x40) - Bouton danger au survol

### Navigation Sidebar
- **nav-button-normal.svg** (124x36) - Bouton de navigation normal
- **nav-button-hover.svg** (124x36) - Bouton de navigation au survol
- **nav-button-selected.svg** (124x36) - Bouton de navigation sélectionné

### Barres de progression
- **progressbar-empty.svg** (490x20) - Barre de progression vide
- **progressbar-filled-70.svg** (490x20) - Barre de progression remplie à 70%

### Autres
- **close-button-normal.svg** (20x20) - Bouton fermeture normal
- **close-button-hover.svg** (20x20) - Bouton fermeture au survol

---

## 👥 Composants MEMBERS

### Boutons d'action
- **button-promote-normal.svg** (35x28) - Bouton promote normal
- **button-promote-hover.svg** (35x28) - Bouton promote au survol
- **button-demote-normal.svg** (35x28) - Bouton demote normal
- **button-demote-hover.svg** (35x28) - Bouton demote au survol
- **button-kick-normal.svg** (35x28) - Bouton kick normal
- **button-kick-hover.svg** (35x28) - Bouton kick au survol

### Cartes
- **member-card-normal.svg** (530x56) - Carte membre normale
- **member-card-hover.svg** (530x56) - Carte membre au survol

---

## 🤝 Composants ALLIANCES

### Boutons
- **button-add-alliance-normal.svg** (530x48) - Bouton ajouter alliance normal
- **button-add-alliance-hover.svg** (530x48) - Bouton ajouter alliance au survol
- **button-remove-normal.svg** (60x32) - Bouton remove normal
- **button-remove-hover.svg** (60x32) - Bouton remove au survol

### Cartes
- **alliance-card-normal.svg** (530x68) - Carte alliance normale
- **alliance-card-hover.svg** (530x68) - Carte alliance au survol

---

## 🗺️ Composants TERRITORY

### Cartes
- **stat-card-normal.svg** (165x60) - Carte de statistique
- **claim-card-normal.svg** (530x48) - Carte de claim normale
- **claim-card-hover.svg** (530x48) - Carte de claim au survol

---

## 📦 Composants CHEST

### Slots d'inventaire
- **inventory-slot-empty.svg** (54x54) - Slot d'inventaire vide
- **inventory-slot-hover.svg** (54x54) - Slot d'inventaire au survol

---

## ⚙️ Composants SETTINGS

### Toggles et inputs
- **toggle-on.svg** (100x32) - Toggle activé
- **toggle-off.svg** (100x32) - Toggle désactivé
- **input-field-normal.svg** (490x36) - Champ de saisie normal
- **input-field-focus.svg** (490x36) - Champ de saisie focus

---

## 🔐 Composants PERMISSIONS

### Checkboxes
- **checkbox-unchecked.svg** (20x20) - Checkbox non cochée
- **checkbox-checked.svg** (20x20) - Checkbox cochée
- **checkbox-hover.svg** (20x20) - Checkbox au survol

---

## ⭐ Composants LEVEL

### Badges
- **level-badge.svg** (90x90) - Badge de niveau circulaire

---

## 🎯 Composants QUESTS

### Cartes de quêtes
- **quest-card-daily-normal.svg** (258x70) - Carte quête journalière normale
- **quest-card-daily-hover.svg** (258x70) - Carte quête journalière au survol
- **quest-card-daily-completed.svg** (258x70) - Carte quête journalière complétée
- **quest-card-weekly-normal.svg** (530x92) - Carte quête hebdomadaire

### Barres de progression
- **questbar-blue-empty.svg** (226x6) - Barre de quête vide
- **questbar-blue-filled-64.svg** (226x6) - Barre de quête bleue remplie à 64%
- **questbar-green-filled-100.svg** (226x6) - Barre de quête verte remplie à 100%

### Boutons
- **button-claim-normal.svg** (60x18) - Bouton claim normal
- **button-claim-hover.svg** (60x18) - Bouton claim au survol

---

## 🛒 Composants SHOP

### Cartes d'items
- **shop-item-normal.svg** (165x110) - Carte d'item normale
- **shop-item-hover.svg** (165x110) - Carte d'item au survol

### Boutons
- **button-purchase-normal.svg** (90x32) - Bouton purchase normal
- **button-purchase-hover.svg** (90x32) - Bouton purchase au survol

---

## 🎨 Palette de couleurs utilisée

### Primaire
- **Violet/Bleu**: `#667eea` → `#764ba2`
- **Cyan**: `#00d2ff` → `#3a47d5`
- **Cosmic**: `#a855f7` → `#ec4899` → `#3b82f6`

### Actions
- **Vert** (Success/Promote): `#10b981` → `#059669`
- **Orange** (Warning/Demote): `#f59e0b` → `#d97706`
- **Rouge** (Danger/Kick): `#ef4444` → `#dc2626`
- **Or** (Premium): `#fbbf24` → `#f59e0b`

### Backgrounds
- **Dark**: `#1e1e2e`, `#2a2a3e`
- **Borders**: `#667eea`

---

## 📝 Notes d'utilisation

1. Tous les composants utilisent des **filtres de glow** pour l'effet cybernétique
2. Les états **hover** ont généralement un glow plus fort et des couleurs plus claires
3. Les **états selected** ont une ligne décorative en haut
4. Tous les composants ont des **coins arrondis** (rx="4" ou rx="6")
5. Les dimensions sont exactes et correspondent aux éléments dans les pages complètes

---

## 🔧 Intégration

Pour utiliser ces composants :
1. Remplacer les éléments graphiques dans vos interfaces
2. Ajouter les événements hover/click en CSS ou JS
3. Adapter les couleurs si nécessaire en modifiant les gradients
