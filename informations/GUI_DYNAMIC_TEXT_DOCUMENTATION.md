# FactionMenuScreen - Documentation Complète

Ce document explique l'architecture du GUI de faction et liste tous les textes dynamiques à remplacer.

---

## 📐 ARCHITECTURE

Le GUI est maintenant modulaire et propre:

### Structure des fichiers:
```
gui/screens/
├── FactionMenuScreen.java          # GUI principal (sidebar + header)
└── pages/
    ├── FactionPage.java            # Classe abstraite
    ├── OverviewPage.java           # Page Overview
    ├── MembersPage.java            # Page Members
    ├── TerritoryPage.java          # Page Territory
    ├── AlliancesPage.java          # Page Alliances
    ├── ChestPage.java              # Page Chest
    ├── LevelPage.java              # Page Level
    ├── QuestsPage.java             # Page Quests
    ├── AdminShopPage.java          # Page Shop
    ├── SettingsFactionPage.java    # Page Settings
    └── SettingsPermissionsPage.java # Page Permissions
```

### Dimensions:
- **Taille FIXE du GUI:** 768x512 pixels
- **Sidebar:** x=16-156 (140px de largeur)
- **Panel principal:** x=164-752 (588px de largeur)
- **Zone de contenu des pages:** x=180, y=88, w=530, h=400

### Système de scaling:
- `sx()`, `sy()`: Positions scalées
- `sw()`, `sh()`: Tailles scalées
- **Limit de scale:** 0.5x à 1.5x (pour s'adapter aux petites fenêtres comme 800x600)

---

## 📋 PAGES DISPONIBLES (basées sur les 10 SVG)

1. **OVERVIEW** - Vue d'ensemble (overview.svg)
2. **MEMBERS** - Liste des membres (members.svg)
3. **TERRITORY** - Territoires (territory.svg)
4. **ALLIANCES** - Alliances (alliances.svg)
5. **CHEST** - Coffre faction 9x4 (chest.svg)
6. **LEVEL** - Niveau et XP (faction-level.svg)
7. **QUESTS** - Quêtes (quests.svg)
8. **ADMINSHOP** - Shop (adminshop.svg)
9. **SETTINGS_FACTION** - Paramètres (settings-faction.svg)
10. **SETTINGS_PERMISSIONS** - Permissions (settings-permissions.svg)

---

## 🔄 PLACEHOLDERS À REMPLACER

### 📍 SIDEBAR (FactionMenuScreen.java)

| Placeholder | Emplacement | Description | Type | Exemple |
|------------|-------------|-------------|------|---------|
| `{{FACTION_NAME}}` | Logo sidebar | Nom de la faction | String | "Erinium" |
| `{{POWER_CURRENT}}` | Power bar | Power actuel | Integer | 75 |
| `{{POWER_MAX}}` | Power bar | Power max | Integer | 100 |

**Valeur hardcodée à remplacer:**
- Ligne ~229: `int powerPercent = 75;` → Calculer avec `(POWER_CURRENT * 100) / POWER_MAX`

---

### 🏠 OverviewPage.java

| Placeholder | Description | Type |
|------------|-------------|------|
| `{{MEMBER_COUNT}}` | Nombre de membres | Integer |
| `{{CLAIM_COUNT}}` | Nombre de claims | Integer |
| `{{KILL_COUNT}}` | Nombre de kills | Integer |
| `{{FACTION_NAME}}` | Nom (répété) | String |
| `{{LEADER_NAME}}` | Leader | String |
| `{{CREATION_DATE}}` | Date création | String |
| `{{FACTION_DESCRIPTION}}` | Description | String |
| `{{RECENT_ACTIVITY_TEXT}}` | Activité récente | String |

---

### 👥 MembersPage.java

Pour chaque membre (i=1 à 5):

| Placeholder | Description | Type |
|------------|-------------|------|
| `{{MEMBER_i_NAME}}` | Nom du membre | String |
| `{{MEMBER_i_RANK}}` | Rang du membre | String |
| `{{MEMBER_i_POWER}}` | Power du membre | Integer |

Footer:
- `{{MEMBERS_ONLINE}}` - Membres en ligne
- `{{MEMBER_COUNT}}` - Total membres

---

### 🗺️ TerritoryPage.java

Stats:
- `{{CLAIM_COUNT}}` - Total chunks
- `{{MAX_CLAIMS}}` - Max claims
- `{{CLAIM_VALUE}}` - Valeur en power

Pour chaque claim (i=1 à 4):
- `{{CLAIM_i_COORDS}}` - Coordonnées
- `{{CLAIM_i_TYPE}}` - Type de territoire

---

### 🤝 AlliancesPage.java

Pour chaque alliance (i=1 à 3):
- `{{ALLIANCE_i_NAME}}` - Nom faction alliée
- `{{ALLIANCE_i_MEMBER_COUNT}}` - Membres de l'alliance

---

### 📦 ChestPage.java

**Aucun placeholder.** Grille d'inventaire 9x4.

---

### ⭐ LevelPage.java

| Placeholder | Description | Type |
|------------|-------------|------|
| `{{FACTION_LEVEL}}` | Niveau | Integer |
| `{{LEVEL_TITLE}}` | Titre du niveau | String |
| `{{MAX_CLAIMS}}` | Claims max | Integer |
| `{{MAX_MEMBERS}}` | Membres max | Integer |
| `{{XP_CURRENT}}` | XP actuel | Integer |
| `{{XP_MAX}}` | XP max | Integer |

Pour chaque benefit (i=1 à 3):
- `{{BENEFIT_i_DESC}}` - Description bénéfice

**Valeur hardcodée:**
- Ligne ~108: `int xpPercent = 75;` → Calculer

---

### 🎯 QuestsPage.java

Pour chaque quête daily (i=1 à 3):
- `{{QUEST_i_NAME}}` - Nom de la quête

Pour chaque quête weekly (i=1 à 2):
- `{{WEEKLY_QUEST_i_NAME}}` - Nom de la quête

**Valeurs hardcodées:**
- Lignes ~52, ~70: `int progress = 50;` et `30;` → Remplacer par données

---

### 🛒 AdminShopPage.java

Pour chaque item (i=1 à 6):
- `{{SHOP_ITEM_i_NAME}}` - Nom de l'item
- `{{SHOP_ITEM_i_PRICE}}` - Prix

---

### ⚙️ SettingsFactionPage.java

- `{{FACTION_NAME}}` - Nom (champ input)
- `{{FACTION_DESCRIPTION}}` - Description (champ input)

**Valeur hardcodée:**
- Ligne ~37: `boolean toggleState = true;` → Remplacer par données

---

### 🔐 SettingsPermissionsPage.java

**Valeur hardcodée:**
- Ligne ~38: `boolean checked = true;` → Remplacer par logique de permissions

---

## 🛠️ COMMENT MODIFIER

### Option 1: Passer des données au constructeur

Modifiez `FactionMenuScreen.java`:
```java
public FactionMenuScreen(FactionMenu container, Inventory inventory, Component text, FactionData factionData) {
    super(container, inventory, text);
    // ...

    // Passer les données aux pages
    pages.put(PageType.OVERVIEW, new OverviewPage(font, factionData));
    pages.put(PageType.MEMBERS, new MembersPage(font, factionData));
    // etc.
}
```

Puis dans chaque page, ajoutez un champ:
```java
public class OverviewPage extends FactionPage {
    private final FactionData factionData;

    public OverviewPage(Font font, FactionData factionData) {
        super(font);
        this.factionData = factionData;
    }

    @Override
    public void render(...) {
        // Au lieu de:
        g.drawString(font, "{{FACTION_NAME}}", ...);

        // Utiliser:
        g.drawString(font, factionData.getName(), ...);
    }
}
```

### Option 2: Méthode template simple

Créez une méthode helper dans chaque page:
```java
private String fillTemplate(String template) {
    return template
        .replace("{{FACTION_NAME}}", factionData.getName())
        .replace("{{MEMBER_COUNT}}", String.valueOf(factionData.getMemberCount()))
        // etc.
}
```

Puis:
```java
g.drawString(font, fillTemplate("{{FACTION_NAME}}"), x, y, color);
```

---

## ✅ CHECKLIST

- [ ] Remplacer tous les `{{PLACEHOLDER}}` dans les 10 classes de pages
- [ ] Remplacer `powerPercent = 75` dans FactionMenuScreen.java
- [ ] Remplacer `xpPercent = 75` dans LevelPage.java
- [ ] Remplacer `progress = 50/30` dans QuestsPage.java
- [ ] Remplacer `toggleState = true` dans SettingsFactionPage.java
- [ ] Remplacer `checked = true` dans SettingsPermissionsPage.java
- [ ] Tester sur une fenêtre 800x600 (tout doit être visible)
- [ ] Tester la navigation entre les 10 pages

---

**Architecture propre et modulaire - Chaque page dans sa propre classe!**
