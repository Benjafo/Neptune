package neptune.neptune.screen;

import neptune.neptune.broker.GearValueCalculator;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.network.BreakdownActionPayload;
import neptune.neptune.processing.BreakdownTableMenu;
import neptune.neptune.processing.EnchantmentShardHelper;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BreakdownTableScreen extends AbstractContainerScreen<BreakdownTableMenu> {

    private enum Tab { SELL, EXTRACT }
    private Tab currentTab = Tab.SELL;

    private static final int TAB_Y_OFFSET = -20;
    private static final int TAB_WIDTH = 60;
    private static final int TAB_HEIGHT = 20;

    public BreakdownTableScreen(BreakdownTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 250;
        this.imageHeight = 200;
    }

    private boolean hasT2() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        UnlockData unlocks = mc.player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        return unlocks.hasTier(UnlockBranch.PROCESSING, 2);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("Sell"), btn -> currentTab = Tab.SELL)
                .bounds(this.leftPos + 4, this.topPos + TAB_Y_OFFSET, TAB_WIDTH, TAB_HEIGHT)
                .build());

        if (hasT2()) {
            this.addRenderableWidget(Button.builder(Component.literal("Extract"), btn -> currentTab = Tab.EXTRACT)
                    .bounds(this.leftPos + 4 + TAB_WIDTH + 4, this.topPos + TAB_Y_OFFSET, TAB_WIDTH, TAB_HEIGHT)
                    .build());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Background
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xCC000000);
        drawBorder(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF555555);

        // Title and balance
        graphics.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0xFFAA00, false);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            VoidEssenceData data = mc.player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
            String balanceText = "Essence: " + data.current();
            int balanceWidth = this.font.width(balanceText);
            graphics.drawString(this.font, balanceText, this.leftPos + this.imageWidth - balanceWidth - 8, this.topPos + 6, 0xAA55FF, false);
        }

        // Separator
        graphics.fill(this.leftPos + 4, this.topPos + 18, this.leftPos + this.imageWidth - 4, this.topPos + 19, 0xFF333333);

        if (currentTab == Tab.SELL) {
            graphics.drawString(this.font, "Click items to sell them", this.leftPos + 8, this.topPos + 24, 0x888888, false);
        } else {
            graphics.drawString(this.font, "Click enchanted items to extract shards", this.leftPos + 8, this.topPos + 24, 0x888888, false);
        }

        // Inventory label
        graphics.drawString(this.font, this.playerInventoryTitle, this.leftPos + 8, this.topPos + this.imageHeight - 94, 0xAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        if (event.button() == 0 && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            int slotIndex = this.hoveredSlot.index;
            // Find the container slot index
            for (int i = 0; i < this.menu.slots.size(); i++) {
                if (this.menu.slots.get(i) == this.hoveredSlot) {
                    slotIndex = i;
                    break;
                }
            }
            String action = currentTab == Tab.SELL ? "sell" : "extract";
            ClientPlayNetworking.send(new BreakdownActionPayload(action, slotIndex));
            return true;
        }
        return super.mouseClicked(event, forwarded);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(stack));

            if (currentTab == Tab.SELL) {
                if (GearValueCalculator.isSellable(stack)) {
                    float value = GearValueCalculator.calculateValue(stack);
                    int roundedValue = GearValueCalculator.roundValue(value);
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal("§aSell value: §d" + roundedValue + " essence"));
                    tooltip.add(Component.literal("§7Click to sell"));
                } else {
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal("§cCannot be sold"));
                }
            } else {
                // Extract tab
                if (EnchantmentShardHelper.canExtract(stack)) {
                    int yield = EnchantmentShardHelper.getExtractionYield(stack);
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal("§aShard yield: §d" + yield + " shard" + (yield > 1 ? "s" : "")));
                    tooltip.add(Component.literal("§cItem will be destroyed!"));
                    tooltip.add(Component.literal("§7Click to extract"));
                } else {
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal("§cNo extractable enchantments"));
                }
            }

            graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Don't render default labels
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }
}
