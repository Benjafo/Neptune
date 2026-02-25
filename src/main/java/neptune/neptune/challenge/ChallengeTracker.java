package neptune.neptune.challenge;

import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Central hub for updating challenge progress from game events.
 * Called by various systems when trackable actions occur.
 */
public class ChallengeTracker {

    /**
     * Called when a player visits a new grid square.
     */
    public static void onGridVisited(ServerPlayer player, int totalGridsVisited) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] gridChallenges = {
                ChallengeType.FIRST_STEPS, ChallengeType.WANDERER,
                ChallengeType.PATHFINDER, ChallengeType.VETERAN_EXPLORER
        };

        for (ChallengeType challenge : gridChallenges) {
            if (!data.isCompleted(challenge) && totalGridsVisited >= challenge.getTargetValue()) {
                data = data.withProgress(challenge, totalGridsVisited);
                notifyCompletion(player, challenge);
                changed = true;
            } else if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, totalGridsVisited);
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when a player marks a city.
     */
    public static void onCityMarked(ServerPlayer player, int totalCitiesMarked) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] cityChallenges = {
                ChallengeType.CITY_FINDER, ChallengeType.CITY_HUNTER,
                ChallengeType.CITY_CONQUEROR, ChallengeType.COMPLETIONIST
        };

        for (ChallengeType challenge : cityChallenges) {
            if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, totalCitiesMarked);
                if (totalCitiesMarked >= challenge.getTargetValue()) {
                    notifyCompletion(player, challenge);
                }
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when player reaches a new max distance from 0,0.
     */
    public static void onDistanceUpdated(ServerPlayer player, int maxDistance) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] distChallenges = {
                ChallengeType.INTO_THE_VOID, ChallengeType.NO_END_IN_SIGHT, ChallengeType.EDGE_WALKER
        };

        for (ChallengeType challenge : distChallenges) {
            if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, maxDistance);
                if (maxDistance >= challenge.getTargetValue()) {
                    notifyCompletion(player, challenge);
                }
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when player collects a new unique relic.
     */
    public static void onRelicCollected(ServerPlayer player, int totalUniqueRelics) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] relicChallenges = {
                ChallengeType.RELIC_HUNTER, ChallengeType.RELIC_SEEKER,
                ChallengeType.RELIC_HOARDER, ChallengeType.RELIC_MASTER
        };

        for (ChallengeType challenge : relicChallenges) {
            if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, totalUniqueRelics);
                if (totalUniqueRelics >= challenge.getTargetValue()) {
                    notifyCompletion(player, challenge);
                }
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when player kills a shulker.
     */
    public static void onShulkerKill(ServerPlayer player, int totalKills) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] killChallenges = {
                ChallengeType.SHULKER_SLAYER, ChallengeType.SHULKER_HUNTER, ChallengeType.SHULKER_NEMESIS
        };

        for (ChallengeType challenge : killChallenges) {
            if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, totalKills);
                if (totalKills >= challenge.getTargetValue()) {
                    notifyCompletion(player, challenge);
                }
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when player sells items to broker (tracks lifetime sold and first sale).
     */
    public static void onItemSold(ServerPlayer player, int essenceGained) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);

        // First Sale
        if (!data.isCompleted(ChallengeType.FIRST_SALE)) {
            data = data.withCompleted(ChallengeType.FIRST_SALE);
            notifyCompletion(player, ChallengeType.FIRST_SALE);
        }

        player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when player spends essence at broker.
     */
    public static void onEssenceSpent(ServerPlayer player, int totalSpent) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] spendChallenges = {
                ChallengeType.BIG_SPENDER, ChallengeType.REGULAR_CUSTOMER, ChallengeType.PATRON
        };

        for (ChallengeType challenge : spendChallenges) {
            if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, totalSpent);
                if (totalSpent >= challenge.getTargetValue()) {
                    notifyCompletion(player, challenge);
                }
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when lifetime essence total changes (for Wealthy/Rich/Mogul).
     */
    public static void onLifetimeEssenceUpdated(ServerPlayer player) {
        VoidEssenceData essence = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
        int lifetime = essence.lifetime();

        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] essenceChallenges = {
                ChallengeType.WEALTHY, ChallengeType.RICH, ChallengeType.MOGUL
        };

        for (ChallengeType challenge : essenceChallenges) {
            if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, lifetime);
                if (lifetime >= challenge.getTargetValue()) {
                    notifyCompletion(player, challenge);
                }
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when a minor set is completed.
     */
    public static void onMinorSetCompleted(ServerPlayer player, int totalMinorSetsComplete) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] setChallenges = {
                ChallengeType.SET_STARTER, ChallengeType.SET_COLLECTOR, ChallengeType.SET_COMPLETIONIST
        };

        for (ChallengeType challenge : setChallenges) {
            if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, totalMinorSetsComplete);
                if (totalMinorSetsComplete >= challenge.getTargetValue()) {
                    notifyCompletion(player, challenge);
                }
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when a major set is completed.
     */
    public static void onMajorSetCompleted(ServerPlayer player, int totalMajorSetsComplete) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        ChallengeType[] setChallenges = {
                ChallengeType.MAJOR_DISCOVERY, ChallengeType.GRAND_CURATOR
        };

        for (ChallengeType challenge : setChallenges) {
            if (!data.isCompleted(challenge)) {
                data = data.withProgress(challenge, totalMajorSetsComplete);
                if (totalMajorSetsComplete >= challenge.getTargetValue()) {
                    notifyCompletion(player, challenge);
                }
                changed = true;
            }
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    /**
     * Called when a legendary relic is found.
     */
    public static void onLegendaryFound(ServerPlayer player, int totalLegendaries) {
        ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        boolean changed = false;

        if (!data.isCompleted(ChallengeType.LUCKY_FIND)) {
            data = data.withCompleted(ChallengeType.LUCKY_FIND);
            notifyCompletion(player, ChallengeType.LUCKY_FIND);
            changed = true;
        }

        if (!data.isCompleted(ChallengeType.LEGENDARY_COLLECTOR)) {
            data = data.withProgress(ChallengeType.LEGENDARY_COLLECTOR, totalLegendaries);
            if (totalLegendaries >= 5) {
                notifyCompletion(player, ChallengeType.LEGENDARY_COLLECTOR);
            }
            changed = true;
        }

        if (changed) player.setAttached(NeptuneAttachments.CHALLENGES, data);
    }

    private static void notifyCompletion(ServerPlayer player, ChallengeType challenge) {
        player.sendSystemMessage(Component.literal(
                "§6§l✦ Challenge Complete: §e" + challenge.getDisplayName() + "§6§l ✦"));
    }
}
