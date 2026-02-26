package neptune.neptune.network;

import neptune.neptune.broker.BrokerStock;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.map.EndMapData;
import neptune.neptune.map.MapCollectionData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
            int index = payload.stockIndex();

            List<BrokerStock.StockEntry> stock = BrokerStock.getCoreStock();
            if (index < 0 || index >= stock.size()) return;

            BrokerStock.StockEntry entry = stock.get(index);

            VoidEssenceData data = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
            VoidEssenceData afterSpend = data.spend(entry.cost());
            if (afterSpend == null) {
                player.sendSystemMessage(Component.literal("§cNot enough void essence! Need " + entry.cost()));
                return;
            }

            switch (entry.name()) {
                case "Basic Repair Kit" -> {
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
                    return;
                }
                case "Recall Pearl" -> {
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    player.teleportTo(0.5, 64, 0.5);
                    player.sendSystemMessage(Component.literal("§aTeleported to main End island!"));
                    return;
                }
                default -> {
                    if (!entry.itemStack().isEmpty()) {
                        ItemStack toGive = entry.itemStack().copy();
                        if (!player.getInventory().add(toGive)) {
                            player.drop(toGive, false);
                        }
                        player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                        player.sendSystemMessage(Component.literal("§aPurchased " + entry.name() + "!"));
                    }
                }
            }
        });
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
