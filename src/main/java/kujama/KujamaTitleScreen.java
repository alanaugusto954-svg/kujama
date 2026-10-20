package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

/**
 * Menu principal oscuro de Kujama: fondo con cortinas de luz y estrellas de pixeles, logo con brillo,
 * lista de opciones que reacciona al mouse y un emblema con alas rojas que aletean.
 * Los botones son los de siempre (un jugador, multijugador, ajustes, salir) mas el menu del cliente.
 */
public class KujamaTitleScreen extends Screen {
    private static final String TITLE = "KUJAMA";
    private static final String[] LABELS = {"Un jugador", "Multijugador", "Ajustes de Minecraft", "Cliente", "Salir"};
    private static final int ROW_H = 22, ROW_W = 170;

    private final Starfield stars = new Starfield(90, 5L);
    private final long openMs = System.currentTimeMillis();
    private final float[] hover = new float[LABELS.length];
    private long lastNano;
    private int mx, logoSize, logoY, menuY;

    public KujamaTitleScreen() {
        super(Component.literal("Kujama"));
    }

    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override
    protected void init() {
        mx = Math.max(16, (int) (width * 0.09f));
        logoSize = Math.max(34, height / 7);
        logoY = (int) (height * 0.12f);
        menuY = Math.max(logoY + logoSize + 60, (int) (height * 0.46f));

        for (int i = 0; i < LABELS.length; i++) {
            final int index = i;
            addRenderableWidget(Button.builder(Component.empty(), b -> action(index))
                    .bounds(mx - 8, menuY + i * ROW_H, ROW_W, 20).build());
        }
        addRenderableWidget(Button.builder(Component.empty(), b -> TitleController.openVanilla(minecraft))
                .bounds(width - 100, height - 18, 90, 14).build());
    }

    private void action(int i) {
        switch (i) {
            case 0 -> minecraft.setScreen(new SelectWorldScreen(this));
            case 1 -> minecraft.setScreen(new JoinMultiplayerScreen(this));
            case 2 -> minecraft.setScreen(new OptionsScreen(this, minecraft.options));
            case 3 -> minecraft.setScreen(new KujamaMenuScreen(this));
            default -> minecraft.stop();
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // fondo propio (se dibuja en render)
    }

    private static boolean in(int px, int py, int x, int y, int w, int h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    private static int shade(int rgb, float f) {
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * f));
        int g = Math.min(255, (int) (((rgb >> 8) & 255) * f));
        int b = Math.min(255, (int) ((rgb & 255) * f));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);   // los botones invisibles reciben los clics; lo visible lo dibujamos encima
        if (Safe.disabled("title-ui")) {
            TitleController.openVanilla(minecraft);
            return;
        }
        Safe.run("title-ui", () -> draw(g, mouseX, mouseY));
    }

    private void draw(GuiGraphics g, int mouseX, int mouseY) {
        long now = System.nanoTime();
        float dt = lastNano == 0 ? 0.016f : Math.min(0.1f, (now - lastNano) / 1_000_000_000f);
        lastNano = now;
        float t = (System.currentTimeMillis() - openMs) / 1000f;

        // fondo oscuro con degradado
        g.fill(0, 0, width, height, 0xFF07090C);
        for (int i = 0; i < 16; i++) {
            g.fill(0, height * i / 16, width, height * (i + 1) / 16, Anim.alpha(0xFF12171F, i / 16f * 0.9f));
        }
        // cortinas de luz que se mueven
        for (int k = 0; k < 6; k++) {
            int cx = (int) (width * (0.35f + 0.1f * k) + Math.sin(t * 0.4f + k * 1.3f) * width * 0.05f);
            int w = 6 + (int) (10 * (0.5f + 0.5f * Math.sin(t * 0.6f + k)));
            g.fill(cx, 0, cx + w, height, Anim.alpha(0xFF1BD96A, 0.045f));
            g.fill(cx + w, 0, cx + w + 2, height, Anim.alpha(0xFFFFFFFF, 0.03f));
        }
        stars.draw(g, width, height, t, 1f);

        drawEmblem(g, t);

        // logo con brillo
        float pulse = 0.5f + 0.5f * (float) Math.sin(t * 2f);
        for (int k = 0; k < 3; k++) {
            int r = (int) (logoSize * (0.70f + 0.22f * k + 0.04f * pulse));
            Draw.circle(g, mx + logoSize / 2, logoY + logoSize / 2, r, Anim.alpha(0xFF1BD96A, 0.05f));
        }
        Draw.logo(g, mx, logoY, logoSize, 1f);

        // titulo de pixeles
        int px = Math.max(2, height / 120);
        int ty = logoY + logoSize + 8;
        for (int i = 0; i < TITLE.length(); i++) {
            Draw.pixelGlyph(g, TITLE.charAt(i), mx + i * 6 * px + px, ty + px, px, 0xCC0B3A20);
            Draw.pixelGlyph(g, TITLE.charAt(i), mx + i * 6 * px, ty, px, 0xFF1BD96A);
        }
        g.drawString(font, "CLIENT  -  1.21.11", mx, ty + 7 * px + 6, 0xFFB4BAC4, false);

        // lista de opciones
        for (int i = 0; i < LABELS.length; i++) {
            int ry = menuY + i * ROW_H;
            boolean hov = in(mouseX, mouseY, mx - 8, ry, ROW_W, 20);
            hover[i] = Anim.approach(hover[i], hov ? 1f : 0f, dt, 14f);
            float h = hover[i];
            Draw.roundRect(g, mx - 8, ry, ROW_W, 20, 4, Anim.alpha(0xFFFFFFFF, 0.07f * h));
            int bar = Math.round(14 * h);
            g.fill(mx - 8, ry + 10 - bar / 2, mx - 8 + 2 + Math.round(h * 2), ry + 10 + bar / 2, 0xFF1BD96A);
            int base = i == LABELS.length - 1 ? 0xFFE08A9B : 0xFFB4BAC4;
            g.drawString(font, LABELS[i], mx + 4 + Math.round(h * 6), ry + 6, Anim.mix(base, 0xFFFFFFFF, h), false);
        }

        // pie y datos
        g.drawString(font, "Kujama Client  |  Minecraft 1.21.11", mx, height - 14, 0xFF6B717C, false);
        boolean hv = in(mouseX, mouseY, width - 100, height - 18, 90, 14);
        g.drawString(font, "Menu original", width - 96, height - 14, hv ? 0xFFFFFFFF : 0xFF6B717C, false);
        String name = minecraft.getUser().getName();
        g.drawString(font, name, width - font.width(name) - 12, 10, 0xFFB4BAC4, false);

        // entrada desde negro
        float fadeIn = 1f - Anim.smoothstep(0f, 0.6f, t);
        if (fadeIn > 0.01f) g.fill(0, 0, width, height, Anim.alpha(0xFF000000, fadeIn));
    }

    /** Emblema: alas rojas (las mismas del juego, en 2D) que aletean alrededor del logo. */
    private void drawEmblem(GuiGraphics g, float t) {
        float appear = Anim.smoothstep(0.2f, 1.2f, t);
        int ex = (int) (width * 0.70f);
        int ey = (int) (height * 0.50f + Math.sin(t * 1.2f) * 3f);
        float s = height * 0.30f;

        for (int k = 0; k < 4; k++) {
            Draw.circle(g, ex, ey, (int) (height * (0.18f + 0.06f * k)), Anim.alpha(0xFFB3122B, 0.05f * appear));
        }

        double theta = Math.sin(t * 1.6f) * 0.22;
        double ct = Math.cos(theta), st = Math.sin(theta);
        for (int side = -1; side <= 1; side += 2) {
            double[] sh = tr(KujamaWingsLayer.S, side, ex, ey, s, ct, st);
            double[][] rim = new double[KujamaWingsLayer.RIM.length][];
            for (int i = 0; i < rim.length; i++) rim[i] = tr(KujamaWingsLayer.RIM[i], side, ex, ey, s, ct, st);

            for (int i = 0; i + 1 < rim.length; i++) {
                float f = (KujamaWingsLayer.SHADE[i] + KujamaWingsLayer.SHADE[i + 1]) / 2f;
                Draw.polygon(g, new double[][]{sh, rim[i], rim[i + 1]}, Anim.alpha(shade(0xB3122B, f), appear));
            }
            int bone = Anim.alpha(0xFF3A0610, appear);
            for (double[] tip : KujamaWingsLayer.TIPS) {
                double[] tp = tr(tip, side, ex, ey, s, ct, st);
                double[] mid = {(sh[0] + tp[0]) / 2.0, (sh[1] + tp[1]) / 2.0 - s * 0.06};
                Draw.line(g, sh[0], sh[1], mid[0], mid[1], 2, bone);
                Draw.line(g, mid[0], mid[1], tp[0], tp[1], 2, bone);
            }
            for (int i = 0; i + 1 < rim.length; i++) {
                Draw.line(g, rim[i][0], rim[i][1], rim[i + 1][0], rim[i + 1][1], 1, bone);
            }
        }
        int logo = (int) (height * 0.13f);
        Draw.logo(g, ex - logo / 2, ey - logo / 2 - (int) (s * 0.02f), logo, appear);
    }

    /** Punto del ala (lateral, altura) -> pantalla, con el aleteo (rotacion alrededor del hombro). */
    private static double[] tr(double[] p, int side, int ex, int ey, float s, double ct, double st) {
        double xw = p[0], yw = p[1] - KujamaWingsLayer.V0;
        double xp = xw * ct - yw * st;
        double yp = xw * st + yw * ct;
        return new double[]{ex + side * xp * s, ey - yp * s};
    }
}
