package com.kujama09.anim;

/** Utilidades para colores ARGB empaquetados en un int. */
public final class Colors {
    public static final int YELLOW = 0xFFFFD43B;
    public static final int ORANGE = 0xFFFF8F1F;
    public static final int DEEP_ORANGE = 0xFFE8590C;
    public static final int SKY_BLUE = 0xFF4DABF7;
    public static final int NAVY = 0xFF0F2557;
    public static final int BLACK = 0xFF000000;
    public static final int WHITE = 0xFFFFFFFF;

    private Colors() {
    }

    /** Multiplica el canal alfa por {@code mult} (0..1). */
    public static int withAlpha(int argb, float mult) {
        int a = Math.round(((argb >>> 24) & 0xFF) * Easing.clamp01(mult));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    public static int lerp(int c0, int c1, float t) {
        t = Easing.clamp01(t);
        int a = channel(c0, c1, 24, t);
        int r = channel(c0, c1, 16, t);
        int g = channel(c0, c1, 8, t);
        int b = channel(c0, c1, 0, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int channel(int c0, int c1, int shift, float t) {
        int a = (c0 >>> shift) & 0xFF;
        int b = (c1 >>> shift) & 0xFF;
        return Math.round(a + (b - a) * t);
    }
}
