package neptune.neptune.relic;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class NeptuneItems {

    public static final Item RELIC = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "relic"),
            new RelicItem(new Item.Properties().stacksTo(1))
    );

    public static final Item ENCHANTMENT_SHARD = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "enchantment_shard"),
            new Item(new Item.Properties().stacksTo(64))
    );

    public static void register() {
        // Force static initialization
    }
}
