package neptune.neptune.hud;

import neptune.neptune.map.ClientMapState;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * Minimap HUD overlay showing a grid centered on the player's position.
 * Displays marked city grids as colored cells.
 */
public class EndMapHud {

    private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("neptune", "end_map_hud");

    // Minimap display settings
    private static final int MAP_SIZE = 100; // pixels
    private static final int GRID_CELLS = 7; // 7x7 grid of cells visible
    private static final int CELL_SIZE = MAP_SIZE / GRID_CELLS; // ~14px per cell
    private static final int MARGIN = 5;

    // Colors
    private static final int BG_COLOR = 0xAA000000;
    private static final int GRID_LINE_COLOR = 0x40FFFFFF;
    private static final int MARKED_COLOR = 0xBB00DD00;
    private static final int PLAYER_COLOR = 0xFFFF5555;
    private static final int BORDER_COLOR = 0xFF555555;
    private static final int TITLE_COLOR = 0xFFAA00;

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                HUD_ID,
                EndMapHud::render
        );
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.player.level().dimension() != Level.END) return;
        if (!ClientMapState.hasLoadedMap()) return;
        if (!ClientMapState.isMinimapVisible()) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // Position: top-right corner
        int mapX = screenWidth - MAP_SIZE - MARGIN;
        int mapY = MARGIN + 12; // Below the title text

        int playerGridX = ClientMapState.toGridX(mc.player.getX());
        int playerGridZ = ClientMapState.toGridZ(mc.player.getZ());

        // Title
        String title = ClientMapState.getMapName();
        int titleWidth = mc.font.width(title);
        graphics.drawString(mc.font, title, screenWidth - titleWidth - MARGIN, MARGIN, TITLE_COLOR, true);

        // Background
        graphics.fill(mapX - 1, mapY - 1, mapX + MAP_SIZE + 1, mapY + MAP_SIZE + 1, BORDER_COLOR);
        graphics.fill(mapX, mapY, mapX + MAP_SIZE, mapY + MAP_SIZE, BG_COLOR);

        int halfCells = GRID_CELLS / 2;

        // Draw grid cells
        for (int dx = -halfCells; dx <= halfCells; dx++) {
            for (int dz = -halfCells; dz <= halfCells; dz++) {
                int gx = playerGridX + dx;
                int gz = playerGridZ + dz;

                int cellX = mapX + (dx + halfCells) * CELL_SIZE;
                int cellY = mapY + (dz + halfCells) * CELL_SIZE;

                // Draw marked cells
                if (ClientMapState.isMarked(gx, gz)) {
                    graphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, MARKED_COLOR);
                }

                // Grid lines
                graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + 1, GRID_LINE_COLOR);
                graphics.fill(cellX, cellY, cellX + 1, cellY + CELL_SIZE, GRID_LINE_COLOR);
            }
        }

        // Draw right and bottom border grid lines
        graphics.fill(mapX, mapY + MAP_SIZE - 1, mapX + MAP_SIZE, mapY + MAP_SIZE, GRID_LINE_COLOR);
        graphics.fill(mapX + MAP_SIZE - 1, mapY, mapX + MAP_SIZE, mapY + MAP_SIZE, GRID_LINE_COLOR);

        // Draw player indicator (center cell)
        int centerX = mapX + halfCells * CELL_SIZE + CELL_SIZE / 2;
        int centerY = mapY + halfCells * CELL_SIZE + CELL_SIZE / 2;
        int dotSize = 2;
        graphics.fill(centerX - dotSize, centerY - dotSize, centerX + dotSize, centerY + dotSize, PLAYER_COLOR);

        // Grid coordinates
        String coords = "[" + playerGridX + ", " + playerGridZ + "]";
        int coordsWidth = mc.font.width(coords);
        graphics.drawString(mc.font, coords, mapX + MAP_SIZE - coordsWidth, mapY + MAP_SIZE + 2, 0xAAAAAA, true);

        // Marked count
        String count = ClientMapState.getMarkedGrids().size() + " cities";
        graphics.drawString(mc.font, count, mapX, mapY + MAP_SIZE + 2, 0x888888, true);
    }
}
