package neptune.neptune.screen;

import neptune.neptune.network.RelicInfusePayload;
import neptune.neptune.network.RelicInfuserSyncPayload;
import neptune.neptune.processing.RelicInfuserMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class RelicInfuserScreen extends AbstractContainerScreen<RelicInfuserMenu> {

    private List<RelicInfuserSyncPayload.InfusableEntry> infusableRelics = List.of();
    private String selectedRelicId = null;

    private static final int ENTRY_HEIGHT = 20;
    private static final int LIST_START_Y = 30;
    private static final int MAX_VISIBLE = 7;
    private int scrollOffset = 0;

    // Buff choice buttons
    private static final int BUFF_BUTTON_Y = 60;
    private static final int BUFF_BUTTON_HEIGHT = 30;
    private static final int BUFF_BUTTON_WIDTH = 220;

    public RelicInfuserScreen(RelicInfuserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 250;
        this.imageHeight = 200;
    }

    public void handleSync(RelicInfuserSyncPayload payload) {
        this.infusableRelics = payload.entries();
        this.selectedRelicId = null;
        this.scrollOffset = 0;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xCC000000);
        drawBorder(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF555555);

        graphics.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0xFFAA00, false);
        graphics.fill(this.leftPos + 4, this.topPos + 18, this.leftPos + this.imageWidth - 4, this.topPos + 19, 0xFF333333);

        if (selectedRelicId == null) {
            renderRelicList(graphics, mouseX, mouseY);
        } else {
            renderBuffChoice(graphics, mouseX, mouseY);
        }
    }

    private void renderRelicList(GuiGraphics graphics, int mouseX, int mouseY) {
        if (infusableRelics.isEmpty()) {
            graphics.drawString(this.font, "No relics with 20+ duplicates.", this.leftPos + 8, this.topPos + LIST_START_Y, 0x888888, false);
            graphics.drawString(this.font, "§7Collect more relics to infuse!", this.leftPos + 8, this.topPos + LIST_START_Y + 14, 0x666666, false);
            return;
        }

        graphics.drawString(this.font, "Select a relic to infuse (20 duplicates):", this.leftPos + 8, this.topPos + 22, 0x888888, false);

        int maxScroll = Math.max(0, infusableRelics.size() - MAX_VISIBLE);
        scrollOffset = Math.min(scrollOffset, maxScroll);
        int visibleCount = Math.min(infusableRelics.size() - scrollOffset, MAX_VISIBLE);

        for (int i = 0; i < visibleCount; i++) {
            RelicInfuserSyncPayload.InfusableEntry entry = infusableRelics.get(i + scrollOffset);
            int y = this.topPos + LIST_START_Y + i * ENTRY_HEIGHT;

            boolean hovered = mouseX >= this.leftPos + 4 && mouseX <= this.leftPos + this.imageWidth - 4
                    && mouseY >= y && mouseY < y + ENTRY_HEIGHT;
            if (hovered) {
                graphics.fill(this.leftPos + 4, y, this.leftPos + this.imageWidth - 4, y + ENTRY_HEIGHT, 0x40FFFFFF);
            }

            graphics.drawString(this.font, entry.displayName(), this.leftPos + 8, y + 4, 0xFFFFFF, false);

            String countText = "x" + entry.duplicateCount();
            int countWidth = this.font.width(countText);
            graphics.drawString(this.font, countText, this.leftPos + this.imageWidth - countWidth - 8, y + 4, 0x55FF55, false);
        }
    }

    private void renderBuffChoice(GuiGraphics graphics, int mouseX, int mouseY) {
        // Find selected relic name
        String relicName = selectedRelicId;
        for (RelicInfuserSyncPayload.InfusableEntry entry : infusableRelics) {
            if (entry.relicId().equals(selectedRelicId)) {
                relicName = entry.displayName();
                break;
            }
        }

        graphics.drawString(this.font, "Infusing: §e" + relicName, this.leftPos + 8, this.topPos + 22, 0xFFFFFF, false);
        graphics.drawString(this.font, "Choose a permanent buff:", this.leftPos + 8, this.topPos + 40, 0x888888, false);

        String[] buffs = {"Health", "Damage", "Speed"};
        String[] descriptions = {"+2 Max Health (1 heart)", "+5% Attack Damage", "+5% Movement Speed"};
        String[] keys = {"health", "damage", "speed"};
        int[] colors = {0xFF5555, 0xFF5555, 0x55FFFF};

        for (int i = 0; i < 3; i++) {
            int y = this.topPos + BUFF_BUTTON_Y + i * (BUFF_BUTTON_HEIGHT + 4);
            int x = this.leftPos + (this.imageWidth - BUFF_BUTTON_WIDTH) / 2;

            boolean hovered = mouseX >= x && mouseX <= x + BUFF_BUTTON_WIDTH
                    && mouseY >= y && mouseY < y + BUFF_BUTTON_HEIGHT;

            graphics.fill(x, y, x + BUFF_BUTTON_WIDTH, y + BUFF_BUTTON_HEIGHT,
                    hovered ? 0x60FFFFFF : 0x30FFFFFF);
            drawBorder(graphics, x, y, BUFF_BUTTON_WIDTH, BUFF_BUTTON_HEIGHT, 0xFF555555);

            graphics.drawString(this.font, "§l" + buffs[i], x + 8, y + 4, colors[i], false);
            graphics.drawString(this.font, "§7" + descriptions[i], x + 8, y + 16, 0x888888, false);
        }

        // Back button
        int backY = this.topPos + this.imageHeight - 22;
        boolean backHovered = mouseX >= this.leftPos + 8 && mouseX <= this.leftPos + 58
                && mouseY >= backY && mouseY < backY + 16;
        graphics.drawString(this.font, backHovered ? "§n< Back" : "§7< Back", this.leftPos + 8, backY, 0xAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        if (event.button() != 0) return super.mouseClicked(event, forwarded);

        double mx = event.x();
        double my = event.y();

        if (selectedRelicId == null) {
            // Click on relic list
            int maxScroll = Math.max(0, infusableRelics.size() - MAX_VISIBLE);
            scrollOffset = Math.min(scrollOffset, maxScroll);
            int visibleCount = Math.min(infusableRelics.size() - scrollOffset, MAX_VISIBLE);

            for (int i = 0; i < visibleCount; i++) {
                int y = this.topPos + LIST_START_Y + i * ENTRY_HEIGHT;
                if (mx >= this.leftPos + 4 && mx <= this.leftPos + this.imageWidth - 4
                        && my >= y && my < y + ENTRY_HEIGHT) {
                    selectedRelicId = infusableRelics.get(i + scrollOffset).relicId();
                    return true;
                }
            }
        } else {
            // Click on buff choices
            String[] keys = {"health", "damage", "speed"};
            for (int i = 0; i < 3; i++) {
                int y = this.topPos + BUFF_BUTTON_Y + i * (BUFF_BUTTON_HEIGHT + 4);
                int x = this.leftPos + (this.imageWidth - BUFF_BUTTON_WIDTH) / 2;

                if (mx >= x && mx <= x + BUFF_BUTTON_WIDTH && my >= y && my < y + BUFF_BUTTON_HEIGHT) {
                    ClientPlayNetworking.send(new RelicInfusePayload(this.menu.getPos(), selectedRelicId, keys[i]));
                    selectedRelicId = null;
                    return true;
                }
            }

            // Back button
            int backY = this.topPos + this.imageHeight - 22;
            if (mx >= this.leftPos + 8 && mx <= this.leftPos + 58 && my >= backY && my < backY + 16) {
                selectedRelicId = null;
                return true;
            }
        }

        return super.mouseClicked(event, forwarded);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (selectedRelicId == null && !infusableRelics.isEmpty()) {
            int maxScroll = Math.max(0, infusableRelics.size() - MAX_VISIBLE);
            if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (scrollY < 0) {
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
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
