package neptune.neptune.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public class ShardInfuserBlockEntity extends BlockEntity {

    private UUID ownerUUID;
    private ItemStack gearSlot = ItemStack.EMPTY;

    public ShardInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(NeptuneBlocks.SHARD_INFUSER_BE, pos, state);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    public ItemStack getGearSlot() {
        return gearSlot;
    }

    public void setGearSlot(ItemStack stack) {
        this.gearSlot = stack;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (ownerUUID != null) {
            output.putString("OwnerUUID", ownerUUID.toString());
        }
        if (!gearSlot.isEmpty()) {
            output.store("GearSlot", ItemStack.CODEC, gearSlot);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getString("OwnerUUID").ifPresent(s -> ownerUUID = UUID.fromString(s));
        input.read("GearSlot", ItemStack.CODEC).ifPresent(stack -> gearSlot = stack);
    }
}
