# Table de Progression XP - Système de Métiers

## Formule
```
XP requis = (1000 + 178.853 * LEVEL^1.5) * (LEVEL^1.005)
```

## Niveaux maximum
- **Niveau max**: 100
- Tous les métiers utilisent la même formule

## Exemples de progression

| Niveau | XP Requis | XP Total Cumulé |
|--------|-----------|-----------------|
| 1 → 2  | 1,179     | 1,179           |
| 2 → 3  | 2,364     | 3,543           |
| 3 → 4  | 3,560     | 7,103           |
| 4 → 5  | 4,768     | 11,871          |
| 5 → 6  | 5,988     | 17,859          |
| 10 → 11 | 12,739   | ~100,000        |
| 15 → 16 | 20,584   | ~250,000        |
| 20 → 21 | 29,564   | ~500,000        |
| 25 → 26 | 39,718   | ~850,000        |
| 30 → 31 | 51,078   | ~1,300,000      |
| 40 → 41 | 78,126   | ~2,700,000      |
| 50 → 51 | 111,041  | ~5,000,000      |
| 60 → 61 | 150,428  | ~8,500,000      |
| 70 → 71 | 196,804  | ~13,500,000     |
| 80 → 81 | 251,053  | ~20,000,000     |
| 90 → 91 | 313,799  | ~28,500,000     |
| 99 → 100| 392,687  | ~40,000,000     |

## Calculs détaillés

### Niveau 1 → 2
```
base = 1000 + (178.853 * 1^1.5) = 1000 + 178.853 = 1178.853
multiplier = 1^1.005 = 1.005
XP = 1178.853 * 1.005 = 1,184 XP
```

### Niveau 50 → 51
```
base = 1000 + (178.853 * 50^1.5) = 1000 + (178.853 * 353.553) = 64,227
multiplier = 50^1.005 = 1.728
XP = 64,227 * 1.728 = 111,041 XP
```

### Niveau 100 (MAX)
```
Au niveau 100, getExperienceForNextLevel() retourne Integer.MAX_VALUE
L'XP ne peut plus augmenter et reste à 0
```

## Caractéristiques de la courbe

1. **Progression exponentielle modérée**
   - Les premiers niveaux sont rapides (1-10)
   - La difficulté augmente progressivement
   - Les derniers niveaux (90-100) sont très exigeants

2. **Équilibrage**
   - Niveau 1-20: Rapide, pour familiariser le joueur
   - Niveau 20-50: Progression normale
   - Niveau 50-75: Devient difficile
   - Niveau 75-100: Très difficile, réservé aux joueurs dévoués

3. **Total estimé**
   - Du niveau 1 au niveau 100: ~40 millions d'XP
   - Chaque niveau prend environ 30-50% de temps en plus que le précédent

## Recommandations de gain XP

Pour un équilibrage optimal:

| Action | XP suggéré | Temps pour 1 niveau (niveau 50) |
|--------|-----------|----------------------------------|
| Mineur - Stone | 5 XP | ~22,000 blocs |
| Mineur - Coal | 15 XP | ~7,400 blocs |
| Mineur - Iron | 30 XP | ~3,700 blocs |
| Mineur - Diamond | 100 XP | ~1,100 blocs |
| Bûcheron - Log | 10 XP | ~11,000 logs |
| Chasseur - Zombie | 20 XP | ~5,500 kills |
| Chasseur - Boss | 1000 XP | ~111 kills |
| Pêcheur - Poisson | 15 XP | ~7,400 poissons |
| Fermier - Récolte | 5 XP | ~22,000 récoltes |

## Implémentation

```java
// Exemple: Donner de l'XP
int levelsGained = JobsManager.addJobExperience(player, JobType.MINER, 100);

// Vérifier le niveau max
if (JobsManager.getJobLevel(player, JobType.MINER) >= JobsData.MAX_LEVEL) {
    player.sendSystemMessage(Component.literal("Max level atteint!"));
}

// Calculer XP pour niveau suivant
int xpNeeded = JobsManager.getExperienceForNextLevel(currentLevel);
```
