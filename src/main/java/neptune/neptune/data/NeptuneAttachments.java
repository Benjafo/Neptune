package neptune.neptune.data;

import neptune.neptune.challenge.ChallengeData;
import neptune.neptune.map.MapCollectionData;
import neptune.neptune.unlock.UnlockData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public class NeptuneAttachments {

    public static final AttachmentType<VoidEssenceData> VOID_ESSENCE = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neptune", "void_essence"),
            builder -> builder
                    .initializer(() -> VoidEssenceData.EMPTY)
                    .persistent(VoidEssenceData.CODEC)
                    .copyOnDeath()
                    .syncWith(VoidEssenceData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    );

    public static final AttachmentType<UnlockData> UNLOCKS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neptune", "unlocks"),
            builder -> builder
                    .initializer(() -> UnlockData.EMPTY)
                    .persistent(UnlockData.CODEC)
                    .copyOnDeath()
                    .syncWith(UnlockData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    );

    public static final AttachmentType<ChallengeData> CHALLENGES = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neptune", "challenges"),
            builder -> builder
                    .initializer(() -> ChallengeData.EMPTY)
                    .persistent(ChallengeData.CODEC)
                    .copyOnDeath()
                    .syncWith(ChallengeData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    );

    public static final AttachmentType<MapCollectionData> MAPS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neptune", "maps"),
            builder -> builder
                    .initializer(() -> MapCollectionData.EMPTY)
                    .persistent(MapCollectionData.CODEC)
                    .copyOnDeath()
    );

    public static void register() {
        // Force static initialization
    }
}
