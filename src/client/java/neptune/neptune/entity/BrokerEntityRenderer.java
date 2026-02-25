package neptune.neptune.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;

/**
 * Placeholder renderer for the Broker entity.
 * Uses NoopRenderer (invisible body) — the entity's custom name tag is still visible.
 * Will be replaced with a proper model during the polish phase.
 */
public class BrokerEntityRenderer extends NoopRenderer<BrokerEntity> {

    public BrokerEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
