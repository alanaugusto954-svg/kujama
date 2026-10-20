package kujama;

import net.minecraft.client.gui.GuiGraphics;

import java.util.Random;

/** Campo de "estrellas" de pixeles que suben lento y titilan (estilo retro). */
public final class Starfield {
    private static final int[] PALETTE = {0xFFFFFFFF, 0xFF1BD96A, 0xFFFFE14D, 0xFF2EE6D6};
    private final int n;
    private final float[] x, y, speed, phase;
    private final int[] size, color;

    public Starfield(int n, long seed) {
        this.n = n;
        x = new float[n]; y = new float[n]; speed = new float[n]; phase = new float[n];
        size = new int[n]; color = new int[n];
        Random r = new Random(seed);
        for (int i = 0; i < n; i++) {
            x[i] = r.nextFloat();
            y[i] = r.nextFloat();
            speed[i] = 0.2f + r.nextFloat() * 0.8f;
            phase[i] = r.nextFloat() * 6.28f;
            size[i] = r.nextInt(4) == 0 ? 2 : 1;
            color[i] = PALETTE[r.nextInt(PALETTE.length)];
        }
    }

    public void draw(GuiGraphics g, int w, int h, float t, float alpha) {
        for (int i = 0; i < n; i++) {
            float yy = ((y[i] - t * speed[i] * 0.03f) % 1f + 1f) % 1f;
            int px = (int) (x[i] * w), py = (int) (yy * h);
            float tw = 0.5f + 0.5f * (float) Math.sin(t * 1.5f + phase[i]);
            g.fill(px, py, px + size[i], py + size[i], Anim.alpha(color[i], alpha * (0.2f + 0.8f * tw)));
        }
    }
}
