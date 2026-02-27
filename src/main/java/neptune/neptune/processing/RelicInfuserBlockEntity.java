package neptune.neptune.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public class RelicInfuserBlockEntity extends BlockEntity {

    private UUID ownerUUID;

    public RelicInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(NeptuneBlocks.RELIC_INFUSER_BE, pos, state);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (ownerUUID != null) {
            output.putString("OwnerUUID", ownerUUID.toString());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getString("OwnerUUID").ifPresent(s -> ownerUUID = UUID.fromString(s));
    }
}
