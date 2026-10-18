package kujama.modules;

import kujama.Category;
import kujama.Module;
import net.minecraft.world.item.Items;
import kujama.Kujama;

/** Mantene la tecla de zoom (C por defecto) para acercar la vista. */
public class ZoomModule extends Module {
    private static final int ZOOM_FOV = 30; // minimo que permite la opcion de FOV de Minecraft
    private boolean zooming;
    private int previousFov;

    public ZoomModule() { super("Zoom", Category.UTILIDAD, Items.SPYGLASS, true); }

    @Override
    public void onTick() {
        boolean want = Kujama.zoomKey != null && Kujama.zoomKey.isDown() && mc.screen == null;
        if (want && !zooming) {
            previousFov = mc.options.fov().get();
            zooming = true;
            mc.options.fov().set(ZOOM_FOV);
        } else if (!want && zooming) {
            stop();
        }
    }

    private void stop() {
        zooming = false;
        mc.options.fov().set(previousFov);
    }

    @Override
    public void onDisable() {
        if (zooming) stop();
    }
}
