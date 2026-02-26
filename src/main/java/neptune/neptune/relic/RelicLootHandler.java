package neptune.neptune.relic;

import neptune.neptune.data.NeptuneAttachments;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Injects relics into end city treasure chest loot.
 * ~64% of end cities contain a relic, max one per structure.
 */
public class RelicLootHandler {

    private static final float RELIC_DROP_CHANCE = 0.64f;
    private static final int BAD_LUCK_THRESHOLD = 150;
    private static final Random RANDOM = new Random();

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
            // Only modify end city treasure chests
            if (!entry.is(BuiltInLootTables.END_CITY_TREASURE)) return;

            ServerLevel level = context.getLevel();

            // Get chest position
            Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
            if (origin == null) return;
            BlockPos pos = BlockPos.containing(origin);

            // Resolve end city structure from registry
            var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            var structureHolder = registry.get(BuiltinStructures.END_CITY);
            if (structureHolder.isEmpty()) return;
            Structure structure = structureHolder.get().value();

            // Check if this chest is inside an end city
            StructureStart start = level.structureManager().getStructureWithPieceAt(pos, structure);
            if (!start.isValid()) return;

            // Get unique key for this structure
            long packedPos = start.getChunkPos().toLong();

            // Check if this structure has already been rolled
            RelicWorldData worldData = RelicWorldData.get(level);
            if (worldData.hasBeenRolled(packedPos)) return;

            // Mark as rolled (even if the 64% roll fails, no second chances)
            worldData.markRolled(packedPos);

            // Get player for bad luck protection / journal preference
            Entity thisEntity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
            ServerPlayer player = thisEntity instanceof ServerPlayer sp ? sp : null;

            // Update cities-without-legendary counter before rolling
            // (will be reset below if legendary drops)
            RelicJournalData journal = null;
            if (player != null) {
                journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
            }

            // Check for forced relic tier from broker hint
            boolean forcedTier = journal != null && journal.hasForcedTier();

            if (forcedTier) {
                // Forced tier: 100% drop chance, use forced rarity, then clear the flag
            } else {
                // Normal: 64% chance to drop a relic
                if (RANDOM.nextFloat() >= RELIC_DROP_CHANCE) {
                    // No relic this city — still counts toward bad luck protection
                    if (player != null && journal != null) {
                        player.setAttached(NeptuneAttachments.RELIC_JOURNAL, journal.withCityVisited(false));
                    }
                    return;
                }
            }

            // Pick rarity
            RelicRarity rarity = forcedTier ? journal.getForcedRarity() : rollRarity(journal);

            // Pick a relic of that rarity, preferring undiscovered ones
            RelicDefinition def = pickRelic(rarity, journal);
            if (def == null) return;

            // Create and add the relic to drops
            ItemStack relicStack = RelicItem.createStack(NeptuneItems.RELIC, def);
            drops.add(relicStack);

            // Update journal counter and consume forced tier if active
            if (player != null && journal != null) {
                boolean isLegendary = rarity == RelicRarity.LEGENDARY;
                RelicJournalData updated = journal.withCityVisited(isLegendary);
                if (forcedTier) {
                    updated = updated.withForcedTierConsumed();
                }
                player.setAttached(NeptuneAttachments.RELIC_JOURNAL, updated);
            }
        });
    }

    private static RelicRarity rollRarity(RelicJournalData journal) {
        // Bad luck protection: guarantee legendary after 150 cities without one
        if (journal != null && journal.citiesWithoutLegendary() >= BAD_LUCK_THRESHOLD) {
            return RelicRarity.LEGENDARY;
        }

        float roll = RANDOM.nextFloat();
        if (roll < 0.516f) return RelicRarity.COMMON;
        if (roll < 0.797f) return RelicRarity.UNCOMMON;
        if (roll < 0.922f) return RelicRarity.RARE;
        if (roll < 0.984f) return RelicRarity.VERY_RARE;
        return RelicRarity.LEGENDARY;
    }

    private static RelicDefinition pickRelic(RelicRarity rarity, RelicJournalData journal) {
        List<RelicDefinition> candidates = RelicDefinition.getByRarity(rarity);
        if (candidates.isEmpty()) return null;

        // Prefer undiscovered relics if player journal is available
        if (journal != null) {
            List<RelicDefinition> undiscovered = candidates.stream()
                    .filter(r -> !journal.hasDiscovered(r.id()))
                    .collect(Collectors.toList());
            if (!undiscovered.isEmpty()) {
                return undiscovered.get(RANDOM.nextInt(undiscovered.size()));
            }
        }

        // Fall back to any relic of this rarity
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }
}
