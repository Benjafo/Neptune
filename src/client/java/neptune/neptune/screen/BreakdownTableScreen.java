package neptune.neptune.screen;

import neptune.neptune.broker.GearValueCalculator;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.network.BreakdownActionPayload;
import neptune.neptune.processing.BreakdownTableMenu;
import neptune.neptune.processing.EnchantmentShardHelper;
import neptune.neptune.processing.ShardInfuserMenu;
import neptune.neptune.processing.VoidSynthesisRecipe;
import neptune.neptune.relic.NeptuneItems;
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

    private enum Tab { SELL, EXTRACT, CRAFT }
    private Tab currentTab = Tab.SELL;

    private static final int TAB_Y_OFFSET = -20;
    private static final int TAB_WIDTH = 60;
    private static final int TAB_HEIGHT = 20;

    private static final int RECIPE_START_Y = 36;
    private static final int RECIPE_ENTRY_HEIGHT = 28;

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

    private boolean hasT3() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        UnlockData unlocks = mc.player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        return unlocks.hasTier(UnlockBranch.PROCESSING, 3);
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

        if (hasT3()) {
            this.addRenderableWidget(Button.builder(Component.literal("Craft"), btn -> currentTab = Tab.CRAFT)
                    .bounds(this.leftPos + 4 + (TAB_WIDTH + 4) * 2, this.topPos + TAB_Y_OFFSET, TAB_WIDTH, TAB_HEIGHT)
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
        } else if (currentTab == Tab.EXTRACT) {
            graphics.drawString(this.font, "Click enchanted items to extract shards", this.leftPos + 8, this.topPos + 24, 0x888888, false);
        } else if (currentTab == Tab.CRAFT) {
            renderCraftTab(graphics, mouseX, mouseY);
        }

        if (currentTab != Tab.CRAFT) {
            // Inventory label
            graphics.drawString(this.font, this.playerInventoryTitle, this.leftPos + 8, this.topPos + this.imageHeight - 94, 0xAAAAAA, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        if (event.button() == 0) {
            if (currentTab == Tab.CRAFT) {
                return handleCraftClick(event.x(), event.y()) || super.mouseClicked(event, forwarded);
            }

            if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
                int slotIndex = this.hoveredSlot.index;
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
        }
        return super.mouseClicked(event, forwarded);
    }

    private boolean handleCraftClick(double mx, double my) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        UnlockData unlocks = mc.player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);

        for (int i = 0; i < VoidSynthesisRecipe.ALL.size(); i++) {
            VoidSynthesisRecipe recipe = VoidSynthesisRecipe.ALL.get(i);
            int y = this.topPos + RECIPE_START_Y + i * RECIPE_ENTRY_HEIGHT;

            if (mx >= this.leftPos + 4 && mx <= this.leftPos + this.imageWidth - 4
                    && my >= y && my < y + RECIPE_ENTRY_HEIGHT) {
                if (unlocks.hasTier(UnlockBranch.PROCESSING, recipe.requiredTier())) {
                    ClientPlayNetworking.send(new BreakdownActionPayload("craft", i));
                }
                return true;
            }
        }
        return false;
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

    private void renderCraftTab(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        UnlockData unlocks = mc.player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        int shardCount = countClientShards();

        graphics.drawString(this.font, "Void Synthesis", this.leftPos + 8, this.topPos + 24, 0x888888, false);

        for (int i = 0; i < VoidSynthesisRecipe.ALL.size(); i++) {
            VoidSynthesisRecipe recipe = VoidSynthesisRecipe.ALL.get(i);
            int y = this.topPos + RECIPE_START_Y + i * RECIPE_ENTRY_HEIGHT;

            boolean hasTier = unlocks.hasTier(UnlockBranch.PROCESSING, recipe.requiredTier());
            boolean hasShards = shardCount >= recipe.shardCost();
            boolean hasIngredients = true;
            for (VoidSynthesisRecipe.Ingredient ing : recipe.ingredients()) {
                if (countClientItem(ing.item()) < ing.count()) {
                    hasIngredients = false;
                    break;
                }
            }

            boolean canCraft = hasTier && hasShards && hasIngredients;
            boolean hovered = mouseX >= this.leftPos + 4 && mouseX <= this.leftPos + this.imageWidth - 4
                    && mouseY >= y && mouseY < y + RECIPE_ENTRY_HEIGHT;

            if (hovered && canCraft) {
                graphics.fill(this.leftPos + 4, y, this.leftPos + this.imageWidth - 4, y + RECIPE_ENTRY_HEIGHT, 0x40FFFFFF);
            }

            int nameColor = canCraft ? 0xFFFFFF : 0x666666;
            int costColor = hasShards ? 0x55FF55 : 0xFF5555;

            graphics.drawString(this.font, recipe.displayName(), this.leftPos + 8, y + 2, nameColor, false);

            String costText = recipe.shardCost() + " shards";
            int costWidth = this.font.width(costText);
            graphics.drawString(this.font, costText, this.leftPos + this.imageWidth - costWidth - 8, y + 2, costColor, false);

            String ingText = "§7" + recipe.ingredientSummary();
            graphics.drawString(this.font, ingText, this.leftPos + 8, y + 14, 0x888888, false);

            if (!hasTier) {
                String lockText = "§c[T" + recipe.requiredTier() + "]";
                graphics.drawString(this.font, lockText, this.leftPos + this.imageWidth - this.font.width(lockText) - 8, y + 14, 0xFF5555, false);
            }
        }
    }

    private int countClientShards() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(NeptuneItems.ENCHANTMENT_SHARD)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int countClientItem(net.minecraft.world.item.Item item) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }
}
