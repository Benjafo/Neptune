package neptune.neptune.data;

import neptune.neptune.challenge.ChallengeData;
import neptune.neptune.map.MapCollectionData;
import neptune.neptune.relic.RelicJournalData;
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

    public static final AttachmentType<RelicJournalData> RELIC_JOURNAL = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neptune", "relic_journal"),
            builder -> builder
                    .initializer(() -> RelicJournalData.EMPTY)
                    .persistent(RelicJournalData.CODEC)
                    .copyOnDeath()
                    .syncWith(RelicJournalData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    );

    public static final AttachmentType<BlockPlacementsData> BLOCK_PLACEMENTS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neptune", "block_placements"),
            builder -> builder
                    .initializer(() -> BlockPlacementsData.EMPTY)
                    .persistent(BlockPlacementsData.CODEC)
                    .copyOnDeath()
    );

    public static final AttachmentType<InfusionData> INFUSION = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neptune", "infusion"),
            builder -> builder
                    .initializer(() -> InfusionData.EMPTY)
                    .persistent(InfusionData.CODEC)
                    .copyOnDeath()
    );

    public static final AttachmentType<WaypointData> WAYPOINTS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neptune", "waypoints"),
            builder -> builder
                    .initializer(() -> WaypointData.EMPTY)
                    .persistent(WaypointData.CODEC)
                    .copyOnDeath()
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
