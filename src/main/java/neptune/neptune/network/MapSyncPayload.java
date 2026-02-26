package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Server-to-client packet: sync the loaded map data for rendering.
 */
public record MapSyncPayload(
        String mapName,
        int gridSize,
        List<String> markedGrids
) implements CustomPacketPayload {

    public static final Type<MapSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "map_sync"));

    public static final StreamCodec<ByteBuf, MapSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MapSyncPayload::mapName,
            ByteBufCodecs.INT, MapSyncPayload::gridSize,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MapSyncPayload::markedGrids,
            MapSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
