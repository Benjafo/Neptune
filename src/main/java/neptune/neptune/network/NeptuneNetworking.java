package neptune.neptune.network;

import neptune.neptune.broker.BrokerStock;
import neptune.neptune.broker.StructureFinder;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.map.EndMapData;
import neptune.neptune.map.MapCollectionData;
import neptune.neptune.relic.*;
import neptune.neptune.unlock.UnlockData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import neptune.neptune.Neptune;

import java.util.ArrayList;
import java.util.List;

public class NeptuneNetworking {

    public static void register() {
        // Register C2S packets
        PayloadTypeRegistry.playC2S().register(BrokerPurchasePayload.TYPE, BrokerPurchasePayload.STREAM_CODEC);

        // Register S2C packets
        PayloadTypeRegistry.playS2C().register(MapSyncPayload.TYPE, MapSyncPayload.STREAM_CODEC);

        // Sync map data when player joins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            MapCollectionData maps = player.getAttachedOrCreate(NeptuneAttachments.MAPS);
            if (maps.getLoadedMap() != null) {
                syncMapToClient(player);
            }
        });

        // Handle broker purchase
        ServerPlayNetworking.registerGlobalReceiver(BrokerPurchasePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            String itemId = payload.itemId();

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

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
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
