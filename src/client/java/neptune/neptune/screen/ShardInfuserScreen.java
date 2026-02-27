package neptune.neptune.screen;

import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.network.ShardApplyPayload;
import neptune.neptune.network.ShardInfuserRetrievePayload;
import neptune.neptune.network.ShardInfuserSetGearPayload;
import neptune.neptune.network.ShardInfuserSyncPayload;
import neptune.neptune.processing.ShardInfuserMenu;
import neptune.neptune.relic.NeptuneItems;
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

public class ShardInfuserScreen extends AbstractContainerScreen<ShardInfuserMenu> {

    private ItemStack syncedGear = ItemStack.EMPTY;
    private List<ShardInfuserSyncPayload.EnchantEntry> syncedEnchantments = List.of();

    private static final int GEAR_AREA_X = 8;
    private static final int GEAR_AREA_Y = 30;
    private static final int GEAR_AREA_SIZE = 24;

    private static final int ENCHANT_LIST_Y = 60;
    private static final int ENCHANT_ENTRY_HEIGHT = 16;
    private static final int MAX_VISIBLE_ENCHANTS = 7;
    private int enchantScrollOffset = 0;

    // Inventory grid for when no gear is set
    private static final int INV_START_X = 8;
    private static final int INV_START_Y = 60;
    private static final int SLOT_SIZE = 18;

    private Button retrieveButton;

    public ShardInfuserScreen(ShardInfuserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 250;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        retrieveButton = this.addRenderableWidget(Button.builder(Component.literal("Retrieve"), btn -> {
            ClientPlayNetworking.send(new ShardInfuserRetrievePayload(this.menu.getPos()));
        }).bounds(this.leftPos + this.imageWidth - 70, this.topPos + GEAR_AREA_Y, 60, 20).build());
    }

    public void handleSync(ShardInfuserSyncPayload payload) {
        this.syncedGear = payload.gear();
        this.syncedEnchantments = payload.enchantments();
        this.enchantScrollOffset = 0;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Background
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xCC000000);
        drawBorder(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF555555);

        // Title
        graphics.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0xFFAA00, false);

        // Shard count + essence
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            int shardCount = countClientShards();
            String shardText = "Shards: " + shardCount;
            VoidEssenceData data = mc.player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
            String essenceText = "Essence: " + data.current();

            int essenceWidth = this.font.width(essenceText);
            graphics.drawString(this.font, essenceText, this.leftPos + this.imageWidth - essenceWidth - 8, this.topPos + 6, 0xAA55FF, false);

            int shardWidth = this.font.width(shardText);
            graphics.drawString(this.font, shardText, this.leftPos + this.imageWidth - shardWidth - essenceWidth - 16, this.topPos + 6, 0x55FFAA, false);
        }

        // Separator
        graphics.fill(this.leftPos + 4, this.topPos + 18, this.leftPos + this.imageWidth - 4, this.topPos + 19, 0xFF333333);

        // Gear display
        if (!syncedGear.isEmpty()) {
            retrieveButton.visible = true;
            graphics.drawString(this.font, "Gear:", this.leftPos + GEAR_AREA_X, this.topPos + GEAR_AREA_Y + 2, 0xAAAAAA, false);

            // Render gear item
            int gearX = this.leftPos + GEAR_AREA_X + 30;
            int gearY = this.topPos + GEAR_AREA_Y;
            graphics.fill(gearX - 1, gearY - 1, gearX + GEAR_AREA_SIZE + 1, gearY + GEAR_AREA_SIZE + 1, 0xFF333333);
            graphics.renderItem(syncedGear, gearX + 4, gearY + 4);

            // Gear name
            graphics.drawString(this.font, syncedGear.getHoverName(), gearX + GEAR_AREA_SIZE + 4, gearY + 6, 0xFFFFFF, false);

            // Enchantment list
            graphics.drawString(this.font, "Available Enchantments:", this.leftPos + 8, this.topPos + ENCHANT_LIST_Y - 12, 0x888888, false);

            if (syncedEnchantments.isEmpty()) {
                graphics.drawString(this.font, "§7No enchantments available", this.leftPos + 8, this.topPos + ENCHANT_LIST_Y + 4, 0x666666, false);
            } else {
                int maxScroll = Math.max(0, syncedEnchantments.size() - MAX_VISIBLE_ENCHANTS);
                enchantScrollOffset = Math.min(enchantScrollOffset, maxScroll);
                int visibleCount = Math.min(syncedEnchantments.size() - enchantScrollOffset, MAX_VISIBLE_ENCHANTS);

                int shardCount = countClientShards();
                for (int i = 0; i < visibleCount; i++) {
                    ShardInfuserSyncPayload.EnchantEntry entry = syncedEnchantments.get(i + enchantScrollOffset);
                    int y = this.topPos + ENCHANT_LIST_Y + i * ENCHANT_ENTRY_HEIGHT;

                    boolean hovered = mouseX >= this.leftPos + 4 && mouseX <= this.leftPos + this.imageWidth - 4
                            && mouseY >= y && mouseY < y + ENCHANT_ENTRY_HEIGHT;
                    if (hovered) {
                        graphics.fill(this.leftPos + 4, y, this.leftPos + this.imageWidth - 4, y + ENCHANT_ENTRY_HEIGHT, 0x40FFFFFF);
                    }

                    boolean canAfford = shardCount >= entry.shardCost();
                    int nameColor = canAfford ? 0xFFFFFF : 0x666666;
                    int costColor = canAfford ? 0x55FF55 : 0xFF5555;

                    String name = entry.displayName() + " " + toRoman(entry.targetLevel());
                    graphics.drawString(this.font, name, this.leftPos + 8, y + 4, nameColor, false);

                    String costText = entry.shardCost() + " shards";
                    int costWidth = this.font.width(costText);
                    graphics.drawString(this.font, costText, this.leftPos + this.imageWidth - costWidth - 8, y + 4, costColor, false);
                }

                // Scroll indicators
                if (enchantScrollOffset > 0) {
                    graphics.drawString(this.font, "▲", this.leftPos + this.imageWidth / 2 - 3, this.topPos + ENCHANT_LIST_Y - 8, 0xAAAAAA, false);
                }
                if (enchantScrollOffset < maxScroll) {
                    int bottomY = this.topPos + ENCHANT_LIST_Y + visibleCount * ENCHANT_ENTRY_HEIGHT;
                    graphics.drawString(this.font, "▼", this.leftPos + this.imageWidth / 2 - 3, bottomY + 2, 0xAAAAAA, false);
                }
            }
        } else {
            retrieveButton.visible = false;
            graphics.drawString(this.font, "Select an item from your inventory:", this.leftPos + 8, this.topPos + 24, 0x888888, false);
            renderInventoryGrid(graphics, mouseX, mouseY);
        }
    }

    private void renderInventoryGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int slotIndex = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                int invIndex = (row < 3) ? (col + row * 9 + 9) : col;
                ItemStack stack = mc.player.getInventory().getItem(invIndex);

                int x = this.leftPos + INV_START_X + col * SLOT_SIZE;
                int y = this.topPos + INV_START_Y + row * SLOT_SIZE;
                if (row == 3) y += 4; // Gap before hotbar

                // Slot background
                graphics.fill(x, y, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, 0xFF222222);

                if (!stack.isEmpty()) {
                    graphics.renderItem(stack, x + 1, y + 1);
                    graphics.renderItemDecorations(this.font, stack, x + 1, y + 1);
                }

                slotIndex++;
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        if (event.button() != 0) return super.mouseClicked(event, forwarded);

        double mx = event.x();
        double my = event.y();

        if (!syncedGear.isEmpty()) {
            // Click on enchantment list
            int maxScroll = Math.max(0, syncedEnchantments.size() - MAX_VISIBLE_ENCHANTS);
            enchantScrollOffset = Math.min(enchantScrollOffset, maxScroll);
            int visibleCount = Math.min(syncedEnchantments.size() - enchantScrollOffset, MAX_VISIBLE_ENCHANTS);

            for (int i = 0; i < visibleCount; i++) {
                int y = this.topPos + ENCHANT_LIST_Y + i * ENCHANT_ENTRY_HEIGHT;
                if (mx >= this.leftPos + 4 && mx <= this.leftPos + this.imageWidth - 4
                        && my >= y && my < y + ENCHANT_ENTRY_HEIGHT) {
                    int enchantIndex = i + enchantScrollOffset;
                    ClientPlayNetworking.send(new ShardApplyPayload(this.menu.getPos(), enchantIndex));
                    return true;
                }
            }
        } else {
            // Click on inventory grid to insert gear
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return super.mouseClicked(event, forwarded);

            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 9; col++) {
                    int invIndex = (row < 3) ? (col + row * 9 + 9) : col;
                    int x = this.leftPos + INV_START_X + col * SLOT_SIZE;
                    int y = this.topPos + INV_START_Y + row * SLOT_SIZE;
                    if (row == 3) y += 4;

                    if (mx >= x && mx < x + SLOT_SIZE - 2 && my >= y && my < y + SLOT_SIZE - 2) {
                        ItemStack stack = mc.player.getInventory().getItem(invIndex);
                        if (!stack.isEmpty()) {
                            ClientPlayNetworking.send(new ShardInfuserSetGearPayload(this.menu.getPos(), invIndex));
                            return true;
                        }
                    }
                }
            }
        }

        return super.mouseClicked(event, forwarded);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!syncedGear.isEmpty() && !syncedEnchantments.isEmpty()) {
            int maxScroll = Math.max(0, syncedEnchantments.size() - MAX_VISIBLE_ENCHANTS);
            if (scrollY > 0) {
                enchantScrollOffset = Math.max(0, enchantScrollOffset - 1);
            } else if (scrollY < 0) {
                enchantScrollOffset = Math.min(maxScroll, enchantScrollOffset + 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Tooltips for enchantments
        if (!syncedGear.isEmpty() && !syncedEnchantments.isEmpty()) {
            int visibleCount = Math.min(syncedEnchantments.size() - enchantScrollOffset, MAX_VISIBLE_ENCHANTS);
            for (int i = 0; i < visibleCount; i++) {
                int y = this.topPos + ENCHANT_LIST_Y + i * ENCHANT_ENTRY_HEIGHT;
                if (mouseX >= this.leftPos + 4 && mouseX <= this.leftPos + this.imageWidth - 4
                        && mouseY >= y && mouseY < y + ENCHANT_ENTRY_HEIGHT) {
                    ShardInfuserSyncPayload.EnchantEntry entry = syncedEnchantments.get(i + enchantScrollOffset);
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.literal("§e" + entry.displayName() + " " + toRoman(entry.targetLevel())));
                    tooltip.add(Component.literal("§6Cost: " + entry.shardCost() + " shards"));
                    tooltip.add(Component.literal("§7Click to apply"));
                    graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
                    break;
                }
            }
        }

        // Tooltip for inventory items in no-gear mode
        if (syncedGear.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 9; col++) {
                        int invIndex = (row < 3) ? (col + row * 9 + 9) : col;
                        int x = this.leftPos + INV_START_X + col * SLOT_SIZE;
                        int y = this.topPos + INV_START_Y + row * SLOT_SIZE;
                        if (row == 3) y += 4;

                        if (mouseX >= x && mouseX < x + SLOT_SIZE - 2 && mouseY >= y && mouseY < y + SLOT_SIZE - 2) {
                            ItemStack stack = mc.player.getInventory().getItem(invIndex);
                            if (!stack.isEmpty()) {
                                List<Component> tooltip = new ArrayList<>();
                                tooltip.addAll(this.getTooltipFromItem(mc, stack));
                                tooltip.add(Component.literal(""));
                                tooltip.add(Component.literal("§7Click to insert into infuser"));
                                graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Don't render default labels
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

    private static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }
}
