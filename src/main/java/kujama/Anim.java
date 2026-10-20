package kujama;

/** Utilidades de animacion: suavizados, interpolaciones y colores. */
public final class Anim {
    private Anim() {}

    public static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }

    public static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1f - t;
        return 1f - u * u * u;
    }

    public static float easeInOutCubic(float t) {
        t = clamp01(t);
        if (t < 0.5f) return 4f * t * t * t;
        float u = -2f * t + 2f;
        return 1f - (u * u * u) / 2f;
    }

    public static float smoothstep(float a, float b, float x) {
        float t = clamp01((x - a) / (b - a));
        return t * t * (3f - 2f * t);
    }

    /** Acerca v a target de forma exponencial; no depende de los FPS. */
    public static float approach(float v, float target, float dtSeconds, float speed) {
        return v + (target - v) * (1f - (float) Math.exp(-dtSeconds * speed));
    }

    public static float wrapDegrees(float d) {
        d %= 360f;
        if (d >= 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return d;
    }

    public static float lerpAngle(float a, float b, float t) { return a + wrapDegrees(b - a) * t; }

    /** Multiplica el alfa de un color ARGB. */
    public static int alpha(int argb, float mul) {
        int a = (int) (((argb >>> 24) & 255) * clamp01(mul));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    /** Igual que alpha, pero nunca llega a 0 (el texto con alfa 0 se dibuja opaco en 1.21). */
    public static int textAlpha(int argb, float mul) {
        int a = (int) (((argb >>> 24) & 255) * clamp01(mul));
        if (a < 6) a = 6;
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int mix(int c1, int c2, float t) {
        t = clamp01(t);
        int a = (int) lerp((c1 >>> 24) & 255, (c2 >>> 24) & 255, t);
        int r = (int) lerp((c1 >> 16) & 255, (c2 >> 16) & 255, t);
        int g = (int) lerp((c1 >> 8) & 255, (c2 >> 8) & 255, t);
        int b = (int) lerp(c1 & 255, c2 & 255, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
