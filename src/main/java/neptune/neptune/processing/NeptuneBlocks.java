package neptune.neptune.processing;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class NeptuneBlocks {

    public static final Block BREAKDOWN_TABLE = Registry.register(
            BuiltInRegistries.BLOCK,
            Identifier.fromNamespaceAndPath("neptune", "breakdown_table"),
            new BreakdownTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion())
    );

    public static final Block SHARD_INFUSER = Registry.register(
            BuiltInRegistries.BLOCK,
            Identifier.fromNamespaceAndPath("neptune", "shard_infuser"),
            new ShardInfuserBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion())
    );

    public static final Item BREAKDOWN_TABLE_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "breakdown_table"),
            new BlockItem(BREAKDOWN_TABLE, new Item.Properties())
    );

    public static final Item SHARD_INFUSER_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "shard_infuser"),
            new BlockItem(SHARD_INFUSER, new Item.Properties())
    );

    public static final BlockEntityType<BreakdownTableBlockEntity> BREAKDOWN_TABLE_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("neptune", "breakdown_table_be"),
            FabricBlockEntityTypeBuilder.create(BreakdownTableBlockEntity::new, BREAKDOWN_TABLE).build()
    );

    public static final BlockEntityType<ShardInfuserBlockEntity> SHARD_INFUSER_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("neptune", "shard_infuser_be"),
            FabricBlockEntityTypeBuilder.create(ShardInfuserBlockEntity::new, SHARD_INFUSER).build()
    );

    // Relic Infuser
    public static final Block RELIC_INFUSER = Registry.register(
            BuiltInRegistries.BLOCK,
            Identifier.fromNamespaceAndPath("neptune", "relic_infuser"),
            new RelicInfuserBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion())
    );

    public static final Item RELIC_INFUSER_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "relic_infuser"),
            new BlockItem(RELIC_INFUSER, new Item.Properties())
    );

    public static final BlockEntityType<RelicInfuserBlockEntity> RELIC_INFUSER_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("neptune", "relic_infuser_be"),
            FabricBlockEntityTypeBuilder.create(RelicInfuserBlockEntity::new, RELIC_INFUSER).build()
    );

    // Waypoint Beacon
    public static final Block WAYPOINT_BEACON = Registry.register(
            BuiltInRegistries.BLOCK,
            Identifier.fromNamespaceAndPath("neptune", "waypoint_beacon"),
            new WaypointBeaconBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> 10))
    );

    public static final Item WAYPOINT_BEACON_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "waypoint_beacon"),
            new BlockItem(WAYPOINT_BEACON, new Item.Properties())
    );

    public static final BlockEntityType<WaypointBeaconBlockEntity> WAYPOINT_BEACON_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("neptune", "waypoint_beacon_be"),
            FabricBlockEntityTypeBuilder.create(WaypointBeaconBlockEntity::new, WAYPOINT_BEACON).build()
    );

    public static void register() {
        // Force static initialization
    }
}
