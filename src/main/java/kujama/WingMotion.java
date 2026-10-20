package kujama;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Fisica simple de las alas, calculada una vez por tick con tu movimiento real:
 * quietas = se pliegan y respiran suave; caminando = se abren un poco; en el aire = aletean fuerte;
 * planeando con elitros = abiertas y casi fijas; agachado = pegadas a la espalda.
 */
public final class WingMotion {
    public static float phase, prevPhase;   // fase del aleteo (radianes)
    public static float open, prevOpen;     // apertura 0..1
    public static float amp, prevAmp;       // amplitud del aleteo 0..1

    private WingMotion() {}

    public static void tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) return;
        prevPhase = phase;
        prevOpen = open;
        prevAmp = amp;

        Vec3 v = p.getDeltaMovement();
        float horiz = (float) Math.hypot(v.x, v.z);
        boolean air = !p.onGround();
        boolean glide = p.isFallFlying();
        boolean sneak = p.isShiftKeyDown();

        float targetOpen;
        float targetAmp;
        float freq;
        if (glide) {
            targetOpen = 1.0f; targetAmp = 0.10f; freq = 0.10f;
        } else if (air) {
            targetOpen = 0.75f + Math.min(0.25f, (float) Math.abs(v.y)); targetAmp = 0.70f; freq = 0.55f;
        } else if (sneak) {
            targetOpen = 0.05f; targetAmp = 0.08f; freq = 0.10f;
        } else {
            targetOpen = Math.min(0.45f, 0.20f + horiz * 1.2f); targetAmp = Math.min(0.30f, 0.10f + horiz * 0.8f);
            freq = 0.14f + horiz * 0.9f;
        }
        open += (targetOpen - open) * 0.18f;
        amp += (targetAmp - amp) * 0.20f;
        phase += freq;
    }
}
