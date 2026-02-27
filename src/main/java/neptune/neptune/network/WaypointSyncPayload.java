package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * S2C: sync waypoint data to the Waypoint screen.
 */
public record WaypointSyncPayload(List<WaypointEntry> waypoints) implements CustomPacketPayload {

    public record WaypointEntry(String name, BlockPos pos, int teleportCost) {
        public static final StreamCodec<ByteBuf, WaypointEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, WaypointEntry::name,
                BlockPos.STREAM_CODEC, WaypointEntry::pos,
                ByteBufCodecs.INT, WaypointEntry::teleportCost,
                WaypointEntry::new
        );
    }

    public static final Type<WaypointSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "waypoint_sync"));

    public static final StreamCodec<ByteBuf, WaypointSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    WaypointEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), WaypointSyncPayload::waypoints,
                    WaypointSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
