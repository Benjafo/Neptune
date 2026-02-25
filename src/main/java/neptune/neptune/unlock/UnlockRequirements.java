package neptune.neptune.unlock;

import neptune.neptune.challenge.ChallengeType;

import java.util.*;

/**
 * Defines the requirements for each unlock beyond essence cost.
 * Requirements include: previous tier, cross-branch dependencies, challenges, and stat milestones.
 */
public class UnlockRequirements {

    public record Requirements(
            UnlockType previousTier,
            List<UnlockType> crossBranchDeps,
            List<ChallengeType> requiredChallenges,
            int citiesMarked,
            int gridsVisited,
            int relicsCollected,
            int lifetimeEssenceSold,
            int relicSetsCompleted
    ) {
        public static Builder builder() { return new Builder(); }
    }

    public static class Builder {
        private UnlockType previousTier;
        private final List<UnlockType> crossBranchDeps = new ArrayList<>();
        private final List<ChallengeType> requiredChallenges = new ArrayList<>();
        private int citiesMarked;
        private int gridsVisited;
        private int relicsCollected;
        private int lifetimeEssenceSold;
        private int relicSetsCompleted;

        public Builder prev(UnlockType t) { previousTier = t; return this; }
        public Builder dep(UnlockType t) { crossBranchDeps.add(t); return this; }
        public Builder challenge(ChallengeType c) { requiredChallenges.add(c); return this; }
        public Builder cities(int n) { citiesMarked = n; return this; }
        public Builder grids(int n) { gridsVisited = n; return this; }
        public Builder relics(int n) { relicsCollected = n; return this; }
        public Builder sold(int n) { lifetimeEssenceSold = n; return this; }
        public Builder sets(int n) { relicSetsCompleted = n; return this; }

        public Requirements build() {
            return new Requirements(previousTier, List.copyOf(crossBranchDeps),
                    List.copyOf(requiredChallenges), citiesMarked, gridsVisited,
                    relicsCollected, lifetimeEssenceSold, relicSetsCompleted);
        }
    }

    private static final Map<UnlockType, Requirements> REQUIREMENTS = new EnumMap<>(UnlockType.class);

    static {
        // Navigation Branch
        REQUIREMENTS.put(UnlockType.NAVIGATION_T1, Requirements.builder()
                .cities(5).grids(15)
                .challenge(ChallengeType.FIRST_STEPS)
                .build());

        REQUIREMENTS.put(UnlockType.NAVIGATION_T2, Requirements.builder()
                .prev(UnlockType.NAVIGATION_T1)
                .cities(20).grids(60)
                .challenge(ChallengeType.WANDERER)
                .build());

        REQUIREMENTS.put(UnlockType.NAVIGATION_T3, Requirements.builder()
                .prev(UnlockType.NAVIGATION_T2)
                .dep(UnlockType.PROCESSING_T2)
                .cities(50).grids(150)
                .challenge(ChallengeType.NO_END_IN_SIGHT)
                .build());

        REQUIREMENTS.put(UnlockType.NAVIGATION_T4, Requirements.builder()
                .prev(UnlockType.NAVIGATION_T3)
                .dep(UnlockType.CATALOG_T2)
                .cities(100).grids(300)
                .challenge(ChallengeType.CITY_CONQUEROR)
                .build());

        // Processing Branch
        REQUIREMENTS.put(UnlockType.PROCESSING_T1, Requirements.builder()
                .sold(750)
                .challenge(ChallengeType.SHULKER_SLAYER)
                .build());

        REQUIREMENTS.put(UnlockType.PROCESSING_T2, Requirements.builder()
                .prev(UnlockType.PROCESSING_T1)
                .dep(UnlockType.NAVIGATION_T1)
                .sold(2500).relics(15)
                .challenge(ChallengeType.WEALTHY)
                .build());

        REQUIREMENTS.put(UnlockType.PROCESSING_T3, Requirements.builder()
                .prev(UnlockType.PROCESSING_T2)
                .dep(UnlockType.CATALOG_T2)
                .sold(6000)
                .challenge(ChallengeType.SHULKER_NEMESIS)
                .build());

        REQUIREMENTS.put(UnlockType.PROCESSING_T4, Requirements.builder()
                .prev(UnlockType.PROCESSING_T3)
                .sold(12000).relics(60)
                .challenge(ChallengeType.RICH)
                .build());

        // Catalog Branch
        REQUIREMENTS.put(UnlockType.CATALOG_T1, Requirements.builder()
                .relics(10)
                .challenge(ChallengeType.RELIC_HUNTER)
                .build());

        REQUIREMENTS.put(UnlockType.CATALOG_T2, Requirements.builder()
                .prev(UnlockType.CATALOG_T1)
                .dep(UnlockType.NAVIGATION_T1)
                .relics(25).cities(25)
                .challenge(ChallengeType.SET_STARTER)
                .build());

        REQUIREMENTS.put(UnlockType.CATALOG_T3, Requirements.builder()
                .prev(UnlockType.CATALOG_T2)
                .dep(UnlockType.PROCESSING_T2)
                .relics(50)
                .challenge(ChallengeType.RELIC_HOARDER)
                .build());

        REQUIREMENTS.put(UnlockType.CATALOG_T4, Requirements.builder()
                .prev(UnlockType.CATALOG_T3)
                .relics(85).cities(100).sets(5)
                .challenge(ChallengeType.MAJOR_DISCOVERY)
                .build());

        // Endgame
        REQUIREMENTS.put(UnlockType.ENDGAME, Requirements.builder()
                .dep(UnlockType.NAVIGATION_T4)
                .dep(UnlockType.PROCESSING_T4)
                .dep(UnlockType.CATALOG_T4)
                .cities(150).relics(110)
                .challenge(ChallengeType.VETERAN_EXPLORER)
                .challenge(ChallengeType.EDGE_WALKER)
                .challenge(ChallengeType.MOGUL)
                .challenge(ChallengeType.TRUE_MASTER)
                .challenge(ChallengeType.GRAND_CURATOR)
                .build());
    }

    public static Requirements getRequirements(UnlockType unlock) {
        return REQUIREMENTS.get(unlock);
    }
}
