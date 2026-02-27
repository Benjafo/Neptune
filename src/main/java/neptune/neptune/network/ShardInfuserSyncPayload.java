package neptune.neptune.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * S2C: sync shard infuser state to client (gear + available enchantments).
 */
public record ShardInfuserSyncPayload(
        ItemStack gear,
        List<EnchantEntry> enchantments
) implements CustomPacketPayload {

    public record EnchantEntry(String displayName, int targetLevel, int shardCost) {
        public static final StreamCodec<ByteBuf, EnchantEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, EnchantEntry::displayName,
                ByteBufCodecs.INT, EnchantEntry::targetLevel,
                ByteBufCodecs.INT, EnchantEntry::shardCost,
                EnchantEntry::new
        );
    }

    public static final Type<ShardInfuserSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("neptune", "shard_infuser_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShardInfuserSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_STREAM_CODEC, ShardInfuserSyncPayload::gear,
                    EnchantEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), ShardInfuserSyncPayload::enchantments,
                    ShardInfuserSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
