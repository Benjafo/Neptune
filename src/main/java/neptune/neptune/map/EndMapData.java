package neptune.neptune.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

/**
 * Represents a single named end map with a grid size.
 * Tracks marked city locations with optional notes.
 */
public record EndMapData(
        String name,
        int gridSize,
        Set<String> markedGrids,     // "x,z" format
        Map<String, String> notes    // "x,z" -> note
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
                            .forGetter(d -> new HashMap<>(d.notes))
            ).apply(instance, EndMapData::new)
    );

    public static EndMapData create(String name, int gridSize) {
        return new EndMapData(name, gridSize, Set.of(), Map.of());
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

    public String getNote(int gridX, int gridZ) {
        return notes.getOrDefault(gridKey(gridX, gridZ), null);
    }

    public int getMarkedCount() {
        return markedGrids.size();
    }

    public EndMapData withMark(int gridX, int gridZ, String note) {
        String key = gridKey(gridX, gridZ);
        Set<String> newMarked = new HashSet<>(markedGrids);
        newMarked.add(key);
        Map<String, String> newNotes = new HashMap<>(notes);
        if (note != null && !note.isEmpty()) {
            newNotes.put(key, note);
        }
        return new EndMapData(name, gridSize, Set.copyOf(newMarked), Map.copyOf(newNotes));
    }

    public EndMapData withUnmark(int gridX, int gridZ) {
        String key = gridKey(gridX, gridZ);
        Set<String> newMarked = new HashSet<>(markedGrids);
        newMarked.remove(key);
        Map<String, String> newNotes = new HashMap<>(notes);
        newNotes.remove(key);
        return new EndMapData(name, gridSize, Set.copyOf(newMarked), Map.copyOf(newNotes));
    }
}
