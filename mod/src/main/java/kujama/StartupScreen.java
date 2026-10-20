package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Intro de inicio: se ve al abrir Minecraft, antes del menu principal.
 * El logo K aparece con ondas de luz, el titulo se arma letra por letra y una barra se llena.
 * Cualquier tecla o clic la salta.
 */
public class StartupScreen extends Screen {
    private static final float TOTAL = 4.4f;
    private static final float SKIP_TIME = 0.35f;
    private static final String TITLE = "KUJAMA";

    private final Starfield stars = new Starfield(70, 11L);
    private final long startMs = System.currentTimeMillis();
    private long skipMs;
    private boolean skipping, done;

    public StartupScreen() {
        super(Component.literal("Kujama"));
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    private float elapsed() { return (System.currentTimeMillis() - startMs) / 1000f; }

    private void skip() {
        if (skipping || done) return;
        skipping = true;
        skipMs = System.currentTimeMillis();
    }

    private void finish() {
        if (done) return;
        done = true;
        TitleController.openTitle(minecraft);
    }

    @Override
    public void tick() {
        if (elapsed() >= TOTAL || (skipping && System.currentTimeMillis() - skipMs > SKIP_TIME * 1000f)) finish();
    }

    @Override public boolean keyPressed(KeyEvent event) { skip(); return true; }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { skip(); return true; }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // fondo propio
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        if (Safe.disabled("startup-ui")) {
            finish();
            return;
        }
        Safe.run("startup-ui", () -> draw(g));
    }

    private void draw(GuiGraphics g) {
        float t = elapsed();
        float out = skipping ? Anim.clamp01((System.currentTimeMillis() - skipMs) / 1000f / SKIP_TIME) : 0f;
        float vis = 1f - out;

        g.fill(0, 0, width, height, 0xFF07090C);
        for (int i = 0; i < 16; i++) {   // degradado suave hacia abajo
            g.fill(0, height * i / 16, width, height * (i + 1) / 16, Anim.alpha(0xFF12171F, i / 16f * 0.9f));
        }
        stars.draw(g, width, height, t, 0.8f * vis);

        // logo con ondas de luz
        int size = Math.max(48, height / 5);
        int cx = width / 2, cy = (int) (height * 0.36f);
        float appear = Anim.easeOutCubic((t - 0.4f) / 1.0f);
        for (int k = 0; k < 3; k++) {
            float ph = (t * 0.7f + k / 3f) % 1f;
            int r = (int) (size * (0.55f + ph * 1.2f));
            Draw.circle(g, cx, cy, r, Anim.alpha(0xFF1BD96A, (1f - ph) * 0.10f * appear * vis));
        }
        int s = Math.max(2, (int) (size * appear));
        Draw.logo(g, cx - s / 2, cy - s / 2, s, vis);

        // titulo letra por letra
        int px = Math.max(3, height / 90);
        int step = 6 * px;
        int x0 = (width - (TITLE.length() * step - px)) / 2;
        int y0 = cy + size / 2 + 16;
        for (int i = 0; i < TITLE.length(); i++) {
            float a = Anim.smoothstep(1.2f + 0.14f * i, 1.8f + 0.14f * i, t) * vis;
            if (a <= 0.01f) continue;
            int dy = Math.round((1f - a) * 8f);
            Draw.pixelGlyph(g, TITLE.charAt(i), x0 + i * step + px, y0 + px + dy, px, Anim.alpha(0xFF0B3A20, a * 0.8f));
            Draw.pixelGlyph(g, TITLE.charAt(i), x0 + i * step, y0 + dy, px, Anim.alpha(0xFF1BD96A, a));
        }
        float sub = Anim.smoothstep(2.2f, 2.9f, t) * vis;
        if (sub > 0.02f) {
            g.drawCenteredString(font, "C L I E N T", width / 2, y0 + 7 * px + 10, Anim.textAlpha(0xFFFFFFFF, sub));
        }

        // barra de carga
        int bw = Math.min(width / 3, 240), bx = (width - bw) / 2, by = (int) (height * 0.80f);
        float bar = Anim.easeInOutCubic((t - 0.8f) / 2.8f);
        float barA = Anim.smoothstep(0.8f, 1.4f, t) * vis;
        g.fill(bx, by, bx + bw, by + 3, Anim.alpha(0x33FFFFFF, barA));
        g.fill(bx, by, bx + (int) (bw * bar), by + 3, Anim.alpha(0xFF1BD96A, barA));
        g.drawCenteredString(font, bar >= 0.99f ? "Listo" : "Iniciando...", width / 2, by + 9,
                Anim.textAlpha(0xFFB4BAC4, barA));

        // fundido final a negro (el menu principal entra desde negro)
        float fade = Anim.smoothstep(TOTAL - 0.6f, TOTAL, t);
        if (fade > 0.01f) g.fill(0, 0, width, height, Anim.alpha(0xFF000000, fade));
    }
}
