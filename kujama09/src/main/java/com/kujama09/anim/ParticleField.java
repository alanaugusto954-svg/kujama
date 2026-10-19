package com.kujama09.anim;

import com.kujama09.config.KujamaConfig;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Campo de partículas flotantes y destellos. Las posiciones son normalizadas (0..1), por lo que
 * se adapta a cualquier resolución o cambio de tamaño de ventana. El movimiento usa el tiempo real
 * transcurrido (no un número fijo de fotogramas). No carga texturas: solo dibuja rectángulos.
 */
public final class ParticleField {
    private static final int[] PALETTE = {0xFFFFD43B, 0xFFFF9F1C, 0xFF4DABF7, 0xFFFFFFFF};

    private static final class Particle {
        float x;
        float y;
        float vx;
        float vy;
        float twinkleSpeed;
        float phase;
        int size;
        int color;
        boolean sparkle;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random(0x4B554A41L);
    private long lastNanos = System.nanoTime();
    private float time;

    public void render(DrawContext context, int width, int height, float intensity,
                       KujamaConfig.Quality quality, float alphaMultiplier) {
        int target = switch (quality) {
            case LOW -> 16;
            case MEDIUM -> 40;
            case HIGH -> 80;
        };
        while (particles.size() < target) {
            particles.add(spawn());
        }
        while (particles.size() > target) {
            particles.remove(particles.size() - 1);
        }

        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
        // Con intensidad 0 las partículas quedan quietas y sin parpadeo.
        time += dt * intensity;

        for (Particle p : particles) {
            p.x += p.vx * dt * intensity;
            p.y += p.vy * dt * intensity;
            if (p.y < -0.05f) {
                p.y = 1.05f;
                p.x = random.nextFloat();
            }
            if (p.x < 0f) p.x += 1f;
            if (p.x > 1f) p.x -= 1f;

            float twinkle = 0.5f + 0.5f * (float) Math.sin(time * p.twinkleSpeed + p.phase);
            float alpha = alphaMultiplier * (0.25f + 0.75f * twinkle);
            int color = Colors.withAlpha(p.color, alpha);
            if ((color >>> 24) < 4) {
                continue;
            }
            int px = Math.round(p.x * width);
            int py = Math.round(p.y * height);
            int s = p.size;
            context.fill(px, py, px + s, py + s, color);
            if (p.sparkle) {
                int arm = s * 2;
                int armColor = Colors.withAlpha(p.color, alpha * 0.7f);
                context.fill(px - arm, py + s / 2, px + s + arm, py + s / 2 + 1, armColor);
                context.fill(px + s / 2, py - arm, px + s / 2 + 1, py + s + arm, armColor);
            }
        }
    }

    private Particle spawn() {
        Particle p = new Particle();
        p.x = random.nextFloat();
        p.y = random.nextFloat();
        p.vx = (random.nextFloat() - 0.5f) * 0.02f;
        p.vy = -(0.01f + random.nextFloat() * 0.03f);
        p.twinkleSpeed = 1.5f + random.nextFloat() * 3f;
        p.phase = random.nextFloat() * 6.2831f;
        p.sparkle = random.nextInt(5) == 0;
        p.size = p.sparkle ? 2 : 1 + random.nextInt(3);
        p.color = PALETTE[random.nextInt(PALETTE.length)];
        return p;
    }
}
