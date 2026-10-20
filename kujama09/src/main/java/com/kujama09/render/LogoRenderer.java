package com.kujama09.render;

import com.kujama09.anim.Colors;
import com.kujama09.anim.Easing;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix3x2fStack;

/**
 * Logotipo "kujama09" (siempre en minúsculas) dibujado como texto escalado en capas:
 * resplandor azul, contorno negro, extrusión naranja y cara amarilla, con un brillo opcional que
 * lo atraviesa. No usa texturas, así que escala nítidamente a cualquier resolución.
 */
public final class LogoRenderer {
    public static final Text LOGO = Text.literal("kujama09").formatted(Formatting.BOLD);

    private static final float[][] OUTLINE = {
            {-1.5f, 0f}, {1.5f, 0f}, {0f, -1.5f}, {0f, 1.5f},
            {-1.1f, -1.1f}, {1.1f, -1.1f}, {-1.1f, 1.1f}, {1.1f, 1.1f},
            {-1.5f, 2f}, {0f, 2.3f}, {1.5f, 2f}
    };

    private LogoRenderer() {
    }

    public static int textWidth(TextRenderer textRenderer) {
        return textRenderer.getWidth(LOGO);
    }

    /**
     * @param cx    centro horizontal en píxeles GUI
     * @param cy    centro vertical en píxeles GUI
     * @param scale factor de escala del texto
     * @param alpha opacidad 0..1
     * @param shine posición del brillo 0..1 (de izquierda a derecha) o un valor negativo para ocultarlo
     */
    public static void draw(DrawContext context, TextRenderer textRenderer, float cx, float cy,
                            float scale, float alpha, float shine) {
        if (alpha <= 0.02f) {
            return;
        }
        float w = textWidth(textRenderer) * scale;
        float h = 9f * scale;
        float left = cx - w / 2f;
        float top = cy - h / 2f;

        int glow = Colors.withAlpha(0x554DABF7, alpha);
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2.0;
            layer(context, textRenderer, left, top, scale,
                    (float) Math.cos(angle) * 2.5f, (float) Math.sin(angle) * 2.5f, glow);
        }
        int black = Colors.withAlpha(Colors.BLACK, alpha);
        for (float[] o : OUTLINE) {
            layer(context, textRenderer, left, top, scale, o[0], o[1], black);
        }
        layer(context, textRenderer, left, top, scale, 0f, 1.2f, Colors.withAlpha(Colors.DEEP_ORANGE, alpha));
        layer(context, textRenderer, left, top, scale, 0f, 0f, Colors.withAlpha(Colors.YELLOW, alpha));

        if (shine >= 0f && shine <= 1f) {
            float bandW = w * 0.16f;
            float center = Easing.lerp(left - bandW, left + w + bandW, shine);
            float[] widths = {1f, 0.55f, 0.25f};
            float[] alphas = {0.30f, 0.35f, 0.50f};
            int y1 = Math.round(top - 3f * scale);
            int y2 = Math.round(top + h + 3f * scale);
            for (int i = 0; i < widths.length; i++) {
                float half = bandW * widths[i] / 2f;
                int x1 = Math.max(Math.round(left), Math.round(center - half));
                int x2 = Math.min(Math.round(left + w), Math.round(center + half));
                if (x2 <= x1) {
                    continue;
                }
                context.enableScissor(x1, y1, x2, y2);
                layer(context, textRenderer, left, top, scale, 0f, 0f,
                        Colors.withAlpha(Colors.WHITE, alpha * alphas[i]));
                context.disableScissor();
            }
        }
    }

    private static void layer(DrawContext context, TextRenderer textRenderer, float left, float top,
                              float scale, float dx, float dy, int color) {
        // TextRenderer trata alfa < 4 como opaco, así que se omite en vez de dibujar un fantasma.
        if ((color >>> 24) < 5) {
            return;
        }
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(left + dx * scale, top + dy * scale);
        matrices.scale(scale, scale);
        context.drawText(textRenderer, LOGO, 0, 0, color, false);
        matrices.popMatrix();
    }
}
