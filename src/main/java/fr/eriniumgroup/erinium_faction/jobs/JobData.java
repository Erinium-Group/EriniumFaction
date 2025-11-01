package fr.eriniumgroup.erinium_faction.jobs;

/**
 * Données d'un métier pour un joueur
 */
public class JobData {
    private final JobType type;
    private int level;
    private int experience;
    private int experienceToNextLevel;

    public JobData(JobType type) {
        this.type = type;
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = calculateExpForLevel(2);
    }

    // Exemple de données pour la démo
    public JobData(JobType type, int level, int experience, int experienceToNextLevel) {
        this.type = type;
        this.level = level;
        this.experience = experience;
        this.experienceToNextLevel = experienceToNextLevel;
    }

    public JobType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    public float getExperienceProgress() {
        if (experienceToNextLevel == 0) return 0f;
        return (float) experience / experienceToNextLevel;
    }

    public int getExperiencePercentage() {
        return (int) (getExperienceProgress() * 100);
    }

    private int calculateExpForLevel(int level) {
        return (int) (100 * Math.pow(level, 1.5));
    }

    // Créer des données d'exemple
    public static JobData[] createExampleData() {
        return new JobData[] {
            new JobData(JobType.MINER, 1, 0, 100),
            new JobData(JobType.LUMBERJACK, 15, 750, 1000),
            new JobData(JobType.HUNTER, 8, 200, 500),
            new JobData(JobType.FISHER, 23, 1100, 1500),
            new JobData(JobType.FARMER, 42, 3300, 4000),
            new JobData(JobType.WIZARD, 5, 80, 300)
        };
    }
}
