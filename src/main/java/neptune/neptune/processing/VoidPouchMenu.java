package neptune.neptune.processing;

import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.relic.NeptuneItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class VoidPouchMenu extends AbstractContainerMenu {

    private final SimpleContainer pouchContainer;
    private final ItemStack pouchStack;
    private final int pouchSlot;

    public VoidPouchMenu(int containerId, Inventory playerInventory, ItemStack pouchStack, int pouchSlot) {
        super(NeptuneMenus.VOID_POUCH_MENU, containerId);
        this.pouchStack = pouchStack;
        this.pouchSlot = pouchSlot;

        this.pouchContainer = new SimpleContainer(9);

        // Load contents from item's CONTAINER component
        ItemContainerContents contents = pouchStack.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.copyInto(pouchContainer.getItems());
        }

        // Pouch slots (1 row of 9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(pouchContainer, col, 8 + col * 18, 20));
        }

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    // Client-side constructor (no pouch data needed)
    public VoidPouchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ItemStack.EMPTY, -1);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide() && !pouchStack.isEmpty()) {
            // Write container contents back to the pouch item
            ItemStack currentPouch = pouchSlot >= 0 ? player.getInventory().getItem(pouchSlot) : ItemStack.EMPTY;
            if (currentPouch.is(NeptuneItems.VOID_POUCH)) {
                currentPouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(pouchContainer.getItems()));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex < 9) {
            // Move from pouch to player inventory
            if (!this.moveItemStackTo(stack, 9, 45, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Move from player inventory to pouch
            if (!this.moveItemStackTo(stack, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        // Valid as long as the player still has the pouch in the expected slot
        if (pouchSlot < 0) return true; // client side
        ItemStack held = player.getInventory().getItem(pouchSlot);
        return held.is(NeptuneItems.VOID_POUCH);
    }
}
