package neptune.neptune.processing;

import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.data.InfusionData;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.relic.RelicDefinition;
import neptune.neptune.relic.RelicJournalData;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class RelicInfuserMenu extends AbstractContainerMenu {

    private static final int REQUIRED_DUPLICATES = 20;
    private final BlockPos pos;

    public RelicInfuserMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(NeptuneMenus.RELIC_INFUSER_MENU, containerId);
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void handleInfuse(ServerPlayer player, String relicId, String buffChoice) {
        UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
        if (!unlocks.hasTier(UnlockBranch.PROCESSING, 4)) {
            player.sendSystemMessage(Component.literal("§cRequires Processing T4!"));
            return;
        }

        RelicDefinition relic = RelicDefinition.get(relicId);
        if (relic == null) {
            player.sendSystemMessage(Component.literal("§cInvalid relic!"));
            return;
        }

        RelicJournalData journal = player.getAttachedOrCreate(NeptuneAttachments.RELIC_JOURNAL);
        int dupes = journal.getDuplicateCount(relicId);
        if (dupes < REQUIRED_DUPLICATES) {
            player.sendSystemMessage(Component.literal("§cNeed " + REQUIRED_DUPLICATES + " duplicates! Have " + dupes));
            return;
        }

        // Consume duplicates
        journal = journal.consumeDuplicates(relicId, REQUIRED_DUPLICATES);
        player.setAttached(NeptuneAttachments.RELIC_JOURNAL, journal);

        // Apply infusion buff
        InfusionData infusion = player.getAttachedOrCreate(NeptuneAttachments.INFUSION);
        switch (buffChoice) {
            case "health" -> {
                infusion = infusion.withHealthInfusion();
                player.setAttached(NeptuneAttachments.INFUSION, infusion);
                applyInfusionAttributes(player, infusion);
                player.sendSystemMessage(Component.literal("§a§lRelic Infusion: +2 Max Health! §7(Total: +" + infusion.bonusHealth() + ")"));
            }
            case "damage" -> {
                infusion = infusion.withDamageInfusion();
                player.setAttached(NeptuneAttachments.INFUSION, infusion);
                applyInfusionAttributes(player, infusion);
                player.sendSystemMessage(Component.literal("§a§lRelic Infusion: +5% Attack Damage! §7(Total: +" + infusion.bonusDamage() + "%)"));
            }
            case "speed" -> {
                infusion = infusion.withSpeedInfusion();
                player.setAttached(NeptuneAttachments.INFUSION, infusion);
                applyInfusionAttributes(player, infusion);
                player.sendSystemMessage(Component.literal("§a§lRelic Infusion: +5% Movement Speed! §7(Total: +" + infusion.bonusSpeed() + "%)"));
            }
            default -> player.sendSystemMessage(Component.literal("§cInvalid buff choice!"));
        }
    }

    public static void applyInfusionAttributes(ServerPlayer player, InfusionData infusion) {
        // Health bonus (in half-hearts)
        if (infusion.bonusHealth() > 0) {
            player.getAttribute(Attributes.MAX_HEALTH).addOrReplacePermanentModifier(
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath("neptune", "infusion_health"),
                            infusion.bonusHealth(),
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            // Heal to new max if needed
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        // Damage bonus (percentage)
        if (infusion.bonusDamage() > 0) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).addOrReplacePermanentModifier(
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath("neptune", "infusion_damage"),
                            infusion.bonusDamage() / 100.0,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            );
        }

        // Speed bonus (percentage)
        if (infusion.bonusSpeed() > 0) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).addOrReplacePermanentModifier(
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath("neptune", "infusion_speed"),
                            infusion.bonusSpeed() / 100.0,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            );
        }
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
        return player.level().getBlockState(pos).is(NeptuneBlocks.RELIC_INFUSER);
    }
}
