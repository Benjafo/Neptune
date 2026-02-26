package neptune.neptune.screen;

import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.relic.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Relic Journal UI — shows all relics organized by set.
 * Discovered relics display with rarity color and name.
 * Undiscovered relics show as dark silhouettes with "???".
 * Scrollable to accommodate all 120 relics across 13 sets.
 * Gated behind Catalog T1 unlock.
 */
public class RelicJournalScreen extends Screen {

    // Colors
    private static final int BG_COLOR = 0xEE0A0A14;
    private static final int BORDER_COLOR = 0xFF555555;
    private static final int HEADER_BG = 0xFF1A1A2E;
    private static final int SECTION_BG = 0xFF12122A;
    private static final int UNDISCOVERED_COLOR = 0xFF333344;
    private static final int UNDISCOVERED_TEXT = 0xFF555566;
    private static final int SET_COMPLETE_COLOR = 0xFF22AA22;
    private static final int MAJOR_SET_LABEL = 0xFFFFAA00;
    private static final int MINOR_SET_LABEL = 0xFFAAAACC;
    private static final int STANDALONE_LABEL = 0xFFCC88FF;
    private static final int PROGRESS_BAR_BG = 0xFF222233;
    private static final int PROGRESS_BAR_FILL = 0xFF6644AA;
    private static final int SCROLL_TRACK = 0xFF1A1A2E;
    private static final int SCROLL_THUMB = 0xFF555577;

    // Layout
    private static final int PANEL_WIDTH = 300;
    private static final int RELIC_ENTRY_HEIGHT = 14;
    private static final int SET_HEADER_HEIGHT = 24;
    private static final int SET_SPACING = 8;
    private static final int PADDING = 8;

    private int panelLeft;
    private int panelTop;
    private int panelHeight;

    private float scrollOffset = 0;
    private int totalContentHeight = 0;

    // Precomputed layout sections
    private final List<Section> sections = new ArrayList<>();

    public RelicJournalScreen() {
        super(Component.literal("Relic Journal"));
    }

    @Override
    protected void init() {
        super.init();
        panelLeft = (this.width - PANEL_WIDTH) / 2;
        panelTop = 30;
        panelHeight = this.height - 60;
        scrollOffset = 0;
        buildSections();
    }

    private void buildSections() {
        sections.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        RelicJournalData journal = mc.player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);

        // Major sets first
        for (RelicSet set : RelicSet.values()) {
            if (!set.isMajor()) continue;
            sections.add(buildSection(set, journal, MAJOR_SET_LABEL));
        }
        // Minor sets
        for (RelicSet set : RelicSet.values()) {
            if (!set.isMinor()) continue;
            sections.add(buildSection(set, journal, MINOR_SET_LABEL));
        }
        // Standalone
        sections.add(buildSection(RelicSet.STANDALONE, journal, STANDALONE_LABEL));

        // Calculate total content height
        totalContentHeight = 0;
        for (Section s : sections) {
            totalContentHeight += SET_HEADER_HEIGHT + s.relics.size() * RELIC_ENTRY_HEIGHT + SET_SPACING;
        }
    }

    private Section buildSection(RelicSet set, RelicJournalData journal, int labelColor) {
        List<RelicDefinition> relics = RelicDefinition.getBySet(set);
        int discovered = journal.getSetProgress(set);
        boolean complete = journal.isSetComplete(set);

        List<RelicEntry> entries = new ArrayList<>();
        for (RelicDefinition def : relics) {
            boolean found = journal.hasDiscovered(def.id());
            entries.add(new RelicEntry(def, found));
        }

        String label = set == RelicSet.STANDALONE ? "Standalone Relics" : set.getDisplayName();
        if (set.isMajor()) label = "★ " + label;

        return new Section(label, labelColor, discovered, set.getSize(), complete, entries, set.isMajor());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Full screen dimmed background
        graphics.fill(0, 0, this.width, this.height, 0xAA000000);

        // Panel background
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, BG_COLOR);
        drawBorder(graphics, panelLeft, panelTop, PANEL_WIDTH, panelHeight, BORDER_COLOR);

        // Title bar
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 22, HEADER_BG);
        graphics.fill(panelLeft, panelTop + 22, panelLeft + PANEL_WIDTH, panelTop + 23, BORDER_COLOR);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        RelicJournalData journal = mc.player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
        String titleText = "Relic Journal — " + journal.getDiscoveredCount() + "/" + RelicDefinition.totalCount();
        graphics.drawCenteredString(this.font, titleText, panelLeft + PANEL_WIDTH / 2, panelTop + 7, 0xFFAA00);

        // Content area with scissoring
        int contentTop = panelTop + 24;
        int contentHeight = panelHeight - 24;

        graphics.enableScissor(panelLeft + 1, contentTop, panelLeft + PANEL_WIDTH - 1, contentTop + contentHeight);

        int y = contentTop - (int) scrollOffset + PADDING;
        String hoveredTooltip = null;

        for (Section section : sections) {
            // Set header
            int headerY = y;
            graphics.fill(panelLeft + PADDING, y, panelLeft + PANEL_WIDTH - PADDING, y + SET_HEADER_HEIGHT, SECTION_BG);

            // Set name
            String setLabel = section.label + " (" + section.discovered + "/" + section.total + ")";
            int setLabelColor = section.complete ? SET_COMPLETE_COLOR : section.labelColor;
            graphics.drawString(this.font, setLabel, panelLeft + PADDING + 4, y + 3, setLabelColor, true);

            // Progress bar
            int barX = panelLeft + PADDING + 4;
            int barY = y + 14;
            int barWidth = PANEL_WIDTH - PADDING * 2 - 8;
            int barHeight = 5;
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, PROGRESS_BAR_BG);
            if (section.total > 0) {
                int fillWidth = (int) ((float) section.discovered / section.total * barWidth);
                int fillColor = section.complete ? SET_COMPLETE_COLOR : PROGRESS_BAR_FILL;
                graphics.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);
            }

            y += SET_HEADER_HEIGHT;

            // Relic entries
            for (RelicEntry entry : section.relics) {
                if (y + RELIC_ENTRY_HEIGHT > contentTop && y < contentTop + contentHeight) {
                    renderRelicEntry(graphics, entry, panelLeft + PADDING + 4, y, mouseX, mouseY);

                    // Tooltip check
                    if (mouseX >= panelLeft + PADDING && mouseX <= panelLeft + PANEL_WIDTH - PADDING
                            && mouseY >= y && mouseY < y + RELIC_ENTRY_HEIGHT
                            && mouseY >= contentTop && mouseY < contentTop + contentHeight) {
                        hoveredTooltip = getEntryTooltip(entry, section);
                    }
                }
                y += RELIC_ENTRY_HEIGHT;
            }

            y += SET_SPACING;
        }

        graphics.disableScissor();

        // Scrollbar
        if (totalContentHeight > contentHeight) {
            int scrollTrackX = panelLeft + PANEL_WIDTH - 8;
            int scrollTrackHeight = contentHeight;
            graphics.fill(scrollTrackX, contentTop, scrollTrackX + 6, contentTop + scrollTrackHeight, SCROLL_TRACK);

            float viewRatio = (float) contentHeight / totalContentHeight;
            int thumbHeight = Math.max(20, (int) (scrollTrackHeight * viewRatio));
            float maxScroll = totalContentHeight - contentHeight + PADDING * 2;
            float scrollRatio = maxScroll > 0 ? scrollOffset / maxScroll : 0;
            int thumbY = contentTop + (int) (scrollRatio * (scrollTrackHeight - thumbHeight));
            graphics.fill(scrollTrackX, thumbY, scrollTrackX + 6, thumbY + thumbHeight, SCROLL_THUMB);
        }

        // Tooltip
        if (hoveredTooltip != null) {
            List<Component> tooltip = new ArrayList<>();
            for (String line : hoveredTooltip.split("\n")) {
                tooltip.add(Component.literal(line));
            }
            graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderRelicEntry(GuiGraphics graphics, RelicEntry entry, int x, int y, int mouseX, int mouseY) {
        if (entry.discovered) {
            // Show rarity icon + colored name
            String rarityIcon = getRarityIcon(entry.def.rarity());
            String colorCode = entry.def.rarity().getColorCode();
            graphics.drawString(this.font, rarityIcon + " " + colorCode + entry.def.displayName(),
                    x, y + 2, 0xFFFFFF, false);
        } else {
            // Silhouette - dark icon + "???"
            graphics.drawString(this.font, "  §8???", x, y + 2, UNDISCOVERED_TEXT, false);

            // Small rarity hint bar
            int hintColor = getUndiscoveredHintColor(entry.def.rarity());
            graphics.fill(x, y + 3, x + 2, y + 10, hintColor);
        }
    }

    private String getEntryTooltip(RelicEntry entry, Section section) {
        if (entry.discovered) {
            StringBuilder sb = new StringBuilder();
            sb.append(entry.def.rarity().getColorCode()).append(entry.def.displayName()).append("\n");
            sb.append("§7Rarity: ").append(entry.def.rarity().getColorCode()).append(entry.def.rarity().getDisplayName()).append("\n");
            if (entry.def.lore() != null && !entry.def.lore().isEmpty()) {
                sb.append("§8§o").append(entry.def.lore());
            }
            if (section.major && section.complete) {
                sb.append("\n§a★ Set bonus active!");
            }
            return sb.toString();
        } else {
            return "§8Undiscovered relic\n§7Find this relic in End city chests";
        }
    }

    private String getRarityIcon(RelicRarity rarity) {
        return switch (rarity) {
            case COMMON -> "§f◆";
            case UNCOMMON -> "§a◆";
            case RARE -> "§9◆";
            case VERY_RARE -> "§d◆";
            case LEGENDARY -> "§6◆";
        };
    }

    private int getUndiscoveredHintColor(RelicRarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFF444455;
            case UNCOMMON -> 0xFF335533;
            case RARE -> 0xFF333366;
            case VERY_RARE -> 0xFF553366;
            case LEGENDARY -> 0xFF554422;
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int contentHeight = panelHeight - 24;
        float maxScroll = Math.max(0, totalContentHeight - contentHeight + PADDING * 2);
        scrollOffset = (float) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 20));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private record Section(String label, int labelColor, int discovered, int total,
                           boolean complete, List<RelicEntry> relics, boolean major) {}

    private record RelicEntry(RelicDefinition def, boolean discovered) {}
}
