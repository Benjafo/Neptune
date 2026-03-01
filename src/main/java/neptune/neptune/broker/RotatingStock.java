package neptune.neptune.broker;

import neptune.neptune.data.RotatingStockData;
import neptune.neptune.relic.RelicDefinition;
import neptune.neptune.relic.RelicJournalData;
import neptune.neptune.relic.RelicRarity;
import neptune.neptune.unlock.UnlockData;
import neptune.neptune.unlock.UnlockType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Defines 14 rotating broker items. 4 are selected at a time based on
 * unlock eligibility and weighted toward unpurchased items.
 */
public class RotatingStock {

    private static final long REFRESH_TICK_INTERVAL = 480000L; // 20 game days
    private static final int REFRESH_CITY_THRESHOLD = 3;
    private static final int ROTATION_SIZE = 4;

    public record RotatingEntry(String id, String name, int cost, String description, UnlockType requiredUnlock) {}

    private static final List<RotatingEntry> ALL_ROTATING = new ArrayList<>();
    private static final Map<String, RotatingEntry> BY_ID = new HashMap<>();

    static {
        add(new RotatingEntry("rot_eff_tome", "Efficiency Tome", 90,
                "Adds +1 Efficiency to held tool for 1 hour", UnlockType.PROCESSING_T2));
        add(new RotatingEntry("rot_prot_tome", "Protection Tome", 90,
                "Adds +1 Protection to held armor for 1 hour", UnlockType.PROCESSING_T2));
        add(new RotatingEntry("rot_double_drop", "Double Drop Charm", 150,
                "Next relic find is doubled", UnlockType.CATALOG_T2));
        add(new RotatingEntry("rot_mystery_box", "Mystery Relic Box", 1000,
                "Receive a random unowned relic (Common/Uncommon/Rare)", UnlockType.CATALOG_T3));
        add(new RotatingEntry("rot_slow_fall", "Slow Falling Potions (3)", 80,
                "3 potions of slow falling (1:30)", null));
        add(new RotatingEntry("rot_totem", "Totem of Undying", 350,
                "One extra life", null));
        add(new RotatingEntry("rot_gap", "Enchanted Golden Apple", 200,
                "One enchanted golden apple", null));
        add(new RotatingEntry("rot_carrots", "Golden Carrots (32)", 150,
                "32 golden carrots", null));
        add(new RotatingEntry("rot_ender_chest", "Ender Chest", 60,
                "One ender chest block", null));
        add(new RotatingEntry("rot_night_vision", "Night Vision Potions (3)", 60,
                "3 potions of night vision (3:00)", null));
        add(new RotatingEntry("rot_scaffolding", "Scaffolding (64)", 45,
                "64 scaffolding blocks", null));
        add(new RotatingEntry("rot_spyglass", "Spyglass", 30,
                "A spyglass for scouting", null));
        add(new RotatingEntry("rot_shield", "Shield", 40,
                "A shield for defense", null));
        add(new RotatingEntry("rot_bow_arrows", "Bow + 64 Arrows", 75,
                "A bow and 64 arrows", null));
    }

    private static void add(RotatingEntry entry) {
        ALL_ROTATING.add(entry);
        BY_ID.put(entry.id(), entry);
    }

    public static RotatingEntry getById(String id) {
        return BY_ID.get(id);
    }

    /**
     * Check if rotation should refresh based on time or cities marked.
     */
    public static boolean shouldRefresh(RotatingStockData data, long currentGameTime) {
        if (data.isEmpty()) return true;
        if (data.citiesSinceRefresh() >= REFRESH_CITY_THRESHOLD) return true;
        return (currentGameTime - data.lastRefreshGameTime()) >= REFRESH_TICK_INTERVAL;
    }

    /**
     * Select 4 items from the eligible pool, weighted toward unpurchased items.
     * Mystery Relic Box is excluded if the player owns all common+uncommon+rare relics.
     */
    public static List<String> selectRotation(UnlockData unlocks, RotatingStockData stockData, RelicJournalData journal) {
        List<RotatingEntry> eligible = new ArrayList<>();
        for (RotatingEntry entry : ALL_ROTATING) {
            if (entry.requiredUnlock() != null && !unlocks.hasUnlock(entry.requiredUnlock())) {
                continue;
            }
            // Exclude mystery box if player owns all common+uncommon+rare relics
            if (entry.id().equals("rot_mystery_box") && ownsAllNonLegendary(journal)) {
                continue;
            }
            eligible.add(entry);
        }

        if (eligible.size() <= ROTATION_SIZE) {
            return eligible.stream().map(RotatingEntry::id).toList();
        }

        // Weighted selection: unpurchased items have 3x weight
        Random random = new Random();
        List<String> selected = new ArrayList<>();
        List<RotatingEntry> pool = new ArrayList<>(eligible);

        for (int i = 0; i < ROTATION_SIZE && !pool.isEmpty(); i++) {
            List<Double> weights = new ArrayList<>();
            double totalWeight = 0;
            for (RotatingEntry entry : pool) {
                double w = stockData.purchaseHistory().contains(entry.id()) ? 1.0 : 3.0;
                weights.add(w);
                totalWeight += w;
            }

            double roll = random.nextDouble() * totalWeight;
            double cumulative = 0;
            int chosenIndex = 0;
            for (int j = 0; j < pool.size(); j++) {
                cumulative += weights.get(j);
                if (roll < cumulative) {
                    chosenIndex = j;
                    break;
                }
            }

            selected.add(pool.get(chosenIndex).id());
            pool.remove(chosenIndex);
        }

        return selected;
    }

    /**
     * Get the current stock entries from the player's rotation data.
     */
    public static List<RotatingEntry> getCurrentStock(RotatingStockData data) {
        List<RotatingEntry> entries = new ArrayList<>();
        for (String id : data.currentItems()) {
            RotatingEntry entry = BY_ID.get(id);
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private static boolean ownsAllNonLegendary(RelicJournalData journal) {
        if (journal == null) return false;
        for (RelicRarity rarity : new RelicRarity[]{RelicRarity.COMMON, RelicRarity.UNCOMMON, RelicRarity.RARE}) {
            for (RelicDefinition def : RelicDefinition.getByRarity(rarity)) {
                if (!journal.hasDiscovered(def.id())) return false;
            }
        }
        return true;
    }
}
