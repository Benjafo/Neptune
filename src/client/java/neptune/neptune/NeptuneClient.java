package neptune.neptune;

import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.entity.BrokerEntityRenderer;
import neptune.neptune.entity.NeptuneEntities;
import neptune.neptune.hud.EndMapHud;
import neptune.neptune.hud.VoidEssenceHud;
import neptune.neptune.map.ClientMapState;
import neptune.neptune.network.MapSyncPayload;
import neptune.neptune.screen.BrokerScreen;
import neptune.neptune.screen.EndMapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class NeptuneClient implements ClientModInitializer {

    public static KeyMapping TOGGLE_MINIMAP_KEY;
    public static KeyMapping OPEN_FULLMAP_KEY;

    @Override
    public void onInitializeClient() {
        // Entity renderers
        EntityRendererRegistry.register(NeptuneEntities.BROKER, BrokerEntityRenderer::new);

        // Menu screens
        MenuScreens.register(NeptuneMenus.BROKER_MENU, BrokerScreen::new);

        // HUD elements
        VoidEssenceHud.register();
        EndMapHud.register();

        // Keybinds
        TOGGLE_MINIMAP_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.neptune.toggle_minimap",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                KeyMapping.Category.MISC
        ));

        OPEN_FULLMAP_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.neptune.open_fullmap",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KeyMapping.Category.MISC
        ));

        // Client tick — check keybinds
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TOGGLE_MINIMAP_KEY.consumeClick()) {
                ClientMapState.toggleMinimap();
            }
            if (OPEN_FULLMAP_KEY.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new EndMapScreen());
                }
            }
        });

        // Receive map sync packets
        ClientPlayNetworking.registerGlobalReceiver(MapSyncPayload.TYPE, (payload, context) -> {
            ClientMapState.handleSync(payload);
        });
    }
}
