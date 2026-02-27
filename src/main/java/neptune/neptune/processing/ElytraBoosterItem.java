package neptune.neptune.processing;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class ElytraBoosterItem extends Item {

    public ElytraBoosterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isFallFlying()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            player.push(look.x * 1.5, look.y * 0.5 + 0.1, look.z * 1.5);
            player.hurtMarked = true;

            ItemStack stack = player.getItemInHand(hand);
            stack.shrink(1);

            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.literal("§aBoosted!"));
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("§7Use while gliding for a speed burst."));
        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }
}
