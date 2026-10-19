package kujama;

import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;
import java.util.Map;

/** Dibujo 2D sin texturas: rectangulos redondeados y letras de pixeles (estilo retro). */
public final class Draw {
    private Draw() {}

    public static void roundRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        g.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(Math.max(0.0, (double) r * r - dy * dy)));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            g.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
    }

    // Letras 5x7 (solo las que usa el titulo)
    private static final Map<Character, String[]> GLYPHS = new HashMap<>();

    static {
        GLYPHS.put('K', new String[]{"10001", "10010", "10100", "11000", "10100", "10010", "10001"});
        GLYPHS.put('U', new String[]{"10001", "10001", "10001", "10001", "10001", "10001", "01110"});
        GLYPHS.put('J', new String[]{"00111", "00010", "00010", "00010", "00010", "10010", "01100"});
        GLYPHS.put('A', new String[]{"01110", "10001", "10001", "11111", "10001", "10001", "10001"});
        GLYPHS.put('M', new String[]{"10001", "11011", "10101", "10101", "10001", "10001", "10001"});
    }

    public static void pixelGlyph(GuiGraphics g, char c, int x, int y, int px, int color) {
        String[] rows = GLYPHS.get(Character.toUpperCase(c));
        if (rows == null) return;
        for (int r = 0; r < rows.length; r++) {
            for (int col = 0; col < 5; col++) {
                if (rows[r].charAt(col) == '1') {
                    g.fill(x + col * px, y + r * px, x + (col + 1) * px, y + (r + 1) * px, color);
                }
            }
        }
    }
}
