package neptune.neptune.broker;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Manages temporary enchantment tome buffs on items.
 * Tomes add +1 enchantment level for 1 hour (72000 ticks).
 * Stacking extends duration, doesn't increase level.
 */
public class TomeBuffManager {

    private static final long BUFF_DURATION = 72000L; // 1 hour in ticks
    private static final int CHECK_INTERVAL = 100; // Check every 5 seconds
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter < CHECK_INTERVAL) return;
            tickCounter = 0;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                checkExpiredBuffs(player);
            }
        });
    }

    /**
     * Apply an efficiency tome to the player's held item.
     * Returns true if successful.
     */
    public static boolean applyEfficiencyTome(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cHold a tool in your main hand!"));
            return false;
        }

        // Check if item can have efficiency (tools, shears, etc.)
        var registry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> effHolder = registry.get(Enchantments.EFFICIENCY).orElse(null);
        if (effHolder == null) return false;

        return applyTomeBuff(player, held, effHolder, "efficiency");
    }

    /**
     * Apply a protection tome to the player's held armor piece.
     * Returns true if successful.
     */
    public static boolean applyProtectionTome(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cHold an armor piece in your main hand!"));
            return false;
        }

        var registry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> protHolder = registry.get(Enchantments.PROTECTION).orElse(null);
        if (protHolder == null) return false;

        return applyTomeBuff(player, held, protHolder, "protection");
    }

    private static boolean applyTomeBuff(ServerPlayer player, ItemStack stack, Holder<Enchantment> enchantment, String tomeType) {
        long gameTime = player.level().getGameTime();
        String nbtKey = "neptune_tome_" + tomeType;

        // Check for existing buff of the same type
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains(nbtKey)) {
            // Extend duration
            CompoundTag buffTag = tag.getCompoundOrEmpty(nbtKey);
            long expiresAt = buffTag.getLongOr("expiresAt", 0L);
            buffTag.putLong("expiresAt", expiresAt + BUFF_DURATION);
            tag.put(nbtKey, buffTag);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            player.sendSystemMessage(Component.literal("§aTome buff extended! " + tomeType + " duration increased."));
            return true;
        }

        // New buff: add +1 enchantment level
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        int currentLevel = enchantments.getLevel(enchantment);
        int newLevel = currentLevel + 1;

        // Apply the enchantment
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
        mutable.set(enchantment, newLevel);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());

        // Store buff metadata in custom data
        CompoundTag buffTag = new CompoundTag();
        buffTag.putString("type", tomeType);
        buffTag.putInt("addedLevels", 1);
        buffTag.putLong("expiresAt", gameTime + BUFF_DURATION);
        tag.put(nbtKey, buffTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        player.sendSystemMessage(Component.literal("§a+" + newLevel + " " + tomeType + " applied for 1 hour!"));
        return true;
    }

    private static void checkExpiredBuffs(ServerPlayer player) {
        long gameTime = player.level().getGameTime();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null || customData.isEmpty()) continue;

            CompoundTag tag = customData.copyTag();
            boolean changed = false;

            changed |= checkAndRemoveBuff(player, stack, tag, "efficiency", Enchantments.EFFICIENCY, gameTime);
            changed |= checkAndRemoveBuff(player, stack, tag, "protection", Enchantments.PROTECTION, gameTime);

            if (changed) {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
    }

    private static boolean checkAndRemoveBuff(ServerPlayer player, ItemStack stack, CompoundTag tag,
                                               String tomeType, ResourceKey<Enchantment> enchantmentKey, long gameTime) {
        String nbtKey = "neptune_tome_" + tomeType;
        if (!tag.contains(nbtKey)) return false;

        CompoundTag buffTag = tag.getCompoundOrEmpty(nbtKey);
        if (buffTag.isEmpty()) return false;

        long expiresAt = buffTag.getLongOr("expiresAt", 0L);
        if (gameTime < expiresAt) return false;

        // Buff expired — remove enchantment levels
        int addedLevels = buffTag.getIntOr("addedLevels", 1);

        var registry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> holder = registry.get(enchantmentKey).orElse(null);
        if (holder != null) {
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            int currentLevel = enchantments.getLevel(holder);
            int newLevel = Math.max(0, currentLevel - addedLevels);

            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
            if (newLevel > 0) {
                mutable.set(holder, newLevel);
            } else {
                mutable.set(holder, 0);
            }
            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        tag.remove(nbtKey);
        player.sendSystemMessage(Component.literal("§eTome buff expired: " + tomeType + " has worn off."));
        return true;
    }
}
