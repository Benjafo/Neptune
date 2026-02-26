package neptune.neptune.map;

import neptune.neptune.network.MapSyncPayload;

import java.util.HashSet;
import java.util.Set;

/**
 * Client-side cache of the currently loaded map data, received from MapSyncPayload.
 */
public class ClientMapState {

    private static String mapName = "";
    private static int gridSize = 0;
    private static final Set<String> markedGrids = new HashSet<>();
    private static boolean minimapVisible = true;

    public static void handleSync(MapSyncPayload payload) {
        mapName = payload.mapName();
        gridSize = payload.gridSize();
        markedGrids.clear();
        markedGrids.addAll(payload.markedGrids());
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

    public static Set<String> getMarkedGrids() {
        return markedGrids;
    }

    public static boolean isMarked(int gridX, int gridZ) {
        return markedGrids.contains(gridX + "," + gridZ);
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
