package neptune.neptune.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

/**
 * Represents a single named end map with a grid size.
 * Tracks marked city locations with optional notes, visited grid squares,
 * and completed 5x5 regions.
 */
public record EndMapData(
        String name,
        int gridSize,
        Set<String> markedGrids,        // "x,z" format — grids with cities marked
        Map<String, String> notes,       // "x,z" -> note
        Set<String> visitedGrids,        // "x,z" format — grids the player has entered
        Set<String> completedRegions     // "rx,rz" format — 5x5 regions fully explored
) {
    public static final Codec<EndMapData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(EndMapData::name),
                    Codec.INT.fieldOf("gridSize").forGetter(EndMapData::gridSize),
                    Codec.STRING.listOf()
                            .xmap(l -> (Set<String>) new HashSet<>(l), s -> new ArrayList<>(s))
                            .fieldOf("markedGrids")
                            .forGetter(EndMapData::markedGrids),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING)
                            .fieldOf("notes")
                            .forGetter(d -> new HashMap<>(d.notes)),
                    Codec.STRING.listOf()
                            .xmap(l -> (Set<String>) new HashSet<>(l), s -> new ArrayList<>(s))
                            .optionalFieldOf("visitedGrids", Set.of())
                            .forGetter(EndMapData::visitedGrids),
                    Codec.STRING.listOf()
                            .xmap(l -> (Set<String>) new HashSet<>(l), s -> new ArrayList<>(s))
                            .optionalFieldOf("completedRegions", Set.of())
                            .forGetter(EndMapData::completedRegions)
            ).apply(instance, EndMapData::new)
    );

    public static EndMapData create(String name, int gridSize) {
        return new EndMapData(name, gridSize, Set.of(), Map.of(), Set.of(), Set.of());
    }

    public static String gridKey(int gridX, int gridZ) {
        return gridX + "," + gridZ;
    }

    public static int[] parseGridKey(String key) {
        String[] parts = key.split(",");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    /**
     * Convert world coordinates to grid coordinates.
     */
    public int toGridX(double worldX) {
        return (int) Math.floor(worldX / gridSize);
    }

    public int toGridZ(double worldZ) {
        return (int) Math.floor(worldZ / gridSize);
    }

    public boolean isMarked(int gridX, int gridZ) {
        return markedGrids.contains(gridKey(gridX, gridZ));
    }

    public boolean isVisited(int gridX, int gridZ) {
        return visitedGrids.contains(gridKey(gridX, gridZ));
    }

    public String getNote(int gridX, int gridZ) {
        return notes.getOrDefault(gridKey(gridX, gridZ), null);
    }

    public int getMarkedCount() {
        return markedGrids.size();
    }

    public int getVisitedCount() {
        return visitedGrids.size();
    }

    public EndMapData withMark(int gridX, int gridZ, String note) {
        String key = gridKey(gridX, gridZ);
        Set<String> newMarked = new HashSet<>(markedGrids);
        newMarked.add(key);
        Map<String, String> newNotes = new HashMap<>(notes);
        if (note != null && !note.isEmpty()) {
            newNotes.put(key, note);
        }
        return new EndMapData(name, gridSize, Set.copyOf(newMarked), Map.copyOf(newNotes),
                visitedGrids, completedRegions);
    }

    public EndMapData withUnmark(int gridX, int gridZ) {
        String key = gridKey(gridX, gridZ);
        Set<String> newMarked = new HashSet<>(markedGrids);
        newMarked.remove(key);
        Map<String, String> newNotes = new HashMap<>(notes);
        newNotes.remove(key);
        return new EndMapData(name, gridSize, Set.copyOf(newMarked), Map.copyOf(newNotes),
                visitedGrids, completedRegions);
    }

    /**
     * Record that the player has visited this grid square.
     * Returns null if already visited (no change needed).
     */
    public EndMapData withVisit(int gridX, int gridZ) {
        String key = gridKey(gridX, gridZ);
        if (visitedGrids.contains(key)) return null;
        Set<String> newVisited = new HashSet<>(visitedGrids);
        newVisited.add(key);
        return new EndMapData(name, gridSize, markedGrids, notes,
                Set.copyOf(newVisited), completedRegions);
    }

    /**
     * Mark a 5x5 region as completed.
     */
    public EndMapData withCompletedRegion(int regionX, int regionZ) {
        String key = gridKey(regionX, regionZ);
        if (completedRegions.contains(key)) return this;
        Set<String> newCompleted = new HashSet<>(completedRegions);
        newCompleted.add(key);
        return new EndMapData(name, gridSize, markedGrids, notes,
                visitedGrids, Set.copyOf(newCompleted));
    }

    public boolean isRegionCompleted(int regionX, int regionZ) {
        return completedRegions.contains(gridKey(regionX, regionZ));
    }

    /**
     * Get the region coordinate for a grid coordinate.
     * Regions are aligned 5x5 blocks.
     */
    public static int toRegionCoord(int gridCoord) {
        return Math.floorDiv(gridCoord, 5);
    }

    /**
     * Check if all 25 grids in a 5x5 region are visited.
     */
    public boolean isRegionFullyVisited(int regionX, int regionZ) {
        int startX = regionX * 5;
        int startZ = regionZ * 5;
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                if (!visitedGrids.contains(gridKey(startX + dx, startZ + dz))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Count how many grids in a region are visited (for progress display).
     */
    public int getRegionVisitedCount(int regionX, int regionZ) {
        int startX = regionX * 5;
        int startZ = regionZ * 5;
        int count = 0;
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                if (visitedGrids.contains(gridKey(startX + dx, startZ + dz))) {
                    count++;
                }
            }
        }
        return count;
    }
}
