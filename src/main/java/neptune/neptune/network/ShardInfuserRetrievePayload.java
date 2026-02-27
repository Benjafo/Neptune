package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: retrieve gear from shard infuser.
 */
public record ShardInfuserRetrievePayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<ShardInfuserRetrievePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "shard_infuser_retrieve"));

    public static final StreamCodec<ByteBuf, ShardInfuserRetrievePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ShardInfuserRetrievePayload::pos,
                    ShardInfuserRetrievePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
