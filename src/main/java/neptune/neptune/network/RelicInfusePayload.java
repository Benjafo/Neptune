package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: player wants to infuse a relic at the Relic Infuser.
 */
public record RelicInfusePayload(BlockPos pos, String relicId, String buffChoice) implements CustomPacketPayload {

    public static final Type<RelicInfusePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "relic_infuse"));

    public static final StreamCodec<ByteBuf, RelicInfusePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, RelicInfusePayload::pos,
                    ByteBufCodecs.STRING_UTF8, RelicInfusePayload::relicId,
                    ByteBufCodecs.STRING_UTF8, RelicInfusePayload::buffChoice,
                    RelicInfusePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
