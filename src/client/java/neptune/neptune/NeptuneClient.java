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
import neptune.neptune.screen.RelicJournalScreen;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.unlock.UnlockBranch;
import neptune.neptune.unlock.UnlockData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class NeptuneClient implements ClientModInitializer {

    public static KeyMapping TOGGLE_MINIMAP_KEY;
    public static KeyMapping OPEN_FULLMAP_KEY;
    public static KeyMapping OPEN_JOURNAL_KEY;

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

        OPEN_JOURNAL_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.neptune.open_journal",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                KeyMapping.Category.MISC
        ));

        // Client tick — check keybinds (gated by Navigation unlocks)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            UnlockData unlocks = client.player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);

            if (TOGGLE_MINIMAP_KEY.consumeClick()) {
                if (unlocks.hasTier(UnlockBranch.NAVIGATION, 2)) {
                    ClientMapState.toggleMinimap();
                } else {
                    client.player.displayClientMessage(
                            Component.literal("§cRequires Navigation T2 (Wayfinder) to use the minimap."), false);
                }
            }
            if (OPEN_FULLMAP_KEY.consumeClick()) {
                if (client.screen == null) {
                    if (unlocks.hasTier(UnlockBranch.NAVIGATION, 1)) {
                        client.setScreen(new EndMapScreen());
                    } else {
                        client.player.displayClientMessage(
                                Component.literal("§cRequires Navigation T1 (Cartographer's Basics) to open the map."), false);
                    }
                }
            }
            if (OPEN_JOURNAL_KEY.consumeClick()) {
                if (client.screen == null) {
                    if (unlocks.hasTier(UnlockBranch.CATALOG, 1)) {
                        client.setScreen(new RelicJournalScreen());
                    } else {
                        client.player.displayClientMessage(
                                Component.literal("§cRequires Catalog T1 (Collector) to open the relic journal."), false);
                    }
                }
            }
        });

        // Receive map sync packets
        ClientPlayNetworking.registerGlobalReceiver(MapSyncPayload.TYPE, (payload, context) -> {
            ClientMapState.handleSync(payload);
        });
    }
}
