package neptune.neptune.processing;

import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.relic.NeptuneItems;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ShardInfuserMenu extends AbstractContainerMenu {

    private final BlockPos pos;

    public ShardInfuserMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(NeptuneMenus.SHARD_INFUSER_MENU, containerId);
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    /**
     * Move an item from the player's inventory into the infuser's gear slot.
     */
    public void handleSetGear(ServerPlayer player, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) return;

        if (!(player.level().getBlockEntity(pos) instanceof ShardInfuserBlockEntity be)) return;

        if (!be.getGearSlot().isEmpty()) {
            player.sendSystemMessage(Component.literal("§cGear slot already occupied! Retrieve it first."));
            return;
        }

        ItemStack stack = player.getInventory().getItem(inventorySlot);
        if (stack.isEmpty()) return;

        // Only accept enchantable gear
        if (!stack.isEnchantable() && stack.getEnchantments().isEmpty()) {
            player.sendSystemMessage(Component.literal("§cThis item cannot be enchanted!"));
            return;
        }

        be.setGearSlot(stack.copy());
        player.getInventory().setItem(inventorySlot, ItemStack.EMPTY);

        neptune.neptune.network.NeptuneNetworking.syncShardInfuserToClient(player, pos);
    }

    /**
     * Return the gear from the infuser back to the player's inventory.
     */
    public void handleRetrieveGear(ServerPlayer player) {
        if (!(player.level().getBlockEntity(pos) instanceof ShardInfuserBlockEntity be)) return;

        ItemStack gear = be.getGearSlot();
        if (gear.isEmpty()) return;

        if (!player.getInventory().add(gear.copy())) {
            player.drop(gear.copy(), false);
        }
        be.setGearSlot(ItemStack.EMPTY);

        neptune.neptune.network.NeptuneNetworking.syncShardInfuserToClient(player, pos);
    }

    /**
     * Apply an enchantment at the given index to the gear.
     */
    public void handleApplyEnchantment(ServerPlayer player, int enchantIndex) {
        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        if (!unlocks.hasTier(UnlockBranch.PROCESSING, 2)) {
            player.sendSystemMessage(Component.literal("§cRequires Processing T2!"));
            return;
        }

        if (!(player.level().getBlockEntity(pos) instanceof ShardInfuserBlockEntity be)) return;

        ItemStack gear = be.getGearSlot();
        if (gear.isEmpty()) return;

        List<EnchantmentShardHelper.ApplicableEnchantment> applicable =
                EnchantmentShardHelper.getApplicableEnchantments(gear, player.level().registryAccess());

        if (enchantIndex < 0 || enchantIndex >= applicable.size()) {
            player.sendSystemMessage(Component.literal("§cInvalid enchantment selection!"));
            return;
        }

        EnchantmentShardHelper.ApplicableEnchantment selected = applicable.get(enchantIndex);
        int cost = selected.shardCost();

        int shardCount = countShards(player);
        if (shardCount < cost) {
            player.sendSystemMessage(Component.literal("§cNot enough shards! Need " + cost + ", have " + shardCount));
            return;
        }

        // Consume shards
        consumeShards(player, cost);

        // Apply enchantment
        EnchantmentShardHelper.applyEnchantment(gear, selected.enchantment(), selected.targetLevel());
        be.setGearSlot(gear);

        player.sendSystemMessage(Component.literal("§aApplied " + selected.getDisplayName() + " " + toRoman(selected.targetLevel()) + "!"));

        neptune.neptune.network.NeptuneNetworking.syncShardInfuserToClient(player, pos);
    }

    public static int countShards(Player player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(NeptuneItems.ENCHANTMENT_SHARD)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static void consumeShards(Player player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(NeptuneItems.ENCHANTMENT_SHARD)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        // All interaction through packets
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).is(NeptuneBlocks.SHARD_INFUSER);
    }
}
