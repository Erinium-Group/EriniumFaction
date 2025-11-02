package fr.eriniumgroup.erinium_faction.jobs.config;

/**
 * Types d'éléments qui peuvent être débloqués
 */
public enum UnlockType {
    /** Débloquer un item - nécessite item ID */
    ITEM,

    /** Débloquer un bloc - nécessite block ID */
    BLOCK,

    /** Débloquer une dimension - nécessite dimension ID */
    DIMENSION,

    /** Débloquage personnalisé - nécessite custom ID (string) */
    CUSTOM
}
