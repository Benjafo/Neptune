package neptune.neptune.screen;

import neptune.neptune.broker.BrokerMenu;
import neptune.neptune.broker.BrokerStock;
import neptune.neptune.broker.GearValueCalculator;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.network.BrokerPurchasePayload;
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

public class BrokerScreen extends AbstractContainerScreen<BrokerMenu> {

    private enum Tab { SELL, BUY }
    private Tab currentTab = Tab.SELL;

    // Tab button positions
    private static final int TAB_Y_OFFSET = -20;
    private static final int TAB_WIDTH = 60;
    private static final int TAB_HEIGHT = 20;

    // Buy page
    private static final int STOCK_START_Y = 35;
    private static final int STOCK_ENTRY_HEIGHT = 20;

    public BrokerScreen(BrokerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 250;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        // Tab buttons
        this.addRenderableWidget(Button.builder(Component.literal("Sell"), btn -> currentTab = Tab.SELL)
                .bounds(this.leftPos + 4, this.topPos + TAB_Y_OFFSET, TAB_WIDTH, TAB_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Buy"), btn -> currentTab = Tab.BUY)
                .bounds(this.leftPos + 4 + TAB_WIDTH + 4, this.topPos + TAB_Y_OFFSET, TAB_WIDTH, TAB_HEIGHT)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Background
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xCC000000);

        // Border
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
            renderSellTab(graphics);
        } else {
            renderBuyTab(graphics, mouseX, mouseY);
        }
    }

    private void renderSellTab(GuiGraphics graphics) {
        graphics.drawString(this.font, "Click items in your inventory to sell them", this.leftPos + 8, this.topPos + 24, 0x888888, false);

        // Inventory label
        graphics.drawString(this.font, this.playerInventoryTitle, this.leftPos + 8, this.topPos + this.imageHeight - 94, 0xAAAAAA, false);
    }

    private void renderBuyTab(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, "Available Items:", this.leftPos + 8, this.topPos + 24, 0x888888, false);

        List<BrokerStock.StockEntry> stock = BrokerStock.getCoreStock();
        Minecraft mc = Minecraft.getInstance();
        int currentEssence = 0;
        if (mc.player != null) {
            currentEssence = mc.player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE).current();
        }

        for (int i = 0; i < stock.size(); i++) {
            BrokerStock.StockEntry entry = stock.get(i);
            int y = this.topPos + STOCK_START_Y + i * STOCK_ENTRY_HEIGHT;

            // Highlight on hover
            boolean hovered = mouseX >= this.leftPos + 4 && mouseX <= this.leftPos + this.imageWidth - 4
                    && mouseY >= y && mouseY < y + STOCK_ENTRY_HEIGHT;
            if (hovered) {
                graphics.fill(this.leftPos + 4, y, this.leftPos + this.imageWidth - 4, y + STOCK_ENTRY_HEIGHT, 0x40FFFFFF);
            }

            // Affordable check
            boolean canAfford = currentEssence >= entry.cost();
            int nameColor = canAfford ? 0xFFFFFF : 0x666666;
            int costColor = canAfford ? 0x55FF55 : 0xFF5555;

            // Name
            graphics.drawString(this.font, entry.name(), this.leftPos + 8, y + 6, nameColor, false);

            // Cost
            String costText = entry.cost() + " essence";
            int costWidth = this.font.width(costText);
            graphics.drawString(this.font, costText, this.leftPos + this.imageWidth - costWidth - 8, y + 6, costColor, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        if (currentTab == Tab.BUY && event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            List<BrokerStock.StockEntry> stock = BrokerStock.getCoreStock();
            for (int i = 0; i < stock.size(); i++) {
                int y = this.topPos + STOCK_START_Y + i * STOCK_ENTRY_HEIGHT;
                if (mouseX >= this.leftPos + 4 && mouseX <= this.leftPos + this.imageWidth - 4
                        && mouseY >= y && mouseY < y + STOCK_ENTRY_HEIGHT) {
                    // Send purchase packet
                    ClientPlayNetworking.send(new BrokerPurchasePayload(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(event, forwarded);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (currentTab == Tab.SELL && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(stack));

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

            graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }

        // Buy tab tooltips
        if (currentTab == Tab.BUY) {
            List<BrokerStock.StockEntry> stock = BrokerStock.getCoreStock();
            for (int i = 0; i < stock.size(); i++) {
                int y = this.topPos + STOCK_START_Y + i * STOCK_ENTRY_HEIGHT;
                if (mouseX >= this.leftPos + 4 && mouseX <= this.leftPos + this.imageWidth - 4
                        && mouseY >= y && mouseY < y + STOCK_ENTRY_HEIGHT) {
                    BrokerStock.StockEntry entry = stock.get(i);
                    List<Component> tooltip = List.of(
                            Component.literal("§e" + entry.name()),
                            Component.literal("§7" + entry.description()),
                            Component.literal("§6Cost: " + entry.cost() + " essence")
                    );
                    graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
                    break;
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Don't render default labels — we handle them in renderBg
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }
}
