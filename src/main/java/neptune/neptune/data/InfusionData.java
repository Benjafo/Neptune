package neptune.neptune.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record InfusionData(int bonusHealth, int bonusDamage, int bonusSpeed, int infusionCount) {

    public static final InfusionData EMPTY = new InfusionData(0, 0, 0, 0);

    public static final Codec<InfusionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("bonusHealth").forGetter(InfusionData::bonusHealth),
                    Codec.INT.fieldOf("bonusDamage").forGetter(InfusionData::bonusDamage),
                    Codec.INT.fieldOf("bonusSpeed").forGetter(InfusionData::bonusSpeed),
                    Codec.INT.fieldOf("infusionCount").forGetter(InfusionData::infusionCount)
            ).apply(instance, InfusionData::new)
    );

    public InfusionData withHealthInfusion() {
        return new InfusionData(bonusHealth + 2, bonusDamage, bonusSpeed, infusionCount + 1);
    }

    public InfusionData withDamageInfusion() {
        return new InfusionData(bonusHealth, bonusDamage + 5, bonusSpeed, infusionCount + 1);
    }

    public InfusionData withSpeedInfusion() {
        return new InfusionData(bonusHealth, bonusDamage, bonusSpeed + 5, infusionCount + 1);
    }
}
