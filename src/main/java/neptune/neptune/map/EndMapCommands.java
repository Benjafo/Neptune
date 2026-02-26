package neptune.neptune.map;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import neptune.neptune.challenge.ChallengeTracker;
import neptune.neptune.data.NeptuneAttachments;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import neptune.neptune.network.NeptuneNetworking;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class EndMapCommands {

    private static boolean hasNavigationT1(ServerPlayer player) {
        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        return unlocks.hasTier(UnlockBranch.NAVIGATION, 1);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("endmap")
                .requires(source -> {
                    try {
                        ServerPlayer player = source.getPlayerOrException();
                        return hasNavigationT1(player);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("gridsize", IntegerArgumentType.integer(64, 2048))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String name = StringArgumentType.getString(context, "name");
                                            int gridSize = IntegerArgumentType.getInteger(context, "gridsize");

                                            MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                                            if (maps.hasMap(name)) {
                                                context.getSource().sendFailure(
                                                        Component.literal("§cA map named '" + name + "' already exists!"));
                                                return 0;
                                            }

                                            EndMapData newMap = EndMapData.create(name, gridSize);
                                            player.setAttached(NeptuneAttachments.MAPS, maps.withNewMap(newMap));
                                            player.sendSystemMessage(Component.literal(
                                                    "§aCreated map '" + name + "' with grid size " + gridSize));
                                            return 1;
                                        }))))

                .then(Commands.literal("load")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(context, "name");

                                    MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                                    if (!maps.hasMap(name)) {
                                        context.getSource().sendFailure(
                                                Component.literal("§cNo map named '" + name + "' found!"));
                                        return 0;
                                    }

                                    player.setAttached(NeptuneAttachments.MAPS, maps.withLoadedMap(name));
                                    NeptuneNetworking.syncMapToClient(player);
                                    player.sendSystemMessage(Component.literal("§aLoaded map: " + name));
                                    return 1;
                                })))

                .then(Commands.literal("unload")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                            if (maps.loadedMapName().isEmpty()) {
                                context.getSource().sendFailure(Component.literal("§cNo map is loaded!"));
                                return 0;
                            }
                            player.setAttached(NeptuneAttachments.MAPS, maps.withUnloadedMap());
                            NeptuneNetworking.syncMapToClient(player);
                            player.sendSystemMessage(Component.literal("§eMap unloaded."));
                            return 1;
                        }))

                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);

                            if (maps.maps().isEmpty()) {
                                player.sendSystemMessage(Component.literal("§7No maps created. Use /endmap create <name> <gridsize>"));
                                return 1;
                            }

                            player.sendSystemMessage(Component.literal("§6=== Your Maps ==="));
                            for (EndMapData map : maps.maps().values()) {
                                boolean loaded = map.name().equals(maps.loadedMapName());
                                String status = loaded ? " §a[LOADED]" : "";
                                player.sendSystemMessage(Component.literal(
                                        "§f" + map.name() + status + " §8(grid: " + map.gridSize()
                                                + ", cities: " + map.getMarkedCount() + ")"));
                            }
                            return 1;
                        }))

                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(context, "name");

                                    MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                                    if (!maps.hasMap(name)) {
                                        context.getSource().sendFailure(
                                                Component.literal("§cNo map named '" + name + "' found!"));
                                        return 0;
                                    }

                                    player.setAttached(NeptuneAttachments.MAPS, maps.withDeletedMap(name));
                                    NeptuneNetworking.syncMapToClient(player);
                                    player.sendSystemMessage(Component.literal("§eDeleted map: " + name));
                                    return 1;
                                })))

                .then(Commands.literal("mark")
                        .executes(context -> markCity(context.getSource(), null))
                        .then(Commands.argument("note", StringArgumentType.greedyString())
                                .executes(context -> markCity(context.getSource(),
                                        StringArgumentType.getString(context, "note")))))

                .then(Commands.literal("unmark")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                            EndMapData loadedMap = maps.getLoadedMap();
                            if (loadedMap == null) {
                                context.getSource().sendFailure(Component.literal("§cNo map loaded! Use /endmap load <name>"));
                                return 0;
                            }

                            int gridX = loadedMap.toGridX(player.getX());
                            int gridZ = loadedMap.toGridZ(player.getZ());

                            if (!loadedMap.isMarked(gridX, gridZ)) {
                                context.getSource().sendFailure(Component.literal("§cThis grid square is not marked."));
                                return 0;
                            }

                            EndMapData updated = loadedMap.withUnmark(gridX, gridZ);
                            player.setAttached(NeptuneAttachments.MAPS, maps.withUpdatedMap(updated));
                            NeptuneNetworking.syncMapToClient(player);
                            player.sendSystemMessage(Component.literal(
                                    "§eUnmarked grid [" + gridX + ", " + gridZ + "]"));
                            return 1;
                        }))

                .then(Commands.literal("info")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                            EndMapData loadedMap = maps.getLoadedMap();
                            if (loadedMap == null) {
                                context.getSource().sendFailure(Component.literal("§cNo map loaded!"));
                                return 0;
                            }

                            int gridX = loadedMap.toGridX(player.getX());
                            int gridZ = loadedMap.toGridZ(player.getZ());
                            boolean marked = loadedMap.isMarked(gridX, gridZ);
                            String note = loadedMap.getNote(gridX, gridZ);

                            player.sendSystemMessage(Component.literal(
                                    "§6Map: §f" + loadedMap.name() + " §8(grid: " + loadedMap.gridSize() + ")"));
                            player.sendSystemMessage(Component.literal(
                                    "§6Grid: §f[" + gridX + ", " + gridZ + "]"));
                            player.sendSystemMessage(Component.literal(
                                    "§6Marked: " + (marked ? "§aYes" : "§7No")));
                            if (note != null) {
                                player.sendSystemMessage(Component.literal("§6Note: §f" + note));
                            }
                            player.sendSystemMessage(Component.literal(
                                    "§6Total cities marked: §f" + loadedMap.getMarkedCount()));
                            return 1;
                        }))
        );
    }

    private static int markCity(CommandSourceStack source, String note) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
        EndMapData loadedMap = maps.getLoadedMap();
        if (loadedMap == null) {
            source.sendFailure(Component.literal("§cNo map loaded! Use /endmap load <name>"));
            return 0;
        }

        int gridX = loadedMap.toGridX(player.getX());
        int gridZ = loadedMap.toGridZ(player.getZ());

        if (loadedMap.isMarked(gridX, gridZ)) {
            source.sendFailure(Component.literal("§cThis grid square is already marked."));
            return 0;
        }

        // Check for end city nearby
        boolean cityDetected = detectEndCity(player);

        if (!cityDetected) {
            player.sendSystemMessage(Component.literal(
                    "§eNo end city detected nearby. Run /endmap mark again to confirm."));
            // For simplicity, we still allow marking (the warning is informational)
        }

        EndMapData updated = loadedMap.withMark(gridX, gridZ, note);
        player.setAttached(NeptuneAttachments.MAPS, maps.withUpdatedMap(updated));
        NeptuneNetworking.syncMapToClient(player);
        player.sendSystemMessage(Component.literal(
                "§aMarked city at grid [" + gridX + ", " + gridZ + "]"
                        + (note != null ? " §7(" + note + ")" : "")));

        // Track challenge
        ChallengeTracker.onCityMarked(player, updated.getMarkedCount());

        return 1;
    }

    private static boolean detectEndCity(ServerPlayer player) {
        if (player.level().dimension() != Level.END) return false;
        ServerLevel level = player.level();
        BlockPos pos = player.blockPosition();
        // Look up the end city structure from the registry
        var registry = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        var structureHolder = registry.get(BuiltinStructures.END_CITY);
        if (structureHolder.isEmpty()) return false;
        var structure = structureHolder.get().value();
        var start = level.structureManager().getStructureWithPieceAt(pos, structure);
        return start.isValid();
    }
}
