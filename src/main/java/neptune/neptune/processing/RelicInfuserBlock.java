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

public class RelicInfuserBlock extends BaseEntityBlock {

    public static final MapCodec<RelicInfuserBlock> CODEC = simpleCodec(RelicInfuserBlock::new);

    public RelicInfuserBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelicInfuserBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide() || !(placer instanceof ServerPlayer player)) return;

        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        if (!unlocks.hasTier(UnlockBranch.PROCESSING, 4)) {
            player.sendSystemMessage(Component.literal("§cRequires Processing T4 (Master Artificer) to place!"));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.RELIC_INFUSER_ITEM));
            return;
        }

        BlockPlacementsData placements = player.getAttachedOrCreate(NeptuneAttachments.BLOCK_PLACEMENTS);
        if (placements.hasRelicInfuser()) {
            player.sendSystemMessage(Component.literal("§cYou already have a Relic Infuser placed! Break the old one first."));
            level.destroyBlock(pos, false);
            giveBack(player, new ItemStack(NeptuneBlocks.RELIC_INFUSER_ITEM));
            return;
        }

        if (level.getBlockEntity(pos) instanceof RelicInfuserBlockEntity be) {
            be.setOwnerUUID(player.getUUID());
        }

        player.setAttached(NeptuneAttachments.BLOCK_PLACEMENTS, placements.withRelicInfuser(pos));
        player.sendSystemMessage(Component.literal("§aRelic Infuser placed!"));
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
                    return Component.literal("Relic Infuser");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                    return new RelicInfuserMenu(containerId, playerInventory, pos);
                }
            });
            neptune.neptune.network.NeptuneNetworking.syncRelicInfuserToClient(serverPlayer);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            clearOwner(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void clearOwner(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RelicInfuserBlockEntity infuserBE)) return;

        if (infuserBE.getOwnerUUID() != null) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(infuserBE.getOwnerUUID());
            if (owner != null) {
                BlockPlacementsData placements = owner.getAttachedOrCreate(NeptuneAttachments.BLOCK_PLACEMENTS);
                owner.setAttached(NeptuneAttachments.BLOCK_PLACEMENTS, placements.withoutRelicInfuser());
            }
        }
    }

    private void giveBack(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
