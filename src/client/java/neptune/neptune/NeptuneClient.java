package neptune.neptune;

import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.entity.BrokerEntityRenderer;
import neptune.neptune.entity.NeptuneEntities;
import neptune.neptune.hud.VoidEssenceHud;
import neptune.neptune.screen.BrokerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public class NeptuneClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Entity renderers
        EntityRendererRegistry.register(NeptuneEntities.BROKER, BrokerEntityRenderer::new);

        // Menu screens
        MenuScreens.register(NeptuneMenus.BROKER_MENU, BrokerScreen::new);

        // HUD elements
        VoidEssenceHud.register();
    }
}
