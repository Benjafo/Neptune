package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server packet: player wants to buy an item from broker stock.
 */
public record BrokerPurchasePayload(int stockIndex) implements CustomPacketPayload {

    public static final Type<BrokerPurchasePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "broker_purchase"));

    public static final StreamCodec<ByteBuf, BrokerPurchasePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, BrokerPurchasePayload::stockIndex,
                    BrokerPurchasePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
