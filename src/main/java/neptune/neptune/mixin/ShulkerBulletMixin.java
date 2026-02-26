package neptune.neptune.mixin;

import com.google.common.base.MoreObjects;
import neptune.neptune.relic.RelicSetBonus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inhabitants set bonus: Shulker status effect -20% duration, shulker bullets deal 25% less damage.
 * Overrides the onHitEntity method to apply reduced effects for players with the completed Inhabitants set.
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin {

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void neptune$reduceShulkerEffects(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity target = entityHitResult.getEntity();
        if (!(target instanceof ServerPlayer player)) return;
        if (!RelicSetBonus.hasInhabitantsBonus(player)) return;

        // Cancel vanilla behavior and apply our modified version
        ci.cancel();

        ShulkerBullet self = (ShulkerBullet) (Object) this;
        Entity owner = self.getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity ? (LivingEntity) owner : null;
        DamageSource damageSource = self.damageSources().mobProjectile(self, livingOwner);

        // Reduced damage: 4.0 * (1 - 0.25) = 3.0
        float reducedDamage = 4.0f * (1.0f - RelicSetBonus.INHABITANTS_DAMAGE_REDUCTION);
        boolean hit = target.hurtOrSimulate(damageSource, reducedDamage);

        if (hit) {
            if (self.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
            }

            // Reduced levitation duration: 200 * (1 - 0.20) = 160 ticks
            int reducedDuration = (int) (200 * (1.0f - RelicSetBonus.INHABITANTS_DURATION_REDUCTION));
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, reducedDuration),
                    MoreObjects.firstNonNull(owner, self));
        }
    }
}
