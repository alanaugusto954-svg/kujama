package kujama;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import kujama.modules.*;

public class Kujama implements ClientModInitializer {
    public static final String ID = "kujama";
    public static KeyMapping openGuiKey;
    public static KeyMapping zoomKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));
        openGuiKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.kujama.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.kujama.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ModuleManager.register(new WatermarkModule());
        ModuleManager.register(new FpsModule());
        ModuleManager.register(new CpsModule());
        ModuleManager.register(new CoordsModule());
        ModuleManager.register(new KeystrokesModule());
        ModuleManager.register(new ArmorModule());
        ModuleManager.register(new SprintModule());
        ModuleManager.register(new ZoomModule());
        ModuleManager.load();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(ID, "hud"),
                (graphics, deltaTracker) -> ModuleManager.render(graphics));

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
            CpsModule.registerClick();
            return false; // no cancela el ataque
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.consumeClick()) {
                if (client.screen == null) client.setScreen(new ClickGuiScreen(null));
            }
            ModuleManager.tick();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ModuleManager.shutdown());
    }
}
