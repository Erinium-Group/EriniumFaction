# 🚀 Rocket Maker - Recipe Export System

## Guide d'Utilisation

### ✨ Comment Exporter une Recette

1. **Lance le jeu** avec `./gradlew runClient`

2. **Place le Rocket Maker** dans le monde

3. **Ouvre l'interface** du Rocket Maker (clic droit)

4. **Place les items** dans la grille de craft (63 slots en forme de fusée)
   - Seuls les slots avec des items seront inclus dans la recette
   - Les slots vides seront automatiquement requis comme vides dans le pattern

5. **Clique sur le bouton "Export Recipe"** en haut à droite de l'interface

6. **Le fichier JSON sera créé** dans : `run/recipes/rocket_making_exported_YYYYMMDD_HHMMSS.json`

7. **Tu recevras un message** en jeu avec le chemin complet du fichier

### 📝 Format du Fichier Exporté

```json
{
  "type": "erinium_faction:rocket_making",
  "pattern": {
    "0": { "item": "erinium_faction:steel_hull_panel" },
    "1": { "item": "erinium_faction:crew_module" },
    "5": { "item": "minecraft:diamond" }
  },
  "result": {
    "id": "minecraft:air",
    "count": 1
  }
}
```

### ⚙️ Après l'Export

1. **Ouvre le fichier** généré dans `run/recipes/`

2. **Change le résultat** manuellement :
   ```json
   "result": {
     "id": "erinium_faction:rocket_spawn_egg",  ← Change ici
     "count": 1
   }
   ```

3. **Copie le fichier** dans :
   ```
   src/main/resources/data/erinium_faction/recipe/rocket_making_<nom>.json
   ```

4. **Rebuild le mod** : `./gradlew build`

5. **La recette est maintenant active** ! 🎉

### 🗺️ Disposition des Slots

La grille a 63 slots organisés en 9 lignes (forme de fusée) :

```
Slots 0-1:    Ligne 1 (2 slots)   - Sommet
Slots 2-5:    Ligne 2 (4 slots)
Slots 6-12:   Ligne 3 (7 slots)
Slots 13-20:  Ligne 4 (8 slots)
Slots 21-27:  Ligne 5 (7 slots)   - Corps
Slots 28-34:  Ligne 6 (7 slots)
Slots 35-42:  Ligne 7 (8 slots)
Slots 43-51:  Ligne 8 (9 slots)
Slots 52-62:  Ligne 9 (12 slots)  - Base/Moteurs
```

### 💡 Astuces

- **Tags** : Tu peux modifier manuellement pour utiliser des tags au lieu d'items :
  ```json
  "5": { "tag": "c:ingots/iron" }
  ```

- **Nommage** : Utilise des noms descriptifs pour tes fichiers :
  - `rocket_making_basic.json`
  - `rocket_making_advanced.json`
  - `rocket_making_creative.json`

- **Versioning** : Le timestamp dans le nom évite d'écraser les exports précédents

### 🔧 Retirer le Bouton (Pour Production)

Quand tu n'as plus besoin du bouton d'export, commente ces lignes dans `RocketMakerScreen.java` (lignes 33-42) :

```java
// // Add export button (temporary for development)
// int buttonX = this.leftPos + this.imageWidth - 90;
// int buttonY = this.topPos + 5;
// this.addRenderableWidget(Button.builder(
//         Component.literal("Export Recipe"),
//         button -> {
//             PacketDistributor.sendToServer(new ExportRecipePacket(this.menu.getBlockEntity().getBlockPos()));
//         }
// ).bounds(buttonX, buttonY, 85, 20).build());
```

---

**Happy Crafting! 🚀**
