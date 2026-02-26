package neptune.neptune.map;

import neptune.neptune.network.MapSyncPayload;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Client-side cache of the currently loaded map data, received from MapSyncPayload.
 */
public class ClientMapState {

    private static final Logger LOGGER = LoggerFactory.getLogger("neptune");

    private static volatile String mapName = "";
    private static volatile int gridSize = 0;
    private static final Set<String> markedGrids = new HashSet<>();
    private static volatile boolean minimapVisible = true;

    public static void handleSync(MapSyncPayload payload) {
        LOGGER.info("[Neptune Client] Received map sync: name='{}', grid={}, marks={}",
                payload.mapName(), payload.gridSize(), payload.markedGrids().size());
        synchronized (markedGrids) {
            mapName = payload.mapName();
            gridSize = payload.gridSize();
            markedGrids.clear();
            markedGrids.addAll(payload.markedGrids());
        }
    }

    public static boolean hasLoadedMap() {
        return !mapName.isEmpty() && gridSize > 0;
    }

    public static String getMapName() {
        return mapName;
    }

    public static int getGridSize() {
        return gridSize;
    }

    public static int getMarkedCount() {
        synchronized (markedGrids) {
            return markedGrids.size();
        }
    }

    public static boolean isMarked(int gridX, int gridZ) {
        synchronized (markedGrids) {
            return markedGrids.contains(gridX + "," + gridZ);
        }
    }

    public static int toGridX(double worldX) {
        return (int) Math.floor(worldX / gridSize);
    }

    public static int toGridZ(double worldZ) {
        return (int) Math.floor(worldZ / gridSize);
    }

    public static boolean isMinimapVisible() {
        return minimapVisible;
    }

    public static void toggleMinimap() {
        minimapVisible = !minimapVisible;
    }
}
