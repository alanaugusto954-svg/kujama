package com.kujama09.screen;

import com.kujama09.anim.Colors;
import com.kujama09.anim.Easing;
import com.kujama09.anim.ParticleField;
import com.kujama09.config.KujamaConfig;
import com.kujama09.render.Backdrop;
import com.kujama09.render.LogoRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.client.input.KeyInput;

/**
 * Intro cinematográfica. Se muestra como una Screen normal, por lo que corre en el hilo de render
 * y nunca bloquea el hilo principal. La línea de tiempo se calcula con el reloj (System.nanoTime), no
 * con un número de fotogramas.
 *
 * Línea de tiempo (p = progreso 0..1):
 *   0.00-0.10  fundido desde negro        0.12-0.40  el logotipo aparece, escala y gana opacidad
 *   0.35-0.55  línea decorativa           0.45-0.75  brillo atraviesa el texto
 *   0.86-1.00  zoom final + fundido a negro, y se abre el menú principal
 */
public final class IntroScreen extends Screen {
    private final Screen next;
    private final ParticleField particles = new ParticleField();

    private long startNanos = -1L;
    private long skipNanos = -1L;
    private boolean soundPlayed;
    private boolean finished;

    public IntroScreen(Screen next) {
        super(Text.literal("kujama09"));
        this.next = next;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            if (skipNanos < 0L) {
                skipNanos = System.nanoTime();
            }
            return true;
        }
        return super.keyPressed(input);
    }

    /** Todo se dibuja en render(); se evita el fondo borroso/menú de vanilla. */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        KujamaConfig config = KujamaConfig.get();
        float intensity = config.animationIntensity;
        boolean effects = intensity > 0.01f;

        // Mientras la pantalla de carga de recursos de Minecraft siga visible, la intro espera.
        boolean overlayActive = this.client != null && this.client.getOverlay() != null;
        long now = System.nanoTime();
        if (startNanos < 0L && !overlayActive) {
            startNanos = now;
        }
        boolean started = startNanos >= 0L;

        float duration = config.introDurationSeconds;
        float elapsed = started ? (now - startNanos) / 1_000_000_000f : 0f;
        float skipProgress = skipNanos < 0L ? 0f : (now - skipNanos) / 1_000_000_000f / 0.35f;

        if ((started && elapsed >= duration) || skipProgress >= 1f) {
            context.fill(0, 0, this.width, this.height, Colors.BLACK);
            finish();
            return;
        }
        float p = Easing.clamp01(elapsed / duration);

        // --- Escenario ---
        float zoom = 1f + 0.07f * intensity * Easing.easeInOutCubic(p);
        Backdrop.drawScene(context, this.width, this.height, elapsed, zoom, intensity, config.quality, true);
        context.fill(0, 0, this.width, this.height, 0x55000A1E);
        Backdrop.drawParticles(context, particles, this.width, this.height, config, 1f);

        // --- Logotipo ---
        float appearP = Easing.clamp01((p - 0.12f) / 0.28f);
        float alpha = started ? Easing.smooth(appearP) : 0f;
        float logoScale = Math.max(2f, Math.min(7f, Math.min(this.width * 0.62f, 340f) / LogoRenderer.textWidth(this.textRenderer)));
        float scale = logoScale;
        if (effects) {
            scale *= Easing.lerp(1f, Easing.lerp(0.65f, 1f, Easing.easeOutBack(appearP)), intensity);
            scale *= 1f + 0.015f * intensity * (float) Math.sin(elapsed * 3f) * Easing.smooth((p - 0.4f) / 0.2f);
            scale *= 1f + 0.12f * intensity * Easing.easeInCubic((p - 0.86f) / 0.14f);
        }
        float shine = -1f;
        if (effects) {
            float shineP = (p - 0.45f) / 0.30f;
            if (shineP >= 0f && shineP <= 1f) {
                shine = Easing.easeInOutCubic(shineP);
            }
        }
        float cx = this.width / 2f;
        float cy = this.height / 2f;
        LogoRenderer.draw(context, this.textRenderer, cx, cy, scale, alpha, shine);

        // Línea decorativa bajo el logotipo.
        float lineP = effects ? Easing.easeOutCubic((p - 0.35f) / 0.20f) : Easing.smooth((p - 0.35f) / 0.20f);
        if (lineP > 0.01f) {
            float logoW = LogoRenderer.textWidth(this.textRenderer) * logoScale;
            int half = Math.round(logoW / 2f * lineP);
            int ly = Math.round(cy + 9f * logoScale / 2f + 6f * logoScale * 0.5f);
            context.fill(Math.round(cx) - half, ly, Math.round(cx) + half, ly + 2, Colors.withAlpha(Colors.ORANGE, alpha));
            context.fill(Math.round(cx) - half, ly + 2, Math.round(cx) + half, ly + 3, Colors.withAlpha(Colors.SKY_BLUE, alpha * 0.8f));
        }

        // --- Barras cinematográficas ---
        float barIn = Easing.easeOutCubic(p / 0.15f);
        float barOut = 1f - Easing.easeInCubic((p - 0.86f) / 0.14f);
        int barHeight = Math.round(this.height * 0.10f * barIn * barOut);
        if (barHeight > 0) {
            context.fill(0, 0, this.width, barHeight, Colors.BLACK);
            context.fill(0, this.height - barHeight, this.width, this.height, Colors.BLACK);
        }

        // --- Pista para omitir ---
        float hintAlpha = Easing.smooth((p - 0.10f) / 0.1f) * (1f - Easing.smooth((p - 0.8f) / 0.1f));
        Text hint = Text.translatable("kujama09.intro.skip");
        int hintColor = Colors.withAlpha(0xFFFFFFFF, hintAlpha * 0.65f);
        if ((hintColor >>> 24) >= 5) {
            context.drawTextWithShadow(this.textRenderer, hint,
                    this.width - this.textRenderer.getWidth(hint) - 6, this.height - 14, hintColor);
        }

        // --- Fundidos a negro (entrada, salida y omitir con ESC) ---
        float black = 0f;
        if (started) {
            black = Math.max(1f - Easing.smooth(p / 0.10f), Easing.smooth((p - 0.86f) / 0.14f));
        }
        black = Math.max(black, Easing.clamp01(skipProgress));
        if (black > 0.01f) {
            context.fill(0, 0, this.width, this.height, Colors.withAlpha(Colors.BLACK, black));
        }

        // --- Sonido opcional (desactivado por defecto) ---
        if (config.introSound && !soundPlayed && started && p >= 0.12f && this.client != null) {
            soundPlayed = true;
            this.client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.6f, 1.0f));
        }
    }

    private void finish() {
        if (finished || this.client == null) {
            return;
        }
        finished = true;
        this.client.setScreen(next);
    }
}
