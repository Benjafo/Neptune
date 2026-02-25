package neptune.neptune.broker;

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

    public static void register() {
        // Force static initialization
    }
}
