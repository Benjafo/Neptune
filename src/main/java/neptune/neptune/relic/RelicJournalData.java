package neptune.neptune.relic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

/**
 * Tracks which relics a player has discovered (permanent journal).
 * Also tracks duplicate counts for infusion and cities-without-legendary for bad luck protection.
 */
public record RelicJournalData(
        Set<String> discoveredIds,
        Map<String, Integer> duplicateCounts,
        int citiesWithoutLegendary
) {
    public static final RelicJournalData EMPTY = new RelicJournalData(Set.of(), Map.of(), 0);

    public static final Codec<RelicJournalData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf()
                            .xmap(l -> (Set<String>) new HashSet<>(l), s -> new ArrayList<>(s))
                            .fieldOf("discoveredIds")
                            .forGetter(RelicJournalData::discoveredIds),
                    Codec.unboundedMap(Codec.STRING, Codec.INT)
                            .fieldOf("duplicateCounts")
                            .forGetter(d -> new HashMap<>(d.duplicateCounts)),
                    Codec.INT.fieldOf("citiesWithoutLegendary")
                            .forGetter(RelicJournalData::citiesWithoutLegendary)
            ).apply(instance, RelicJournalData::new)
    );

    public static final StreamCodec<ByteBuf, RelicJournalData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            d -> new ArrayList<>(d.discoveredIds),
            ByteBufCodecs.INT,
            RelicJournalData::citiesWithoutLegendary,
            (list, cities) -> new RelicJournalData(new HashSet<>(list), Map.of(), cities)
    );

    public boolean hasDiscovered(String relicId) {
        return discoveredIds.contains(relicId);
    }

    public int getDiscoveredCount() {
        return discoveredIds.size();
    }

    public int getDuplicateCount(String relicId) {
        return duplicateCounts.getOrDefault(relicId, 0);
    }

    public RelicJournalData withDiscovery(String relicId) {
        if (discoveredIds.contains(relicId)) {
            // Already discovered — increment duplicate count
            Map<String, Integer> newDupes = new HashMap<>(duplicateCounts);
            newDupes.merge(relicId, 1, Integer::sum);
            return new RelicJournalData(discoveredIds, Map.copyOf(newDupes), citiesWithoutLegendary);
        }
        Set<String> newIds = new HashSet<>(discoveredIds);
        newIds.add(relicId);
        return new RelicJournalData(Set.copyOf(newIds), duplicateCounts, citiesWithoutLegendary);
    }

    public RelicJournalData withCityVisited(boolean hadLegendary) {
        if (hadLegendary) {
            return new RelicJournalData(discoveredIds, duplicateCounts, 0);
        }
        return new RelicJournalData(discoveredIds, duplicateCounts, citiesWithoutLegendary + 1);
    }

    public int getCompletedMinorSets() {
        int count = 0;
        for (RelicSet set : RelicSet.values()) {
            if (set.isMinor() && isSetComplete(set)) count++;
        }
        return count;
    }

    public int getCompletedMajorSets() {
        int count = 0;
        for (RelicSet set : RelicSet.values()) {
            if (set.isMajor() && isSetComplete(set)) count++;
        }
        return count;
    }

    public int getCompletedSetsTotal() {
        return getCompletedMinorSets() + getCompletedMajorSets();
    }

    public boolean isSetComplete(RelicSet set) {
        List<RelicDefinition> setRelics = RelicDefinition.getBySet(set);
        for (RelicDefinition relic : setRelics) {
            if (!discoveredIds.contains(relic.id())) return false;
        }
        return !setRelics.isEmpty();
    }

    public int getSetProgress(RelicSet set) {
        int count = 0;
        for (RelicDefinition relic : RelicDefinition.getBySet(set)) {
            if (discoveredIds.contains(relic.id())) count++;
        }
        return count;
    }

    public boolean hasAnyLegendary() {
        return discoveredIds.stream()
                .map(RelicDefinition::get)
                .filter(Objects::nonNull)
                .anyMatch(r -> r.rarity() == RelicRarity.LEGENDARY);
    }

    public int getLegendaryCount() {
        return (int) discoveredIds.stream()
                .map(RelicDefinition::get)
                .filter(Objects::nonNull)
                .filter(r -> r.rarity() == RelicRarity.LEGENDARY)
                .count();
    }
}
