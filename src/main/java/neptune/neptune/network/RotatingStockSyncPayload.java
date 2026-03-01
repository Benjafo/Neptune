package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * S2C: sync rotating stock data to the client for BrokerScreen rendering.
 */
public record RotatingStockSyncPayload(List<RotatingEntry> entries) implements CustomPacketPayload {

    public record RotatingEntry(String id, String name, int cost, String description) {
        public static final StreamCodec<ByteBuf, RotatingEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, RotatingEntry::id,
                ByteBufCodecs.STRING_UTF8, RotatingEntry::name,
                ByteBufCodecs.INT, RotatingEntry::cost,
                ByteBufCodecs.STRING_UTF8, RotatingEntry::description,
                RotatingEntry::new
        );
    }

    public static final Type<RotatingStockSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "rotating_stock_sync"));

    public static final StreamCodec<ByteBuf, RotatingStockSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    RotatingEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), RotatingStockSyncPayload::entries,
                    RotatingStockSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
