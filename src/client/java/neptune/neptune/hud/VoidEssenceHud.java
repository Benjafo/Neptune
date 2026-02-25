package neptune.neptune.hud;

import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class VoidEssenceHud {

    private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("neptune", "void_essence_hud");

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                HUD_ID,
                VoidEssenceHud::render
        );
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        // Only show in the End dimension
        if (mc.player.level().dimension() != Level.END) return;

        VoidEssenceData data = mc.player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);

        String text = "§5✦ §d" + data.current();

        int x = 5;
        int y = 5;

        // Draw background
        int textWidth = mc.font.width("✦ " + data.current());
        graphics.fill(x - 2, y - 2, x + textWidth + 4, y + mc.font.lineHeight + 2, 0x80000000);

        // Draw text
        graphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
    }
}
