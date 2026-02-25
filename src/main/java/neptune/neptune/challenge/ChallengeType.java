package neptune.neptune.challenge;

/**
 * All 45 challenges across 5 categories.
 * Each challenge has a target value and description.
 */
public enum ChallengeType {
    // Exploration (11)
    FIRST_STEPS(ChallengeCategory.EXPLORATION, "First Steps", "Visit 10 grids", 10),
    WANDERER(ChallengeCategory.EXPLORATION, "Wanderer", "Visit 50 grids", 50),
    PATHFINDER(ChallengeCategory.EXPLORATION, "Pathfinder", "Visit 150 grids", 150),
    VETERAN_EXPLORER(ChallengeCategory.EXPLORATION, "Veteran Explorer", "Visit 500 grids", 500),
    INTO_THE_VOID(ChallengeCategory.EXPLORATION, "Into the Void", "Travel 10,000 blocks from main island", 10000),
    NO_END_IN_SIGHT(ChallengeCategory.EXPLORATION, "No End in Sight", "Travel 20,000 blocks from main island", 20000),
    EDGE_WALKER(ChallengeCategory.EXPLORATION, "Edge Walker", "Travel 50,000 blocks from main island", 50000),
    CITY_FINDER(ChallengeCategory.EXPLORATION, "City Finder", "Mark 5 cities", 5),
    CITY_HUNTER(ChallengeCategory.EXPLORATION, "City Hunter", "Mark 25 cities", 25),
    CITY_CONQUEROR(ChallengeCategory.EXPLORATION, "City Conqueror", "Mark 75 cities", 75),
    COMPLETIONIST(ChallengeCategory.EXPLORATION, "Completionist", "Mark 150 cities", 150),

    // Collection (11)
    RELIC_HUNTER(ChallengeCategory.COLLECTION, "Relic Hunter", "Collect 10 relics", 10),
    RELIC_SEEKER(ChallengeCategory.COLLECTION, "Relic Seeker", "Collect 30 relics", 30),
    RELIC_HOARDER(ChallengeCategory.COLLECTION, "Relic Hoarder", "Collect 60 relics", 60),
    RELIC_MASTER(ChallengeCategory.COLLECTION, "Relic Master", "Collect 100 relics", 100),
    SET_STARTER(ChallengeCategory.COLLECTION, "Set Starter", "Complete 1 minor set", 1),
    SET_COLLECTOR(ChallengeCategory.COLLECTION, "Set Collector", "Complete 4 minor sets", 4),
    SET_COMPLETIONIST(ChallengeCategory.COLLECTION, "Set Completionist", "Complete all minor sets", 8),
    MAJOR_DISCOVERY(ChallengeCategory.COLLECTION, "Major Discovery", "Complete 1 major set", 1),
    GRAND_CURATOR(ChallengeCategory.COLLECTION, "Grand Curator", "Complete all major sets", 4),
    LUCKY_FIND(ChallengeCategory.COLLECTION, "Lucky Find", "Find a legendary relic", 1),
    LEGENDARY_COLLECTOR(ChallengeCategory.COLLECTION, "Legendary Collector", "Find all 5 legendary relics", 5),

    // Combat (6)
    SHULKER_SLAYER(ChallengeCategory.COMBAT, "Shulker Slayer", "Kill 50 shulkers", 50),
    SHULKER_HUNTER(ChallengeCategory.COMBAT, "Shulker Hunter", "Kill 200 shulkers", 200),
    SHULKER_NEMESIS(ChallengeCategory.COMBAT, "Shulker Nemesis", "Kill 500 shulkers", 500),
    SHELL_COLLECTOR(ChallengeCategory.COMBAT, "Shell Collector", "Collect 100 shulker shells", 100),
    BULLET_DODGER(ChallengeCategory.COMBAT, "Bullet Dodger", "Kill 10 shulkers without being hit", 10),
    ENDERMAN_PACIFIST(ChallengeCategory.COMBAT, "Enderman Pacifist", "Mark 10 cities without killing an enderman", 10),

    // Efficiency (6)
    QUICK_LOOT(ChallengeCategory.EFFICIENCY, "Quick Loot", "Fully loot a city in under 5 minutes", 1),
    MARATHON(ChallengeCategory.EFFICIENCY, "Marathon", "Loot 5 cities without returning to main island", 5),
    MINIMALIST(ChallengeCategory.EFFICIENCY, "Minimalist", "Loot 3 cities with only elytra + rockets + food", 3),
    WEALTHY(ChallengeCategory.EFFICIENCY, "Wealthy", "Accumulate 1,000 essence (lifetime)", 1000),
    RICH(ChallengeCategory.EFFICIENCY, "Rich", "Accumulate 5,000 essence (lifetime)", 5000),
    MOGUL(ChallengeCategory.EFFICIENCY, "Mogul", "Accumulate 15,000 essence (lifetime)", 15000),

    // Milestones (11)
    FIRST_SALE(ChallengeCategory.MILESTONES, "First Sale", "Sell an item to the broker", 1),
    BIG_SPENDER(ChallengeCategory.MILESTONES, "Big Spender", "Spend 500 essence at the broker", 500),
    REGULAR_CUSTOMER(ChallengeCategory.MILESTONES, "Regular Customer", "Spend 2,000 essence at the broker", 2000),
    PATRON(ChallengeCategory.MILESTONES, "Patron", "Spend 10,000 essence at the broker", 10000),
    FULLY_EQUIPPED(ChallengeCategory.MILESTONES, "Fully Equipped", "Purchase every unlock-gated broker item", 1),
    UPGRADE_NOVICE(ChallengeCategory.MILESTONES, "Upgrade Novice", "Unlock any Tier 2", 1),
    UPGRADE_ADEPT(ChallengeCategory.MILESTONES, "Upgrade Adept", "Unlock any Tier 3", 1),
    UPGRADE_MASTER(ChallengeCategory.MILESTONES, "Upgrade Master", "Unlock any Tier 4", 1),
    JACK_OF_ALL_TRADES(ChallengeCategory.MILESTONES, "Jack of All Trades", "Reach Tier 2 in all three branches", 1),
    WELL_ROUNDED(ChallengeCategory.MILESTONES, "Well Rounded", "Reach Tier 3 in all three branches", 1),
    TRUE_MASTER(ChallengeCategory.MILESTONES, "True Master", "Reach Tier 4 in all three branches", 1);

    private final ChallengeCategory category;
    private final String displayName;
    private final String description;
    private final int targetValue;

    ChallengeType(ChallengeCategory category, String displayName, String description, int targetValue) {
        this.category = category;
        this.displayName = displayName;
        this.description = description;
        this.targetValue = targetValue;
    }

    public ChallengeCategory getCategory() { return category; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getTargetValue() { return targetValue; }
}
