package neptune.neptune.processing;

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
            BlockEntityType.Builder.of(BreakdownTableBlockEntity::new, BREAKDOWN_TABLE).build()
    );

    public static final BlockEntityType<ShardInfuserBlockEntity> SHARD_INFUSER_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("neptune", "shard_infuser_be"),
            BlockEntityType.Builder.of(ShardInfuserBlockEntity::new, SHARD_INFUSER).build()
    );

    public static void register() {
        // Force static initialization
    }
}
