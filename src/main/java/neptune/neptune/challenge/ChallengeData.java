package neptune.neptune.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

/**
 * Tracks challenge progress and completion for a player.
 * Immutable — returns new instances on modification.
 */
public record ChallengeData(Map<String, Integer> progress, Set<String> completed) {

    public static final ChallengeData EMPTY = new ChallengeData(Map.of(), Set.of());

    public static final Codec<ChallengeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.INT)
                            .fieldOf("progress")
                            .forGetter(d -> new HashMap<>(d.progress)),
                    Codec.STRING.listOf()
                            .xmap(l -> (Set<String>) new HashSet<>(l), s -> new ArrayList<>(s))
                            .fieldOf("completed")
                            .forGetter(ChallengeData::completed)
            ).apply(instance, ChallengeData::new)
    );

    public static final StreamCodec<ByteBuf, ChallengeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT),
            ChallengeData::progress,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(HashSet::new, ArrayList::new),
            ChallengeData::completed,
            ChallengeData::new
    );

    public int getProgress(ChallengeType type) {
        return progress.getOrDefault(type.name(), 0);
    }

    public boolean isCompleted(ChallengeType type) {
        return completed.contains(type.name());
    }

    /**
     * Set progress for a challenge. Auto-completes if target is reached.
     */
    public ChallengeData withProgress(ChallengeType type, int value) {
        Map<String, Integer> newProgress = new HashMap<>(progress);
        Set<String> newCompleted = new HashSet<>(completed);

        newProgress.put(type.name(), value);
        if (value >= type.getTargetValue()) {
            newCompleted.add(type.name());
        }

        return new ChallengeData(Map.copyOf(newProgress), Set.copyOf(newCompleted));
    }

    /**
     * Increment progress by an amount. Auto-completes if target is reached.
     */
    public ChallengeData incrementProgress(ChallengeType type, int amount) {
        int current = getProgress(type);
        return withProgress(type, current + amount);
    }

    /**
     * Mark a challenge as complete (sets progress to target).
     */
    public ChallengeData withCompleted(ChallengeType type) {
        return withProgress(type, type.getTargetValue());
    }

    /**
     * Count how many challenges of a given category are completed.
     */
    public int completedInCategory(ChallengeCategory category) {
        int count = 0;
        for (ChallengeType type : ChallengeType.values()) {
            if (type.getCategory() == category && isCompleted(type)) {
                count++;
            }
        }
        return count;
    }

    public int totalCompleted() {
        return completed.size();
    }
}
