package kujama.modules;

import net.minecraft.client.gui.GuiGraphics;
import kujama.HudModule;
import net.minecraft.world.item.Items;

public class CoordsModule extends HudModule {
    public CoordsModule() { super("Coordenadas", Items.COMPASS, 4, 42); }

    @Override
    public void render(GuiGraphics g) {
        if (mc.player == null) return;
        text(g, String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()), 0, 0);
    }
}
