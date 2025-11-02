package fr.eriniumgroup.erinium_faction.features.jobs.type;

/**
 * Types d'actions qui peuvent donner de l'XP
 */
public enum ActionType {
    /** Casser un bloc - nécessite block ID */
    BREAK,

    /** Placer un bloc - nécessite block ID */
    PLACE,

    /** Craft un item - nécessite item ID */
    CRAFT,

    /** Smelter un item - nécessite item ID */
    SMELT,

    /** Tuer une entité - nécessite entity ID */
    KILL,

    /** Pêcher un item - nécessite item ID */
    FISHING,

    /** Boire un item - nécessite item ID */
    DRINK,

    /** Manger un item - nécessite item ID */
    EAT,

    /** Utiliser un item - nécessite item ID */
    USE,

    /** Lancer un item (projectile) - nécessite item ID */
    THROW,

    /** Autre action - nécessite item ID */
    OTHER,

    /** Action personnalisée - nécessite custom ID (string) */
    CUSTOM
}
