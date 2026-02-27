package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: player wants to sell or extract in Breakdown Table.
 */
public record BreakdownActionPayload(String action, int slotIndex) implements CustomPacketPayload {

    public static final Type<BreakdownActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "breakdown_action"));

    public static final StreamCodec<ByteBuf, BreakdownActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, BreakdownActionPayload::action,
                    ByteBufCodecs.INT, BreakdownActionPayload::slotIndex,
                    BreakdownActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
