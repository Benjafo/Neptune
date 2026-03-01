package neptune.neptune.network;

import neptune.neptune.broker.BrokerStock;
import neptune.neptune.broker.RotatingStock;
import neptune.neptune.broker.StructureFinder;
import neptune.neptune.broker.TomeBuffManager;
import neptune.neptune.data.*;
import neptune.neptune.map.EndMapData;
import neptune.neptune.map.MapCollectionData;
import neptune.neptune.processing.*;
import neptune.neptune.relic.*;
import neptune.neptune.unlock.UnlockData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import neptune.neptune.Neptune;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class NeptuneNetworking {

    public static void register() {
        // Register C2S packets
        PayloadTypeRegistry.playC2S().register(BrokerPurchasePayload.TYPE, BrokerPurchasePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BreakdownActionPayload.TYPE, BreakdownActionPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ShardInfuserSetGearPayload.TYPE, ShardInfuserSetGearPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ShardInfuserRetrievePayload.TYPE, ShardInfuserRetrievePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ShardApplyPayload.TYPE, ShardApplyPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RelicInfusePayload.TYPE, RelicInfusePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(WaypointTeleportPayload.TYPE, WaypointTeleportPayload.STREAM_CODEC);

        // Register S2C packets
        PayloadTypeRegistry.playS2C().register(MapSyncPayload.TYPE, MapSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ShardInfuserSyncPayload.TYPE, ShardInfuserSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(RelicInfuserSyncPayload.TYPE, RelicInfuserSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(WaypointSyncPayload.TYPE, WaypointSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(RotatingStockSyncPayload.TYPE, RotatingStockSyncPayload.STREAM_CODEC);

        // Sync map data, validate block placements, apply infusion on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
            if (maps.getLoadedMap() != null) {
                syncMapToClient(player);
            }
            validateBlockPlacements(player);
            applyInfusionOnJoin(player);
            validateWaypoints(player);
        });

        // Handle breakdown table actions
        ServerPlayNetworking.registerGlobalReceiver(BreakdownActionPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!(player.containerMenu instanceof BreakdownTableMenu menu)) return;

            switch (payload.action()) {
                case "sell" -> menu.handleSell(player, payload.slotIndex());
                case "extract" -> menu.handleExtract(player, payload.slotIndex());
                case "craft" -> menu.handleCraft(player, payload.slotIndex());
            }
        });

        // Handle shard infuser set gear
        ServerPlayNetworking.registerGlobalReceiver(ShardInfuserSetGearPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!(player.containerMenu instanceof ShardInfuserMenu menu)) return;
            if (!menu.getPos().equals(payload.pos())) return;
            menu.handleSetGear(player, payload.inventorySlot());
        });

        // Handle shard infuser retrieve gear
        ServerPlayNetworking.registerGlobalReceiver(ShardInfuserRetrievePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!(player.containerMenu instanceof ShardInfuserMenu menu)) return;
            if (!menu.getPos().equals(payload.pos())) return;
            menu.handleRetrieveGear(player);
        });

        // Handle shard apply
        ServerPlayNetworking.registerGlobalReceiver(ShardApplyPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!(player.containerMenu instanceof ShardInfuserMenu menu)) return;
            if (!menu.getPos().equals(payload.pos())) return;
            menu.handleApplyEnchantment(player, payload.enchantIndex());
        });

        // Handle relic infuse
        ServerPlayNetworking.registerGlobalReceiver(RelicInfusePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!(player.containerMenu instanceof RelicInfuserMenu menu)) return;
            if (!menu.getPos().equals(payload.pos())) return;
            menu.handleInfuse(player, payload.relicId(), payload.buffChoice());
            // Re-sync infusable relics after infusion
            syncRelicInfuserToClient(player);
        });

        // Handle waypoint teleport
        ServerPlayNetworking.registerGlobalReceiver(WaypointTeleportPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!(player.containerMenu instanceof WaypointMenu menu)) return;
            if (!menu.getPos().equals(payload.fromPos())) return;
            menu.handleTeleport(player, payload.waypointIndex());
        });

        // Handle broker purchase
        ServerPlayNetworking.registerGlobalReceiver(BrokerPurchasePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            String itemId = payload.itemId();

            // Check if this is a rotating stock item
            RotatingStock.RotatingEntry rotEntry = RotatingStock.getById(itemId);
            if (rotEntry != null) {
                handleRotatingPurchase(player, rotEntry);
                return;
            }

            BrokerStock.StockEntry entry = BrokerStock.getById(itemId);
            if (entry == null) return;

            // Check unlock requirement
            if (entry.requiredUnlock() != null) {
                UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
                if (!unlocks.hasUnlock(entry.requiredUnlock())) {
                    player.sendSystemMessage(Component.literal("§cYou haven't unlocked this item yet!"));
                    return;
                }
            }

            // Calculate cost with Explorers discount
            int cost = entry.cost();
            if (RelicSetBonus.hasExplorersBonus(player)) {
                cost = RelicSetBonus.applyExplorersDiscount(cost);
            }

            VoidEssenceData data = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
            VoidEssenceData afterSpend = data.spend(cost);
            if (afterSpend == null) {
                player.sendSystemMessage(Component.literal("§cNot enough void essence! Need " + cost));
                return;
            }

            // Handle each item type
            switch (itemId) {
                case "basic_repair" -> {
                    ItemStack held = player.getMainHandItem();
                    if (held.isEmpty() || !held.isDamageableItem()) {
                        player.sendSystemMessage(Component.literal("§cHold a damageable item in your main hand!"));
                        return;
                    }
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    int maxDamage = held.getMaxDamage();
                    int currentDamage = held.getDamageValue();
                    int repair = maxDamage / 2;
                    held.setDamageValue(Math.max(0, currentDamage - repair));
                    player.sendSystemMessage(Component.literal("§aRepaired " + held.getHoverName().getString() + "!"));
                }
                case "adv_repair" -> {
                    ItemStack held = player.getMainHandItem();
                    if (held.isEmpty() || !held.isDamageableItem()) {
                        player.sendSystemMessage(Component.literal("§cHold a damageable item in your main hand!"));
                        return;
                    }
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    held.setDamageValue(0);
                    player.sendSystemMessage(Component.literal("§aFully repaired " + held.getHoverName().getString() + "!"));
                }
                case "recall_pearl" -> {
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    player.teleportTo(0.5, 64, 0.5);
                    player.sendSystemMessage(Component.literal("§aTeleported to main End island!"));
                }
                case "city_map" -> {
                    MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                    EndMapData loadedMap = maps.getLoadedMap();
                    if (loadedMap == null) {
                        player.sendSystemMessage(Component.literal("§cLoad a map first with /endmap load!"));
                        return;
                    }
                    int radius = StructureFinder.getSearchRadius("city_map");
                    BlockPos cityPos = StructureFinder.findNearestCity(player, radius);
                    if (cityPos == null) {
                        player.sendSystemMessage(Component.literal("§cNo End City found within range!"));
                        return;
                    }
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    String gridCoords = StructureFinder.toGridString(player, cityPos);
                    player.sendSystemMessage(Component.literal("§aEnd City detected at grid " + gridCoords + "!"));
                }
                case "hint_common", "hint_uncommon", "hint_rare", "hint_very_rare" -> {
                    MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
                    EndMapData loadedMap = maps.getLoadedMap();
                    if (loadedMap == null) {
                        player.sendSystemMessage(Component.literal("§cLoad a map first with /endmap load!"));
                        return;
                    }
                    RelicRarity hintRarity = switch (itemId) {
                        case "hint_common" -> RelicRarity.COMMON;
                        case "hint_uncommon" -> RelicRarity.UNCOMMON;
                        case "hint_rare" -> RelicRarity.RARE;
                        case "hint_very_rare" -> RelicRarity.VERY_RARE;
                        default -> RelicRarity.COMMON;
                    };
                    int radius = StructureFinder.getSearchRadius(itemId);
                    BlockPos cityPos = StructureFinder.findNearestUnmarkedCity(player, radius);
                    if (cityPos == null) {
                        player.sendSystemMessage(Component.literal("§cNo End City found within range!"));
                        return;
                    }
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    // Set forced relic tier
                    RelicJournalData journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
                    player.setAttached(NeptuneAttachments.RELIC_JOURNAL, journal.withForcedTier(hintRarity));
                    String gridCoords = StructureFinder.toGridString(player, cityPos);
                    String rarityName = hintRarity.getDisplayName();
                    player.sendSystemMessage(Component.literal(
                            "§a" + rarityName + " relic detected at grid " + gridCoords + "! Visit that city to claim it."));
                }
                case "shulker_shell" -> {
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    giveItem(player, new ItemStack(Items.SHULKER_SHELL, 1));
                    player.sendSystemMessage(Component.literal("§aPurchased Shulker Shell!"));
                }
                case "ench_shards" -> {
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    giveItem(player, new ItemStack(NeptuneItems.ENCHANTMENT_SHARD, 5));
                    player.sendSystemMessage(Component.literal("§aPurchased 5 Enchantment Shards!"));
                }
                case "void_elytra" -> {
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    ItemStack elytra = new ItemStack(Items.ELYTRA);
                    int maxDmg = elytra.getMaxDamage();
                    elytra.setDamageValue(maxDmg - 100);
                    elytra.set(DataComponents.CUSTOM_NAME, Component.literal("§5Void Elytra"));
                    elytra.set(DataComponents.LORE, new ItemLore(
                            List.of(Component.literal("§7Emergency elytra. Low durability."))));
                    giveItem(player, elytra);
                    player.sendSystemMessage(Component.literal("§aPurchased Void Elytra!"));
                }
                case "pocket_shulker" -> {
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    ItemStack shulkerBox = new ItemStack(Items.PURPLE_SHULKER_BOX, 1);
                    // Tag it as a pocket shulker so the death mixin can find it
                    CustomData customData = CustomData.EMPTY.update(tag -> tag.putBoolean("neptune_pocket", true));
                    shulkerBox.set(DataComponents.CUSTOM_DATA, customData);
                    shulkerBox.set(DataComponents.CUSTOM_NAME, Component.literal("§dPocket Shulker"));
                    shulkerBox.set(DataComponents.LORE, new ItemLore(
                            List.of(Component.literal("§cVanishes on death with all contents"))));
                    giveItem(player, shulkerBox);
                    player.sendSystemMessage(Component.literal("§aPurchased Pocket Shulker!"));
                }
                default -> {
                    // Generic give-item entries (rockets, pearls, food, etc.)
                    if (!entry.itemStack().isEmpty()) {
                        player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                        giveItem(player, entry.itemStack().copy());
                        player.sendSystemMessage(Component.literal("§aPurchased " + entry.name() + "!"));
                    }
                }
            }
        });
    }

    /**
     * Sync infusable relic data to client for the Relic Infuser screen.
     */
    public static void syncRelicInfuserToClient(ServerPlayer player) {
        RelicJournalData journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
        List<String> infusableIds = journal.getInfusableRelicIds();
        List<RelicInfuserSyncPayload.InfusableEntry> entries = new ArrayList<>();

        for (String relicId : infusableIds) {
            RelicDefinition def = RelicDefinition.get(relicId);
            String name = def != null ? def.displayName() : relicId;
            int dupes = journal.getDuplicateCount(relicId);
            entries.add(new RelicInfuserSyncPayload.InfusableEntry(relicId, name, dupes));
        }

        ServerPlayNetworking.send(player, new RelicInfuserSyncPayload(entries));
    }

    /**
     * Sync waypoint data to client for the Waypoint screen.
     */
    public static void syncWaypointsToClient(ServerPlayer player, BlockPos currentPos) {
        WaypointData waypoints = player.getAttachedOrCreate(NeptuneAttachments.WAYPOINTS);
        List<WaypointSyncPayload.WaypointEntry> entries = new ArrayList<>();

        for (WaypointData.Waypoint wp : waypoints.getWaypoints()) {
            double distance = Math.sqrt(currentPos.distSqr(wp.pos()));
            int cost = 20 + (int)(distance / 1000) * 5;
            entries.add(new WaypointSyncPayload.WaypointEntry(wp.name(), wp.pos(), cost));
        }

        ServerPlayNetworking.send(player, new WaypointSyncPayload(entries));
    }

    /**
     * Apply infusion attribute modifiers on player join.
     */
    private static void applyInfusionOnJoin(ServerPlayer player) {
        InfusionData infusion = player.getAttachedOrCreate(NeptuneAttachments.INFUSION);
        if (infusion.infusionCount() > 0) {
            RelicInfuserMenu.applyInfusionAttributes(player, infusion);
        }
    }

    /**
     * Validate that waypoint beacons still exist. Called on player join.
     */
    private static void validateWaypoints(ServerPlayer player) {
        WaypointData waypoints = player.getAttachedOrCreate(NeptuneAttachments.WAYPOINTS);
        boolean changed = false;
        List<WaypointData.Waypoint> valid = new ArrayList<>();

        for (WaypointData.Waypoint wp : waypoints.getWaypoints()) {
            boolean stillPlaced = false;
            boolean anyLoaded = false;
            for (ServerLevel level : player.level().getServer().getAllLevels()) {
                if (level.isLoaded(wp.pos())) {
                    anyLoaded = true;
                    if (level.getBlockState(wp.pos()).is(NeptuneBlocks.WAYPOINT_BEACON)) {
                        stillPlaced = true;
                    }
                    break;
                }
            }
            if (!anyLoaded || stillPlaced) {
                valid.add(wp);
            } else {
                changed = true;
            }
        }

        if (changed) {
            player.setAttached(NeptuneAttachments.WAYPOINTS, new WaypointData(List.copyOf(valid)));
        }
    }

    /**
     * Handle purchase of a rotating stock item.
     */
    private static void handleRotatingPurchase(ServerPlayer player, RotatingStock.RotatingEntry entry) {
        String itemId = entry.id();

        // Validate item is in the player's current rotation
        RotatingStockData stockData = player.getAttachedOrCreate(NeptuneAttachments.ROTATING_STOCK);
        if (!stockData.hasCurrentItem(itemId)) {
            player.sendSystemMessage(Component.literal("§cThis item is not in your current rotation!"));
            return;
        }

        // Check unlock requirement
        if (entry.requiredUnlock() != null) {
            UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
            if (!unlocks.hasUnlock(entry.requiredUnlock())) {
                player.sendSystemMessage(Component.literal("§cYou haven't unlocked this item yet!"));
                return;
            }
        }

        // Calculate cost with Explorers discount
        int cost = entry.cost();
        if (RelicSetBonus.hasExplorersBonus(player)) {
            cost = RelicSetBonus.applyExplorersDiscount(cost);
        }

        VoidEssenceData data = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
        VoidEssenceData afterSpend = data.spend(cost);
        if (afterSpend == null) {
            player.sendSystemMessage(Component.literal("§cNot enough void essence! Need " + cost));
            return;
        }

        switch (itemId) {
            case "rot_eff_tome" -> {
                if (!TomeBuffManager.applyEfficiencyTome(player)) return;
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
            }
            case "rot_prot_tome" -> {
                if (!TomeBuffManager.applyProtectionTome(player)) return;
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
            }
            case "rot_double_drop" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                RelicJournalData journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
                player.setAttached(NeptuneAttachments.RELIC_JOURNAL, journal.withDoubleDropActive());
                player.sendSystemMessage(Component.literal("§dDouble Drop Charm activated! Your next relic find will be doubled."));
            }
            case "rot_mystery_box" -> {
                RelicJournalData journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
                // Find unowned relics across common, uncommon, rare
                List<RelicDefinition> candidates = new ArrayList<>();
                for (RelicRarity rarity : new RelicRarity[]{RelicRarity.COMMON, RelicRarity.UNCOMMON, RelicRarity.RARE}) {
                    for (RelicDefinition def : RelicDefinition.getByRarity(rarity)) {
                        if (!journal.hasDiscovered(def.id())) {
                            candidates.add(def);
                        }
                    }
                }
                if (candidates.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§cYou already own all common, uncommon, and rare relics!"));
                    return;
                }
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                RelicDefinition chosen = candidates.get(new Random().nextInt(candidates.size()));
                ItemStack relicStack = RelicItem.createStack(NeptuneItems.RELIC, chosen);
                giveItem(player, relicStack);
                player.sendSystemMessage(Component.literal(
                        chosen.rarity().getColorCode() + "✦ Mystery Box revealed: " + chosen.displayName() + "!"));
            }
            case "rot_slow_fall" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                for (int i = 0; i < 3; i++) {
                    ItemStack potion = new ItemStack(Items.POTION);
                    potion.set(DataComponents.POTION_CONTENTS,
                            new net.minecraft.world.item.alchemy.PotionContents(Potions.LONG_SLOW_FALLING));
                    giveItem(player, potion);
                }
                player.sendSystemMessage(Component.literal("§aPurchased 3 Slow Falling Potions!"));
            }
            case "rot_totem" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                giveItem(player, new ItemStack(Items.TOTEM_OF_UNDYING));
                player.sendSystemMessage(Component.literal("§aPurchased Totem of Undying!"));
            }
            case "rot_gap" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                giveItem(player, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
                player.sendSystemMessage(Component.literal("§aPurchased Enchanted Golden Apple!"));
            }
            case "rot_carrots" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                giveItem(player, new ItemStack(Items.GOLDEN_CARROT, 32));
                player.sendSystemMessage(Component.literal("§aPurchased 32 Golden Carrots!"));
            }
            case "rot_ender_chest" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                giveItem(player, new ItemStack(Items.ENDER_CHEST));
                player.sendSystemMessage(Component.literal("§aPurchased Ender Chest!"));
            }
            case "rot_night_vision" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                for (int i = 0; i < 3; i++) {
                    ItemStack potion = new ItemStack(Items.POTION);
                    potion.set(DataComponents.POTION_CONTENTS,
                            new net.minecraft.world.item.alchemy.PotionContents(Potions.LONG_NIGHT_VISION));
                    giveItem(player, potion);
                }
                player.sendSystemMessage(Component.literal("§aPurchased 3 Night Vision Potions!"));
            }
            case "rot_scaffolding" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                giveItem(player, new ItemStack(Items.SCAFFOLDING, 64));
                player.sendSystemMessage(Component.literal("§aPurchased 64 Scaffolding!"));
            }
            case "rot_spyglass" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                giveItem(player, new ItemStack(Items.SPYGLASS));
                player.sendSystemMessage(Component.literal("§aPurchased Spyglass!"));
            }
            case "rot_shield" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                giveItem(player, new ItemStack(Items.SHIELD));
                player.sendSystemMessage(Component.literal("§aPurchased Shield!"));
            }
            case "rot_bow_arrows" -> {
                player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                giveItem(player, new ItemStack(Items.BOW));
                giveItem(player, new ItemStack(Items.ARROW, 64));
                player.sendSystemMessage(Component.literal("§aPurchased Bow + 64 Arrows!"));
            }
        }

        // Record purchase in history
        player.setAttached(NeptuneAttachments.ROTATING_STOCK, stockData.withPurchase(itemId));
    }

    /**
     * Sync rotating stock to client for the broker screen.
     */
    public static void syncRotatingStockToClient(ServerPlayer player) {
        RotatingStockData stockData = player.getAttachedOrCreate(NeptuneAttachments.ROTATING_STOCK);
        List<RotatingStock.RotatingEntry> entries = RotatingStock.getCurrentStock(stockData);

        boolean discount = RelicSetBonus.hasExplorersBonus(player);

        List<RotatingStockSyncPayload.RotatingEntry> syncEntries = new ArrayList<>();
        for (RotatingStock.RotatingEntry entry : entries) {
            int cost = discount ? RelicSetBonus.applyExplorersDiscount(entry.cost()) : entry.cost();
            syncEntries.add(new RotatingStockSyncPayload.RotatingEntry(
                    entry.id(), entry.name(), cost, entry.description()));
        }

        ServerPlayNetworking.send(player, new RotatingStockSyncPayload(syncEntries));
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /**
     * Sync shard infuser state to the client for rendering.
     */
    public static void syncShardInfuserToClient(ServerPlayer player, BlockPos pos) {
        if (!(player.level().getBlockEntity(pos) instanceof ShardInfuserBlockEntity be)) return;

        ItemStack gear = be.getGearSlot();
        List<ShardInfuserSyncPayload.EnchantEntry> entries = new ArrayList<>();

        if (!gear.isEmpty()) {
            List<EnchantmentShardHelper.ApplicableEnchantment> applicable =
                    EnchantmentShardHelper.getApplicableEnchantments(gear, player.level().registryAccess());
            for (EnchantmentShardHelper.ApplicableEnchantment ae : applicable) {
                entries.add(new ShardInfuserSyncPayload.EnchantEntry(
                        ae.getDisplayName(), ae.targetLevel(), ae.shardCost()));
            }
        }

        ServerPlayNetworking.send(player, new ShardInfuserSyncPayload(gear.copy(), entries));
    }

    /**
     * Validate that tracked block placements still exist. Called on player join.
     */
    private static void validateBlockPlacements(ServerPlayer player) {
        BlockPlacementsData placements = player.getAttachedOrCreate(NeptuneAttachments.BLOCK_PLACEMENTS);
        boolean changed = false;

        if (placements.hasBreakdownTable()) {
            BlockPos pos = placements.breakdownTablePos().get();
            if (!isBlockStillPlaced(player, pos, NeptuneBlocks.BREAKDOWN_TABLE)) {
                placements = placements.withoutBreakdownTable();
                changed = true;
            }
        }

        if (placements.hasShardInfuser()) {
            BlockPos pos = placements.shardInfuserPos().get();
            if (!isBlockStillPlaced(player, pos, NeptuneBlocks.SHARD_INFUSER)) {
                placements = placements.withoutShardInfuser();
                changed = true;
            }
        }

        if (placements.hasRelicInfuser()) {
            BlockPos pos = placements.relicInfuserPos().get();
            if (!isBlockStillPlaced(player, pos, NeptuneBlocks.RELIC_INFUSER)) {
                placements = placements.withoutRelicInfuser();
                changed = true;
            }
        }

        if (changed) {
            player.setAttached(NeptuneAttachments.BLOCK_PLACEMENTS, placements);
        }
    }

    private static boolean isBlockStillPlaced(ServerPlayer player, BlockPos pos, net.minecraft.world.level.block.Block block) {
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            if (level.isLoaded(pos) && level.getBlockState(pos).is(block)) {
                return true;
            }
        }
        // If no level has it loaded, we can't confirm — assume still valid
        // (will be validated next time the chunk loads)
        boolean anyLoaded = false;
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            if (level.isLoaded(pos)) {
                anyLoaded = true;
                break;
            }
        }
        return !anyLoaded;
    }

    /**
     * Sync the player's loaded map data to the client for rendering.
     */
    public static void syncMapToClient(ServerPlayer player) {
        MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
        EndMapData loadedMap = maps.getLoadedMap();

        if (loadedMap == null) {
            Neptune.LOGGER.info("[Neptune] Syncing empty map to client for {}", player.getName().getString());
            ServerPlayNetworking.send(player, new MapSyncPayload("", 0, List.of()));
        } else {
            Neptune.LOGGER.info("[Neptune] Syncing map '{}' (grid={}, marks={}) to client for {}",
                    loadedMap.name(), loadedMap.gridSize(), loadedMap.markedGrids().size(),
                    player.getName().getString());
            ServerPlayNetworking.send(player, new MapSyncPayload(
                    loadedMap.name(),
                    loadedMap.gridSize(),
                    new ArrayList<>(loadedMap.markedGrids())
            ));
        }
    }
}
