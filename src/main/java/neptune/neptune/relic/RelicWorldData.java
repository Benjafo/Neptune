package neptune.neptune.relic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.Set;

/**
 * World-level saved data tracking which end city structures have already been
 * "rolled" for relic spawning. Prevents multiple relics per city.
 */
public class RelicWorldData extends SavedData {

    private final Set<Long> rolledStructures;

    public RelicWorldData() {
        this(new HashSet<>());
    }

    public RelicWorldData(Set<Long> rolledStructures) {
        this.rolledStructures = new HashSet<>(rolledStructures);
    }

    public static final Codec<RelicWorldData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.listOf()
                            .xmap(list -> (Set<Long>) new HashSet<>(list), set -> set.stream().toList())
                            .fieldOf("rolledStructures")
                            .forGetter(d -> d.rolledStructures)
            ).apply(instance, RelicWorldData::new)
    );

    public static final SavedDataType<RelicWorldData> TYPE = new SavedDataType<>(
            "neptune_relic_world",
            RelicWorldData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public boolean hasBeenRolled(long packedChunkPos) {
        return rolledStructures.contains(packedChunkPos);
    }

    public void markRolled(long packedChunkPos) {
        rolledStructures.add(packedChunkPos);
        setDirty();
    }

    public static RelicWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}
