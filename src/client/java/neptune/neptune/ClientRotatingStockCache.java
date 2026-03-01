package neptune.neptune;

import neptune.neptune.network.RotatingStockSyncPayload;

import java.util.List;

/**
 * Client-side cache for rotating stock data received from the server.
 * Used by BrokerScreen to render the rotating stock section.
 */
public class ClientRotatingStockCache {

    private static List<RotatingStockSyncPayload.RotatingEntry> currentEntries = List.of();

    public static void update(List<RotatingStockSyncPayload.RotatingEntry> entries) {
        currentEntries = List.copyOf(entries);
    }

    public static List<RotatingStockSyncPayload.RotatingEntry> getEntries() {
        return currentEntries;
    }

    public static void clear() {
        currentEntries = List.of();
    }
}
