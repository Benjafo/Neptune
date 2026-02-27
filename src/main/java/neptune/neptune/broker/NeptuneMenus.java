package neptune.neptune.broker;

import neptune.neptune.processing.BreakdownTableMenu;
import neptune.neptune.processing.ShardInfuserMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class NeptuneMenus {

    public static final MenuType<BrokerMenu> BROKER_MENU = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("neptune", "broker"),
            new MenuType<>(BrokerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final ExtendedScreenHandlerType<BreakdownTableMenu, BlockPos> BREAKDOWN_TABLE_MENU = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("neptune", "breakdown_table"),
            new ExtendedScreenHandlerType<>(BreakdownTableMenu::new, BlockPos.STREAM_CODEC)
    );

    public static final ExtendedScreenHandlerType<ShardInfuserMenu, BlockPos> SHARD_INFUSER_MENU = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("neptune", "shard_infuser"),
            new ExtendedScreenHandlerType<>(ShardInfuserMenu::new, BlockPos.STREAM_CODEC)
    );

    public static void register() {
        // Force static initialization
    }
}
