package neptune.neptune.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/**
 * Tracks per-player placed block positions for Processing blocks.
 * Each player can have at most one Breakdown Table and one Shard Infuser.
 */
public record BlockPlacementsData(
        Optional<BlockPos> breakdownTablePos,
        Optional<BlockPos> shardInfuserPos
) {
    public static final BlockPlacementsData EMPTY = new BlockPlacementsData(Optional.empty(), Optional.empty());

    public static final Codec<BlockPlacementsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.optionalFieldOf("breakdownTablePos")
                            .forGetter(BlockPlacementsData::breakdownTablePos),
                    BlockPos.CODEC.optionalFieldOf("shardInfuserPos")
                            .forGetter(BlockPlacementsData::shardInfuserPos)
            ).apply(instance, BlockPlacementsData::new)
    );

    public BlockPlacementsData withBreakdownTable(BlockPos pos) {
        return new BlockPlacementsData(Optional.of(pos), shardInfuserPos);
    }

    public BlockPlacementsData withoutBreakdownTable() {
        return new BlockPlacementsData(Optional.empty(), shardInfuserPos);
    }

    public BlockPlacementsData withShardInfuser(BlockPos pos) {
        return new BlockPlacementsData(breakdownTablePos, Optional.of(pos));
    }

    public BlockPlacementsData withoutShardInfuser() {
        return new BlockPlacementsData(breakdownTablePos, Optional.empty());
    }

    public boolean hasBreakdownTable() {
        return breakdownTablePos.isPresent();
    }

    public boolean hasShardInfuser() {
        return shardInfuserPos.isPresent();
    }
}
