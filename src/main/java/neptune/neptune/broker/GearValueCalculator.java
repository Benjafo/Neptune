package neptune.neptune.broker;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.relic.RelicDefinition;
import neptune.neptune.relic.RelicItem;
import neptune.neptune.relic.NeptuneItems;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Calculates the void essence sell value of items.
 * Formula: (Base Material Value × Type Multiplier + Enchantments) × Durability Modifier × Sell Rate
 */
public class GearValueCalculator {

    // Sell rate modifier (before Processing T1: 0.7, after: 1.0)
    private static float sellRateModifier = 0.7f;

    // --- Base Material Values ---
    private static final Map<Item, Float> BASE_VALUES = new HashMap<>();

    static {
        // Leather gear (base 2)
        registerGear(Items.LEATHER_HELMET, 2f, 1.0f);
        registerGear(Items.LEATHER_CHESTPLATE, 2f, 1.6f);
        registerGear(Items.LEATHER_LEGGINGS, 2f, 1.4f);
        registerGear(Items.LEATHER_BOOTS, 2f, 1.0f);

        // Gold gear (base 3)
        registerGear(Items.GOLDEN_HELMET, 3f, 1.0f);
        registerGear(Items.GOLDEN_CHESTPLATE, 3f, 1.6f);
        registerGear(Items.GOLDEN_LEGGINGS, 3f, 1.4f);
        registerGear(Items.GOLDEN_BOOTS, 3f, 1.0f);
        registerGear(Items.GOLDEN_SWORD, 3f, 1.2f);
        registerGear(Items.GOLDEN_PICKAXE, 3f, 1.4f);
        registerGear(Items.GOLDEN_AXE, 3f, 1.4f);
        registerGear(Items.GOLDEN_SHOVEL, 3f, 0.8f);
        registerGear(Items.GOLDEN_HOE, 3f, 0.6f);

        // Chainmail gear (base 4)
        registerGear(Items.CHAINMAIL_HELMET, 4f, 1.0f);
        registerGear(Items.CHAINMAIL_CHESTPLATE, 4f, 1.6f);
        registerGear(Items.CHAINMAIL_LEGGINGS, 4f, 1.4f);
        registerGear(Items.CHAINMAIL_BOOTS, 4f, 1.0f);

        // Iron gear (base 2)
        registerGear(Items.IRON_HELMET, 2f, 1.0f);
        registerGear(Items.IRON_CHESTPLATE, 2f, 1.6f);
        registerGear(Items.IRON_LEGGINGS, 2f, 1.4f);
        registerGear(Items.IRON_BOOTS, 2f, 1.0f);
        registerGear(Items.IRON_SWORD, 2f, 1.2f);
        registerGear(Items.IRON_PICKAXE, 2f, 1.4f);
        registerGear(Items.IRON_AXE, 2f, 1.4f);
        registerGear(Items.IRON_SHOVEL, 2f, 0.8f);
        registerGear(Items.IRON_HOE, 2f, 0.6f);

        // Diamond gear (base 7)
        registerGear(Items.DIAMOND_HELMET, 7f, 1.0f);
        registerGear(Items.DIAMOND_CHESTPLATE, 7f, 1.6f);
        registerGear(Items.DIAMOND_LEGGINGS, 7f, 1.4f);
        registerGear(Items.DIAMOND_BOOTS, 7f, 1.0f);
        registerGear(Items.DIAMOND_SWORD, 7f, 1.2f);
        registerGear(Items.DIAMOND_PICKAXE, 7f, 1.4f);
        registerGear(Items.DIAMOND_AXE, 7f, 1.4f);
        registerGear(Items.DIAMOND_SHOVEL, 7f, 0.8f);
        registerGear(Items.DIAMOND_HOE, 7f, 0.6f);

        // Netherite gear (base 18)
        registerGear(Items.NETHERITE_HELMET, 18f, 1.0f);
        registerGear(Items.NETHERITE_CHESTPLATE, 18f, 1.6f);
        registerGear(Items.NETHERITE_LEGGINGS, 18f, 1.4f);
        registerGear(Items.NETHERITE_BOOTS, 18f, 1.0f);
        registerGear(Items.NETHERITE_SWORD, 18f, 1.2f);
        registerGear(Items.NETHERITE_PICKAXE, 18f, 1.4f);
        registerGear(Items.NETHERITE_AXE, 18f, 1.4f);
        registerGear(Items.NETHERITE_SHOVEL, 18f, 0.8f);
        registerGear(Items.NETHERITE_HOE, 18f, 0.6f);
    }

    private static void registerGear(Item item, float baseMaterial, float typeMultiplier) {
        BASE_VALUES.put(item, baseMaterial * typeMultiplier);
    }

    // --- Other sellable items (fixed values) ---
    private static final Map<Item, Float> OTHER_VALUES = new HashMap<>();

    static {
        OTHER_VALUES.put(Items.GOLD_INGOT, 1f);
        OTHER_VALUES.put(Items.IRON_INGOT, 0.5f);
        OTHER_VALUES.put(Items.DIAMOND, 2f);
        OTHER_VALUES.put(Items.EMERALD, 2f);
        OTHER_VALUES.put(Items.SADDLE, 5f);
        OTHER_VALUES.put(Items.IRON_HORSE_ARMOR, 2f);
        OTHER_VALUES.put(Items.GOLDEN_HORSE_ARMOR, 3f);
        OTHER_VALUES.put(Items.DIAMOND_HORSE_ARMOR, 5f);
        OTHER_VALUES.put(Items.COPPER_HORSE_ARMOR, 1f);
        OTHER_VALUES.put(Items.ENDER_PEARL, 1f);
        OTHER_VALUES.put(Items.CHORUS_FRUIT, 0.1f);
        OTHER_VALUES.put(Items.SHULKER_SHELL, 4f);
        OTHER_VALUES.put(Items.ELYTRA, 25f);
        OTHER_VALUES.put(Items.DRAGON_HEAD, 25f);
        OTHER_VALUES.put(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 5f);
    }

    // --- Enchantment value tiers ---
    // Common enchantments: +0.5 per level
    private static final Set<ResourceKey<Enchantment>> COMMON_ENCHANTS = Set.of(
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION,
            Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION,
            Enchantments.THORNS, Enchantments.UNBREAKING,
            Enchantments.RESPIRATION, Enchantments.AQUA_AFFINITY,
            Enchantments.FEATHER_FALLING, Enchantments.DEPTH_STRIDER,
            Enchantments.FROST_WALKER, Enchantments.SOUL_SPEED,
            Enchantments.SWIFT_SNEAK, Enchantments.KNOCKBACK
    );

    // Mid-tier enchantments: +1 per level
    private static final Set<ResourceKey<Enchantment>> MID_ENCHANTS = Set.of(
            Enchantments.SHARPNESS, Enchantments.SMITE,
            Enchantments.BANE_OF_ARTHROPODS, Enchantments.LOOTING,
            Enchantments.EFFICIENCY, Enchantments.FORTUNE,
            Enchantments.FIRE_ASPECT, Enchantments.LUNGE
    );

    // Valuable enchantments: +7 flat
    private static final Set<ResourceKey<Enchantment>> VALUABLE_ENCHANTS = Set.of(
            Enchantments.MENDING, Enchantments.SILK_TOUCH
    );

    // Curses: -3 flat
    private static final Set<ResourceKey<Enchantment>> CURSE_ENCHANTS = Set.of(
            Enchantments.VANISHING_CURSE, Enchantments.BINDING_CURSE
    );

    /**
     * Calculate the sell value of an item stack using the global sell rate.
     * Returns 0 if the item cannot be sold.
     */
    public static float calculateValue(ItemStack stack) {
        return calculateValueWithRate(stack, sellRateModifier);
    }

    /**
     * Calculate the sell value with per-player rate (1.0 if T1, 0.7 otherwise).
     */
    public static float calculateValue(ItemStack stack, ServerPlayer player) {
        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        float rate = unlocks.hasTier(UnlockBranch.PROCESSING, 1) ? 1.0f : 0.7f;
        return calculateValueWithRate(stack, rate);
    }

    private static float calculateValueWithRate(ItemStack stack, float rate) {
        if (stack.isEmpty()) return 0;

        Item item = stack.getItem();

        // Relics use a fixed value based on rarity
        if (item == NeptuneItems.RELIC) {
            RelicDefinition def = RelicItem.getDefinition(stack);
            if (def != null) return def.rarity().getSellValue();
            return 0;
        }

        // Check if it's gear (with base value calculation)
        Float gearBase = BASE_VALUES.get(item);
        if (gearBase != null) {
            float value = gearBase;
            value += calculateEnchantmentValue(stack);
            value *= getDurabilityModifier(stack);
            return Math.max(0, value * rate);
        }

        // Check if it's an enchanted book
        if (item == Items.ENCHANTED_BOOK) {
            float enchValue = calculateEnchantmentValue(stack);
            return Math.max(0, enchValue * rate);
        }

        // Check other fixed-value items
        Float otherValue = OTHER_VALUES.get(item);
        if (otherValue != null) {
            return otherValue * stack.getCount() * rate;
        }

        return 0;
    }

    /**
     * Calculate total enchantment value bonus for an item.
     */
    private static float calculateEnchantmentValue(ItemStack stack) {
        ItemEnchantments enchantments = stack.getEnchantments();
        if (enchantments.isEmpty()) return 0;

        float total = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            int level = entry.getIntValue();

            ResourceKey<Enchantment> key = holder.unwrapKey().orElse(null);
            if (key == null) continue;

            if (COMMON_ENCHANTS.contains(key)) {
                total += level * 0.5f;
            } else if (MID_ENCHANTS.contains(key)) {
                total += level * 1.0f;
            } else if (VALUABLE_ENCHANTS.contains(key)) {
                total += 7f;
            } else if (CURSE_ENCHANTS.contains(key)) {
                total -= 3f;
            }
        }
        return total;
    }

    /**
     * Get durability modifier based on remaining durability percentage.
     */
    private static float getDurabilityModifier(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() == 0) return 1.0f;

        float durabilityPercent = 1.0f - ((float) stack.getDamageValue() / stack.getMaxDamage());

        if (durabilityPercent >= 1.0f) return 1.0f;
        if (durabilityPercent >= 0.75f) return 0.5f;
        if (durabilityPercent >= 0.50f) return 0.35f;
        if (durabilityPercent >= 0.25f) return 0.2f;
        return 0.1f;
    }

    /**
     * Calculate raw enchantment value (no sell rate applied). Used for shard extraction.
     */
    public static float calculateRawEnchantmentValue(ItemStack stack) {
        return calculateEnchantmentValue(stack);
    }

    /**
     * Get the value-per-level for an enchantment by its ResourceKey.
     * Returns -1 if the enchantment is unknown/not in any tier.
     */
    public static float getEnchantmentValuePerLevel(ResourceKey<Enchantment> key) {
        if (COMMON_ENCHANTS.contains(key)) return 0.5f;
        if (MID_ENCHANTS.contains(key)) return 1.0f;
        if (VALUABLE_ENCHANTS.contains(key)) return 7.0f;
        if (CURSE_ENCHANTS.contains(key)) return -3.0f;
        return -1f;
    }

    /**
     * Round a float value to the nearest int for display/transaction.
     * Values below 1 are rounded up to 1 (minimum sell value for sellable items).
     */
    public static int roundValue(float value) {
        if (value <= 0) return 0;
        return Math.max(1, Math.round(value));
    }

    /**
     * Set sell rate modifier (called when Processing T1 is unlocked).
     */
    public static void setSellRateModifier(float modifier) {
        sellRateModifier = modifier;
    }

    public static float getSellRateModifier() {
        return sellRateModifier;
    }

    /**
     * Check if an item can be sold.
     */
    public static boolean isSellable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == NeptuneItems.RELIC) return true;
        return BASE_VALUES.containsKey(item) || OTHER_VALUES.containsKey(item) || item == Items.ENCHANTED_BOOK;
    }

    /**
     * Calculate relic sell value. Checks if this is a duplicate in the player's journal.
     * Note: Catalog T1 bonus (+50%) is applied separately by the broker.
     */
    public static float calculateRelicValue(ItemStack stack, boolean isDuplicate) {
        RelicDefinition def = RelicItem.getDefinition(stack);
        if (def == null) return 0;
        if (isDuplicate) {
            return def.rarity().getDuplicateSellValue();
        }
        return def.rarity().getSellValue();
    }
}
