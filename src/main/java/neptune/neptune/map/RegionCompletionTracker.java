package neptune.neptune.map;

import neptune.neptune.Neptune;
import neptune.neptune.challenge.ChallengeTracker;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks player grid visits and detects 5x5 region completions.
 * Runs on server tick to detect when players enter new grid squares.
 * Requires CATALOG_T3 unlock for region completion rewards.
 */
public class RegionCompletionTracker {

    private static final int REGION_REWARD = 200;

    // Track last known grid position per player to detect grid changes
    private static final Map<java.util.UUID, String> lastGridPositions = new HashMap<>();

    public static void register() {
        // Clean up tracking on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            lastGridPositions.remove(handler.player.getUUID());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.level().dimension() != Level.END) continue;

                MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                EndMapData loadedMap = maps.getLoadedMap();
                if (loadedMap == null) continue;

                int gridX = loadedMap.toGridX(player.getX());
                int gridZ = loadedMap.toGridZ(player.getZ());
                String currentKey = EndMapData.gridKey(gridX, gridZ);

                // Check if player moved to a new grid
                String lastKey = lastGridPositions.get(player.getUUID());
                if (currentKey.equals(lastKey)) continue;

                lastGridPositions.put(player.getUUID(), currentKey);

                // Record grid visit
                EndMapData updated = loadedMap.withVisit(gridX, gridZ);
                if (updated == null) continue; // Already visited

                player.setAttached(NeptuneAttachments.MAPS, maps.withUpdatedMap(updated));

                // Track challenge progress (total visited grids across this map)
                ChallengeTracker.onGridVisited(player, updated.getVisitedCount());

                // Check region completion if player has CATALOG_T3
                UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
                if (!unlocks.hasTier(UnlockBranch.CATALOG, 3)) continue;

                checkRegionCompletion(player, updated, maps, gridX, gridZ);
            }
        });
    }

    /**
     * Check if visiting this grid completed a 5x5 region.
     */
    private static void checkRegionCompletion(ServerPlayer player, EndMapData map,
                                               MapCollectionData maps, int gridX, int gridZ) {
        int regionX = EndMapData.toRegionCoord(gridX);
        int regionZ = EndMapData.toRegionCoord(gridZ);

        // Already completed this region?
        if (map.isRegionCompleted(regionX, regionZ)) return;

        // Check if all 25 grids in this region are visited
        if (!map.isRegionFullyVisited(regionX, regionZ)) return;

        // Region complete! Mark it and award essence
        EndMapData completed = map.withCompletedRegion(regionX, regionZ);
        player.setAttached(NeptuneAttachments.MAPS, maps.withUpdatedMap(completed));

        // Award essence
        VoidEssenceData essence = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
        player.setAttached(NeptuneAttachments.VOID_ESSENCE, essence.add(REGION_REWARD));

        // Notify player
        int startX = regionX * 5;
        int startZ = regionZ * 5;
        player.sendSystemMessage(Component.literal(
                "§6§l\u2726 Region Complete! §e[" + startX + "," + startZ + "] to ["
                        + (startX + 4) + "," + (startZ + 4) + "] §6§l\u2726"));
        player.sendSystemMessage(Component.literal(
                "§a+" + REGION_REWARD + " void essence §7(all 25 grids explored)"));

        Neptune.LOGGER.info("[Neptune] {} completed region [{},{}] on map '{}', awarded {} essence",
                player.getName().getString(), regionX, regionZ, completed.name(), REGION_REWARD);

        // Track lifetime essence for challenges
        ChallengeTracker.onLifetimeEssenceUpdated(player);
    }

}
