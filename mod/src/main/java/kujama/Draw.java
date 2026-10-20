package kujama;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    /** Tramos horizontales {y, x1, x2} que cubren un poligono (relleno por lineas de barrido). */
    public static List<int[]> scanlineSpans(double[][] pts) {
        List<int[]> out = new ArrayList<>();
        if (pts.length < 3) return out;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (double[] p : pts) {
            minY = Math.min(minY, p[1]);
            maxY = Math.max(maxY, p[1]);
        }
        double[] xs = new double[pts.length];
        for (int y = (int) Math.floor(minY); y < (int) Math.ceil(maxY); y++) {
            double sy = y + 0.5;
            int n = 0;
            for (int i = 0; i < pts.length; i++) {
                double[] a = pts[i], b = pts[(i + 1) % pts.length];
                if ((a[1] <= sy && b[1] > sy) || (b[1] <= sy && a[1] > sy)) {
                    xs[n++] = a[0] + (sy - a[1]) / (b[1] - a[1]) * (b[0] - a[0]);
                }
            }
            Arrays.sort(xs, 0, n);
            for (int i = 0; i + 1 < n; i += 2) {
                int x1 = (int) Math.round(xs[i]), x2 = (int) Math.round(xs[i + 1]);
                if (x2 > x1) out.add(new int[]{y, x1, x2});
            }
        }
        return out;
    }

    public static void polygon(GuiGraphics g, double[][] pts, int color) {
        for (int[] sp : scanlineSpans(pts)) g.fill(sp[1], sp[0], sp[2], sp[0] + 1, color);
    }

    public static void line(GuiGraphics g, double x1, double y1, double x2, double y2, int thick, int color) {
        double dx = x2 - x1, dy = y2 - y1;
        int steps = (int) Math.max(1, Math.ceil(Math.hypot(dx, dy)));
        int hl = thick / 2;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int x = (int) Math.round(x1 + dx * t), y = (int) Math.round(y1 + dy * t);
            g.fill(x - hl, y - hl, x - hl + thick, y - hl + thick, color);
        }
    }

    /** Logo de Kujama: cuadrado verde redondeado con una K de pixeles. */
    public static void logo(GuiGraphics g, int x, int y, int size, float alpha) {
        roundRect(g, x, y, size, size, Math.max(2, size / 4), Anim.alpha(0xFF1BD96A, alpha));
        int px = Math.max(1, size / 9);
        pixelGlyph(g, 'K', x + (size - 5 * px) / 2, y + (size - 7 * px) / 2, px, Anim.alpha(0xFF0B1F12, alpha));
    }

    /** Circulo relleno (rectangulo redondeado con radio maximo). */
    public static void circle(GuiGraphics g, int cx, int cy, int r, int color) {
        roundRect(g, cx - r, cy - r, r * 2, r * 2, r, color);
    }
}
