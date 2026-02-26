package neptune.neptune.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

/**
 * Collection of all maps for a player, plus the currently loaded map name.
 * Stored as a Fabric attachment.
 */
public record MapCollectionData(
        Map<String, EndMapData> maps,
        String loadedMapName
) {
    public static final MapCollectionData EMPTY = new MapCollectionData(Map.of(), "");

    public static final Codec<MapCollectionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, EndMapData.CODEC)
                            .fieldOf("maps")
                            .forGetter(d -> new HashMap<>(d.maps)),
                    Codec.STRING.fieldOf("loadedMapName").forGetter(MapCollectionData::loadedMapName)
            ).apply(instance, MapCollectionData::new)
    );

    // StreamCodec for sync — we only sync the loaded map name and basic stats, not full map data
    // Full map data is too large to sync every time. Client requests specific data via packets.
    public static final StreamCodec<ByteBuf, MapCollectionData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, d -> d.loadedMapName,
            ByteBufCodecs.INT, d -> d.getLoadedMap() != null ? d.getLoadedMap().getMarkedCount() : 0,
            (name, count) -> new MapCollectionData(Map.of(), name)
    );

    public boolean hasMap(String name) {
        return maps.containsKey(name);
    }

    public EndMapData getMap(String name) {
        return maps.get(name);
    }

    public EndMapData getLoadedMap() {
        if (loadedMapName.isEmpty()) return null;
        return maps.get(loadedMapName);
    }

    public MapCollectionData withNewMap(EndMapData map) {
        Map<String, EndMapData> newMaps = new HashMap<>(maps);
        newMaps.put(map.name(), map);
        return new MapCollectionData(Map.copyOf(newMaps), loadedMapName);
    }

    public MapCollectionData withUpdatedMap(EndMapData map) {
        Map<String, EndMapData> newMaps = new HashMap<>(maps);
        newMaps.put(map.name(), map);
        return new MapCollectionData(Map.copyOf(newMaps), loadedMapName);
    }

    public MapCollectionData withLoadedMap(String name) {
        return new MapCollectionData(maps, name);
    }

    public MapCollectionData withUnloadedMap() {
        return new MapCollectionData(maps, "");
    }

    public MapCollectionData withDeletedMap(String name) {
        Map<String, EndMapData> newMaps = new HashMap<>(maps);
        newMaps.remove(name);
        String newLoaded = loadedMapName.equals(name) ? "" : loadedMapName;
        return new MapCollectionData(Map.copyOf(newMaps), newLoaded);
    }
}
