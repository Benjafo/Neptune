package neptune.neptune.data;

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

    public static void register() {
        // Force static initialization
    }
}
