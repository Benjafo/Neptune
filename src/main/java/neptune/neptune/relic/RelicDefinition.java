package neptune.neptune.relic;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Defines all 120 relics in the game.
 * Each relic has a unique ID, display name, rarity, set affiliation, and lore.
 * Names/lore are placeholders — to be finalized in Phase 9 polish.
 */
public record RelicDefinition(
        String id,
        String displayName,
        RelicRarity rarity,
        RelicSet set,
        String lore
) {
    private static final Map<String, RelicDefinition> ALL_RELICS = new LinkedHashMap<>();
    private static final Map<RelicSet, List<RelicDefinition>> BY_SET = new EnumMap<>(RelicSet.class);

    static {
        // Major Set: The Builders (15) — 6C, 5U, 3R, 1VR
        reg("builders_chisel", "Builder's Chisel", RelicRarity.COMMON, RelicSet.THE_BUILDERS);
        reg("builders_compass", "Builder's Compass", RelicRarity.COMMON, RelicSet.THE_BUILDERS);
        reg("builders_plumb", "Endstone Plumb Bob", RelicRarity.COMMON, RelicSet.THE_BUILDERS);
        reg("builders_level", "Void Level", RelicRarity.COMMON, RelicSet.THE_BUILDERS);
        reg("builders_trowel", "Purpur Trowel", RelicRarity.COMMON, RelicSet.THE_BUILDERS);
        reg("builders_blueprint", "Faded Blueprint", RelicRarity.COMMON, RelicSet.THE_BUILDERS);
        reg("builders_mortar", "Endite Mortar", RelicRarity.UNCOMMON, RelicSet.THE_BUILDERS);
        reg("builders_hammer", "Resonant Hammer", RelicRarity.UNCOMMON, RelicSet.THE_BUILDERS);
        reg("builders_ruler", "Dimensional Ruler", RelicRarity.UNCOMMON, RelicSet.THE_BUILDERS);
        reg("builders_lens", "Architect's Lens", RelicRarity.UNCOMMON, RelicSet.THE_BUILDERS);
        reg("builders_seal", "Master Builder's Seal", RelicRarity.UNCOMMON, RelicSet.THE_BUILDERS);
        reg("builders_keystone", "City Keystone", RelicRarity.RARE, RelicSet.THE_BUILDERS);
        reg("builders_crown", "Builder's Crown", RelicRarity.RARE, RelicSet.THE_BUILDERS);
        reg("builders_tablet", "Foundation Tablet", RelicRarity.RARE, RelicSet.THE_BUILDERS);
        reg("builders_heart", "Heart of the City", RelicRarity.VERY_RARE, RelicSet.THE_BUILDERS);

        // Major Set: The Void (15) — 6C, 5U, 3R, 1VR
        reg("void_shard", "Void Shard", RelicRarity.COMMON, RelicSet.THE_VOID);
        reg("void_dust", "Crystallized Void Dust", RelicRarity.COMMON, RelicSet.THE_VOID);
        reg("void_pearl", "Dark Pearl", RelicRarity.COMMON, RelicSet.THE_VOID);
        reg("void_feather", "Weightless Feather", RelicRarity.COMMON, RelicSet.THE_VOID);
        reg("void_glass", "Void Glass", RelicRarity.COMMON, RelicSet.THE_VOID);
        reg("void_thread", "Ender Thread", RelicRarity.COMMON, RelicSet.THE_VOID);
        reg("void_prism", "Null Prism", RelicRarity.UNCOMMON, RelicSet.THE_VOID);
        reg("void_mirror", "Reflection of Nothing", RelicRarity.UNCOMMON, RelicSet.THE_VOID);
        reg("void_bell", "Silent Bell", RelicRarity.UNCOMMON, RelicSet.THE_VOID);
        reg("void_flame", "Frozen Flame", RelicRarity.UNCOMMON, RelicSet.THE_VOID);
        reg("void_eye", "Eye of the Void", RelicRarity.UNCOMMON, RelicSet.THE_VOID);
        reg("void_tear", "Dimensional Tear", RelicRarity.RARE, RelicSet.THE_VOID);
        reg("void_orb", "Condensed Void Orb", RelicRarity.RARE, RelicSet.THE_VOID);
        reg("void_star", "End Star", RelicRarity.RARE, RelicSet.THE_VOID);
        reg("void_core", "Core of the End", RelicRarity.VERY_RARE, RelicSet.THE_VOID);

        // Major Set: The Inhabitants (15) — 6C, 5U, 3R, 1VR
        reg("inhab_scale", "Enderman Scale", RelicRarity.COMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_tooth", "Shulker Tooth", RelicRarity.COMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_claw", "Dragon Claw Fragment", RelicRarity.COMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_silk", "Endermite Silk", RelicRarity.COMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_shell", "Ancient Shulker Shell", RelicRarity.COMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_egg", "Petrified Endermite Egg", RelicRarity.COMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_horn", "Enderman Horn", RelicRarity.UNCOMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_eye", "Shulker Eye", RelicRarity.UNCOMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_bone", "Dragon Bone", RelicRarity.UNCOMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_heart", "Enderman Heart", RelicRarity.UNCOMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_crown", "Shulker Crown", RelicRarity.UNCOMMON, RelicSet.THE_INHABITANTS);
        reg("inhab_fang", "Elder Dragon Fang", RelicRarity.RARE, RelicSet.THE_INHABITANTS);
        reg("inhab_idol", "Enderman Idol", RelicRarity.RARE, RelicSet.THE_INHABITANTS);
        reg("inhab_gem", "Shulker Gem", RelicRarity.RARE, RelicSet.THE_INHABITANTS);
        reg("inhab_soul", "Soul of the Ender", RelicRarity.VERY_RARE, RelicSet.THE_INHABITANTS);

        // Major Set: The Explorers (15) — 6C, 5U, 3R, 1VR
        reg("expl_journal", "Tattered Journal", RelicRarity.COMMON, RelicSet.THE_EXPLORERS);
        reg("expl_coin", "Strange Coin", RelicRarity.COMMON, RelicSet.THE_EXPLORERS);
        reg("expl_button", "Worn Button", RelicRarity.COMMON, RelicSet.THE_EXPLORERS);
        reg("expl_ring", "Faded Ring", RelicRarity.COMMON, RelicSet.THE_EXPLORERS);
        reg("expl_map", "Crumbled Map Fragment", RelicRarity.COMMON, RelicSet.THE_EXPLORERS);
        reg("expl_quill", "Dried Quill", RelicRarity.COMMON, RelicSet.THE_EXPLORERS);
        reg("expl_pendant", "Explorer's Pendant", RelicRarity.UNCOMMON, RelicSet.THE_EXPLORERS);
        reg("expl_spyglass", "Cracked Spyglass", RelicRarity.UNCOMMON, RelicSet.THE_EXPLORERS);
        reg("expl_boots", "Worn Expedition Boots", RelicRarity.UNCOMMON, RelicSet.THE_EXPLORERS);
        reg("expl_lantern", "Extinguished Lantern", RelicRarity.UNCOMMON, RelicSet.THE_EXPLORERS);
        reg("expl_sextant", "Broken Sextant", RelicRarity.UNCOMMON, RelicSet.THE_EXPLORERS);
        reg("expl_sword", "Shattered Sword", RelicRarity.RARE, RelicSet.THE_EXPLORERS);
        reg("expl_flag", "Expedition Banner", RelicRarity.RARE, RelicSet.THE_EXPLORERS);
        reg("expl_logbook", "Captain's Logbook", RelicRarity.RARE, RelicSet.THE_EXPLORERS);
        reg("expl_compass", "The Final Compass", RelicRarity.VERY_RARE, RelicSet.THE_EXPLORERS);

        // Minor Set: Void Constellations (6) — 3C, 2U, 1R
        reg("const_orion", "Void Orion Chart", RelicRarity.COMMON, RelicSet.VOID_CONSTELLATIONS);
        reg("const_serpent", "Serpent Star Map", RelicRarity.COMMON, RelicSet.VOID_CONSTELLATIONS);
        reg("const_crown", "Crown Constellation", RelicRarity.COMMON, RelicSet.VOID_CONSTELLATIONS);
        reg("const_eye", "The Watching Eye", RelicRarity.UNCOMMON, RelicSet.VOID_CONSTELLATIONS);
        reg("const_gate", "Gateway Pattern", RelicRarity.UNCOMMON, RelicSet.VOID_CONSTELLATIONS);
        reg("const_void", "The Void Between Stars", RelicRarity.RARE, RelicSet.VOID_CONSTELLATIONS);

        // Minor Set: Shulker Origins (4) — 2C, 1U, 1R
        reg("shulk_fossil", "Shulker Fossil", RelicRarity.COMMON, RelicSet.SHULKER_ORIGINS);
        reg("shulk_cocoon", "Ancient Cocoon", RelicRarity.COMMON, RelicSet.SHULKER_ORIGINS);
        reg("shulk_crystal", "Evolution Crystal", RelicRarity.UNCOMMON, RelicSet.SHULKER_ORIGINS);
        reg("shulk_record", "Origin Record", RelicRarity.RARE, RelicSet.SHULKER_ORIGINS);

        // Minor Set: Dragon's Legacy (5) — 2C, 2U, 1VR
        reg("dragon_scale", "Shed Dragon Scale", RelicRarity.COMMON, RelicSet.DRAGONS_LEGACY);
        reg("dragon_ash", "Dragon Fire Ash", RelicRarity.COMMON, RelicSet.DRAGONS_LEGACY);
        reg("dragon_talon", "Ancient Talon", RelicRarity.UNCOMMON, RelicSet.DRAGONS_LEGACY);
        reg("dragon_eye", "Dragon's Eye Gem", RelicRarity.UNCOMMON, RelicSet.DRAGONS_LEGACY);
        reg("dragon_egg_shard", "Egg Shell Fragment", RelicRarity.VERY_RARE, RelicSet.DRAGONS_LEGACY);

        // Minor Set: Chorus Mutations (4) — 2C, 1U, 1R
        reg("chorus_bloom", "Eternal Bloom", RelicRarity.COMMON, RelicSet.CHORUS_MUTATIONS);
        reg("chorus_root", "Twisted Root", RelicRarity.COMMON, RelicSet.CHORUS_MUTATIONS);
        reg("chorus_spore", "Glowing Spore", RelicRarity.UNCOMMON, RelicSet.CHORUS_MUTATIONS);
        reg("chorus_heart", "Chorus Heart", RelicRarity.RARE, RelicSet.CHORUS_MUTATIONS);

        // Minor Set: End City Seals (6) — 2C, 2U, 1R, 1VR
        reg("seal_trade", "Seal of Trade", RelicRarity.COMMON, RelicSet.END_CITY_SEALS);
        reg("seal_guard", "Seal of the Guard", RelicRarity.COMMON, RelicSet.END_CITY_SEALS);
        reg("seal_archive", "Seal of the Archive", RelicRarity.UNCOMMON, RelicSet.END_CITY_SEALS);
        reg("seal_council", "Seal of the Council", RelicRarity.UNCOMMON, RelicSet.END_CITY_SEALS);
        reg("seal_king", "Seal of the King", RelicRarity.RARE, RelicSet.END_CITY_SEALS);
        reg("seal_void", "Seal of the Void", RelicRarity.VERY_RARE, RelicSet.END_CITY_SEALS);

        // Minor Set: Ritual Objects (5) — 2C, 2U, 1R
        reg("ritual_candle", "Void Candle", RelicRarity.COMMON, RelicSet.RITUAL_OBJECTS);
        reg("ritual_incense", "End Incense", RelicRarity.COMMON, RelicSet.RITUAL_OBJECTS);
        reg("ritual_chalice", "Ritual Chalice", RelicRarity.UNCOMMON, RelicSet.RITUAL_OBJECTS);
        reg("ritual_dagger", "Ceremonial Dagger", RelicRarity.UNCOMMON, RelicSet.RITUAL_OBJECTS);
        reg("ritual_mask", "Ritual Mask", RelicRarity.RARE, RelicSet.RITUAL_OBJECTS);

        // Minor Set: Cartographer's Tools (5) — 2C, 2U, 1R
        reg("carto_divider", "Ancient Dividers", RelicRarity.COMMON, RelicSet.CARTOGRAPHERS_TOOLS);
        reg("carto_ink", "Ender Ink", RelicRarity.COMMON, RelicSet.CARTOGRAPHERS_TOOLS);
        reg("carto_astrolabe", "Void Astrolabe", RelicRarity.UNCOMMON, RelicSet.CARTOGRAPHERS_TOOLS);
        reg("carto_scope", "Distance Scope", RelicRarity.UNCOMMON, RelicSet.CARTOGRAPHERS_TOOLS);
        reg("carto_atlas", "Grand Atlas", RelicRarity.RARE, RelicSet.CARTOGRAPHERS_TOOLS);

        // Minor Set: Warrior's Remnants (5) — 1C, 2U, 1R, 1VR
        reg("warrior_badge", "Warrior's Badge", RelicRarity.COMMON, RelicSet.WARRIORS_REMNANTS);
        reg("warrior_gauntlet", "Ender Gauntlet", RelicRarity.UNCOMMON, RelicSet.WARRIORS_REMNANTS);
        reg("warrior_shield", "Void Shield Fragment", RelicRarity.UNCOMMON, RelicSet.WARRIORS_REMNANTS);
        reg("warrior_helm", "Commander's Helm", RelicRarity.RARE, RelicSet.WARRIORS_REMNANTS);
        reg("warrior_blade", "Legendary Blade Shard", RelicRarity.VERY_RARE, RelicSet.WARRIORS_REMNANTS);

        // Standalone Relics (20) — 7R, 8VR, 5L
        reg("stand_anomaly", "Spatial Anomaly", RelicRarity.RARE, RelicSet.STANDALONE);
        reg("stand_echo", "Echo of Creation", RelicRarity.RARE, RelicSet.STANDALONE);
        reg("stand_paradox", "Temporal Paradox", RelicRarity.RARE, RelicSet.STANDALONE);
        reg("stand_nexus", "Nexus Fragment", RelicRarity.RARE, RelicSet.STANDALONE);
        reg("stand_cipher", "Void Cipher", RelicRarity.RARE, RelicSet.STANDALONE);
        reg("stand_rune", "Primordial Rune", RelicRarity.RARE, RelicSet.STANDALONE);
        reg("stand_seed", "World Seed Crystal", RelicRarity.RARE, RelicSet.STANDALONE);
        reg("stand_mirror", "Mirror of Infinity", RelicRarity.VERY_RARE, RelicSet.STANDALONE);
        reg("stand_hourglass", "Frozen Hourglass", RelicRarity.VERY_RARE, RelicSet.STANDALONE);
        reg("stand_compass", "Compass of Souls", RelicRarity.VERY_RARE, RelicSet.STANDALONE);
        reg("stand_chalice", "Chalice of Endings", RelicRarity.VERY_RARE, RelicSet.STANDALONE);
        reg("stand_crown", "Crown of the Forgotten", RelicRarity.VERY_RARE, RelicSet.STANDALONE);
        reg("stand_tome", "Tome of Void Whispers", RelicRarity.VERY_RARE, RelicSet.STANDALONE);
        reg("stand_orb", "Orb of Dimensions", RelicRarity.VERY_RARE, RelicSet.STANDALONE);
        reg("stand_key", "Key to Nowhere", RelicRarity.VERY_RARE, RelicSet.STANDALONE);
        reg("legend_heart", "Heart of the End", RelicRarity.LEGENDARY, RelicSet.STANDALONE);
        reg("legend_eye", "The All-Seeing Eye", RelicRarity.LEGENDARY, RelicSet.STANDALONE);
        reg("legend_star", "Dying Star", RelicRarity.LEGENDARY, RelicSet.STANDALONE);
        reg("legend_void", "Fragment of the Void", RelicRarity.LEGENDARY, RelicSet.STANDALONE);
        reg("legend_origin", "The Origin Stone", RelicRarity.LEGENDARY, RelicSet.STANDALONE);
    }

    private static void reg(String id, String name, RelicRarity rarity, RelicSet set) {
        RelicDefinition def = new RelicDefinition(id, name, rarity, set, "");
        ALL_RELICS.put(id, def);
        BY_SET.computeIfAbsent(set, k -> new ArrayList<>()).add(def);
    }

    public static RelicDefinition get(String id) {
        return ALL_RELICS.get(id);
    }

    public static Collection<RelicDefinition> getAll() {
        return ALL_RELICS.values();
    }

    public static List<RelicDefinition> getBySet(RelicSet set) {
        return BY_SET.getOrDefault(set, List.of());
    }

    public static List<RelicDefinition> getByRarity(RelicRarity rarity) {
        return ALL_RELICS.values().stream()
                .filter(r -> r.rarity == rarity)
                .collect(Collectors.toList());
    }

    public static int totalCount() {
        return ALL_RELICS.size();
    }
}
