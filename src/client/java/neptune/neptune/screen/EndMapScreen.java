
package neptune.neptune.screen;

import neptune.neptune.map.ClientMapState;
import neptune.neptune.map.EndMapData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Full-screen map view showing the entire explored grid with marked cities.
 * Scrollable and zoomable via mouse wheel.
 */
public class EndMapScreen extends Screen {

    private static final int CELL_SIZE_BASE = 20;
    private static final int MIN_CELL_SIZE = 6;
    private static final int MAX_CELL_SIZE = 40;

    // Colors
    private static final int BG_COLOR = 0xFF0A0A14;
    private static final int GRID_LINE_COLOR = 0x30FFFFFF;
    private static final int MARKED_COLOR = 0xBB00DD00;
    private static final int PLAYER_COLOR = 0xFFFF5555;
    private static final int PLAYER_CELL_COLOR = 0x40FF5555;
    private static final int BORDER_COLOR = 0xFF555555;

    private int cellSize = CELL_SIZE_BASE;
    private double scrollX = 0;
    private double scrollZ = 0;
    private boolean dragging = false;
    private double dragStartX, dragStartY;
    private double dragStartScrollX, dragStartScrollZ;

    public EndMapScreen() {
        super(Component.literal("End Map"));
    }

    @Override
    protected void init() {
        super.init();
        // Center on player position
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && ClientMapState.hasLoadedMap()) {
            scrollX = ClientMapState.toGridX(mc.player.getX());
            scrollZ = ClientMapState.toGridZ(mc.player.getZ());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Full screen dark background
        graphics.fill(0, 0, this.width, this.height, BG_COLOR);

        if (!ClientMapState.hasLoadedMap()) {
            graphics.drawCenteredString(this.font, "No map loaded. Use /endmap load <name>", this.width / 2, this.height / 2, 0xFF5555);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int centerScreenX = this.width / 2;
        int centerScreenY = this.height / 2;

        // Calculate visible grid range
        int visibleCellsX = (this.width / cellSize) / 2 + 2;
        int visibleCellsZ = (this.height / cellSize) / 2 + 2;

        int centerGridX = (int) Math.round(scrollX);
        int centerGridZ = (int) Math.round(scrollZ);

        // Draw grid
        for (int dx = -visibleCellsX; dx <= visibleCellsX; dx++) {
            for (int dz = -visibleCellsZ; dz <= visibleCellsZ; dz++) {
                int gx = centerGridX + dx;
                int gz = centerGridZ + dz;

                int screenCellX = centerScreenX + (int) ((gx - scrollX) * cellSize);
                int screenCellY = centerScreenY + (int) ((gz - scrollZ) * cellSize);

                // Marked cells
                if (ClientMapState.isMarked(gx, gz)) {
                    graphics.fill(screenCellX + 1, screenCellY + 1,
                            screenCellX + cellSize - 1, screenCellY + cellSize - 1, MARKED_COLOR);
                }

                // Grid lines
                graphics.fill(screenCellX, screenCellY, screenCellX + cellSize, screenCellY + 1, GRID_LINE_COLOR);
                graphics.fill(screenCellX, screenCellY, screenCellX + 1, screenCellY + cellSize, GRID_LINE_COLOR);
            }
        }

        // Draw player position
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            int playerGridX = ClientMapState.toGridX(mc.player.getX());
            int playerGridZ = ClientMapState.toGridZ(mc.player.getZ());

            int playerScreenX = centerScreenX + (int) ((playerGridX - scrollX) * cellSize);
            int playerScreenY = centerScreenY + (int) ((playerGridZ - scrollZ) * cellSize);

            // Player cell highlight
            graphics.fill(playerScreenX, playerScreenY,
                    playerScreenX + cellSize, playerScreenY + cellSize, PLAYER_CELL_COLOR);

            // Player dot
            int dotCX = playerScreenX + cellSize / 2;
            int dotCY = playerScreenY + cellSize / 2;
            int dotSize = Math.max(2, cellSize / 4);
            graphics.fill(dotCX - dotSize, dotCY - dotSize, dotCX + dotSize, dotCY + dotSize, PLAYER_COLOR);
        }

        // Title bar
        graphics.fill(0, 0, this.width, 20, 0xCC000000);
        graphics.fill(0, 20, this.width, 21, BORDER_COLOR);
        graphics.drawString(this.font, "End Map: " + ClientMapState.getMapName(), 5, 6, 0xFFAA00, true);

        String stats = ClientMapState.getMarkedGrids().size() + " cities marked | Grid: " + ClientMapState.getGridSize() + "b | Scroll to zoom";
        int statsWidth = this.font.width(stats);
        graphics.drawString(this.font, stats, this.width - statsWidth - 5, 6, 0x888888, true);

        // Hovered cell info
        int hoveredGridX = centerGridX + (int) Math.floor((double) (mouseX - centerScreenX) / cellSize + (scrollX - centerGridX));
        int hoveredGridZ = centerGridZ + (int) Math.floor((double) (mouseY - centerScreenY) / cellSize + (scrollZ - centerGridZ));
        String hoverInfo = "Grid [" + hoveredGridX + ", " + hoveredGridZ + "]";
        if (ClientMapState.isMarked(hoveredGridX, hoveredGridZ)) {
            hoverInfo += " - CITY";
        }
        graphics.drawString(this.font, hoverInfo, 5, this.height - 14, 0xAAAAAA, true);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        if (event.button() == 2 || event.button() == 0) { // Middle or left click to drag
            dragging = true;
            dragStartX = event.x();
            dragStartY = event.y();
            dragStartScrollX = scrollX;
            dragStartScrollZ = scrollZ;
            return true;
        }
        return super.mouseClicked(event, forwarded);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 2 || event.button() == 0) {
            dragging = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) {
            double dx = event.x() - dragStartX;
            double dy = event.y() - dragStartY;
            scrollX = dragStartScrollX - dx / cellSize;
            scrollZ = dragStartScrollZ - dy / cellSize;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            cellSize = Math.min(MAX_CELL_SIZE, cellSize + 2);
        } else if (scrollY < 0) {
            cellSize = Math.max(MIN_CELL_SIZE, cellSize - 2);
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
