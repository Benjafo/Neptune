package neptune.neptune.unlock;

import neptune.neptune.challenge.ChallengeData;
import neptune.neptune.challenge.ChallengeType;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages unlock purchases and requirement checking.
 */
public class UnlockManager {

    /**
     * Check if a player meets all requirements for an unlock.
     * Returns a list of unmet requirements (empty if all met).
     */
    public static List<String> getUnmetRequirements(ServerPlayer player, UnlockType unlock) {
        List<String> unmet = new ArrayList<>();
        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        ChallengeData challenges = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
        VoidEssenceData essence = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);

        // Already unlocked?
        if (unlocks.hasUnlock(unlock)) {
            unmet.add("Already unlocked");
            return unmet;
        }

        UnlockRequirements.Requirements reqs = UnlockRequirements.getRequirements(unlock);
        if (reqs == null) return unmet;

        // Previous tier in same branch
        if (reqs.previousTier() != null && !unlocks.hasUnlock(reqs.previousTier())) {
            unmet.add("Requires: " + reqs.previousTier().getDisplayName());
        }

        // Cross-branch dependencies
        for (UnlockType dep : reqs.crossBranchDeps()) {
            if (!unlocks.hasUnlock(dep)) {
                unmet.add("Requires: " + dep.getDisplayName());
            }
        }

        // Required challenges
        for (ChallengeType challenge : reqs.requiredChallenges()) {
            if (!challenges.isCompleted(challenge)) {
                unmet.add("Challenge: " + challenge.getDisplayName());
            }
        }

        // Essence cost
        if (essence.current() < unlock.getEssenceCost()) {
            unmet.add("Need " + unlock.getEssenceCost() + " essence (have " + essence.current() + ")");
        }

        // Stat requirements (these will be checked against player stats when those systems are built)
        // For now, we track them but the actual stat values will come from other systems
        // Cities marked, grids visited, relics collected, lifetime essence sold, relic sets completed
        // These checks will be added when the respective systems are implemented

        return unmet;
    }

    /**
     * Attempt to purchase an unlock. Returns true if successful.
     */
    public static boolean tryPurchase(ServerPlayer player, UnlockType unlock) {
        List<String> unmet = getUnmetRequirements(player, unlock);
        if (!unmet.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cCannot unlock " + unlock.getDisplayName() + ":"));
            for (String req : unmet) {
                player.sendSystemMessage(Component.literal("§c  - " + req));
            }
            return false;
        }

        // Deduct essence
        VoidEssenceData essence = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
        VoidEssenceData afterSpend = essence.spend(unlock.getEssenceCost());
        if (afterSpend == null) {
            player.sendSystemMessage(Component.literal("§cNot enough void essence!"));
            return false;
        }
        player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);

        // Grant unlock
        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        player.setAttached(NeptuneAttachments.UNLOCKS, unlocks.withUnlock(unlock));

        // Notify
        player.sendSystemMessage(Component.literal("§a§lUnlocked: " + unlock.getDisplayName() + "!"));
        player.sendSystemMessage(Component.literal("§7" + unlock.getDescription()));

        // Apply immediate effects
        applyUnlockEffects(player, unlock);

        // Check milestone challenges
        checkMilestoneChallenges(player);

        return true;
    }

    /**
     * Apply immediate effects when an unlock is purchased.
     */
    private static void applyUnlockEffects(ServerPlayer player, UnlockType unlock) {
        switch (unlock) {
            case PROCESSING_T1 -> {
                player.sendSystemMessage(Component.literal("§eGear now sells at full value! You can now place a Breakdown Table."));
            }
            case PROCESSING_T2 -> {
                player.sendSystemMessage(Component.literal("§eYou can now extract enchantment shards! You can now place a Shard Infuser."));
            }
            case CATALOG_T1 -> {
                // Relics sell for +50% (handled by checking unlock state)
                player.sendSystemMessage(Component.literal("§eRelics now sell for 50% more!"));
            }
            default -> {
                // Other unlocks enable features passively (checked when features are used)
            }
        }
    }

    /**
     * Check and update milestone challenges related to unlocks.
     */
    private static void checkMilestoneChallenges(ServerPlayer player) {
        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        ChallengeData challenges = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);

        // Upgrade Novice: any T2
        if (!challenges.isCompleted(ChallengeType.UPGRADE_NOVICE)) {
            for (UnlockBranch branch : UnlockBranch.values()) {
                if (unlocks.getHighestTier(branch) >= 2) {
                    challenges = challenges.withCompleted(ChallengeType.UPGRADE_NOVICE);
                    player.sendSystemMessage(Component.literal("§6Challenge Complete: Upgrade Novice!"));
                    break;
                }
            }
        }

        // Upgrade Adept: any T3
        if (!challenges.isCompleted(ChallengeType.UPGRADE_ADEPT)) {
            for (UnlockBranch branch : UnlockBranch.values()) {
                if (unlocks.getHighestTier(branch) >= 3) {
                    challenges = challenges.withCompleted(ChallengeType.UPGRADE_ADEPT);
                    player.sendSystemMessage(Component.literal("§6Challenge Complete: Upgrade Adept!"));
                    break;
                }
            }
        }

        // Upgrade Master: any T4
        if (!challenges.isCompleted(ChallengeType.UPGRADE_MASTER)) {
            for (UnlockBranch branch : UnlockBranch.values()) {
                if (unlocks.getHighestTier(branch) >= 4) {
                    challenges = challenges.withCompleted(ChallengeType.UPGRADE_MASTER);
                    player.sendSystemMessage(Component.literal("§6Challenge Complete: Upgrade Master!"));
                    break;
                }
            }
        }

        // Jack of All Trades: T2 in all branches
        if (!challenges.isCompleted(ChallengeType.JACK_OF_ALL_TRADES)) {
            boolean allT2 = true;
            for (UnlockBranch branch : UnlockBranch.values()) {
                if (unlocks.getHighestTier(branch) < 2) { allT2 = false; break; }
            }
            if (allT2) {
                challenges = challenges.withCompleted(ChallengeType.JACK_OF_ALL_TRADES);
                player.sendSystemMessage(Component.literal("§6Challenge Complete: Jack of All Trades!"));
            }
        }

        // Well Rounded: T3 in all branches
        if (!challenges.isCompleted(ChallengeType.WELL_ROUNDED)) {
            boolean allT3 = true;
            for (UnlockBranch branch : UnlockBranch.values()) {
                if (unlocks.getHighestTier(branch) < 3) { allT3 = false; break; }
            }
            if (allT3) {
                challenges = challenges.withCompleted(ChallengeType.WELL_ROUNDED);
                player.sendSystemMessage(Component.literal("§6Challenge Complete: Well Rounded!"));
            }
        }

        // True Master: T4 in all branches
        if (!challenges.isCompleted(ChallengeType.TRUE_MASTER)) {
            boolean allT4 = true;
            for (UnlockBranch branch : UnlockBranch.values()) {
                if (unlocks.getHighestTier(branch) < 4) { allT4 = false; break; }
            }
            if (allT4) {
                challenges = challenges.withCompleted(ChallengeType.TRUE_MASTER);
                player.sendSystemMessage(Component.literal("§6Challenge Complete: True Master!"));
            }
        }

        player.setAttached(NeptuneAttachments.CHALLENGES, challenges);
    }
}
