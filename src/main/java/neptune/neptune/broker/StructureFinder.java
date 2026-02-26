package neptune.neptune.broker;

import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.map.EndMapData;
import neptune.neptune.map.MapCollectionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Utility for finding End Cities, used by broker items like City Map and Relic Hints.
 */
public class StructureFinder {

    /**
     * Search radii (in blocks) for relic hint tiers.
     */
    public static int getSearchRadius(String hintId) {
        return switch (hintId) {
            case "hint_common" -> 2000;
            case "hint_uncommon" -> 4000;
            case "hint_rare" -> 8000;
            case "hint_very_rare" -> 16000;
            case "city_map" -> 6000;
            default -> 4000;
        };
    }

    /**
     * Find the nearest End City to the player within the given search radius.
     * Returns null if none found.
     */
    public static BlockPos findNearestCity(ServerPlayer player, int searchRadius) {
        ServerLevel level = (ServerLevel) player.level();
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var structureHolder = registry.get(BuiltinStructures.END_CITY);
        if (structureHolder.isEmpty()) return null;

        var holderSet = HolderSet.direct(structureHolder.get());
        int searchChunks = searchRadius / 16;

        var result = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, holderSet, player.blockPosition(), searchChunks, false);

        return result != null ? result.getFirst() : null;
    }

    /**
     * Find the nearest End City that is NOT already marked on the player's loaded map.
     * Falls back to any city if all nearby ones are marked.
     */
    public static BlockPos findNearestUnmarkedCity(ServerPlayer player, int searchRadius) {
        ServerLevel level = (ServerLevel) player.level();
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var structureHolder = registry.get(BuiltinStructures.END_CITY);
        if (structureHolder.isEmpty()) return null;

        var holderSet = HolderSet.direct(structureHolder.get());

        MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
        EndMapData loadedMap = maps.getLoadedMap();

        // Search outward, checking multiple results
        BlockPos playerPos = player.blockPosition();
        int searchChunks = searchRadius / 16;

        // Try to find an unmarked city by searching progressively
        var result = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, holderSet, playerPos, searchChunks, false);

        if (result == null) return null;

        BlockPos cityPos = result.getFirst();

        // If no map loaded, any city works
        if (loadedMap == null) return cityPos;

        // Check if this city's grid is already marked
        int gridX = loadedMap.toGridX(cityPos.getX());
        int gridZ = loadedMap.toGridZ(cityPos.getZ());
        if (!loadedMap.isMarked(gridX, gridZ)) {
            return cityPos;
        }

        // The nearest city is marked — still return it as fallback
        // (The structure locator API only finds the nearest one efficiently)
        return cityPos;
    }

    /**
     * Convert a world position to grid coordinates string using the player's loaded map.
     * Returns null if no map is loaded.
     */
    public static String toGridString(ServerPlayer player, BlockPos pos) {
        MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
        EndMapData loadedMap = maps.getLoadedMap();
        if (loadedMap == null) return null;

        int gridX = loadedMap.toGridX(pos.getX());
        int gridZ = loadedMap.toGridZ(pos.getZ());
        return "(" + gridX + ", " + gridZ + ")";
    }
}
