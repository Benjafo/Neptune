package neptune.neptune.processing;

import neptune.neptune.broker.GearValueCalculator;
import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.challenge.ChallengeTracker;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.relic.NeptuneItems;
import neptune.neptune.relic.RelicSetBonus;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BreakdownTableMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final BlockPos pos;

    public BreakdownTableMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(NeptuneMenus.BREAKDOWN_TABLE_MENU, containerId);
        this.pos = pos;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_START_X + col * 18,
                        PLAYER_INV_START_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_START_X + col * 18, HOTBAR_Y));
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    /**
     * Handle a sell action from a packet.
     */
    public void handleSell(ServerPlayer player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
        sellItem(player, this.slots.get(slotIndex));
    }

    /**
     * Handle an extract action from a packet.
     */
    public void handleExtract(ServerPlayer player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;

        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        if (!unlocks.hasTier(UnlockBranch.PROCESSING, 2)) {
            player.sendSystemMessage(Component.literal("§cRequires Processing T2 to extract shards!"));
            return;
        }

        Slot slot = this.slots.get(slotIndex);
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        int yield = EnchantmentShardHelper.getExtractionYield(stack);
        if (yield <= 0) {
            player.sendSystemMessage(Component.literal("§cNo enchantments to extract!"));
            return;
        }

        // Destroy item and give shards
        slot.set(ItemStack.EMPTY);
        ItemStack shards = new ItemStack(NeptuneItems.ENCHANTMENT_SHARD, yield);
        if (!player.getInventory().add(shards)) {
            player.drop(shards, false);
        }

        player.sendSystemMessage(Component.literal("§aExtracted §d" + yield + " §aenchantment shard" + (yield > 1 ? "s" : "") + "!"));
    }

    private void sellItem(ServerPlayer serverPlayer, Slot slot) {
        ItemStack stack = slot.getItem();
        if (stack.isEmpty() || !GearValueCalculator.isSellable(stack)) return;

        float value = GearValueCalculator.calculateValue(stack, serverPlayer);
        int essenceGained = GearValueCalculator.roundValue(value);
        if (essenceGained <= 0) return;

        // Apply Builders set bonus (+15% essence from selling)
        if (RelicSetBonus.hasBuildersBonus(serverPlayer)) {
            int boosted = RelicSetBonus.applyBuildersBonus(essenceGained);
            int bonus = boosted - essenceGained;
            essenceGained = boosted;
            serverPlayer.sendSystemMessage(
                    Component.literal("§aSold for §d" + essenceGained + " §avoid essence §7(+" + bonus + " Builders bonus)"));
        } else {
            serverPlayer.sendSystemMessage(
                    Component.literal("§aSold for §d" + essenceGained + " §avoid essence"));
        }

        VoidEssenceData current = serverPlayer.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
        serverPlayer.setAttached(NeptuneAttachments.VOID_ESSENCE, current.add(essenceGained));
        slot.set(ItemStack.EMPTY);

        // Track challenges
        ChallengeTracker.onItemSold(serverPlayer, essenceGained);
        ChallengeTracker.onLifetimeEssenceUpdated(serverPlayer);
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        // No-op: all actions go through explicit packets to avoid double-fire
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).is(NeptuneBlocks.BREAKDOWN_TABLE);
    }
}
