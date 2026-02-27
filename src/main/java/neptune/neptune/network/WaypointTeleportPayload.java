package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: player wants to teleport to a waypoint.
 */
public record WaypointTeleportPayload(BlockPos fromPos, int waypointIndex) implements CustomPacketPayload {

    public static final Type<WaypointTeleportPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "waypoint_teleport"));

    public static final StreamCodec<ByteBuf, WaypointTeleportPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, WaypointTeleportPayload::fromPos,
                    ByteBufCodecs.INT, WaypointTeleportPayload::waypointIndex,
                    WaypointTeleportPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
