package neptune.neptune.processing;

import com.mojang.serialization.MapCodec;
import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.data.BlockPlacementsData;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

public class ShardInfuserBlock extends BaseEntityBlock {

    public static final MapCodec<ShardInfuserBlock> CODEC = simpleCodec(ShardInfuserBlock::new);

    public ShardInfuserBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShardInfuserBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (level.isClientSide || !(placer instanceof ServerPlayer player)) return;

        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        if (!unlocks.hasTier(UnlockBranch.PROCESSING, 2)) {
            player.sendSystemMessage(Component.literal("§cRequires Processing T2 (Enchantment Studies) to place!"));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.SHARD_INFUSER_ITEM));
            return;
        }

        BlockPlacementsData placements = player.getAttachedOrCreate(NeptuneAttachments.BLOCK_PLACEMENTS);
        if (placements.hasShardInfuser()) {
            player.sendSystemMessage(Component.literal("§cYou already have a Shard Infuser placed! Break the old one first."));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.SHARD_INFUSER_ITEM));
            return;
        }

        if (level.getBlockEntity(pos) instanceof ShardInfuserBlockEntity be) {
            be.setOwnerUUID(player.getUUID());
        }

        player.setAttached(NeptuneAttachments.BLOCK_PLACEMENTS, placements.withShardInfuser(pos));
        player.sendSystemMessage(Component.literal("§aShard Infuser placed!"));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
                @Override
                public BlockPos getScreenOpeningData(ServerPlayer p) {
                    return pos;
                }

                @Override
                public Component getDisplayName() {
                    return Component.literal("Shard Infuser");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                    return new ShardInfuserMenu(containerId, playerInventory, pos);
                }
            });
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            returnGearAndClearOwner(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                returnGearAndClearOwner(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void returnGearAndClearOwner(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ShardInfuserBlockEntity infuserBE)) return;

        // Return gear to owner
        if (!infuserBE.getGearSlot().isEmpty() && infuserBE.getOwnerUUID() != null) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(infuserBE.getOwnerUUID());
            if (owner != null) {
                giveBack(owner, infuserBE.getGearSlot().copy());
            } else {
                // Owner offline — drop item at block pos
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                        level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        infuserBE.getGearSlot().copy());
                level.addFreshEntity(itemEntity);
            }
            infuserBE.setGearSlot(ItemStack.EMPTY);
        }

        // Clear owner attachment
        if (infuserBE.getOwnerUUID() != null) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(infuserBE.getOwnerUUID());
            if (owner != null) {
                BlockPlacementsData placements = owner.getAttachedOrCreate(NeptuneAttachments.BLOCK_PLACEMENTS);
                owner.setAttached(NeptuneAttachments.BLOCK_PLACEMENTS, placements.withoutShardInfuser());
            }
        }
    }

    private void giveBack(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
