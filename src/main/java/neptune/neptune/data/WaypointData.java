package neptune.neptune.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record WaypointData(List<Waypoint> waypoints) {

    public static final int MAX_WAYPOINTS = 3;

    public record Waypoint(String name, BlockPos pos) {
        public static final Codec<Waypoint> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("name").forGetter(Waypoint::name),
                        BlockPos.CODEC.fieldOf("pos").forGetter(Waypoint::pos)
                ).apply(instance, Waypoint::new)
        );
    }

    public static final WaypointData EMPTY = new WaypointData(List.of());

    public static final Codec<WaypointData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Waypoint.CODEC.listOf().fieldOf("waypoints").forGetter(WaypointData::waypoints)
            ).apply(instance, WaypointData::new)
    );

    public boolean isFull() {
        return waypoints.size() >= MAX_WAYPOINTS;
    }

    public WaypointData addWaypoint(String name, BlockPos pos) {
        if (isFull()) return this;
        List<Waypoint> newList = new ArrayList<>(waypoints);
        newList.add(new Waypoint(name, pos));
        return new WaypointData(List.copyOf(newList));
    }

    public WaypointData removeWaypoint(BlockPos pos) {
        List<Waypoint> newList = new ArrayList<>();
        for (Waypoint wp : waypoints) {
            if (!wp.pos().equals(pos)) {
                newList.add(wp);
            }
        }
        return new WaypointData(List.copyOf(newList));
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public int getNextWaypointNumber() {
        int max = 0;
        for (Waypoint wp : waypoints) {
            if (wp.name().startsWith("Waypoint ")) {
                try {
                    int num = Integer.parseInt(wp.name().substring(9));
                    max = Math.max(max, num);
                } catch (NumberFormatException ignored) {}
            }
        }
        return max + 1;
    }
}
