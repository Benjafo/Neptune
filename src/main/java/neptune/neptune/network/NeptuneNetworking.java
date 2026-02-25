package neptune.neptune.network;

import neptune.neptune.broker.BrokerStock;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class NeptuneNetworking {

    public static void register() {
        // Register C2S packets
        PayloadTypeRegistry.playC2S().register(BrokerPurchasePayload.TYPE, BrokerPurchasePayload.STREAM_CODEC);

        // Handle broker purchase
        ServerPlayNetworking.registerGlobalReceiver(BrokerPurchasePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            int index = payload.stockIndex();

            List<BrokerStock.StockEntry> stock = BrokerStock.getCoreStock();
            if (index < 0 || index >= stock.size()) return;

            BrokerStock.StockEntry entry = stock.get(index);

            // Check if player has enough essence
            VoidEssenceData data = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
            VoidEssenceData afterSpend = data.spend(entry.cost());
            if (afterSpend == null) {
                player.sendSystemMessage(Component.literal("§cNot enough void essence! Need " + entry.cost()));
                return;
            }

            // Handle special items
            switch (entry.name()) {
                case "Basic Repair Kit" -> {
                    ItemStack held = player.getMainHandItem();
                    if (held.isEmpty() || !held.isDamageableItem()) {
                        player.sendSystemMessage(Component.literal("§cHold a damageable item in your main hand!"));
                        return;
                    }
                    // Deduct essence
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    // Restore 50% durability
                    int maxDamage = held.getMaxDamage();
                    int currentDamage = held.getDamageValue();
                    int repair = maxDamage / 2;
                    held.setDamageValue(Math.max(0, currentDamage - repair));
                    player.sendSystemMessage(Component.literal("§aRepaired " + held.getHoverName().getString() + "!"));
                    return;
                }
                case "Recall Pearl" -> {
                    // Deduct essence
                    player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                    // Teleport to 0, ~, 0 (main End island)
                    player.teleportTo(0.5, 64, 0.5);
                    player.sendSystemMessage(Component.literal("§aTeleported to main End island!"));
                    return;
                }
                default -> {
                    // Give item stack
                    if (!entry.itemStack().isEmpty()) {
                        ItemStack toGive = entry.itemStack().copy();
                        if (!player.getInventory().add(toGive)) {
                            // Drop on ground if inventory is full
                            player.drop(toGive, false);
                        }
                        player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
                        player.sendSystemMessage(Component.literal("§aPurchased " + entry.name() + "!"));
                    }
                }
            }
        });
    }
}
