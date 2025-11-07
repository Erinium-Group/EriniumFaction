# 📋 Système de Nameplate Personnalisé - Documentation

## 🎨 Vue d'ensemble

Le système de nameplate personnalisé remplace le nom par défaut des joueurs par un élément graphique modulaire et configurable. Inspiré du mod "Neat", il affiche :

- **Nom du joueur**
- **Barre de vie** avec texte et couleur dynamique
- **Nom de faction** (optionnel)
- **Niveau du joueur** (optionnel)

Le système utilise un **Mixin** pour injecter du code directement dans le renderer d'entités de Minecraft, garantissant un rendu 3D parfaitement intégré qui s'adapte à la distance et à la caméra.

---

## ⚙️ Configuration Client

Le fichier `erinium_faction-client.toml` contient toutes les options de personnalisation :

### Options d'affichage

```toml
[nameplates]
    # Afficher le nom de faction au-dessus des joueurs
    showFaction = true

    # Afficher le niveau du joueur
    showLevel = true

    # Largeur maximale de la nameplate en pixels
    maxWidth = 80
```

### Couleurs personnalisables

Toutes les couleurs sont au format **ARGB hexadécimal** (ex: `0xAARRGGBB`)

```toml
[nameplates]
    # Couleur de fond (ARGB hex, ex: 0xAA000000)
    backgroundColor = -1442840576  # 0xAA000000 - Noir semi-transparent

    # Couleur du nom du joueur
    nameColor = -1  # 0xFFFFFFFF - Blanc

    # Couleur de la barre de vie
    healthBarColor = -16711936  # 0xFF00FF00 - Vert

    # Couleur de fond de la barre de vie
    healthBarBackgroundColor = -13421773  # 0xFF333333 - Gris foncé

    # Couleur du texte de vie
    healthTextColor = -1  # 0xFFFFFFFF - Blanc

    # Couleur du nom de faction
    factionColor = -43520  # 0xFFFFAA00 - Orange

    # Couleur du niveau
    levelColor = -11141291  # 0xFF55FF55 - Vert clair
```

---

## 🏗️ Architecture - Système de Composants

### Pourquoi un Mixin ?

Après plusieurs tentatives (événements Forge, overlay GUI), le système utilise un **Mixin** qui injecte directement dans `EntityRenderer.renderNameTag()`. Cette approche garantit :

- ✅ Rendu 3D parfaitement intégré
- ✅ Échelle automatique avec la distance
- ✅ Rotation avec la caméra
- ✅ Aucun z-fighting (scintillement)
- ✅ Buffers correctement flushés par Minecraft

### Fichiers principaux

1. **`EntityRendererMixin.java`** - Mixin qui intercepte le rendu des nameplates
2. **`NameplateMixinRenderer.java`** - Logique de rendu avec système de composants
3. **`erinium_faction.mixins.json`** - Configuration du mixin

### Composants actuels

Le système affiche **4 composants** (de haut en bas) :

1. **Niveau** - Petit texte (50% scale) "Niv. X"
2. **Faction** - Texte moyen (70% scale) avec nom de faction
3. **Nom** - Nom du joueur (100% scale)
4. **Barre de vie** - Barre graphique avec texte "X.X / X.X"

---

## ➕ Ajouter un Nouveau Composant

### C'est ultra simple ! Seulement 2 étapes :

#### Étape 1 : Créer une méthode de rendu

Dans `NameplateMixinRenderer.java`, ajoutez une méthode statique :

```java
/**
 * COMPOSANT: Nombre de kills du joueur
 */
private static void renderKillCount(PoseStack poseStack, MultiBufferSource bufferSource,
                                    Player player, int y, int maxWidth, Font font, int light) {
    int kills = PlayerKillsCache.getKills(player.getUUID());

    poseStack.pushPose();
    poseStack.scale(0.6f, 0.6f, 0.6f);

    String killText = "⚔ " + kills + " kills";
    int killColor = 0xFFFF5555; // Rouge clair
    int textWidth = font.width(killText);

    font.drawInBatch(killText, -textWidth / 2.0f, y / 0.6f, killColor, false,
        poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);

    poseStack.popPose();
}
```

#### Étape 2 : L'ajouter dans setupComponents()

Dans la méthode `setupComponents()`, ajoutez une seule ligne :

```java
private static List<NameplateElement> setupComponents(Player player, Font font, int maxWidth) {
    List<NameplateElement> elements = new ArrayList<>();

    String factionName = PlayerFactionCache.getFactionName(player.getUUID());
    int level = PlayerLevelCache.getLevel(player.getUUID());
    int kills = PlayerKillsCache.getKills(player.getUUID()); // Récupérer les données

    // ORDRE D'AFFICHAGE (de haut en bas)
    elements.add(new NameplateElement("level", 7,
        EFClientConfig.NAMEPLATE_SHOW_LEVEL.get() && level > 0,
        NameplateMixinRenderer::renderLevel));

    elements.add(new NameplateElement("faction", 8,
        EFClientConfig.NAMEPLATE_SHOW_FACTION.get() && factionName != null && !factionName.isEmpty(),
        NameplateMixinRenderer::renderFaction));

    elements.add(new NameplateElement("name", font.lineHeight + 2,
        true,
        NameplateMixinRenderer::renderName));

    // ← NOUVEAU COMPOSANT
    elements.add(new NameplateElement("kills", 6,
        kills > 0,  // Condition d'affichage
        NameplateMixinRenderer::renderKillCount));

    elements.add(new NameplateElement("health", 10,
        true,
        NameplateMixinRenderer::renderHealthBar));

    return elements;
}
```

### Paramètres de NameplateElement

```java
new NameplateElement(
    "id",           // Identifiant unique du composant
    hauteur,        // Hauteur en pixels (avec le scale actuel)
    condition,      // true = afficher, false = masquer
    methodeRendu    // Référence à votre méthode (MonRenderer::maMethode)
)
```

### C'est tout ! 🎉

Le système s'occupe automatiquement de :
- Calculer la hauteur totale du fond
- Positionner chaque composant verticalement
- Afficher uniquement les composants dont la condition est `true`

---

## 🔄 Changer l'Ordre Vertical

**Super simple !** Il suffit de **réorganiser les lignes** dans `setupComponents()`.

### Exemple : Mettre la santé en haut

```java
// AVANT (santé en bas)
elements.add(new NameplateElement("level", ...));
elements.add(new NameplateElement("faction", ...));
elements.add(new NameplateElement("name", ...));
elements.add(new NameplateElement("health", ...));  // ← En bas

// APRÈS (santé en haut)
elements.add(new NameplateElement("health", ...));  // ← En haut maintenant !
elements.add(new NameplateElement("level", ...));
elements.add(new NameplateElement("faction", ...));
elements.add(new NameplateElement("name", ...));
```

L'ordre dans la liste = l'ordre d'affichage de **haut en bas** ! 📏

---

## 🎨 Exemples de Composants

### Composant avec texte simple

```java
private static void renderStatus(PoseStack poseStack, MultiBufferSource bufferSource,
                                 Player player, int y, int maxWidth, Font font, int light) {
    String status = "🛡 Protégé";
    int color = 0xFFFFFF00; // Jaune
    int textWidth = font.width(status);

    font.drawInBatch(status, -textWidth / 2.0f, y, color, false,
        poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
}
```

### Composant avec barre de progression

```java
private static void renderXpBar(PoseStack poseStack, MultiBufferSource bufferSource,
                                Player player, int y, int maxWidth, Font font, int light) {
    float xpPercent = player.experienceProgress;

    int barWidth = maxWidth - 4;
    int barX1 = -barWidth / 2;
    int barX2 = barWidth / 2;
    int barY1 = y;
    int barY2 = y + 4;

    // Fond de la barre
    drawBackground(poseStack, bufferSource, barX1, barY1, barX2, barY2, 0xFF333333, light);

    // Barre remplie
    int filledWidth = (int) (barWidth * xpPercent);
    if (filledWidth > 0) {
        drawBackground(poseStack, bufferSource, barX1, barY1, barX1 + filledWidth, barY2, 0xFF00FFAA, light);
    }
}
```

### Composant avec texte scalé

```java
private static void renderSmallInfo(PoseStack poseStack, MultiBufferSource bufferSource,
                                    Player player, int y, int maxWidth, Font font, int light) {
    poseStack.pushPose();
    poseStack.scale(0.5f, 0.5f, 0.5f); // Texte 2x plus petit

    String info = "Info importante";
    int color = 0xFFAAAAAA;
    int textWidth = font.width(info);

    // IMPORTANT: Diviser y par le scale pour compenser
    font.drawInBatch(info, -textWidth / 2.0f, y / 0.5f, color, false,
        poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);

    poseStack.popPose();
}
```

---

## 🛠️ Méthode Utilitaire

### Dessiner un rectangle de fond

Utilisez la méthode `drawBackground()` déjà présente :

```java
drawBackground(poseStack, bufferSource, x1, y1, x2, y2, couleur, light);
```

**Paramètres** :
- `x1, y1` - Coin supérieur gauche
- `x2, y2` - Coin inférieur droit
- `couleur` - Format ARGB (0xAARRGGBB)
- `light` - Niveau de lumière (utilisez le paramètre `light` reçu)

**Exemple** :
```java
// Rectangle rouge semi-transparent de 50x10 pixels, centré
int color = 0xAAFF0000; // Rouge semi-transparent
drawBackground(poseStack, bufferSource, -25, y, 25, y + 10, color, light);
```

---

## 📡 Système de Synchronisation

### Comment fonctionnent les données réseau ?

Le système utilise un **packet custom** pour synchroniser les données du serveur vers tous les clients :

1. **Serveur** : Envoie les données (faction, level, etc.) via `SyncPlayerNameplateDataPacket`
2. **Client** : Reçoit le packet et met à jour les caches locaux (`PlayerFactionCache`, `PlayerLevelCache`)
3. **Renderer** : Lit les caches pour afficher les informations

### Ajouter une nouvelle donnée synchronisée

#### 1. Créer un cache côté client

```java
package fr.eriniumgroup.erinium_faction.client.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerKillsCache {
    private static final Map<UUID, Integer> kills = new ConcurrentHashMap<>();

    public static void setKills(UUID playerUUID, int killCount) {
        kills.put(playerUUID, killCount);
    }

    public static int getKills(UUID playerUUID) {
        return kills.getOrDefault(playerUUID, 0);
    }

    public static void clear() {
        kills.clear();
    }
}
```

#### 2. Modifier le packet de synchronisation

Dans `SyncPlayerNameplateDataPacket.java` :

```java
public record SyncPlayerNameplateDataPacket(UUID playerUUID,
                                            String factionName,
                                            int level,
                                            int kills) // ← NOUVEAU
        implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerNameplateDataPacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, msg) -> {
                buf.writeUUID(msg.playerUUID);
                buf.writeUtf(msg.factionName, 256);
                buf.writeVarInt(msg.level);
                buf.writeVarInt(msg.kills); // ← NOUVEAU
            },
            buf -> new SyncPlayerNameplateDataPacket(
                buf.readUUID(),
                buf.readUtf(256),
                buf.readVarInt(),
                buf.readVarInt() // ← NOUVEAU
            )
        );
}
```

#### 3. Mettre à jour le handler client

Dans `ClientNameplateHandler.java` :

```java
@OnlyIn(Dist.CLIENT)
public static void handleClientSide(SyncPlayerNameplateDataPacket packet, IPayloadContext context) {
    context.enqueueWork(() -> {
        PlayerFactionCache.setFactionName(packet.playerUUID(), packet.factionName());
        PlayerLevelCache.setLevel(packet.playerUUID(), packet.level());
        PlayerKillsCache.setKills(packet.playerUUID(), packet.kills()); // ← NOUVEAU
    });
}
```

#### 4. Envoyer les données depuis le serveur

Dans `NameplatePacketHandler.java` :

```java
public static void syncPlayerNameplateData(ServerPlayer player) {
    PlayerLevelData levelData = player.getData(PlayerLevelAttachments.PLAYER_LEVEL_DATA);
    Faction faction = FactionManager.getFactionOf(player.getUUID());
    int kills = getPlayerKills(player); // ← RÉCUPÉRER VOS DONNÉES

    String factionName = faction != null ? faction.getName() : "";
    int level = levelData.getLevel();

    SyncPlayerNameplateDataPacket packet = new SyncPlayerNameplateDataPacket(
        player.getUUID(),
        factionName,
        level,
        kills // ← NOUVEAU
    );

    PacketDistributor.sendToPlayersTrackingEntity(player, packet);
    PacketDistributor.sendToPlayer(player, packet);
}
```

---

## 🎯 Points Techniques Importants

### Scaling et coordonnées Y

Quand vous utilisez `poseStack.scale()`, vous devez **diviser les coordonnées Y** par le facteur de scale :

```java
poseStack.scale(0.5f, 0.5f, 0.5f);
// Pour dessiner à y=10 dans le système scalé, utilisez y/0.5f
font.drawInBatch(text, x, y / 0.5f, color, ...);
```

### Centrage du texte

Pour centrer un texte horizontalement :

```java
int textWidth = font.width(texte);
float x = -textWidth / 2.0f; // Position X centrée
```

### Couleurs dynamiques

Vous pouvez changer la couleur selon une condition :

```java
int color = baseColor;
if (someCondition) {
    color = 0xFFFF0000; // Rouge
} else if (otherCondition) {
    color = 0xFFFFAA00; // Orange
}
```

### RenderType utilisé

Le système utilise `RenderType.textBackgroundSeeThrough()` qui :
- ✅ Désactive le depth test (pas de z-fighting)
- ✅ Rend toujours derrière le texte
- ✅ Est compatible avec les buffers de Minecraft

**Ne changez pas ce RenderType** sauf si vous savez exactement ce que vous faites !

---

## 🐛 Debugging

### Mon composant ne s'affiche pas

1. **Vérifier la condition** dans `setupComponents()` :
   ```java
   elements.add(new NameplateElement("test", 10,
       true,  // ← Forcer à true pour tester
       NameplateMixinRenderer::renderTest));
   ```

2. **Vérifier la hauteur** (doit être > 0) :
   ```java
   elements.add(new NameplateElement("test", 10,  // ← Hauteur = 10 pixels
       true,
       NameplateMixinRenderer::renderTest));
   ```

3. **Vérifier que les données existent** :
   ```java
   int value = MonCache.getValue(player.getUUID());
   System.out.println("DEBUG: value=" + value); // ← Ajoutez un log temporaire
   ```

### Le texte est mal positionné

- **Trop haut/bas** : Ajustez le paramètre `y` ou le scale
- **Pas centré** : Vérifiez le calcul de `textWidth / 2.0f`
- **Tronqué** : Augmentez la hauteur dans `NameplateElement`

### Les données ne se synchronisent pas

1. Vérifiez que le packet est bien envoyé côté serveur
2. Vérifiez que le handler client est appelé
3. Vérifiez que le cache est bien mis à jour
4. Ajoutez des logs dans `ClientNameplateHandler.handleClientSide()`

---

## ✅ Checklist pour un nouveau composant

- [ ] Créer une méthode `renderMonComposant(...)` dans `NameplateMixinRenderer`
- [ ] Ajouter une ligne dans `setupComponents()` avec l'ID, hauteur, condition et méthode
- [ ] Tester in-game
- [ ] (Optionnel) Ajouter une config dans `EFClientConfig`
- [ ] (Si besoin de données serveur) Créer un cache client
- [ ] (Si besoin de données serveur) Modifier le packet
- [ ] (Si besoin de données serveur) Mettre à jour les handlers
- [ ] Build réussi ✅

---

## 📚 Fichiers Importants

### Rendu
- **`EntityRendererMixin.java`** - Mixin d'injection dans EntityRenderer
- **`NameplateMixinRenderer.java`** - Logique de rendu et composants
- **`erinium_faction.mixins.json`** - Configuration des mixins

### Configuration
- **`EFClientConfig.java`** - Options client (couleurs, visibilité, etc.)

### Synchronisation réseau
- **`SyncPlayerNameplateDataPacket.java`** - Packet de sync serveur→client
- **`NameplatePacketHandler.java`** - Envoi des packets (serveur)
- **`ClientNameplateHandler.java`** - Réception des packets (client, `@OnlyIn(Dist.CLIENT)`)

### Caches client
- **`PlayerFactionCache.java`** - Cache du nom de faction
- **`PlayerLevelCache.java`** - Cache du niveau du joueur

### Events
- **`NameplateEventHandler.java`** - Events serveur (envoi packets)

---

**📌 Système créé par Claude Code pour Erinium Faction**
**Version : 2.0 - Janvier 2025 - Architecture Mixin**
