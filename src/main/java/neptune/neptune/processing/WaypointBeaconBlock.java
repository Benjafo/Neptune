package neptune.neptune.processing;

import com.mojang.serialization.MapCodec;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.WaypointData;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class WaypointBeaconBlock extends BaseEntityBlock {

    public static final MapCodec<WaypointBeaconBlock> CODEC = simpleCodec(WaypointBeaconBlock::new);

    public WaypointBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaypointBeaconBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide() || !(placer instanceof ServerPlayer player)) return;

        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        if (!unlocks.hasTier(UnlockBranch.NAVIGATION, 4)) {
            player.sendSystemMessage(Component.literal("§cRequires Navigation T4 (End Cartographer) to place!"));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.WAYPOINT_BEACON_ITEM));
            return;
        }

        // End dimension only
        if (level.dimension() != Level.END) {
            player.sendSystemMessage(Component.literal("§cWaypoint Beacons can only be placed in the End!"));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.WAYPOINT_BEACON_ITEM));
            return;
        }

        WaypointData waypoints = player.getAttachedOrCreate(NeptuneAttachments.WAYPOINTS);
        if (waypoints.isFull()) {
            player.sendSystemMessage(Component.literal("§cMaximum " + WaypointData.MAX_WAYPOINTS + " waypoints reached! Break one first."));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.WAYPOINT_BEACON_ITEM));
            return;
        }

        int wpNum = waypoints.getNextWaypointNumber();
        String wpName = "Waypoint " + wpNum;

        if (level.getBlockEntity(pos) instanceof WaypointBeaconBlockEntity be) {
            be.setOwnerUUID(player.getUUID());
            be.setWaypointName(wpName);
        }

        player.setAttached(NeptuneAttachments.WAYPOINTS, waypoints.addWaypoint(wpName, pos));
        player.sendSystemMessage(Component.literal("§a" + wpName + " placed!"));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
                @Override
                public BlockPos getScreenOpeningData(ServerPlayer p) {
                    return pos;
                }

                @Override
                public Component getDisplayName() {
                    return Component.literal("Waypoint Beacon");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                    return new WaypointMenu(containerId, playerInventory, pos);
                }
            });
            neptune.neptune.network.NeptuneNetworking.syncWaypointsToClient(serverPlayer, pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            clearWaypoint(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void clearWaypoint(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof WaypointBeaconBlockEntity waypointBE)) return;

        if (waypointBE.getOwnerUUID() != null) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(waypointBE.getOwnerUUID());
            if (owner != null) {
                WaypointData waypoints = owner.getAttachedOrCreate(NeptuneAttachments.WAYPOINTS);
                owner.setAttached(NeptuneAttachments.WAYPOINTS, waypoints.removeWaypoint(pos));
            }
        }
    }

    private void giveBack(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
