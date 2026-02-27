package neptune.neptune.mixin;

import neptune.neptune.relic.RelicSetBonus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import neptune.neptune.relic.NeptuneItems;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Void set bonus: Elytra durability drains 15% slower.
 * Cancels 15% of elytra durability damage ticks for players with the completed Void set.
 */
@Mixin(ItemStack.class)
public class ElytraDurabilityMixin {

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"), cancellable = true)
    private void neptune$reduceElytraDurability(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (slot != EquipmentSlot.CHEST) return;
        if (!(entity instanceof ServerPlayer player)) return;

        ItemStack self = (ItemStack) (Object) this;
        if (!self.is(Items.ELYTRA) && !self.is(NeptuneItems.REINFORCED_ELYTRA)) return;

        if (RelicSetBonus.hasVoidBonus(player)) {
            if (player.getRandom().nextFloat() < RelicSetBonus.VOID_DURABILITY_REDUCTION) {
                ci.cancel();
            }
        }
    }
}
