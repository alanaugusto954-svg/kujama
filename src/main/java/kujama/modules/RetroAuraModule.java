package kujama.modules;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.Item;

/**
 * Aura "retro" de 8 bits: octogonos de pixeles que suben en escalones, a 5 cuadros por segundo,
 * con paleta de pocos colores y todo alineado a una cuadricula. Cada tanto aparece una estrellita en cruz.
 */
public class RetroAuraModule extends ParticleCosmetic {
    private final int[] palette;

    public RetroAuraModule(String name, Item icon, int... palette) {
        super(name, icon);
        this.palette = palette;
    }

    /** Alinea a una cuadricula de 1/8 de bloque (efecto pixel). */
    private static double snap(double v) {
        return Math.round(v * 8.0) / 8.0;
    }

    @Override
    public void onTick() {
        if (!active()) return;
        ticks++;
        if (ticks % 4 != 0) return; // animacion de pocos cuadros

        int frame = ticks / 4;
        int step = frame % 10;
        double height = step * 0.22;
        double radius = (step % 2 == 0) ? 0.75 : 0.625;
        DustParticleOptions pixel = new DustParticleOptions(palette[frame % palette.length], 1.2f);
        double cx = mc.player.getX(), cy = mc.player.getY(), cz = mc.player.getZ();

        // octogono: 4 puntos en cruz + 4 diagonales
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4;
            double r = (k % 2 == 0) ? radius : radius * 0.75;
            emit(pixel, cx + snap(Math.cos(a) * r), cy + height, cz + snap(Math.sin(a) * r), 0, 0, 0, 4);
        }

        // estrellita en cruz sobre la cabeza, cada 5 cuadros
        if (frame % 5 == 0) {
            DustParticleOptions star = new DustParticleOptions(palette[(frame / 5) % palette.length], 1.4f);
            double[][] cross = {{0, 0}, {0.25, 0}, {-0.25, 0}, {0, 0.25}, {0, -0.25}};
            for (double[] o : cross) {
                emit(star, cx + o[0], cy + 2.35, cz + o[1], 0, 0, 0, 10);
            }
        }
    }
}
