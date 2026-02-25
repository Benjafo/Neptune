package neptune.neptune.broker;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the broker's stock: items available for purchase.
 */
public class BrokerStock {

    public record StockEntry(String name, int cost, ItemStack itemStack, String description) {}

    private static final List<StockEntry> CORE_STOCK = new ArrayList<>();

    static {
        CORE_STOCK.add(new StockEntry(
                "Rocket Bundle",
                120,
                new ItemStack(Items.FIREWORK_ROCKET, 64),
                "64 firework rockets for elytra flight"
        ));
        CORE_STOCK.add(new StockEntry(
                "Ender Pearl Bundle",
                50,
                new ItemStack(Items.ENDER_PEARL, 16),
                "16 ender pearls"
        ));
        CORE_STOCK.add(new StockEntry(
                "Cooked Food Bundle",
                175,
                new ItemStack(Items.COOKED_BEEF, 32),
                "32 cooked beef"
        ));
        CORE_STOCK.add(new StockEntry(
                "Basic Repair Kit",
                30,
                ItemStack.EMPTY, // Special item — handled via custom logic
                "Restores 50% durability to held item"
        ));
        CORE_STOCK.add(new StockEntry(
                "Recall Pearl",
                130,
                ItemStack.EMPTY, // Special item — teleports to 0,0
                "One-use teleport to main End island"
        ));
        CORE_STOCK.add(new StockEntry(
                "Bulk Ender Pearls",
                140,
                new ItemStack(Items.ENDER_PEARL, 64),
                "64 ender pearls (better value)"
        ));
    }

    public static List<StockEntry> getCoreStock() {
        return CORE_STOCK;
    }
}
