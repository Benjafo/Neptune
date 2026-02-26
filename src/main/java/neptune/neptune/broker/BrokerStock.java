package neptune.neptune.broker;

import neptune.neptune.unlock.UnlockData;
import neptune.neptune.unlock.UnlockType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Defines the broker's stock: items available for purchase.
 * Core items are always available; gated items require specific unlocks.
 */
public class BrokerStock {

    public record StockEntry(String id, String name, int cost, ItemStack itemStack, String description, UnlockType requiredUnlock) {}

    private static final List<StockEntry> ALL_STOCK = new ArrayList<>();
    private static final Map<String, StockEntry> BY_ID = new HashMap<>();

    static {
        // Core items (always available)
        add(new StockEntry("rockets", "Rocket Bundle", 120,
                new ItemStack(Items.FIREWORK_ROCKET, 64),
                "64 firework rockets for elytra flight", null));
        add(new StockEntry("pearls", "Ender Pearl Bundle", 50,
                new ItemStack(Items.ENDER_PEARL, 16),
                "16 ender pearls", null));
        add(new StockEntry("food", "Cooked Food Bundle", 175,
                new ItemStack(Items.COOKED_BEEF, 32),
                "32 cooked beef", null));
        add(new StockEntry("basic_repair", "Basic Repair Kit", 30,
                ItemStack.EMPTY,
                "Restores 50% durability to held item", null));
        add(new StockEntry("recall_pearl", "Recall Pearl", 130,
                ItemStack.EMPTY,
                "One-use teleport to main End island", null));
        add(new StockEntry("bulk_pearls", "Bulk Ender Pearls", 140,
                new ItemStack(Items.ENDER_PEARL, 64),
                "64 ender pearls (better value)", null));

        // Gated items (require unlocks)
        add(new StockEntry("city_map", "City Map", 150,
                ItemStack.EMPTY,
                "Reveals nearest End City grid coordinates", UnlockType.NAVIGATION_T1));
        add(new StockEntry("adv_repair", "Advanced Repair Kit", 60,
                ItemStack.EMPTY,
                "Fully repairs held item", UnlockType.PROCESSING_T1));
        add(new StockEntry("shulker_shell", "Shulker Shell", 45,
                new ItemStack(Items.SHULKER_SHELL, 1),
                "1 shulker shell", UnlockType.PROCESSING_T2));
        add(new StockEntry("hint_common", "Relic Hint (Common)", 120,
                ItemStack.EMPTY,
                "Locate a Common relic at the nearest unexplored city", UnlockType.CATALOG_T1));
        add(new StockEntry("hint_uncommon", "Relic Hint (Uncommon)", 170,
                ItemStack.EMPTY,
                "Locate an Uncommon relic at a nearby city", UnlockType.CATALOG_T2));
        add(new StockEntry("hint_rare", "Relic Hint (Rare)", 240,
                ItemStack.EMPTY,
                "Locate a Rare relic at a nearby city", UnlockType.CATALOG_T3));
        add(new StockEntry("hint_very_rare", "Relic Hint (Very Rare)", 320,
                ItemStack.EMPTY,
                "Locate a Very Rare relic at a nearby city", UnlockType.CATALOG_T4));
        add(new StockEntry("ench_shards", "Enchantment Shards (5)", 75,
                ItemStack.EMPTY,
                "5 enchantment shards for future use", UnlockType.PROCESSING_T2));
        add(new StockEntry("void_elytra", "Void Elytra", 220,
                ItemStack.EMPTY,
                "Emergency elytra with low durability", UnlockType.NAVIGATION_T3));
        add(new StockEntry("pocket_shulker", "Pocket Shulker", 120,
                ItemStack.EMPTY,
                "Portable shulker box. Vanishes on death!", UnlockType.PROCESSING_T3));
    }

    private static void add(StockEntry entry) {
        ALL_STOCK.add(entry);
        BY_ID.put(entry.id(), entry);
    }

    /**
     * Returns items visible to a player based on their unlocks.
     * Core items (no unlock required) are always included.
     */
    public static List<StockEntry> getVisibleStock(UnlockData unlocks) {
        List<StockEntry> visible = new ArrayList<>();
        for (StockEntry entry : ALL_STOCK) {
            if (entry.requiredUnlock() == null || unlocks.hasUnlock(entry.requiredUnlock())) {
                visible.add(entry);
            }
        }
        return visible;
    }

    /**
     * Look up a stock entry by its stable ID.
     */
    public static StockEntry getById(String id) {
        return BY_ID.get(id);
    }

    /**
     * @deprecated Use getVisibleStock(UnlockData) instead
     */
    @Deprecated
    public static List<StockEntry> getCoreStock() {
        return getVisibleStock(UnlockData.EMPTY);
    }
}
