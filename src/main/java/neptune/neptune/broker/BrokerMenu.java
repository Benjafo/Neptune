package neptune.neptune.broker;

import neptune.neptune.challenge.ChallengeTracker;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.relic.RelicSetBonus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BrokerMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    public BrokerMenu(int containerId, Inventory playerInventory) {
        super(NeptuneMenus.BROKER_MENU, containerId);

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

    private void sellItem(ServerPlayer serverPlayer, Slot slot) {
        ItemStack stack = slot.getItem();
        if (stack.isEmpty() || !GearValueCalculator.isSellable(stack)) return;

        float value = GearValueCalculator.calculateValue(stack);
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
        if (slotIndex >= 0 && slotIndex < this.slots.size() && player instanceof ServerPlayer serverPlayer) {
            sellItem(serverPlayer, this.slots.get(slotIndex));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex >= 0 && slotIndex < this.slots.size() && player instanceof ServerPlayer serverPlayer) {
            sellItem(serverPlayer, this.slots.get(slotIndex));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
