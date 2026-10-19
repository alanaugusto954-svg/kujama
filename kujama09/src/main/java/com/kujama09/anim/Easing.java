package com.kujama09.anim;

/** Funciones de interpolación. Todas reciben tiempo normalizado (0..1). */
public final class Easing {
    private Easing() {
    }

    public static float clamp01(float v) {
        return v < 0f ? 0f : Math.min(v, 1f);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float smooth(float t) {
        t = clamp01(t);
        return t * t * (3f - 2f * t);
    }

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1f - t;
        return 1f - u * u * u;
    }

    public static float easeInCubic(float t) {
        t = clamp01(t);
        return t * t * t;
    }

    public static float easeInOutCubic(float t) {
        t = clamp01(t);
        if (t < 0.5f) {
            return 4f * t * t * t;
        }
        float u = -2f * t + 2f;
        return 1f - (u * u * u) / 2f;
    }

    public static float easeOutBack(float t) {
        t = clamp01(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }
}
