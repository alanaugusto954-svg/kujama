package kujama.modules;

import net.minecraft.client.gui.GuiGraphics;
import kujama.HudModule;
import net.minecraft.world.item.Items;

/** Marca de Kujama en pantalla. */
public class WatermarkModule extends HudModule {
    public WatermarkModule() { super("Marca", Items.NETHER_STAR, 4, 4); }

    @Override
    public void render(GuiGraphics g) {
        text(g, "Kujama", 0, 0, 0xFF1BD96A);
        text(g, "Client", mc.font.width("Kujama ") , 0, 0xFFFFFFFF);
    }
}
