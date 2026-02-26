package neptune.neptune.relic;

public enum RelicRarity {
    COMMON(20, 15, 0x55FF55),
    UNCOMMON(35, 15, 0x55FFFF),
    RARE(50, 15, 0xFFAA00),
    VERY_RARE(75, 15, 0xFF55FF),
    LEGENDARY(100, 15, 0xFFFF55);

    private final int sellValue;
    private final int duplicateSellValue;
    private final int color;

    RelicRarity(int sellValue, int duplicateSellValue, int color) {
        this.sellValue = sellValue;
        this.duplicateSellValue = duplicateSellValue;
        this.color = color;
    }

    public int getSellValue() { return sellValue; }
    public int getDuplicateSellValue() { return duplicateSellValue; }
    public int getColor() { return color; }

    public String getDisplayName() {
        return switch (this) {
            case COMMON -> "Common";
            case UNCOMMON -> "Uncommon";
            case RARE -> "Rare";
            case VERY_RARE -> "Very Rare";
            case LEGENDARY -> "Legendary";
        };
    }

    public String getColorCode() {
        return switch (this) {
            case COMMON -> "§a";
            case UNCOMMON -> "§b";
            case RARE -> "§6";
            case VERY_RARE -> "§d";
            case LEGENDARY -> "§e";
        };
    }
}
