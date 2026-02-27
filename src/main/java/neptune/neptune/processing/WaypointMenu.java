package neptune.neptune.processing;

import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.data.WaypointData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WaypointMenu extends AbstractContainerMenu {

    private final BlockPos pos;

    public WaypointMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(NeptuneMenus.WAYPOINT_MENU, containerId);
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void handleTeleport(ServerPlayer player, int waypointIndex) {
        WaypointData waypoints = player.getAttachedOrCreate(NeptuneAttachments.WAYPOINTS);
        List<WaypointData.Waypoint> wpList = waypoints.getWaypoints();

        if (waypointIndex < 0 || waypointIndex >= wpList.size()) {
            player.sendSystemMessage(Component.literal("§cInvalid waypoint!"));
            return;
        }

        WaypointData.Waypoint target = wpList.get(waypointIndex);

        // Can't teleport to self
        if (target.pos().equals(pos)) {
            player.sendSystemMessage(Component.literal("§cYou're already at this waypoint!"));
            return;
        }

        // Calculate cost
        double distance = Math.sqrt(pos.distSqr(target.pos()));
        int cost = 20 + (int)(distance / 1000) * 5;

        VoidEssenceData essence = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
        VoidEssenceData afterSpend = essence.spend(cost);
        if (afterSpend == null) {
            player.sendSystemMessage(Component.literal("§cNot enough void essence! Need " + cost + ", have " + essence.current()));
            return;
        }

        player.setAttached(NeptuneAttachments.VOID_ESSENCE, afterSpend);
        player.closeContainer();
        player.teleportTo(target.pos().getX() + 0.5, target.pos().getY() + 1, target.pos().getZ() + 0.5);
        player.sendSystemMessage(Component.literal("§aTeleported to " + target.name() + "! §7(-" + cost + " essence)"));
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
        return player.level().getBlockState(pos).is(NeptuneBlocks.WAYPOINT_BEACON);
    }
}
