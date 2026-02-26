package neptune.neptune.relic;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Custom item class for all relics. Each relic ItemStack stores a "relicId" in CUSTOM_DATA.
 */
public class RelicItem extends Item {

    public RelicItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        RelicPickupHandler.onRelicTick(stack, level, entity, slot);
    }

    @Override
    public Component getName(ItemStack stack) {
        RelicDefinition def = getDefinition(stack);
        if (def != null) {
            return Component.literal(def.rarity().getColorCode() + def.displayName());
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        RelicDefinition def = getDefinition(stack);
        if (def != null) {
            tooltipAdder.accept(Component.literal(def.rarity().getColorCode() + def.rarity().getDisplayName() + " Relic"));
            if (def.set() != RelicSet.STANDALONE) {
                tooltipAdder.accept(Component.literal("§7Set: " + def.set().getDisplayName()));
            }
            if (!def.lore().isEmpty()) {
                tooltipAdder.accept(Component.literal("§8§o" + def.lore()));
            }
        }
        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }

    /**
     * Create a relic ItemStack for the given relic definition.
     */
    public static ItemStack createStack(Item relicItem, RelicDefinition def) {
        ItemStack stack = new ItemStack(relicItem);
        CompoundTag tag = new CompoundTag();
        tag.putString("relicId", def.id());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        return stack;
    }

    /**
     * Get the relic ID from an ItemStack.
     */
    public static String getRelicId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) return null;
        CompoundTag tag = data.copyTag();
        if (tag.contains("relicId")) {
            return tag.getStringOr("relicId", null);
        }
        return null;
    }

    /**
     * Get the RelicDefinition from an ItemStack.
     */
    public static RelicDefinition getDefinition(ItemStack stack) {
        String id = getRelicId(stack);
        if (id == null) return null;
        return RelicDefinition.get(id);
    }
}
