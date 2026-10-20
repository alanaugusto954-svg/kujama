package kujama.modules;

import kujama.Draw;
import kujama.HudModule;
import kujama.Kujama;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;

/** Barra superior en forma de pildora: logo K, nombre, FPS y tiempo de sesion. */
public class PillModule extends HudModule {
    private int frames, fps;
    private long last = System.currentTimeMillis();

    public PillModule() {
        super("Barra", Items.NAME_TAG, -1, 6); // x = -1: se centra la primera vez
    }

    private static String sessionTime() {
        long s = Math.max(0, (System.currentTimeMillis() - Kujama.sessionStartMs) / 1000);
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, sec) : String.format("%02d:%02d", m, sec);
    }

    @Override
    public void render(GuiGraphics g) {
        frames++;
        long now = System.currentTimeMillis();
        if (now - last >= 1000) {
            fps = frames;
            frames = 0;
            last = now;
        }
        String name = mc.getUser().getName();
        String f = fps + " fps";
        String t = sessionTime();

        int pad = 6, gap = 7, logo = 11;
        int total = pad + logo + gap + mc.font.width(name) + gap + 1 + gap + mc.font.width(f) + gap + 1 + gap
                + mc.font.width(t) + pad;
        int height = 17;
        if (x < 0) x = Math.max(0, (g.guiWidth() - total) / 2);

        Draw.roundRect(g, x - 1, y - 1, total + 2, height + 2, 6, 0xFF2C313B);
        Draw.roundRect(g, x, y, total, height, 5, 0xE0111419);

        int cx = x + pad;
        Draw.roundRect(g, cx, y + 3, logo, logo, 3, 0xFF1BD96A);
        Draw.pixelGlyph(g, 'K', cx + 3, y + 5, 1, 0xFF0B1F12);
        cx += logo + gap;
        g.drawString(mc.font, name, cx, y + 4, 0xFFFFFFFF, false);
        cx += mc.font.width(name) + gap;
        g.fill(cx, y + 4, cx + 1, y + 13, 0xFF3A404B);
        cx += 1 + gap;
        g.drawString(mc.font, f, cx, y + 4, 0xFF1BD96A, false);
        cx += mc.font.width(f) + gap;
        g.fill(cx, y + 4, cx + 1, y + 13, 0xFF3A404B);
        cx += 1 + gap;
        g.drawString(mc.font, t, cx, y + 4, 0xFFB4BAC4, false);

        track(0, 0, total, height);
    }
}
