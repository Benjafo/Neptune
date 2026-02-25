package neptune.neptune.challenge;

public enum ChallengeCategory {
    EXPLORATION("Exploration"),
    COLLECTION("Collection"),
    COMBAT("Combat"),
    EFFICIENCY("Efficiency"),
    MILESTONES("Milestones");

    private final String displayName;

    ChallengeCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
