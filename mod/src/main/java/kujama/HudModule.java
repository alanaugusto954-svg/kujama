package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;

/** Modulo que dibuja algo en pantalla en la posicion (x, y). Guarda su tamano para el editor de HUD. */
public abstract class HudModule extends Module {
    public int x, y;
    /** Tamano del ultimo cuadro dibujado (lo usa el editor para saber donde hacer clic). */
    public int w, h;

    protected HudModule(String name, Item icon, int x, int y) {
        super(name, Category.HUD, icon, true);
        this.x = x;
        this.y = y;
    }

    public abstract void render(GuiGraphics g);

    public void beginFrame() {
        w = 0;
        h = 0;
    }

    protected void track(int dx, int dy, int tw, int th) {
        w = Math.max(w, dx + tw);
        h = Math.max(h, dy + th);
    }

    protected void text(GuiGraphics g, String s, int dx, int dy) {
        text(g, s, dx, dy, 0xFFFFFFFF);
    }

    protected void text(GuiGraphics g, String s, int dx, int dy, int argb) {
        g.drawString(mc.font, s, x + dx, y + dy, argb, true);
        track(dx, dy, mc.font.width(s), 9);
    }
}
