package neptune.neptune.hud;

import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.map.ClientMapState;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
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
    private static final int MAP_SIZE = 105; // pixels (divisible by 7)
    private static final int GRID_CELLS = 7; // 7x7 grid of cells visible
    private static final int CELL_SIZE = MAP_SIZE / GRID_CELLS; // 15px per cell
    private static final int MARGIN = 5;

    // Colors
    private static final int BG_COLOR = 0xCC0A0A1E;
    private static final int CELL_LIGHT = 0xCC141428;
    private static final int CELL_DARK = 0xCC0A0A1E;
    private static final int GRID_LINE_COLOR = 0x60FFFFFF;
    private static final int MARKED_COLOR = 0xCC00DD00;
    private static final int PLAYER_CELL_COLOR = 0x50FF5555;
    private static final int PLAYER_DOT_COLOR = 0xFFFF5555;
    private static final int BORDER_COLOR = 0xFF666666;
    private static final int TITLE_COLOR = 0xFFFFAA00;
    private static final int AXIS_COLOR = 0x80FFFF55;
    private static final int CARDINAL_COLOR = 0xCCCCCCCC;

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

        // Minimap requires Navigation T2
        UnlockData unlocks = mc.player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        if (!unlocks.hasTier(UnlockBranch.NAVIGATION, 2)) return;

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

        // Background with border
        graphics.fill(mapX - 2, mapY - 2, mapX + MAP_SIZE + 2, mapY + MAP_SIZE + 2, BORDER_COLOR);
        graphics.fill(mapX, mapY, mapX + MAP_SIZE, mapY + MAP_SIZE, BG_COLOR);

        int halfCells = GRID_CELLS / 2;

        // Draw grid cells
        for (int dx = -halfCells; dx <= halfCells; dx++) {
            for (int dz = -halfCells; dz <= halfCells; dz++) {
                int gx = playerGridX + dx;
                int gz = playerGridZ + dz;

                int cellX = mapX + (dx + halfCells) * CELL_SIZE;
                int cellY = mapY + (dz + halfCells) * CELL_SIZE;

                // Checkerboard pattern — shifts visually as player crosses grid boundaries
                int checkerColor = ((gx + gz) & 1) == 0 ? CELL_LIGHT : CELL_DARK;
                graphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, checkerColor);

                // Highlight player's cell
                if (dx == 0 && dz == 0) {
                    graphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, PLAYER_CELL_COLOR);
                }

                // Draw marked cells
                if (ClientMapState.isMarked(gx, gz)) {
                    graphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, MARKED_COLOR);
                }

                // Highlight axis lines (where grid X=0 or Z=0 passes through)
                if (gx == 0 || gx == -1) {
                    graphics.fill(cellX + CELL_SIZE - 1, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, AXIS_COLOR);
                }
                if (gz == 0 || gz == -1) {
                    graphics.fill(cellX, cellY + CELL_SIZE - 1, cellX + CELL_SIZE, cellY + CELL_SIZE, AXIS_COLOR);
                }

                // Grid lines
                graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + 1, GRID_LINE_COLOR);
                graphics.fill(cellX, cellY, cellX + 1, cellY + CELL_SIZE, GRID_LINE_COLOR);
            }
        }

        // Draw right and bottom border grid lines
        graphics.fill(mapX, mapY + MAP_SIZE - 1, mapX + MAP_SIZE, mapY + MAP_SIZE, GRID_LINE_COLOR);
        graphics.fill(mapX + MAP_SIZE - 1, mapY, mapX + MAP_SIZE, mapY + MAP_SIZE, GRID_LINE_COLOR);

        // Draw player indicator (center cell) — crosshair style
        int centerCellX = mapX + halfCells * CELL_SIZE;
        int centerCellY = mapY + halfCells * CELL_SIZE;
        int cx = centerCellX + CELL_SIZE / 2;
        int cy = centerCellY + CELL_SIZE / 2;

        // Crosshair
        graphics.fill(cx - 1, centerCellY + 2, cx + 1, centerCellY + CELL_SIZE - 2, PLAYER_DOT_COLOR);
        graphics.fill(centerCellX + 2, cy - 1, centerCellX + CELL_SIZE - 2, cy + 1, PLAYER_DOT_COLOR);

        // Cardinal direction labels
        int dirY = mapY - 1;
        graphics.drawString(mc.font, "N", mapX + MAP_SIZE / 2 - 2, dirY - 9, CARDINAL_COLOR, true);
        graphics.drawString(mc.font, "S", mapX + MAP_SIZE / 2 - 2, mapY + MAP_SIZE + 2, CARDINAL_COLOR, true);
        graphics.drawString(mc.font, "W", mapX - 9, mapY + MAP_SIZE / 2 - 4, CARDINAL_COLOR, true);
        graphics.drawString(mc.font, "E", mapX + MAP_SIZE + 3, mapY + MAP_SIZE / 2 - 4, CARDINAL_COLOR, true);

        // Grid coordinates below the map (below S label)
        String coords = "[" + playerGridX + ", " + playerGridZ + "]";
        int coordsWidth = mc.font.width(coords);
        graphics.drawString(mc.font, coords, mapX + MAP_SIZE - coordsWidth, mapY + MAP_SIZE + 12, 0xCCCCCC, true);

        // Marked count
        int markedCount = ClientMapState.getMarkedCount();
        if (markedCount > 0) {
            String count = markedCount + " cities";
            graphics.drawString(mc.font, count, mapX, mapY + MAP_SIZE + 12, 0x88CC88, true);
        }
    }
}
