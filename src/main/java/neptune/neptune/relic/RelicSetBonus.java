package neptune.neptune.relic;

import neptune.neptune.data.NeptuneAttachments;
import net.minecraft.server.level.ServerPlayer;

/**
 * Checks and applies major relic set bonuses.
 * Bonuses activate immediately on set completion, regardless of unlock progression.
 *
 * THE_BUILDERS:    +15% essence from all gear sold
 * THE_VOID:        Elytra durability drains 15% slower
 * THE_INHABITANTS: Shulker status effect -20% duration, shulker bullets deal 25% less damage
 * THE_EXPLORERS:   All broker items 15% cheaper
 */
public class RelicSetBonus {

    public static final float BUILDERS_SELL_BONUS = 0.15f;
    public static final float VOID_DURABILITY_REDUCTION = 0.15f;
    public static final float INHABITANTS_DURATION_REDUCTION = 0.20f;
    public static final float INHABITANTS_DAMAGE_REDUCTION = 0.25f;
    public static final float EXPLORERS_COST_REDUCTION = 0.15f;

    public static boolean hasBuildersBonus(ServerPlayer player) {
        return hasSetComplete(player, RelicSet.THE_BUILDERS);
    }

    public static boolean hasVoidBonus(ServerPlayer player) {
        return hasSetComplete(player, RelicSet.THE_VOID);
    }

    public static boolean hasInhabitantsBonus(ServerPlayer player) {
        return hasSetComplete(player, RelicSet.THE_INHABITANTS);
    }

    public static boolean hasExplorersBonus(ServerPlayer player) {
        return hasSetComplete(player, RelicSet.THE_EXPLORERS);
    }

    private static boolean hasSetComplete(ServerPlayer player, RelicSet set) {
        RelicJournalData journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
        return journal.isSetComplete(set);
    }

    /**
     * Apply Builders bonus to an essence value (+15%).
     */
    public static int applyBuildersBonus(int baseEssence) {
        return Math.round(baseEssence * (1.0f + BUILDERS_SELL_BONUS));
    }

    /**
     * Apply Explorers discount to a broker price (-15%).
     */
    public static int applyExplorersDiscount(int basePrice) {
        return Math.max(1, Math.round(basePrice * (1.0f - EXPLORERS_COST_REDUCTION)));
    }
}
