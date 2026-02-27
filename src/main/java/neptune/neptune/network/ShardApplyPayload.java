package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: apply enchantment to gear in shard infuser.
 */
public record ShardApplyPayload(BlockPos pos, int enchantIndex) implements CustomPacketPayload {

    public static final Type<ShardApplyPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "shard_apply"));

    public static final StreamCodec<ByteBuf, ShardApplyPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ShardApplyPayload::pos,
                    ByteBufCodecs.INT, ShardApplyPayload::enchantIndex,
                    ShardApplyPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
