package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * S2C: sync infusable relics to the Relic Infuser screen.
 */
public record RelicInfuserSyncPayload(List<InfusableEntry> entries) implements CustomPacketPayload {

    public record InfusableEntry(String relicId, String displayName, int duplicateCount) {
        public static final StreamCodec<ByteBuf, InfusableEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, InfusableEntry::relicId,
                ByteBufCodecs.STRING_UTF8, InfusableEntry::displayName,
                ByteBufCodecs.INT, InfusableEntry::duplicateCount,
                InfusableEntry::new
        );
    }

    public static final Type<RelicInfuserSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "relic_infuser_sync"));

    public static final StreamCodec<ByteBuf, RelicInfuserSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    InfusableEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), RelicInfuserSyncPayload::entries,
                    RelicInfuserSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
