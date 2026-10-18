package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;

/** Modulo que dibuja algo en pantalla en la posicion (x, y). */
public abstract class HudModule extends Module {
    public int x, y;

    protected HudModule(String name, Item icon, int x, int y) {
        super(name, Category.HUD, icon, true);
        this.x = x;
        this.y = y;
    }

    public abstract void render(GuiGraphics g);

    protected void text(GuiGraphics g, String s, int dx, int dy) {
        text(g, s, dx, dy, 0xFFFFFFFF);
    }

    protected void text(GuiGraphics g, String s, int dx, int dy, int argb) {
        g.drawString(mc.font, s, x + dx, y + dy, argb, true);
    }
}
