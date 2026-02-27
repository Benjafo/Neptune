package neptune.neptune.mixin;

import neptune.neptune.relic.NeptuneItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Ender Magnet: Doubles item pickup radius by pulling nearby items toward players
 * who have an Ender Magnet in their inventory.
 */
@Mixin(ItemEntity.class)
public class ItemPickupRadiusMixin {

    private static final double MAGNET_RANGE = 3.5;
    private static final double PULL_SPEED = 0.075;

    @Inject(method = "tick", at = @At("HEAD"))
    private void neptune$magnetPull(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) return;

        // Don't pull items that can't be picked up yet
        if (!self.isAlive() || self.hasPickUpDelay()) return;

        AABB searchBox = self.getBoundingBox().inflate(MAGNET_RANGE);
        List<ServerPlayer> nearbyPlayers = self.level().getEntitiesOfClass(ServerPlayer.class, searchBox);

        for (ServerPlayer player : nearbyPlayers) {
            if (hasMagnet(player)) {
                Vec3 direction = player.position().subtract(self.position());
                double distance = direction.length();
                if (distance > 0.5 && distance <= MAGNET_RANGE) {
                    Vec3 pull = direction.normalize().scale(PULL_SPEED);
                    self.setDeltaMovement(self.getDeltaMovement().add(pull));
                }
                break; // Pull toward first magnet holder found
            }
        }
    }

    private static boolean hasMagnet(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(NeptuneItems.ENDER_MAGNET)) {
                return true;
            }
        }
        return false;
    }
}
