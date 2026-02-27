package neptune.neptune.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        if (!gearSlot.isEmpty()) {
            tag.put("GearSlot", gearSlot.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("GearSlot")) {
            gearSlot = ItemStack.parse(registries, tag.getCompound("GearSlot")).orElse(ItemStack.EMPTY);
        }
    }
}
