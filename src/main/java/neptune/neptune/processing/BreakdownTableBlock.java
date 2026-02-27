package neptune.neptune.processing;

import com.mojang.serialization.MapCodec;
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
import org.jspecify.annotations.Nullable;

public class BreakdownTableBlock extends BaseEntityBlock {

    public static final MapCodec<BreakdownTableBlock> CODEC = simpleCodec(BreakdownTableBlock::new);

    public BreakdownTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BreakdownTableBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide() || !(placer instanceof ServerPlayer player)) return;

        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        if (!unlocks.hasTier(UnlockBranch.PROCESSING, 1)) {
            player.sendSystemMessage(Component.literal("§cRequires Processing T1 (Salvager) to place!"));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.BREAKDOWN_TABLE_ITEM));
            return;
        }

        BlockPlacementsData placements = player.getAttachedOrCreate(NeptuneAttachments.BLOCK_PLACEMENTS);
        if (placements.hasBreakdownTable()) {
            player.sendSystemMessage(Component.literal("§cYou already have a Breakdown Table placed! Break the old one first."));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.BREAKDOWN_TABLE_ITEM));
            return;
        }

        if (level.getBlockEntity(pos) instanceof BreakdownTableBlockEntity be) {
            be.setOwnerUUID(player.getUUID());
        }

        player.setAttached(NeptuneAttachments.BLOCK_PLACEMENTS, placements.withBreakdownTable(pos));
        player.sendSystemMessage(Component.literal("§aBreakdown Table placed!"));
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
                    return Component.literal("Breakdown Table");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                    return new BreakdownTableMenu(containerId, playerInventory, pos);
                }
            });
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            clearOwnerAttachment(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void clearOwnerAttachment(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BreakdownTableBlockEntity tableBE)) return;
        if (tableBE.getOwnerUUID() == null) return;

        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(tableBE.getOwnerUUID());
        if (owner != null) {
            BlockPlacementsData placements = owner.getAttachedOrCreate(NeptuneAttachments.BLOCK_PLACEMENTS);
            owner.setAttached(NeptuneAttachments.BLOCK_PLACEMENTS, placements.withoutBreakdownTable());
        }
    }

    private void giveBack(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
