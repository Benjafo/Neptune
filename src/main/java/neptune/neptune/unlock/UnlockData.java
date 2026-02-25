package neptune.neptune.unlock;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

/**
 * Tracks which unlocks a player has purchased.
 * Immutable — returns new instances on modification.
 */
public record UnlockData(Set<String> unlockedIds) {

    public static final UnlockData EMPTY = new UnlockData(Set.of());

    public static final Codec<UnlockData> CODEC = Codec.STRING.listOf()
            .xmap(
                    list -> new UnlockData(new HashSet<>(list)),
                    data -> new ArrayList<>(data.unlockedIds)
            );

    public static final StreamCodec<ByteBuf, UnlockData> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .apply(ByteBufCodecs.list())
            .map(
                    list -> new UnlockData(new HashSet<>(list)),
                    data -> new ArrayList<>(data.unlockedIds)
            );

    public boolean hasUnlock(UnlockType type) {
        return unlockedIds.contains(type.name());
    }

    public UnlockData withUnlock(UnlockType type) {
        Set<String> newSet = new HashSet<>(unlockedIds);
        newSet.add(type.name());
        return new UnlockData(Set.copyOf(newSet));
    }

    /**
     * Check if player has reached at least the given tier in the given branch.
     */
    public boolean hasTier(UnlockBranch branch, int tier) {
        for (UnlockType type : UnlockType.values()) {
            if (type.getBranch() == branch && type.getTier() == tier && hasUnlock(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the highest tier unlocked in a branch (0 if none).
     */
    public int getHighestTier(UnlockBranch branch) {
        int highest = 0;
        for (UnlockType type : UnlockType.values()) {
            if (type.getBranch() == branch && hasUnlock(type)) {
                highest = Math.max(highest, type.getTier());
            }
        }
        return highest;
    }
}
