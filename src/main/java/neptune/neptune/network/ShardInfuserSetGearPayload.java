package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: insert gear into shard infuser.
 */
public record ShardInfuserSetGearPayload(BlockPos pos, int inventorySlot) implements CustomPacketPayload {

    public static final Type<ShardInfuserSetGearPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "shard_infuser_set_gear"));

    public static final StreamCodec<ByteBuf, ShardInfuserSetGearPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ShardInfuserSetGearPayload::pos,
                    ByteBufCodecs.INT, ShardInfuserSetGearPayload::inventorySlot,
                    ShardInfuserSetGearPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
