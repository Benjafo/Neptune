package neptune.neptune.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Immutable record tracking void essence currency.
 * Must be immutable so attachment sync/persistence triggers properly.
 */
public record VoidEssenceData(int current, int lifetime) {

    public static final VoidEssenceData EMPTY = new VoidEssenceData(0, 0);

    public static final Codec<VoidEssenceData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("current").forGetter(VoidEssenceData::current),
                    Codec.INT.fieldOf("lifetime").forGetter(VoidEssenceData::lifetime)
            ).apply(instance, VoidEssenceData::new)
    );

    public static final StreamCodec<ByteBuf, VoidEssenceData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, VoidEssenceData::current,
            ByteBufCodecs.INT, VoidEssenceData::lifetime,
            VoidEssenceData::new
    );

    /**
     * Returns new data with essence added to both current and lifetime.
     */
    public VoidEssenceData add(int amount) {
        if (amount <= 0) return this;
        return new VoidEssenceData(this.current + amount, this.lifetime + amount);
    }

    /**
     * Returns new data with essence spent from current, or null if insufficient.
     */
    public VoidEssenceData spend(int amount) {
        if (amount <= 0 || this.current < amount) return null;
        return new VoidEssenceData(this.current - amount, this.lifetime);
    }

    /**
     * Returns new data with current set directly (for admin/testing).
     */
    public VoidEssenceData withCurrent(int amount) {
        return new VoidEssenceData(Math.max(0, amount), this.lifetime);
    }

    /**
     * Returns new data with lifetime set directly (for admin/testing).
     */
    public VoidEssenceData withLifetime(int amount) {
        return new VoidEssenceData(this.current, Math.max(0, amount));
    }
}
