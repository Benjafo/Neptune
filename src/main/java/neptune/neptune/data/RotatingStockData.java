package neptune.neptune.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

/**
 * Per-player rotating stock state for the broker.
 * Tracks the current 4 offered items, refresh timing, and purchase history.
 */
public record RotatingStockData(
        List<String> currentItems,
        long lastRefreshGameTime,
        int citiesSinceRefresh,
        Set<String> purchaseHistory
) {
    public static final RotatingStockData EMPTY = new RotatingStockData(List.of(), 0L, 0, Set.of());

    public static final Codec<RotatingStockData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf()
                            .optionalFieldOf("currentItems", List.of())
                            .forGetter(RotatingStockData::currentItems),
                    Codec.LONG.optionalFieldOf("lastRefreshGameTime", 0L)
                            .forGetter(RotatingStockData::lastRefreshGameTime),
                    Codec.INT.optionalFieldOf("citiesSinceRefresh", 0)
                            .forGetter(RotatingStockData::citiesSinceRefresh),
                    Codec.STRING.listOf()
                            .xmap(l -> (Set<String>) new HashSet<>(l), s -> new ArrayList<>(s))
                            .optionalFieldOf("purchaseHistory", Set.of())
                            .forGetter(RotatingStockData::purchaseHistory)
            ).apply(instance, RotatingStockData::new)
    );

    public boolean isEmpty() {
        return currentItems.isEmpty();
    }

    public RotatingStockData withItems(List<String> items, long gameTime) {
        return new RotatingStockData(List.copyOf(items), gameTime, 0, purchaseHistory);
    }

    public RotatingStockData withCityMarked() {
        return new RotatingStockData(currentItems, lastRefreshGameTime, citiesSinceRefresh + 1, purchaseHistory);
    }

    public RotatingStockData withPurchase(String itemId) {
        Set<String> newHistory = new HashSet<>(purchaseHistory);
        newHistory.add(itemId);
        return new RotatingStockData(currentItems, lastRefreshGameTime, citiesSinceRefresh, Set.copyOf(newHistory));
    }

    public boolean hasCurrentItem(String itemId) {
        return currentItems.contains(itemId);
    }
}
