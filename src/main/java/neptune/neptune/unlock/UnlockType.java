package neptune.neptune.unlock;

/**
 * All 13 unlocks in the progression tree.
 * Each unlock has: branch, tier, essence cost, display name, and description.
 */
public enum UnlockType {
    // Navigation Branch
    NAVIGATION_T1(UnlockBranch.NAVIGATION, 1, 75, "Cartographer's Basics",
            "Unlocks the map system and exploration tracking"),
    NAVIGATION_T2(UnlockBranch.NAVIGATION, 2, 250, "Wayfinder",
            "Unlocks minimap display and compass item"),
    NAVIGATION_T3(UnlockBranch.NAVIGATION, 3, 600, "Voidwalker",
            "Map shows city hints, unlocks Void Compass"),
    NAVIGATION_T4(UnlockBranch.NAVIGATION, 4, 1200, "End Cartographer",
            "Waypoint system with teleportation"),

    // Processing Branch
    PROCESSING_T1(UnlockBranch.PROCESSING, 1, 75, "Salvager",
            "Gear sells at full value, unlocks Breakdown Table"),
    PROCESSING_T2(UnlockBranch.PROCESSING, 2, 300, "Enchantment Studies",
            "Extract and apply enchantment shards"),
    PROCESSING_T3(UnlockBranch.PROCESSING, 3, 750, "Void Synthesis",
            "Craft Reinforced Elytra, Void Pouch, Ender Magnet"),
    PROCESSING_T4(UnlockBranch.PROCESSING, 4, 1500, "Master Artificer",
            "Elytra boosters, portable ender chest, relic infusion"),

    // Catalog Branch
    CATALOG_T1(UnlockBranch.CATALOG, 1, 75, "Collector",
            "Unlocks relic journal, relics sell for +50%"),
    CATALOG_T2(UnlockBranch.CATALOG, 2, 300, "Archivist",
            "Relic detector, journal hints, map integration"),
    CATALOG_T3(UnlockBranch.CATALOG, 3, 750, "Curator",
            "Display cases, set bonuses, region completion bonus"),
    CATALOG_T4(UnlockBranch.CATALOG, 4, 1500, "Grand Archivist",
            "Relic transmutation, legendary quests"),

    // Endgame
    ENDGAME(null, 5, 3000, "Void Master",
            "Void flight, prestige system, cosmetic rewards");

    private final UnlockBranch branch;
    private final int tier;
    private final int essenceCost;
    private final String displayName;
    private final String description;

    UnlockType(UnlockBranch branch, int tier, int essenceCost, String displayName, String description) {
        this.branch = branch;
        this.tier = tier;
        this.essenceCost = essenceCost;
        this.displayName = displayName;
        this.description = description;
    }

    public UnlockBranch getBranch() { return branch; }
    public int getTier() { return tier; }
    public int getEssenceCost() { return essenceCost; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /**
     * Get the previous tier in the same branch (null if T1 or endgame).
     */
    public UnlockType getPreviousTier() {
        if (branch == null || tier <= 1) return null;
        for (UnlockType type : values()) {
            if (type.branch == this.branch && type.tier == this.tier - 1) {
                return type;
            }
        }
        return null;
    }
}
