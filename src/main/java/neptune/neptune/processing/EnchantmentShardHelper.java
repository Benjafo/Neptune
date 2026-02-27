package neptune.neptune.processing;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import neptune.neptune.broker.GearValueCalculator;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentShardHelper {

    public record ApplicableEnchantment(Holder<Enchantment> enchantment, int targetLevel, int shardCost) {
        public String getDisplayName() {
            return enchantment.unwrapKey()
                    .map(key -> formatEnchantmentName(key.location().getPath()))
                    .orElse("Unknown");
        }

        private static String formatEnchantmentName(String path) {
            StringBuilder sb = new StringBuilder();
            boolean capitalize = true;
            for (char c : path.toCharArray()) {
                if (c == '_') {
                    sb.append(' ');
                    capitalize = true;
                } else {
                    sb.append(capitalize ? Character.toUpperCase(c) : c);
                    capitalize = false;
                }
            }
            return sb.toString();
        }
    }

    /**
     * Get the number of enchantment shards yielded from extracting an item.
     */
    public static int getExtractionYield(ItemStack stack) {
        float raw = GearValueCalculator.calculateRawEnchantmentValue(stack);
        return Math.max(0, (int) Math.ceil(raw));
    }

    /**
     * Whether an item can have its enchantments extracted.
     */
    public static boolean canExtract(ItemStack stack) {
        return getExtractionYield(stack) > 0;
    }

    /**
     * Get the shard cost per level for an enchantment (2x extraction value).
     * Returns -1 if the enchantment is unknown.
     */
    public static int getCostPerLevel(ResourceKey<Enchantment> key) {
        float valuePerLevel = GearValueCalculator.getEnchantmentValuePerLevel(key);
        if (valuePerLevel < 0) return -1;
        // Common: 0.5 × 2 = 1 per level
        // Mid: 1.0 × 2 = 2 per level
        // Valuable: 7.0 × 2 = 14 flat
        return (int) Math.ceil(valuePerLevel * 2);
    }

    /**
     * Get all applicable enchantments that can be applied to the given gear item.
     */
    public static List<ApplicableEnchantment> getApplicableEnchantments(ItemStack gear, net.minecraft.core.RegistryAccess registryAccess) {
        List<ApplicableEnchantment> result = new ArrayList<>();
        ItemEnchantments existing = gear.getEnchantments();

        var enchantmentRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        enchantmentRegistry.listElements().forEach(holder -> {
            ResourceKey<Enchantment> key = holder.key();
            Enchantment enchantment = holder.value();

            // Skip curses
            if (enchantment.isCurse()) return;

            // Must be applicable to this item
            if (!enchantment.canEnchant(gear)) return;

            // Check tier is known
            int costPerLevel = getCostPerLevel(key);
            if (costPerLevel < 0) return;

            // Get current level
            int currentLevel = existing.getLevel(holder);

            // Must not be at max level
            if (currentLevel >= enchantment.getMaxLevel()) return;

            // Check for conflicts with existing enchantments
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : existing.entrySet()) {
                Holder<Enchantment> existingHolder = entry.getKey();
                if (existingHolder.equals(holder)) continue;
                if (!Enchantment.areCompatible(holder, existingHolder)) return;
            }

            int targetLevel = currentLevel + 1;
            result.add(new ApplicableEnchantment(holder, targetLevel, costPerLevel));
        });

        return result;
    }

    /**
     * Apply an enchantment at the given level to gear, returning the modified stack.
     */
    public static ItemStack applyEnchantment(ItemStack gear, Holder<Enchantment> enchantment, int targetLevel) {
        gear.enchant(enchantment, targetLevel);
        return gear;
    }
}
