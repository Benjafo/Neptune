package neptune.neptune.relic;

/**
 * All relic sets in the game.
 * Major sets have 15 relics and grant gameplay bonuses.
 * Minor sets have 4-6 relics and provide collection goals.
 */
public enum RelicSet {
    // Major Sets (15 relics each)
    THE_BUILDERS(true, 15, "The Builders", "Ancient civilization artifacts"),
    THE_VOID(true, 15, "The Void", "The dimension's nature and energy"),
    THE_INHABITANTS(true, 15, "The Inhabitants", "Endermen, shulkers, and dragon"),
    THE_EXPLORERS(true, 15, "The Explorers", "Those who came before"),

    // Minor Sets
    VOID_CONSTELLATIONS(false, 6, "Void Constellations", "Star patterns seen from the End"),
    SHULKER_ORIGINS(false, 4, "Shulker Origins", "How shulkers came to be"),
    DRAGONS_LEGACY(false, 5, "Dragon's Legacy", "Relics connected to the dragon"),
    CHORUS_MUTATIONS(false, 4, "Chorus Mutations", "Strange chorus plant growths"),
    END_CITY_SEALS(false, 6, "End City Seals", "Official markers from different cities"),
    RITUAL_OBJECTS(false, 5, "Ritual Objects", "Ceremonial items"),
    CARTOGRAPHERS_TOOLS(false, 5, "Cartographer's Tools", "Ancient mapping equipment"),
    WARRIORS_REMNANTS(false, 5, "Warrior's Remnants", "Ancient weapons and armor fragments"),

    // Standalone
    STANDALONE(false, 20, "Standalone", "Unique artifacts");

    private final boolean major;
    private final int size;
    private final String displayName;
    private final String description;

    RelicSet(boolean major, int size, String displayName, String description) {
        this.major = major;
        this.size = size;
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isMajor() { return major; }
    public boolean isMinor() { return !major && this != STANDALONE; }
    public int getSize() { return size; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
