package neptune.neptune.screen;

import neptune.neptune.processing.VoidPouchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VoidPouchScreen extends AbstractContainerScreen<VoidPouchMenu> {

    public VoidPouchScreen(VoidPouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = 40;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Background
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xCC000000);
        drawBorder(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF555555);

        // Title
        graphics.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0xFFAA00, false);

        // Separator
        graphics.fill(this.leftPos + 4, this.topPos + 16, this.leftPos + this.imageWidth - 4, this.topPos + 17, 0xFF333333);

        // Draw slot backgrounds for pouch
        for (int col = 0; col < 9; col++) {
            int x = this.leftPos + 7 + col * 18;
            int y = this.topPos + 19;
            graphics.fill(x, y, x + 18, y + 18, 0xFF222222);
        }

        // Separator before player inventory
        graphics.fill(this.leftPos + 4, this.topPos + 39, this.leftPos + this.imageWidth - 4, this.topPos + 40, 0xFF333333);

        // Draw slot backgrounds for player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = this.leftPos + 7 + col * 18;
                int y = this.topPos + 50 + row * 18;
                graphics.fill(x, y, x + 18, y + 18, 0xFF222222);
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            int x = this.leftPos + 7 + col * 18;
            int y = this.topPos + 108;
            graphics.fill(x, y, x + 18, y + 18, 0xFF222222);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
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
