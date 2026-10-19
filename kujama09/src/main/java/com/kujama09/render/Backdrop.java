package com.kujama09.render;

import com.kujama09.anim.ParticleField;
import com.kujama09.config.KujamaConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Dibujo del escenario compartido por la intro, el menú y los ajustes. Las texturas se declaran como
 * Identifier estáticos: el TextureManager de Minecraft las carga una sola vez y las reutiliza,
 * así que no hay cargas repetidas ni fugas.
 */
public final class Backdrop {
    public static final Identifier BACKGROUND = Identifier.of("kujama09", "textures/gui/background.png");
    public static final Identifier CLOUDS = Identifier.of("kujama09", "textures/gui/clouds.png");
    public static final Identifier MASCOT = Identifier.of("kujama09", "textures/gui/mascot.png");

    // Dimensiones reales de los PNG (deben coincidir con los archivos de resources).
    private static final int BG_W = 1920;
    private static final int BG_H = 1080;
    private static final int CLOUDS_W = 2048;
    private static final int CLOUDS_H = 256;
    private static final int MASCOT_SIZE = 512;

    private Backdrop() {
    }

    /**
     * Fondo + nubes en movimiento + viñeta.
     *
     * @param time      segundos transcurridos (para el desplazamiento de las nubes)
     * @param zoom      1.0 = sin zoom; la intro lo aumenta ligeramente
     * @param intensity intensidad de animación configurada (0..1)
     */
    public static void drawScene(DrawContext context, int width, int height, float time, float zoom,
                                 float intensity, KujamaConfig.Quality quality, boolean clouds) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        if (zoom != 1f) {
            matrices.translate(width / 2f, height / 2f, 0f);
            matrices.scale(zoom, zoom, 1f);
            matrices.translate(-width / 2f, -height / 2f, 0f);
        }
        drawCover(context, width, height);
        if (clouds && quality != KujamaConfig.Quality.LOW) {
            drawClouds(context, width, height, time, intensity);
        }
        matrices.pop();

        // Viñeta suave para que los botones y el logotipo destaquen.
        context.fillGradient(0, 0, width, height / 4, 0x40000000, 0x00000000);
        context.fillGradient(0, height * 55 / 100, width, height, 0x00000000, 0x70000000);
    }

    /** Dibuja el fondo cubriendo toda la pantalla sin deformarlo (recorte centrado). */
    private static void drawCover(DrawContext context, int width, int height) {
        float screenAspect = (float) width / (float) height;
        float texAspect = (float) BG_W / (float) BG_H;
        int regionW;
        int regionH;
        int u;
        int v;
        if (screenAspect > texAspect) {
            regionW = BG_W;
            regionH = Math.round(BG_W / screenAspect);
            u = 0;
            v = (int) ((BG_H - regionH) * 0.6f);
        } else {
            regionH = BG_H;
            regionW = Math.round(BG_H * screenAspect);
            u = (BG_W - regionW) / 2;
            v = 0;
        }
        context.drawTexture(BACKGROUND, 0, 0, width, height, (float) u, (float) v,
                regionW, regionH, BG_W, BG_H);
    }

    private static void drawClouds(DrawContext context, int width, int height, float time, float intensity) {
        int ch = Math.max(24, Math.round(height * 0.30f));
        int cw = ch * (CLOUDS_W / CLOUDS_H);
        int y = Math.round(height * 0.02f);
        float offset = (time * 8f * intensity) % cw;

        RenderSystem.enableBlend();
        context.setShaderColor(1f, 1f, 1f, 0.92f);
        for (float x = -offset; x < width; x += cw) {
            context.drawTexture(CLOUDS, Math.round(x), y, cw + 1, ch, 0f, 0f,
                    CLOUDS_W, CLOUDS_H, CLOUDS_W, CLOUDS_H);
        }
        context.setShaderColor(1f, 1f, 1f, 1f);
    }

    /** Partículas encima del escenario (si están activadas en la configuración). */
    public static void drawParticles(DrawContext context, ParticleField field, int width, int height,
                                     KujamaConfig config, float alphaMultiplier) {
        if (config.particles) {
            field.render(context, width, height, config.animationIntensity, config.quality, alphaMultiplier);
        }
    }

    /** Sombra elíptica en el suelo + mascota. {@code size} es el lado del cuadrado en píxeles GUI. */
    public static void drawMascot(DrawContext context, int x, int y, int size, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        // Sombra: elipse aproximada con franjas horizontales.
        int shadowCx = x + size / 2;
        int shadowY = y + Math.round(size * 0.93f);
        int half = Math.round(size * 0.30f);
        for (int i = 0; i < 6; i++) {
            int w = Math.round(half * (1f - i / 7f));
            int col = ((int) (0x55 * alpha) << 24);
            context.fill(shadowCx - w, shadowY + i, shadowCx + w, shadowY + i + 1, col);
        }

        RenderSystem.enableBlend();
        context.setShaderColor(1f, 1f, 1f, alpha);
        context.drawTexture(MASCOT, x, y, size, size, 0f, 0f,
                MASCOT_SIZE, MASCOT_SIZE, MASCOT_SIZE, MASCOT_SIZE);
        context.setShaderColor(1f, 1f, 1f, 1f);
    }
}
