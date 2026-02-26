package neptune.neptune.relic;

import neptune.neptune.challenge.ChallengeTracker;
import neptune.neptune.data.NeptuneAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Handles relic discovery when a relic item ticks in a player's inventory.
 * Uses a flag in the item's CUSTOM_DATA to avoid re-triggering.
 */
public class RelicPickupHandler {

    /**
     * Called from RelicItem.inventoryTick on the server side.
     * Registers the relic to the player's journal if not already registered for this stack.
     */
    public static void onRelicTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof ServerPlayer player)) return;

        String relicId = RelicItem.getRelicId(stack);
        if (relicId == null) return;

        // Check if this specific stack has already been registered
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null) {
            var tag = customData.copyTag();
            if (tag.getBooleanOr("registered", false)) return;
        }

        // Mark as registered on this stack
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putBoolean("registered", true)
        );

        RelicDefinition def = RelicDefinition.get(relicId);
        if (def == null) return;

        RelicJournalData journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
        boolean isNew = !journal.hasDiscovered(relicId);

        player.setAttached(NeptuneAttachments.RELIC_JOURNAL, journal.withDiscovery(relicId));

        if (isNew) {
            player.sendSystemMessage(Component.literal(
                    def.rarity().getColorCode() + "✦ New relic discovered: " + def.displayName() + "!"));
            if (def.set() != RelicSet.STANDALONE) {
                RelicJournalData updated = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
                int progress = updated.getSetProgress(def.set());
                int total = def.set().getSize();
                player.sendSystemMessage(Component.literal(
                        "§7  Set: " + def.set().getDisplayName() + " (" + progress + "/" + total + ")"));
            }

            // Track challenges
            RelicJournalData updated = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
            ChallengeTracker.onRelicCollected(player, updated.getDiscoveredCount());
            if (def.rarity() == RelicRarity.LEGENDARY) {
                ChallengeTracker.onLegendaryFound(player, updated.getLegendaryCount());
            }
        } else {
            player.sendSystemMessage(Component.literal(
                    "§7Duplicate relic: " + def.displayName() + " (can sell or save for infusion)"));
        }
    }
}
