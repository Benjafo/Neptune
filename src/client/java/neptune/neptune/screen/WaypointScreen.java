package neptune.neptune.screen;

import neptune.neptune.network.WaypointSyncPayload;
import neptune.neptune.network.WaypointTeleportPayload;
import neptune.neptune.processing.WaypointMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class WaypointScreen extends AbstractContainerScreen<WaypointMenu> {

    private List<WaypointSyncPayload.WaypointEntry> waypoints = List.of();

    private static final int ENTRY_HEIGHT = 28;
    private static final int LIST_START_Y = 30;

    public WaypointScreen(WaypointMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 250;
        this.imageHeight = 150;
    }

    public void handleSync(WaypointSyncPayload payload) {
        this.waypoints = payload.waypoints();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xCC000000);
        drawBorder(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF555555);

        graphics.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0xFFAA00, false);
        graphics.fill(this.leftPos + 4, this.topPos + 18, this.leftPos + this.imageWidth - 4, this.topPos + 19, 0xFF333333);

        if (waypoints.isEmpty()) {
            graphics.drawString(this.font, "No waypoints placed yet.", this.leftPos + 8, this.topPos + LIST_START_Y, 0x888888, false);
            return;
        }

        graphics.drawString(this.font, "Click a waypoint to teleport:", this.leftPos + 8, this.topPos + 22, 0x888888, false);

        for (int i = 0; i < waypoints.size(); i++) {
            WaypointSyncPayload.WaypointEntry wp = waypoints.get(i);
            int y = this.topPos + LIST_START_Y + i * ENTRY_HEIGHT;

            boolean isCurrentPos = wp.pos().equals(this.menu.getPos());
            boolean hovered = !isCurrentPos && mouseX >= this.leftPos + 4 && mouseX <= this.leftPos + this.imageWidth - 4
                    && mouseY >= y && mouseY < y + ENTRY_HEIGHT;

            if (isCurrentPos) {
                graphics.fill(this.leftPos + 4, y, this.leftPos + this.imageWidth - 4, y + ENTRY_HEIGHT, 0x20FFFF00);
            } else if (hovered) {
                graphics.fill(this.leftPos + 4, y, this.leftPos + this.imageWidth - 4, y + ENTRY_HEIGHT, 0x40FFFFFF);
            }

            int nameColor = isCurrentPos ? 0xFFFF55 : 0xFFFFFF;
            graphics.drawString(this.font, wp.name(), this.leftPos + 8, y + 2, nameColor, false);

            String coords = "§7(" + wp.pos().getX() + ", " + wp.pos().getY() + ", " + wp.pos().getZ() + ")";
            graphics.drawString(this.font, coords, this.leftPos + 8, y + 14, 0x888888, false);

            if (isCurrentPos) {
                String here = "§e(Here)";
                int hereWidth = this.font.width(here);
                graphics.drawString(this.font, here, this.leftPos + this.imageWidth - hereWidth - 8, y + 8, 0xFFFF55, false);
            } else {
                String costText = wp.teleportCost() + " essence";
                int costWidth = this.font.width(costText);
                graphics.drawString(this.font, costText, this.leftPos + this.imageWidth - costWidth - 8, y + 8, 0x55FF55, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        if (event.button() != 0) return super.mouseClicked(event, forwarded);

        double mx = event.x();
        double my = event.y();

        for (int i = 0; i < waypoints.size(); i++) {
            WaypointSyncPayload.WaypointEntry wp = waypoints.get(i);
            int y = this.topPos + LIST_START_Y + i * ENTRY_HEIGHT;

            if (wp.pos().equals(this.menu.getPos())) continue;

            if (mx >= this.leftPos + 4 && mx <= this.leftPos + this.imageWidth - 4
                    && my >= y && my < y + ENTRY_HEIGHT) {
                ClientPlayNetworking.send(new WaypointTeleportPayload(this.menu.getPos(), i));
                return true;
            }
        }

        return super.mouseClicked(event, forwarded);
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
