package neptune.neptune.entity;

import neptune.neptune.broker.BrokerMenu;
import neptune.neptune.broker.RotatingStock;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.RotatingStockData;
import neptune.neptune.network.NeptuneNetworking;
import neptune.neptune.relic.RelicJournalData;
import neptune.neptune.unlock.UnlockData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class BrokerEntity extends Mob {

    private static final Component BROKER_TITLE = Component.literal("Broker");

    public BrokerEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setNoAi(true);
        this.setPersistenceRequired();
        this.setCustomName(BROKER_TITLE);
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createBrokerAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    // --- Invulnerability ---

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    // --- Prevent despawn ---

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return false;
    }

    // --- Prevent movement ---

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
        // Cannot be pushed
    }

    @Override
    protected void registerGoals() {
        // No AI goals
    }

    // --- Right-click interaction ---

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Refresh rotating stock if needed
            refreshRotatingStock(serverPlayer);

            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, playerAccess) ->
                            new BrokerMenu(containerId, playerInventory),
                    BROKER_TITLE
            ));

            // Sync rotating stock to client
            NeptuneNetworking.syncRotatingStockToClient(serverPlayer);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    private void refreshRotatingStock(ServerPlayer player) {
        RotatingStockData stockData = player.getAttachedOrCreate(NeptuneAttachments.ROTATING_STOCK);
        long gameTime = player.level().getGameTime();

        if (RotatingStock.shouldRefresh(stockData, gameTime)) {
            UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
            RelicJournalData journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
            List<String> newItems = RotatingStock.selectRotation(unlocks, stockData, journal);
            player.setAttached(NeptuneAttachments.ROTATING_STOCK, stockData.withItems(newItems, gameTime));
        }
    }
}
