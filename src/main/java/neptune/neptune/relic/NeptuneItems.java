package neptune.neptune.relic;

import neptune.neptune.processing.*;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;

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

    public static final Item REINFORCED_ELYTRA = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "reinforced_elytra"),
            new ReinforcedElytraItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(432 * 2)
                    .component(DataComponents.GLIDER, net.minecraft.util.Unit.INSTANCE)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).build()))
    );

    public static final Item VOID_POUCH = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "void_pouch"),
            new VoidPouchItem(new Item.Properties().stacksTo(1))
    );

    public static final Item ENDER_MAGNET = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "ender_magnet"),
            new EnderMagnetItem(new Item.Properties().stacksTo(1))
    );

    public static final Item ELYTRA_BOOSTER = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "elytra_booster"),
            new ElytraBoosterItem(new Item.Properties().stacksTo(16))
    );

    public static final Item PORTABLE_ENDER_CHEST = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath("neptune", "portable_ender_chest"),
            new PortableEnderChestItem(new Item.Properties().stacksTo(1))
    );

    public static void register() {
        // Force static initialization
    }
}
