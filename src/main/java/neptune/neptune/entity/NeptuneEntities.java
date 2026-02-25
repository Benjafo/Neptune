package neptune.neptune.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class NeptuneEntities {

    public static final Identifier BROKER_ID = Identifier.fromNamespaceAndPath("neptune", "broker");
    public static final ResourceKey<EntityType<?>> BROKER_KEY = ResourceKey.create(Registries.ENTITY_TYPE, BROKER_ID);

    public static final EntityType<BrokerEntity> BROKER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            BROKER_KEY,
            EntityType.Builder.<BrokerEntity>of(BrokerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(BROKER_KEY)
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(BROKER, BrokerEntity.createBrokerAttributes());
    }
}
