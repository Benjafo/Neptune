package neptune.neptune.broker;

import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side menu for the Broker.
 * Players click items in their inventory to sell them for void essence.
 */
public class BrokerMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    public BrokerMenu(int containerId, Inventory playerInventory) {
        super(NeptuneMenus.BROKER_MENU, containerId);

        // Add player inventory slots (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_START_X + col * 18,
                        PLAYER_INV_START_Y + row * 18));
            }
        }
        // Add hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_START_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        if (slotIndex >= 0 && slotIndex < this.slots.size() && player instanceof ServerPlayer serverPlayer) {
            Slot slot = this.slots.get(slotIndex);
            ItemStack stack = slot.getItem();

            if (!stack.isEmpty() && GearValueCalculator.isSellable(stack)) {
                float value = GearValueCalculator.calculateValue(stack);
                int essenceGained = GearValueCalculator.roundValue(value);

                if (essenceGained > 0) {
                    // Add essence
                    VoidEssenceData current = serverPlayer.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
                    serverPlayer.setAttached(NeptuneAttachments.VOID_ESSENCE, current.add(essenceGained));

                    // Remove the item
                    slot.set(ItemStack.EMPTY);

                    // Notify player
                    serverPlayer.sendSystemMessage(
                            Component.literal("§aSold for §d" + essenceGained + " §avoid essence")
                    );
                    return;
                }
            }
        }
        // For non-sellable items or invalid clicks, do nothing (prevent normal inventory interaction)
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        // Shift-click also sells
        if (slotIndex >= 0 && slotIndex < this.slots.size() && player instanceof ServerPlayer serverPlayer) {
            Slot slot = this.slots.get(slotIndex);
            ItemStack stack = slot.getItem();

            if (!stack.isEmpty() && GearValueCalculator.isSellable(stack)) {
                float value = GearValueCalculator.calculateValue(stack);
                int essenceGained = GearValueCalculator.roundValue(value);

                if (essenceGained > 0) {
                    VoidEssenceData current = serverPlayer.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
                    serverPlayer.setAttached(NeptuneAttachments.VOID_ESSENCE, current.add(essenceGained));
                    slot.set(ItemStack.EMPTY);
                    serverPlayer.sendSystemMessage(
                            Component.literal("§aSold for §d" + essenceGained + " §avoid essence")
                    );
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
