package fr.eriniumgroup.erinium_faction.gui.widgets;

import fr.eriniumgroup.erinium_faction.features.jobs.type.JobType;

/**
 * Représente une notification toast pour un job
 */
public class JobToast {
    private final JobType jobType;
    private final String jobName;
    private int level;
    private int xpGained;
    private String actionDescription;
    private int currentXp;
    private int xpToNextLevel;
    private boolean isLevelUp;
    private long creationTime;
    private long lastUpdateTime; // Dernier moment où le toast a été mis à jour
    private AnimationState animationState;
    private long stateChangeTime;
    private static final long DURATION_MS = 3000; // 3 secondes
    private static final long DROP_DURATION_MS = 300; // Animation d'arrivée
    private static final long SLIDE_DURATION_MS = 200; // Animation de départ

    public enum AnimationState {
        ENTERING,  // Tombant depuis le haut
        VISIBLE,   // Visible normalement
        LEAVING    // Glissant vers la droite
    }

    public JobToast(JobType jobType, String jobName, int level, int xpGained, String actionDescription, int currentXp, int xpToNextLevel, boolean isLevelUp) {
        this.jobType = jobType;
        this.jobName = jobName;
        this.level = level;
        this.xpGained = xpGained;
        this.actionDescription = actionDescription;
        this.currentXp = currentXp;
        this.xpToNextLevel = xpToNextLevel;
        this.isLevelUp = isLevelUp;
        this.creationTime = System.currentTimeMillis();
        this.lastUpdateTime = System.currentTimeMillis();
        this.animationState = AnimationState.ENTERING;
        this.stateChangeTime = System.currentTimeMillis();
    }

    /**
     * Met à jour les valeurs du toast avec de nouvelles données
     * Réinitialise le timer de visibilité
     */
    public void updateValues(int level, int xpGained, String actionDescription, int currentXp, int xpToNextLevel, boolean isLevelUp) {
        this.level = level;
        this.xpGained = xpGained;
        this.actionDescription = actionDescription;
        this.currentXp = currentXp;
        this.xpToNextLevel = xpToNextLevel;
        this.isLevelUp = isLevelUp;
        this.lastUpdateTime = System.currentTimeMillis();

        // Remettre à VISIBLE si le toast était en train de partir
        if (animationState == AnimationState.LEAVING) {
            animationState = AnimationState.VISIBLE;
            stateChangeTime = System.currentTimeMillis();
        }
    }

    public JobType getJobType() {
        return jobType;
    }

    public String getJobName() {
        return jobName;
    }

    public int getLevel() {
        return level;
    }

    public int getXpGained() {
        return xpGained;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public int getCurrentXp() {
        return currentXp;
    }

    public int getXpToNextLevel() {
        return xpToNextLevel;
    }

    public boolean isLevelUp() {
        return isLevelUp;
    }

    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Met à jour l'état d'animation basé sur le temps
     * Utilise lastUpdateTime pour le timer de visibilité
     */
    public void updateState() {
        long elapsed = System.currentTimeMillis() - lastUpdateTime;
        long stateElapsed = System.currentTimeMillis() - stateChangeTime;

        if (animationState == AnimationState.ENTERING && stateElapsed >= DROP_DURATION_MS) {
            animationState = AnimationState.VISIBLE;
            stateChangeTime = System.currentTimeMillis();
        } else if (animationState == AnimationState.VISIBLE && elapsed >= DURATION_MS - SLIDE_DURATION_MS) {
            animationState = AnimationState.LEAVING;
            stateChangeTime = System.currentTimeMillis();
        }
    }

    /**
     * Démarre l'animation de départ immédiatement
     */
    public void startLeaving() {
        if (animationState != AnimationState.LEAVING) {
            animationState = AnimationState.LEAVING;
            stateChangeTime = System.currentTimeMillis();
        }
    }

    /**
     * Vérifie si le toast est encore visible
     */
    public boolean isVisible() {
        if (animationState == AnimationState.LEAVING) {
            long elapsed = System.currentTimeMillis() - stateChangeTime;
            return elapsed < SLIDE_DURATION_MS;
        }
        return true;
    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    /**
     * Obtient le pourcentage de progression de l'animation actuelle (0.0 à 1.0)
     */
    public float getAnimationProgress() {
        long elapsed = System.currentTimeMillis() - stateChangeTime;
        float progress;

        switch (animationState) {
            case ENTERING:
                progress = Math.min(1.0f, elapsed / (float) DROP_DURATION_MS);
                break;
            case LEAVING:
                progress = Math.min(1.0f, elapsed / (float) SLIDE_DURATION_MS);
                break;
            default:
                progress = 1.0f;
        }

        return progress;
    }

    /**
     * Obtient le pourcentage de progression XP
     */
    public float getProgressPercentage() {
        if (xpToNextLevel <= 0) return 1.0f;
        return Math.min(1.0f, (float) currentXp / (float) xpToNextLevel);
    }

    /**
     * Obtient l'opacité du toast
     */
    public float getOpacity() {
        return 1.0f;
    }
}
