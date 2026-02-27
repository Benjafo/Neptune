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
        Optional<BlockPos> shardInfuserPos,
        Optional<BlockPos> relicInfuserPos
) {
    public static final BlockPlacementsData EMPTY = new BlockPlacementsData(Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<BlockPlacementsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.optionalFieldOf("breakdownTablePos")
                            .forGetter(BlockPlacementsData::breakdownTablePos),
                    BlockPos.CODEC.optionalFieldOf("shardInfuserPos")
                            .forGetter(BlockPlacementsData::shardInfuserPos),
                    BlockPos.CODEC.optionalFieldOf("relicInfuserPos")
                            .forGetter(BlockPlacementsData::relicInfuserPos)
            ).apply(instance, BlockPlacementsData::new)
    );

    public BlockPlacementsData withBreakdownTable(BlockPos pos) {
        return new BlockPlacementsData(Optional.of(pos), shardInfuserPos, relicInfuserPos);
    }

    public BlockPlacementsData withoutBreakdownTable() {
        return new BlockPlacementsData(Optional.empty(), shardInfuserPos, relicInfuserPos);
    }

    public BlockPlacementsData withShardInfuser(BlockPos pos) {
        return new BlockPlacementsData(breakdownTablePos, Optional.of(pos), relicInfuserPos);
    }

    public BlockPlacementsData withoutShardInfuser() {
        return new BlockPlacementsData(breakdownTablePos, Optional.empty(), relicInfuserPos);
    }

    public BlockPlacementsData withRelicInfuser(BlockPos pos) {
        return new BlockPlacementsData(breakdownTablePos, shardInfuserPos, Optional.of(pos));
    }

    public BlockPlacementsData withoutRelicInfuser() {
        return new BlockPlacementsData(breakdownTablePos, shardInfuserPos, Optional.empty());
    }

    public boolean hasBreakdownTable() {
        return breakdownTablePos.isPresent();
    }

    public boolean hasShardInfuser() {
        return shardInfuserPos.isPresent();
    }

    public boolean hasRelicInfuser() {
        return relicInfuserPos.isPresent();
    }
}
