package kujama;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;

/**
 * Intro cinematografica al entrar al mundo: fundido desde negro, barras de cine, titulo de pixeles
 * y una camara que orbita alrededor de tu personaje (se ven las alas) antes de volver a tu vista normal.
 * La camara es una entidad auxiliar (Marker) que existe solo en tu cliente; el mundo y el servidor no se enteran.
 * Cualquier tecla o clic la salta.
 */
public class IntroScreen extends Screen {
    private static final float ORBIT_END = 5.4f;
    private static final float END = 6.8f;
    private static final float SKIP_TIME = 0.35f;
    private static final String TITLE = "KUJAMA";

    private final Minecraft mc = Minecraft.getInstance();
    private Marker camera;
    private Entity previousCamera;
    private boolean started, finishing, done, prevHideGui;
    private long startMs, finishMs;
    private double[] lastPose = new double[5];
    private double[] skipFrom;

    public IntroScreen() {
        super(Component.literal("Kujama"));
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    private float elapsed() { return (System.currentTimeMillis() - startMs) / 1000f; }

    // ------------------------------------------------------------ camara

    @Override
    protected void init() {
        if (started) return;
        started = true;
        startMs = System.currentTimeMillis();
        prevHideGui = mc.options.hideGui;
        mc.options.hideGui = true;
        previousCamera = mc.getCameraEntity();
        camera = new Marker(EntityType.MARKER, mc.level);
        lastPose = pose(0f);
        place(lastPose, true);
        mc.setCameraEntity(camera);
    }

    private void place(double[] p, boolean snap) {
        if (!snap) camera.setOldPosAndRot();
        camera.setPos(p[0], p[1], p[2]);
        camera.setYRot((float) p[3]);
        camera.setXRot((float) p[4]);
        if (snap) camera.setOldPosAndRot();
    }

    private boolean solid(double x, double y, double z) {
        return mc.level.getBlockState(BlockPos.containing(x, y, z)).canOcclude();
    }

    private static double[] lookAt(double cx, double cy, double cz, double tx, double ty, double tz) {
        double dx = tx - cx, dy = ty - cy, dz = tz - cz;
        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        double pitch = -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        return new double[]{yaw, pitch};
    }

    /** Pose {x, y, z, yaw, pitch} de la camara en el instante t (segundos). */
    private double[] pose(float t) {
        LocalPlayer p = mc.player;
        double[] eye = {p.getX(), p.getEyeY(), p.getZ(), p.getYRot(), p.getXRot()};

        if (finishing) {
            float k = Anim.easeInOutCubic((System.currentTimeMillis() - finishMs) / 1000f / SKIP_TIME);
            return blend(skipFrom, eye, k);
        }
        if (t >= ORBIT_END) {
            float k = Anim.easeInOutCubic((t - ORBIT_END) / (END - ORBIT_END));
            return blend(orbit(ORBIT_END), eye, k);
        }
        return orbit(t);
    }

    /** Orbita: empieza a un costado, pasa por delante y termina detras del jugador (mostrando las alas). */
    private double[] orbit(float t) {
        LocalPlayer p = mc.player;
        float u = Anim.clamp01(t / ORBIT_END);
        float e = Anim.easeInOutCubic(u);
        double tx = p.getX(), tz = p.getZ(), py = p.getY();
        double ty = py + 1.15 + 0.25 * e;
        double frontPhi = Math.toRadians(p.getYRot() + 90.0);
        double phi = frontPhi - Math.PI * 0.5 + e * Math.PI * 1.5;
        double r = Anim.lerp(6.2f, 3.3f, e);
        double h = Anim.lerp(3.4f, 1.6f, e) + Math.sin(u * Math.PI) * 0.6;
        double cx = tx + Math.cos(phi) * r, cz = tz + Math.sin(phi) * r, cy = py + h;
        for (int i = 0; i < 6 && solid(cx, cy, cz); i++) {   // si hay un bloque en medio, acerca la camara
            r *= 0.85;
            cx = tx + Math.cos(phi) * r;
            cz = tz + Math.sin(phi) * r;
        }
        double[] look = lookAt(cx, cy, cz, tx, ty, tz);
        return new double[]{cx, cy, cz, look[0], look[1]};
    }

    private static double[] blend(double[] a, double[] b, float k) {
        return new double[]{
                a[0] + (b[0] - a[0]) * k, a[1] + (b[1] - a[1]) * k, a[2] + (b[2] - a[2]) * k,
                Anim.lerpAngle((float) a[3], (float) b[3], k), a[4] + (b[4] - a[4]) * k};
    }

    private void skip() {
        if (finishing || done) return;
        finishing = true;
        finishMs = System.currentTimeMillis();
        skipFrom = lastPose.clone();
    }

    private void finishNow() {
        if (done) return;
        done = true;
        mc.setScreen(null);
    }

    @Override
    public void tick() {
        if (mc.player == null || mc.level == null || camera == null) {
            finishNow();
            return;
        }
        float t = elapsed();
        if (finishing && (System.currentTimeMillis() - finishMs) / 1000f >= SKIP_TIME) {
            finishNow();
            return;
        }
        if (!finishing && t >= END) {
            finishNow();
            return;
        }
        Safe.run("intro", () -> {
            lastPose = pose(Math.min(t, END));
            place(lastPose, false);
        });
    }

    @Override
    public void removed() {
        if (camera == null) return;
        mc.setCameraEntity(previousCamera != null ? previousCamera : mc.player);
        mc.options.hideGui = prevHideGui;
        camera = null;
    }

    // ------------------------------------------------------------ entrada

    @Override
    public boolean keyPressed(KeyEvent event) {
        skip();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        skip();
        return true;
    }

    // ------------------------------------------------------------ dibujo

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // sin desenfoque: queremos ver el mundo
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        if (Safe.disabled("intro-ui")) return;
        Safe.run("intro-ui", () -> draw(g));
    }

    private void draw(GuiGraphics g) {
        float t = elapsed();
        float out = finishing ? Anim.clamp01((System.currentTimeMillis() - finishMs) / 1000f / SKIP_TIME) : 0f;
        float vis = 1f - out;

        // fundido desde negro
        float black = 1f - Anim.smoothstep(0f, 1.4f, t);
        if (black > 0.01f) g.fill(0, 0, width, height, Anim.alpha(0xFF000000, black));

        // viñeta
        for (int i = 0; i < 24; i++) {
            float a = (1f - i / 24f) * 0.45f * vis;
            int c = Anim.alpha(0xFF000000, a);
            g.fill(0, i * 2, width, i * 2 + 2, c);
            g.fill(0, height - i * 2 - 2, width, height - i * 2, c);
        }

        // barras de cine
        float bar;
        if (t < 1.6f) bar = Anim.lerp(0.5f, 0.11f, Anim.easeOutCubic(t / 1.6f));
        else if (t < ORBIT_END) bar = 0.11f;
        else bar = Anim.lerp(0.11f, 0f, Anim.easeInOutCubic((t - ORBIT_END) / (END - ORBIT_END)));
        bar *= vis;
        int bh = (int) (height * bar);
        g.fill(0, 0, width, bh, 0xFF000000);
        g.fill(0, height - bh, width, height, 0xFF000000);

        // titulo de pixeles, letra por letra
        int px = Math.max(2, height / 70);
        int letterW = 5 * px, step = 6 * px;
        int total = TITLE.length() * step - px;
        int x0 = (width - total) / 2;
        int y0 = (int) (height * 0.20f);
        float fadeOut = 1f - Anim.smoothstep(4.6f, 5.4f, t);
        for (int i = 0; i < TITLE.length(); i++) {
            float a = Anim.smoothstep(1.8f + 0.16f * i, 2.5f + 0.16f * i, t) * fadeOut * vis;
            if (a <= 0.01f) continue;
            int dy = Math.round((1f - a) * 8f);
            int lx = x0 + i * step;
            Draw.pixelGlyph(g, TITLE.charAt(i), lx + px, y0 + px + dy, px, Anim.alpha(0xFF0B3A20, a * 0.8f));   // sombra
            Draw.pixelGlyph(g, TITLE.charAt(i), lx, y0 + dy, px, Anim.alpha(0xFF1BD96A, a));
        }
        float sub = Anim.smoothstep(2.9f, 3.6f, t) * fadeOut * vis;
        if (sub > 0.02f) {
            String name = mc.getUser().getName();
            g.drawCenteredString(font, "C L I E N T   -   " + name, width / 2, y0 + 7 * px + 10, Anim.textAlpha(0xFFFFFFFF, sub));
        }

        // aviso para saltar
        float hint = Anim.smoothstep(1.5f, 2.2f, t) * (1f - Anim.smoothstep(4.8f, 5.4f, t)) * vis;
        if (hint > 0.02f && bh > 12) {
            g.drawCenteredString(font, "Pulsa cualquier tecla para saltar", width / 2, height - bh / 2 - 4,
                    Anim.textAlpha(0xFFB4BAC4, hint));
        }
    }
}
